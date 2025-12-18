# Flight Booking System - Postman API Documentation

## Base URL
```
http://localhost:8080
```

All requests go through the API Gateway.

---

## 1. Authentication Endpoints

### 1.1 Admin Signup
**POST** `/auth/signup`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "admin@example.com",
  "password": "Admin@123",
  "role": "ADMIN"
}
```

**Response:** `200 OK` (empty body)

---

### 1.2 Customer Signup
**POST** `/auth/signup`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "customer@example.com",
  "password": "Customer@123",
  "role": "CUSTOMER"
}
```

**Response:** `200 OK` (empty body)

---

### 1.3 Login (Admin or Customer)
**POST** `/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "admin@example.com",
  "password": "Admin@123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "ADMIN"
}
```

**Save the token** for subsequent authenticated requests.

---

## 2. Flight Management (Admin Only)

### 2.1 Create Flight
**POST** `/flights`

**Headers:**
```
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json
```

**Body:**
```json
{
  "flightNumber": "UK-123",
  "airline": "VISTARA",
  "source": "DELHI",
  "destination": "MUMBAI",
  "departureTime": "2025-12-20T08:30:00",
  "arrivalTime": "2025-12-20T10:30:00",
  "totalSeats": 60,
  "availableSeats": 60,
  "price": 5500.00
}
```

**Response:**
```json
{
  "id": 1,
  "message": "Flight created successfully"
}
```

---

### 2.2 Update Flight
**PUT** `/flights/{flightId}`

**Headers:**
```
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json
```

**Body:**
```json
{
  "flightNumber": "UK-123",
  "airline": "VISTARA",
  "source": "DELHI",
  "destination": "MUMBAI",
  "departureTime": "2025-12-20T09:00:00",
  "arrivalTime": "2025-12-20T11:00:00",
  "totalSeats": 60,
  "availableSeats": 60,
  "price": 6000.00
}
```

**Response:** `204 No Content`

---

### 2.3 Delete Flight
**DELETE** `/flights/{flightId}`

