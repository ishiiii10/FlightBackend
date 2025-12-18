import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080';

interface CreateBookingRequest {
  flightNumber: string;
  travelDate: string;
  tripType: string;
  seatsBooked: number;
  contactEmail: string;
  passengers: Array<{
    name: string;
    gender: string;
    mealType: string;
    seatNumber: string;
  }>;
  returnFlightNumber?: string;
  returnTravelDate?: string;
  returnPassengers?: Array<{
    name: string;
    gender: string;
    mealType: string;
    seatNumber: string;
  }>;
}

interface CreateBookingResponse {
  pnr: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Not authenticated');
    }
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  createBooking(booking: CreateBookingRequest): Observable<CreateBookingResponse> {
    const headers = this.getHeaders();
    return this.http.post<CreateBookingResponse>(`${API_URL}/bookings`, booking, { headers });
  }
}
