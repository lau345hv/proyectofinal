/**
 * DTO con las estadísticas generales que el backend expone para el
 * panel de administración.
 */
export interface ResumenAdmin {
    /** Número total de usuarios registrados en el sistema. */
    totalUsuarios: number;
    /** Número total de conversiones realizadas en el sistema. */
    totalConversiones: number;
    /** Mapa con el conteo de conversiones agrupadas por tipo de archivo. */
    conversionesPorTipo?: { [key: string]: number };
    /** Mapa con el conteo de conversiones agrupadas por estado. */
    conversionesPorEstado?: { [key: string]: number };
    /** Mapa con el conteo de usuarios agrupados por rol. */
    usuariosPorRol?: { [key: string]: number };
}