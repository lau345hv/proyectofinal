import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ConversionService } from './conversion.service';
import { AdminService } from './admin.service';
import { HistorialService } from './historial.service';
import { HistorialConversionDTO } from '../models/models';

// ═══════════════════════════════════════════════════════════════════════════
// ConversionService
// ═══════════════════════════════════════════════════════════════════════════
describe('ConversionService', () => {
  let service: ConversionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ConversionService]
    });
    service = TestBed.inject(ConversionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('convertir() hace POST a /conversion/convertir con FormData', () => {
    const file = new File(['contenido'], 'audio.mp3', { type: 'audio/mpeg' });
    service.convertir(file, 'AUDIO', 'wav').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/conversion/convertir');
    expect(req.request.method).toBe('POST');
    const body = req.request.body as FormData;
    expect(body.get('tipoArchivo')).toBe('AUDIO');
    expect(body.get('formatoDestino')).toBe('wav');
    expect(body.get('archivo')).toBeTruthy();
    req.flush('http://localhost:8080/descargas/audio.wav');
  });

  it('formatosPorTipo() hace GET a /conversion/formatos con el parámetro tipoArchivo', () => {
    service.formatosPorTipo('VIDEO').subscribe();

    const req = httpMock.expectOne(r =>
      r.url === 'http://localhost:8080/conversion/formatos' &&
      r.params.get('tipoArchivo') === 'VIDEO'
    );
    expect(req.request.method).toBe('GET');
    req.flush(['mp4', 'mkv', 'avi']);
  });

  it('convertir() retorna la URL de descarga como string', () => {
    const file = new File(['x'], 'img.png', { type: 'image/png' });
    let url = '';
    service.convertir(file, 'IMAGEN', 'webp').subscribe((u: string) => { url = u; });

    const req = httpMock.expectOne('http://localhost:8080/conversion/convertir');
    req.flush('http://server/img.webp');
    expect(url).toBe('http://server/img.webp');
  });
});

// ═══════════════════════════════════════════════════════════════════════════
// AdminService
// ═══════════════════════════════════════════════════════════════════════════
describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('resumen() hace GET a /historial/resumen', () => {
    service.resumen().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/historial/resumen');
    expect(req.request.method).toBe('GET');
    req.flush({ totalUsuarios: 5, totalConversiones: 20 });
  });

  it('listarUsuarios() hace GET a /usuario/listar', () => {
    service.listarUsuarios().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/usuario/listar');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('eliminarUsuario() hace DELETE con parámetro id', () => {
    service.eliminarUsuario(42).subscribe();
    const req = httpMock.expectOne(r =>
      r.url === 'http://localhost:8080/usuario/eliminar' &&
      r.params.get('id') === '42'
    );
    expect(req.request.method).toBe('DELETE');
    req.flush('Usuario eliminado');
  });

  it('listarConversiones() hace GET a /historial/listar', () => {
    service.listarConversiones().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/historial/listar');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('conversionesPorTipo() hace GET con parámetro tipoArchivo', () => {
    service.conversionesPorTipo('AUDIO').subscribe();
    const req = httpMock.expectOne(r =>
      r.url === 'http://localhost:8080/historial/porTipo' &&
      r.params.get('tipoArchivo') === 'AUDIO'
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('conversionesPorEstado() hace GET con parámetro estado', () => {
    service.conversionesPorEstado('COMPLETADO').subscribe();
    const req = httpMock.expectOne(r =>
      r.url === 'http://localhost:8080/historial/porEstado' &&
      r.params.get('estado') === 'COMPLETADO'
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('eliminarConversion() hace DELETE con parámetro id', () => {
    service.eliminarConversion(7).subscribe();
    const req = httpMock.expectOne(r =>
      r.url === 'http://localhost:8080/historial/eliminar' &&
      r.params.get('id') === '7'
    );
    expect(req.request.method).toBe('DELETE');
    req.flush('ok');
  });

  it('eliminarHistorialPorUsuario() hace DELETE con parámetro usuarioId', () => {
    service.eliminarHistorialPorUsuario(3).subscribe();
    const req = httpMock.expectOne(r =>
      r.url === 'http://localhost:8080/historial/eliminarPorUsuario' &&
      r.params.get('usuarioId') === '3'
    );
    expect(req.request.method).toBe('DELETE');
    req.flush('Historial eliminado');
  });
});

// ═══════════════════════════════════════════════════════════════════════════
// HistorialService
// ═══════════════════════════════════════════════════════════════════════════
describe('HistorialService', () => {
  let service: HistorialService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HistorialService]
    });
    service = TestBed.inject(HistorialService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('misConversiones() hace GET a /historial/misConversiones', () => {
    service.misConversiones().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/historial/misConversiones');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('misConversiones() retorna el listado de conversiones', () => {
    const mockData = [
      {
        id: 1, nombreArchivoOriginal: 'video.mp4',
        formatoOrigen: 'MP4', formatoDestino: 'MKV',
        tipoArchivo: 'VIDEO' as const, estado: 'COMPLETADO' as const,
        urlDescarga: 'http://server/video.mkv', fechaConversion: '2024-01-01'
      }
    ];
    let resultado: HistorialConversionDTO[] | undefined;
    service.misConversiones().subscribe((d: HistorialConversionDTO[]) => { resultado = d; });

    const req = httpMock.expectOne('http://localhost:8080/historial/misConversiones');
    req.flush(mockData);
    expect(resultado?.length).toBe(1);
    expect(resultado?.[0].formatoDestino).toBe('MKV');
  });
});
