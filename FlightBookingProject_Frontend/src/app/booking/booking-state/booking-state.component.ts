// ...existing code...
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BookingService, BookingHistory } from '../../services/booking.service';

@Component({
  selector: 'app-booking-state',
  imports: [CommonModule],
  templateUrl: './booking-state.component.html',
  styleUrl: './booking-state.component.css'
})
export class BookingStateComponent implements OnInit {
  loading = false;
  error = '';
  booking: BookingHistory | null = null;
  cancelling=false;
  successMessage = '';


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService
  ) {}

  ngOnInit(): void {
    const pnr = this.route.snapshot.paramMap.get('pnr');
    if (!pnr) {
      this.error = 'PNR not provided';
      return;
    }

    this.loadBooking(pnr);
  }

  loadBooking(pnr: string) {
    this.loading = true;
    this.bookingService.getBookingByPnr(pnr).subscribe({
      next: (b) => {
        this.booking = b;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load booking';
        this.loading = false;
      }
    });
  }

  cancelBooking() {
    if (!this.booking || this.booking.status === 'CANCELLED') return;

    const confirmCancel = confirm(
      `Are you sure you want to cancel booking PNR ${this.booking.pnr}?`
    );

    if (!confirmCancel) return;

    this.cancelling = true;

    this.bookingService.cancelBooking(this.booking.pnr).subscribe({
      next: (res) => {
        this.successMessage = res.message;
        this.cancelling = false;
        this.loadBooking(this.booking!.pnr); // refresh state
      },
      error: (err) => {
        this.error = err.error?.message || 'Cancellation failed';
        this.cancelling = false;
      }
    });
  }
  goToSearch() { this.router.navigate(['/search']); }
  goToHistory() { this.router.navigate(['/bookings']); }
}
// ...existing code...