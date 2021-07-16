import org.apache.kafka.streams.scala._

import java.util.Properties
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._
import java.time.Duration

object WordCountStream {

  def main(args: Array[String]): Unit = {

    import org.apache.kafka.streams.scala.ImplicitConversions._
    import org.apache.kafka.streams.scala.Serdes.String
    import org.apache.kafka.streams.scala.Serdes._

    val str : StreamsBuilder = new StreamsBuilder
    val kstr : KStream[String, String]  = str.stream[String, String]("streams_app")
    val kstrMaj : KStream[String, String] = kstr.mapValues(v => v.toUpperCase)
    val kCount : KTable[String, Long] = kstrMaj.flatMapValues( r => r.split(","))
      .map((_, v) => (v, 1))
      .groupByKey
      .count()(Materialized.as("counts-store"))       // state store

    kCount.toStream.to("streams_count")

    val topologie : Topology = str.build()
    val kkStream : KafkaStreams = new KafkaStreams(topologie, getParams("localhost:9092"))
    kkStream.start()

    sys.ShutdownHookThread{
      kkStream.close(Duration.ofSeconds(10))
    }

  }

  def getParams(bootStrapServer : String) : Properties = {

    val props : Properties = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "helloWorld")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer)
    // props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    props.put("auto.offset.reset.config", "lastest")

    props

  }


}
