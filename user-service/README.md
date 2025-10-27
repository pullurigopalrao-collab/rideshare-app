# 🚗 RideShare App - User Service

## 📘 Overview
**User Service** is a core microservice in the RideShare platform responsible for user registration, authentication, and profile management.  
It integrates with PostgreSQL for persistence, Redis for caching, and Kafka for future event streaming.

---

## 🧩 Tech Stack

| Component | Technology |
|------------|-------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.x |
| **Database** | PostgreSQL |
| **Cache** | Redis |
| **Messaging** | Kafka (Planned Integration) |
| **Build Tool** | Maven |
| **Testing** | JUnit 5, Mockito |
| **Containerization** | Docker & Docker Compose |
| **Logging** | Centralized logging (Planned - Sleuth / Zipkin) |

---

## ⚙️ Features

✅ User Registration (Rider / Owner)  
✅ OTP-Based Login (Planned)  
✅ Role-based Access (RIDER / OWNER / BOTH)  
✅ Profile Retrieval with Redis Caching  
✅ Centralized Exception Handling  
✅ Validation for Mobile Numbers  
✅ JUnit + Mockito Test Coverage  
✅ Docker-based Multi-service Environment

---

## 🗂️ Folder Structure
rideshare-app/
└── userservice/
├── src/
│ ├── main/java/com/rideshare/userservice/
│ │ ├── controller/ # REST Controllers
│ │ ├── dto/ # Data Transfer Objects
│ │ ├── entity/ # JPA Entities
│ │ ├── exception/ # Custom Exceptions
│ │ ├── repository/ # JPA Repositories
│ │ ├── service/ # Business Logic
│ │ └── config/ # App Configuration
│ └── test/java/... # JUnit + Mockito Tests
├── Dockerfile
├── pom.xml
└── application.yml

yaml
Copy code

---

## 🐳 Docker Compose Setup

`docker-compose.yml` (in root `/rideshare-app` folder) includes:

- PostgreSQL
- Redis
- Kafka & Zookeeper
- Kafka UI
- pgAdmin

### 🚀 Run all containers
```bash
docker-compose up -d
🧾 Verify running containers
bash
Copy code
docker ps
🧠 Run the Application
Step 1️⃣ - Clean and build
bash
Copy code
mvn clean install -DskipTests
Step 2️⃣ - Run Spring Boot App
bash
Copy code
mvn spring-boot:run
Application starts at:
🔗 http://localhost:8080

🔑 Redis Cache Verification
Connect to Redis (Windows CMD)
bash
Copy code
docker exec -it redis redis-cli
View cached keys
bash
Copy code
KEYS *
Delete specific key (if needed)
bash
Copy code
DEL userProfiles::9999999999
🧪 Run Tests
Run all JUnit + Mockito tests:

bash
Copy code
mvn test
Includes coverage for:
✅ UserService (with mock repositories & ModelMapper)

✅ UserController (MockMvc tests for endpoints)

✅ Exception handler responses (400, 404, etc.)

✅ Redis cache integration

📡 API Endpoints
👤 Register User
POST /api/users/register

Request:

json
Copy code
{
  "firstName": "John",
  "lastName": "Doe",
  "mobileNumber": "9999999999",
  "role": "RIDER"
}
Response:

json
Copy code
{
  "success": true,
  "message": "Registration successful for John"
}
🔍 Get All Users
GET /api/users

Returns a list of all registered users with their role names.

🪪 Get User Profile
GET /api/users/admin/profile/{mobileNumber}

Example:

ruby
Copy code
GET http://localhost:8080/api/users/admin/profile/9999999999
Returns cached user profile (if exists).

⚠️ Error Response Example
json
Copy code
{
  "timestamp": "2025-10-27T19:40:00",
  "status": 400,
  "error": "Invalid Mobile Number",
  "message": "Please provide a valid 10-digit mobile number."
}
🔍 Future Enhancements
Centralized logging with Trace ID / Span ID

JWT-based Authentication Service

API Gateway Integration

Distributed Tracing with Zipkin

User Profile Update & Delete APIs

## 👨‍💻 Author
**Gopal Rao Pulluri**  
💼 GitHub Repo: [pullurigopalrao-collab/rideshare-app](https://github.com/pullurigopalrao-collab/rideshare-app)


🏁 License
This project is licensed under the MIT License.

yaml
Copy code
