package com.archielcode.usage_service.service;

import com.archielcode.kafka.event.AlertingEvent;
import com.archielcode.kafka.event.EnergyUsageEvent;
import com.archielcode.usage_service.client.DeviceClient;
import com.archielcode.usage_service.client.UserClient;
import com.archielcode.usage_service.dto.DeviceDto;
import com.archielcode.usage_service.dto.UserDto;
import com.archielcode.usage_service.model.DeviceEnergy;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;

    private final UserClient userClient;

    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influx.url}")
    private String influxUrl;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.token}")
    private String influxToken;

    @Value("${influx.org}")
    private String influxOrg;

    public UsageService(InfluxDBClient influxDBClient, DeviceClient deviceClient, UserClient userClient, KafkaTemplate<String, AlertingEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
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
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(1000);

        String fluxQuery = String.format("""
        from(bucket: "%s")
          |> range(start: time(v: "%s"), stop: time(v: "%s"))
          |> filter(fn: (r) => r["_measurement"] == "energy_usage")
          |> filter(fn: (r) => r["_field"] == "energyConsumed")
          |> group(columns: ["deviceId"])
          |> sum(column: "_value")
        """, influxBucket, oneHourAgo.toString(), now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for(FluxTable table: tables){
            for (FluxRecord record : table.getRecords()){
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                double energyConsumed = record.getValueByKey("_value") instanceof Number? ((Number) record.getValueByKey("_value")).doubleValue(): 0.0;

                deviceEnergies.add(
                        DeviceEnergy.builder()
                                .deviceId(Long.valueOf(deviceIdStr))
                                .energyConsumed(energyConsumed)
                                .build()
                );
            }
        }
        log.info("Aggregated device energies over the past hour: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy: deviceEnergies){
            final DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());

            if(deviceResponse == null | deviceResponse.id() == null){
                log.warn("Device not found ");
            }
            deviceEnergy.setUserId(deviceResponse.userId());
        }

        //Remove devices with null userId
        deviceEnergies.removeIf(de -> de.getUserId() == null);

        //Maps alist of devices Consumption to the userId key
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap =
                deviceEnergies.stream().collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-Device Energy Map {}:", userDeviceEnergyMap);

        //get users consumption threshold
        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for(final Long userId : userIds){
            try{
                UserDto user = userClient.getUserById(userId);
                if (user==null || !user.alerting()){
                    log.warn("User not found or alerting disabled for Id: {}", userId);
                    continue;
                }
                userThresholdMap.put(userId, user.energyAlertingThreshold());
                userEmailMap.put(userId, user.email());
            }
            catch (Exception e){
                log.warn("failed to fetch user for ID: {}", userId);
            }
        }
        log.info("User Threshold Map: {}", userThresholdMap);

        final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());
        for (final Long userId: alertedUsers){
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);

            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed).sum();

            if (totalConsumption>threshold){
                log.info("ALERT: User ID {} has exceeded the energy threshold! " +
                        "Total Consumption: {}, Threshold: {}", userId, totalConsumption, threshold);
                //Put message on kafka alert-topic
                final AlertingEvent alertingEvent = AlertingEvent.builder()
                        .userId(userId)
                        .message("Energy consumption thresold exceeded")
                        .threshold(threshold)
                        .energyConsumed(totalConsumption)
                        .email(userEmailMap.get(userId))
                        .build();

                //send message to kafka template
                kafkaTemplate.send("energy-alerts", alertingEvent);
            }
            else {
                log.info("User Id {} is within the energy threshold. Total Consumption: {}, Threshold: {}", userId, totalConsumption, threshold);
            }


        }
    }


}
