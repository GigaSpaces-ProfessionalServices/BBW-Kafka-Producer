package com.javatechie.k8s;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
import java.util.*;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@RestController
public class SpringbootK8sDemoApplication {

	@Value("${BOOTSTRAP_SERVERS}")
	private String kafkaBootstrapServers;

	@Value("${CLIENT_ID}")
	private String kafkaClientId;

	@Value("${AZURE_STORAGE_ACCOUNT_NAME}")
	private String azureStorageAccountName;

	@Value("${AZURE_STORAGE_ACCOUNT_KEY}")
	private String azureStorageAccountKey;

	@Value("${AZURE_STORAGE_CONTAINER_NAME}")
	private String azureStorageContainerName;

	@Value("${AZURE_STORAGE_MESSAGE_FILE}")
	private String azureStorageMessageFile;

	@GetMapping("/message")
	public String displayMessage(){
		return "Congratulation, you successfully deployed your application to kubernetes !! Environment variables are BOOTSTRAP_SERVERS="+kafkaBootstrapServers+" , CLIENT_ID="+kafkaClientId;
	}

	@GetMapping("/pushtokafka/{topicName}/{count}")
	public String publishToKafka(@PathVariable String topicName,@PathVariable long count){
		createTopic(topicName);
		String response = runProducer(topicName,count);
		if(response != null && response.equals("Success")) {
			return "Sent " + count + " record(s) to Kafka topic '" + topicName + "'";
		}else{
			return "Sending failed to Kafka topic '" + topicName + "'.";
		}
	}
	public static void main(String[] args) {
		SpringApplication.run(SpringbootK8sDemoApplication.class, args);
	}
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		createTopic("bbw");
	}

	public void createTopic(String topicName){
		System.out.printf("########## Creating New Topic with Name =  '"+topicName+"'");
		Properties props = ProducerCreator.getKafkaProperties(kafkaBootstrapServers,kafkaClientId);
		AdminClient adminClient = AdminClient.create(props);

		Map<String, String> newTopicConfig = new HashMap<>();
		newTopicConfig.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, "10485880");

		NewTopic newTopic = new NewTopic(topicName, 1, (short)1); //new NewTopic(topicName, numPartitions, replicationFactor)
		newTopic.configs(newTopicConfig);

		List<NewTopic> newTopics = new ArrayList<NewTopic>();
		newTopics.add(newTopic);

		adminClient.createTopics(newTopics);
		adminClient.close();
	}
	public String runProducer(String topicName, long count) {
		System.out.printf("########## Start sending "+count+" record(s) to Kafka topic '"+topicName+"'");

		Producer<Long, String> producer = ProducerCreator.createProducer(kafkaBootstrapServers,kafkaClientId);
		Gson gson = new Gson();
		JsonObject bbwData=null;
		String message = null;
		try {
			message=AzureBlobUtils.getFileData(azureStorageAccountName,azureStorageAccountKey
					,azureStorageContainerName,azureStorageMessageFile);
			if(message != null) {
				bbwData = gson.fromJson(message, JsonObject.class);
				JsonArray gtFinal = bbwData.getAsJsonArray("gtFinal");
				System.out.println("Json Array: " + gtFinal.size());
				System.out.println("Json Array 0th Ele: " + gtFinal.get(0));
				//System.out.printf("Json Data: "+bbwData);

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
						return "Error";
					} catch (InterruptedException e) {
						System.out.println("Error in sending record");
						System.out.println(e);
						return "Error";
					}
				}
			}else{
				return "Error";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
		return "Success";
	}
}
