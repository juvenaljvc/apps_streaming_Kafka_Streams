::consumer console - Démarrage console Consumer :
%KAFKA_HOME%\bin\kafka-console-consumer.bat --bootstrap-server localhost:9092 \
    --topic nom_topic \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer