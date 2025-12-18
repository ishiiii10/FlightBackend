import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { FlightService } from '../../services/flight.service';
import { BookingService } from '../../services/booking.service';
import { CommonModule } from '@angular/common';

interface Flight {
  id: number;
  flightNumber: string;
  airline: string;
  source: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  price: number;
  availableSeats: number;
}

interface Passenger {
  name: string;
  gender: string;
  mealType: string;
  seatNumber: string;
}

@Component({
  selector: 'app-book-flight',
  imports: [FormsModule, CommonModule],
  templateUrl: './book-flight.component.html',
  styleUrl: './book-flight.component.css'
})
export class BookFlightComponent implements OnInit {
  flightId: string = '';
  flight: Flight | null = null;
  loading = false;
  loadingFlight = false;
  errorMessage = '';
  successMessage = '';
  availableSeats: string[] = [];
  returnAvailableSeats: string[] = [];
  today = new Date().toISOString().split('T')[0];
  travelDateFromSearch = false;

  bookingData = {
    tripType: 'ONE_WAY',
    travelDate: '',
    seatsBooked: 1,
    contactEmail: '',
    passengers: [] as Passenger[],
    returnFlightNumber: '',
    returnTravelDate: '',
    returnPassengers: [] as Passenger[]
  };

  constructor(
    public route: ActivatedRoute,
    private router: Router,
    private flightService: FlightService,
    private bookingService: BookingService
  ) {}

  ngOnInit() {
    this.flightId = this.route.snapshot.paramMap.get('flightId') || '';
    
    // Get travel date from query params if available (from search)
    const travelDate = this.route.snapshot.queryParamMap.get('travelDate');
    if (travelDate) {
      this.bookingData.travelDate = travelDate;
      this.travelDateFromSearch = true;
    }
    
    this.loadFlight();
    const userEmail = localStorage.getItem('email');
    if (userEmail) {
      this.bookingData.contactEmail = userEmail;
    }
  }

  loadFlight() {
    this.loadingFlight = true;
    this.flightService.getFlightById(this.flightId).subscribe({
      next: (flight) => {
        this.flight = flight;
        // Initialize seats if travel date is already set
        if (this.bookingData.travelDate) {
          this.initializeSeatsAndLoad();
        }
        this.loadingFlight = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load flight details';
        this.loadingFlight = false;
      }
    });
  }

  initializeSeatsAndLoad() {
    if (!this.flight || !this.bookingData.travelDate) return;
    
    // Get internal flight details to get totalSeats
    this.flightService.getInternalFlightDetails(this.flight.flightNumber).subscribe({
      next: (internalDetails) => {
        // Initialize seats first, then load available seats
        this.flightService.initializeSeats(this.flight!.flightNumber, this.bookingData.travelDate, internalDetails.totalSeats).subscribe({
          next: () => {
            // Seats initialized, now load available seats
            this.loadAvailableSeats();
          },
          error: (error) => {
            // Seats might already be initialized, try loading anyway
            console.log('Seat initialization note:', error);
            this.loadAvailableSeats();
          }
        });
      },
      error: (error) => {
        console.error('Error getting flight details:', error);
        // Fallback: try to load seats anyway (they might be initialized)
        this.loadAvailableSeats();
      }
    });
  }

  loadAvailableSeats() {
    if (!this.flight || !this.bookingData.travelDate) return;
    
    // Format date as YYYY-MM-DD
    const formattedDate = this.bookingData.travelDate;
    
    this.flightService.getAvailableSeats(this.flight.flightNumber, formattedDate).subscribe({
      next: (seats) => {
        // Filter out already selected seats
        const selectedSeats = this.bookingData.passengers
          .map(p => p.seatNumber)
          .filter(seat => seat && seat.trim() !== '');
        
        this.availableSeats = seats.filter(seat => !selectedSeats.includes(seat));
        
        // Update passengers when seats are loaded
        if (this.bookingData.passengers.length > 0) {
          this.updatePassengers();
        }
      },
      error: (error) => {
        console.error('Error loading available seats:', error);
        this.availableSeats = [];
        this.errorMessage = 'Failed to load available seats. Please try again.';
      }
    });
  }

