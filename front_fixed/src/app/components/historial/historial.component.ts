import { Component, OnInit } from '@angular/core';
import { HistorialService } from '../../services/historial.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { HistorialConversionDTO } from '../../models/historial-conversion-dto';

/**
 * Componente que muestra el historial de conversiones del usuario autenticado.
 *
 * Carga la lista al inicializarse y ofrece métodos auxiliares para
 * formatear fechas, asignar colores por tipo de archivo y mostrar
 * íconos según el estado de cada conversión.
 */
@Component({
    selector: 'app-historial',
    templateUrl: './historial.component.html',
    styleUrls: ['./historial.component.css']
})
export class HistorialComponent implements OnInit {

    /** Lista de conversiones del usuario cargadas desde el backend. */
    conversiones: HistorialConversionDTO[] = [];

    /** Indica si la carga de datos está en curso. */
    cargando: boolean = false;

    /** Mensaje de error a mostrar si la carga falla. */
    error: string = '';

    /**
     * @param historialService Servicio HTTP para obtener el historial del usuario.
     * @param errorHandler     Servicio centralizado de manejo de errores HTTP.
     */
    constructor(
        private historialService: HistorialService,
        private errorHandler: ErrorHandlerService
    ) {}

    /**
     * Carga el historial de conversiones al inicializar el componente.
     */
    ngOnInit(): void {
        this.cargar();
    }

    /**
     * Solicita al backend el historial de conversiones del usuario autenticado
     * y actualiza la lista `conversiones`. En caso de error muestra un mensaje.
     */
    cargar(): void {
        this.cargando = true;
        this.error = '';
        this.historialService.misConversiones().subscribe({
            next: (data) => {
                this.conversiones = data || [];
                this.cargando = false;
            },
            error: (err) => {
                this.cargando = false;
                if (err?.status === 204) {
                    this.conversiones = [];
                    this.error = '';
                    return;
                }
                this.error = this.errorHandler.extraerMensaje(
                    err,
                    'No se pudo cargar el historial. Intenta de nuevo.'
                );
            }
        });
    }

    /**
     * Abre en una nueva pestaña la URL de descarga del archivo convertido.
     * No hace nada si la conversión no tiene URL de descarga disponible.
     *
     * @param conv Registro de conversión cuyo archivo convertido se desea descargar.
     */
    descargar(conv: HistorialConversionDTO): void {
        const url = conv.rutaArchivoConvertido;
        if (!url) return;
        window.open(url, '_blank');
    }

    /**
     * Abre en una nueva pestaña la URL del archivo original.
     * No hace nada si la conversión no tiene URL del archivo original.
     *
     * @param conv Registro de conversión cuyo archivo original se desea descargar.
     */
    descargarOriginal(conv: HistorialConversionDTO): void {
        const url = conv.rutaArchivoOriginal;
        if (!url) return;
        window.open(url, '_blank');
    }

    /**
     * Retorna el color de acento CSS asociado al tipo de archivo.
     *
     * @param tipo Categoría del archivo: `'AUDIO'`, `'VIDEO'` o `'IMAGEN'`.
     * @returns Color en formato hexadecimal, o `'#888'` si el tipo no es reconocido.
     */
    colorTipo(tipo: string): string {
        switch (tipo) {
            case 'AUDIO':  return '#FF6B4A';
            case 'VIDEO':  return '#4361EE';
            case 'IMAGEN': return '#2EC4B6';
            default:       return '#888';
        }
    }

    /**
     * Retorna el ícono Unicode asociado al estado de una conversión.
     *
     * @param estado Estado de la conversión: `'COMPLETADO'`, `'PENDIENTE'`,
     *               `'EN_PROCESO'` o `'FALLIDO'`.
     * @returns Carácter o emoji representativo del estado, o `'?'` si es desconocido.
     */
    iconoEstado(estado: string): string {
        switch (estado) {
            case 'COMPLETADO': return '✓';
            case 'PENDIENTE':  return '⏳';
            case 'EN_PROCESO': return '⚙️';
            case 'FALLIDO':    return '✕';
            default:           return '?';
        }
    }

    /**
     * Formatea una fecha ISO 8601 al formato local colombiano.
     *
     * @param fecha Fecha en formato ISO 8601, o `undefined` si no está disponible.
     * @returns Cadena con la fecha formateada, `'—'` si la fecha es nula, o la
     *          fecha original si el parseo falla.
     */
    formatearFecha(fecha: string | undefined): string {
        if (!fecha) return '—';
        try {
            return new Date(fecha).toLocaleString('es-CO');
        } catch {
            return fecha;
        }
    }
}