# Starting The Server

To start the server, run the following command from the project root:

```bash
./mvnw spring-boot:run
```

Or on Windows:

```bash
mvnw.cmd spring-boot:run
```

The server will start on the default port 8080.

## Running Tests

To run the tests, execute the following command:

```bash
./mvnw test
```

Or on Windows:

```bash
mvnw.cmd test
```

## Example API Calls

```bash
curl -X POST http://localhost:8080/api/order -H 'Content-Type: application/json' -d '{"currencyPair": "USDEUR", "dealtCurrency": "USD", "direction":"BUY", "valueDate": "20250519", "userId":"Justin", "amount":100.00}' 
```

```json
{
  "currencyPair": "USDEUR",
  "dealtCurrency": "USD",
  "direction": "BUY",
  "amount": 100.0,
  "valueDate": "20250519",
  "userId": "Justin"
}
```

```bash
curl -X POST http://localhost:8080/api/order -H 'Content-Type: application/json' -d '{"currencyPair": "USDEUR", "dealtCurrency": "USD", "direction":"SELL", "valueDate": "20250519", "userId":"Amanda", "amount":50.00}' 
```

```json
{
  "currencyPair": "USDEUR",
  "dealtCurrency": "USD",
  "direction": "SELL",
  "amount": 50.0,
  "valueDate": "20250519",
  "userId": "Amanda"
}
```

```bash
curl http://localhost:8080/api/match/Justin 
```

```json
[
  {
    "currencyPair": "USDEUR",
    "dealtCurrency": "USD",
    "direction": "BUY",
    "amount": 100.0,
    "valueDate": "20250519",
    "userId": "Justin",
    "createdAt": 1747643697325,
    "match": 0.5
  }
]
```

## Technology Stack

- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA w/ SQLite

## Project Structure

The application follows a standard Spring Boot project structure:

- `src/main/java`: Source code
- `src/test/java`: Test code
- `src/main/resources`: Configuration files
