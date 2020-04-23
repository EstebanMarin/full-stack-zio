package example.modules.services.auth

import example.model.Errors.AuthenticationError
import example.model.Errors.TokenNotFound
import example.model.Errors.UserExists
import example.model.SqlData
import example.modules.db.userRepository.UserRepository
import example.modules.services.auth.authService.AuthService
import example.shared.Dto
import io.scalaland.chimney.dsl._
import org.mindrot.jbcrypt.BCrypt
import org.reactormonk.CryptoBits
import org.reactormonk.PrivateKey
import zio._
import zio.clock.Clock
import zio.logging.Logger
import zio.logging.LogLevel

class AuthServiceLive(
  userRepository: UserRepository.Service,
  logger: Logger,
  clock: Clock.Service
) extends AuthService.Service {

  val key = PrivateKey(scala.io.Codec.toUTF8("secret-key")) // TODO
  val crypto = CryptoBits(key)
  val testUsername = "test"

  def getUser(token: Dto.Token): Task[Dto.User] = for {
    maybeUser <- userRepository.getUserByToken(token)
    user <- ZIO.fromOption(maybeUser).flatMapError(_ => tokenNotFoundError("getUser", token))
    dtoUser = toDto(user)
    _ <- logger.log(LogLevel.Trace)(s"getUser: $dtoUser")
  } yield dtoUser

  def generateNewToken(cred: Dto.AuthCredentials): Task[Dto.User] = for {
    maybeUser <- userRepository.getUserByName(cred.name)
    maybeAuthenticatedUser = maybeUser.filter(u => BCrypt.checkpw(cred.password, u.password))
    user <- ZIO
      .fromOption(maybeAuthenticatedUser)
      .flatMapError(_ => authenticationError("generateNewToken", cred.name))
    newToken <- generateToken(crypto, user.name).provideLayer(ZLayer.succeed(clock))
    updatedUser <- userRepository.updateToken(user.id.get, newToken)

    dtoUser = toDto(updatedUser)
    _ <- logger.log(LogLevel.Trace)(s"generateNewToken: from: '${user.token}' to '$dtoUser'")
  } yield dtoUser

  def createUser(cred: Dto.AuthCredentials): Task[Dto.User] = for {
    newToken <- generateToken(crypto, cred.name).provideLayer(ZLayer.succeed(clock))
    hashedPasswd = BCrypt.hashpw(cred.password, BCrypt.gensalt())
    toInsert = cred
      .into[SqlData.User]
      .withFieldComputed(_.password, c => hashedPasswd)
      .withFieldComputed(_.token, _ => newToken)
      .transform
    maybeInsertedUser <- userRepository.insert(toInsert)
    insertedUser <- ZIO
      .fromOption(maybeInsertedUser)
      .flatMapError(_ => userExistsError("createUser", cred.name))

    newUser = toDto(insertedUser)
    _ <- logger.log(LogLevel.Trace)(s"createUser: $newUser")
  } yield newUser

  def secretText(user: Dto.User): Task[String] =
    ZIO.succeed(s"This is super secure message for ${user.name}!")

  def generateToken(crypto: CryptoBits, s: String): ZIO[Clock, Throwable, Dto.Token] = {
    clock.nanoTime.map(nanos =>
      if (s == testUsername) testUsername
      else crypto.signToken(s, nanos.toString())
    )
  }

  def toDto(sqlUser: SqlData.User): Dto.User =
    sqlUser
      .into[Dto.User]
      .withFieldComputed(_.id, _.id.getOrElse(0L))
      .transform

  def tokenNotFoundMsg(t: Dto.Token) = s"Token '$t' not found"
  def userExistsMsg(name: String) = s"User '$name' probably already exists"
  def unauthMsg(name: String) = s"Wrong name/password for user '$name'"

  def tokenNotFoundError(errName: String, token: String) = {
    val msg = tokenNotFoundMsg(token)
    logger.log(LogLevel.Trace)(s"$errName: $msg") as TokenNotFound(msg)
  }

  def authenticationError(errName: String, username: String) = {
    val msg = unauthMsg(username)
    logger.log(LogLevel.Trace)(s"$errName: $msg") as AuthenticationError(msg)
  }

  def userExistsError(errName: String, username: String) = {
    val msg = userExistsMsg(username)
    logger.log(LogLevel.Trace)(s"createUser: $msg") as UserExists(msg)
  }

}
