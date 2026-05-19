# Kafka Java Lab

Maven project: producer + consumer for topic `lab-messages` on `localhost:9092`.

**Full guide (create, build, run, troubleshoot):**  
[../kafka-java-lab-guide.md](../kafka-java-lab-guide.md)

**Quick run**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q compile
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleConsumer
```

```bat
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleProducer
```

Or from `Labs/`: `run-consumer.bat` then `run-producer.bat`
