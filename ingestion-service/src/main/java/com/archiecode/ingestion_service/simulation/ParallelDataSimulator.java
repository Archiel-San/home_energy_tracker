package com.archiecode.ingestion_service.simulation;

import com.archiecode.ingestion_service.dto.EnergyUsageDto;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ParallelDataSimulator implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.requests-per-interval}")
    private int requestPerInterval;

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    //it is used when u want to test many threads running at the same time
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void run(String... args) throws Exception {
        log.info("ParallelDataSimulator Started");
        ((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);
    }

    //de 1 em 1 min corre isto
    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData(){

        //batchSize the amount of data that will be retrieved
        int batchSize = requestPerInterval/parallelThreads;
        int remainder = requestPerInterval%parallelThreads;

        for (int i =0; i<parallelThreads; i++){
            int requestsForThread = batchSize + (i< remainder? 1:0);
            executorService.submit(
                    ()->{
                        for(int j=0; j<requestsForThread; j++){
                            EnergyUsageDto dto = new EnergyUsageDto(
                                    random.nextLong(1,6),
                                    Math.round(random.nextDouble(0.0,2.0)*100.0)/100.0,
                                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
                            );
                            try {
                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.APPLICATION_JSON);
                                HttpEntity<EnergyUsageDto> request = new HttpEntity<>(dto, headers);
                                restTemplate.postForEntity(ingestionEndpoint, request, Void.class);
                                log.info("sent mock data: "+dto);
                            }catch (Exception e){
                                log.error("Failed to send data: {}", e.getLocalizedMessage());
                            }

                        }
                    }
            );
        }
    }

    @PreDestroy
    public void shutdown(){
        executorService.shutdown();
        log.info("ParallelDataSimulator Shut down");
    }



}
