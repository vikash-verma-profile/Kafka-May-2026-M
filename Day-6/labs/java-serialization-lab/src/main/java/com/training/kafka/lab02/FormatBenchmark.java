package com.training.kafka.lab02;

import com.training.kafka.lab01.FourFormatsLab;
import com.training.kafka.model.EmployeePojo;

/**
 * Lab 02 — benchmark size and serialization time for four formats.
 *
 * <p>Run: mvn -q compile exec:java -Dexec.mainClass=com.training.kafka.lab02.FormatBenchmark
 */
public final class FormatBenchmark {

    private static final int WARMUP = 10_000;
    private static final int ITERATIONS = 100_000;

    public static void main(String[] args) throws Exception {
        EmployeePojo emp = new EmployeePojo(101, "Asha", "a@x.io");

        byte[] jsonOnce = FourFormatsLab.serializeJson(emp);
        byte[] xmlOnce = FourFormatsLab.serializeXml(emp);
        byte[] avroOnce = FourFormatsLab.serializeAvro(emp);
        byte[] protoOnce = FourFormatsLab.serializeProtobuf(emp);

        long jsonNs = bench(() -> FourFormatsLab.serializeJson(emp));
        long xmlNs = bench(() -> FourFormatsLab.serializeXml(emp));
        long avroNs = bench(() -> FourFormatsLab.serializeAvro(emp));
        long protoNs = bench(() -> FourFormatsLab.serializeProtobuf(emp));

        double jsonMs = jsonNs / 1_000_000.0;
        double baseline = jsonMs;

        System.out.println("| Format   | Size (B) | Time (ms) | Ratio vs JSON |");
        System.out.println("|----------|----------|-----------|---------------|");
        printRow("JSON", jsonOnce.length, jsonMs, baseline);
        printRow("XML", xmlOnce.length, xmlNs / 1_000_000.0, baseline);
        printRow("Avro", avroOnce.length, avroNs / 1_000_000.0, baseline);
        printRow("Protobuf", protoOnce.length, protoNs / 1_000_000.0, baseline);
    }

    private static long bench(ThrowingRunnable action) throws Exception {
        for (int i = 0; i < WARMUP; i++) {
            action.run();
        }
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            action.run();
        }
        return System.nanoTime() - start;
    }

    private static void printRow(String name, int size, double ms, double baselineMs) {
        double ratio = ms / baselineMs;
        System.out.printf("| %-8s | %8d | %9.2f | %13.2fx |%n", name, size, ms, ratio);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
