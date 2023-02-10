package com.javatechie.k8s;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerCreator {

	public static Producer<Long, String> createProducer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "kafka:9092");
		props.put("client.id", "client1");
		props.put("key.serializer", LongSerializer.class.getName());
		props.put("value.serializer", StringSerializer.class.getName());
		props.put("max.request.size",100000000);
		return new KafkaProducer<>(props);
	}
}