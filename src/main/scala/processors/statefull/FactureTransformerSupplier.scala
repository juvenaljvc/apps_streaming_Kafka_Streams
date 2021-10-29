package processors.statefull
import org.apache.kafka.streams.kstream.{ValueTransformer, ValueTransformerSupplier}
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import processors.statefull.TransformValueProcessor.factureStoreName
import schemas.Facture


class FactureTransformerSupplier extends ValueTransformerSupplier[String, String]{
  override def get(): ValueTransformer[String, String] = new ValueTransformer[String, String] {

    private var context : ProcessorContext = null
    private var factureStateStore : KeyValueStore[String, String] = null
    private var description : String = ""

    override def init(processorContext: ProcessorContext): Unit = {
      this.context = processorContext
      this.factureStateStore = context.getStateStore(factureStoreName).asInstanceOf[KeyValueStore[String, String]]

    }

    override def transform(v: String): String= {

      factureStateStore.putIfAbsent("45i", "Televiseur Plasma LG")
      factureStateStore.putIfAbsent("45a", "Smart One Refrigerator")
      factureStateStore.putIfAbsent("34b", "Living Climatiseur")
      factureStateStore.putIfAbsent("48i", "Electrolux Machine à Laver")

      if (factureStateStore.get(v) == null) {
        description = "Aucune valeur correspondante"
      } else {
        description = factureStateStore.get(v)
      }

      return description

    }

    override def close(): Unit = {

      if (factureStateStore.isOpen) {
        factureStateStore.close()
      }

    }
  }
}
