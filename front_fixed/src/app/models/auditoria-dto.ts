/**
 * DTO que representa un registro de auditoría retornado por el backend.
 * Cada registro captura una acción relevante realizada por un usuario
 * dentro de la plataforma.
 */
export interface AuditoriaDTO {
    /** Identificador único del registro de auditoría. */
    id?: number;
    /** ID del usuario que realizó la acción auditada. */
    usuarioId?: number;
    /** Nombre de usuario en el momento en que se realizó la acción. */
    nombreUsuario?: string;
    /** Rol del usuario en el momento de ejecutar la acción. */
    rolUsuario?: 'USUARIO' | 'ADMIN';
    /** Tipo de acción ejecutada y registrada. */
    tipoAccion?: 'CREATE' | 'READ' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT' | 'CONVERSION';
    /** Descripción legible de la acción realizada. */
    descripcion?: string;
    /** Fecha y hora en que se registró la acción (ISO 8601). */
    fecha?: string;
    /** Indica si el registro está expandido en la vista del dashboard. */
    expandida?: boolean;
}