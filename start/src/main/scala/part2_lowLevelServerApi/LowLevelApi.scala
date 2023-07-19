//1. Crear un nuevo paquete
package part2_lowLevelServerApi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

import scala.concurrent.duration.DurationInt
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}

//2. Crear una nueva clase de tipo objeto
// 3. Hacer a extensión a App
object LowLevelApi extends  App {
  // 4. Crear el actor system y el actor materializing
  implicit val system = ActorSystem("LowLevelServerAPI")
  implicit val materializer = ActorMaterializer()
  //5. Importar el despachador de sistema de actores (contexto de ejecución que se puede usar en el futuro)

  import  system.dispatcher

  // 6. Crear el servidor fuente HTTP y con el metodo bind pasarle el nombre del host y el # de puerto
  val serverSource = Http().bind("localhost", 8000)
  //7. Agregar la forma en la que se manejaran las conexiones entrantes
  val connectionSink = Sink.foreach[IncomingConnection]{ connection =>
    println(s"Accepted incoming connection from: ${connection.remoteAddress}")
  }
  // 8. Crear el flujo de la conexiones entrantes
  val serverBindingFuture = serverSource.to(connectionSink).run()
  //9 Manejar las respuestas exitosas y fallidas, en desconexiones
  serverBindingFuture.onComplete{
    case Success(binding) =>
      println("Server binding successful")
      binding.terminate(2 seconds)
    case Failure(ex) => println(s"Server binding failed: ${ex}")
  }

  //10. RESPUESTAS DEL SERVIDOR:
  /*
  Metodo #1: synchronously serve HHTP responses
   */
  // 11. Crear la respuesta 200 para una solicitud get
  val requestHandler: HttpRequest => HttpResponse ={
    case HttpRequest(HttpMethods.GET, uri, headers, entity, protocol) =>
      HttpResponse(
        StatusCodes.OK, //HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
         <html>
          <body>
            Hello from Akka HTPP!
          </body>
         </html>
         """
            .stripMargin
        )
      )
    // 12. Crear la respuesta 404 para una solicitud get
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound, //404
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
         <html>
          <body>
            Oops! The resource can´t be find
          </body>
         </html>
         """
            .stripMargin
        )
      )
  }
  // 13. Implementar manejo sincrónico de conexiones HTTP
  //val httpSyncConnectionHandler = Sink.foreach[IncomingConnection]{ connection =>
  //  connection.handleWithSyncHandler(requestHandler)
  //}
  // 14. Ejecutar el servidor en el puerto 8080 con el Sink de manejo de conexiones HTTP sincrónicas
  // Http().bind("localhost", 8080).runWith(httpSyncConnectionHandler)

  // version corta, simplifica los pasos 13 y 14 en una linea:
  Http().bindAndHandleSync(requestHandler, "localhost", 8080)


}
