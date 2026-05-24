import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConversionService } from '../../services/conversion.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthService } from '../../services/auth.service';

/**
 * Configuración de una herramienta de conversión específica por tipo de archivo.
 */
interface ConversionConfig {
    /** Título legible mostrado en la interfaz. */
    titulo: string;
    /** Valor del atributo `accept` del input file (ej. `'audio/*'`). */
    aceptar: string;
    /** Identificador del tipo que espera el backend: `'AUDIO'`, `'VIDEO'` o `'IMAGEN'`. */
    tipoBackend: 'AUDIO' | 'VIDEO' | 'IMAGEN';
    /** Lista de extensiones de salida disponibles para el usuario. */
    formatos: string[];
    /** Color de acento de la herramienta en formato CSS. */
    color: string;
    /** Tamaño máximo permitido del archivo en megabytes. */
    tamanoMaxMB: number;
}

/**
 * Componente genérico de conversión de archivos.
 *
 * Se activa mediante el parámetro de ruta `tipo` (valores: `audio`, `video`,
 * `imagen`). Si el tipo no está reconocido redirige a `/`. Soporta selección
 * de archivo por clic y por arrastrar y soltar (drag & drop).
 *
 * El flujo de conversión es:
 * 1. El usuario selecciona o arrastra un archivo.
 * 2. El componente valida el tipo MIME / extensión y el tamaño.
 * 3. El usuario elige el formato de salida.
 * 4. Al pulsar "Convertir" se llama a `ConversionService.convertir()`.
 * 5. Ante éxito se muestra el enlace de descarga; ante error se muestra
 *    el mensaje correspondiente.
 */
@Component({
    selector: 'app-conversion-tool',
    templateUrl: './conversion-tool.component.html',
    styleUrls: ['./conversion-tool.component.css']
})
export class ConversionToolComponent implements OnInit {

    /** Tipo de conversión activo, leído desde el parámetro de ruta `tipo`. */
    tipo: string = '';

    /** Archivo seleccionado por el usuario, o `null` si no se ha seleccionado ninguno. */
    archivoSeleccionado: File | null = null;

    /** Extensión del formato de salida elegida por el usuario. */
    formatoDestino: string = '';

    /** Indica si la conversión está en curso. */
    convirtiendo: boolean = false;

    /** URL de descarga del archivo convertido, o `null` si aún no está disponible. */
    urlDescarga: string | null = null;

    /** Nombre sugerido para el archivo descargado (base + extensión de destino). */
    nombreArchivo: string = '';

    /** Mensaje de error a mostrar al usuario. */
    error: string = '';

    /** Indica si la conversión finalizó con éxito. */
    exito: boolean = false;

    /**
     * Mapa de configuraciones por tipo de archivo.
     * Las claves coinciden con los posibles valores del parámetro de ruta `tipo`.
     */
    configs: { [key: string]: ConversionConfig } = {
        audio: {
            titulo: 'Convertir Audio',
            aceptar: 'audio/*',
            tipoBackend: 'AUDIO',
            formatos: ['mp3', 'wav', 'aac', 'flac', 'ogg', 'm4a'],
            color: '#FF6B4A',
            tamanoMaxMB: 100
        },
        video: {
            titulo: 'Convertir Video',
            aceptar: 'video/*',
            tipoBackend: 'VIDEO',
            formatos: ['mp4', 'mkv', 'avi', 'mov', 'webm', 'flv'],
            color: '#4361EE',
            tamanoMaxMB: 500
        },
        imagen: {
            titulo: 'Convertir Imagen',
            aceptar: 'image/*',
            tipoBackend: 'IMAGEN',
            formatos: ['jpg', 'png', 'webp', 'gif', 'bmp', 'tiff'],
            color: '#2EC4B6',
            tamanoMaxMB: 50
        }
    };

    /**
     * Retorna la configuración activa según el tipo de ruta actual.
     * Si el tipo no existe, devuelve la configuración de audio como fallback.
     */
    get config(): ConversionConfig {
        return this.configs[this.tipo] || this.configs['audio'];
    }

