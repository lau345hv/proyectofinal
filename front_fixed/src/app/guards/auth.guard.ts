import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard que protege las rutas que requieren que el usuario haya
 * iniciado sesión.
 *
 * Si el usuario no está autenticado, lo redirige a `/login` y
 * bloquea el acceso a la ruta solicitada.
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

    /**
     * @param auth   Servicio de autenticación para verificar el estado de sesión.
     * @param router Router de Angular para redirigir al login si es necesario.
     */
    constructor(private auth: AuthService, private router: Router) {}

    /**
     * Determina si la ruta puede activarse.
     *
     * @returns `true` si el usuario tiene sesión activa; `false` y redirige
     *          a `/login` en caso contrario.
     */
    canActivate(): boolean {
        if (this.auth.isLoggedIn()) return true;
        this.router.navigate(['/login']);
        return false;
    }
}
