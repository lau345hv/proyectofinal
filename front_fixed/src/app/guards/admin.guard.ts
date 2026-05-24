import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard que protege las rutas exclusivas del administrador.
 *
 * Si el usuario no tiene rol `ADMIN`, lo redirige a la página de inicio
 * y bloquea el acceso a la ruta solicitada.
 */
@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {

    /**
     * @param auth   Servicio de autenticación para verificar el rol del usuario.
     * @param router Router de Angular para redirigir si el acceso es denegado.
     */
    constructor(private auth: AuthService, private router: Router) {}

    /**
     * Determina si la ruta puede activarse.
     *
     * @returns `true` si el usuario tiene rol ADMIN; `false` y redirige
     *          a `/` en caso contrario.
     */
    canActivate(): boolean {
        if (this.auth.isAdmin()) return true;
        this.router.navigate(['/']);
        return false;
    }
}
