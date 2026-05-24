import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { HistorialConversionDTO } from '../models/historial-conversion-dto';

/** URL base del módulo de historial en el backend. */
const BASE = 'https://gpcueb.org/SuperConvertLJJJ/historial';

/**
 * Servicio que encapsula las llamadas HTTP al módulo de historial
 * de conversiones del usuario autenticado.
 */
@Injectable({ providedIn: 'root' })
export class HistorialService {

    /** @param http Cliente HTTP de Angular para realizar las peticiones REST. */
    constructor(private http: HttpClient) {}

    /**
     * Obtiene el historial de conversiones del usuario autenticado.
     *
     * Si el backend responde con `204 No Content` (sin conversiones),
     * el error se captura y se retorna un array vacío para que la vista
     * no muestre mensajes de error innecesarios.
     *
     * @returns Observable que emite el array de conversiones del usuario,
     *          o un array vacío si no tiene ninguna.
     */
    misConversiones(): Observable<HistorialConversionDTO[]> {
        return this.http.get<HistorialConversionDTO[]>(`${BASE}/misConversiones`).pipe(
            map(data => data ?? []),
            catchError(err => {
                if (err?.status === 204) return of([]);
                throw err;
            })
        );
    }
}