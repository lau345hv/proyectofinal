import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';
import { UsuarioDTO } from '../models/usuario-dto';
import { AuthBackendResponse, LoginResponse } from '../models/auth-backend-response';

const API  = 'https://gpcueb.org/SuperConvertLJJJ';
const AUTH = `${API}/autenticacion`;
const USER = `${API}/usuario`;

/**
 * Servicio central de autenticación de la aplicación.
 *
 * Gestiona el ciclo de vida de la sesión del usuario: registro, login,
 * logout y consulta del perfil. Persiste el token JWT y los datos del
 * usuario en `localStorage` y expone el usuario activo a través del
 * observable `currentUser$`.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

    /**
     * BehaviorSubject que mantiene el usuario activo. Se inicializa
     * leyendo el valor guardado en `localStorage`.
     */
    private userSubject = new BehaviorSubject<UsuarioDTO | null>(this.cargarUsuario());

    /**
     * Observable público del usuario activo. Los componentes pueden
     * suscribirse para reaccionar a cambios de sesión.
     */
    currentUser$ = this.userSubject.asObservable();

    /** @param http Cliente HTTP de Angular para las peticiones REST. */
    constructor(private http: HttpClient) {}

    /**
     * Paso 1 del registro: solicita el envío de un código de verificación
     * al correo indicado.
     *
     * @param correo Dirección de correo electrónico del futuro usuario.
     * @returns Observable que emite la respuesta del servidor como texto.
     */
    solicitarCodigo(correo: string): Observable<string> {
        return this.http.post(`${AUTH}/solicitar-codigo`, null, {
            params: { correo },
            responseType: 'text'
        });
    }

    /**
     * Paso 2 del registro: crea la cuenta del usuario enviando sus datos
     * junto con el código de verificación recibido por correo.
     *
     * El backend recibe el body como `UsuarioDTO` y el código como
     * query param `codigoVerificacion`.
     *
     * @param data Objeto con los datos del usuario y el código de verificación.
     * @returns Observable que emite un `LoginResponse` con el usuario creado.
     */
    registrar(data: {
        nombre: string; apellido: string; correo: string;
        nombreUsuario: string; contrasena: string; telefono: string;
        codigoVerificacion: string;
    }): Observable<LoginResponse> {
        const body = {
            nombre: data.nombre,
            apellido: data.apellido,
            correo: data.correo,
            nombreUsuario: data.nombreUsuario,
            contrasena: data.contrasena,
            telefono: data.telefono
        };
        return this.http.post<AuthBackendResponse>(`${AUTH}/register`, body, {
            params: { codigoVerificacion: data.codigoVerificacion }
        }).pipe(
            tap(res => this.persistirSesion(res)),
            switchMap(res => of(this.toLoginResponse(res)))
        );
    }

    /**
     * Inicia sesión con nombre de usuario y contraseña.
     *
     * El backend retorna `{ token, rol, id, nombreUsuario }`. El token
     * se guarda en `localStorage` y el perfil completo se carga en
     * segundo plano.
     *
     * @param nombreUsuario Nombre de usuario único.
     * @param contrasena    Contraseña en texto plano.
     * @returns Observable que emite un `LoginResponse` con el usuario autenticado.
     */
    login(nombreUsuario: string, contrasena: string): Observable<LoginResponse> {
        return this.http.post<AuthBackendResponse>(`${AUTH}/login`, { nombreUsuario, contrasena }).pipe(
            tap(res => this.persistirSesion(res)),
            switchMap(res => of(this.toLoginResponse(res)))
        );
    }

    /**
     * Cierra la sesión del usuario llamando al endpoint del backend y
     * limpiando los datos locales independientemente del resultado.
     *
     * @returns Observable que emite `'ok'` al completar.
     */
    logout(): Observable<string> {
        return this.http.post(`${AUTH}/logout`, {}, { responseType: 'text' }).pipe(
            tap({
                next: () => this.limpiarSesionLocal(),
                error: () => this.limpiarSesionLocal()
            }),
            switchMap(() => of('ok'))
        );
    }

    /**
     * Elimina el token JWT y los datos del usuario de `localStorage`
     * y emite `null` en `currentUser$`.
     */
    private limpiarSesionLocal(): void {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('usuario');
        this.userSubject.next(null);
    }

    /**
     * Obtiene el perfil completo del usuario autenticado desde el backend.
     *
     * @returns Observable que emite el `UsuarioDTO` del usuario activo.
     */
    miPerfil(): Observable<UsuarioDTO> {
        return this.http.get<UsuarioDTO>(`${USER}/miPerfil`);
    }

    /**
     * Verifica si el usuario tiene una sesión activa comprobando la
     * existencia del token JWT y del usuario en el BehaviorSubject.
     *
     * @returns `true` si hay sesión activa, `false` en caso contrario.
     */
    isLoggedIn(): boolean {
        return !!localStorage.getItem('jwt_token') && !!this.userSubject.value;
    }

    /**
     * Verifica si el usuario activo tiene rol de administrador.
     *
     * Comprueba tanto el campo `rol` como el array `roles` para compatibilidad
     * con distintas versiones de la respuesta del backend.
     *
     * @returns `true` si el usuario es ADMIN, `false` en caso contrario.
     */
    isAdmin(): boolean {
        const u = this.userSubject.value as any;
        if (!u) return false;
        return u.rol === 'ADMIN' || u.roles?.includes('ADMIN') || false;
    }

    /**
     * Retorna el usuario activo almacenado en el BehaviorSubject.
     *
     * @returns El `UsuarioDTO` actual, o `null` si no hay sesión activa.
     */
    getCurrentUser(): UsuarioDTO | null {
        return this.userSubject.value;
    }

    /**
     * Limpia la sesión local de forma pública.
     * Útil para forzar el cierre de sesión desde otros componentes
     * (ej. al detectar un error 401).
     */
    clearLocal(): void {
        this.limpiarSesionLocal();
    }

    /**
     * Retorna el token JWT guardado en `localStorage`.
     *
     * @returns El token como string, o `null` si no existe.
     */
    getAuthToken(): string | null {
        return localStorage.getItem('jwt_token');
    }

    /**
     * Persiste el token JWT en `localStorage`, guarda un objeto mínimo del
     * usuario de forma inmediata (para que `isAdmin()` funcione al instante)
     * y lanza una petición en segundo plano a `/miPerfil` para enriquecer
     * los datos con la información completa del usuario.
     *
     * @param res Respuesta cruda del backend tras login o registro.
     */
    private persistirSesion(res: AuthBackendResponse): void {
        localStorage.setItem('jwt_token', res.token);
        const minimo: any = {
            id: res.id,
            nombreUsuario: res.nombreUsuario,
            nombre: '', apellido: '', correo: '', telefono: '',
            rol: res.rol,
            roles: [res.rol]
        };
        this.guardarUsuario(minimo);

        this.http.get<UsuarioDTO>(`${USER}/miPerfil`).subscribe({
            next: perfil => {
                const completo: any = { ...perfil, rol: res.rol, roles: [res.rol] };
                this.guardarUsuario(completo);
            },
            error: () => {}
        });
    }

    /**
     * Construye un `LoginResponse` normalizado a partir de la respuesta
     * cruda del backend.
     *
     * @param res Respuesta cruda del backend.
     * @returns `LoginResponse` con mensaje `'ok'` y datos mínimos del usuario.
     */
    private toLoginResponse(res: AuthBackendResponse): LoginResponse {
        const usuario: any = {
            id: res.id,
            nombreUsuario: res.nombreUsuario,
            nombre: '', apellido: '', correo: '', telefono: '',
            rol: res.rol,
            roles: [res.rol]
        };
        return { mensaje: 'ok', usuario };
    }

    /**
     * Guarda el objeto de usuario en `localStorage` y lo emite en
     * el BehaviorSubject para notificar a los suscriptores.
     *
     * @param u Objeto de usuario a persistir.
     */
    private guardarUsuario(u: any): void {
        localStorage.setItem('usuario', JSON.stringify(u));
        this.userSubject.next(u);
    }

    /**
     * Carga el usuario almacenado en `localStorage` al inicializar el servicio.
     *
     * @returns El `UsuarioDTO` parseado, o `null` si no existe o el JSON es inválido.
     */
    private cargarUsuario(): UsuarioDTO | null {
        try {
            const raw = localStorage.getItem('usuario');
            return raw ? JSON.parse(raw) : null;
        } catch { return null; }
    }
}