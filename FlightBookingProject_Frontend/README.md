# NimbusAir - Flight Booking Frontend

Angular frontend application for the Flight Booking System.

## Features

- **Login & Register**: User authentication with role-based access (Customer/Admin)
- **Search Flights**: Search flights by source and destination
- **Book Flight**: Book flights with seat selection, passenger details, and round-trip support

## Prerequisites

- Node.js (v18 or higher)
- npm or yarn
- Angular CLI (`npm install -g @angular/cli`)

## Installation

1. Navigate to the frontend directory:
```bash
cd FlightBookingProject_Frontend
```

2. Install dependencies:
```bash
npm install
```

## Development Server

Run the development server:
```bash
ng serve
```

The application will be available at `http://localhost:4200`

## Build

Build for production:
```bash
ng build
```

The build artifacts will be stored in the `dist/` directory.

## API Configuration

The frontend connects to the backend API Gateway at `http://localhost:8080`. Make sure:
- API Gateway is running on port 8080
- CORS is configured to allow requests from `http://localhost:4200`

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

## Usage

1. **Register/Login**: Create an account or login with existing credentials
2. **Search Flights**: Enter source and destination cities to search for flights
3. **Book Flight**: Click "Book Now" on a flight, fill in passenger details, select seats, and confirm booking

## Design

- Clean, modern UI with paper-style grid layout
- Blue and white color scheme
- Responsive design
- Card-based components with subtle shadows

## Project Structure

```
src/
├── app/
│   ├── auth/
│   │   ├── login/
│   │   └── register/
│   ├── flight/
│   │   └── search-flight/
│   ├── booking/
│   │   └── book-flight/
│   ├── shared/
│   │   └── navbar/
│   └── services/
│       ├── auth.service.ts
│       ├── flight.service.ts
│       └── booking.service.ts
└── styles.css (global styles)
```
