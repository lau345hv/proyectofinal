import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UsuarioDTO, HistorialConversionDTO, ResumenAdmin } from '../models/models';

const BASE = 'http://localhost:8080/admin';

@Injectable({ providedIn: 'root' })
export class AdminService {

  constructor(private http: HttpClient) {}

  resumen(): Observable<string> {
    return this.http.get(`${BASE}/resumen`, {
      responseType: 'text',
      withCredentials: true
    });
  }

  listarUsuarios(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(`${BASE}/listarUsuarios`, {
      withCredentials: true
    });
  }

  eliminarUsuario(id: number): Observable<string> {
    return this.http.delete(`${BASE}/eliminarUsuario`, {
      params: new HttpParams().set('id', id),
      responseType: 'text',
      withCredentials: true
    });
  }

  listarConversiones(): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${BASE}/listarConversiones`, {
      withCredentials: true
    });
  }

  conversionesPorUsuario(usuarioId: number): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${BASE}/conversionesPorUsuario`, {
      params: new HttpParams().set('usuarioId', usuarioId),
      withCredentials: true
    });
  }

  conversionesPorTipo(tipoArchivo: string): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${BASE}/conversionesPorTipo`, {
      params: new HttpParams().set('tipoArchivo', tipoArchivo),
      withCredentials: true
    });
  }

  conversionesPorEstado(estado: string): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${BASE}/conversionesPorEstado`, {
      params: new HttpParams().set('estado', estado),
      withCredentials: true
    });
  }
}
