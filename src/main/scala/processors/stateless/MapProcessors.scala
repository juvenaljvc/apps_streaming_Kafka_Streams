package processors.stateless

import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._
import schemas.{Facture, OrderLine}
import serdes.{JSONDeserializer, JSONSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}

import java.util.Properties


object MapProcessors extends App {

  implicit val jsonSerdes : Serde[Facture] = Serdes.serdeFrom[Facture](new JSONSerializer[Facture], new JSONDeserializer)
  implicit val consumed : Consumed[String, Facture] = Consumed.`with`(Serdes.String(), jsonSerdes)
  implicit val produced : Produced[String, Facture] = Produced.`with`(Serdes.String(), jsonSerdes)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-processor")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")


  val str : StreamsBuilder = new StreamsBuilder
  val kstrFacture : KStream[String, Facture]  = str.stream[String, Facture]("factureJson")
  // utilisation d'un MapValue()
  val kstrTotal : KStream[String, Double] = kstrFacture.mapValues(f => f.orderline.numunits * f.orderline.unitprice)

  // utilisation d'un Map() - cause les données à être marqué pour re-partionnement
  val kstrTotal2 : KStream[String, Double] = kstrFacture.map((k, f) => (k.substring(2), f.orderline.numunits * f.orderline.unitprice ))

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }

}
