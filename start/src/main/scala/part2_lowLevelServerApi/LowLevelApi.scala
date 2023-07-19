package part2_lowLevelServerApi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

import scala.concurrent.duration.DurationInt
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}

import scala.concurrent.Future

object LowLevelApi extends  App {
  implicit val system = ActorSystem("LowLevelServerAPI")
  implicit val materializer = ActorMaterializer()

  import  system.dispatcher


  val serverSource = Http().bind("localhost", 8000)
  val connectionSink = Sink.foreach[IncomingConnection]{ connection =>
    println(s"Accepted incoming connection from: ${connection.remoteAddress}")
  }
  val serverBindingFuture = serverSource.to(connectionSink).run()

  serverBindingFuture.onComplete {
    case Success(binding) =>
      println("Server binding successful")
      binding.terminate(2 seconds)
    case Failure(ex) => println(s"Server binding failed: ${ex}")
  }

  //RESPUESTAS DEL SERVIDOR:
  /*
  Metodo #1: synchronously serve HHTP responses
   */

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

  //val httpSyncConnectionHandler = Sink.foreach[IncomingConnection]{ connection =>
  //  connection.handleWithSyncHandler(requestHandler)
  //}

  // Http().bind("localhost", 8080).runWith(httpSyncConnectionHandler)

  // version corta, simplifica los pasos 13 y 14 en una linea:
  Http().bindAndHandleSync(requestHandler, "localhost", 8080)

  /*
  Metodo #2: serve back HTTP response Asynchronously
   */
  // 15.
  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), headers, entity, protocol) =>
      Future(HttpResponse(
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
      ))

    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(
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
      ))
  }
  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
      connection.handleWithAsyncHandler(asyncRequestHandler)
  }
  // stream-based "manual" version
  Http().bind("localhost", 8081).runWith(httpAsyncConnectionHandler)

  //Shorthand version
  Http().bindAndHandleAsync(asyncRequestHandler, "localhost", 8081)

}
