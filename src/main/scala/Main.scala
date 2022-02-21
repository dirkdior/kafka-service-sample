import KafkaTopic.Greetings

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed._

object Main extends App {
  implicit val system: ActorSystem[KafkaServiceProtocol.Command] = ActorSystem(
    startSystem,
    "KafkaConnectionSystem"
  )

  val kafkaProducerService = new KafkaProducerService(Greetings)

  def startSystem: Behavior[KafkaServiceProtocol.Command] = {
    Behaviors
      .supervise[KafkaServiceProtocol.Command] {
        Behaviors.setup { context =>
          println("\nStarting KafkaConnection ActorSystem...")
          val consumer = new KafkaConsumerService(Greetings)(context)
          Behaviors
            .receiveMessage[KafkaServiceProtocol.Command] { msg =>
              println(s"processing: $msg")
              throw new Exception("Something went wrong")
              Behaviors.same
            }
            .receiveSignal {
              case (_, signal) if signal == PreRestart || signal == PostStop =>
                println("Received Signal: " + signal)
                println("Stopping Kafka Consumer")
                consumer.shutdownConsumer()
                Behaviors.same
            }
        }
      }
      .onFailure[Exception](SupervisorStrategy.restart)
  }

  kafkaProducerService.send(
    KafkaMsg(
      text      = "hey",
      otherText = "greetings"
    )
  )

  kafkaProducerService.send(
    KafkaMsg(
      text      = "hello",
      otherText = "pleasantries"
    )
  )

  kafkaProducerService.send(
    KafkaMsg(
      text      = "hi",
      otherText = "salute"
    )
  )

  kafkaProducerService.send(
    KafkaMsg(
      text      = "welcome",
      otherText = "ovation"
    )
  )

  Thread.sleep(2000)

  system ! KafkaServiceProtocol.ThrowException

  Thread.sleep(2000)
}
