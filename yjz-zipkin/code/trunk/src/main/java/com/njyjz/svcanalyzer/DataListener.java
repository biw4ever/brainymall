package com.njyjz.svcanalyzer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.stream.Spans;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import zipkin.Span;

//@Component
public class DataListener
{
//    private static Logger logger = LoggerFactory.getLogger(DataListener.class);
//    
//    @StreamListener("sleuth")
//    public void sink(Spans input) 
//    {
//        List<Span> converted = ConvertToZipkinSpanList.convert(input);
//        logger.debug(converted.toString());
//        
//    }
//    
//    @KafkaListener(containerFactory = "kafkaListenerContainerFactory", topics = {"sleuth"})
//    public void sink(Object input) 
//    {
//        logger.error(input.toString());    
//    }
    
}
