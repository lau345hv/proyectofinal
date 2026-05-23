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
  cargando: boolean = false;
  error: string = '';

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
    switch (tipo) {
      case 'AUDIO':  return '#FF6B4A';
      case 'VIDEO':  return '#4361EE';
      case 'IMAGEN': return '#2EC4B6';
      default:       return '#888';
    }
  }

  iconoEstado(estado: string): string {
    switch (estado) {
      case 'COMPLETADO': return '✓';
      case 'PENDIENTE':  return '⏳';
      case 'EN_PROCESO': return '⚙️';
      case 'FALLIDO':    return '✕';
      default:           return '?';
    }
  }
}
