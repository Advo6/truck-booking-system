# Truck Booking Management System

A modern, full-stack truck booking platform for **Noko Transport** — a business with two Code 14 trucks. Customers can check availability, make bookings, and receive confirmations. Admins manage bookings, trucks, and customers through a secure dashboard.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Frontend | HTML5, CSS3, JavaScript (ES6) |
| Backend | Java Spring Boot 3.2 (REST API) |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Auth | Spring Security + JWT |
| Build | Maven |

## Project Structure

```
truck-booking-system/
├── frontend/          # Static HTML/CSS/JS website
│   ├── index.html     # Home page
│   ├── about.html
│   ├── fleet.html
│   ├── booking.html   # Booking form + calendar
│   ├── contact.html
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html # Admin dashboard
│   ├── css/
│   └── js/
├── backend/           # Spring Boot REST API
│   └── src/main/java/com/truckbooking/
└── database/
    └── schema.sql
```

## Prerequisites

- **Java 17+** (JDK, not just JRE — Spring Boot 3.2 requires it)
- MySQL 8.0+
- A local web server for the frontend (Live Server, Python http.server, etc.)


## Setup Instructions

### 1. Database

```bash
mysql -u root -p < database/schema.sql
```

Or let Spring Boot auto-create the schema (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Backend Configuration

Copy the example config and add your local values:

```bash
cd backend/src/main/resources
cp application.example.properties application.properties
```

Edit `application.properties` (this file is **gitignored** — never commit real passwords):

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password_here
app.jwt.secret=your_long_random_secret_at_least_32_characters
```

Optional email configuration:

```properties
app.mail.enabled=true
spring.mail.username=your-email@gmail.com
spring.mail.password=your-gmail-app-password
```

You can also set environment variables instead: `DB_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

### 3. Install Java 17 (Windows)

If `java -version` shows **1.8** (Java 8), install JDK 17 first:

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

Close and reopen CMD/PowerShell, then set `JAVA_HOME` (adjust path if your install folder differs):

```cmd
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
```

Verify:

```cmd
java -version
```

You should see `openjdk version "17..."`.

### 4. Run Backend

```cmd
cd backend
.\mvnw.cmd spring-boot:run
```

In **CMD** you can use `mvnw.cmd` without `.\`; in **PowerShell** you must use `.\mvnw.cmd`.

On first run, the wrapper downloads Maven automatically (no separate Maven install needed).

API runs at `http://localhost:8080`

### 5. Run Frontend

Serve the `frontend/` folder with any static file server:

```bash
cd frontend
python -m http.server 5500
```

Open `http://localhost:5500`


## API Endpoints

### Public

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/register` | Register customer |
| GET | `/api/trucks` | List trucks |
| GET | `/api/trucks/{id}` | Get truck |
| POST | `/api/bookings` | Create booking |
| GET | `/api/bookings/reference/{ref}` | Get booking by reference |
| GET | `/api/bookings/availability?year=&month=` | Calendar availability |
| POST | `/api/contact` | Contact form |

### Admin (JWT required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard` | Dashboard statistics |
| GET | `/api/admin/bookings` | List/search/filter bookings |
| PATCH | `/api/admin/bookings/{id}/status` | Approve/reject/cancel |
| PUT | `/api/admin/bookings/{id}` | Edit booking |
| DELETE | `/api/admin/bookings/{id}` | Delete booking |
| POST/PUT/DELETE | `/api/admin/trucks` | Manage trucks |
| GET | `/api/admin/customers` | List customers |
| GET | `/api/admin/calendar` | Admin calendar view |

## Key Features

- **Interactive booking calendar** — green (available), red (booked), orange (pending)
- **Overlap prevention** — no double bookings on the same truck
- **Auto truck suggestion** — suggests Truck 2 if Truck 1 is booked
- **Booking reference generation** — unique reference per booking (e.g. `TB-A1B2C3D4`)
- **Email confirmations** — configurable via SMTP
- **Admin dashboard** — stats, booking management, truck status, customer history
- **Dark mode toggle**
- **WhatsApp integration**
- **Google Maps** on contact page
- **Print booking** confirmation
- **Responsive design** — mobile, tablet, desktop

## Business Rules

1. Business owns exactly **two** Code 14 trucks
2. One truck = one booking per date range (no overlaps)
3. Booking automatically updates truck availability
4. Expired bookings release trucks (daily scheduled task)
5. Pickup date cannot be in the past
6. Return date must be after pickup date


## License

MIT — for educational and commercial use.
