package com.training.kafka.lab04;

import com.training.kafka.Employee;
import com.training.kafka.config.LabConfig;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Lab 04 — produce Avro Employee records to employees-avro.
 *
 * <p>Run: mvn -q compile exec:java -Dexec.mainClass=com.training.kafka.lab04.AvroProducer
 */
public final class AvroProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = LabConfig.arg(args, 0, LabConfig.BOOTSTRAP);
        String topic = LabConfig.arg(args, 1, "employees-avro");
        String registry = LabConfig.arg(args, 2, LabConfig.SCHEMA_REGISTRY);

        Properties props = LabConfig.avroProducerProps(bootstrap, registry);

        try (KafkaProducer<String, Employee> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 10; i++) {
                Employee emp =
                        Employee.newBuilder()
                                .setId(i)
                                .setName("Employee-" + i)
                                .setDept("Engineering")
                                .setSalary(50_000 + i * 1_000.0)
                                .build();
                producer.send(new ProducerRecord<>(topic, String.valueOf(i), emp)).get();
                System.out.println("Sent " + emp);
            }
        }
        System.out.println("Done. Check Schema Registry: curl " + registry + "/subjects");
    }
}
