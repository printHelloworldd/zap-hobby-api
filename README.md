# Hobby API

A **Spring Boot** application with **GraphQL**, **PostgreSQL**, **Redis**, and **Nginx load balancing**.
The system is designed to store and retrieve hobbies data with caching support.
Nginx is used as a **load balancer** with a **Round Robin** strategy to distribute traffic between two API instances.

---

## 🚀 How to Run

1. Create a `.env` file in the project root with the following variables:

    ```env
    POSTGRES_URL=jdbc:postgresql://postgres:5432/db-name
    POSTGRES_DB=db-name
    POSTGRES_USER=postgres-user
    POSTGRES_PASSWORD=postgres-password
    REDIS_PASSWORD=redis-password
    REDIS_URL=redis://:redis-password@redis:6379
    REDIS_HOST=redis
    REDIS_PORT=6379
    ```

2. **Build and start with Docker Compose**:

   ```bash
   docker-compose up --build
   ```

3. **Default Access Points**:

   * API (via Nginx Load Balancer): `http://localhost:80`
   * Direct API instance #1: `http://localhost:8081`
   * Direct API instance #2: `http://localhost:8082`
   * PostgreSQL: `localhost:5332`
   * Redis: `localhost:6379`

---

## 🧩 Example GraphQL Query

**Request:**

```graphql
query {
  hobbies {
    id
    name
    iconLink
    categories {
        id
        name
        hobbies {
            name
        }
    }
  }
}
```

**Response:**

```json
{
  "data": {
    "hobbies": [
      {
        "id": "1",
        "name": "Air sports",
        "iconLink": "https://cdn-icons-png.flaticon.com/128/1384/1384739.png",
        "categories": [
          {
            "id": "2",
            "name": "Outdoors and sports",
            "hobbies": [
              {
                "name": "Air sports"
              },
              {
                "name": "Airsoft"
              },
              {
                "name": "Amateur geology"
              },
            ],
          }
        ]
      }
    ]
  }
}
```

---

## 🧩 Example REST request

**Request:**

```sh
curl -v http://localhost:80/api/v1/hobbies
```

---

## 📦 Tech Stack

* **Java 17**
* **Spring Boot** (Web, GraphQL, Data JPA, Redis)
* **PostgreSQL** – main database
* **Redis** – caching
* **Nginx** – load balancer (Round Robin)
* **Docker & Docker Compose**

---

## 📚 Dependencies

- **Spring Boot**
  - `spring-boot-starter-web` – REST API
  - `spring-boot-starter-data-jpa` – ORM support
  - `spring-boot-starter-graphql` – GraphQL API
  - `spring-boot-starter-data-redis` – Redis cache integration
- **Database**
  - `postgresql` – PostgreSQL JDBC driver
- **Caching**
  - `jedis` – Redis client
- **Rate Limiting**
  - `bucket4j-core` – API rate limiting
- **Testing**
  - `spring-boot-starter-test` – Unit & integration testing
  - `httpclient5` – HTTP client for tests

---

## 🏗 Architecture
![Architecture Diagram](hobby-api-arch-dark.png)


## 📜 License

MIT
