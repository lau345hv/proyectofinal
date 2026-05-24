import { UsuarioDTO } from './usuario-dto';

/**
 * Respuesta cruda que retorna el backend tras un login o registro exitoso.
 * Contiene el token JWT y los datos mínimos del usuario autenticado.
 */
export interface AuthBackendResponse {
    /** Token JWT que debe incluirse en cada petición autenticada. */
    token: string;
    /** Rol del usuario autenticado (ej. 'USUARIO' o 'ADMIN'). */
    rol: string;
    /** Identificador único del usuario en la base de datos. */
    id: number;
    /** Nombre de usuario único con el que inició sesión. */
    nombreUsuario: string;
}

/**
 * Respuesta normalizada que el servicio expone al resto de la aplicación
 * tras un login o registro exitoso.
 */
export interface LoginResponse {
    /** Mensaje de confirmación de la operación. */
    mensaje: string;
    /** Datos del usuario autenticado. */
    usuario: UsuarioDTO;
}