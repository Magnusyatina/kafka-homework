package com.magnusario
package com.magnusario

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

object Application extends App {
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val materializer: SystemMaterializer = SystemMaterializer(system)

  val source = Source(1 to 5)

  val multiplyBy10 = Flow[Int].map(_ * 10)
  val multiplyBy2 = Flow[Int].map(_ * 2)
  val multiplyBy3 = Flow[Int].map(_ * 3)

  val sink = Sink.foreach(println)

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val bcast = builder.add(Broadcast[Int](3))
    val merge = builder.add(ZipWith((a: Int, b: Int, c: Int) => a + b + c))

    source ~> bcast ~> multiplyBy10 ~> merge.in0
              bcast ~> multiplyBy2  ~> merge.in1
              bcast ~> multiplyBy3  ~> merge.in2
              merge.out ~> sink

    ClosedShape
  })

  g.run()

}
