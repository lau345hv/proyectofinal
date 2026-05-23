import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { AuthService, LoginResponse } from './auth.service';
import { UsuarioDTO } from '../models/models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const authResponse = {
    token: 'test-token-123',
    rol: 'USER',
    id: 1,
    nombreUsuario: 'juanito'
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // ── Creación ─────────────────────────────────────────────────────────
  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  // ── isLoggedIn ────────────────────────────────────────────────────────
  it('isLoggedIn() devuelve false cuando no hay token', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('isLoggedIn() devuelve true cuando hay token y usuario en localStorage', () => {
    localStorage.setItem('jwt_token', 'tok');
    localStorage.setItem('usuario', JSON.stringify({ id: 1, nombreUsuario: 'u', nombre: '', apellido: '', correo: '', telefono: '' }));
    // Recrea el servicio para que cargue el usuario guardado
    const serviceInstance = new (service.constructor as unknown as new (h: HttpClient) => AuthService)(TestBed.inject(HttpClient));
    expect(serviceInstance.isLoggedIn()).toBeTrue();
  });

  // ── isAdmin ───────────────────────────────────────────────────────────
  it('isAdmin() devuelve false cuando no hay usuario', () => {
    expect(service.isAdmin()).toBeFalse();
  });

  // ── getCurrentUser ────────────────────────────────────────────────────
  it('getCurrentUser() devuelve null cuando no hay sesión', () => {
    expect(service.getCurrentUser()).toBeNull();
  });

  // ── getAuthToken ──────────────────────────────────────────────────────
  it('getAuthToken() devuelve null cuando no hay token', () => {
    expect(service.getAuthToken()).toBeNull();
  });

  it('getAuthToken() devuelve el token guardado en localStorage', () => {
    localStorage.setItem('jwt_token', 'mi-token');
    expect(service.getAuthToken()).toBe('mi-token');
  });

  // ── clearLocal ────────────────────────────────────────────────────────
  it('clearLocal() limpia el localStorage y el usuario en el subject', () => {
    localStorage.setItem('jwt_token', 'tok');
    localStorage.setItem('usuario', JSON.stringify({ id: 1 }));
    service.clearLocal();
    expect(localStorage.getItem('jwt_token')).toBeNull();
    expect(localStorage.getItem('usuario')).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
  });

  // ── logout ────────────────────────────────────────────────────────────
  it('logout() elimina el token y emite null en currentUser$', fakeAsync(() => {
    localStorage.setItem('jwt_token', 'tok');
    localStorage.setItem('usuario', JSON.stringify({ id: 1, nombreUsuario: 'u', nombre: '', apellido: '', correo: '', telefono: '' }));
    let emittedUser: UsuarioDTO | null | string = 'initial';
    service.currentUser$.subscribe((u: UsuarioDTO | null) => { emittedUser = u; });

    service.logout().subscribe();
    tick();

    expect(localStorage.getItem('jwt_token')).toBeNull();
    expect(emittedUser).toBeNull();
  }));

  // ── login ─────────────────────────────────────────────────────────────
  it('login() guarda el token y retorna LoginResponse correctamente', fakeAsync(() => {
    let result: LoginResponse | undefined;
    service.login('juanito', 'pass1234').subscribe((r: LoginResponse) => { result = r; });

    const req = httpMock.expectOne('http://localhost:8080/autenticacion/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nombreUsuario: 'juanito', contrasena: 'pass1234' });
    req.flush(authResponse);

    // El servicio pide miPerfil automáticamente tras login
    const perfilReq = httpMock.expectOne('http://localhost:8080/usuario/miPerfil');
    perfilReq.flush({ id: 1, nombre: 'Juan', apellido: 'Pérez', correo: 'juan@mail.com', nombreUsuario: 'juanito', telefono: '3000000000' });

    tick();

    expect(localStorage.getItem('jwt_token')).toBe('test-token-123');
    expect(result).toBeTruthy();
    expect(result?.usuario.nombreUsuario).toBe('juanito');
  }));

  // ── registrar ─────────────────────────────────────────────────────────
  it('registrar() llama al endpoint correcto y guarda el token', fakeAsync(() => {
    const datos = {
      nombre: 'Juan', apellido: 'Pérez', correo: 'juan@mail.com',
      nombreUsuario: 'juanito', contrasena: 'pass1234',
      telefono: '3000000000', codigoVerificacion: '1234'
    };
    let result: LoginResponse | undefined;
    service.registrar(datos).subscribe((r: LoginResponse) => { result = r; });

    const req = httpMock.expectOne(r => r.url === 'http://localhost:8080/autenticacion/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('codigoVerificacion')).toBe('1234');
    req.flush(authResponse);

    const perfilReq = httpMock.expectOne('http://localhost:8080/usuario/miPerfil');
    perfilReq.flush({ id: 1, nombre: 'Juan', apellido: 'Pérez', correo: 'juan@mail.com', nombreUsuario: 'juanito', telefono: '3000000000' });

    tick();

    expect(localStorage.getItem('jwt_token')).toBe('test-token-123');
    expect(result?.mensaje).toBe('ok');
  }));

  // ── solicitarCodigo ───────────────────────────────────────────────────
  it('solicitarCodigo() realiza POST con el parámetro correo', () => {
    service.solicitarCodigo('test@mail.com').subscribe();

    const req = httpMock.expectOne(r =>
      r.url === 'http://localhost:8080/autenticacion/solicitar-codigo' &&
      r.params.get('correo') === 'test@mail.com'
    );
    expect(req.request.method).toBe('POST');
    req.flush('ok');
  });

  // ── miPerfil ──────────────────────────────────────────────────────────
  it('miPerfil() hace GET a /usuario/miPerfil', () => {
    service.miPerfil().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/usuario/miPerfil');
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, nombre: 'Juan', apellido: 'Pérez', correo: 'j@m.com', nombreUsuario: 'j', telefono: '300' });
  });
});
