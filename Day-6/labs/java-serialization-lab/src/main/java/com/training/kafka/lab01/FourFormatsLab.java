package com.training.kafka.lab01;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.kafka.Employee;
import com.training.kafka.model.EmployeePojo;
import com.training.kafka.proto.EmployeeMessage;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

/**
 * Lab 01 — serialize the same employee in JSON, XML, Avro, and Protobuf.
 *
 * <p>Run: mvn -q compile exec:java -Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab
 */
public final class FourFormatsLab {

    private static final ObjectMapper JSON = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        EmployeePojo pojo = new EmployeePojo(101, "Asha", "a@x.io");
        Path outDir = Path.of("target", "serialized");
        Files.createDirectories(outDir);

        byte[] json = serializeJson(pojo);
        byte[] xml = serializeXml(pojo);
        byte[] avro = serializeAvro(pojo);
        byte[] proto = serializeProtobuf(pojo);

        Files.write(outDir.resolve("employee.json.bin"), json);
        Files.write(outDir.resolve("employee.xml.bin"), xml);
        Files.write(outDir.resolve("employee.avro.bin"), avro);
        Files.write(outDir.resolve("employee.protobuf.bin"), proto);

        System.out.printf("JSON     %4d bytes%n", json.length);
        System.out.printf("XML      %4d bytes%n", xml.length);
        System.out.printf("Avro     %4d bytes%n", avro.length);
        System.out.printf("Protobuf %4d bytes%n", proto.length);

        assert pojo.equals(deserializeJson(json));
        assert pojo.equals(deserializeXml(xml));
        assert pojo.equals(deserializeAvro(avro));
        assert pojo.equals(deserializeProtobuf(proto));

        System.out.println("All round-trips OK. Files in " + outDir.toAbsolutePath());
    }

    public static byte[] serializeJson(EmployeePojo pojo) throws Exception {
        return JSON.writeValueAsBytes(pojo);
    }

    static EmployeePojo deserializeJson(byte[] bytes) throws Exception {
        return JSON.readValue(bytes, EmployeePojo.class);
    }

    public static byte[] serializeXml(EmployeePojo pojo) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(EmployeePojo.class);
        Marshaller marshaller = ctx.createMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(pojo, out);
        return out.toByteArray();
    }

    static EmployeePojo deserializeXml(byte[] bytes) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(EmployeePojo.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        return (EmployeePojo) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));
    }

    public static byte[] serializeAvro(EmployeePojo pojo) throws Exception {
        Employee avro =
                Employee.newBuilder()
                        .setId(pojo.getId())
                        .setName(pojo.getName())
                        .setDept("N/A")
                        .setSalary(0.0)
                        .build();
        SpecificDatumWriter<Employee> writer = new SpecificDatumWriter<>(Employee.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(avro, encoder);
        encoder.flush();
        return out.toByteArray();
    }

    static EmployeePojo deserializeAvro(byte[] bytes) throws Exception {
        SpecificDatumReader<Employee> reader = new SpecificDatumReader<>(Employee.class);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        Employee avro = reader.read(null, decoder);
        return new EmployeePojo(avro.getId(), avro.getName().toString(), "a@x.io");
    }

    public static byte[] serializeProtobuf(EmployeePojo pojo) {
        return EmployeeMessage.newBuilder()
                .setId(pojo.getId())
                .setName(pojo.getName())
                .setEmail(pojo.getEmail())
                .build()
                .toByteArray();
    }

    static EmployeePojo deserializeProtobuf(byte[] bytes) throws Exception {
        EmployeeMessage msg = EmployeeMessage.parseFrom(bytes);
        return new EmployeePojo(msg.getId(), msg.getName(), msg.getEmail());
    }
}
