import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Interceptor HTTP que adjunta automáticamente el token JWT a cada
 * petición saliente, siempre que exista un token guardado en
 * `localStorage` bajo la clave `jwt_token`.
 *
 * El token se agrega como cabecera `Authorization: Bearer <token>`.
 * Si no hay token, la petición se envía sin modificaciones.
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

    /**
     * Intercepta la petición HTTP y, si existe un token JWT en
     * `localStorage`, clona la petición agregando la cabecera
     * `Authorization`.
     *
     * @param req  Petición HTTP original.
     * @param next Manejador que pasa la petición al siguiente interceptor
     *             o al backend.
     * @returns Observable con el evento HTTP resultante.
     */
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = localStorage.getItem('jwt_token');
        if (token) {
            const cloned = req.clone({
                setHeaders: { Authorization: `Bearer ${token}` }
            });
            return next.handle(cloned);
        }
        return next.handle(req);
    }
}