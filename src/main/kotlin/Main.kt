import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.KafkaAdminClient
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.io.File
import java.io.FileInputStream
import java.time.Duration
import java.time.Instant
import java.util.*

class Main : CliktCommand() {
    private val config by option(help = "Kafka producer configurations").default("kafka.properties")
    private val topic by option(help = "Kafka Topic to load the messages").required()
    private val data by option(help = "Data file which will be loaded to Kafka topic, line by line").required()

    override fun run() {
        println("Load file '$data' to topic '$topic' using Kafka configuration file: $config.")
        val props = Properties()
        FileInputStream(config).use {
            props.load(it)
        }

        println("Kafka configuration: $props")

        listTopics(props)

        val producer = KafkaProducer<String, String>(props)

        var count = 0
        val startTime = Instant.now()
        File(data).forEachLine {
            if (count == 0 || count % 10000 == 0) {
                println("Sending $count records..")
            }
            val rec = ProducerRecord<String, String>(topic, it)
            //rec.headers().add("no", "${count+1}".toByteArray())
            producer.send(rec)
            count++
        }

        producer.flush()
        producer.close()
        val spent = Duration.between(startTime, Instant.now()).seconds.coerceAtLeast(1)
        val rps = count / spent
        println("Done. Spent $spent seconds for $count records. Perf: ~ $rps rps")
    }
}

fun listTopics(props: Properties) {
    println("Validating connection to Kafka brokers...")
    KafkaAdminClient.create(props).use { client ->
        println("Retrieving topics from Kafka brokers...")
        var count = 0
        client.listTopics().names().get().forEach {
            val partitionCount = getPartitionCount(client, it)
            println("${++count}. $it ($partitionCount partitions)")
        }
        println("Found $count topics.")
    }
}

fun getPartitionCount(client: AdminClient, topic: String): Int {
    val topicDescription = client.describeTopics(listOf(topic)).topicNameValues()[topic]
    return topicDescription?.get()?.partitions()?.size ?: 0
}

fun main(args: Array<String>) = Main().main(args)
