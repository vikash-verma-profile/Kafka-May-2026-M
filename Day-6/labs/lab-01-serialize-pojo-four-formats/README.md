# Lab 01 — Serialize a POJO in Four Formats

**Objective:** Serialize the same `Employee` object (id, name, email) using JSON, XML, Avro, and Protobuf; compare byte sizes and round-trip equality.

From **Seralization.pptx** — Slide 13.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `cd java-serialization-lab` → `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab` |
| **Python** | `cd python-serialization-lab` → `pip install -r requirements.txt` → `python lab01_four_formats.py` |

Or use [scripts/run-lab01.bat](../scripts/run-lab01.bat) (Java).

---

## Prerequisites

- Java JDK 17+, Maven 3.8+
- IDE or terminal for a small Maven module

---

## Step 1 — Create the Employee model

Create a Maven project (or use a `labs/java-serialization-lab` module) with:

```java
public class Employee {
    private int id;
    private String name;
    private String email;
    // constructors, getters, setters, equals/hashCode
}
```

Sample instance: `new Employee(101, "Asha", "a@x.io")`.

---

## Step 2 — Add dependencies (`pom.xml`)

| Format | Dependency |
|--------|------------|
| JSON | `com.fasterxml.jackson.core:jackson-databind` |
| XML | `jakarta.xml.bind:jakarta.xml.bind-api` + JAXB runtime |
| Avro | `org.apache.avro:avro:1.11.x` |
| Protobuf | `com.google.protobuf:protobuf-java:3.x` |

---

## Step 3 — JSON serialization

```java
ObjectMapper mapper = new ObjectMapper();
byte[] jsonBytes = mapper.writeValueAsBytes(employee);
Employee back = mapper.readValue(jsonBytes, Employee.class);
```

Write `employee.json.bin` and print `jsonBytes.length`.

---

## Step 4 — XML serialization

Annotate `Employee` with JAXB (`@XmlRootElement`, `@XmlElement`) or use Jackson XML.

```java
JAXBContext ctx = JAXBContext.newInstance(Employee.class);
Marshaller m = ctx.createMarshaller();
ByteArrayOutputStream out = new ByteArrayOutputStream();
m.marshal(employee, out);
byte[] xmlBytes = out.toByteArray();
```

---

## Step 5 — Avro schema and serialization

Create `src/main/avro/employee.avsc`:

```json
{
  "type": "record",
  "name": "Employee",
  "namespace": "com.training.kafka",
  "fields": [
    {"name": "id", "type": "int"},
    {"name": "name", "type": "string"},
    {"name": "email", "type": ["null", "string"], "default": null}
  ]
}
```

Use `avro-maven-plugin` to generate classes, then:

```java
DatumWriter<Employee> writer = new SpecificDatumWriter<>(Employee.class);
ByteArrayOutputStream out = new ByteArrayOutputStream();
Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
writer.write(avroEmployee, encoder);
encoder.flush();
byte[] avroBytes = out.toByteArray();
```

---

## Step 6 — Protobuf schema and serialization

Create `employee.proto`:

```protobuf
syntax = "proto3";
package com.training.kafka;
message Employee {
  int32 id = 1;
  string name = 2;
  string email = 3;
}
```

Run `protoc` to generate Java, build message, call `employeeProto.toByteArray()`.

---

## Step 7 — Compare and verify

| Format | Expected size (approx.) |
|--------|-------------------------|
| JSON | ~80 B |
| XML | ~140 B |
| Avro | ~25 B |
| Protobuf | ~22 B |

Assert `equals()` after each deserialize path.

**Optional:** Open bytes in a hex viewer — JSON/XML are readable; Avro/Protobuf are binary.

---

## Checkpoint

- [ ] Four files written with distinct byte lengths
- [ ] Avro/Protobuf noticeably smaller than JSON/XML
- [ ] Round-trip equality for all four formats

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| JAXB not found (Java 11+) | Add `org.glassfish.jaxb:jaxb-runtime` |
| Avro class not found | Run `mvn generate-sources` after adding `.avsc` |
| Protobuf compile errors | Match `protoc` plugin version with `protobuf-java` |
