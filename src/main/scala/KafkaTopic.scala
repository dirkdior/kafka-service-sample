object KafkaTopic {
  sealed trait KafkaTopicT {
    val topicName: String
  }

  object Greetings extends KafkaTopicT {
    val topicName = "Greetings"
  }

  object Languages extends KafkaTopicT {
    val topicName = "Languages"
  }
}
