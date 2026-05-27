import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistorialConversionDTO } from '../models/models';

const BASE = 'http://localhost:8080/historial';

@Injectable({ providedIn: 'root' })
export class HistorialService {

  constructor(private http: HttpClient) {}

  misConversiones(): Observable<HistorialConversionDTO[]> {
    return this.http.get<HistorialConversionDTO[]>(`${BASE}/misConversiones`);
  }
}
