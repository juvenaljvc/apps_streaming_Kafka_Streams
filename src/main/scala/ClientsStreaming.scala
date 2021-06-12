
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.protocol
import org.apache.kafka.common.serialization._
import org.apache.kafka.common._
import org.apache.kafka.common.security.auth.SecurityProtocol
import java.util.Properties
import java.util.Collections
import scala.collection.JavaConverters._
import org.apache.kafka.clients.producer.{ProducerRecord, _}
import org.apache.kafka.clients.producer.ProducerConfig._
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode


object ClientsStreaming {


  def main(args: Array[String]): Unit = {


    val producer_Kafka = new KafkaProducer[String, String](getKafkaProducerParams_prod("localhost:9092"))

    val record_publish1 =  getJSON("localhost:9092", "orderline")
    val record_publish2 =  getJSON("localhost:9092", "detailsOrderline")

    producer_Kafka.initTransactions()
    producer_Kafka.beginTransaction()
    try {
      for( i <- 1 to 10 ) {
        producer_Kafka.send(record_publish1)
        producer_Kafka.send(record_publish2)
      }
    producer_Kafka.commitTransaction()
    } catch {
      case ex : Exception =>
        producer_Kafka.abortTransaction()
        println("transaction avortée")
    }


  }

  def producerKafka_prod(KafkaBootStrapServers : String, topic_name : String) : Unit = {

    val producer_Kafka = new KafkaProducer[String, String](getKafkaProducerParams_prod(KafkaBootStrapServers))

    val record_publish =  getJSON(KafkaBootStrapServers : String, topic_name : String)

    producer_Kafka.initTransactions()

    producer_Kafka.beginTransaction()

    try {

      producer_Kafka.send(record_publish, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          if (exception == null) {
            //le message a été enregistré dans Kafka sans problème.
            println("offset du message : " + metadata.offset().toString)
            println("topic du message : " + metadata.topic().toString())
            println("partition du message : " + metadata.partition().toString())
            println("heure d'enregistrement du message : " + metadata.timestamp())
          }
        }
      })
      producer_Kafka.commitTransaction()
      println("message publié avec succès ! :)")
    } catch {
      case ex : Exception =>
        producer_Kafka.abortTransaction()
        println(s"erreur dans la publication du message dans Kafka ${ex.printStackTrace()}")
        println("La liste des paramètres pour la connexion du Producer Kafka sont :" + getKafkaProducerParams_prod(KafkaBootStrapServers))
    }

  }


  def getKafkaProducerParams_prod (KafkaBootStrapServers : String) : Properties = {

    val props : Properties = new Properties()
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaBootStrapServers)
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put("min.insync.replica", "2")
    props.put(ProducerConfig.RETRIES_CONFIG, 2)   // sémantique au moins une fois.

    props.put(ProducerConfig.RETRIES_CONFIG, 0)   // sémantique au plus une fois.

    // activation des transactions sous reserve que "min.insync.replica" >= 2 et replication factor >=3
    props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transaction_1")
    return props

  }

  def getJSON(KafkaBootStrapServers : String, topic_name : String) : ProducerRecord[String, String] = {

    val objet_json = JsonNodeFactory.instance.objectNode()

    val price : Int = 45

    objet_json.put("orderid", "10")
    objet_json.put("customerid", "150")
    objet_json.put("campaignid", "30")
    objet_json.put("orderdate", "10/04/2020")
    objet_json.put("city", "Paris")
    objet_json.put("state", "RAS")
    objet_json.put("zipcode", "75000")
    objet_json.put("paymenttype", "CB")
    objet_json.put("totalprice", price)
    objet_json.put("numorderlines", 200)
    objet_json.put("numunit",10)

    return  new ProducerRecord[String, String](topic_name,objet_json.toString)

  }

}
