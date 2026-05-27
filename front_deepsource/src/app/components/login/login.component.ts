import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ErrorHandlerService } from '../../services/error-handler.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  form: FormGroup;
  cargando = false;          // JS-0331
  error = '';                // JS-0331
  mostrarContrasena = false; // JS-0331

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private errorHandler: ErrorHandlerService
  ) {
    this.form = this.fb.group({
      nombreUsuario: ['', [Validators.required, Validators.minLength(3)]],
      contrasena: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  toggleContrasena(): void {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error = 'Por favor completa todos los campos.';
      return;
    }
    this.cargando = true;
    this.error = '';

    const { nombreUsuario, contrasena } = this.form.value;

    // JS-0356: parámetro 'res' no usado → reemplazado por _
    this.auth.login(nombreUsuario.trim(), contrasena).subscribe({
      next: (_res) => {
        this.cargando = false;
        if (this.auth.isAdmin()) {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/']);
        }
      },
      error: (err: unknown) => {
        this.cargando = false;
        this.error = this.errorHandler.extraerMensaje(err, 'Usuario o contraseña incorrectos.');
      }
    });
  }
}
