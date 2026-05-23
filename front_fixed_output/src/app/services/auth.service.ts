import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';
import { UsuarioDTO } from '../models/models';

const API   = 'http://localhost:8080';
const AUTH  = `${API}/autenticacion`;
const USER  = `${API}/usuario`;

/** Estructura que devuelve el backend en login/register */
interface AuthResponse {
  token: string;
  rol: string;
  id: number;
  nombreUsuario: string;
}

/** Estructura que espera el componente de login (mantiene compatibilidad) */
export interface LoginResponse {
  mensaje: string;
  usuario: UsuarioDTO;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly JWT_KEY = 'jwt_token';
  private readonly USER_KEY = 'usuario';
  private userSubject = new BehaviorSubject<UsuarioDTO | null>(this.cargarUsuario());
  currentUser$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ── Autenticación pública ──────────────────────────────────────────

  solicitarCodigo(correo: string): Observable<string> {
    return this.http.post(`${AUTH}/solicitar-codigo`, null, {
      params: { correo },
      responseType: 'text'
    });
  }

  registrar(data: {
    nombre: string; apellido: string; correo: string;
    nombreUsuario: string; contrasena: string; telefono: string;
    codigoVerificacion: string;
  }): Observable<LoginResponse> {
    const body = {
      nombre: data.nombre, apellido: data.apellido, correo: data.correo,
      nombreUsuario: data.nombreUsuario, contrasena: data.contrasena,
      telefono: data.telefono
    };
    return this.http.post<AuthResponse>(`${AUTH}/register`, body, {
      params: { codigoVerificacion: data.codigoVerificacion }
    }).pipe(
      tap(res => this.persistirSesion(res)),
      switchMap(res => of(AuthService.toLoginResponse(res)))
    );
  }

  login(nombreUsuario: string, contrasena: string): Observable<LoginResponse> {
    return this.http.post<AuthResponse>(`${AUTH}/login`, { nombreUsuario, contrasena }).pipe(
      tap(res => this.persistirSesion(res)),
      switchMap(res => of(AuthService.toLoginResponse(res)))
    );
  }

  logout(): Observable<string> {
    localStorage.removeItem(this.JWT_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.userSubject.next(null);
    return of('ok');
  }

  miPerfil(): Observable<UsuarioDTO> {
    return this.http.get<UsuarioDTO>(`${USER}/miPerfil`);
  }

  // ── Estado ────────────────────────────────────────────────────────

  isLoggedIn(): boolean {
    return Boolean(localStorage.getItem(this.JWT_KEY)) && Boolean(this.userSubject.value);
  }

  isAdmin(): boolean {
    const usuario = this.userSubject.value;
    if (!usuario) return false;
    const rol = (usuario as UsuarioDTO & { rol?: string }).rol;
    return usuario.roles?.includes('ADMIN') || rol === 'ADMIN' || false;
  }

  getCurrentUser(): UsuarioDTO | null {
    return this.userSubject.value;
  }

  clearLocal(): void {
    localStorage.removeItem(this.JWT_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.userSubject.next(null);
  }

  getAuthToken(): string | null {
    return localStorage.getItem(this.JWT_KEY);
  }

  // ── Helpers privados ──────────────────────────────────────────────

  private persistirSesion(res: AuthResponse): void {
    localStorage.setItem(this.JWT_KEY, res.token);
    const minimo: UsuarioDTO & { rol: string } = {
      id: res.id, nombreUsuario: res.nombreUsuario,
      nombre: '', apellido: '', correo: '', telefono: '',
      roles: [res.rol], rol: res.rol
    };
    this.guardarUsuario(minimo);

    this.http.get<UsuarioDTO>(`${USER}/miPerfil`).subscribe({
      next: perfil => {
        const completo: UsuarioDTO & { rol: string } = { ...perfil, roles: [res.rol], rol: res.rol };
        this.guardarUsuario(completo);
      },
    });
  }

  private static toLoginResponse(res: AuthResponse): LoginResponse {
    const usuario: UsuarioDTO & { rol: string } = {
      id: res.id, nombreUsuario: res.nombreUsuario,
      nombre: '', apellido: '', correo: '', telefono: '',
      roles: [res.rol], rol: res.rol
    };
    return { mensaje: 'ok', usuario };
  }

  private guardarUsuario(u: UsuarioDTO): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(u));
    this.userSubject.next(u);
  }

  private cargarUsuario(): UsuarioDTO | null {
    try {
      const raw = localStorage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }
}
