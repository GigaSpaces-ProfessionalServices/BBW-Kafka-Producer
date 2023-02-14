FROM openjdk:8
EXPOSE 8080
COPY target/BBW-kafka-producer.jar bbw-kafka-producer.jar
ENTRYPOINT ["java","-jar","/bbw-kafka-producer.jar"]