import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandlerService } from './error-handler.service';

describe('ErrorHandlerService', () => {
  let service: ErrorHandlerService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [ErrorHandlerService] });
    service = TestBed.inject(ErrorHandlerService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  // ── Status 0 (sin conexión) ───────────────────────────────────────────
  it('devuelve mensaje de sin conexión cuando status es 0', () => {
    const err = new HttpErrorResponse({ status: 0, error: null });
    const msg = service.extraerMensaje(err);
    expect(msg).toContain('No se puede conectar con el servidor');
  });

  // ── Objeto con campo "message" ────────────────────────────────────────
  it('extrae el campo message del cuerpo del error', () => {
    const err = new HttpErrorResponse({
      status: 400,
      error: { message: 'El nombre de usuario ya existe.' }
    });
    const msg = service.extraerMensaje(err);
    expect(msg).toBe('El nombre de usuario ya existe.');
  });

  // ── Error string corto ────────────────────────────────────────────────
  it('devuelve el string del cuerpo si es corto y limpio', () => {
    const err = new HttpErrorResponse({
      status: 422,
      error: 'Correo inválido'
    });
    const msg = service.extraerMensaje(err);
    expect(msg).toBe('Correo inválido');
  });

  // ── JSON en string ────────────────────────────────────────────────────
  it('parsea JSON embebido en un string de error', () => {
    const err = new HttpErrorResponse({
      status: 400,
      error: JSON.stringify({ message: 'Campo requerido' })
    });
    const msg = service.extraerMensaje(err);
    expect(msg).toBe('Campo requerido');
  });

  // ── Mensajes por código ───────────────────────────────────────────────
  it('devuelve mensaje estándar para 401', () => {
    const err = new HttpErrorResponse({ status: 401, error: {} });
    expect(service.extraerMensaje(err)).toBe('Usuario o contraseña incorrectos.');
  });

  it('devuelve mensaje estándar para 403', () => {
    const err = new HttpErrorResponse({ status: 403, error: {} });
    expect(service.extraerMensaje(err)).toBe('Usuario o contraseña incorrectos.');
  });

  it('devuelve mensaje estándar para 404', () => {
    const err = new HttpErrorResponse({ status: 404, error: {} });
    expect(service.extraerMensaje(err)).toBe('Recurso no encontrado en el servidor.');
  });

  it('devuelve mensaje estándar para 409', () => {
    const err = new HttpErrorResponse({ status: 409, error: {} });
    expect(service.extraerMensaje(err)).toBe('Ya existe un registro con esos datos.');
  });

  it('devuelve fallback para 500', () => {
    const err = new HttpErrorResponse({ status: 500, error: {} });
    const msg = service.extraerMensaje(err, 'Error grave del servidor');
    expect(msg).toBe('Error grave del servidor');
  });

  // ── Error tipo string ─────────────────────────────────────────────────
  it('devuelve el string directamente cuando el error no es HttpErrorResponse', () => {
    expect(service.extraerMensaje('Error genérico')).toBe('Error genérico');
  });

  // ── Error con propiedad message ───────────────────────────────────────
  it('devuelve err.message cuando el error tiene esa propiedad', () => {
    expect(service.extraerMensaje({ message: 'Error de JS' })).toBe('Error de JS');
  });

  // ── Fallback por defecto ──────────────────────────────────────────────
  it('devuelve el fallback cuando no hay información útil', () => {
    expect(service.extraerMensaje(null)).toBe('Ocurrió un error inesperado.');
  });

  // ── Extracción desde trace ────────────────────────────────────────────
  it('extrae mensaje del campo trace cuando no hay message', () => {
    const trace = 'java.lang.RuntimeException: El usuario no fue encontrado\n\tat com.example.Service.find(Service.java:42)';
    const err = new HttpErrorResponse({
      status: 500,
      error: { trace }
    });
    const msg = service.extraerMensaje(err);
    expect(msg).toContain('El usuario no fue encontrado');
  });

  // ── Campo error != "Internal Server Error" ────────────────────────────
  it('usa el campo error del objeto si no es "Internal Server Error"', () => {
    const err = new HttpErrorResponse({
      status: 400,
      error: { error: 'Bad Request personalizado' }
    });
    const msg = service.extraerMensaje(err);
    expect(msg).toBe('Bad Request personalizado');
  });

  it('ignora el campo error cuando es "Internal Server Error"', () => {
    const err = new HttpErrorResponse({
      status: 500,
      error: { error: 'Internal Server Error' }
    });
    const msg = service.extraerMensaje(err, 'fallback');
    expect(msg).toBe('fallback');
  });
});
