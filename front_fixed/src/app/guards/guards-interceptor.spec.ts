import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { of } from 'rxjs';
import { AuthGuard } from './auth.guard';
import { AdminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';
import { JwtInterceptor } from '../interceptors/jwt.interceptor';

// ═══════════════════════════════════════════════════════════════════════════
// AuthGuard
// ═══════════════════════════════════════════════════════════════════════════
describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    });
    guard = TestBed.inject(AuthGuard);
  });

  it('debería crearse correctamente', () => {
    expect(guard).toBeTruthy();
  });

  it('canActivate() retorna true cuando el usuario está autenticado', () => {
    authService.isLoggedIn.and.returnValue(true);
    expect(guard.canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('canActivate() retorna false y redirige a /login cuando no está autenticado', () => {
    authService.isLoggedIn.and.returnValue(false);
    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});

// ═══════════════════════════════════════════════════════════════════════════
// AdminGuard
// ═══════════════════════════════════════════════════════════════════════════
describe('AdminGuard', () => {
  let guard: AdminGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['isAdmin']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AdminGuard,
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    });
    guard = TestBed.inject(AdminGuard);
  });

  it('debería crearse correctamente', () => {
    expect(guard).toBeTruthy();
  });

  it('canActivate() retorna true cuando el usuario es admin', () => {
    authService.isAdmin.and.returnValue(true);
    expect(guard.canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('canActivate() retorna false y redirige a / cuando no es admin', () => {
    authService.isAdmin.and.returnValue(false);
    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});

// ═══════════════════════════════════════════════════════════════════════════
// JwtInterceptor
// ═══════════════════════════════════════════════════════════════════════════
describe('JwtInterceptor', () => {
  let interceptor: JwtInterceptor;

  beforeEach(() => {
    localStorage.clear();
    interceptor = new JwtInterceptor();
  });

  afterEach(() => localStorage.clear());

  it('debería crearse correctamente', () => {
    expect(interceptor).toBeTruthy();
  });

  it('agrega el header Authorization cuando hay token en localStorage', () => {
    localStorage.setItem('jwt_token', 'mi-jwt-token');

    const req = new HttpRequest('GET', '/api/recurso');
    let requestCapturado: HttpRequest<unknown> | null = null;

    const handler: HttpHandler = {
      handle: (r: HttpRequest<unknown>) => {
        requestCapturado = r;
        return of({} as HttpEvent<unknown>);
      }
    };

    interceptor.intercept(req, handler).subscribe();

    expect(requestCapturado).not.toBeNull();
    if (requestCapturado) {
      expect(requestCapturado.headers.get('Authorization')).toBe('Bearer mi-jwt-token');
    }
  });

  it('NO agrega el header Authorization cuando NO hay token en localStorage', () => {
    const req = new HttpRequest('GET', '/api/recurso');
    let requestCapturado: HttpRequest<unknown> | null = null;

    const handler: HttpHandler = {
      handle: (r: HttpRequest<unknown>) => {
        requestCapturado = r;
        return of({} as HttpEvent<unknown>);
      }
    };

    interceptor.intercept(req, handler).subscribe();

    if (requestCapturado) {
      expect(requestCapturado.headers.has('Authorization')).toBeFalse();
    }
  });

  it('pasa la petición sin modificar cuando no hay token', () => {
    const req = new HttpRequest('POST', '/api/login', { user: 'test' });
    let requestCapturado: HttpRequest<unknown> | null = null;

    const handler: HttpHandler = {
      handle: (r: HttpRequest<unknown>) => {
        requestCapturado = r;
        return of({} as HttpEvent<unknown>);
      }
    };

    interceptor.intercept(req, handler).subscribe();

    if (requestCapturado) {
      expect(requestCapturado.url).toBe('/api/login');
      expect(requestCapturado.method).toBe('POST');
    }
  });
});
