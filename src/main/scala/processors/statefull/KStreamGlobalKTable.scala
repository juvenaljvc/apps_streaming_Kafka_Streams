package processors.statefull
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology, scala}
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.kstream.GlobalKTable
import schemas.{Commande, CommandeComplet, DetailsCommande}
import serdes.{JSONDeserializerCmdComplet, JSONDeserializerCommandes, JSONDeserializerDtlCommandes, JSONSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}

import java.time.Duration
import java.util.Properties

// jointure KTStream-KTable/GlobalKTable
object KStreamGlobalKTable  extends App {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  implicit val jsonSerdesCommandes : Serde[Commande] = Serdes.serdeFrom(new JSONSerializer[Commande], new JSONDeserializerCommandes)
  implicit val jsonSerdesDetailsCommandes : Serde[DetailsCommande] = Serdes.serdeFrom(new JSONSerializer[DetailsCommande], new JSONDeserializerDtlCommandes)
  implicit val jsonSerdesCommandesComplet : Serde[CommandeComplet] = Serdes.serdeFrom(new JSONSerializer[CommandeComplet], new JSONDeserializerCmdComplet)

  implicit val consumedCommandes : Consumed[String, Commande] = Consumed.`with`(String, jsonSerdesCommandes)
  implicit val consumedDetailsCommandes: Consumed[String, DetailsCommande] = Consumed.`with`(String, jsonSerdesDetailsCommandes)

  implicit val produced : Produced[String, CommandeComplet] = Produced.`with`(String, jsonSerdesCommandesComplet)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-globalktable-join")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val str : StreamsBuilder = new StreamsBuilder
  val kgCommande : GlobalKTable[String, Commande]  = str.globalTable[String, Commande]("commande")(consumedCommandes)
  val ktDtlCommande : KStream[String, DetailsCommande]  = str.stream[String, DetailsCommande]("DetailsCommande")(consumedDetailsCommandes)

  val ktjoin = ktDtlCommande.join(kgCommande)(
    (_, d : DetailsCommande) => d.productid,     // key du nouveau KStream (message key)
    (d : DetailsCommande, c : Commande) =>
    {
      CommandeComplet(d.orderid, d.productid, d.shipdate, d.billdate, d.unitprice, d.numunits, d.totalprice, c.city, c.state)
    }
  )


  ktjoin.to("commandeComplet")(produced)

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }


}

