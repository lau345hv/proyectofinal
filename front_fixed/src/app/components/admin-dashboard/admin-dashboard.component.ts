import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { UsuarioDTO } from '../../models/usuario-dto';
import { HistorialConversionDTO } from '../../models/historial-conversion-dto';
import { ResumenAdmin } from '../../models/resumen-admin';
import { AuditoriaDTO } from '../../models/auditoria-dto';
import { ActivatedRoute } from '@angular/router';

/**
 * Componente del panel de administración.
 *
 * Organiza la información en tres pestañas:
 * - **Usuarios:** lista, edición y eliminación de cuentas.
 * - **Conversiones:** historial global con filtros por tipo y estado.
 * - **Auditoría:** registro de acciones con filtros por tipo de acción y rol.
 *
 * La pestaña inicial puede determinarse mediante el dato de ruta `tabInicial`
 * (valor `'auditoria'` para abrir directamente en auditoría).
 */
@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {

    /** Pestaña activa en el panel. */
    tabActiva: 'usuarios' | 'conversiones' | 'auditoria' = 'usuarios';



    /** Lista de usuarios registrados en el sistema. */
    usuarios: UsuarioDTO[] = [];
    /** Indica si la carga de usuarios está en curso. */
    cargandoUsuarios = false;
    /** Mensaje de error al cargar o modificar usuarios. */
    errorUsuarios = '';



    /** Lista de conversiones actualmente mostrada (puede estar filtrada). */
    conversiones: HistorialConversionDTO[] = [];
    /** Indica si la carga de conversiones está en curso. */
    cargandoConversiones = false;
    /** Mensaje de error al cargar conversiones. */
    errorConversiones = '';
    /** Filtro activo por tipo de archivo (`'AUDIO'`, `'VIDEO'`, `'IMAGEN'` o `''`). */
    filtroTipo = '';
    /** Filtro activo por estado de conversión o `''` si no hay filtro. */
    filtroEstado = '';



    /** Estadísticas generales del sistema cargadas desde el backend. */
    resumen: ResumenAdmin | null = null;



    /** Lista de registros de auditoría actualmente mostrada. */
    auditoria: AuditoriaDTO[] = [];
    /** Indica si la carga de auditoría está en curso. */
    cargandoAuditoria = false;
    /** Mensaje de error al cargar la auditoría. */
    errorAuditoria = '';
    /** Filtro activo por tipo de acción (ej. `'LOGIN'`, `'CONVERSION'`) o `''`. */
    filtroAccion = '';
    /** Filtro activo por rol del usuario (`'USUARIO'`, `'ADMIN'`) o `''`. */
    filtroRolAuditoria = '';
    /** Total de registros de auditoría en la lista actual. */
    totalRegistrosAuditoria = 0;



    /** Controla la visibilidad del modal de edición de usuario. */
    mostrarModalEdicion = false;
    /** Copia del usuario que se está editando en el modal, o `null` si no hay ninguno. */
    usuarioEditando: UsuarioDTO | null = null;
    /** Indica si el guardado de la edición está en curso. */
    guardandoEdicion = false;
    /** Mensaje de error dentro del modal de edición. */
    errorEdicion = '';
    /** Mensaje de éxito dentro del modal de edición. */
    exitoEdicion = '';

    /**
     * @param adminService Servicio HTTP del panel de administración.
     * @param errorHandler Servicio centralizado de manejo de errores HTTP.
     * @param route        Ruta activa para leer el dato `tabInicial`.
     */
    constructor(
        private adminService: AdminService,
        private errorHandler: ErrorHandlerService,
        private route: ActivatedRoute
    ) {}

    /**
     * Inicializa el componente determinando la pestaña activa e iniciando
     * la carga del resumen, usuarios y conversiones. Si la pestaña inicial
     * es `'auditoria'`, también carga los registros de auditoría.
     */
    ngOnInit(): void {
        const tabInicial = this.route.snapshot.data['tabInicial'];
        if (tabInicial === 'auditoria') {
            this.tabActiva = 'auditoria';
        }
        this.cargarResumen();
        this.cargarUsuarios();
        this.cargarConversiones();
        if (this.tabActiva === 'auditoria') {
            this.cargarAuditoria();
        }
    }

    /**
     * Carga las estadísticas generales del sistema desde el backend
     * y las asigna a `resumen`.
     */
    cargarResumen(): void {
        this.adminService.resumen().subscribe({
            next: (data) => { this.resumen = data; },
            error: () => {}
        });
    }

    /**
     * Carga la lista completa de usuarios registrados.
     * Actualiza `usuarios`, `cargandoUsuarios` y `errorUsuarios`.
     */
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

    /**
     * Elimina el usuario con el ID indicado tras solicitar confirmación.
     * No hace nada si `id` es `undefined`. Tras la eliminación recarga
     * el resumen y actualiza la lista local.
     *
     * @param id Identificador del usuario a eliminar.
     */
    eliminarUsuario(id: number | undefined): void {
        if (!id) return;
        if (!confirm('¿Estás seguro de que quieres eliminar este usuario? Esta acción no se puede deshacer.')) return;

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

    /**
     * Abre el modal de edición con una copia del usuario seleccionado.
     * La contraseña se inicializa vacía para que solo se actualice si el
     * administrador ingresa un nuevo valor.
     *
     * @param usuario Usuario cuyos datos se cargarán en el modal.
     */
    abrirModalEdicion(usuario: UsuarioDTO): void {
        this.usuarioEditando = { ...usuario, contrasena: '' };
        this.mostrarModalEdicion = true;
        this.errorEdicion = '';
        this.exitoEdicion = '';
    }

    /**
     * Cierra el modal de edición y limpia el estado relacionado.
     */
    cerrarModalEdicion(): void {
        this.mostrarModalEdicion = false;
        this.usuarioEditando = null;
        this.errorEdicion = '';
        this.exitoEdicion = '';
    }

    /**
     * Envía al backend los datos editados del usuario en el modal.
     * No hace nada si no hay usuario en edición o si carece de `id`.
     * Tras una edición exitosa recarga la lista de usuarios y cierra
     * el modal después de 1 200 ms.
     */
    guardarEdicion(): void {
        if (!this.usuarioEditando || !this.usuarioEditando.id) return;

        this.guardandoEdicion = true;
        this.errorEdicion = '';
        this.exitoEdicion = '';

        this.adminService.editarUsuario(this.usuarioEditando.id, this.usuarioEditando).subscribe({
            next: () => {
                this.guardandoEdicion = false;
                this.exitoEdicion = 'Usuario actualizado correctamente.';
                this.cargarUsuarios();
                setTimeout(() => this.cerrarModalEdicion(), 1200);
            },
            error: (err) => {
                this.guardandoEdicion = false;
                this.errorEdicion = this.errorHandler.extraerMensaje(
                    err, 'No se pudo actualizar el usuario.'
                );
            }
        });
    }

    /**
     * Carga las conversiones aplicando el filtro activo de tipo o estado.
     * Si no hay ningún filtro activo, carga todas las conversiones.
     */
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

    /**
     * Activa o desactiva el filtro por tipo de archivo (toggle).
     * Si el tipo ya estaba activo lo limpia; en caso contrario lo aplica
     * y limpia el filtro por estado. Recarga las conversiones.
     *
     * @param tipo Tipo de archivo a filtrar: `'AUDIO'`, `'VIDEO'` o `'IMAGEN'`.
     */
    cambiarFiltroTipo(tipo: string): void {
        this.filtroTipo = this.filtroTipo === tipo ? '' : tipo;
        this.filtroEstado = '';
        this.cargarConversiones();
    }

    /**
     * Activa o desactiva el filtro por estado de conversión (toggle).
     * Si el estado ya estaba activo lo limpia; en caso contrario lo aplica
     * y limpia el filtro por tipo. Recarga las conversiones.
     *
     * @param estado Estado a filtrar: `'PENDIENTE'`, `'EN_PROCESO'`, `'COMPLETADO'` o `'FALLIDO'`.
     */
    cambiarFiltroEstado(estado: string): void {
        this.filtroEstado = this.filtroEstado === estado ? '' : estado;
        this.filtroTipo = '';
        this.cargarConversiones();
    }

    /**
     * Elimina todos los filtros activos de conversiones y recarga la lista completa.
     */
    limpiarFiltros(): void {
        this.filtroTipo = '';
        this.filtroEstado = '';
        this.cargarConversiones();
    }

    /**
     * Elimina todo el historial de conversiones de un usuario específico.
     * Primero obtiene la lista de conversiones del usuario, luego elimina
     * cada una individualmente y al terminar recarga el resumen.
     * No hace nada si `usuarioId` es `undefined`.
     *
     * @param usuarioId    ID del usuario cuyo historial se eliminará.
     * @param nombreUsuario Nombre del usuario para mostrar en el diálogo de confirmación.
     */
    eliminarHistorialDeUsuario(usuarioId: number | undefined, nombreUsuario: string): void {
        if (!usuarioId) return;
        if (!confirm(`¿Eliminar todo el historial de conversiones de "${nombreUsuario}"? Esta acción no se puede deshacer.`)) return;

        this.adminService.conversionesPorUsuario(usuarioId).subscribe({
            next: (lista) => {
                const ids = lista.map(c => c.id!).filter(Boolean);
                if (ids.length === 0) return;
                let completadas = 0;
                for (const id of ids) {
                    this.adminService.eliminarConversion(id).subscribe({
                        next: () => {
                            completadas++;
                            this.conversiones = this.conversiones.filter(c => c.id !== id);
                            if (completadas === ids.length) this.cargarResumen();
                        },
                        error: () => { completadas++; }
                    });
                }
            },
            error: (err) => {
                this.errorConversiones = this.errorHandler.extraerMensaje(
                    err, 'No se pudo cargar el historial del usuario.'
                );
            }
        });
    }

    /**
     * Carga los registros de auditoría aplicando el filtro activo de acción o rol.
     * Si no hay ningún filtro activo, carga todos los registros.
     */
    cargarAuditoria(): void {
        this.cargandoAuditoria = true;
        this.errorAuditoria = '';

        const obs = this.filtroAccion
            ? this.adminService.auditoriaPorTipoAccion(this.filtroAccion)
            : this.filtroRolAuditoria
                ? this.adminService.auditoriaPorRol(this.filtroRolAuditoria)
                : this.adminService.listarAuditoria();

        obs.subscribe({
            next: (data) => {
                this.auditoria = data || [];
                this.totalRegistrosAuditoria = this.auditoria.length;
                this.cargandoAuditoria = false;
            },
            error: (err) => {
                this.cargandoAuditoria = false;
                this.errorAuditoria = this.errorHandler.extraerMensaje(
                    err, 'No se pudo cargar el registro de auditoría.'
                );
            }
        });
    }

    /**
     * Activa o desactiva el filtro de auditoría por tipo de acción (toggle).
     * Limpia el filtro por rol y recarga la auditoría.
     *
     * @param accion Tipo de acción a filtrar (ej. `'LOGIN'`, `'CONVERSION'`).
     */
    cambiarFiltroAccion(accion: string): void {
        this.filtroAccion = this.filtroAccion === accion ? '' : accion;
        this.filtroRolAuditoria = '';
        this.cargarAuditoria();
    }

    /**
     * Activa o desactiva el filtro de auditoría por rol del usuario (toggle).
     * Limpia el filtro por acción y recarga la auditoría.
     *
     * @param rol Rol a filtrar: `'USUARIO'` o `'ADMIN'`.
     */
    cambiarFiltroRolAuditoria(rol: string): void {
        this.filtroRolAuditoria = this.filtroRolAuditoria === rol ? '' : rol;
        this.filtroAccion = '';
        this.cargarAuditoria();
    }

    /**
     * Elimina todos los filtros de auditoría y recarga la lista completa.
     */
    limpiarFiltrosAuditoria(): void {
        this.filtroAccion = '';
        this.filtroRolAuditoria = '';
        this.cargarAuditoria();
    }

    /**
     * Activa la pestaña indicada. Si se activa `'auditoria'` por primera vez
     * y la lista está vacía, inicia la carga de registros.
     *
     * @param tab Pestaña a activar: `'usuarios'`, `'conversiones'` o `'auditoria'`.
     */
    activarTab(tab: 'usuarios' | 'conversiones' | 'auditoria'): void {
        this.tabActiva = tab;
        if (tab === 'auditoria' && this.auditoria.length === 0 && !this.cargandoAuditoria) {
            this.cargarAuditoria();
        }
    }

    /**
     * Retorna la URL de descarga del archivo convertido de una conversión.
     *
     * @param conv Registro de conversión.
     * @returns URL de descarga, o `undefined` si no está disponible.
     */
    urlDescarga(conv: HistorialConversionDTO): string | undefined {
        return conv.rutaArchivoConvertido;
    }

    /**
     * Indica si un usuario tiene rol de administrador.
     *
     * @param u Usuario a verificar.
     * @returns `true` si el usuario es ADMIN, `false` en caso contrario.
     */
    esAdmin(u: UsuarioDTO): boolean {
        return u.rol === 'ADMIN' || u.roles?.includes('ADMIN') || false;
    }

    /**
     * Retorna el color de acento CSS asociado al tipo de archivo de una conversión.
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
     * Retorna la clase CSS de color asociada al tipo de acción de un registro
     * de auditoría.
     *
     * @param accion Tipo de acción del registro de auditoría.
     * @returns Nombre de clase CSS para colorear el badge de acción,
     *          o cadena vacía si la acción no es reconocida.
     */
    colorAccion(accion: string | undefined): string {
        switch (accion) {
            case 'LOGIN':      return 'a-login';
            case 'LOGOUT':     return 'a-logout';
            case 'CONVERSION': return 'a-conversion';
            case 'CREATE':     return 'a-create';
            case 'UPDATE':     return 'a-update';
            case 'DELETE':     return 'a-delete';
            case 'READ':       return 'a-read';
            default:           return '';
        }
    }
}