package com.javatechie.k8s;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerCreator {

	public static Producer<Long, String> createProducer(String kafkaBootstrapServers,String kafkaClientId) {
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaBootstrapServers);
		props.put("client.id", kafkaClientId);
		props.put("key.serializer", LongSerializer.class.getName());
		props.put("value.serializer", StringSerializer.class.getName());
		props.put("max.request.size",100000000);
		return new KafkaProducer<>(props);
	}
}