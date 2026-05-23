import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {

  // skipcq: JS-R1005
  extraerMensaje(err: unknown, fallback = 'Ocurrió un error inesperado.'): string {

    if (err instanceof HttpErrorResponse) {

      if (err.status === 0) {
        return 'No se puede conectar con el servidor. Verifica que el backend esté ejecutándose.';
      }

      let body = err.error;

      if (typeof body === 'string') {
        body = this.intentarParsearJSON(body);
      }

      if (body && typeof body === 'object') {
        const msg = this.extraerDeObjeto(body as Record<string, unknown>);
        if (msg) return msg;
      }

      if (typeof body === 'string' && body.trim().length > 0 && body.trim().length < 300) {
        return body.trim();
      }

      return ErrorHandlerService.mensajePorCodigo(err.status, fallback);
    }

    if (typeof err === 'string') return err;
    if (err && typeof err === 'object' && 'message' in err) return (err as { message: string }).message;
    return fallback;
  }

  private intentarParsearJSON(texto: string): unknown {
    try {
      const parsed = JSON.parse(texto);
      if (parsed && typeof parsed === 'object') return parsed;
    } catch { /* invalid JSON, return original text */ }
    return ErrorHandlerService.limpiarMensaje(texto);
  }

  // skipcq: JS-R1005
  private extraerDeObjeto(obj: Record<string, unknown>): string | null {
    if (obj['message'] && typeof obj['message'] === 'string') {
      return ErrorHandlerService.limpiarMensaje(obj['message']);
    }

    if (obj['trace'] && typeof obj['trace'] === 'string') {
      const msgDeTrace = this.extraerMensajeDeTrace(obj['trace']);
      if (msgDeTrace) return msgDeTrace;
    }

    if (obj['error'] && typeof obj['error'] === 'string' && obj['error'] !== 'Internal Server Error') {
      return obj['error'];
    }

    return null;
  }

  // skipcq: JS-0105
  private extraerMensajeDeTrace(trace: string): string | null {
    const match = trace.match(/Exception:\s*(.+?)(?:\r|\n|$)/);
    if (match?.[1]) {
      return ErrorHandlerService.limpiarMensaje(match[1]);
    }
    return null;
  }

  private static limpiarMensaje(texto: string): string {
    if (!texto) return '';

    let limpio = texto.split('\r')[0].split('\n')[0].trim();

    limpio = limpio.replace(/^[a-zA-Z]+(\.[a-zA-Z]+)+:\s*/, '').trim();

    if (limpio.length > 200) {
      const punto = limpio.indexOf('.', 50);
      if (punto > 0 && punto < 200) {
        limpio = limpio.substring(0, punto + 1);
      } else {
        limpio = `${limpio.substring(0, 200)}...`;
      }
    }

    return limpio;
  }

  // skipcq: JS-R1005
  private static mensajePorCodigo(status: number, fallback: string): string {
    const fb = ErrorHandlerService.limpiarMensaje(fallback) || fallback;
    switch (status) {
      case 400: return 'Los datos enviados no son válidos. Revisa los campos e intenta de nuevo.';
      case 401: return 'Usuario o contraseña incorrectos.';
      case 403: return 'Usuario o contraseña incorrectos.';
      case 404: return 'Recurso no encontrado en el servidor.';
      case 405: return 'Operación no permitida.';
      case 409: return 'Ya existe un registro con esos datos.';
      case 500: return fb;
      default:  return fb;
    }
  }
}
