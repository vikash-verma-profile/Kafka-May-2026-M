package day9.labs;

import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Lab 01 — produce one message with SASL/SCRAM.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=day9.labs.Lab01ScramProducer
 * -Dexec.args="localhost:9093 orders alice secret"
 */
public final class Lab01ScramProducer {

  public static void main(String[] args) {
    String bootstrap = args.length > 0 ? args[0] : KafkaClientConfig.defaultBootstrap();
    String topic = args.length > 1 ? args[1] : "orders";
    String username = args.length > 2 ? args[2] : "alice";
    String password = args.length > 3 ? args[3] : "secret";

    try (KafkaProducer<String, String> producer =
        new KafkaProducer<>(
            KafkaClientConfig.scramProducerProperties(bootstrap, username, password))) {

      ProducerRecord<String, String> record =
          new ProducerRecord<>(topic, "hello-from-java-scram");

      RecordMetadata meta = producer.send(record).get(10, TimeUnit.SECONDS);
      System.out.printf(
          "Sent to %s partition=%d offset=%d%n",
          meta.topic(), meta.partition(), meta.offset());
    } catch (Exception e) {
      System.err.println("Failed: " + e.getMessage());
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }
}
