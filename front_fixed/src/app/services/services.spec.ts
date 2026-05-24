import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ConversionService } from './conversion.service';
import { AdminService } from './admin.service';
import { HistorialService } from './historial.service';

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

        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/conversion/convertir');
        expect(req.request.method).toBe('POST');
        const body = req.request.body as FormData;
        expect(body.get('tipoArchivo')).toBe('AUDIO');
        expect(body.get('formatoDestino')).toBe('wav');
        expect(body.get('archivo')).toBeTruthy();
        req.flush('http://gpcueb.org/SuperConvertLJJJ/descargas/audio.wav');
    });

    it('formatosPorTipo() hace GET a /conversion/formatos con el parámetro tipoArchivo', () => {
        service.formatosPorTipo('VIDEO').subscribe();

        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/conversion/formatos' &&
            r.params.get('tipoArchivo') === 'VIDEO'
        );
        expect(req.request.method).toBe('GET');
        req.flush(['mp4', 'mkv', 'avi']);
    });

    it('convertir() retorna la URL de descarga como string', () => {
        const file = new File(['x'], 'img.png', { type: 'image/png' });
        let url = '';
        service.convertir(file, 'IMAGEN', 'webp').subscribe((u: any) => (url = u));

        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/conversion/convertir');
        req.flush('http://server/img.webp');
        expect(url).toBe('http://server/img.webp');
    });
});

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
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/historial/resumen');
        expect(req.request.method).toBe('GET');
        req.flush({ totalUsuarios: 5, totalConversiones: 20 });
    });

    it('listarUsuarios() hace GET a /usuario/listar', () => {
        service.listarUsuarios().subscribe();
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/usuario/listar');
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('listarUsuarios() retorna array vacío cuando el backend responde 204', () => {
        let resultado: any;
        service.listarUsuarios().subscribe(r => (resultado = r));
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/usuario/listar');
        req.flush(null, { status: 204, statusText: 'No Content' });
        expect(resultado).toEqual([]);
    });

    it('eliminarUsuario() hace DELETE con parámetro id', () => {
        service.eliminarUsuario(42).subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/usuario/eliminar' &&
            r.params.get('id') === '42'
        );
        expect(req.request.method).toBe('DELETE');
        req.flush('Usuario eliminado');
    });

    it('editarUsuario() hace PUT a /usuario/admin/editar con parámetro id', () => {
        const datos: any = { nombre: 'Ana', apellido: 'García', correo: 'ana@mail.com', nombreUsuario: 'ana', telefono: '3001111111' };
        service.editarUsuario(5, datos).subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/usuario/admin/editar' &&
            r.params.get('id') === '5'
        );
        expect(req.request.method).toBe('PUT');
        req.flush('ok');
    });

    it('listarConversiones() hace GET a /historial/listar', () => {
        service.listarConversiones().subscribe();
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/historial/listar');
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('listarConversiones() retorna array vacío cuando el backend responde 204', () => {
        let resultado: any;
        service.listarConversiones().subscribe(r => (resultado = r));
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/historial/listar');
        req.flush(null, { status: 204, statusText: 'No Content' });
        expect(resultado).toEqual([]);
    });

    it('conversionesPorTipo() hace GET con parámetro tipoArchivo', () => {
        service.conversionesPorTipo('AUDIO').subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/historial/porTipo' &&
            r.params.get('tipoArchivo') === 'AUDIO'
        );
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('conversionesPorEstado() hace GET con parámetro estado', () => {
        service.conversionesPorEstado('COMPLETADO').subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/historial/porEstado' &&
            r.params.get('estado') === 'COMPLETADO'
        );
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('conversionesPorUsuario() hace GET con parámetro usuarioId', () => {
        service.conversionesPorUsuario(7).subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/historial/porUsuario' &&
            r.params.get('usuarioId') === '7'
        );
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('eliminarConversion() hace DELETE con parámetro id', () => {
        service.eliminarConversion(7).subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/historial/eliminar' &&
            r.params.get('id') === '7'
        );
        expect(req.request.method).toBe('DELETE');
        req.flush('ok');
    });

    it('eliminarHistorialPorUsuario() (alias eliminarConversion por usuario) hace DELETE correcto', () => {
        service.eliminarConversion(3).subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/historial/eliminar' &&
            r.params.get('id') === '3'
        );
        expect(req.request.method).toBe('DELETE');
        req.flush('Historial eliminado');
    });



    it('listarAuditoria() hace GET a /auditoria/listar', () => {
        service.listarAuditoria().subscribe();
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/auditoria/listar');
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('listarAuditoria() retorna array vacío cuando el backend responde 204', () => {
        let resultado: any;
        service.listarAuditoria().subscribe(r => (resultado = r));
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/auditoria/listar');
        req.flush(null, { status: 204, statusText: 'No Content' });
        expect(resultado).toEqual([]);
    });

    it('auditoriaPorUsuario() hace GET con parámetro usuarioId', () => {
        service.auditoriaPorUsuario(2).subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/auditoria/porUsuario' &&
            r.params.get('usuarioId') === '2'
        );
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('auditoriaPorTipoAccion() hace GET con parámetro tipoAccion', () => {
        service.auditoriaPorTipoAccion('LOGIN').subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/auditoria/porTipoAccion' &&
            r.params.get('tipoAccion') === 'LOGIN'
        );
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('auditoriaPorRol() hace GET con parámetro rolUsuario', () => {
        service.auditoriaPorRol('ADMIN').subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/auditoria/porRol' &&
            r.params.get('rolUsuario') === 'ADMIN'
        );
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('contarAuditoria() hace GET a /auditoria/contar', () => {
        service.contarAuditoria().subscribe();
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/auditoria/contar');
        expect(req.request.method).toBe('GET');
        req.flush(42);
    });

    it('eliminarRegistroAuditoria() hace DELETE con parámetro id', () => {
        service.eliminarRegistroAuditoria(9).subscribe();
        const req = httpMock.expectOne(r =>
            r.url === 'http://gpcueb.org/SuperConvertLJJJ/auditoria/eliminar' &&
            r.params.get('id') === '9'
        );
        expect(req.request.method).toBe('DELETE');
        req.flush('ok');
    });
});

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
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/historial/misConversiones');
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
        let resultado: any;
        service.misConversiones().subscribe((d: any) => (resultado = d));

        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/historial/misConversiones');
        req.flush(mockData);
        expect(resultado.length).toBe(1);
        expect(resultado[0].formatoDestino).toBe('MKV');
    });

    it('misConversiones() retorna array vacío cuando el backend responde 204', () => {
        let resultado: any;
        service.misConversiones().subscribe(r => (resultado = r));
        const req = httpMock.expectOne('http://gpcueb.org/SuperConvertLJJJ/historial/misConversiones');
        req.flush(null, { status: 204, statusText: 'No Content' });
        expect(resultado).toEqual([]);
    });
});