package processors.statefull

import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.Serdes.String
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream, Produced}
import org.apache.kafka.streams.state.{KeyValueBytesStoreSupplier, KeyValueStore, StoreBuilder, Stores}
import schemas.Facture
import serdes.{JSONDeserializer, JSONSerializer}

import java.util
import java.util.Properties

object TransformValueProcessor extends App {

  implicit val factureSerdes : Serde[Facture] = Serdes.serdeFrom(new JSONSerializer[Facture], new JSONDeserializer)
  implicit val consumed : Consumed[String, Facture] = Consumed.`with`(Serdes.String(), factureSerdes)
  implicit val produced : Produced[String, Facture] = Produced.`with`(Serdes.String(), factureSerdes)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "transformValue5-processor")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val props_state : java.util.Map[String, String] = new util.HashMap[String, String]()
  props_state.put("retention.ms","172800000")
  props_state.put("retention.bytes", "10000000000")
  props_state.put("cleanup.policy", "compact")
 // props_state.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "10000")
 // props_state.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "10485760")
 // props_state.put(StreamsConfig.STATE_CLEANUP_DELAY_MS_CONFIG, "50000")

  val factureStoreName = "factureStore"
  val factureStoreSupplier : KeyValueBytesStoreSupplier = Stores.persistentKeyValueStore(factureStoreName)
  val factureStoreBuilder : StoreBuilder[KeyValueStore[String, String]] = Stores.keyValueStoreBuilder(factureStoreSupplier, String, String)
    .withCachingEnabled()
    .withLoggingEnabled(props_state)

 // val factureStore = factureStoreBuilder.build()

  val str : StreamsBuilder = new StreamsBuilder
  str.addStateStore(factureStoreBuilder)

  val kstrFacture : KStream[String, Facture]  = str.stream[String, Facture]("factureJson")(consumed)

  val kstrDesc : KStream[String, String]   = kstrFacture
    .mapValues(p => p.orderline.productid)
    .transformValues(new FactureTransformerSupplier, factureStoreName)

  kstrDesc.print(Printed.toSysOut().withLabel("Description du produit"))

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()


}
