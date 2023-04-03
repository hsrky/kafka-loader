File To Kafka Loader
=====

Simplest tool to load data from local file into a Kafka topic, line by line.

Build
---
Build the Kotlin/Java application using Maven command:
`mvn clean install`

The output binary can be found on target directory
```
$ ls -al target/kafka-loader-1.0.0-SNAPSHOT.jar 
-rwxrwxrwx 1 user user 15881437 Apr  3 17:49 target/kafka-loader-1.0.0-SNAPSHOT.jar
```

Usage
---
1. Prepare a Kafka properties file with name `kafka.properties` with correct configurations, example:

```
bootstrap.servers=172.26.188.96:9092,172.26.188.96:9093,172.26.188.96:9094
acks=1
retries=0
batch.size=16384
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

Refer to https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html#bootstrap-servers for all possible configurations.

2. Place the `kafka.properties` and `kafka-loader-<version>.jar` on same directory, and run using command similar as below:

`java -jar kafka-loader-<version>.jar --topic <target kafka topic> --data <path to line separated local file>`