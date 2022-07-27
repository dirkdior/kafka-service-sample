package akka.sample

import akka.actor.typed.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

case class KafkaMsg(
  text: String,
  otherText: String
)

class KafkaProducerService(topic: KafkaTopic.KafkaTopicT)(implicit system: ActorSystem[KafkaServiceProtocol.Command]) {
  val bootstrapServers = "localhost:9092"

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  // create a producer
  val kafkaProducer                                          = producerSettings.createKafkaProducer()
  val settingsWithProducer: ProducerSettings[String, String] = producerSettings.withProducer(kafkaProducer)

  def send(msg: KafkaMsg): Unit = {
    Thread.sleep(5000)
    val done = Source
      .single(msg)
      .map(value => new ProducerRecord[String, String](topic.topicName, value.asJson.noSpaces))
      .runWith(Producer.plainSink(settingsWithProducer))
    done onComplete {
      case Success(value) =>
        // to close the producer after use
        //      kafkaProducer.close()
        println(s"KafkaProducer Response: $value for message $msg")
      case Failure(ex)    =>
        println("Got error: " + ex)
    }
  }
}
