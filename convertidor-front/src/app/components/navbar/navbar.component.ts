import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(
    public auth: AuthService,
    private router: Router,
    public theme: ThemeService
  ) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
