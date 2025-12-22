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
    this.authService.login(this.loginData.email, this.loginData.password).subscribe({
      next: (response) => {
        // Store auth data in localStorage
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('email', this.loginData.email);
        
        // Debug: log role to console (can be removed in production)
        console.log('Login successful. Role:', response.role);
        
        this.router.navigate(['/search']);
      },
      error: (error) => {
        // Better error handling
        const errorMsg = error.error?.message || error.message || 'Login failed. Please check your credentials.';
        this.errorMessage = errorMsg;
        console.error('Login error:', error);
      }
    });
  }
}
