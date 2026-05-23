import { Component, OnInit } from '@angular/core';
import { HistorialService } from '../../services/historial.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { HistorialConversionDTO } from '../../models/models';

@Component({
  selector: 'app-historial',
  templateUrl: './historial.component.html',
  styleUrls: ['./historial.component.css']
})
export class HistorialComponent implements OnInit {

  readonly COLORES_TIPO: Record<string, string> = {
    AUDIO:  '#FF6B4A',
    VIDEO:  '#4361EE',
    IMAGEN: '#2EC4B6',
  };
  readonly ICONOS_ESTADO: Record<string, string> = {
    COMPLETADO: '✓',
    PENDIENTE:  '⏳',
    EN_PROCESO: '⚙️',
    FALLIDO:    '✕',
  };

  conversiones: HistorialConversionDTO[] = [];
  cargando = false;
  error = '';

  constructor(
    private historialService: HistorialService,
    private errorHandler: ErrorHandlerService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

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
        if (err?.status === 204 || err?.status === 404) {
          this.conversiones = [];
        } else {
          this.error = this.errorHandler.extraerMensaje(
            err,
            'No se pudo cargar el historial. Intenta de nuevo.'
          );
        }
      }
    });
  }

  descargar(url: string | undefined): void {
    if (!url) return;
    window.open(url, '_blank');
  }

  colorTipo(tipo: string): string {
    return this.COLORES_TIPO[tipo] ?? '#888';
  }

  iconoEstado(estado: string): string {
    return this.ICONOS_ESTADO[estado] ?? '?';
  }
}
