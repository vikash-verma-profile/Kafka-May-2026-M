package day9.labs;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsSpec;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

/**
 * Lab 05 — print per-partition consumer lag (offset-based).
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=day9.labs.Lab05LagCheck
 * -Dexec.args="localhost:9092 billing-svc"
 */
public final class Lab05LagCheck {

  public static void main(String[] args) throws Exception {
    String bootstrap = args.length > 0 ? args[0] : "localhost:9092";
    String group = args.length > 1 ? args[1] : "billing-svc";

    Properties adminProps = new Properties();
    adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);

    try (Admin admin = Admin.create(adminProps)) {
      Map<String, Map<TopicPartition, OffsetAndMetadata>> byGroup =
          admin
              .listConsumerGroupOffsets(
                  Collections.singletonMap(group, new ListConsumerGroupOffsetsSpec()))
              .all()
              .get();

      Map<TopicPartition, OffsetAndMetadata> committed =
          byGroup.getOrDefault(group, Collections.emptyMap());

      if (committed.isEmpty()) {
        System.out.println("No committed offsets for group " + group);
        return;
      }

      Set<TopicPartition> partitions = committed.keySet();
      Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets =
          admin
              .listOffsets(
                  partitions.stream()
                      .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest())))
              .all()
              .get();

      committed.forEach(
          (tp, offsetMeta) -> {
            long end = endOffsets.get(tp).offset();
            long lag = end - offsetMeta.offset();
            System.out.printf(
                "%s %s p%d committed=%d end=%d lag=%d%n",
                group, tp.topic(), tp.partition(), offsetMeta.offset(), end, lag);
          });
    }
  }
}
