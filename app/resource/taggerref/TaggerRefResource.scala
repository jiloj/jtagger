package resource.taggerref

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json, Writes}

/**
  *
  * @param id
  * @param name
  * @param created
  */
case class TaggerRefResource(id: Int, name: String, created: LocalDate)

/**
  *
  */
object TaggerRefResource {
  /**
    *
    */
  implicit val implicitWrites = new Writes[TaggerRefResource] {
    def writes(resource: TaggerRefResource): JsValue = {
      Json.obj(
        "id" -> resource.id,
        "name" -> resource.name
        "created" -> resource.created
      )
    }
  }
}
