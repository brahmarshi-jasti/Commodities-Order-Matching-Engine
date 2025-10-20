# Low-Latency Commodities Order Matching Engine

## Overview
High-performance commodities order matching engine for oil, metals, and other commodities. Built with Java Spring Boot backend and React frontend for real-time monitoring.

**Last Updated:** October 20, 2025

## Features
- Sub-millisecond order matching with price-time priority algorithm
- LMAX Disruptor pattern for low-latency message processing
- Real-time WebSocket streaming for order book updates
- In-memory order book supporting limit and market orders
- Performance metrics tracking (latency, throughput, fill rates, slippage)
- Web dashboard for visualization and monitoring
- Production-ready configuration files for Kafka, Kubernetes, Prometheus, and Grafana

## Tech Stack

### Backend
- Java 17 with Spring Boot 3.1.5
- LMAX Disruptor for high-performance event processing
- WebSocket for real-time communication
- Micrometer + Prometheus for metrics export
- Maven for dependency management

### Frontend
- React 18 with TypeScript
- Recharts for data visualization
- WebSocket client for real-time updates
- Vite for build tooling

### Infrastructure (Config Files Provided)
- Apache Kafka for event streaming
- Apache Flink for streaming analytics
- Kubernetes for orchestration
- Prometheus for metrics collection
- Grafana for visualization

## Project Structure
```
├── src/main/java/com/commodities/matching/
│   ├── MatchingEngineApplication.java
│   ├── config/
│   ├── model/
│   ├── engine/
│   ├── controller/
│   └── metrics/
├── frontend/
│   ├── src/
│   ├── public/
│   └── package.json
├── k8s/
├── kafka/
├── monitoring/
└── pom.xml
```

## Performance Targets
- Order matching latency: < 100 microseconds
- Throughput: 50,000+ orders per second
- Order book updates: Real-time via WebSocket

## Recent Changes
- Initial project setup with Java and React
- Core order matching engine implementation
- WebSocket integration for real-time updates
- Performance metrics and monitoring dashboard
