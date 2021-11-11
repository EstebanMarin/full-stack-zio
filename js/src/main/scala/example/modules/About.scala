package example.modules
import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object About {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    div(
      className := "card",
      div(className := "card-header", "About"),
      div(
        className := "card-body",
        table(
          className := "table table-striped",
          tbody(
            tr(
              td("author"),
              td("@EstebanMarin")
            ),
            tr(
              td("github"),
              td(
                a(
                  target := "_blank",
                  href := "https://github.com/oen9/full-stack-zio",
                  "https://github.com/oen9/full-stack-zio"
                )
              )
            ),
            tr(
              td("api documentation (swagger)"),
              td(
                a(
                  target := "_blank",
                  href := "https://full-stack-zio.herokuapp.com/docs/index.html?url=/docs/docs.yaml",
                  "https://full-stack-zio.herokuapp.com/docs/index.html?url=/docs/docs.yaml"
                )
              )
            )
          )
        )
      )
    )
  }
}
