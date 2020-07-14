package com.njyjz.svcanalyzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConfig
{
    @Value("${kafka.bootstrapServers}")
    private String bootstrapServers;
    
    @Value("${kafka.groupId}")
    private String groupId;
 
    @Value("${kafka.sessionTimeout}")
    private String sessionTimeout;
 
    @Value("${kafka.maxPollRecords}")
    private String maxPollRecords;
 
    @Value("${kafka.autoOffsetReset}")
    private String autoOffsetReset;
 
    @Value("${kafka.autoCommitIntervalMs}")
    private String autoCommitIntervalMs;
 
    @Value("${kafka.consumerRequestTimeoutMs}")
    private String consumerRequestTimeoutMs;
 
    @Value("${kafka.concurrency}")
    private Integer concurrency;
    
    @Bean("kafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<?> batchFactory(){
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs()));
        factory.setBatchListener(true);
        factory.setConcurrency(concurrency);
        return factory;
    }
 
    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG,consumerRequestTimeoutMs);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitIntervalMs);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);//每一批数量
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
//        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG) 超过对接时间认为是lock
        return props;
    }

}
