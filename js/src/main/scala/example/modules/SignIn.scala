package example.modules

import cats.implicits._
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import example.bridges.reactrouter.NavLink
import example.services.AppCircuit
import example.services.ReactDiode
import example.services.TryAuth
import example.services.Validator
import org.scalajs.dom.{Event, html}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._
import slinky.core.facade.ReactElement

@react object SignIn {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (auth, dispatch) = ReactDiode.useDiode(AppCircuit.zoom(_.auth))
    val (username, setUsername) = useState("foo")
    val (password, setPassword) = useState("bar")
    val (errorMsgs, setErrorMsgs) = useState(Vector[String]())

    def handleUsername(e: SyntheticEvent[html.Input, Event]): Unit = setUsername(e.target.value)
    def handlePassword(e: SyntheticEvent[html.Input, Event]): Unit = setPassword(e.target.value)

    def signIn(tryAuth: TryAuth) = {
      dispatch(tryAuth)
      setUsername("")
      setPassword("")
      setErrorMsgs(Vector())
    }

    def handleSingIn() = {
      Validator
        .validateTryAuth(username, password)
        .fold(setErrorMsgs, signIn)
    }

    def signInForm() = Fragment(
      div(className := "input-group mb-3",
        div(className := "input-group-prepend",
          span(className := "input-group-text", "username", id := "form-username-label")
        ),
        input(`type` := "text",
          className := "form-control",
          placeholder := "Username",
          aria-"label" := "Username",
          aria-"describedby" := "form-username-label",
          value := username,
          onChange := (handleUsername(_))
        )
      ),
      div(className := "input-group mb-3",
        div(className := "input-group-prepend",
          span(className := "input-group-text", "password", id := "form-password-label")
        ),
        input(`type` := "password",
          className := "form-control",
          placeholder := "Password",
          aria-"label" := "Password",
          aria-"describedby" := "form-password-label",
          value := password,
          onChange := (handlePassword(_))
        )
      ),
      errorMsgs.zipWithIndex.map { case (msg, idx) =>
        div(key := idx.toString,className := "alert alert-danger", role := "alert", msg)
      },
      div(className := "row",
        div(className := "col",
          button(className := "btn btn-secondary", "Sign In", onClick := handleSingIn _),
        ),
        div(className := "col text-right",
          NavLink(exact = true, to = MainRouter.Loc.register)("register")
        )
      )
    )

    div(className := "card",
      div(className := "card-header", "Sign In"),
      div(className := "card-body",
        h5(className := "card-title",
          auth.state match {
            case PotPending =>
              div(className := "spinner-border text-primary", role := "status",
                span(className := "sr-only", "Loading...")
              )
            case PotFailed =>
                auth.exceptionOption.fold("unknown error")(msg => " error: " + msg.getMessage())
            case PotReady =>
              auth.fold("unknown error")(a => s"Logged as ${a.username.toString}")
            case _ =>
              "Sign In!"
          },
        ),

        auth.state match {
          case PotReady => none[ReactElement]
          case _ => signInForm().some
        },

      )
    )
  }
}
