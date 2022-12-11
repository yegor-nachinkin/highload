package ltd.very

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

object Bomber {
  def main(args: Array[String]){
      
      implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
      
      val params = new Array[String](4)
      params(0) = "5" // number of threads
      if(args.length > 0) params(0) = args(0)
      params(1) = "10" // number of requests per thread
      if(args.length > 1) params(1) = args(1)
      params(2) = "5" // number of milliseconds to wait between requests
      if(args.length > 2) params(2) = args(2)
      params(3) = "http://localhost:9000/reverse/abcd" // address to bomb
      if(args.length > 3) params(3) = args(3)
      
      val ai = new AtomicInteger(0)
      val t1 = System.currentTimeMillis

      for(i <- 0 until params(0).toInt){
          val f = Future { 
            println("future " + i.toString + " started")  
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder().uri(URI.create(params(3))).header("Content-Type", "text/plain").build()
            for(z <- 0 until params(1).toInt){
                println("request # " + i.toString + ":" + z.toString)
                //client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).thenApply(_.body).thenAccept(println(_))
                val s = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body.replace("\n","")
                ai.incrementAndGet()
                println(s + " (" + ai.get.toString + ")")
                if(params(2).toInt > 0)Thread.sleep(params(2).toInt)
            } 
          }
          f.onComplete{
            case Success(x) => {println("success " + i.toString)}
            case Failure(e) => {println("failure! " + e)}
          }
      }

      while(ai.get < params(0).toInt * params(1).toInt){
        Thread.sleep(500)
        println("Response count: " + ai.get.toString)
      }
      
      val t2 = System.currentTimeMillis
      val avgT = (t2 - t1) / ai.get
      println("Average request / response time (ms): " + avgT.toString)
      
  }
}

