import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConversionService } from '../../services/conversion.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthService } from '../../services/auth.service';

interface ConversionConfig {
  titulo: string;
  aceptar: string;
  tipoBackend: 'AUDIO' | 'VIDEO' | 'IMAGEN';
  formatos: string[];
  color: string;
  tamanoMaxMB: number;
}

@Component({
  selector: 'app-conversion-tool',
  templateUrl: './conversion-tool.component.html',
  styleUrls: ['./conversion-tool.component.css']
})
export class ConversionToolComponent implements OnInit {

  tipo = '';
  archivoSeleccionado: File | null = null;
  formatoDestino = '';
  convirtiendo = false;
  urlDescarga: string | null = null;
  nombreArchivo = '';
  error = '';
  exito = false;

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

  get config(): ConversionConfig {
    return this.configs[this.tipo] || this.configs['audio'];
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private conversionService: ConversionService,
    private errorHandler: ErrorHandlerService,
    private auth: AuthService
  ) {}

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

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.procesarArchivo(input.files[0]);
    }
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.procesarArchivo(files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

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
   * Verifica que el archivo coincida con el tipo de conversión seleccionado.
   * Valida por MIME type (archivo.type) y por extensión como respaldo.
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

  descargar(): void {
    if (!this.urlDescarga) return;
    window.open(this.urlDescarga, '_blank');
  }

  resetear(): void {
    this.archivoSeleccionado = null;
    this.formatoDestino = '';
    this.convirtiendo = false;
    this.urlDescarga = null;
    this.nombreArchivo = '';
    this.error = '';
    this.exito = false;
  }

  formatearTamano(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  private obtenerExtension(nombre: string): string | null {
    const idx = nombre.lastIndexOf('.');
    return idx >= 0 ? nombre.substring(idx + 1) : null;
  }
}
