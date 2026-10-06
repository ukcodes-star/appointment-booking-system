# 📅 SlotBook — Role-Based Appointment Booking System

An enterprise-grade, slot-based appointment booking system built for hackathons and production environments. Features dynamic slot derivation without slot database storage, concurrency-safe transactional bookings, strict role-based access control (RBAC), and a responsive React + Tailwind CSS dashboard.

---

## 🚀 Key Highlights & Architectural Innovations

1. **Zero-Storage Dynamic Slot Derivation Engine**:
   - Time slots are **never stored** in the database.
   - Sliced dynamically on demand from weekly recurring provider availability windows (`30`, `60`, `90`, `120` minute intervals).
   - Past dates, passed hours of today, and overlapping booked appointments are eliminated on the fly.
   - Deterministic Slot ID format: `<serviceId>_<YYYY-MM-DD>_<HH:MM>`.

2. **Race-Condition & Concurrency Safe**:
   - Guaranteed single-booking under high load using a database-level unique constraint on `slotId` combined with Spring transactional atomicity (`@Transactional`).
   - Automatically catches concurrency conflicts and responds with `409 Conflict`.

3. **Strict Domain Validation & Constraints**:
   - 24-hour time formatting (`HH:MM`) strictly restricted to `:00` and `:30` minutes.
   - Service duration restricted to 30–120 minutes in multiples of 30.
   - Service Providers cannot self-book their own services (`403 Forbidden`).

---

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 19, Vite, Tailwind CSS v4, Lucide Icons, Axios |
| **Backend** | Java 23 / Spring Boot 3.3.5, Spring Security 6, JJWT 0.12.6, Hibernate / JPA |
| **Database** | PostgreSQL 16 (Dockerized) |
| **DevOps** | Docker Compose, Maven Wrapper |

---

## 🏛️ System Architecture

```text
┌────────────────────────────────────────────────────────┐
│               Frontend (React 19 + Vite)               │
│      Provider Dashboard   │   Customer Booking Flow    │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP + Bearer JWT
                            ▼
┌────────────────────────────────────────────────────────┐
│            Spring Boot 3.3.5 (Port 8080)               │
│  ├── SecurityFilter (Stateless JWT + RBAC)             │
│  ├── Dynamic Slot Engine (Zero-Storage derivation)     │
│  ├── Appointment Service (Transactional Booking)       │
│  └── Global Exception Handler (RFC 7807 compatible)    │
└───────────────────────────┬────────────────────────────┘
                            │ PostgreSQL Dialect
                            ▼
┌────────────────────────────────────────────────────────┐
│          PostgreSQL 16 (Docker Host Port 5433)         │
│  ├── users (id, name, email, password_hash, role)      │
│  ├── services (id, name, type, duration, provider_id)  │
│  ├── availabilities (service_id, day_of_week, window)  │
│  └── appointments (user_id, service_id, date, slot_id) │
└────────────────────────────────────────────────────────┘
```

---

## ⚡ Quickstart Guide (Run in 2 Minutes)

### 1. Start PostgreSQL (Docker)
In the project root folder:
```bash
docker compose up -d
```
*(Runs PostgreSQL on host port `5433` with database `appointment_db`)*

### 2. Start Spring Boot Backend
In `backend/`:
```bash
./mvnw spring-boot:run
# Or run the pre-built jar directly:
java -jar target/appointment-booking-0.0.1-SNAPSHOT.jar
```
*Backend runs on `http://localhost:8080`.*

### 3. Start React Frontend
In `frontend/`:
```bash
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`.*

---

## 🔑 Pre-Seeded Demo Accounts

The database comes pre-populated on startup with ready-to-test accounts and services:

| Role | Email | Password | Pre-configured Data |
|---|---|---|---|
| **Service Provider (Doctor)** | `doctor@clinic.com` | `Password123` | "General Health Consultation" (30 min), Mon–Fri 09:00–17:00 |
| **Service Provider (Trainer)** | `trainer@gym.com` | `Password123` | "Personal Fitness Training" (60 min), Mon–Sat 08:00–16:00 |
| **Customer / User** | `user@example.com` | `Password123` | Ready to browse and book |

---

## 📋 2-Minute Live Demo Script for Evaluators

1. **Step 1 — Provider Experience (`doctor@clinic.com`)**:
   - Sign in as `doctor@clinic.com` / `Password123`.
   - Navigate to **Provider Portal**: View existing services, set new weekly availability windows, or view the **Daily Schedule** grouped by service.

2. **Step 2 — Customer Experience (`user@example.com`)**:
   - Sign in as `user@example.com` / `Password123`.
   - Filter services by category pill (`MEDICAL`, `FITNESS`).
   - Pick an upcoming working day (e.g., upcoming Friday).
   - Observe **Dynamic Slots generation** (e.g. `09:00 - 09:30`, `09:30 - 10:00`, etc.).
   - Click a slot and click **Book Appointment**.
   - Notice the slot instantly disappears from available slots (preventing double booking).
   - Switch to **My Booked Appointments** tab to see your confirmed booking.

