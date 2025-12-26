# Project Structure

Root
- docker-compose.yml
- README.md
- POSTMAN_API_DOCUMENTATION.md
- start-services.cmd

Services (each is a Maven Spring Boot service)
- Api-Gateway/
  - Dockerfile
  - pom.xml, mvnw, mvnw.cmd
  - src/main/java/
  - src/main/resources/
  - target/
- Auth-Service/
  - Dockerfile
  - pom.xml, mvnw, mvnw.cmd
  - src/main/java/
  - src/main/resources/
  - target/
- Booking-Service/
  - Dockerfile
  - pom.xml, mvnw, mvnw.cmd
  - src/main/java/
  - src/main/resources/
  - target/
- Config-Server/
  - Dockerfile
  - pom.xml, mvnw, mvnw.cmd
  - src/main/, src/test/
  - target/
- Email-Service/
  - Dockerfile
  - pom.xml, mvnw, mvnw.cmd
  - src/main/, src/test/
  - target/
- Eureka-Service/
  - Dockerfile
  - pom.xml, mvnw, mvnw.cmd
  - src/main/, src/test/
  - target/
- Flight-Service/
  - Dockerfile
  - pom.xml, mvnw, mvnw.cmd
  - src/main/, src/test/
  - target/

Frontend: FlightBookingProject_Frontend/
- angular.json
- package.json
- README.md, SETUP.md
- tsconfig.*.json
- public/
- src/
  - index.html
  - main.ts
  - styles.css
  - app/

Detailed frontend layout (suggested `flight-booking-frontend/` organization)

flight-booking-frontend/
├─ src/
│  ├─ app/
│  │  ├─ app.component.ts       # Root component (navbar + layout)
│  │  ├─ app.component.html     # Navbar + router outlet
│  │  ├─ app.component.css      # Navbar & layout styles
│  │  ├─ app.config.ts          # Angular application config (API base URLs etc.)
│  │  ├─ app.routes.ts          # Routes: home, login, register, search, booking
│  │  ├─ main.module.ts         # App module (imports, providers)
│  │  
│  │  ├─ auth/
│  │  │  ├─ login/
│  │  │  │  ├─ login.component.ts
│  │  │  │  ├─ login.component.html
│  │  │  │  └─ login.component.css
│  │  │  └─ register/
│  │  │     ├─ register.component.ts
│  │  │     ├─ register.component.html
│  │  │     └─ register.component.css
│  │  
│  │  ├─ flights/
│  │  │  └─ search-flights/
│  │  │     ├─ search-flights.component.ts
│  │  │     ├─ search-flights.component.html
│  │  │     └─ search-flights.component.css
│  │  
│  │  ├─ booking/
│  │  │  ├─ booking-list/
│  │  │  │  ├─ booking-list.component.ts
│  │  │  │  ├─ booking-list.component.html
│  │  │  │  └─ booking-list.component.css
│  │  │  ├─ booking-detail/
│  │  │  │  ├─ booking-detail.component.ts
│  │  │  │  ├─ booking-detail.component.html
│  │  │  │  └─ booking-detail.component.css
│  │  │  └─ booking-state/
│  │  │     ├─ booking-state.component.ts
│  │  │     ├─ booking-state.component.html
│  │  │     └─ booking-state.component.css
│  │  
│  │  ├─ shared/
│  │  │  ├─ components/          # reusable UI components (buttons, modals, inputs)
│  │  │  ├─ models/              # TypeScript interfaces and models
│  │  │  └─ pipes/               # shared pipes
│  │  
│  │  ├─ services/
│  │  │  ├─ auth.service.ts      # login, token storage, isAuthenticated()
│  │  │  ├─ flight.service.ts    # search flights, flight details
│  │  │  ├─ booking.service.ts   # create, list, cancel bookings
│  │  │  └─ api.interceptor.ts   # attach JWT to requests, handle 401
│  │  
│  │  └─ store/                  # optional: NgRx or simple state management
│  │     ├─ actions/
│  │     ├─ reducers/
│  │     └─ effects/
│  
│  ├─ assets/
│  │  ├─ images/
│  │  └─ styles/
│  
│  └─ environments/
│     ├─ environment.ts
│     └─ environment.prod.ts
 
├─ e2e/                        # end-to-end tests
├─ angular.json
├─ package.json
├─ tsconfig.json
└─ README.md

Notes
- Services follow standard Maven layout: `src/main/java`, `src/main/resources`, `src/test/java`.
- Frontend can map to `FlightBookingProject_Frontend/` in this repo; rename to `flight-booking-frontend/` if you prefer the name in this document.

If you want, I can: 1) save a second copy named `flight-booking-frontend/README.md` inside the frontend folder, 2) scaffold missing files, or 3) generate a basic Angular module and a few starter components.
