package processors.statefull

import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import schemas.Facture
import serdes.{JSONDeserializer, JSONSerializer}

import java.util.Properties


object ReduceProcessorKTable extends App {

  import org.apache.kafka.streams.scala.Serdes.{String, _}

  implicit val jsonSerdes : Serde[Facture] = Serdes.serdeFrom(new JSONSerializer[Facture], new JSONDeserializer)
  implicit val consumed : Consumed[String, Facture] = Consumed.`with`(Serdes.String(), jsonSerdes)
  implicit val produced : Produced[String, Facture] = Produced.`with`(Serdes.String(), jsonSerdes)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "reduce-processor-ktable")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val str : StreamsBuilder = new StreamsBuilder
  val kstrFacture : KTable[String, Facture]  = str.table[String, Facture]("factureJson")

  val kCA = kstrFacture.mapValues(f => f.total)
    .groupBy((k, v) => (k, v))(Grouped.`with`(String, Double))
    .reduce(
      (aggValue, currentValue) => aggValue + currentValue,
      (aggValue, oldValue) => aggValue - oldValue
    )(Materialized.as("ReduceStoreKT")(String, Double))


  kCA.toStream.print(Printed.toSysOut().withLabel("Chiffre d'affaire global"))


  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }


}
