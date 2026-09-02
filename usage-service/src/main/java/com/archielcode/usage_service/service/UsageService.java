package com.archielcode.usage_service.service;

import com.archielcode.kafka.event.EnergyUsageEvent;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;

    @Value("${influx.url}")
    private String influxUrl;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.token}")
    private String influxToken;

    @Value("${influx.org}")
    private String influxOrg;

    public UsageService(InfluxDBClient influxDBClient){
        this.influxDBClient = influxDBClient;
    }

    // timeseries DB
    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent){
        log.info("Received energy usage event: {}", energyUsageEvent);

        Point point = Point.measurement("energy-usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }

    @Scheduled(cron = "*10 * * * * *")
    public void aggregateDeviceEnergyUsage(){



    }


}