  getAvailableSeatsForPassenger(passengerIndex: number): string[] {
    // Get seats that are not selected by other passengers
    const selectedSeats = this.bookingData.passengers
      .map((p, idx) => idx !== passengerIndex ? p.seatNumber : '')
      .filter(seat => seat && seat.trim() !== '');
    
    return this.availableSeats.filter(seat => !selectedSeats.includes(seat));
  }

  loadReturnAvailableSeats() {
    if (!this.bookingData.returnFlightNumber || !this.bookingData.returnTravelDate) return;
    
    this.flightService.getAvailableSeats(this.bookingData.returnFlightNumber, this.bookingData.returnTravelDate).subscribe({
      next: (seats) => {
        this.returnAvailableSeats = seats;
      },
      error: () => {
        this.returnAvailableSeats = [];
      }
    });
  }

  updatePassengers() {
    const count = this.bookingData.seatsBooked;
    
    // Preserve existing passenger data when adjusting count
    const existingPassengers = [...this.bookingData.passengers];
    
    while (this.bookingData.passengers.length < count) {
      const index = this.bookingData.passengers.length;
      this.bookingData.passengers.push({
        name: existingPassengers[index]?.name || '',
        gender: existingPassengers[index]?.gender || '',
        mealType: existingPassengers[index]?.mealType || '',
        seatNumber: existingPassengers[index]?.seatNumber || ''
      });
    }
    while (this.bookingData.passengers.length > count) {
      this.bookingData.passengers.pop();
    }
    
    if (this.bookingData.tripType === 'ROUND_TRIP') {
      this.updateReturnPassengers();
    }
  }

  updateReturnPassengers() {
    this.bookingData.returnPassengers = this.bookingData.passengers.map(p => ({
      name: p.name,
      gender: p.gender,
      mealType: p.mealType,
      seatNumber: ''
    }));
  }

  onTripTypeChange() {
    if (this.bookingData.tripType === 'ROUND_TRIP') {
      this.updateReturnPassengers();
      if (this.bookingData.returnTravelDate && this.bookingData.returnFlightNumber) {
        this.loadReturnAvailableSeats();
      }
    }
  }

  onReturnDateChange() {
    if (this.bookingData.returnTravelDate && this.bookingData.returnFlightNumber) {
      this.loadReturnAvailableSeats();
    }
  }

  onReturnFlightChange() {
    if (this.bookingData.returnTravelDate && this.bookingData.returnFlightNumber) {
      this.loadReturnAvailableSeats();
    }
  }

  onTravelDateChange() {
    if (this.bookingData.travelDate && this.flight) {
      this.initializeSeatsAndLoad();
    }
  }

