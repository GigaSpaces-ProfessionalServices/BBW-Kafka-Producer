# BBW Kafka Producer + Pluggable Connector

###1. Create Cluster on AWS EKS

>eksctl create cluster --name bbwkafka --version 1.21 --region eu-west-2  --nodegroup-name standard-workers --node-type t3.large --nodes 3 --nodes-min 1 --nodes-max 3 --tags owner=niharkapadia,project=bbw

###2. Set docker authentication in Kubernetes

>kubectl create secret docker-registry docker-secret   --docker-username=USER --docker-password=PASSWD

###3. Install DI Pipeline Umbrella

>helm repo add dih https://s3.amazonaws.com/resources.gigaspaces.com/helm-charts-dih

>helm install di dih/di-pipeline --version 16.3.0-m5

#### To access Kafka-UI from local (http://localhost:8080)
>kubectl port-forward svc/kafka-ui 8080:8080

#### To access Space deck from local (http://localhost:3000)
>kubectl port-forward svc/spacedeck 3000:3000

####  DEPLOY SPACE
>helm install bbw gigaspaces-repo-ea/xap-pu --version 16.3.0-m5

###4. Build BBW-kafka-producer code
> cd ~/work/gigaspaces/BBW/BBW-kafka-producer

> mvn clean install

###5. Build and Push Docker Image
> sudo docker build -t niharkapadia/bbw-kafka-producer:1.0 .

> sudo docker push niharkapadia/bbw-kafka-producer:1.0

###6. Deploy BBW-kafka-producer app to Kubernetes
> kubectl delete deployments bbw-kafka-producer

> kubectl apply -f /home/nihar/work/gigaspaces/BBW/BBW-kafka-producer/deployment.yaml

> kubectl delete svc bbw-kafka-producer-svc

> kubectl apply -f /home/nihar/work/gigaspaces/BBW/BBW-kafka-producer/loadbalance.yaml

>  kubectl port-forward bbw-kafka-producer-86776859f9-vrttp 8081:8080

###7. Create Kafka Topic manually from redpanda Kafka-UI
>Use topic name as 'bbw' and set configuration 'max.message.bytes=10485880'

###8. Install Pluggable Connector

#### Download code from Git repository

>https://github.com/GigaSpaces-POCs/kafka-connector/tree/bbw-multi-records

#### Verify Kafka and Space Configuration in values.yaml
>kafka-connector-bbw-multi-records/helm-chart/pluggable-connector/values.yaml

````
e.g. ----------
kafka:
    bootstrapServers: kafka:9092
    consumerGroup: DIH
    max:
        request:
        size: 10485880
space:
    name: bbw
    locators: xap-manager-hs
    group: xap-16.3.0
----------- 
````

#### Verify data-pipeline.yml
>kafka-connector-bbw-multi-records/helm-chart/pluggable-connector/files/data-pipeline.yml

#### Verify deployment.yaml
>kafka-connector-bbw-multi-records/helm-chart/pluggable-connector/templates/deployment.yaml

#### Install Pluggable Connectors
>helm install bbwpc kafka-connector-bbw-multi-records/helm-chart/pluggable-connector

#### To access Pluggable-Connector-UI from local (http://localhost:6085)
>kubectl port-forward svc/bbwpc-pluggable-connector 6085:6085


###9. Run the Flow

#### Before execution verification
1. Space 'bbw' has 0 record counts (Verify on space deck - http://localhost:3000)
2. Topic 'bbw' has 0 messages (Verify on kafka-ui - http://localhost:8080)

#### Execution Steps

1. Get external ip for ' BBW-kafka-producer app' (use command 'Kubectl get service')
2. Call REST url to push messages to kafka topic
    > Rest: http://<external-ip>:8081/pushtokafka/<TOPIC_NAME>/<MESSAGE_COUNT>
   
    > Example: http://<external-ip>:8081/pushtokafka/bbw/5

#### After execution verification

1. Space 'bbw' has 2538 record counts (Verify on space deck - http://localhost:3000)

   ![snapshot](Pictures/Picture1.png)

3. Topic 'bbw' has 5 messages (Verify on kafka-ui - http://localhost:8080)

   ![snapshot](Pictures/Picture2.png)# BBW-Kafka-Producer
