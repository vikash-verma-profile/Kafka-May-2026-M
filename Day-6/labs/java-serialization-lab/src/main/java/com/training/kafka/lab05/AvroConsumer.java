package com.training.kafka.lab05;

import com.training.kafka.Employee;
import com.training.kafka.config.LabConfig;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * Lab 05 — consume Avro Employee records from employees-avro.
 *
 * <p>Run: mvn -q compile exec:java -Dexec.mainClass=com.training.kafka.lab05.AvroConsumer
 */
public final class AvroConsumer {

    public static void main(String[] args) {
        String bootstrap = LabConfig.arg(args, 0, LabConfig.BOOTSTRAP);
        String topic = LabConfig.arg(args, 1, "employees-avro");
        String registry = LabConfig.arg(args, 2, LabConfig.SCHEMA_REGISTRY);

        Properties props = LabConfig.avroConsumerProps(bootstrap, registry, "avro-consumer-grp");

        try (KafkaConsumer<String, Employee> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            int total = 0;
            while (total < 10) {
                ConsumerRecords<String, Employee> records = consumer.poll(Duration.ofSeconds(2));
                for (ConsumerRecord<String, Employee> r : records) {
                    Employee e = r.value();
                    System.out.printf(
                            "partition=%d offset=%d id=%d name=%s dept=%s salary=%.0f%n",
                            r.partition(), r.offset(), e.getId(), e.getName(), e.getDept(), e.getSalary());
                    total++;
                }
            }
            consumer.commitSync();
            System.out.println("Read " + total + " records. Restart to verify offset resume.");
        }
    }
}
