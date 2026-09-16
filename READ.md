# Home Energy Tracker

Home Energy Tracker is a microservices-based system for monitoring electricity usage in a home environment. It collects device data, stores it in a time-series database, analyzes consumption patterns, and sends alerts when abnormal behavior is detected.

The system is designed to separate responsibilities so each service handles one part of the process:

- User management
- Device management
- Data ingestion
- Usage processing
- Alerts
- Insights
- API routing

## How the system works

The system is built around a set of independent Spring Boot services that communicate through Kafka and a shared infrastructure stack.

### 1. User and authentication flow
Users are created and managed in the user-service. Authentication is handled through Keycloak, which provides login and access control.

When a user logs in or requests access to protected resources, the request is sent through the API gateway. The gateway forwards the request to the appropriate backend service, while protecting the internal services from direct exposure.

### 2. Device registration and management
Homes can have multiple smart devices or energy meters. These devices are registered and managed through the device-service.

This service stores device metadata such as:
- device ID
- user ownership
- device type
- status
- location / home association

This information is used later to match incoming usage data to the correct home and user.

### 3. Data collection from devices
Energy data originates from connected devices or meter readings. These readings are sent to the ingestion-service.

The ingestion-service is responsible for:
- receiving raw device data
- validating the payload
- checking if the device is known
- normalizing the event format
- forwarding the event to the message bus

### 4. Event processing with Kafka
Kafka acts as the central messaging layer between services.

When data is ingested, it is published as an event. Other services subscribe to that event stream and react to it independently.

This keeps the architecture decoupled:
- ingestion-service handles incoming data
- usage-service consumes usage events
- alert-service reacts to abnormal values
- insight-service can compute trends and summaries

### 5. Usage processing and storage
The usage-service is the main processing component for energy consumption data.

It consumes events from Kafka and transforms raw readings into usable usage information, such as:
- total consumption
- per-hour or per-day patterns
- time-bucket summaries
- trend calculations
- energy load analysis

Usage data is stored in InfluxDB, which is optimized for time-series data and is ideal for energy metrics that are collected continuously over time.

Relational data, such as users, devices, and service metadata, is stored in PostgreSQL.

### 6. Alerts and notifications
The alert-service monitors usage patterns and thresholds.

Examples of alert conditions:
- unusually high energy use
- sudden spike in consumption
- device offline or inactive
- irregular readings from a sensor

When a rule is triggered, the alert-service can send notifications, such as:
- email alerts
- internal warnings
- dashboard notifications

Mailpit is included in the infrastructure for local email testing, which makes alert delivery easier to test in development.

### 7. Insights and analytics
The insight-service analyzes historical usage and provides higher-level information such as:
- daily/weekly energy consumption summaries
- trends over time
- comparison between periods
- suggested efficiency improvements

This service builds on the stored usage data and can help users understand energy behavior and reduce waste.

### 8. API gateway
All external client requests go through the api-gateway service.

The gateway:
- exposes a single entry point
- routes requests to the correct internal service
- centralizes traffic management
- can provide resilience and monitoring features
- reduces coupling between clients and backend services

This makes the architecture easier to scale and maintain.

## Main infrastructure components

The project includes these supporting services:

- PostgreSQL: relational database for app metadata
- Kafka: asynchronous communication between services
- Kafka UI: monitoring for Kafka topics and messages
- InfluxDB: time-series database for energy usage data
- Keycloak: authentication and authorization
- Prometheus: system metrics collection
- Grafana: dashboards and observability
- Mailpit: local email testing

## End-to-end flow

A typical system flow looks like this:

1. A user logs in through the API gateway and Keycloak.
2. The user registers or manages devices in the device-service.
3. A smart device sends readings to the ingestion-service.
4. The ingestion-service validates and pushes the event into Kafka.
5. The usage-service consumes the event and stores usage data in InfluxDB.
6. The alert-service checks the data and detects threshold violations.
7. The insight-service analyzes the historic data and produces summaries.
8. Clients query the API gateway to view usage, alerts, and insights.

## Why this architecture is useful

This design gives the system several advantages:

- Each service has a clear responsibility
- New features can be added without affecting the whole system
- Kafka allows asynchronous and scalable communication
- Time-series data is stored in the right database for performance
- Alerts and insights can be processed independently from raw ingestion
- You can scale individual services separately

## Running the system

The project uses Docker Compose to start the required infrastructure services.

Typical setup steps:
1. Start the Docker containers
2. Ensure Kafka, PostgreSQL, InfluxDB, and Keycloak are running
3. Start the backend services with Maven or Spring Boot
4. Use the API gateway for app requests
5. Monitor the system with Grafana and Prometheus

## Summary

Home Energy Tracker is a microservice-based energy monitoring platform. Devices generate usage data, that data is ingested, processed, stored, and analyzed, and the system delivers real-time monitoring, trend analysis, and alerts to help users understand and optimize energy consumption.

This architecture is well-suited for systems that need to handle continuous measurement data, event-driven processing, and user-specific monitoring.
