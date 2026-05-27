import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ConversionToolComponent } from './conversion-tool.component';
import { ConversionService } from '../../services/conversion.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthService } from '../../services/auth.service';

// Interfaz local para tipado del evento drag-and-drop en tests
interface DropEventMock {
  preventDefault: jasmine.Spy;
  dataTransfer: { files: File[] };
}

describe('ConversionToolComponent', () => {
  let component: ConversionToolComponent;
  let fixture: ComponentFixture<ConversionToolComponent>;
  let conversionService: jasmine.SpyObj<ConversionService>;
  let errorHandler: jasmine.SpyObj<ErrorHandlerService>;
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  const crearFile = (nombre: string, tipo: string, contenido = 'x'): File =>
    new File([contenido], nombre, { type: tipo });

  beforeEach(async () => {
    conversionService = jasmine.createSpyObj('ConversionService', ['convertir', 'formatosPorTipo']);
    errorHandler = jasmine.createSpyObj('ErrorHandlerService', ['extraerMensaje']);
    auth = jasmine.createSpyObj('AuthService', ['clearLocal']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [ConversionToolComponent],
      providers: [
        { provide: ConversionService, useValue: conversionService },
        { provide: ErrorHandlerService, useValue: errorHandler },
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { params: of({ tipo: 'audio' }) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConversionToolComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit carga el tipo desde los parámetros de ruta', () => {
    expect(component.tipo).toBe('audio');
  });

  it('ngOnInit redirige a / si el tipo no existe', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      declarations: [ConversionToolComponent],
      providers: [
        { provide: ConversionService, useValue: conversionService },
        { provide: ErrorHandlerService, useValue: errorHandler },
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { params: of({ tipo: 'desconocido' }) } }
      ]
    });
    // JS-C1002: variable 'f' renombrada a 'fixtureRedireccion'
    const fixtureRedireccion = TestBed.createComponent(ConversionToolComponent);
    fixtureRedireccion.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('config retorna la configuración de audio', () => {
    expect(component.config.titulo).toBe('Convertir Audio');
    expect(component.config.tipoBackend).toBe('AUDIO');
  });

  it('formatearTamano() formatea bytes correctamente', () => {
    expect(component.formatearTamano(500)).toBe('500 B');
    expect(component.formatearTamano(2048)).toBe('2.0 KB');
    expect(component.formatearTamano(1024 * 1024 * 3)).toBe('3.0 MB');
  });

  it('resetear() limpia todos los campos del estado', () => {
    component.error = 'Hay error';
    component.exito = true;
    component.formatoDestino = 'mp3';
    component.resetear();
    expect(component.error).toBe('');
    expect(component.exito).toBeFalse();
    expect(component.formatoDestino).toBe('');
    expect(component.archivoSeleccionado).toBeNull();
    expect(component.urlDescarga).toBeNull();
  });

  it('convertir() pone error si no hay archivo seleccionado', () => {
    component.archivoSeleccionado = null;
    component.formatoDestino = 'wav';
    component.convertir();
    expect(component.error).toBe('Selecciona un archivo primero.');
    expect(conversionService.convertir).not.toHaveBeenCalled();
  });

  it('convertir() pone error si no hay formato destino', () => {
    component.archivoSeleccionado = crearFile('pista.mp3', 'audio/mpeg');
    component.formatoDestino = '';
    component.convertir();
    expect(component.error).toBe('Selecciona un formato de destino.');
    expect(conversionService.convertir).not.toHaveBeenCalled();
  });

  it('convertir() pone error si el formato destino es igual al origen', () => {
    component.archivoSeleccionado = crearFile('pista.mp3', 'audio/mpeg');
    component.formatoDestino = 'mp3';
    component.convertir();
    expect(component.error).toContain('ya está en formato');
    expect(conversionService.convertir).not.toHaveBeenCalled();
  });

  it('convertir() llama al servicio y asigna urlDescarga en éxito', fakeAsync(() => {
    component.archivoSeleccionado = crearFile('pista.mp3', 'audio/mpeg');
    component.formatoDestino = 'wav';
    conversionService.convertir.and.returnValue(of('http://server/pista.wav'));

    component.convertir();
    tick();

    expect(component.urlDescarga).toBe('http://server/pista.wav');
    expect(component.exito).toBeTrue();
    expect(component.convirtiendo).toBeFalse();
  }));

  it('convertir() pone error cuando la URL devuelta está vacía', fakeAsync(() => {
    component.archivoSeleccionado = crearFile('pista.mp3', 'audio/mpeg');
    component.formatoDestino = 'wav';
    conversionService.convertir.and.returnValue(of('   '));

    component.convertir();
    tick();

    expect(component.error).toContain('respuesta vacía');
    expect(component.exito).toBeFalse();
  }));

  it('convertir() limpia sesión y redirige cuando el error es 401', fakeAsync(() => {
    component.archivoSeleccionado = crearFile('pista.mp3', 'audio/mpeg');
    component.formatoDestino = 'wav';
    const err = new HttpErrorResponse({ status: 401 });
    conversionService.convertir.and.returnValue(throwError(() => err));

    component.convertir();
    tick();

    expect(auth.clearLocal).toHaveBeenCalled();
    expect(component.error).toContain('sesión expiró');

    tick(1500);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('onDrop() procesa el archivo del evento drag-and-drop', () => {
    const file = crearFile('audio.mp3', 'audio/mpeg');
    // JS-0323: tipado explícito con la interfaz local en lugar de 'as any'
    const event: DropEventMock = {
      preventDefault: jasmine.createSpy(),
      dataTransfer: { files: [file] }
    };

    component.onDrop(event as unknown as DragEvent);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.archivoSeleccionado).toBe(file);
  });

  it('procesarArchivo rechaza un archivo de tipo incorrecto (imagen en audio)', () => {
    const imgFile = crearFile('foto.jpg', 'image/jpeg');
    const event: DropEventMock = {
      preventDefault: jasmine.createSpy(),
      dataTransfer: { files: [imgFile] }
    };

    component.onDrop(event as unknown as DragEvent);

    expect(component.archivoSeleccionado).toBeNull();
    expect(component.error).toContain('imagen');
  });

  it('procesarArchivo rechaza archivo vacío', () => {
    const emptyFile = new File([], 'silencio.mp3', { type: 'audio/mpeg' });
    const event: DropEventMock = {
      preventDefault: jasmine.createSpy(),
      dataTransfer: { files: [emptyFile] }
    };

    component.onDrop(event as unknown as DragEvent);

    expect(component.error).toContain('vacío');
  });
});
