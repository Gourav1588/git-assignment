# Vehicle Rental System

A full-stack web application for renting vehicles. Users can browse, filter, and book cars and bikes. Admins manage the fleet, categories, and bookings through a dedicated dashboard.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 17 |
| Database | PostgreSQL |
| Security | Spring Security + JWT (stateless) |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, JaCoCo |
| Frontend | HTML, CSS, JavaScript |

---

## Features

### User
- Register and login with JWT authentication
- Browse vehicles with name search, type and category filters, pagination
- View vehicle details and daily price
- Create a booking — review summary before confirming
- Confirm or cancel own bookings
- View booking history and status
- Update profile details

### Admin
- Add, update and soft-delete vehicles
- Manage vehicle categories dynamically
- View all bookings 
- View all users

### Technical
- Stateless JWT authentication with role-based access control
- Booking overlap prevention with custom JPQL query
- Soft delete for vehicles — preserves historical booking records
- Stale PENDING booking cleanup via scheduled task
- Centralized exception handling with meaningful HTTP status codes
- Input validation on all endpoints using Jakarta Bean Validation
- IS NULL trick for dynamic vehicle filtering — single query handles all filter combinations
- Layered architecture — Controller → Service → Repository → Database

---

## Project Structure

```
Capstone_VehicleRentalSystem/
│
├── backend/rental/
│   └── src/
│       ├── main/java/com/vehicle/rental/
│       │   ├── config/
│       │   │   ├── AdminInitializer.java
│       │   │   ├── CorsConfig.java
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── BookingController.java
│       │   │   ├── CategoryController.java
│       │   │   ├── UserController.java
│       │   │   └── VehicleController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   ├── BookingRequest.java
│       │   │   │   ├── CategoryRequest.java
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   ├── UserProfileUpdateRequest.java
│       │   │   │   └── VehicleRequest.java
│       │   │   └── response/
│       │   │       ├── ApiResponse.java
│       │   │       ├── BookingResponse.java
│       │   │       ├── CategoryResponse.java
│       │   │       ├── UserResponse.java
│       │   │       └── VehicleResponse.java
│       │   ├── entity/
│       │   │   ├── Booking.java
│       │   │   ├── User.java
│       │   │   ├── Vehicle.java
│       │   │   └── VehicleCategory.java
│       │   ├── exception/
│       │   │   ├── BadRequestException.java
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── ResourceNotFoundException.java
│       │   │   ├── UnauthorizedAccessException.java
│       │   │   └── VehicleNotAvailableException.java
│       │   ├── mapper/
│       │   │   ├── BookingMapper.java
│       │   │   ├── CategoryMapper.java
│       │   │   ├── UserMapper.java
│       │   │   └── VehicleMapper.java
│       │   ├── repository/
│       │   │   ├── BookingRepository.java
│       │   │   ├── CategoryRepository.java
│       │   │   ├── UserRepository.java
│       │   │   └── VehicleRepository.java
│       │   ├── security/
│       │   │   ├── CustomUserDetails.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   └── JwtService.java
│       │   └── service/
│       │       ├── AuthService.java
│       │       ├── BookingService.java
│       │       ├── CategoryService.java
│       │       ├── UserService.java
│       │       └── VehicleService.java
│       │
│       └── test/java/com/vehicle/rental/
│           ├── controller/
│           │   ├── AuthControllerTest.java
│           │   ├── BookingControllerTest.java
│           │   ├── CategoryControllerTest.java
│           │   ├── UserControllerTest.java
│           │   └── VehicleControllerTest.java
│           └── service/
│               ├── AuthServiceTest.java
│               ├── BookingServiceTest.java
│               ├── CategoryServiceTest.java
│               ├── UserServiceTest.java
│               └── VehicleServiceTest.java
│
└── frontend/
    ├── config/config.js
    ├── css/
    │   ├── admin.css
    │   ├── auth.css
    │   ├── global.css
    │   ├── index.css
    │   ├── profile.css
    │   └── vehicles.css
    ├── js/
    │   ├── admin.js
    │   ├── auth.js
    │   ├── index.js
    │   ├── profile.js
    │   └── vehicles.js
    ├── admin.html
    ├── index.html
    ├── login.html
    ├── profile.html
    ├── register.html
    └── vehicles.html
```

---

## Database Schema

```
users
  id, name, email, password, role, created_at

vehicle_categories
  id, name, description

vehicles
  id, category_id (FK), name, type, is_active,
  description, price_per_day, created_at

bookings
  id, user_id (FK), vehicle_id (FK), start_date,
  end_date, total_cost, status, created_at
```

**Booking status lifecycle:**
```
PENDING → ACTIVE → COMPLETED
        ↘ CANCELLED
```

**Vehicle types:** CAR, BIKE

**User roles:** USER, ADMIN

---

## Setup

### Prerequisites

- Java 17
- PostgreSQL
- Maven 3.8+

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd Capstone_VehicleRentalSystem/backend/rental
```

### 2. Create the database

```sql
CREATE DATABASE vehicle_rental_db;
```

### 3. Configure environment

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

```env
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_secret_key_at_least_256_bits_long
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Application runs at `http://localhost:8080`

### 5. Open the frontend

Open `frontend/index.html` in your browser.

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <jwt_token>
```

### Auth
```
POST   /api/auth/register
POST   /api/auth/login
```

### Vehicles
```
GET    /api/vehicles                    # filter by name, type, category + paginate
GET    /api/vehicles/{id}
POST   /api/vehicles                    # ADMIN only
PUT    /api/vehicles/{id}               # ADMIN only
DELETE /api/vehicles/{id}               # ADMIN only — soft delete
```

**Vehicle filter params:**
```
?name=swift        search by name (case insensitive)
?type=CAR          filter by type: CAR or BIKE
?categoryId=1      filter by category
?page=0&size=10    pagination
```

### Bookings
```
POST   /api/bookings                    # create — returns PENDING booking
PUT    /api/bookings/{id}/confirm       # confirm — moves to ACTIVE
PUT    /api/bookings/{id}/cancel        # cancel
GET    /api/bookings/my                 # user's own bookings
GET    /api/bookings                    # ADMIN only — all bookings
GET    /api/bookings/status/{status}    # ADMIN only — filter by status
```

### Categories
```
POST   /api/categories                  # ADMIN only
```

### Users
```
GET    /api/users/me                    # current user profile
PUT    /api/users/me                    # update profile
```

---

## Running Tests

Run all tests:

```bash
mvn test
```

Generate JaCoCo coverage report:

```bash
mvn test jacoco:report
```

View report at:
```
target/site/jacoco/index.html
```


Tests are written for both the controller layer (MockMvc) and service layer (Mockito).

---

## Default Admin Account

The application automatically creates a default admin account on first startup
if one does not already exist.
```
Email:    admin@vehicle.com
Password: admin123
Change it after first login.

---

## Git Workflow

- `main` — stable branch, target for pull requests
- `develop` — active development branch


---

## Author

Gourav Yadav — Java Training Capstone Project, 2026