package com.training.kafka.lab06;

import com.training.kafka.Employee;
import com.training.kafka.config.LabConfig;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Lab 06 — produce records after evolving schema (add email with default).
 *
 * <p>Register employee_v2.avsc via Schema Registry REST or auto-register on first send.
 *
 * <p>Run: mvn -q compile exec:java -Dexec.mainClass=com.training.kafka.lab06.SchemaEvolutionDemo
 */
public final class SchemaEvolutionDemo {

    public static void main(String[] args) throws Exception {
        String bootstrap = LabConfig.arg(args, 0, LabConfig.BOOTSTRAP);
        String topic = LabConfig.arg(args, 1, "employees-avro");
        String registry = LabConfig.arg(args, 2, LabConfig.SCHEMA_REGISTRY);

        Properties props = LabConfig.avroProducerProps(bootstrap, registry);

        try (KafkaProducer<String, Employee> producer = new KafkaProducer<>(props)) {
            Employee withEmail =
                    Employee.newBuilder()
                            .setId(99)
                            .setName("Evolved-Employee")
                            .setDept("Sales")
                            .setSalary(60_000.0)
                            .build();
            producer.send(new ProducerRecord<>(topic, "99", withEmail)).get();
            System.out.println("Sent evolved record: " + withEmail);
        }
        System.out.println("Verify versions: curl " + registry + "/subjects/employees-avro-value/versions");
    }
}
