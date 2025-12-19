import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { SearchFlightComponent } from './flight/search-flight/search-flight.component';
import { BookFlightComponent } from './booking/book-flight/book-flight.component';
import { ProfileComponent } from './profile/profile.component';
import { ViewHistoryComponent } from './booking/view-history/view-history.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/search', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'search', component: SearchFlightComponent },
  { path: 'book/:flightId', component: BookFlightComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'booking-history', component: ViewHistoryComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '/search' }
];
