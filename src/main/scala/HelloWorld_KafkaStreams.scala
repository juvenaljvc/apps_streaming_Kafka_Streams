import org.apache.kafka.streams.scala._

import java.util.Properties
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._

object HelloWorld_KafkaStreams {

  def main(args: Array[String]): Unit = {

    import org.apache.kafka.streams.scala.ImplicitConversions._
    import org.apache.kafka.streams.scala.Serdes.String
    import org.apache.kafka.streams.scala.Serdes._

    val str : StreamsBuilder = new StreamsBuilder
    val kstr : KStream[String, String]  = str.stream[String, String]("streams_app")
    val kstrMaj : KStream[String, String] = kstr.mapValues(v => v.toUpperCase)
    kstrMaj.to("streams_app_upper")

    val topologie : Topology = str.build()
    val kkStream : KafkaStreams = new KafkaStreams(topologie, getParams("localhost:9092"))
    kkStream.start()

    sys.ShutdownHookThread{
      kkStream.close()
    }

  }

  def getParams(bootStrapServer : String) : Properties = {

    val props : Properties = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "hello world")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer)
    // props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.streams.scala.Serdes.String")
    // props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.streams.scala.Serdes.String")
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    props.put("auto.offset.reset.config", "latest")
    // props.put("consumer.group.id", "hello world")

    props

  }


}
