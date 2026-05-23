import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

/**
 * Interceptor de HTTP que adjunta el token JWT a todas las peticiones
 * salientes, excepto a las rutas de autenticación (login/crear).
 *
 * - Si la respuesta es `401 Unauthorized`, elimina los datos de sesión y redirige al login.
 * - Si la respuesta es `403 Forbidden`, redirige al usuario al inicio.
 *
 * @param req - Petición HTTP entrante.
 * @param next - Manejador que pasa la petición al siguiente interceptor o al servidor.
 * @returns Observable de la respuesta HTTP, con manejo de errores de autorización.
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('jwtToken');

    const esRutaPublica =
      req.url.includes('/usuario/login') ||
      req.url.includes('/usuario/crear') ||
      req.url.includes('/verificacion');

    if (token && !esRutaPublica) {
      const cloned = req.clone({
        setHeaders: {
          Authorization: 'Bearer ' + token
        }
      });

      return next.handle(cloned).pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('usuario');
            window.location.href = '/login';
          }
          if (error.status === 403) {
            window.location.href = '/';
          }
          return throwError(() => error);
        })
      );
    }

    return next.handle(req);
  }
}
