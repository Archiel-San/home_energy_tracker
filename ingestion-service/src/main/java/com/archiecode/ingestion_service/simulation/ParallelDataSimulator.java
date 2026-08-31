package com.archiecode.ingestion_service.simulation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ParallelDataSimulator implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final Random random = new Random();

    @Value("${simulation.requests-per-interval}")
    private int requestPerInterval;

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    //it is used when u want to test many threads running at the same time
    private final ExecutorService executorService;

    public ParallelDataSimulator(ExecutorService executorService, RestTemplate restTemplate){
        this.executorService = Executors.newCachedThreadPool();
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("ParallelDataSimulator Started");
        ((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);

    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData(){
        //int batchSize = requestPerInterval/
    }



}
