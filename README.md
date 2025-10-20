# Low-Latency Commodities Order Matching Engine

A high-performance commodities order matching engine designed for sub-100 microsecond latency, processing 50K+ orders per second.

## Features

- **Ultra-Low Latency**: Sub-100µs order matching using LMAX Disruptor pattern
- **High Throughput**: Capable of processing 50,000+ orders per second
- **Real-Time Analytics**: Live metrics tracking for latency, fill rates, and slippage
- **Multi-Commodity Support**: Oil, Gold, Silver, Copper, and Natural Gas
- **Production Ready**: Kubernetes deployment configs with auto-scaling
- **Comprehensive Monitoring**: Prometheus metrics and Grafana dashboards

## Architecture

### Core Components

1. **Matching Engine** (Java/Spring Boot)
   - LMAX Disruptor for low-latency event processing
   - Price-time priority algorithm
   - In-memory order book with concurrent data structures

2. **Real-Time Dashboard** (React/TypeScript)
   - WebSocket-based live updates
   - Order book visualization
   - Performance metrics and latency charts

3. **Streaming Analytics** (Apache Flink - config provided)
   - Real-time P&L computation
   - Slippage analysis
   - Fill rate monitoring

4. **Message Broker** (Apache Kafka - config provided)
   - Order event streaming
   - Trade persistence
   - Metrics export

## Quick Start

### Local Development

```bash
# Start the backend
mvn spring-boot:run

# In a separate terminal, start the frontend
cd frontend
npm run dev
```

Access the dashboard at `http://localhost:5000`

### Production Deployment

#### Docker Compose
```bash
docker-compose up -d
```

#### Kubernetes
```bash
# Apply configurations
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/hpa.yaml

# Verify deployment
kubectl get pods -l app=matching-engine
```

## Configuration Files

### Kafka Configuration
- `kafka/kafka-producer.properties` - Producer settings with compression and batching
- `kafka/kafka-consumer.properties` - Consumer group configuration
- `kafka/topics.txt` - Topic definitions for orders, trades, and metrics

### Kubernetes Configuration
- `k8s/deployment.yaml` - Deployment with pod anti-affinity for resource optimization
- `k8s/configmap.yaml` - Application configuration
- `k8s/hpa.yaml` - Horizontal Pod Autoscaler for automatic scaling

### Monitoring Configuration
- `monitoring/prometheus.yml` - Prometheus scrape configuration
- `monitoring/alert_rules.yml` - Latency and throughput alerts
- `monitoring/grafana-dashboard.json` - Pre-built Grafana dashboard

## Performance Metrics

- **Latency**: < 100 microseconds (target)
- **Throughput**: 50,000+ orders/second
- **Fill Rate**: Tracked per commodity
- **Slippage**: Real-time calculation and monitoring

## API Endpoints

### Order Management
- `POST /api/orders` - Submit new order
- `GET /api/orderbook/{commodity}` - Get order book depth

### Metrics
- `GET /api/metrics` - Engine performance metrics
- `GET /actuator/prometheus` - Prometheus metrics export

### WebSocket
- `/ws` - WebSocket endpoint for real-time updates
- `/topic/trades` - Trade stream
- `/topic/orders` - Order stream

## Monitoring

### Prometheus Metrics
- `matching_engine_total_orders` - Total orders processed
- `matching_engine_total_trades` - Total trades executed
- `matching_engine_avg_latency_micros` - Average latency
- `matching_engine_processing_time_nanos` - Processing time histogram

### Grafana Dashboards
Import the dashboard from `monitoring/grafana-dashboard.json` to visualize:
- Order matching latency with P99 percentiles
- Throughput (orders and trades per minute)
- Fill rates by commodity
- System resource utilization

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.1.5, LMAX Disruptor
- **Frontend**: React 18, TypeScript, Vite, Recharts
- **Streaming**: Apache Kafka, Apache Flink
- **Monitoring**: Prometheus, Grafana
- **Orchestration**: Kubernetes, Docker

## License

MIT
