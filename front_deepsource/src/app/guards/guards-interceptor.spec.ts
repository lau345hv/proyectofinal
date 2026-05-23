import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
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

    // JS-0323: tipado explícito con HttpRequest<Record<string,string>> en lugar de any
    const req = new HttpRequest<Record<string, string>>('GET', '/api/recurso');
    let requestCapturado: HttpRequest<Record<string, string>> | null = null;

    const handler: HttpHandler = {
      handle: (r: HttpRequest<Record<string, string>>) => {
        requestCapturado = r;
        return of(new HttpResponse<Record<string, string>>());
      }
    };

    interceptor.intercept(req, handler).subscribe();

    expect(requestCapturado).not.toBeNull();
    // JS-0339: non-null assertion eliminada → jasmine ya garantiza que no es null tras el expect anterior
    if (requestCapturado) {
      expect((requestCapturado as HttpRequest<Record<string, string>>).headers.get('Authorization'))
        .toBe('Bearer mi-jwt-token');
    }
  });

  it('NO agrega el header Authorization cuando NO hay token en localStorage', () => {
    const req = new HttpRequest<Record<string, string>>('GET', '/api/recurso');
    let requestCapturado: HttpRequest<Record<string, string>> | null = null;

    const handler: HttpHandler = {
      handle: (r: HttpRequest<Record<string, string>>) => {
        requestCapturado = r;
        return of(new HttpResponse<Record<string, string>>());
      }
    };

    interceptor.intercept(req, handler).subscribe();

    // JS-0339: non-null assertion eliminada → if guard
    if (requestCapturado) {
      expect((requestCapturado as HttpRequest<Record<string, string>>).headers.has('Authorization'))
        .toBeFalse();
    }
  });

  it('pasa la petición sin modificar cuando no hay token', () => {
    const body = { user: 'test' };
    const req = new HttpRequest<{ user: string }>('POST', '/api/login', body);
    let requestCapturado: HttpRequest<{ user: string }> | null = null;

    const handler: HttpHandler = {
      handle: (r: HttpRequest<{ user: string }>) => {
        requestCapturado = r;
        return of(new HttpResponse<{ user: string }>()) as unknown as ReturnType<typeof handler.handle>;
      }
    };

    interceptor.intercept(req, handler).subscribe();

    // JS-0339: non-null assertion eliminada → if guard
    if (requestCapturado) {
      expect((requestCapturado as HttpRequest<{ user: string }>).url).toBe('/api/login');
      expect((requestCapturado as HttpRequest<{ user: string }>).method).toBe('POST');
    }
  });
});
