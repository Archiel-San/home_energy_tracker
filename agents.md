# Agents

This document describes the autonomous agents and background services in the Home Energy Tracker system.

## Overview

The system uses event-driven architecture with Kafka-based message passing to enable loosely coupled services that act as agents, consuming and producing domain events asynchronously.

## Services as Agents

### 1. **Ingestion Agent** (Ingestion Service)
**Role:** Entry point for energy data collection  
**Triggers:**
- HTTP POST requests from smart meters or IoT devices
- Device registration events from Device Service

**Actions:**
- Parse incoming energy readings (kWh, timestamp, device ID)
- Publish `EnergyReadingEvent` to Kafka topic `energy-readings`
- Validate data against device metadata
- Handle duplicate detection and retry logic

**State:** In-memory buffer + REST endpoint state  
**Output Topics:** `energy-readings`

---

### 2. **Usage Agent** (Usage Service)
**Role:** Time-series aggregation and metrics computation  
**Triggers:**
- Consumes `EnergyReadingEvent` from `energy-readings` topic

**Actions:**
- Aggregate raw readings into hourly/daily/monthly buckets
- Calculate consumption trends and baseline usage
- Write metrics to InfluxDB with tags (device ID, user ID, location)
- Publish `UsageMetricsEvent` for downstream consumers

**State:** InfluxDB time-series store  
**Input Topics:** `energy-readings`  
**Output Topics:** `usage-metrics`

---

### 3. **Alert Agent** (Alert Service)
**Role:** Threshold monitoring and anomaly detection  
**Triggers:**
- Consumes `UsageMetricsEvent` from `usage-metrics` topic
- Periodically checks user-defined thresholds

**Actions:**
- Compare current usage against configurable limits (daily/monthly caps)
- Detect anomalies (sudden spikes vs. baseline)
- Generate `AlertEvent` when threshold exceeded
- Send email notifications via Mailpit SMTP
- Log alerts to PostgreSQL for user dashboard

**State:** PostgreSQL alert history + in-memory threshold cache  
**Input Topics:** `usage-metrics`  
**Output Topics:** `alerts`

---

### 4. **Insight Agent** (Insight Service)
**Role:** AI-powered recommendations and analysis  
**Triggers:**
- Scheduled batch jobs (e.g., daily at 9 AM)
- Manual trigger via REST endpoint
- Consumes `UsageMetricsEvent` for real-time insights

**Actions:**
- Query historical usage patterns from InfluxDB
- Send aggregated metrics to Ollama LLM
- Generate natural language insights and recommendations
- Store insights in PostgreSQL for user retrieval
- Optionally publish `InsightEvent` to Kafka

**External Dependencies:** Ollama LLM (local)  
**State:** PostgreSQL insight history + InfluxDB time-series  
**Input Topics:** `usage-metrics` (optional)

---

### 5. **User Agent** (User Service)
**Role:** Identity, authentication, and user preferences  
**Triggers:**
- REST API calls from API Gateway
- Scheduled tasks (user quota resets, session cleanup)

**Actions:**
- Manage user registration and authentication
- Store user preferences (alert thresholds, notification settings)
- Maintain user-device relationships
- Audit login attempts and API access

**State:** PostgreSQL user store  
**No Kafka integration** (synchronous only)

---

### 6. **Device Agent** (Device Service)
**Role:** Smart meter registry and metadata  
**Triggers:**
- REST API calls for device onboarding
- Manual device registration via admin portal

**Actions:**
- Register new smart meters with unique device IDs
- Store device location, capacity, and model info
- Publish `DeviceRegisteredEvent` to Kafka
- Validate device credentials for ingestion

**State:** PostgreSQL device registry  
**Output Topics:** `device-events`

---

### 7. **Gateway Agent** (API Gateway)
**Role:** Request routing, rate limiting, and circuit breaking  
**Triggers:**
- All HTTP requests from external clients

**Actions:**
- Route requests to appropriate downstream service
- Apply rate limiting per user
- Implement circuit breakers for fault tolerance
- Log all API traffic for audit trails

**State:** In-memory circuit breaker state, Spring Security session tokens  
**No Kafka integration**

---

## Event Flow Diagram

```
┌─────────────┐
│   Devices   │ (HTTP POST /readings)
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│ Ingestion Service    │──┐ publish
│ (REST Endpoint)      │  │
└──────────────────────┘  │
                          ▼
                   energy-readings (Kafka)
                          │
       ┌──────────────────┼──────────────────┐
       │                  │                  │
       ▼                  ▼                  ▼
 ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
 │Usage Service │  │Insight Agent │  │Other Agents  │
 │(Aggregation) │  │   (Polling)  │  │   (Future)   │
 └──────┬───────┘  └──────┬───────┘  └──────────────┘
        │                 │
        │ publish      read metrics
        │                 │
        ▼                 ▼
   usage-metrics    InfluxDB (Query)
        │                 │
        │                 ▼
        │          ┌──────────────────┐
        │          │ Insight Service  │
        │          │ (LLM Analysis)   │
        │          └──────┬───────────┘
        │                 │
        │                 ▼
        │          PostgreSQL insights
        │
        ▼
  ┌──────────────┐
  │Alert Service │────────┐ send
  │(Thresholds)  │        │
  └──────────────┘        │
                          ▼
                      Mailpit (Email)
```

---

## Configuration & Deployment

**Kafka Topics to Create:**
```bash
docker exec -it kafka kafka-topics.sh --create --topic energy-readings --bootstrap-server localhost:9092
docker exec -it kafka kafka-topics.sh --create --topic usage-metrics --bootstrap-server localhost:9092
docker exec -it kafka kafka-topics.sh --create --topic alerts --bootstrap-server localhost:9092
docker exec -it kafka kafka-topics.sh --create --topic device-events --bootstrap-server localhost:9092
```

**Environment Variables (per service):**
- `KAFKA_BROKERS` – Kafka bootstrap servers (default: `localhost:9092`)
- `POSTGRES_URL` – Database connection string
- `INFLUXDB_URL` – InfluxDB API endpoint
- `OLLAMA_URL` – Ollama LLM server (default: `http://localhost:11434`)
- `MAIL_HOST` – SMTP server for alerts (Mailpit: `localhost:1025`)

---

## Future Enhancements

- **Prediction Agent:** ML model that forecasts future usage based on historical trends
- **Optimization Agent:** Suggests load-shifting recommendations to reduce peak consumption
- **Billing Agent:** Generates invoices and cost breakdowns
- **Multi-tenant Agent:** Supports aggregation across multiple properties or organizations
