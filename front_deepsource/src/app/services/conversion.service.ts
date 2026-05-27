import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

const BASE = 'http://localhost:8080/conversion';

@Injectable({ providedIn: 'root' })
export class ConversionService {

  constructor(private http: HttpClient) {}

  convertir(archivo: File, tipoArchivo: string, formatoDestino: string): Observable<string> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    formData.append('tipoArchivo', tipoArchivo);
    formData.append('formatoDestino', formatoDestino);
    return this.http.post(`${BASE}/convertir`, formData, { responseType: 'text' });
  }

  formatosPorTipo(tipoArchivo: string): Observable<string[]> {
    const params = new HttpParams().set('tipoArchivo', tipoArchivo);
    return this.http.get<string[]>(`${BASE}/formatos`, { params });
  }
}
