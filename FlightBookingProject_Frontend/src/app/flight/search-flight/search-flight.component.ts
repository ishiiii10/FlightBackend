import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FlightService } from '../../services/flight.service';
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

@Component({
  selector: 'app-search-flight',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './search-flight.component.html',
  styleUrl: './search-flight.component.css'
})
export class SearchFlightComponent {

  searchData = {
    source: '',
    destination: '',
    date: ''
  };

  flights: Flight[] = [];
  loading = false;
  hasSearched = false;
  errorMessage = '';

  today = new Date().toISOString().split('T')[0];

  cities: string[] = [
  'DELHI',
  'MUMBAI',
  'BANGALORE',
  'CHENNAI',
  'KOLKATA',
  'HYDERABAD',
  'PUNE',
  'JAIPUR'
];

  constructor(
    private flightService: FlightService,
    private router: Router,
    public authService: AuthService
  ) {}

  onSearch() {
    // Validation
    if (!this.searchData.source || !this.searchData.destination) {
      this.errorMessage = 'Please enter both source and destination';
      return;
    }

    if (!this.searchData.date) {
      this.errorMessage = 'Please select a travel date';
      return;
    }

    const selectedDate = new Date(this.searchData.date);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    selectedDate.setHours(0, 0, 0, 0);

    if (selectedDate < today) {
      this.errorMessage = 'Travel date cannot be in the past';
      return;
    }

    const source = this.searchData.source.toUpperCase().trim();
    const destination = this.searchData.destination.toUpperCase().trim();
    const date = this.searchData.date;

    this.loading = true;
    this.hasSearched = true;
    this.errorMessage = '';
    this.flights = [];

    // ✅ DATE IS NOW PASSED CORRECTLY
    this.flightService.searchFlights(source, destination, date).subscribe({
      next: (flights) => {
        this.flights = flights;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage =
          error.error?.message || 'Failed to search flights. Please try again.';
        this.loading = false;
      }
    });
  }

  formatDateTime(dateTimeString: string): string {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  selectFlight(flight: Flight) {
    // Flight card click handler
  }


  bookFlight(flight: Flight) {
    // Extra safety: even if the UI hides the button, protect navigation.
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.router.navigate(['/book', flight.id], {
      queryParams: { travelDate: this.searchData.date }
    });
  }
}