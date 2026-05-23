import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    localStorage.clear();
    // Simula matchMedia
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {},
        removeListener: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => false
      })
    });

    TestBed.configureTestingModule({ providers: [ThemeService] });
    service = TestBed.inject(ThemeService);
  });

  afterEach(() => {
    localStorage.clear();
    document.body.classList.remove('dark-mode');
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  // ── Estado inicial ────────────────────────────────────────────────────
  it('inicia en modo claro cuando no hay preferencia guardada y matchMedia es false', () => {
    expect(service.isDark()).toBeFalse();
  });

  it('inicia en modo oscuro cuando localStorage tiene "dark"', () => {
    localStorage.setItem('superconvert-theme', 'dark');
    // Recrea el servicio
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [ThemeService] });
    const s = TestBed.inject(ThemeService);
    expect(s.isDark()).toBeTrue();
  });

  it('inicia en modo claro cuando localStorage tiene "light"', () => {
    localStorage.setItem('superconvert-theme', 'light');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [ThemeService] });
    const s = TestBed.inject(ThemeService);
    expect(s.isDark()).toBeFalse();
  });

  // ── toggle ────────────────────────────────────────────────────────────
  it('toggle() cambia de claro a oscuro', () => {
    expect(service.isDark()).toBeFalse();
    service.toggle();
    expect(service.isDark()).toBeTrue();
  });

  it('toggle() cambia de oscuro a claro', () => {
    service.toggle(); // → oscuro
    service.toggle(); // → claro
    expect(service.isDark()).toBeFalse();
  });

  it('toggle() guarda la preferencia en localStorage', () => {
    service.toggle();
    expect(localStorage.getItem('superconvert-theme')).toBe('dark');
    service.toggle();
    expect(localStorage.getItem('superconvert-theme')).toBe('light');
  });

  // ── aplicarTema (DOM) ─────────────────────────────────────────────────
  it('toggle() agrega la clase dark-mode al body', () => {
    service.toggle();
    expect(document.body.classList.contains('dark-mode')).toBeTrue();
  });

  it('toggle() quita la clase dark-mode del body al volver al modo claro', () => {
    service.toggle(); // oscuro
    service.toggle(); // claro
    expect(document.body.classList.contains('dark-mode')).toBeFalse();
  });
});