    /**
     * @param route             Ruta activa para leer el parámetro `tipo`.
     * @param router            Router de Angular para redirigir si el tipo es inválido.
     * @param conversionService Servicio HTTP de conversión de archivos.
     * @param errorHandler      Servicio centralizado de manejo de errores HTTP.
     * @param auth              Servicio de autenticación para limpiar sesión en 401.
     */
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private conversionService: ConversionService,
        private errorHandler: ErrorHandlerService,
        private auth: AuthService
    ) {}

    /**
     * Lee el parámetro de ruta `tipo` y redirige a `/` si no es reconocido.
     * Resetea el estado del componente cada vez que el parámetro cambia.
     */
    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.tipo = params['tipo'];
            if (!this.configs[this.tipo]) {
                this.router.navigate(['/']);
                return;
            }
            this.resetear();
        });
    }

    /**
     * Manejador del evento `change` del input de tipo file.
     * Extrae el primer archivo seleccionado y lo procesa.
     *
     * @param event Evento `change` del elemento `<input type="file">`.
     */
    onFileChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.procesarArchivo(input.files[0]);
        }
    }

    /**
     * Manejador del evento `drop` de drag & drop.
     * Cancela el comportamiento por defecto del navegador y procesa el
     * primer archivo soltado.
     *
     * @param event Evento de arrastre con los archivos soltados.
     */
    onDrop(event: DragEvent): void {
        event.preventDefault();
        const files = event.dataTransfer?.files;
        if (files && files.length > 0) {
            this.procesarArchivo(files[0]);
        }
    }

    /**
     * Cancela el comportamiento por defecto del navegador durante el arrastre
     * para permitir el evento `drop`.
     *
     * @param event Evento de arrastre sobre la zona de drop.
     */
    onDragOver(event: DragEvent): void {
        event.preventDefault();
    }

    /**
     * Valida el tipo MIME, la extensión y el tamaño del archivo seleccionado.
     * Si pasa todas las validaciones, lo asigna a `archivoSeleccionado`.
     * En caso contrario asigna un mensaje a `error` y no guarda el archivo.
     *
     * @param archivo Archivo a procesar.
     */
    private procesarArchivo(archivo: File): void {
        this.error = '';
        this.exito = false;
        this.urlDescarga = null;

        const errorTipo = this.validarTipoArchivo(archivo);
        if (errorTipo) {
            this.error = errorTipo;
            this.archivoSeleccionado = null;
            return;
        }

        const tamanoMB = archivo.size / (1024 * 1024);
        if (tamanoMB > this.config.tamanoMaxMB) {
            this.error = `El archivo es muy grande (${tamanoMB.toFixed(1)} MB). Tamaño máximo: ${this.config.tamanoMaxMB} MB.`;
            return;
        }

        if (archivo.size === 0) {
            this.error = 'El archivo está vacío. Selecciona otro.';
            return;
        }

        this.archivoSeleccionado = archivo;
    }

    /**
     * Verifica que el tipo MIME o la extensión del archivo correspondan
     * al tipo de conversión activo.
     *
     * @param archivo Archivo a validar.
     * @returns `null` si el archivo es válido, o un string con el mensaje
     *          de error descriptivo si no lo es.
     */
    private validarTipoArchivo(archivo: File): string | null {
        const mime = (archivo.type || '').toLowerCase();
        const extension = (this.obtenerExtension(archivo.name) || '').toLowerCase();

        const extensionesAudio  = ['mp3', 'wav', 'aac', 'flac', 'ogg', 'm4a', 'wma', 'opus', 'aiff', 'amr'];
        const extensionesVideo  = ['mp4', 'mkv', 'avi', 'mov', 'webm', 'flv', 'wmv', 'mpeg', 'mpg', '3gp'];
        const extensionesImagen = ['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp', 'tiff', 'tif', 'svg', 'heic'];

        const nombresUsuarioAmigables: { [k: string]: string } = {
            audio: 'audio (MP3, WAV, AAC, FLAC, OGG, M4A...)',
            video: 'video (MP4, MKV, AVI, MOV, WEBM, FLV...)',
            imagen: 'imagen (JPG, PNG, WEBP, GIF, BMP, TIFF...)'
        };

        let coincide = false;
        let tipoDetectado: string | null = null;

        if (mime.startsWith('audio/') || extensionesAudio.includes(extension)) {
            tipoDetectado = 'audio';
        } else if (mime.startsWith('video/') || extensionesVideo.includes(extension)) {
            tipoDetectado = 'video';
        } else if (mime.startsWith('image/') || extensionesImagen.includes(extension)) {
            tipoDetectado = 'imagen';
        }

        if (tipoDetectado === this.tipo) {
            coincide = true;
        }

        if (!coincide) {
            const esperado = nombresUsuarioAmigables[this.tipo] || this.tipo;
            if (tipoDetectado && tipoDetectado !== this.tipo) {
                return `El archivo seleccionado es un ${tipoDetectado}, pero esta sección sólo acepta archivos de ${esperado}. Sube un archivo válido o cambia a la sección de ${tipoDetectado === 'imagen' ? 'imágenes' : tipoDetectado + 's'}.`;
            }
            return `El archivo seleccionado no es un archivo de ${esperado} válido. Sube un archivo del tipo correcto.`;
        }

        return null;
    }

    /**
     * Inicia la conversión del archivo seleccionado al formato de destino elegido.
     *
     * Validaciones previas:
     * - Debe haber un archivo seleccionado.
     * - Debe haberse elegido un formato de destino.
     * - El formato de destino no puede ser igual al formato de origen.
     *
     * Ante un error 401 limpia la sesión local y redirige al login tras 1 500 ms.
     */
    convertir(): void {
        if (!this.archivoSeleccionado) {
            this.error = 'Selecciona un archivo primero.';
            return;
        }
        if (!this.formatoDestino) {
            this.error = 'Selecciona un formato de destino.';
            return;
        }

        const extActual = this.obtenerExtension(this.archivoSeleccionado.name);
        if (extActual && extActual.toLowerCase() === this.formatoDestino.toLowerCase()) {
            this.error = `El archivo ya está en formato .${this.formatoDestino.toUpperCase()}. Elige un formato diferente.`;
            return;
        }

        this.convirtiendo = true;
        this.error = '';
        this.urlDescarga = null;

        this.conversionService.convertir(
            this.archivoSeleccionado,
            this.config.tipoBackend,
            this.formatoDestino
        ).subscribe({
            next: (urlDescarga: string) => {
                this.convirtiendo = false;
                if (!urlDescarga || urlDescarga.trim().length === 0) {
                    this.error = 'El servidor devolvió una respuesta vacía. Intenta de nuevo.';
                    return;
                }
                this.urlDescarga = urlDescarga.trim();
                const nombreBase = this.archivoSeleccionado!.name.replace(/\.[^/.]+$/, '');
                this.nombreArchivo = `${nombreBase}.${this.formatoDestino}`;
                this.exito = true;
            },
            error: (err) => {
                this.convirtiendo = false;
                if (err?.status === 401) {
                    this.error = 'Tu sesión expiró. Inicia sesión nuevamente.';
                    this.auth.clearLocal();
                    setTimeout(() => this.router.navigate(['/login']), 1500);
                    return;
                }
                this.error = this.errorHandler.extraerMensaje(
                    err,
                    'Error durante la conversión. Verifica el archivo e intenta de nuevo.'
                );
            }
        });
    }

    /**
     * Abre en una nueva pestaña la URL de descarga del archivo convertido.
     * No hace nada si `urlDescarga` es `null`.
     */
    descargar(): void {
        if (!this.urlDescarga) return;
        window.open(this.urlDescarga, '_blank');
    }

    /**
     * Restablece todos los campos del estado del componente a sus valores
     * iniciales, dejándolo listo para una nueva conversión.
     */
    resetear(): void {
        this.archivoSeleccionado = null;
        this.formatoDestino = '';
        this.convirtiendo = false;
        this.urlDescarga = null;
        this.nombreArchivo = '';
        this.error = '';
        this.exito = false;
    }

    /**
     * Formatea un tamaño en bytes a una cadena legible con unidad.
     *
     * @param bytes Tamaño del archivo en bytes.
     * @returns Cadena con el tamaño formateado: `'500 B'`, `'2.0 KB'` o `'3.0 MB'`.
     */
    formatearTamano(bytes: number): string {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    /**
     * Extrae la extensión de un nombre de archivo.
     *
     * @param nombre Nombre del archivo (ej. `'video.mp4'`).
     * @returns Extensión sin el punto (ej. `'mp4'`), o `null` si no tiene extensión.
     */
    private obtenerExtension(nombre: string): string | null {
        const idx = nombre.lastIndexOf('.');
        return idx >= 0 ? nombre.substring(idx + 1) : null;
    }
}