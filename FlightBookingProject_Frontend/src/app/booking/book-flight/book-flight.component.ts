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
  isBooked: boolean =false;
  loadingFlight = false;
  errorMessage = '';
  successMessage = '';
  availableSeats: string[] = [];
  returnAvailableSeats: string[] = [];
  today = new Date().toISOString().split('T')[0];

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
    private route: ActivatedRoute,
    private router: Router,
    private flightService: FlightService,
    private bookingService: BookingService
  ) {}

  ngOnInit() {
    this.flightId = this.route.snapshot.paramMap.get('flightId') || '';
    
    // Get travel date from query parameters
    const travelDate = this.route.snapshot.queryParamMap.get('travelDate');
    if (travelDate) {
      this.bookingData.travelDate = travelDate;
    }
    
    this.loadFlight();
    const userEmail = localStorage.getItem('email');
    if (userEmail) {
      this.bookingData.contactEmail = userEmail;
    }
    this.updatePassengers();
  }

  loadFlight() {
    this.loadingFlight = true;
    this.flightService.getFlightById(this.flightId).subscribe({
      next: (flight) => {
        this.flight = flight;
        // Load available seats if travel date is already set
        if (this.bookingData.travelDate) {
          this.loadAvailableSeats();
        }
        this.loadingFlight = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load flight details';
        this.loadingFlight = false;
      }
    });
  }

  loadAvailableSeats() {
    if (!this.flight || !this.bookingData.travelDate) return;
    
    // Format date as YYYY-MM-DD
    const formattedDate = this.bookingData.travelDate;
    
    this.flightService.getAvailableSeats(this.flight.flightNumber, formattedDate).subscribe({
      next: (seats) => {
        this.availableSeats = seats;
      },
      error: () => {
        this.availableSeats = [];
      }
    });
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
    while (this.bookingData.passengers.length < count) {
      this.bookingData.passengers.push({
        name: '',
        gender: '',
        mealType: '',
        seatNumber: ''
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

  get bookingButtonLabel(): string {
    if (this.loading) return 'Booking…';
    if (this.isBooked) return 'Booked';
    return 'Confirm Booking';
  }

  // Disable while booking or after booked
  get bookingDisabled(): boolean {
    return this.loading || this.isBooked;
  }

  onBookFlight() {
    if (!this.flight) return;

    // Validate all fields
    if (!this.bookingData.travelDate) {
      this.errorMessage = 'Travel date is required';
      return;
    }

    if (!this.bookingData.contactEmail || !this.bookingData.contactEmail.trim()) {
      this.errorMessage = 'Contact email is required';
      return;
    }

    if (!this.bookingData.passengers || this.bookingData.passengers.length === 0) {
      this.errorMessage = 'At least one passenger is required';
      return;
    }

    // Validate all passengers have required fields
    for (let i = 0; i < this.bookingData.passengers.length; i++) {
      const passenger = this.bookingData.passengers[i];
      if (!passenger.name || !passenger.name.trim()) {
        this.errorMessage = `Passenger ${i + 1}: Name is required`;
        return;
      }
      if (!passenger.gender) {
        this.errorMessage = `Passenger ${i + 1}: Gender is required`;
        return;
      }
      if (!passenger.mealType) {
        this.errorMessage = `Passenger ${i + 1}: Meal type is required`;
        return;
      }
      if (!passenger.seatNumber || !passenger.seatNumber.trim()) {
        this.errorMessage = `Passenger ${i + 1}: Seat number is required`;
        return;
      }
      // Validate seat is in available seats
      if (!this.availableSeats.includes(passenger.seatNumber)) {
        this.errorMessage = `Passenger ${i + 1}: Selected seat ${passenger.seatNumber} is not available. Please select from available seats.`;
        return;
      }
    }

    // Validate return passengers for round trip
    if (this.bookingData.tripType === 'ROUND_TRIP') {
      if (!this.bookingData.returnFlightNumber || !this.bookingData.returnFlightNumber.trim()) {
        this.errorMessage = 'Return flight number is required for round trip';
        return;
      }
      if (!this.bookingData.returnTravelDate) {
        this.errorMessage = 'Return travel date is required for round trip';
        return;
      }
      if (!this.bookingData.returnPassengers || this.bookingData.returnPassengers.length === 0) {
        this.errorMessage = 'Return flight passengers are required';
        return;
      }
      for (let i = 0; i < this.bookingData.returnPassengers.length; i++) {
        const passenger = this.bookingData.returnPassengers[i];
        if (!passenger.seatNumber || !passenger.seatNumber.trim()) {
          this.errorMessage = `Return Passenger ${i + 1}: Seat number is required`;
          return;
        }
        if (!this.returnAvailableSeats.includes(passenger.seatNumber)) {
          this.errorMessage = `Return Passenger ${i + 1}: Selected seat ${passenger.seatNumber} is not available. Please select from available seats.`;
          return;
        }
      }
    }

    this.loading = true;
    this.isBooked=false;
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
        this.isBooked=true;
        this.loading=false;
        this.successMessage = `Booking confirmed! PNR: ${response.pnr}`;
        /*setTimeout(() => {
          this.router.navigate(['/search']);
        }, 3000); */
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Booking failed. Please try again.';
        this.loading = false;
      }
    });
  }
}
