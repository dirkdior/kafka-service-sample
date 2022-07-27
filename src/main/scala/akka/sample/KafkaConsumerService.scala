package akka.sample

import akka.NotUsed
import akka.actor.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ ConsumerSettings, KafkaConsumerActor, Subscriptions }
import akka.stream.scaladsl.{ Flow, Keep, Sink }
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{ Decoder, parser }
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

class KafkaConsumerService(topic: KafkaTopic.KafkaTopicT)(
  context: ActorContext[KafkaServiceProtocol.Command]
) {
  println("Starting akka.sample.KafkaConsumerService...")
  private implicit val system: ActorSystem[KafkaServiceProtocol.Command] =
    context.system.asInstanceOf[ActorSystem[KafkaServiceProtocol.Command]]
  private implicit val kafkaMsgDecoder: Decoder[KafkaMsg]                = deriveDecoder

  private val bootstrapServers                                   = "localhost:9092"
  private val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer).withBootstrapServers(bootstrapServers)

  private val consumer: ActorRef = context.actorOf(KafkaConsumerActor.props(consumerSettings), "kafka-consumer-actor")

  private val (controlPartition, result) = Consumer
    .plainExternalSource[String, String](
      consumer,
      Subscriptions.assignment(new TopicPartition(topic.topicName, 0))
    )
    .via(businessFlow)
    .toMat(Sink.seq)(Keep.both)
    .run()

  private def businessFlow: Flow[ConsumerRecord[String, String], KafkaMsg, NotUsed] =
    Flow[ConsumerRecord[String, String]].map { record =>
      val payload   = record.value()
      val key       = record.key()
      val partition = record.partition()
      println(s"[akka.sample.KafkaConsumerService]: payload $payload  Key: $key  partition $partition")
      val kafkaMsg  = parser.decode[KafkaMsg](payload).toOption.get
      kafkaMsg
    }

  println(s"Partition: $controlPartition")

  result onComplete {
    case Success(value) =>
      println("[akka.sample.KafkaConsumerService] Results from Consumer: " + value)
    case Failure(ex)    =>
      println("[akka.sample.KafkaConsumerService] Ran into an error: " + ex.printStackTrace)
  }

  def shutdownConsumer(): Unit = {
    controlPartition.shutdown()
    consumer ! KafkaConsumerActor.Stop
  }
}
