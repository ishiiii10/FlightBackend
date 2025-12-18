import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginData = {
    email: '',
    password: ''
  };
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin() {
    this.errorMessage = '';

    // Validation
    if (!this.loginData.email || this.loginData.email.trim() === '') {
      this.errorMessage = 'Email is required';
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.loginData.email)) {
      this.errorMessage = 'Invalid email format';
      return;
    }

    if (!this.loginData.password || this.loginData.password.trim() === '') {
      this.errorMessage = 'Password is required';
      return;
    }

    if (this.loginData.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters';
      return;
    }

    this.authService.login(this.loginData.email, this.loginData.password).subscribe({
      next: (response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('email', this.loginData.email);
        this.router.navigate(['/search']);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Login failed. Please check your credentials.';
      }
    });
  }
}
