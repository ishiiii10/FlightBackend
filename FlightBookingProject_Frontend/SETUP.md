# NimbusAir Frontend Setup Guide

## Quick Start

1. **Install Dependencies**
   ```bash
   cd FlightBookingProject_Frontend
   npm install
   ```

2. **Start Development Server**
   ```bash
   ng serve
   ```
   The app will be available at `http://localhost:4200`

3. **Ensure Backend is Running**
   - API Gateway should be running on `http://localhost:8080`
   - All microservices should be registered with Eureka

## Project Structure

```
FlightBookingProject_Frontend/
├── src/
│   ├── app/
│   │   ├── auth/              # Authentication components
│   │   │   ├── login/
│   │   │   └── register/
│   │   ├── flight/             # Flight search component
│   │   │   └── search-flight/
│   │   ├── booking/            # Booking component
│   │   │   └── book-flight/
│   │   ├── shared/             # Shared components
│   │   │   └── navbar/
│   │   ├── services/           # API services
│   │   │   ├── auth.service.ts
│   │   │   ├── flight.service.ts
│   │   │   └── booking.service.ts
│   │   ├── app.component.ts    # Root component
│   │   ├── app.routes.ts       # Routing configuration
│   │   └── app.config.ts       # App configuration
│   └── styles.css              # Global styles
└── README.md
```

## Features Implemented

### 1. Authentication (Login & Register)
- **Login Component**: User login with email and password
- **Register Component**: User registration with role selection (Customer/Admin)
- JWT token stored in localStorage
- Automatic redirect to login if not authenticated

### 2. Search Flights
- Search by source and destination cities
- Real-time flight results display
- Flight cards with details (airline, route, timing, price, seats)
- "Book Now" button for authenticated users

### 3. Book Flight
- Flight details summary
- Trip type selection (One Way / Round Trip)
- Travel date selection
- Passenger information form
- Seat selection from available seats
- Round trip support with return flight details
- Booking confirmation with PNR

## Design Features

- **Grid Background**: Light blue grid pattern matching the design
- **Color Scheme**: White and blue tones only
- **Card-based Layout**: Clean cards with subtle shadows
- **Responsive Design**: Works on desktop and mobile
- **Modern UI**: Clean, minimal, professional airline-themed design

## API Endpoints Used

- `POST /auth/signup` - User registration
- `POST /auth/login` - User login
- `GET /flights` - Get all flights
- `GET /flights/{id}` - Get flight by ID
- `GET /flights/search?source={source}&destination={destination}` - Search flights
- `GET /flights/internal/{flightNumber}/seats/available?travelDate={date}` - Get available seats
- `POST /bookings` - Create booking

## Configuration

### API URL
The API base URL is configured in each service file:
```typescript
const API_URL = 'http://localhost:8080';
```

### CORS
Ensure the API Gateway has CORS configured to allow requests from `http://localhost:4200`.

## Testing the Application

1. **Register a new user**
   - Navigate to `/register`
   - Fill in email, password, and select role (CUSTOMER)
   - Submit the form

2. **Login**
   - Navigate to `/login`
   - Enter credentials
   - You'll be redirected to the search page

3. **Search Flights**
   - Enter source city (e.g., DELHI)
   - Enter destination city (e.g., MUMBAI)
   - Click "Search flights"
   - View available flights

4. **Book a Flight**
   - Click "Book Now" on any flight
   - Fill in travel date
   - Enter number of seats
   - Fill in passenger details
   - Select seats for each passenger
   - Confirm booking

## Available Cities

The search accepts these city names (case-insensitive):
- DELHI
- MUMBAI
- BANGALORE
- CHENNAI
- KOLKATA
- HYDERABAD
- PUNE
- JAIPUR

## Troubleshooting

### CORS Errors
If you see CORS errors, ensure:
- API Gateway CORS configuration allows `http://localhost:4200`
- Backend services are running

### 401 Unauthorized
- Check if you're logged in (token in localStorage)
- Verify token is being sent in Authorization header
- Ensure token hasn't expired

### No Flights Found
- Verify flights exist in the database
- Check city names match enum values (uppercase)
- Ensure Flight-Service is running

### Seat Selection Not Working
- Ensure travel date is selected
- Verify seats are initialized for the flight
- Check Flight-Service is accessible

## Build for Production

```bash
ng build --configuration production
```

Output will be in `dist/FlightBookingProject_Frontend/`

