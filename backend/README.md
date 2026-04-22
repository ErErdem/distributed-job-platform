# Backend

This directory contains the Maven multi-module backend for the distributed job platform.

Service modules:
- `control-plane-service`
- `scheduler-service`
- `worker-service`

Build command:

```bash
mvn clean verify
```

## Local Infrastructure

The local development infrastructure lives in [`docker-compose.yml`](./docker-compose.yml) and starts PostgreSQL, Kafka, and Redis only.

Start the infrastructure from `backend/`:

```bash
docker compose up -d
```

Stop the infrastructure:

```bash
docker compose down
```

Stop the infrastructure and remove named volumes:

```bash
docker compose down -v
```

If you want to override defaults, copy `.env.example` to `.env` and adjust the values before starting the stack.

### Exposed Ports

- PostgreSQL: `5432`
- Kafka: `9092`
- Redis: `6379`

### Future Local Connection Values

PostgreSQL:
- host: `localhost`
- port: `5432`
- database: `job_platform`
- username: `jobplatform`
- password: `jobplatform`

Redis:
- host: `localhost`
- port: `6379`

Kafka:
- bootstrap servers for apps running on your machine: `localhost:9092`
- bootstrap servers for future containers on the same Compose network: `kafka:9092`
