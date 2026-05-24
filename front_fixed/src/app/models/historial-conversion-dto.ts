/** Categoría del archivo que se convierte. */
export type TipoArchivo      = 'AUDIO' | 'VIDEO' | 'IMAGEN';

/** Estado del proceso de conversión. */
export type EstadoConversion = 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO' | 'FALLIDO';

/**
 * DTO que representa una conversión de archivo registrada en el historial
 * del usuario o en el panel de administración.
 */
export interface HistorialConversionDTO {
    /** Identificador único del registro de conversión. */
    id?: number;
    /** Fecha y hora en que se realizó la conversión (ISO 8601). */
    fechaConversion?: string;
    /** Categoría del archivo (AUDIO, VIDEO o IMAGEN). */
    tipoArchivo: TipoArchivo;
    /** Extensión del archivo original (ej. 'mp4', 'wav'). */
    formatoOrigen: string;
    /** Extensión del archivo convertido (ej. 'mp3', 'mkv'). */
    formatoDestino: string;
    /** Nombre original del archivo subido por el usuario. */
    nombreArchivoOriginal?: string;
    /** Nombre del archivo resultante tras la conversión. */
    nombreArchivoConvertido?: string;
    /** Ruta o URL del archivo original almacenado en el servidor. */
    rutaArchivoOriginal?: string;
    /** Ruta o URL del archivo convertido listo para descarga. */
    rutaArchivoConvertido?: string;
    /** Estado actual del proceso de conversión. */
    estado: EstadoConversion;
    /** ID del usuario propietario de la conversión. */
    usuarioId?: number;
}