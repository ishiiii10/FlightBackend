import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080';

export interface Flight {
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

@Injectable({
  providedIn: 'root'
})
export class FlightService {
  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    });
  }

  getAllFlights(): Observable<Flight[]> {
    return this.http.get<Flight[]>(`${API_URL}/flights`);
  }

  getFlightById(id: string): Observable<Flight> {
    return this.http.get<Flight>(`${API_URL}/flights/${id}`);
  }

  searchFlights(source: string, destination: string, date: string): Observable<Flight[]> {
    return this.http.get<Flight[]>(`${API_URL}/flights/search`, {
      params: { source, destination, date }
    });
  }

  getAvailableSeats(flightNumber: string, travelDate: string): Observable<string[]> {
    const headers = this.getHeaders();
    return this.http.get<string[]>(`${API_URL}/flights/internal/${flightNumber}/seats/available`, {
      headers,
      params: { travelDate }
    });
  }
}
