package net.eenss.springcamp2019.service;

import net.eenss.springcamp2019.configure.KafkaConfigure;
import net.eenss.springcamp2019.repository.SomeRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;

@Service
public class Step3Service {
    private static final Logger logger = LoggerFactory.getLogger(Step3Service.class);

    private KafkaConfigure configure;
    private SomeRepository repository;

    public Step3Service(KafkaConfigure configure, SomeRepository repository) {
        this.configure = configure;
        this.repository = repository;
    }

    public void consume() {
        configure.consumer("topic-3")
                .flatMap(this::recordToMessage)
                .groupBy(this::groupByKey)
                .subscribe(groupedFlux ->
                    groupedFlux.sampleFirst(Duration.ofSeconds(5))
                            .flatMap(repository::saveItem)
                            .flatMap(repository::getReceivers)
                            .flatMap(repository::notify)
                            .flatMap(repository::saveResult)
                            .subscribe()
                );
    }

    public void produce() {
        final Flux<SenderRecord<String, String, String>> records = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(100))
                .map(Object::toString)
                .map(i -> SenderRecord.create(new ProducerRecord<>("topic-3", i, i), i));

        configure.producer(records)
                .subscribe(r -> logger.info("PRODUCER) [{}] {}", r.recordMetadata().offset(), r.correlationMetadata()));
    }

    private Mono<String> recordToMessage(ReceiverRecord<String, String> record) {
        return Mono.just(record.value());
    }

    private int groupByKey(final String s) {
        return Integer.parseInt(s) % 10;
    }
}