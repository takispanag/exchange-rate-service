# Exchange Rate Service

A Spring Boot microservice that provides real-time currency exchange rates and conversion capabilities with caching
support.

## Features

- Real-time exchange rate fetching
- Single and multi-currency conversions
- Cached exchange rates with automatic refresh
- RESTful API with OpenAPI documentation
- Comprehensive validation and error handling

## Tech Stack

- Java 21
- Spring Boot
- Caffeine Cache
- OpenAPI/Swagger
- RestClient
- JUnit 5
- Docker

## API Endpoints

### Exchange Rates

- `GET /api/v1/exchange/rates/single` - Get exchange rate between two currencies
- `GET /api/v1/exchange/rates/all` - Get all exchange rates for a base currency

### Currency Conversion

- `GET /api/v1/exchange/convert/single` - Convert amount between two currencies
- `POST /api/v1/exchange/convert/multiple` - Convert amount to multiple currencies

## Getting Started

### Prerequisites

- Java 21
- Docker
- API Key from currency exchange provider

### Configuration

Key application properties:

1. `exchange.rate.provider-api.key`: API key for the exchange rate provider
2. `exchange.rate.cache.duration-seconds`: Cache duration in seconds before entries expire
3. `exchange.rate.cache.refresh-interval-ms`: Scheduler interval in milliseconds for refreshing rates
4. `exchange.rate.preload.currencies`: List of base currencies to preload in cache

### Currency Validation

The `@ValidCurrency` annotation provides automatic validation of currency codes against a configured list in
`application.yml`.

```yaml
# application.yml
currencies:
  supported:
    - USD
    - EUR
    - GBP
    - ...
```

### Running with Docker

1. Clone the repository:

```bash
git clone https://github.com/takispanag/exchange-rate-service
cd exchange-rate-service
```

2. Start the application:

```bash
docker-compose up
```

The application will be available at `http://localhost:8080`

### Running Locally

1. Build the project:

```bash
./gradlew clean build
```

2. Run the application:

```bash
./gradlew bootRun
```

### Caching Mechanism

The service implements a two-layer caching approach optimized for clients who accept data with up to 1-minute delay:

#### Preloading

On application startup and at fixed intervals, the service proactively fetches and caches exchange rates for configured
base currencies (`exchange.rate.preload.currencies`). This ensures commonly used rates are immediately available.

Essentially, we preload the exchange rates for the most used currencies every *51 seconds* and we also expire the cache
entries 50 seconds after it's written to ensure our data are not stale.

#### Cache Management

* Exchange rates are cached for 1 minute (`exchange.rate.cache.duration-minutes=1`)
* The cache is automatically refreshed at fixed intervals (`exchange.rate.cache.refresh-interval`) with 51 seconds
  default
* Before loading new rates, the cache is explicitly cleared to ensure fresh data
* Individual cache entries expire automatically after the configured duration (default 1 minute)

### API Documentation

Access the Swagger UI documentation at `http://localhost:8080/swagger-ui.html`

### Testing

Run the test suite:

```bash
./gradlew test
```

And find your coverage report in: `build/reports/jacoco/test/html/index.html`

### Future improvements

* Add preloading mechanism to other parts of the functionality (e.g. currency conversion of most used currencies)
* Auth using JWT
* Monitoring
* Add more external providers