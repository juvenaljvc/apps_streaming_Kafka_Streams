import org.apache.kafka.streams.scala._

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._
import HelloWorld_KafkaStreams._
import schemas.{Facture, OrderLine}
import serdes.BytesSerDes

object CustomSerdesUsage extends App {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String
  import org.apache.kafka.streams.scala.Serdes.Integer

  implicit val bytesSerdes = new BytesSerDes[Facture]
  implicit val bytesOrdersSerdes = new BytesSerDes[OrderLine]

  val str : StreamsBuilder = new StreamsBuilder
  val kstr : KStream[String, Facture]  = str.stream[String, Facture]("streams_app")
  val kstrMaj : KStream[String, Int] = kstr.mapValues(v => v.qantite)
  kstrMaj.to("streams_app_upper")

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, getParams("localhost:9092"))
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }

}
