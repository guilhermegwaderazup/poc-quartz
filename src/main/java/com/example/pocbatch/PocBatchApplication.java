package com.example.pocbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PocBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocBatchApplication.class, args);
    }


}
