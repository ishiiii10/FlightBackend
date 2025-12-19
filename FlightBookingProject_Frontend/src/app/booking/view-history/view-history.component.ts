import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BookingService, BookingHistory } from '../../services/booking.service';

@Component({
  selector: 'app-view-history',
  imports: [CommonModule, RouterLink],
  templateUrl: './view-history.component.html',
  styleUrl: './view-history.component.css'
})
export class ViewHistoryComponent implements OnInit {
  bookings: BookingHistory[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  cancelingPnr: string | null = null;
  showCancelModal = false;
  bookingToCancel: BookingHistory | null = null;

  constructor(private bookingService: BookingService) {}

  ngOnInit() {
    this.loadBookingHistory();
  }

  loadBookingHistory() {
    this.loading = true;
    this.errorMessage = '';
    
    this.bookingService.getBookingHistory().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to load booking history';
        this.loading = false;
      }
    });
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  formatDateTime(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCurrency(amount: number): string {
    if (amount === undefined || amount === null) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getStatusClass(status: string): string {
    return status === 'CONFIRMED' ? 'status-confirmed' : 'status-cancelled';
  }

  getStatusDisplay(status: string): string {
    return status || 'UNKNOWN';
  }

  canCancelBooking(booking: BookingHistory): boolean {
    if (booking.status !== 'CONFIRMED') {
      return false;
    }

    // Check if cancellation is allowed (24 hours before departure)
    const travelDate = new Date(booking.travelDate);
    const now = new Date();
    const hoursUntilDeparture = (travelDate.getTime() - now.getTime()) / (1000 * 60 * 60);

    // Must be at least 24 hours before departure and flight hasn't departed
    return hoursUntilDeparture >= 24 && travelDate > now;
  }

  openCancelModal(booking: BookingHistory) {
    this.bookingToCancel = booking;
    this.showCancelModal = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeCancelModal() {
    this.showCancelModal = false;
    this.bookingToCancel = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  confirmCancel() {
    if (!this.bookingToCancel) return;

    this.cancelingPnr = this.bookingToCancel.pnr;
    this.errorMessage = '';
    this.successMessage = '';

    this.bookingService.cancelBooking(this.bookingToCancel.pnr).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Booking cancelled successfully';
        this.closeCancelModal();
        // Reload booking history
        this.loadBookingHistory();
        this.cancelingPnr = null;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to cancel booking. Please try again.';
        this.cancelingPnr = null;
      }
    });
  }
}
