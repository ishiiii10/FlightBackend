import { Component } from '@angular/core';
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
export class AddFlightComponent {
  airlines = ['INDIGO', 'AIR_INDIA', 'VISTARA'];   // match backend enum
  cities = ['DELHI', 'MUMBAI', 'BANGALORE', 'CHENNAI']; // match City enum

  formData = {
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
  errorMessage = '';
  successMessage = '';

  constructor(
    private flightService: FlightService,
    private router: Router
  ) {}

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
    if (isNaN(dep.getTime()) || isNaN(arr.getTime()) || arr <= dep) {
      this.errorMessage = 'Arrival time must be after departure time.';
      return;
    }

    if (this.formData.totalSeats <= 0 ||
        this.formData.availableSeats <= 0 ||
        this.formData.availableSeats > this.formData.totalSeats) {
      this.errorMessage = 'Seat counts are invalid.';
      return;
    }

    if (this.formData.price <= 0) {
      this.errorMessage = 'Price must be greater than zero.';
      return;
    }

    this.loading = true;

    this.flightService.createFlight({
      ...this.formData,
      // ensure backend expects ISO strings
      departureTime: dep.toISOString(),
      arrivalTime: arr.toISOString()
    }).subscribe({
      next: () => {
        this.successMessage = 'Flight added successfully.';
        this.loading = false;
        form.resetForm();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to add flight.';
        this.loading = false;
      }
    });
  }
}