**Headers:**
```
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response:** `204 No Content`

---

## 3. Flight Search (Public - No Auth Required)

### 3.1 Get All Flights
**GET** `/flights`

**Response:**
```json
[
  {
    "id": 1,
    "flightNumber": "UK-123",
    "airline": "VISTARA",
    "source": "DELHI",
    "destination": "MUMBAI",
    "departureTime": "2025-12-20T08:30:00",
    "arrivalTime": "2025-12-20T10:30:00",
    "price": 5500.00,
    "availableSeats": 60
  }
]
```

---

### 3.2 Get Flight by ID
**GET** `/flights/{flightId}`

**Response:**
```json
{
  "id": 1,
  "flightNumber": "UK-123",
  "airline": "VISTARA",
  "source": "DELHI",
  "destination": "MUMBAI",
  "departureTime": "2025-12-20T08:30:00",
  "arrivalTime": "2025-12-20T10:30:00",
  "price": 5500.00,
  "availableSeats": 60
}
```

---

### 3.3 Search Flights
**GET** `/flights/search?source=DELHI&destination=MUMBAI`

**Response:** Same as Get All Flights (filtered)

---

### 3.4 Get Available Seats (Internal - via Gateway)
**GET** `/flights/internal/{flightNumber}/seats/available?travelDate=2025-12-20`

**Note:** This endpoint is typically called internally, but you can test it directly.

**Response:**
```json
["1A", "1B", "1C", "1D", "1E", "1F", "2A", "2B", ...]
```

---

## 4. Booking Management (Customer Only)

### 4.1 Book One-Way Flight
**POST** `/bookings`

**Headers:**
```
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
Content-Type: application/json
```

**Body:**
```json
{
  "flightNumber": "UK-123",
  "travelDate": "2025-12-20",
  "tripType": "ONE_WAY",
  "seatsBooked": 2,
  "contactEmail": "customer@example.com",
  "passengers": [
    {
      "name": "John Doe",
      "gender": "MALE",
      "mealType": "VEG",
      "seatNumber": "1A"
    },
    {
      "name": "Jane Doe",
      "gender": "FEMALE",
      "mealType": "NON_VEG",
      "seatNumber": "1B"
    }
  ]
}
```

**Response:**
```json
{
  "pnr": "ABC123",
  "message": "Booking created successfully"
}
```

**Important:** 
- `seatsBooked` must match the number of passengers
- Each passenger must have a unique `seatNumber` (e.g., "1A", "1B", "2C")
- Seat numbers must be available for that flight/date

---

### 4.2 Book Round Trip Flight
**POST** `/bookings`

**Headers:**
```
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
Content-Type: application/json
```

**Body:**
```json
{
  "flightNumber": "UK-123",
  "travelDate": "2025-12-20",
  "tripType": "ROUND_TRIP",
  "seatsBooked": 2,
  "contactEmail": "customer@example.com",
  "returnFlightNumber": "UK-124",
  "returnTravelDate": "2025-12-25",
  "passengers": [
    {
      "name": "John Doe",
      "gender": "MALE",
      "mealType": "VEG",
      "seatNumber": "1A"
    },
    {
      "name": "Jane Doe",
      "gender": "FEMALE",
      "mealType": "NON_VEG",
      "seatNumber": "1B"
    }
  ],
  "returnPassengers": [
    {
      "name": "John Doe",
      "gender": "MALE",
      "mealType": "VEG",
      "seatNumber": "2A"
    },
    {
      "name": "Jane Doe",
      "gender": "FEMALE",
      "mealType": "NON_VEG",
      "seatNumber": "2B"
    }
  ]
}
```

**Response:**
```json
{
  "pnr": "XYZ789",
  "message": "Booking created successfully"
}
```

**Important:**
- `returnFlightNumber` and `returnTravelDate` are required for round trip
- `returnPassengers` must have the same count as `passengers`
- Return seat numbers must be available for the return flight/date

---

### 4.3 Get Booking by PNR
**GET** `/bookings/{pnr}`

**Headers:**
```
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

**Response:**
```json
{
  "pnr": "ABC123",
  "status": "CONFIRMED",
  "flightNumber": "UK-123",
  "travelDate": "2025-12-20",
  "passengerName": "John Doe",
  "seatsBooked": 2
}
```

---

### 4.4 Get My Bookings
**GET** `/bookings/my`

**Headers:**
```
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

**Response:**
```json
[
  {
    "pnr": "ABC123",
    "status": "CONFIRMED",
    "flightNumber": "UK-123",
    "travelDate": "2025-12-20",
    "passengerName": "John Doe",
    "seatsBooked": 2
  }
]
```

---

### 4.5 Cancel Booking
**DELETE** `/bookings/cancel/{pnr}`

**Headers:**
```
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

**Response:**
```json
{
  "pnr": "ABC123",
  "status": "CANCELLED",
  "message": "Booking cancelled successfully"
}
```

**Note:** This releases the seats back to inventory automatically.

---

## 5. Testing Flow

### Step-by-Step Testing:

1. **Create Admin User:**
   - POST `/auth/signup` with role "ADMIN"
   - Login and save ADMIN token

2. **Create Customer User:**
   - POST `/auth/signup` with role "CUSTOMER"
   - Login and save CUSTOMER token

3. **Create Flights (Admin):**
   - POST `/flights` - Create outbound flight (e.g., UK-123)
   - POST `/flights` - Create return flight (e.g., UK-124)

4. **Search Flights (Public):**
   - GET `/flights` - View all flights
   - GET `/flights/search?source=DELHI&destination=MUMBAI`

5. **Check Available Seats:**
   - GET `/flights/internal/UK-123/seats/available?travelDate=2025-12-20`
   - Note the available seat numbers (e.g., ["1A", "1B", "1C", ...])