3. **Step 3 — Concurrency & Protection Checks**:
   - Provider attempting to book their own service returns `403 Forbidden`.
   - Attempting to book an already taken slot returns `409 Conflict`.
   - Selecting past dates returns `400 Bad Request`.

---

## 📚 Complete API Specification (All 9 Endpoints)

| # | Method | Endpoint | Role | Description |
|---|:---:|---|:---:|---|
| 1 | `POST` | `/auth/register` | Public | Register user (`USER` or `SERVICE_PROVIDER`) |
| 2 | `POST` | `/auth/login` | Public | Login with email & password, receives JWT token |
| 3 | `POST` | `/services` | `SERVICE_PROVIDER` | Create service (30–120 min, multiple of 30) |
| 4 | `POST` | `/services/{id}/availability` | `SERVICE_PROVIDER` | Set weekly recurring availability window (`409` on overlap) |
| 5 | `GET` | `/services` | Public | List all services (optional query filter `?type=...`) |
| 6 | `GET` | `/services/{id}/slots?date=YYYY-MM-DD` | Public | Dynamic slot derivation (excludes past & booked slots) |
| 7 | `POST` | `/appointments` | `USER` | Concurrency-safe slot booking using `slotId` |
| 8 | `GET` | `/appointments/me` | `USER` | View authenticated user's booked appointments |
| 9 | `GET` | `/providers/me/schedule?date=YYYY-MM-DD` | `SERVICE_PROVIDER` | Provider daily schedule grouped by service |

---

## 🧪 Automated Test Suite (35/35 PASSED)

Run the automated verification suite anytime from the project root:

```bash
python backend/test_api.py
```

### Test Suite Execution Output
```text
========================================================
     APPOINTMENT BOOKING SYSTEM - API TEST SUITE        
========================================================

1. Register Provider (Success 201)............................... PASS [OK]
2. Register Duplicate Email (409 Conflict)....................... PASS [OK]
3. Register Regular User (Success 201)........................... PASS [OK]
4. Register Other Provider (Success 201)......................... PASS [OK]
5. Provider Login Success (200 OK + JWT)......................... PASS [OK]
6. User Login Success (200 OK + JWT)............................. PASS [OK]
7. Other Provider Login Success (200 OK + JWT)................... PASS [OK]
8. Login with Invalid Credentials (401 Unauthorized)............. PASS [OK]
9. Create Service as Provider (Success 201)...................... PASS [OK]
10. Create Service with USER role (403 Forbidden)................ PASS [OK]
11. Create Service invalid duration (45 mins != multiple of 30 -> 400) PASS [OK]
12. Create Service duration out of range (150 mins > 120 -> 400). PASS [OK]
13. Create Service invalid enum type (400 Bad Request)........... PASS [OK]
14. Set Availability as Provider (Success 201)................... PASS [OK]
15. Set Overlapping Availability (409 Conflict).................. PASS [OK]
16. Set Availability invalid minutes (09:15 != 00/30 -> 400)..... PASS [OK]
17. Set Availability startTime >= endTime (12:00 to 09:00 -> 400) PASS [OK]
18. Set Availability by wrong provider (403 Forbidden)........... PASS [OK]
19. Set Availability with USER role (403 Forbidden).............. PASS [OK]
20. Get All Services (200 OK).................................... PASS [OK]
21. Get Services Filtered by type=MEDICAL (200 OK)............... PASS [OK]
22. Get Services with Invalid Type (400 Bad Request)............. PASS [OK]
23. Get Slots for Service on Friday (200 OK)..................... PASS [OK]
24. Get Slots for Past Date (400 Bad Request).................... PASS [OK]
25. Get Slots for Non-Existent Service (404 Not Found)........... PASS [OK]
26. Book Appointment as User (Success 201)....................... PASS [OK]
27. Book Already Booked Slot (409 Conflict)...................... PASS [OK]
28. Book Appointment as Service Provider (403 Forbidden)......... PASS [OK]
29. Book Appointment Without Auth (401 Unauthorized)............. PASS [OK]
30. Get My Appointments as User (200 OK)......................... PASS [OK]
31. Get My Appointments Without Auth (401 Unauthorized).......... PASS [OK]
32. Get Provider Daily Schedule (200 OK)......................... PASS [OK]
33. Get Provider Schedule with USER Role (403 Forbidden)......... PASS [OK]
34. Get Provider Schedule Invalid Date Format (400 Bad Request).. PASS [OK]
35. Get Provider Schedule Without Auth (401 Unauthorized)........ PASS [OK]

========================================================
             FINAL SUMMARY: 35/35 PASSED (100%)
========================================================
```
