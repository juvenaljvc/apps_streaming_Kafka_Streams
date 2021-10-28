import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._
import schemas.{Facture, OrderLine}
import serdes.{JSONDeserializer, JSONSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.Printed

import java.time.Duration
import java.util.Properties



object KTableComputations extends App {
  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes._

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ktable-print1")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val str : StreamsBuilder = new StreamsBuilder
  val ktblTest : KTable[String, String]  = str.table("ktabletest", Materialized.as("STATE-STORE-STR"))

  // val kTble2 : KTable[String, String]  = str.table("ktabletest", Materialized.as("STATE-STORE-STR"))
  ktblTest.toStream.print(Printed.toSysOut().withLabel("Clé/Valeur du KTable"))

  val ks = ktblTest.toStream

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread{
    kkStream.close()
  }



}
