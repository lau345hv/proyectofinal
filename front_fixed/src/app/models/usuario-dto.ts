/**
 * DTO que representa un usuario de la plataforma.
 * Se usa tanto para mostrar datos del perfil como para enviar
 * formularios de creación y actualización.
 */
export interface UsuarioDTO {
    /** Identificador único del usuario en la base de datos. */
    id?: number;
    /** Nombre de pila del usuario. */
    nombre: string;
    /** Apellido del usuario. */
    apellido: string;
    /** Correo electrónico único del usuario. */
    correo: string;
    /** Nombre de usuario único utilizado para iniciar sesión. */
    nombreUsuario: string;
    /** Contraseña en texto plano (solo se envía al crear o actualizar; nunca se recibe). */
    contrasena?: string;
    /** Número de teléfono colombiano de 10 dígitos. */
    telefono: string;
    /** Rol principal del usuario (ej. 'USUARIO' o 'ADMIN'). */
    rol?: string;
    /** Lista de roles del usuario (puede incluir múltiples roles). */
    roles?: string[];
    /** IDs de los registros de historial de conversión asociados al usuario. */
    historialConversionesId?: number[];
}