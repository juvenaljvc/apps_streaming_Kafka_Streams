package processors.stateless

import org.apache.kafka.streams.processor.StreamPartitioner

class CustomPartitioner extends StreamPartitioner[String, Double]{
  override def partition(topic : String, cle : String, valeur : Double, numPartition: Int): Integer = {

    val partitionNumber = cle.hashCode.hashCode() / numPartition
    partitionNumber

  }
}
