import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { UsuarioDTO, LoginResponse } from '../models/models';

const API_ROOT = 'http://localhost:8080';
const USUARIO = `${API_ROOT}/usuario`;

const RUTAS_SOLICITAR_CODIGO: Array<{ url: string; metodo: 'GET' | 'POST' }> = [
  { url: `${API_ROOT}/verificacion/solicitar-codigo`, metodo: 'POST' },
  { url: `${API_ROOT}/verificacion/solicitar-codigo`, metodo: 'GET' },
  { url: `${API_ROOT}/verificacion-correo/solicitar-codigo`, metodo: 'POST' },
  { url: `${API_ROOT}/verificacion-correo/solicitar-codigo`, metodo: 'GET' },
  { url: `${API_ROOT}/usuario/solicitar-codigo`, metodo: 'POST' },
  { url: `${API_ROOT}/usuario/solicitar-codigo`, metodo: 'GET' }
];

/**
 * Interfaz que representa la respuesta del servidor al autenticar un usuario con JWT.
 */
export interface LoginJwtResponse {
  /** Token JWT generado por el servidor para autorizar peticiones posteriores. */
  token: string;
  /** Datos del usuario autenticado. */
  usuario: UsuarioDTO;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private currentUserSubject = new BehaviorSubject<UsuarioDTO | null>(this.loadFromStorage());
  currentUser$ = this.currentUserSubject.asObservable();

  private rutaSolicitarCodigoEncontrada: { url: string; metodo: 'GET' | 'POST' } | null = null;

  constructor(private http: HttpClient) {}

  /**
   * Carga el usuario almacenado en localStorage al iniciar la app.
   * Verifica además que el token JWT no haya expirado.
   */
  private loadFromStorage(): UsuarioDTO | null {
    const token = localStorage.getItem('jwtToken');
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const ahora = Math.floor(Date.now() / 1000);
      if (payload.exp < ahora) {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('usuario');
        return null;
      }
    } catch {
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('usuario');
      return null;
    }

    const data = localStorage.getItem('usuario');
    return data ? JSON.parse(data) : null;
  }

  /**
   * Intenta solicitar el código probando múltiples rutas/métodos hasta encontrar
   * uno que no devuelva 404. La ruta exitosa queda memorizada para futuras llamadas.
   */
  solicitarCodigo(correo: string): Observable<string> {
    if (this.rutaSolicitarCodigoEncontrada) {
      return this.invocarSolicitarCodigo(this.rutaSolicitarCodigoEncontrada, correo);
    }
    return this.probarRutas(RUTAS_SOLICITAR_CODIGO, 0, correo);
  }

  private probarRutas(
    rutas: Array<{ url: string; metodo: 'GET' | 'POST' }>,
    indice: number,
    correo: string
  ): Observable<string> {
    if (indice >= rutas.length) {
      return throwError(() => new HttpErrorResponse({
        error: 'No se encontró el endpoint para solicitar el código de verificación. Verifica que el backend esté corriendo.',
        status: 0,
        statusText: 'No endpoint found'
      }));
    }

    const ruta = rutas[indice];

    return this.invocarSolicitarCodigo(ruta, correo).pipe(
      tap(() => { this.rutaSolicitarCodigoEncontrada = ruta; }),
      catchError((err: HttpErrorResponse) => {
        if (err.status === 404 || err.status === 405 || err.status === 0) {
          return this.probarRutas(rutas, indice + 1, correo);
        }
        return throwError(() => err);
      })
    );
  }

  private invocarSolicitarCodigo(
    ruta: { url: string; metodo: 'GET' | 'POST' },
    correo: string
  ): Observable<string> {
    const params = new HttpParams().set('correo', correo);
    const opciones = { params, responseType: 'text' as const };
    return ruta.metodo === 'POST'
      ? this.http.post(ruta.url, null, opciones)
      : this.http.get(ruta.url, opciones);
  }

  registrar(data: {
    nombre: string;
    apellido: string;
    correo: string;
    nombreUsuario: string;
    contrasena: string;
    telefono: string;
    codigoVerificacion: string;
  }): Observable<string> {
    let params = new HttpParams();
    Object.entries(data).forEach(([k, v]) => params = params.set(k, String(v)));
    return this.http.post(`${USUARIO}/crear`, null, {
      params,
      responseType: 'text'
    });
  }

  /**
   * Envía las credenciales al servidor y, si son correctas, almacena
   * el token JWT y los datos del usuario en localStorage.
   *
   * @param nombreUsuario - Nombre de usuario.
   * @param contrasena - Contraseña del usuario.
   * @returns Observable de {@link LoginJwtResponse} con el token y el usuario.
   */
  login(nombreUsuario: string, contrasena: string): Observable<LoginJwtResponse> {
    const params = new HttpParams()
      .set('nombreUsuario', nombreUsuario)
      .set('contrasena', contrasena);
    return this.http.post<LoginJwtResponse>(`${USUARIO}/login`, null, { params }).pipe(
      tap(res => {
        if (res?.token) {
          localStorage.setItem('jwtToken', res.token);
        }
        if (res?.usuario) {
          localStorage.setItem('usuario', JSON.stringify(res.usuario));
          this.currentUserSubject.next(res.usuario);
        }
      })
    );
  }

  /**
   * Cierra la sesión eliminando el token JWT y los datos del usuario de localStorage.
   */
  logout(): void {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('usuario');
    this.currentUserSubject.next(null);
  }

  miPerfil(): Observable<UsuarioDTO> {
    return this.http.get<UsuarioDTO>(`${USUARIO}/miPerfil`);
  }

  /**
   * Verifica si existe una sesión activa comprobando que el token JWT
   * está presente y no ha expirado.
   * Si el token ha expirado, cierra la sesión automáticamente.
   *
   * @returns `true` si el usuario está autenticado y el token es válido, `false` en caso contrario.
   */
  isLoggedIn(): boolean {
    const token = localStorage.getItem('jwtToken');
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const ahora = Math.floor(Date.now() / 1000);
      if (payload.exp < ahora) {
        this.logout();
        return false;
      }
    } catch {
      this.logout();
      return false;
    }

    return true;
  }

  isAdmin(): boolean {
    const u = this.currentUserSubject.value;
    return u?.roles?.includes('ADMIN') ?? false;
  }

  getCurrentUser(): UsuarioDTO | null {
    return this.currentUserSubject.value;
  }

  clearLocal(): void {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('usuario');
    this.currentUserSubject.next(null);
  }
}
