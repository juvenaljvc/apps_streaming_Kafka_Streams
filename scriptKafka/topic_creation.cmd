::topic creation
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create  --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic facture-topic