  formatDateTime(dateTimeString: string): string {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  validateBooking(): string | null {
    // Validate travel date
    if (!this.bookingData.travelDate) {
      return 'Travel date is required';
    }

    const travelDate = new Date(this.bookingData.travelDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    travelDate.setHours(0, 0, 0, 0);

    if (travelDate < today) {
      return 'Travel date cannot be in the past';
    }

    // Validate number of seats
    if (!this.bookingData.seatsBooked || this.bookingData.seatsBooked < 1) {
      return 'At least one seat must be booked';
    }

    if (this.flight && this.bookingData.seatsBooked > this.flight.availableSeats) {
      return `Cannot book more than ${this.flight.availableSeats} seats`;
    }

    // Validate contact email
    if (!this.bookingData.contactEmail) {
      return 'Contact email is required';
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.bookingData.contactEmail)) {
      return 'Invalid email format';
    }

    // Validate passengers
    if (this.bookingData.passengers.length !== this.bookingData.seatsBooked) {
      return 'Passenger count must match number of seats';
    }

    // Validate each passenger
    const seatNumbers = new Set<string>();
    for (let i = 0; i < this.bookingData.passengers.length; i++) {
      const passenger = this.bookingData.passengers[i];
      
      if (!passenger.name || passenger.name.trim() === '') {
        return `Passenger ${i + 1}: Name is required`;
      }

      if (!passenger.gender) {
        return `Passenger ${i + 1}: Gender is required`;
      }

      if (!passenger.mealType) {
        return `Passenger ${i + 1}: Meal type is required`;
      }

      if (!passenger.seatNumber || passenger.seatNumber.trim() === '') {
        return `Passenger ${i + 1}: Seat selection is required`;
      }

      // Check for duplicate seats
      if (seatNumbers.has(passenger.seatNumber)) {
        return `Passenger ${i + 1}: Duplicate seat ${passenger.seatNumber} selected`;
      }
      seatNumbers.add(passenger.seatNumber);

      // Validate seat is in available seats list
      if (!this.availableSeats.includes(passenger.seatNumber)) {
        return `Passenger ${i + 1}: Seat ${passenger.seatNumber} is not available`;
      }
    }

    // Validate round trip if selected
    if (this.bookingData.tripType === 'ROUND_TRIP') {
      if (!this.bookingData.returnFlightNumber || this.bookingData.returnFlightNumber.trim() === '') {
        return 'Return flight number is required for round trip';
      }

      if (!this.bookingData.returnTravelDate) {
        return 'Return travel date is required for round trip';
      }

      const returnDate = new Date(this.bookingData.returnTravelDate);
      if (returnDate < travelDate) {
        return 'Return date must be after travel date';
      }

      // Validate return passengers
      if (this.bookingData.returnPassengers.length !== this.bookingData.seatsBooked) {
        return 'Return passenger count must match number of seats';
      }

      const returnSeatNumbers = new Set<string>();
      for (let i = 0; i < this.bookingData.returnPassengers.length; i++) {
        const passenger = this.bookingData.returnPassengers[i];
        
        if (!passenger.seatNumber || passenger.seatNumber.trim() === '') {
          return `Return Passenger ${i + 1}: Seat selection is required`;
        }

        // Check for duplicate return seats
        if (returnSeatNumbers.has(passenger.seatNumber)) {
          return `Return Passenger ${i + 1}: Duplicate seat ${passenger.seatNumber} selected`;
        }
        returnSeatNumbers.add(passenger.seatNumber);

        // Validate return seat is in available seats list
        if (this.returnAvailableSeats.length > 0 && !this.returnAvailableSeats.includes(passenger.seatNumber)) {
          return `Return Passenger ${i + 1}: Seat ${passenger.seatNumber} is not available`;
        }
      }
    }

    return null; // Validation passed
  }

  onBookFlight() {
    if (!this.flight) {
      this.errorMessage = 'Flight details not loaded';
      return;
    }

    // Validate all fields
    const validationError = this.validateBooking();
    if (validationError) {
      this.errorMessage = validationError;
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const bookingRequest = {
      flightNumber: this.flight.flightNumber,
      travelDate: this.bookingData.travelDate,
      tripType: this.bookingData.tripType,
      seatsBooked: this.bookingData.seatsBooked,
      contactEmail: this.bookingData.contactEmail,
      passengers: this.bookingData.passengers,
      returnFlightNumber: this.bookingData.tripType === 'ROUND_TRIP' ? this.bookingData.returnFlightNumber : undefined,
      returnTravelDate: this.bookingData.tripType === 'ROUND_TRIP' ? this.bookingData.returnTravelDate : undefined,
      returnPassengers: this.bookingData.tripType === 'ROUND_TRIP' ? this.bookingData.returnPassengers : undefined
    };

    this.bookingService.createBooking(bookingRequest).subscribe({
      next: (response) => {
        this.successMessage = `Booking confirmed! PNR: ${response.pnr}`;
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/search']);
        }, 3000);
      },
      error: (error) => {
        let errorMsg = 'Booking failed. Please try again.';
        if (error.error?.message) {
          errorMsg = error.error.message;
        } else if (error.message) {
          errorMsg = error.message;
        }
        this.errorMessage = errorMsg;
        this.loading = false;
      }
    });
  }
}
