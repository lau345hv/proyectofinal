import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { UsuarioDTO, HistorialConversionDTO } from '../../models/models';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {

  readonly ROL_ADMIN = 'ADMIN';
  readonly COLORES_TIPO: Record<string, string> = {
    AUDIO:  '#FF6B4A',
    VIDEO:  '#4361EE',
    IMAGEN: '#2EC4B6',
  };

  usuarios: UsuarioDTO[] = [];
  conversiones: HistorialConversionDTO[] = [];
  tabActiva: 'usuarios' | 'conversiones' = 'usuarios';
  cargandoUsuarios = false;
  cargandoConversiones = false;
  errorUsuarios = '';
  errorConversiones = '';
  resumenTexto = '';
  filtroTipo = '';
  filtroEstado = '';

  // Dialog de confirmación (reemplaza window.confirm — JS-0052)
  dialogVisible = false;
  dialogMensaje = '';
  private dialogCallback: (() => void) | null = null;

  constructor(
    private adminService: AdminService,
    private errorHandler: ErrorHandlerService
  ) {}

  ngOnInit(): void {
    this.cargarResumen();
    this.cargarUsuarios();
    this.cargarConversiones();
  }

  cargarResumen(): void {
    this.adminService.resumen().subscribe();
  }

  cargarUsuarios(): void {
    this.cargandoUsuarios = true;
    this.errorUsuarios = '';
    this.adminService.listarUsuarios().subscribe({
      next: (data) => {
        this.usuarios = data || [];
        this.cargandoUsuarios = false;
      },
      error: (err) => {
        this.cargandoUsuarios = false;
        this.errorUsuarios = this.errorHandler.extraerMensaje(
          err, 'No se pudo cargar la lista de usuarios.'
        );
      }
    });
  }

  cargarConversiones(): void {
    this.cargandoConversiones = true;
    this.errorConversiones = '';

    const obs = this.filtroTipo
      ? this.adminService.conversionesPorTipo(this.filtroTipo)
      : this.filtroEstado
        ? this.adminService.conversionesPorEstado(this.filtroEstado)
        : this.adminService.listarConversiones();

    obs.subscribe({
      next: (data: HistorialConversionDTO[]) => {
        this.conversiones = data || [];
        this.cargandoConversiones = false;
      },
      error: (err: unknown) => {
        this.cargandoConversiones = false;
        this.errorConversiones = this.errorHandler.extraerMensaje(
          err, 'No se pudo cargar el historial de conversiones.'
        );
      }
    });
  }

  cambiarFiltroTipo(tipo: string): void {
    this.filtroTipo = this.filtroTipo === tipo ? '' : tipo;
    this.filtroEstado = '';
    this.cargarConversiones();
  }

  cambiarFiltroEstado(estado: string): void {
    this.filtroEstado = this.filtroEstado === estado ? '' : estado;
    this.filtroTipo = '';
    this.cargarConversiones();
  }

  limpiarFiltros(): void {
    this.filtroTipo = '';
    this.filtroEstado = '';
    this.cargarConversiones();
  }

  eliminarUsuario(id: number | undefined): void {
    if (!id) return;
    this.mostrarDialogo(
      '¿Estás seguro de que quieres eliminar este usuario? Esta acción no se puede deshacer.',
      () => {
        this.adminService.eliminarUsuario(id).subscribe({
          next: () => {
            this.usuarios = this.usuarios.filter(u => u.id !== id);
            this.cargarResumen();
          },
          error: (err) => {
            this.errorUsuarios = this.errorHandler.extraerMensaje(
              err, 'No se pudo eliminar el usuario.'
            );
          }
        });
      }
    );
  }

  eliminarHistorialDeUsuario(usuarioId: number | undefined, nombreUsuario: string): void {
    if (!usuarioId) return;
    this.mostrarDialogo(
      `¿Eliminar todo el historial de conversiones de "${nombreUsuario}"? Esta acción no se puede deshacer.`,
      () => {
        this.adminService.eliminarHistorialPorUsuario(usuarioId).subscribe({
          next: () => {
            this.conversiones = this.conversiones.filter(c => c.usuarioId !== usuarioId);
            this.cargarResumen();
          },
          error: (err) => {
            this.errorConversiones = this.errorHandler.extraerMensaje(
              err, 'No se pudo eliminar el historial del usuario.'
            );
          }
        });
      }
    );
  }

  confirmarDialogo(): void {
    if (this.dialogCallback) this.dialogCallback();
    this.cerrarDialogo();
  }

  cerrarDialogo(): void {
    this.dialogVisible = false;
    this.dialogMensaje = '';
    this.dialogCallback = null;
  }

  esAdmin(roles: string[] | undefined): boolean {
    return roles?.includes(this.ROL_ADMIN) ?? false;
  }

  colorTipo(tipo: string): string {
    return this.COLORES_TIPO[tipo] ?? '#888';
  }

  private mostrarDialogo(mensaje: string, callback: () => void): void {
    this.dialogMensaje = mensaje;
    this.dialogCallback = callback;
    this.dialogVisible = true;
  }
}
