package com.javatechie.k8s;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@RestController
public class SpringbootK8sDemoApplication {

	@Value("classpath:message.json")
	Resource resource;

	@Value("${BOOTSTRAP_SERVERS}")
	private String kafkaBootstrapServers;

	@Value("${CLIENT_ID}")
	private String kafkaClientId;

	@GetMapping("/message")
	public String displayMessage(){
		System.out.println(" ############ Hello world ####################");
		return "Congratulation Nihar, you successfully deployed your application to kubernetes !! Environment variables are BOOTSTRAP_SERVERS="+kafkaBootstrapServers+" , CLIENT_ID="+kafkaClientId;
	}

	@GetMapping("/pushtokafka/{topicName}/{count}")
	public String publishToKafka(@PathVariable String topicName,@PathVariable long count){;
		runProducer(topicName,count);
		return "Sent "+count+" record(s) to Kafka topic '"+topicName+"'";
	}
	public static void main(String[] args) {
		SpringApplication.run(SpringbootK8sDemoApplication.class, args);
	}

	public void runProducer(String topicName, long count) {
		System.out.printf("########## Start sending "+count+" record(s) to Kafka topic '"+topicName+"'");

		Producer<Long, String> producer = ProducerCreator.createProducer(kafkaBootstrapServers,kafkaClientId);
		Gson gson = new Gson();
		JsonObject bbwData=null;

		try (Reader reader = new FileReader(getFile())) {
			bbwData = gson.fromJson(reader, JsonObject.class);
			JsonArray gtFinal = bbwData.getAsJsonArray("gtFinal");
			System.out.println("Json Array: "+gtFinal.size());
			System.out.println("Json Array 0th Ele: "+gtFinal.get(0));
			//System.out.printf("Json Data: "+bbwData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int index = 0; index < count; index++) {
			final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topicName,
					bbwData.toString());
			try {
				RecordMetadata metadata = producer.send(record).get();
				System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
						+ " with offset " + metadata.offset());
			} catch (ExecutionException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			} catch (InterruptedException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			}
		}
	}
	public File getFile(){
		InputStream is = null;
		try {
			is = resource.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = null;
		try {
			file = new File("message.json");
			Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Is message.json file Exists: "+file.exists());
		return file;
	}
}