6. **Book One-Way Flight (Customer):**
   - POST `/bookings` with selected seat numbers from step 5
   - Use unique seat numbers (e.g., "1A", "1B")

7. **Book Round Trip (Customer):**
   - POST `/bookings` with `tripType: "ROUND_TRIP"`
   - Include return flight details and return seat numbers

8. **View Bookings:**
   - GET `/bookings/my` - See all your bookings
   - GET `/bookings/{pnr}` - View specific booking

9. **Cancel Booking:**
   - DELETE `/bookings/cancel/{pnr}`
   - Verify seats are released (check available seats again)

---

## 6. Error Scenarios to Test

### 6.1 Duplicate Seat Booking
Try booking the same seat number twice:
```json
{
  "flightNumber": "UK-123",
  "travelDate": "2025-12-20",
  "passengers": [
    {"name": "John", "gender": "MALE", "mealType": "VEG", "seatNumber": "1A"},
    {"name": "Jane", "gender": "FEMALE", "mealType": "NON_VEG", "seatNumber": "1A"}
  ]
}
```
**Expected:** Error - "Duplicate seat numbers selected"

### 6.2 Invalid Seat Number
Try booking a seat that doesn't exist:
```json
{
  "seatNumber": "999Z"
}
```
**Expected:** Error - "Seat not found"

### 6.3 Round Trip Missing Return Flight
```json
{
  "tripType": "ROUND_TRIP",
  "returnFlightNumber": null
}
```
**Expected:** Error - "Return flight number is required for round trip"

### 6.4 Unauthorized Access
- Try accessing `/bookings` without token → 401/403
- Try accessing `/flights` POST without ADMIN token → 403

---

## 7. Email Notifications

When a booking is created:
- **Email sent to:** Booking owner (`contactEmail` in request)
- **Email sent to:** Admin (`admin@flightbooking.com` - configurable)
- **Email includes:**
  - PNR
  - Flight details
  - Seat numbers
  - Return flight details (if round trip)

**Note:** Configure SMTP settings in `Email-Service/application.properties` to receive actual emails.

---

## 8. Seat Number Format

Seats are named as: `{row}{letter}`
- Row: 1, 2, 3, ...
- Letter: A, B, C, D, E, F

Examples: `1A`, `1B`, `2C`, `10F`

For a flight with 60 seats, seats will be:
- Row 1: 1A, 1B, 1C, 1D, 1E, 1F
- Row 2: 2A, 2B, 2C, 2D, 2E, 2F
- ...
- Row 10: 10A, 10B, 10C, 10D, 10E, 10F

---

## 9. Important Notes

1. **Seat Uniqueness:** Each seat (flightNumber + travelDate + seatNumber) can only be booked once
2. **Multiple Bookings:** Multiple customers can book the same flight/date as long as they select different seats
3. **Round Trip:** Both outbound and return flights must exist and have available seats
4. **Cancellation:** Cancelling releases all seats (both outbound and return if round trip)
5. **Email:** Emails are sent asynchronously - booking succeeds even if email fails

---

## 10. Environment Variables

Make sure these services are running:
- **Eureka Service:** `http://localhost:8761`
- **MySQL:** `localhost:3306`
- **RabbitMQ:** `localhost:5672`
- **API Gateway:** `http://localhost:8080`
- **Auth Service:** `http://localhost:8083`
- **Flight Service:** `http://localhost:8081`
- **Booking Service:** `http://localhost:8082`
- **Email Service:** `http://localhost:8084`

---

## Quick Test Collection

Save these as a Postman collection:

1. **Admin Signup** → **Admin Login** → **Create Flight**
2. **Customer Signup** → **Customer Login** → **Get Available Seats** → **Book One-Way** → **View Booking**
3. **Book Round Trip** → **Cancel Booking**

---

**Happy Testing!** 🚀

