package com.archiecode.alert_service.service;

import com.archiecode.kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertService {

    private final EmailService emailService;

    public AlertService(EmailService emailService){
        this.emailService = emailService;
    }


    @KafkaListener(topics = "energy-alerts", groupId = "aleer-service")
    public void energyUsageAlertEvent(AlertingEvent alertingEvent){
        log.info("Received Alert event. {}", alertingEvent);

        //send email alert
        final String subject = "Energy Usage Alert for User "
                +alertingEvent.userId();

        final String message = "Alert: "+ alertingEvent.message()+
                " Threshold: "+alertingEvent.threshold()+
                " Energy Consumed: "+alertingEvent.energyConsumed();
        emailService.sendEmail(alertingEvent.email(), subject,message, alertingEvent.userId());

    }

}
