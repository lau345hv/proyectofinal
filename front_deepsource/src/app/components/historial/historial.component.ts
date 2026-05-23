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
      error: (err: unknown) => {
        this.cargando = false;
        const httpErr = err as { status?: number };
        if (httpErr?.status === 204 || httpErr?.status === 404) {
          this.conversiones = [];
        } else {
          this.error = this.errorHandler.extraerMensaje(
            err, 'No se pudo cargar el historial. Intenta de nuevo.'
          );
        }
      }
    });
  }

  // JS-0105: métodos sin 'this' → static
  static descargar(url: string | undefined): void {
    if (!url) return;
    window.open(url, '_blank');
  }

  static colorTipo(tipo: string): string {
    switch (tipo) {
      case 'AUDIO':  return '#FF6B4A';
      case 'VIDEO':  return '#4361EE';
      case 'IMAGEN': return '#2EC4B6';
      default:       return '#888';
    }
  }

  static iconoEstado(estado: string): string {
    switch (estado) {
      case 'COMPLETADO': return '✓';
      case 'PENDIENTE':  return '⏳';
      case 'EN_PROCESO': return '⚙️';
      case 'FALLIDO':    return '✕';
      default:           return '?';
    }
  }

  // Wrappers de instancia para el template Angular
  colorTipo(tipo: string): string { return HistorialComponent.colorTipo(tipo); }
  iconoEstado(estado: string): string { return HistorialComponent.iconoEstado(estado); }
  descargar(url: string | undefined): void { HistorialComponent.descargar(url); }
}
