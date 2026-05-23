import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UsuarioDTO, HistorialConversionDTO, ResumenAdmin } from '../models/models';

const API = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class AdminService {

  constructor(private http: HttpClient) {}

  resumen(): Observable<ResumenAdmin> {
    return this.http.get<ResumenAdmin>(`${API}/historial/resumen`);
  }

  listarUsuarios(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(`${API}/usuario/listar`);
  }

  eliminarUsuario(id: number): Observable<string> {
    return this.http.delete(`${API}/usuario/eliminar`, {
      params: new HttpParams().set('id', id),
      responseType: 'text'
    });
  }

  listarConversiones(): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${API}/historial/listar`);
  }

  conversionesPorTipo(tipo: string): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${API}/historial/porTipo`, {
      params: new HttpParams().set('tipoArchivo', tipo)
    });
  }

  conversionesPorEstado(estado: string): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${API}/historial/porEstado`, {
      params: new HttpParams().set('estado', estado)
    });
  }

  eliminarConversion(id: number): Observable<string> {
    return this.http.delete(`${API}/historial/eliminar`, {
      params: new HttpParams().set('id', id),
      responseType: 'text'
    });
  }

  eliminarHistorialPorUsuario(usuarioId: number): Observable<string> {
    return this.http.delete(`${API}/historial/eliminarPorUsuario`, {
      params: new HttpParams().set('usuarioId', usuarioId),
      responseType: 'text'
    });
  }
}
