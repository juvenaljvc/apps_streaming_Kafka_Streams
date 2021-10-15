package processors.stateless

import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._
import schemas.{Facture, OrderLine}
import serdes.{JSONDeserializer, JSONSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.Printed

import java.util.Properties

object BranchMergeProcessor extends App {

  implicit val jsonSerdes : Serde[Facture] = Serdes.serdeFrom[Facture](new JSONSerializer[Facture], new JSONDeserializer)
  implicit val consumed : Consumed[String, Facture] = Consumed.`with`(Serdes.String(), jsonSerdes)
  implicit val produced : Produced[String, Facture] = Produced.`with`(Serdes.String(), jsonSerdes)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "branch-processor")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val str : StreamsBuilder = new StreamsBuilder
  val kstrFacture : KStream[String, Facture]  = str.stream[String, Facture]("factureJson")
  val kstrTotal : KStream[String, Double] = kstrFacture.mapValues(f => f.orderline.numunits * f.orderline.unitprice)

  val kstBranch : Array[KStream[String, Double]] = kstrTotal.branch(
    (_, t) => t >= 10000,
    (_, t) => t > 5000,
    (_, t) => t > 2000,
    (_, t) => t <= 2000
  )

  val kstr2K = kstBranch(3)
  kstr2K.foreach{ (k, m) =>
    println(s"factures avec un total inférieur ou égal à 2K : ${k} => ${m}")
  }

  // méthode 2
  val liste_predicats : List[(String, Double) => Boolean] = List(
    (_, t) => t >= 10000,
    (_, t) => t > 5000,
    (_, t) => t > 2000,
    (_, t) => t <= 2000
  )

  val kstBranch2 : Array[KStream[String, Double]] = kstrTotal.branch(liste_predicats:_*)
  val kstr10K = kstBranch(0)

  val kMerge = kstr2K.merge(kstr10K.merge(kstBranch(1)))
  // kstr2K => kstr10K => kstr5K

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

}
