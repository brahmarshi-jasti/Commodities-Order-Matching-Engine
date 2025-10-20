# Low-Latency Commodities Order Matching Engine

A high-performance commodities order matching engine designed for sub-100 microsecond latency, processing 50K+ orders per second.

## Features

- **Ultra-Low Latency**: Sub-100µs order matching using LMAX Disruptor pattern
- **High Throughput**: Capable of processing 50,000+ orders per second
- **Real-Time Analytics**: Live metrics tracking for latency, fill rates, and slippage
- **Multi-Commodity Support**: Oil, Gold, Silver, Copper, and Natural Gas
- **Production Ready**: Kubernetes deployment configs with auto-scaling
- **Comprehensive Monitoring**: Prometheus metrics and Grafana dashboards
- **Database Persistence**: H2/PostgreSQL support for trade history
- **API Documentation**: Interactive Swagger/OpenAPI documentation
- **Health Checks**: Spring Boot Actuator with custom health indicators
- **Error Handling**: Global exception handling and validation
- **Comprehensive Testing**: Unit and integration tests with JUnit 5

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
- `GET /actuator/health` - Health check endpoint
- `GET /actuator/info` - Application information

### WebSocket
- `/ws` - WebSocket endpoint for real-time updates
- `/topic/trades` - Trade stream
- `/topic/orders` - Order stream

### API Documentation
- `GET /swagger-ui.html` - Interactive API documentation
- `GET /v3/api-docs` - OpenAPI JSON specification

### Database Console (Development)
- `GET /h2-console` - H2 database console (in-memory database)

## API Examples

### Submit a Market Buy Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "commodity": "OIL",
    "side": "BUY",
    "type": "MARKET",
    "price": 0,
    "quantity": 100
  }'
```

### Submit a Limit Sell Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "commodity": "GOLD",
    "side": "SELL",
    "type": "LIMIT",
    "price": 1850.50,
    "quantity": 10
  }'
```

### Get Order Book for Gold
```bash
curl http://localhost:8080/api/orderbook/GOLD
```

### Get Performance Metrics
```bash
curl http://localhost:8080/api/metrics
```

### Check Application Health
```bash
curl http://localhost:8080/actuator/health
```

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
- **Frontend**: React 18, TypeScript, Vite, Recharts, TailwindCSS
- **Database**: H2 (development), PostgreSQL (production ready)
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito, AssertJ
- **Streaming**: Apache Kafka, Apache Flink
- **Monitoring**: Prometheus, Grafana, Spring Boot Actuator
- **Orchestration**: Kubernetes, Docker

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage Report
After running tests with coverage, view the report at:
`target/site/jacoco/index.html`

## Development Guide

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm (for frontend)
- Docker and Docker Compose (optional, for full stack)

### Environment Variables
```bash
# Backend Configuration
SPRING_PROFILES_ACTIVE=development
SERVER_PORT=8080

# Database Configuration (Optional - defaults to H2)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/matchingdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
```

### Logging
Logs are written to:
- Console (stdout)
- File: `logs/matching-engine.log`

Log levels can be configured in `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.commodities.matching: DEBUG
```

## Performance Tuning

### JVM Options for Production
```bash
java -Xms4G -Xmx4G \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=10 \
  -XX:+UseStringDeduplication \
  -jar matching-engine.jar
```

### Disruptor Configuration
Adjust ring buffer size in `application.yml`:
```yaml
matching-engine:
  disruptor:
    ring-buffer-size: 65536  # Must be power of 2
    wait-strategy: blocking  # Options: blocking, sleeping, yielding, busy-spin
```

## Troubleshooting

### Connection Issues
- Ensure backend is running on port 8080
- Check CORS configuration in `CorsConfig.java`
- Verify WebSocket connection at `/ws`

### High Latency
- Check system load and CPU usage
- Review GC logs for long pauses
- Increase ring buffer size if needed
- Consider using busy-spin wait strategy for lowest latency

### Database Issues
- H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:matchingdb`
- Username: `sa`, Password: (empty)

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

MIT
