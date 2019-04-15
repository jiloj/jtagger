package resource.tagger

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json, Writes}

/**
  * A tagger resource to display to jtagger clients.
  *
  * @param id The id of the tagger.
  * @param name The name of the tagger.
  * @param created The time the tagger was created.
  */
case class TaggerResource(id: Int, name: String, created: LocalDate)

/**
  * Companion object to the Tagger resource.
  */
object TaggerResource {

  /**
    * Defines the implicit conversion from Tagger to the resource object.
    */
  implicit val implicitWrites = new Writes[TaggerResource] {
    def writes(resource: TaggerResource): JsValue = {
      Json.obj(
        "id" -> resource.id,
        "name" -> resource.name,
        "created" -> resource.created
      )
    }
  }
}
