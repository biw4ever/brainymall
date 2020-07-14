package com.njyjz.svcanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.zipkin.stream.EnableZipkinStreamServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableZipkinStreamServer
@ComponentScan(basePackages = {"com.njyjz"})
@EnableKafka
public class ZipkinStarter
{
    public static void main(String[] args)
    {
        SpringApplication.run(ZipkinStarter.class, args);
    }
}
