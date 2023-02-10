FROM openjdk:8
EXPOSE 8080
ADD target/BBW-kafka-producer.jar bbw-kafka-producer.jar
ENTRYPOINT ["java","-jar","/bbw-kafka-producer.jar"]