import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * Servicio centralizado para extraer mensajes de error legibles a partir
 * de respuestas HTTP fallidas u otras excepciones de JavaScript.
 *
 * Maneja respuestas con body en formato objeto JSON, string plano o JSON
 * embebido en string. Para errores de red (status 0) y códigos HTTP
 * conocidos (400, 401, 403, 404, 409, 500) devuelve mensajes en español
 * predefinidos.
 */
@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {

    /**
     * Extrae el mensaje de error más descriptivo disponible.
     *
     * @param err      Error capturado; puede ser un `HttpErrorResponse`,
     *                 un string, un objeto con `.message`, o `null`.
     * @param fallback Mensaje a mostrar cuando no se puede extraer uno
     *                 del error. Por defecto `'Ocurrió un error inesperado.'`
     * @returns Cadena de texto con el mensaje de error listo para mostrar al usuario.
     */
    extraerMensaje(err: any, fallback: string = 'Ocurrió un error inesperado.'): string {

        if (err instanceof HttpErrorResponse) {

            if (err.status === 0) {
                return 'No se puede conectar con el servidor. Verifica que el backend esté ejecutándose.';
            }

            let body = err.error;

            if (typeof body === 'string') {
                body = this.intentarParsearJSON(body);
            }

            if (body && typeof body === 'object') {
                const msg = this.extraerDeObjeto(body);
                if (msg) return msg;
            }

            if (typeof body === 'string' && body.trim().length > 0 && body.trim().length < 300) {
                return body.trim();
            }

            return this.mensajePorCodigo(err.status, fallback);
        }

        if (typeof err === 'string') return err;
        if (err?.message) return err.message;
        return fallback;
    }

    /**
     * Intenta parsear un string como JSON. Si el resultado es un objeto,
     * lo retorna; en caso contrario retorna el string original.
     *
     * @param texto String que puede contener JSON embebido.
     * @returns Objeto parseado o el string original si no era JSON válido.
     */
    private intentarParsearJSON(texto: string): any {
        try {
            const parsed = JSON.parse(texto);
            if (parsed && typeof parsed === 'object') return parsed;
        } catch {}
        return texto;
    }

    /**
     * Extrae el mensaje de error desde un objeto con posibles campos
     * `message`, `trace` o `error`.
     *
     * @param obj Objeto del cuerpo de la respuesta HTTP de error.
     * @returns Mensaje extraído, o `null` si no se encontró ninguno útil.
     */
    private extraerDeObjeto(obj: any): string | null {
        if (obj.message && typeof obj.message === 'string') {
            return this.limpiarMensaje(obj.message);
        }

        if (obj.trace && typeof obj.trace === 'string') {
            const msgDeTrace = this.extraerMensajeDeTrace(obj.trace);
            if (msgDeTrace) return msgDeTrace;
        }

        if (obj.error && typeof obj.error === 'string' && obj.error !== 'Internal Server Error') {
            return obj.error;
        }

        return null;
    }

    /**
     * Extrae el mensaje de la primera línea del stack trace de una excepción
     * Java (patrón `Exception: mensaje`).
     *
     * @param trace Stack trace como string.
     * @returns Mensaje limpio extraído del trace, o `null` si no coincide el patrón.
     */
    private extraerMensajeDeTrace(trace: string): string | null {
        const match = trace.match(/Exception:\s*(.+?)(?:\r|\n|$)/);
        if (match && match[1]) {
            return this.limpiarMensaje(match[1]);
        }
        return null;
    }

    /**
     * Limpia y recorta un mensaje de error eliminando prefijos de clase Java,
     * saltos de línea y exceso de longitud.
     *
     * @param texto Mensaje de error crudo.
     * @returns Mensaje limpio de hasta 200 caracteres.
     */
    private limpiarMensaje(texto: string): string {
        if (!texto) return '';

        let limpio = texto.split('\r')[0].split('\n')[0].trim();

        limpio = limpio.replace(/^[a-zA-Z]+(\.[a-zA-Z]+)+:\s*/, '').trim();

        if (limpio.length > 200) {
            const punto = limpio.indexOf('.', 50);
            if (punto > 0 && punto < 200) {
                limpio = limpio.substring(0, punto + 1);
            } else {
                limpio = limpio.substring(0, 200) + '...';
            }
        }

        return limpio;
    }

    /**
     * Retorna un mensaje predefinido en español para los códigos HTTP más comunes.
     *
     * @param status   Código de estado HTTP.
     * @param fallback Mensaje de fallback si el código no está mapeado.
     * @returns Mensaje legible para el usuario.
     */
    private mensajePorCodigo(status: number, fallback: string): string {
        switch (status) {
            case 400: return 'Los datos enviados no son válidos. Revisa los campos e intenta de nuevo.';
            case 401: return 'Usuario o contraseña incorrectos.';
            case 403: return 'Usuario o contraseña incorrectos.';
            case 404: return 'Recurso no encontrado en el servidor.';
            case 405: return 'Operación no permitida.';
            case 409: return 'Ya existe un registro con esos datos.';
            case 500: return fallback;
            default:  return fallback;
        }
    }
}