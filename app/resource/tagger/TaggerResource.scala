package resource.tagger

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json, Writes}

/**
  *
  * @param id
  * @param name
  * @param created
  */
case class TaggerResource(id: Int, name: String, created: LocalDate)

/**
  *
  */
object TaggerResource {
  /**
    *
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
