object KafkaServiceProtocol {

  sealed trait Command
  object ThrowException extends Command

}
