import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { FlightService } from '../../services/flight.service';

@Component({
  selector: 'app-add-flight',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-flight.component.html',
  styleUrl: './add-flight.component.css'
})
export class AddFlightComponent implements OnInit {
  airlines: string[] = [];
  cities: string[] = [];

  formData = {
    flightNumber: '',
    airline: '',
    source: '',
    destination: '',
    departureTime: '',
    arrivalTime: '',
    totalSeats: 0,
    price: 0
  };

  // Used to prevent selecting past dates in the datetime-local picker.
  minDeparture: string = new Date().toISOString().slice(0, 16);

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private flightService: FlightService,
    private router: Router
  ) {}

  goToAdmin() {
    this.router.navigate(['/admin']);
  }

  ngOnInit(): void {
    // Load enum-backed lists from backend instead of hardcoding.
    this.flightService.getAirlines().subscribe({
      next: (airlines) => (this.airlines = airlines),
      error: () => (this.airlines = [])
    });

    this.flightService.getCities().subscribe({
      next: (cities) => (this.cities = cities),
      error: () => (this.cities = [])
    });
  }

  onSourceChange() {
    // if user selects a source that equals current destination, clear destination
    if (this.formData.source && this.formData.destination === this.formData.source) {
      this.formData.destination = '';
    }
  }

  onSubmit(form: NgForm) {
    this.errorMessage = '';
    this.successMessage = '';

    if (!form.valid) {
      this.errorMessage = 'Please fill all required fields correctly.';
      return;
    }

    if (this.formData.source === this.formData.destination) {
      this.errorMessage = 'Source and destination must be different.';
      return;
    }

    const dep = new Date(this.formData.departureTime);
    const arr = new Date(this.formData.arrivalTime);
    const now = new Date();

    if (isNaN(dep.getTime()) || isNaN(arr.getTime())) {
      this.errorMessage = 'Please provide valid departure and arrival times.';
      return;
    }

    // Frontend guard: departure cannot be in the past.
    if (dep.getTime() < now.getTime()) {
      this.errorMessage = 'Departure time cannot be in the past.';
      return;
    }

    if (arr <= dep) {
      this.errorMessage = 'Arrival time must be after departure time.';
      return;
    }

    if (this.formData.totalSeats <= 0) {
      this.errorMessage = 'Total seats must be greater than zero.';
      return;
    }

    if (this.formData.price <= 0) {
      this.errorMessage = 'Price must be greater than zero.';
      return;
    }

    // Business rule: for new flights, availableSeats must equal totalSeats.
    const availableSeats = this.formData.totalSeats;

    this.loading = true;

    this.flightService.createFlight({
      ...this.formData,
      availableSeats,
      // ensure backend expects ISO strings
      departureTime: dep.toISOString(),
      arrivalTime: arr.toISOString()
    }).subscribe({
      next: () => {
        this.successMessage = 'Flight added successfully.';
        this.loading = false;
        // navigate to search so the added flight can be found immediately
        this.router.navigate(['/search'], { queryParams: { source: this.formData.source, destination: this.formData.destination } });
        form.resetForm();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to add flight.';
        this.loading = false;
      }
    });
  }
}