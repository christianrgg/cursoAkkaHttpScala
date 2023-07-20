package part2_lowLevelServerApi

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.stream.ActorMaterializer
import part2_lowLevelServerApi.GuitarDB.FindAllGuitars

// 8. Importar toda la libreria spray para clasificación de datos json
import spray.json._

// 2.Definir la estructura de la guitarra: Parametros
case class Guitar(make:String, model:String)

//4. Definir mensajes para interactuar con la base de datos
object GuitarDB {
  case class CreateGuitar(guitar: Guitar)
  case class GuitarCreated(id:Int)
  case class FindGuitar(id:Int)
  case object FindAllGuitars
}


// 3. Definir subclase de actor para facilitar el registro (logging) de mensajes en un actor.
class  GuitarDB extends Actor with ActorLogging {
  // 5. Importar al compañero
  import GuitarDB._

  // 6. Declarar variables mutable, una para almacenar guitarras e inicializarla un mapa vacio y otra variable de seguimiento para dar un id a las nuevas guitarras creadas
  var guitars: Map[Int, Guitar] = Map()
  var currentGuitarId: Int = 0

  // 7. Definir las funciones para los mensajes de interaccion con la base de datos
  override def receive: Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars")
      sender() ! guitars.values.toList

    case FindGuitar(id) =>
      log.info(s"Searching guitar by id: $id")
      sender() ! guitars.get(id)

    case CreateGuitar(guitar) =>
      log.info(s"Adding guitar $guitar with $currentGuitarId")
      guitars = guitars + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1

  }

}

// 9. Hacer la  serialización y deserialización JSON para la case class "Guitar" utilizando el DefaultJsonProtocol
trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  //10. Generar formato JSON de serialización y deserialización JSON para una case class con dos parámetros.
  implicit val guitarFormat = jsonFormat2(Guitar)
}

// 11. extender objecto con GuitarStoreJsonProtocol
object LowLevelRest  extends  App with GuitarStoreJsonProtocol{
  // 1. Definir valores implicitos de alcance local
  implicit  val system = ActorSystem("LowLevelRest")
  implicit val materializer = ActorMaterializer
  import system.dispatcher

  /*
  GET on localhost:8080/api/guitar => ALL the guitars in the store
  POST on localhost:8080/api/guitar => insert the guitar into the store
   */
  //JSON -> marshalling
  // 12 . Crear una nueva guitarra utilizando la case class "Guitar" y almacena esa guitarra en la variable "simpleGuitar".
  val simpleGuitar = Guitar("Fender", "Stratocaster")
  println(simpleGuitar.toJson.prettyPrint)


  // 13. Unmarshalling
  val simpleGuitarJsonString =
    """
      |{
      |  "make": "Fender",
      |  "model": "Stratocaster"
      |}
      |""".stripMargin

  println(simpleGuitarJsonString.parseJson.convertTo[Guitar])

}
