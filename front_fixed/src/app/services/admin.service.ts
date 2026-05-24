import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { UsuarioDTO } from '../models/usuario-dto';
import { HistorialConversionDTO } from '../models/historial-conversion-dto';
import { ResumenAdmin } from '../models/resumen-admin';
import { AuditoriaDTO } from '../models/auditoria-dto';

const API  = 'https://gpcueb.org/SuperConvertLJJJ';

/**
 * Servicio que encapsula todas las llamadas HTTP del panel de
 * administración: gestión de usuarios, historial de conversiones
 * y registros de auditoría.
 *
 * Los endpoints que pueden responder con `204 No Content` manejan
 * ese caso retornando un array vacío para evitar errores en la vista.
 */
@Injectable({ providedIn: 'root' })
export class AdminService {

    /** @param http Cliente HTTP de Angular para realizar las peticiones REST. */
    constructor(private http: HttpClient) {}



    /**
     * Obtiene el resumen estadístico general del sistema.
     *
     * @returns Observable que emite un `ResumenAdmin` con totales y agrupaciones.
     */
    resumen(): Observable<ResumenAdmin> {
        return this.http.get<ResumenAdmin>(`${API}/historial/resumen`);
    }



    /**
     * Obtiene la lista completa de usuarios registrados en el sistema.
     *
     * @returns Observable que emite el array de usuarios, o vacío si no hay ninguno.
     */
    listarUsuarios(): Observable<UsuarioDTO[]> {
        return this.http.get<UsuarioDTO[]>(`${API}/usuario/listar`).pipe(
            map(data => data ?? []),
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Elimina el usuario con el ID indicado.
     *
     * @param id Identificador del usuario a eliminar.
     * @returns Observable que emite el mensaje de confirmación del servidor.
     */
    eliminarUsuario(id: number): Observable<string> {
        return this.http.delete(`${API}/usuario/eliminar`, {
            params: new HttpParams().set('id', id),
            responseType: 'text'
        });
    }

    /**
     * Edición completa de un usuario por el ADMIN.
     * Permite cambiar nombre, apellido, correo, nombreUsuario,
     * teléfono y ROL. La contraseña solo se actualiza si no está vacía.
     *
     * Endpoint: `PUT /usuario/admin/editar?id={id}`
     *
     * @param id    Identificador del usuario a editar.
     * @param datos DTO con los nuevos datos del usuario.
     * @returns Observable que emite el mensaje de confirmación del servidor.
     */
    editarUsuario(id: number, datos: UsuarioDTO): Observable<string> {
        return this.http.put(`${API}/usuario/admin/editar`, datos, {
            params: new HttpParams().set('id', id),
            responseType: 'text'
        });
    }



    /**
     * Obtiene el historial completo de conversiones de todos los usuarios.
     *
     * @returns Observable que emite el array de conversiones, o vacío si no hay ninguna.
     */
    listarConversiones(): Observable<HistorialConversionDTO[]> {
        return this.http.get<HistorialConversionDTO[]>(`${API}/historial/listar`).pipe(
            map(data => data ?? []),
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Filtra el historial de conversiones por tipo de archivo.
     *
     * @param tipo Categoría del archivo: `'AUDIO'`, `'VIDEO'` o `'IMAGEN'`.
     * @returns Observable que emite el array filtrado, o vacío si no hay resultados.
     */
    conversionesPorTipo(tipo: string): Observable<HistorialConversionDTO[]> {
        return this.http.get<HistorialConversionDTO[]>(`${API}/historial/porTipo`, {
            params: new HttpParams().set('tipoArchivo', tipo)
        }).pipe(
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Filtra el historial de conversiones por estado.
     *
     * @param estado Estado a filtrar: `'PENDIENTE'`, `'EN_PROCESO'`, `'COMPLETADO'` o `'FALLIDO'`.
     * @returns Observable que emite el array filtrado, o vacío si no hay resultados.
     */
    conversionesPorEstado(estado: string): Observable<HistorialConversionDTO[]> {
        return this.http.get<HistorialConversionDTO[]>(`${API}/historial/porEstado`, {
            params: new HttpParams().set('estado', estado)
        }).pipe(
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Obtiene el historial de conversiones de un usuario específico.
     *
     * @param usuarioId ID del usuario cuyas conversiones se desean consultar.
     * @returns Observable que emite el array de conversiones, o vacío si no hay ninguna.
     */
    conversionesPorUsuario(usuarioId: number): Observable<HistorialConversionDTO[]> {
        return this.http.get<HistorialConversionDTO[]>(`${API}/historial/porUsuario`, {
            params: new HttpParams().set('usuarioId', usuarioId)
        }).pipe(
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Elimina un registro de conversión por su ID.
     *
     * @param id Identificador del registro a eliminar.
     * @returns Observable que emite el mensaje de confirmación del servidor.
     */
    eliminarConversion(id: number): Observable<string> {
        return this.http.delete(`${API}/historial/eliminar`, {
            params: new HttpParams().set('id', id),
            responseType: 'text'
        });
    }



    /**
     * Obtiene todos los registros de auditoría del sistema.
     *
     * @returns Observable que emite el array de registros, o vacío si no hay ninguno.
     */
    listarAuditoria(): Observable<AuditoriaDTO[]> {
        return this.http.get<AuditoriaDTO[]>(`${API}/auditoria/listar`).pipe(
            map(data => data ?? []),
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Obtiene los registros de auditoría generados por un usuario específico.
     *
     * @param usuarioId ID del usuario cuyos registros se desean consultar.
     * @returns Observable que emite el array filtrado, o vacío si no hay resultados.
     */
    auditoriaPorUsuario(usuarioId: number): Observable<AuditoriaDTO[]> {
        return this.http.get<AuditoriaDTO[]>(`${API}/auditoria/porUsuario`, {
            params: new HttpParams().set('usuarioId', usuarioId)
        }).pipe(
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Filtra los registros de auditoría por tipo de acción.
     *
     * @param tipoAccion Tipo de acción a filtrar (ej. `'LOGIN'`, `'CONVERSION'`, `'DELETE'`).
     * @returns Observable que emite el array filtrado, o vacío si no hay resultados.
     */
    auditoriaPorTipoAccion(tipoAccion: string): Observable<AuditoriaDTO[]> {
        return this.http.get<AuditoriaDTO[]>(`${API}/auditoria/porTipoAccion`, {
            params: new HttpParams().set('tipoAccion', tipoAccion)
        }).pipe(
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Filtra los registros de auditoría por rol del usuario que realizó la acción.
     *
     * @param rolUsuario Rol a filtrar: `'USUARIO'` o `'ADMIN'`.
     * @returns Observable que emite el array filtrado, o vacío si no hay resultados.
     */
    auditoriaPorRol(rolUsuario: string): Observable<AuditoriaDTO[]> {
        return this.http.get<AuditoriaDTO[]>(`${API}/auditoria/porRol`, {
            params: new HttpParams().set('rolUsuario', rolUsuario)
        }).pipe(
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }

    /**
     * Retorna el conteo total de registros de auditoría en el sistema.
     *
     * @returns Observable que emite el número total de registros.
     */
    contarAuditoria(): Observable<number> {
        return this.http.get<number>(`${API}/auditoria/contar`);
    }

    /**
     * Elimina un registro de auditoría por su ID.
     *
     * @param id Identificador del registro a eliminar.
     * @returns Observable que emite el mensaje de confirmación del servidor.
     */
    eliminarRegistroAuditoria(id: number): Observable<string> {
        return this.http.delete(`${API}/auditoria/eliminar`, {
            params: new HttpParams().set('id', id),
            responseType: 'text'
        });
    }
}