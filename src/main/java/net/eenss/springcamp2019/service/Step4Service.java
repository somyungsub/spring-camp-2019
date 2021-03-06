package net.eenss.springcamp2019.service;

import net.eenss.springcamp2019.core.KafkaManager;
import net.eenss.springcamp2019.core.RecordProcessor;
import net.eenss.springcamp2019.core.Step4Subscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.kafka.receiver.ReceiverRecord;

@Service
public class Step4Service extends SubscriberDemoService implements RecordProcessor {

    public Step4Service(KafkaManager kafkaManager) {
        super("step-4", kafkaManager);
    }

    @Override
    protected BaseSubscriber<ReceiverRecord<String, String>> getSubscriber() {
        return new Step4Subscriber(this::justTrueWithDelay);
    }
}
