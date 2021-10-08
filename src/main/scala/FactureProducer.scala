import java.util.Properties
import org.apache.kafka.clients.producer.{ProducerRecord, _}
import serdes._
import schemas._

object FactureProducer extends App {

  val props = new Properties()
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer" )
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[JSONSerializer[Facture]])
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val facture1  = List(Facture("a320", "téléviseur LG 3A Nano", 3, 3350.75, OrderLine("34e", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10)),
    Facture("a321", "téléviseur LG", 3, 3350.75, OrderLine("34e", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10)),
    Facture("a322", "téléviseur LG 3A Nano", 3, 3350.75, OrderLine("34a", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10)),
    Facture("a323", "téléviseur LG 3A", 1, 3350.75, OrderLine("34e", "45i", "21/09/2010", "20/09/2010", 15.00, 700, 10)),
    Facture("a324", "téléviseur LG Nano", 2, 3350.75, OrderLine("34I", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10)))

  val factureProducer = new KafkaProducer[String, Facture](props)

  facture1.foreach{
    e => factureProducer.send(new ProducerRecord[String, Facture]("factureJson", e.factureid, e))
      Thread.sleep(3000)
  }

  println("rajout effectué avec succès !")

}
