import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ErrorHandlerService } from '../../services/error-handler.service';

/**
 * Componente que gestiona el formulario de inicio de sesión.
 *
 * Presenta un formulario reactivo con los campos `nombreUsuario` y
 * `contrasena`. Tras un login exitoso redirige al panel de administración
 * si el usuario es ADMIN, o a la página de inicio en caso contrario.
 */
@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent {

    /** Formulario reactivo con los campos de inicio de sesión. */
    form: FormGroup;

    /** Indica si hay una petición de login en curso. */
    cargando: boolean = false;

    /** Mensaje de error a mostrar al usuario cuando el login falla. */
    error: string = '';

    /** Controla si la contraseña se muestra en texto plano o enmascarada. */
    mostrarContrasena: boolean = false;

    /**
     * @param fb           Constructor de formularios reactivos de Angular.
     * @param auth         Servicio de autenticación.
     * @param router       Router de Angular para la navegación post-login.
     * @param errorHandler Servicio centralizado de manejo de errores HTTP.
     */
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

    /**
     * Alterna la visibilidad de la contraseña entre texto plano y enmascarado.
     */
    toggleContrasena(): void {
        this.mostrarContrasena = !this.mostrarContrasena;
    }

    /**
     * Envía el formulario de login al backend.
     *
     * Si el formulario es inválido, marca todos los campos como tocados
     * y muestra un mensaje de error sin llamar al servicio.
     * Tras un login exitoso redirige a `/admin` si el usuario es ADMIN,
     * o a `/` en caso contrario.
     */
    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            this.error = 'Por favor completa todos los campos.';
            return;
        }
        this.cargando = true;
        this.error = '';

        const { nombreUsuario, contrasena } = this.form.value;

        this.auth.login(nombreUsuario.trim(), contrasena).subscribe({
            next: (res) => {
                this.cargando = false;
                if (this.auth.isAdmin()) {
                    this.router.navigate(['/admin']);
                } else {
                    this.router.navigate(['/']);
                }
            },
            error: (err) => {
                this.cargando = false;
                this.error = this.errorHandler.extraerMensaje(err, 'Usuario o contraseña incorrectos.');
            }
        });
    }
}