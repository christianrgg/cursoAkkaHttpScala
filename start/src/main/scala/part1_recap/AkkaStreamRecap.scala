package part1_recap

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.Success
import scala.util.Failure


object AkkaStreamRecap extends App {
  implicit val system = ActorSystem("AkkaStreamsRecap")
  implicit  val materializer = ActorMaterializer()

  val source = Source(1 to 100)
  val sink = Sink.foreach[Int](println)
  val flow = Flow[Int].map(x=> x+1)

  val runnableGraph = source.via(flow).to(sink)
  val simpleMaterializedValue = runnableGraph
    // .run() // materialization

  // MATERIALIZED VALUE
  val sumSink = Sink.fold[Int, Int](0)((currentSum, element)=> currentSum + element)
  val sumFuture = source.runWith(sumSink)

  import system.dispatcher
  sumFuture.onComplete {
    case Success(sum) => println(s"The sum of all the numbers from the simple source is: $sum")
    case Failure(ex) => println(s"Summing all the numbers from the simple source FAILED: $ex")


  val anotherMaterializedValue = source.viaMat(flow)(Keep.right).toMat(sink)(Keep.left)
  /*
  1. Materializing a graph means materializing ALL the components
  2. a materializing value can be ANYTHING AT ALL
   */

  /*
  -buffer a elements
  - apply a strategy in case the buffer overflows
  - fail the entire stream
   */

  val bufferedFlow = Flow[Int].buffer(10,OverflowStrategy.dropHead)
      source.async
        .via(bufferedFlow).async
        .runForeach{e=>
          //a slow consumer
          Thread.sleep(100)
          println(e)
        }
  }
}
