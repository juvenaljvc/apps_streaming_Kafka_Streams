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
import processors.stateless.CustomPartitioner

object WriteProcessors extends App {

  import org.apache.kafka.streams.scala.Serdes._
  implicit val jsonSerdes : Serde[Facture] = Serdes.serdeFrom[Facture](new JSONSerializer[Facture], new JSONDeserializer)
  implicit val consumed : Consumed[String, Facture] = Consumed.`with`(String, jsonSerdes)
  implicit val produced : Produced[String, Facture] = Produced.`with`(String, jsonSerdes)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "group-processor")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val str : StreamsBuilder = new StreamsBuilder
  val kstrFacture : KStream[String, Facture]  = str.stream[String, Facture]("factureJson")
  val kstrTotal : KStream[String, Double] = kstrFacture.mapValues(f => f.orderline.numunits * f.orderline.unitprice)

  // selectKey()
  val newKeys = kstrTotal.selectKey((k, t) => k.toUpperCase())
  newKeys.print(Printed.toSysOut().withLabel("Select keys"))

  // GroupByKey()
  val kstGroupKeys = newKeys.groupByKey(Grouped.`with`(String, Double))

  // GroupBy()
  val kstGroupBy = newKeys.groupBy((k, v) => v)(Grouped.`with`(Double, Double))

  // écriture  dans un topic Kafka existant - opération finale
  newKeys.to("topic-test")(Produced.`with`(String, Double))

  // écriture  dans un topic Kafka existant - opération non-finale
  val t = newKeys.through("topic-test")(Produced.`with`(String, Double))

  // écriture  dans un topic via un custom partitionner
  val t2 = newKeys.through("topic-test")(Produced.`with`(new CustomPartitioner))

  // transformation du KStream en KTable
  val kTable = str.table[String, Facture]("factureJson")(Consumed.`with`(String, jsonSerdes))

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

}
