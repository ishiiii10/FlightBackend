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
    this.loading = true;
    this.bookingService.getBookingByPnr(pnr).subscribe({
      next: (b) => { this.booking = b; this.loading = false; },
      error: (err) => { this.error = err.error?.message || 'Failed to load booking'; this.loading = false; }
    });
  }

  goToSearch() { this.router.navigate(['/search']); }
  goToHistory() { this.router.navigate(['/bookings']); }
}
// ...existing code...