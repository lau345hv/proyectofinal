import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

/** URL base del módulo de conversión en el backend. */
const BASE = 'https://gpcueb.org/SuperConvertLJJJ/conversion';

/**
 * Servicio que encapsula las llamadas HTTP al módulo de conversión
 * de archivos del backend.
 */
@Injectable({ providedIn: 'root' })
export class ConversionService {

    /** @param http Cliente HTTP de Angular para realizar las peticiones REST. */
    constructor(private http: HttpClient) {}

    /**
     * Envía un archivo al backend para convertirlo al formato indicado.
     *
     * @param archivo         Archivo que el usuario desea convertir.
     * @param tipoArchivo     Categoría del archivo en el backend: `'AUDIO'`, `'VIDEO'` o `'IMAGEN'`.
     * @param formatoDestino  Extensión del formato de salida (ej. `'mp3'`, `'mp4'`, `'png'`).
     * @returns Observable que emite la URL de descarga del archivo convertido.
     */
    convertir(archivo: File, tipoArchivo: string, formatoDestino: string): Observable<string> {
        const formData = new FormData();
        formData.append('archivo', archivo);
        formData.append('tipoArchivo', tipoArchivo);
        formData.append('formatoDestino', formatoDestino);
        return this.http.post(`${BASE}/convertir`, formData, { responseType: 'text' });
    }

    /**
     * Obtiene la lista de formatos de salida disponibles para un tipo de archivo.
     *
     * @param tipoArchivo Categoría del archivo: `'AUDIO'`, `'VIDEO'` o `'IMAGEN'`.
     * @returns Observable que emite un array de extensiones soportadas (ej. `['mp3','wav','flac']`).
     */
    formatosPorTipo(tipoArchivo: string): Observable<string[]> {
        const params = new HttpParams().set('tipoArchivo', tipoArchivo);
        return this.http.get<string[]>(`${BASE}/formatos`, { params });
    }
}