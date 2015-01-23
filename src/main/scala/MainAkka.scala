import java.io.PrintWriter
import java.net.URL

import akka.actor._
import akka.util.Timeout
import org.apache.commons.io.IOUtils

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global

object MainActor {
  case class Init(url: String, depth: Int)
  case class Resp(links: List[String])
}


class WorkerActor extends Actor {


  implicit val timeout = Timeout(100.seconds)

  import MainActor._
  def receive: Receive = {
    case Init(url, depth) =>
      val pattern = "<a href=\"([^\"]*)\"".r
      try {
        val links: List[String] = (pattern.findAllIn(IOUtils.toString(new URL(url))).matchData map (m => m.group(1))).map(
          x => if (x.contains("http://") || x.contains("https://")) x else url + (if ((url take (url.length - 1)) != "/")  "/" else "") + x
        ).toList

        if(depth == 0) {
          sender() ! links
        } else {
          val futures = for(link <- links) yield context.actorOf(Props[WorkerActor]) ? Init(link, depth - 1)
          val result = Await.result(Future.sequence(futures), timeout.duration).asInstanceOf[List[List[String]]]
          sender() ! (links :: result).flatten
        }
      } catch {
        case e: Exception => sender() ! List()
      }



  }
}

class MainActor extends Actor {
  import MainActor._
  import scala.util.{Success, Failure}

  def receive: Receive = {
    case Init(url, depth) =>
      implicit val timeout = Timeout(100.seconds)

      val worker = context.actorOf(Props[WorkerActor], "worker")
      val future = worker.ask(Init(url, depth)).mapTo[List[String]]
      future.onComplete {
        case Success(lista) =>
          val sortedResult = lista.groupBy(x => x).map(x => (x._1, x._2.length)).toList.sortBy(_._2)(Ordering[Int].reverse).mkString(System.lineSeparator())
          println(sortedResult)
          val pw = new PrintWriter("wynik.txt")
          pw.write(sortedResult)
          pw.close()
          context.system.shutdown()
        case Failure(_) => context.system.shutdown()
      }

  }
}

object MainAkka {

  import MainActor._

  def main(args: Array[String]): Unit = {
    if(args.length != 2) {
      println("Wywołanie run [url] [glebokosc]")
      return
    }
    val url = args(0)
    val depth = args(1).toInt
    val sys = ActorSystem("system")
    val main = sys.actorOf(Props[MainActor], "Main")
    main ! Init(url, depth)
  }

}
