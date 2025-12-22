import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlightService, CreateFlightRequest } from '../../services/flight.service';

@Component({
  selector: 'app-add-flight',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-flight.component.html',
  styleUrls: ['./add-flight.component.css']
})
export class AddFlightComponent {

  airlines = ['AIR_INDIA', 'INDIGO', 'VISTARA', 'SPICEJET', 'GO_FIRST'];
  cities = ['DELHI', 'MUMBAI', 'BANGALORE', 'CHENNAI', 'KOLKATA', 'HYDERABAD', 'PUNE', 'JAIPUR'];

  flight: CreateFlightRequest = {
    flightNumber: '',
    airline: '',
    source: '',
    destination: '',
    departureTime: '',
    arrivalTime: '',
    totalSeats: 0,
    availableSeats: 0,
    price: 0
  };

  loading = false;
  successMessage = '';
  errorMessage = '';

  constructor(private flightService: FlightService) {}

  submit() {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.flight.source === this.flight.destination) {
      this.errorMessage = 'Source and destination cannot be the same';
      return;
    }

    this.loading = true;

    this.flightService.createFlight(this.flight).subscribe({
      next: () => {
        this.successMessage = 'Flight added successfully';
        this.loading = false;
        this.resetForm();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to create flight';
        this.loading = false;
      }
    });
  }

  resetForm() {
    this.flight = {
      flightNumber: '',
      airline: '',
      source: '',
      destination: '',
      departureTime: '',
      arrivalTime: '',
      totalSeats: 0,
      availableSeats: 0,
      price: 0
    };
  }
}