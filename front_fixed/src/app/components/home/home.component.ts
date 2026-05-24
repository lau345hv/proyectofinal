import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * Configuración de una herramienta de conversión mostrada en la página de inicio.
 */
interface HerramientaConversion {
    /** Identificador único de la herramienta (usado como parámetro de ruta). */
    id: string;
    /** Título legible de la herramienta. */
    titulo: string;
    /** Descripción breve de la funcionalidad. */
    descripcion: string;
    /** Tipo de archivo que maneja la herramienta. */
    tipo: 'audio' | 'video' | 'imagen';
    /** Lista de formatos soportados para mostrar en la tarjeta. */
    formatos: string[];
    /** Color de acento de la tarjeta en formato CSS. */
    color: string;
    /** Nombre del ícono asociado a la herramienta. */
    icono: string;
}

/**
 * Componente de página de inicio.
 *
 * Muestra las tres herramientas de conversión disponibles (audio, video
 * e imagen) como tarjetas interactivas. Al hacer clic en una tarjeta,
 * si el usuario no tiene sesión activa lo redirige al login; en caso
 * contrario navega a la herramienta de conversión correspondiente.
 */
@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})
export class HomeComponent {

    /**
     * Lista de herramientas de conversión disponibles en la plataforma.
     * Cada elemento define la configuración visual y funcional de una tarjeta.
     */
    herramientas: HerramientaConversion[] = [
        {
            id: 'audio',
            titulo: 'Convertir Audio',
            descripcion: 'Convierte tus archivos de audio entre los formatos más populares con alta calidad.',
            tipo: 'audio',
            formatos: ['MP3', 'WAV', 'AAC', 'FLAC', 'OGG', 'M4A'],
            color: '#FF6B4A',
            icono: 'audio'
        },
        {
            id: 'video',
            titulo: 'Convertir Video',
            descripcion: 'Transforma tus videos a cualquier formato compatible con todos tus dispositivos.',
            tipo: 'video',
            formatos: ['MP4', 'MKV', 'AVI', 'MOV', 'WEBM', 'FLV'],
            color: '#4361EE',
            icono: 'video'
        },
        {
            id: 'imagen',
            titulo: 'Convertir Imagen',
            descripcion: 'Cambia el formato de tus imágenes manteniendo la mejor calidad posible.',
            tipo: 'imagen',
            formatos: ['JPG', 'PNG', 'WEBP', 'GIF', 'BMP', 'TIFF'],
            color: '#2EC4B6',
            icono: 'imagen'
        }
    ];

    /**
     * @param router Router de Angular para la navegación entre vistas.
     * @param auth   Servicio de autenticación (público para el template).
     */
    constructor(private router: Router, public auth: AuthService) {}

    /**
     * Navega a la herramienta de conversión indicada.
     *
     * Si el usuario no tiene sesión activa lo redirige a `/login` primero.
     *
     * @param tipo Identificador del tipo de conversión: `'audio'`, `'video'` o `'imagen'`.
     */
    irAConvertir(tipo: string): void {
        if (!this.auth.isLoggedIn()) {
            this.router.navigate(['/login']);
            return;
        }
        this.router.navigate(['/convertir', tipo]);
    }
}