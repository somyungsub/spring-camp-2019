package net.eenss.springcamp2019.core;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaManager {

    private final Map<String, Object> consumerProps;
    private final Map<String, Object> producerProps;

    public KafkaManager() {
        final EmbeddedKafkaBroker broker = new EmbeddedKafkaBroker(1, false, 1);
        broker.afterPropertiesSet();

        final String bootstrapServers = broker.getBrokersAsString();

        this.consumerProps = new HashMap<>();
        this.consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer");
        this.consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group");
        this.consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.producerProps = new HashMap<>();
        this.producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, "producer");
        this.producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        this.producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }

    public Flux<ReceiverRecord<String, String>> consumer(final String topic) {
        final ReceiverOptions<String, String> options = ReceiverOptions.<String, String>create(consumerProps)
                .subscription(Collections.singleton(topic));

        return KafkaReceiver.create(options)
                .receive();
    }

    public Flux<SenderResult<String>> producer(final Publisher<? extends SenderRecord<String, String, String>> publisher) {
        final SenderOptions<String, String> options = SenderOptions.create(producerProps);

        return KafkaSender.create(options)
                .send(publisher);
    }
}
