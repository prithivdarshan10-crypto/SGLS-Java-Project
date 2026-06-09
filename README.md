# Smart Global Logistics & Warehouse Management System (SGLS)

> Enterprise-grade logistics platform for import/export organizations to manage inventory, suppliers, shipments, warehouses, employees, and analytics.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 + Spring Boot 3.2 |
| Security | Spring Security + JWT (JJWT) |
| Database | MySQL 8 + Spring Data JPA (Hibernate) |
| Frontend | Thymeleaf + Bootstrap 5 |
| Build | Maven |
| Deployment | Render (free tier) |

---

## Architecture

```
Frontend (Bootstrap + Thymeleaf)
         │  HTTP + JWT
         ▼
┌─── Controller Layer (@RestController) ───────────────┐
│  AuthController  WarehouseController  InventoryCtrl  │
│  ShipmentCtrl    SupplierCtrl         EmployeeCtrl    │
└───────────────────────────────────────────────────────┘
         │
         ▼
┌─── Service Layer (@Service) ─────────────────────────┐
│  AuthService  WarehouseService  InventoryService      │
│  ShipmentService  SupplierService  EmployeeService    │
└───────────────────────────────────────────────────────┘
         │
         ▼
┌─── Repository Layer (JpaRepository) ─────────────────┐
│  UserRepo  WarehouseRepo  ProductRepo  ShipmentRepo   │
└───────────────────────────────────────────────────────┘
         │
         ▼
┌─── MySQL Database ────────────────────────────────────┐
│  users  warehouses  products  shipments  suppliers    │
│  employees  purchase_orders  attendance               │
└───────────────────────────────────────────────────────┘
```

---

## Modules

- [x] Module 1: Authentication & Authorization (JWT + RBAC)
- [ ] Module 2: Warehouse Management
- [ ] Module 3: Inventory Management
- [ ] Module 4: Supplier Management
- [ ] Module 5: Shipment Management
- [ ] Module 6: Employee Management
- [ ] Module 7: Analytics Dashboard

---

## Quick Start (Local)

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.9+

### 1. Clone
```bash
git clone https://github.com/YOUR_USERNAME/smart-global-logistics.git
cd smart-global-logistics
```

### 2. Database Setup
```sql
CREATE DATABASE sgls_db;
-- Spring Boot will create tables automatically via ddl-auto=update
```

### 3. Configure
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sgls_db
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 4. Run
```bash
mvn spring-boot:run
```

App starts at `http://localhost:8080`

### Default Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Manager | `manager1` | `manager123` |
| Employee | `employee1` | `employee123` |

---

## API Documentation

### Authentication

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/login` | Public | Login, returns JWT |
| POST | `/api/auth/register` | ADMIN | Create new user |
| GET | `/api/auth/me` | Authenticated | Get current user |
| POST | `/api/auth/logout` | Authenticated | Logout |

### Request/Response Examples

**Login:**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

// Response 200:
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "type": "Bearer",
    "username": "admin",
    "role": "ADMIN",
    "expiresIn": 86400000
  }
}
```

**Using the token:**
```
Authorization: Bearer eyJhbGci...
```

---

## Deployment (Render)

### Environment Variables to set in Render Dashboard:

| Variable | Value |
|----------|-------|
| `DB_URL` | `jdbc:mysql://YOUR_RENDER_MYSQL_HOST/sgls_db` |
| `DB_USERNAME` | your MySQL username |
| `DB_PASSWORD` | your MySQL password |
| `JWT_SECRET` | any long random string (32+ chars) |
| `DDL_AUTO` | `update` |

### Build Command:
```
mvn clean package -DskipTests
```

### Start Command:
```
java -jar target/smart-global-logistics-1.0.0.jar
```

---

## Project Structure

```
src/main/java/com/sgls/
├── SglsApplication.java          # Entry point + data seeder
├── config/
│   └── WebSecurityConfig.java    # Spring Security configuration
├── controller/
│   └── AuthController.java       # REST endpoints
├── dto/
│   ├── request/                  # Input DTOs
│   └── response/                 # Output DTOs
├── entity/
│   └── User.java                 # JPA entities
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
├── repository/
│   └── UserRepository.java       # Spring Data JPA repos
├── security/
│   ├── AuthTokenFilter.java      # JWT filter
│   ├── JwtUtils.java             # JWT generation/validation
│   └── UserDetailsServiceImpl.java
└── service/
    └── AuthService.java          # Business logic
```

---

## Author

**Priyadarshan S V**
B.Tech AIML — SRM IST Tiruchirappalli
[GitHub](https://github.com/prithivdarshan10-crypto) | [LinkedIn](https://linkedin.com/in/darshan-7039bb352)
