import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, UserProfile } from '../services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, RouterLink],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  loading = false;
  errorMessage = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.errorMessage = '';
    
    this.authService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to load profile';
        this.loading = false;
      }
    });
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  getRoleDisplayName(): string {
    if (!this.profile) return '';
    return this.profile.role === 'ADMIN' ? 'Administrator' : 'Customer';
  }
}
