package com.kafka.consumer.lab;

import java.util.Collection;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

final class AssignmentLogger implements ConsumerRebalanceListener {

    private final String label;

    AssignmentLogger(String label) {
        this.label = label;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        System.out.printf("[%s] Revoked: %s%n", label, partitions);
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        System.out.printf("[%s] Assigned partitions: %s%n", label, partitions);
    }
}
