import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent {
  form = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(form: NgForm) {
    this.errorMessage = '';
    this.successMessage = '';

    if (!form.valid) {
      this.errorMessage = 'Please fill all fields.';
      return;
    }

    if (this.form.newPassword !== this.form.confirmPassword) {
      this.errorMessage = 'New password and confirm password do not match.';
      return;
    }

    if (this.form.newPassword.length < 6) {
      this.errorMessage = 'New password must be at least 6 characters.';
      return;
    }

   

    this.loading = true;

    this.authService.changePassword(
      this.form.oldPassword,
      this.form.newPassword
    ).subscribe({
      next: () => {
        this.successMessage = 'Password changed successfully. Please log in again.';
        this.loading = false;
        // Security best practice: log user out so they must re-authenticate
        this.authService.logout();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to change password.';
        this.loading = false;
      }
    });
  }
}