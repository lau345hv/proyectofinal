import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError, Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { HistorialComponent } from './historial/historial.component';
import { NavbarComponent } from './navbar/navbar.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { HomeComponent } from './home/home.component';

import { AuthService } from '../services/auth.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { HistorialService } from '../services/historial.service';
import { ThemeService } from '../services/theme.service';
import { AdminService } from '../services/admin.service';
import { ActivatedRoute } from '@angular/router';


describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let auth: jasmine.SpyObj<AuthService>;
    let errorHandler: jasmine.SpyObj<ErrorHandlerService>;
    let router: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        auth = jasmine.createSpyObj('AuthService', ['login', 'isAdmin']);
        errorHandler = jasmine.createSpyObj('ErrorHandlerService', ['extraerMensaje']);
        router = jasmine.createSpyObj('Router', ['navigate']);

        await TestBed.configureTestingModule({
            declarations: [LoginComponent],
            imports: [ReactiveFormsModule],
            providers: [
                { provide: AuthService, useValue: auth },
                { provide: ErrorHandlerService, useValue: errorHandler },
                { provide: Router, useValue: router }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('debería crearse correctamente', () => {
        expect(component).toBeTruthy();
    });

    it('el formulario es inválido cuando está vacío', () => {
        expect(component.form.valid).toBeFalse();
    });

    it('el formulario es válido con datos correctos', () => {
        component.form.setValue({ nombreUsuario: 'juanito', contrasena: 'pass1234' });
        expect(component.form.valid).toBeTrue();
    });

    it('toggleContrasena() alterna la visibilidad de la contraseña', () => {
        expect(component.mostrarContrasena).toBeFalse();
        component.toggleContrasena();
        expect(component.mostrarContrasena).toBeTrue();
        component.toggleContrasena();
        expect(component.mostrarContrasena).toBeFalse();
    });

    it('submit() con formulario inválido pone mensaje de error', () => {
        component.submit();
        expect(component.error).toBe('Por favor completa todos los campos.');
        expect(auth.login).not.toHaveBeenCalled();
    });

    it('submit() exitoso redirige a /admin cuando es admin', fakeAsync(() => {
        auth.login.and.returnValue(of({ mensaje: 'ok', usuario: {} as any }));
        auth.isAdmin.and.returnValue(true);
        component.form.setValue({ nombreUsuario: 'admin', contrasena: 'pass1234' });
        component.submit();
        tick();
        expect(router.navigate).toHaveBeenCalledWith(['/admin']);
    }));

    it('submit() exitoso redirige a / cuando es usuario normal', fakeAsync(() => {
        auth.login.and.returnValue(of({ mensaje: 'ok', usuario: {} as any }));
        auth.isAdmin.and.returnValue(false);
        component.form.setValue({ nombreUsuario: 'user', contrasena: 'pass1234' });
        component.submit();
        tick();
        expect(router.navigate).toHaveBeenCalledWith(['/']);
    }));

    it('submit() con error llama a extraerMensaje y muestra el error', fakeAsync(() => {
        const err = new HttpErrorResponse({ status: 401 });
        auth.login.and.returnValue(throwError(() => err));
        errorHandler.extraerMensaje.and.returnValue('Usuario o contraseña incorrectos.');
        component.form.setValue({ nombreUsuario: 'user', contrasena: 'pass1234' });
        component.submit();
        tick();
        expect(component.error).toBe('Usuario o contraseña incorrectos.');
        expect(component.cargando).toBeFalse();
    }));

    it('submit() pone cargando=true mientras la petición está en curso', fakeAsync(() => {
        const subject = new Subject<any>();
        auth.login.and.returnValue(subject.asObservable());
        component.form.setValue({ nombreUsuario: 'user', contrasena: 'pass1234' });
        component.submit();
        expect(component.cargando).toBeTrue();
        subject.complete();
        tick();
    }));
});


describe('RegisterComponent', () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;
    let auth: jasmine.SpyObj<AuthService>;
    let errorHandler: jasmine.SpyObj<ErrorHandlerService>;
    let router: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        auth = jasmine.createSpyObj('AuthService', ['solicitarCodigo', 'registrar']);
        errorHandler = jasmine.createSpyObj('ErrorHandlerService', ['extraerMensaje']);
        router = jasmine.createSpyObj('Router', ['navigate']);

        await TestBed.configureTestingModule({
            declarations: [RegisterComponent],
            imports: [ReactiveFormsModule],
            providers: [
                { provide: AuthService, useValue: auth },
                { provide: ErrorHandlerService, useValue: errorHandler },
                { provide: Router, useValue: router }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(RegisterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('debería crearse correctamente', () => {
        expect(component).toBeTruthy();
    });

    it('inicia en el paso 1', () => {
        expect(component.paso as number).toBe(1);
    });

    it('solicitarCodigo() con correo inválido pone error', () => {
        component.formCorreo.setValue({ correo: 'no-es-email' });
        component.solicitarCodigo();
        expect(component.error).toBe('Ingresa un correo electrónico válido.');
        expect(auth.solicitarCodigo).not.toHaveBeenCalled();
    });

    it('solicitarCodigo() con correo válido avanza al paso 2', fakeAsync(() => {
        auth.solicitarCodigo.and.returnValue(of('ok'));
        component.formCorreo.setValue({ correo: 'user@mail.com' });
        component.solicitarCodigo();
        tick();
        expect(component.paso as number).toBe(2);
        expect(component.correoVerificado).toBe('user@mail.com');
    }));

    it('solicitarCodigo() con error muestra mensaje', fakeAsync(() => {
        auth.solicitarCodigo.and.returnValue(throwError(() => ({ status: 409 })));
        errorHandler.extraerMensaje.and.returnValue('Correo ya registrado.');
        component.formCorreo.setValue({ correo: 'dup@mail.com' });
        component.solicitarCodigo();
        tick();
        expect(component.error).toBe('Correo ya registrado.');
        expect(component.cargando).toBeFalse();
    }));

    it('reenviarCodigo() no hace nada si correoVerificado está vacío', () => {
        component.correoVerificado = '';
        component.reenviarCodigo();
        expect(auth.solicitarCodigo).not.toHaveBeenCalled();
    });

    it('reenviarCodigo() exitoso muestra mensajeExito', fakeAsync(() => {
        auth.solicitarCodigo.and.returnValue(of('ok'));
        component.correoVerificado = 'u@m.com';
        component.reenviarCodigo();
        tick();
        expect(component.mensajeExito).toContain('reenviado');
    }));

    it('volverACorreo() regresa al paso 1 y limpia errores', () => {
        component.paso = 2;
        component.error = 'algo';
        component.volverACorreo();
        expect(component.paso as number).toBe(1);
        expect(component.error).toBe('');
    });

    it('toggleContrasena() alterna la visibilidad', () => {
        expect(component.mostrarContrasena).toBeFalse();
        component.toggleContrasena();
        expect(component.mostrarContrasena).toBeTrue();
    });

    it('registrar() con formulario inválido pone error y no llama al servicio', () => {
        component.registrar();
        expect(component.error).toBe('Por favor completa todos los campos correctamente.');
        expect(auth.registrar).not.toHaveBeenCalled();
    });

    it('registrar() exitoso redirige a /login después de 1800ms', fakeAsync(() => {
        auth.registrar.and.returnValue(of({ mensaje: 'ok', usuario: {} as any }));
        component.paso = 2;
        component.correoVerificado = 'u@m.com';
        component.formRegistro.setValue({
            nombre: 'Juan', apellido: 'Pérez', nombreUsuario: 'juanito',
            contrasena: 'pass1234', telefono: '3001234567', codigoVerificacion: '1234'
        });
        component.registrar();
        tick(1800);
        expect(router.navigate).toHaveBeenCalledWith(['/login']);
    }));
});


describe('HistorialComponent', () => {
    let component: HistorialComponent;
    let fixture: ComponentFixture<HistorialComponent>;
    let historialService: jasmine.SpyObj<HistorialService>;
    let errorHandler: jasmine.SpyObj<ErrorHandlerService>;

    const mockConversiones = [
        {
            id: 1, nombreArchivoOriginal: 'clip.mp4',
            formatoOrigen: 'MP4', formatoDestino: 'MKV',
            tipoArchivo: 'VIDEO' as const, estado: 'COMPLETADO' as const,
            urlDescarga: 'http://s/clip.mkv', fechaConversion: '2024-01-01'
        }
    ];

    beforeEach(async () => {
        historialService = jasmine.createSpyObj('HistorialService', ['misConversiones']);
        errorHandler = jasmine.createSpyObj('ErrorHandlerService', ['extraerMensaje']);
        historialService.misConversiones.and.returnValue(of(mockConversiones));

        await TestBed.configureTestingModule({
            declarations: [HistorialComponent],
            providers: [
                { provide: HistorialService, useValue: historialService },
                { provide: ErrorHandlerService, useValue: errorHandler }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(HistorialComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('debería crearse correctamente', () => {
        expect(component).toBeTruthy();
    });

    it('carga las conversiones en ngOnInit', () => {
        expect(component.conversiones.length).toBe(1);
        expect(component.conversiones[0].formatoDestino).toBe('MKV');
    });

    it('cuando el servicio falla con 204 pone lista vacía', () => {
        historialService.misConversiones.and.returnValue(throwError(() => ({ status: 204 })));
        component.cargar();
        expect(component.conversiones).toEqual([]);
        expect(component.error).toBe('');
    });

    it('cuando el servicio falla con otro error muestra mensaje', () => {
        errorHandler.extraerMensaje.and.returnValue('Error de red');
        historialService.misConversiones.and.returnValue(throwError(() => ({ status: 500 })));
        component.cargar();
        expect(component.error).toBe('Error de red');
    });

    it('colorTipo() devuelve el color correcto para cada tipo', () => {
        expect(component.colorTipo('AUDIO')).toBe('#FF6B4A');
        expect(component.colorTipo('VIDEO')).toBe('#4361EE');
        expect(component.colorTipo('IMAGEN')).toBe('#2EC4B6');
        expect(component.colorTipo('OTRO')).toBe('#888');
    });

    it('iconoEstado() devuelve el ícono correcto', () => {
        expect(component.iconoEstado('COMPLETADO')).toBe('✓');
        expect(component.iconoEstado('PENDIENTE')).toBe('⏳');
        expect(component.iconoEstado('EN_PROCESO')).toBe('⚙️');
        expect(component.iconoEstado('FALLIDO')).toBe('✕');
        expect(component.iconoEstado('DESCONOCIDO')).toBe('?');
    });

    it('formatearFecha() devuelve "—" si la fecha es undefined', () => {
        expect(component.formatearFecha(undefined)).toBe('—');
    });

    it('formatearFecha() formatea una fecha ISO válida', () => {
        const resultado = component.formatearFecha('2024-06-15T10:30:00');
        expect(resultado).toBeTruthy();
        expect(resultado).not.toBe('—');
    });
});


describe('NavbarComponent', () => {
    let component: NavbarComponent;
    let fixture: ComponentFixture<NavbarComponent>;
    let auth: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;
    let theme: jasmine.SpyObj<ThemeService>;

    beforeEach(async () => {
        auth = jasmine.createSpyObj('AuthService', ['logout', 'clearLocal', 'isLoggedIn', 'isAdmin']);
        router = jasmine.createSpyObj('Router', ['navigate']);
        theme = jasmine.createSpyObj('ThemeService', ['isDark', 'toggle']);

        await TestBed.configureTestingModule({
            declarations: [NavbarComponent],
            schemas: [NO_ERRORS_SCHEMA],
            providers: [
                { provide: AuthService, useValue: auth },
                { provide: Router, useValue: router },
                { provide: ThemeService, useValue: theme }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(NavbarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('debería crearse correctamente', () => {
        expect(component).toBeTruthy();
    });

    it('logout() navega a / tras cerrar sesión exitosamente', fakeAsync(() => {
        auth.logout.and.returnValue(of('ok'));
        component.logout();
        tick();
        expect(router.navigate).toHaveBeenCalledWith(['/']);
    }));

    it('logout() llama clearLocal y navega a / si el observable falla', fakeAsync(() => {
        auth.logout.and.returnValue(throwError(() => new Error('fail')));
        component.logout();
        tick();
        expect(auth.clearLocal).toHaveBeenCalled();
        expect(router.navigate).toHaveBeenCalledWith(['/']);
    }));
});


describe('HomeComponent', () => {
    let component: HomeComponent;
    let fixture: ComponentFixture<HomeComponent>;
    let auth: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        auth = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'isAdmin']);
        router = jasmine.createSpyObj('Router', ['navigate']);

        await TestBed.configureTestingModule({
            declarations: [HomeComponent],
            providers: [
                { provide: AuthService, useValue: auth },
                { provide: Router, useValue: router }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(HomeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('debería crearse correctamente', () => {
        expect(component).toBeTruthy();
    });

    it('tiene 3 herramientas de conversión', () => {
        expect(component.herramientas.length).toBe(3);
    });

    it('las herramientas tienen los tipos correctos', () => {
        const tipos = component.herramientas.map(h => h.tipo);
        expect(tipos).toContain('audio');
        expect(tipos).toContain('video');
        expect(tipos).toContain('imagen');
    });

    it('irAConvertir() redirige a /login si el usuario no está autenticado', () => {
        auth.isLoggedIn.and.returnValue(false);
        component.irAConvertir('audio');
        expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('irAConvertir() navega a /convertir/:tipo si el usuario está autenticado', () => {
        auth.isLoggedIn.and.returnValue(true);
        component.irAConvertir('video');
        expect(router.navigate).toHaveBeenCalledWith(['/convertir', 'video']);
    });

    it('irAConvertir() funciona con los tres tipos de conversión', () => {
        auth.isLoggedIn.and.returnValue(true);
        ['audio', 'video', 'imagen'].forEach(tipo => {
            component.irAConvertir(tipo);
            expect(router.navigate).toHaveBeenCalledWith(['/convertir', tipo]);
        });
    });
});


describe('AdminDashboardComponent', () => {
    let component: AdminDashboardComponent;
    let fixture: ComponentFixture<AdminDashboardComponent>;
    let adminService: jasmine.SpyObj<AdminService>;
    let errorHandler: jasmine.SpyObj<ErrorHandlerService>;

    beforeEach(async () => {
        adminService = jasmine.createSpyObj('AdminService', [
            'resumen', 'listarUsuarios', 'listarConversiones',
            'conversionesPorTipo', 'conversionesPorEstado', 'conversionesPorUsuario',
            'eliminarUsuario', 'eliminarConversion', 'editarUsuario',
            'listarAuditoria', 'auditoriaPorTipoAccion', 'auditoriaPorRol',
            'eliminarRegistroAuditoria', 'contarAuditoria'
        ]);
        errorHandler = jasmine.createSpyObj('ErrorHandlerService', ['extraerMensaje']);

        adminService.resumen.and.returnValue(of({ totalUsuarios: 3, totalConversiones: 10 }));
        adminService.listarUsuarios.and.returnValue(of([]));
        adminService.listarConversiones.and.returnValue(of([]));
        adminService.listarAuditoria.and.returnValue(of([]));

        await TestBed.configureTestingModule({
            declarations: [AdminDashboardComponent],
            providers: [
                { provide: AdminService, useValue: adminService },
                { provide: ErrorHandlerService, useValue: errorHandler },
                { provide: ActivatedRoute, useValue: { snapshot: { data: {} } } }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AdminDashboardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('debería crearse correctamente', () => {
        expect(component).toBeTruthy();
    });

    it('ngOnInit carga resumen, usuarios y conversiones', () => {
        expect(adminService.resumen).toHaveBeenCalled();
        expect(adminService.listarUsuarios).toHaveBeenCalled();
        expect(adminService.listarConversiones).toHaveBeenCalled();
    });

    it('colorTipo() retorna el color correcto', () => {
        expect(component.colorTipo('AUDIO')).toBe('#FF6B4A');
        expect(component.colorTipo('VIDEO')).toBe('#4361EE');
        expect(component.colorTipo('IMAGEN')).toBe('#2EC4B6');
        expect(component.colorTipo('X')).toBe('#888');
    });

    it('colorAccion() retorna la clase CSS correcta para cada acción', () => {
        expect(component.colorAccion('LOGIN')).toBe('a-login');
        expect(component.colorAccion('LOGOUT')).toBe('a-logout');
        expect(component.colorAccion('CONVERSION')).toBe('a-conversion');
        expect(component.colorAccion('CREATE')).toBe('a-create');
        expect(component.colorAccion('UPDATE')).toBe('a-update');
        expect(component.colorAccion('DELETE')).toBe('a-delete');
        expect(component.colorAccion('READ')).toBe('a-read');
        expect(component.colorAccion('OTRO')).toBe('');
        expect(component.colorAccion(undefined)).toBe('');
    });

    it('esAdmin() retorna true cuando roles incluye ADMIN', () => {
        expect(component.esAdmin({ nombre:'a', apellido:'b', correo:'c', nombreUsuario:'d', telefono:'e', roles: ['ADMIN'] })).toBeTrue();
    });

    it('esAdmin() retorna true cuando rol es ADMIN', () => {
        expect(component.esAdmin({ nombre:'a', apellido:'b', correo:'c', nombreUsuario:'d', telefono:'e', rol: 'ADMIN' })).toBeTrue();
    });

    it('esAdmin() retorna false cuando roles no incluye ADMIN', () => {
        expect(component.esAdmin({ nombre:'a', apellido:'b', correo:'c', nombreUsuario:'d', telefono:'e', roles: ['USUARIO'] })).toBeFalse();
    });

    it('cambiarFiltroTipo() limpia filtroEstado y recarga conversiones', () => {
        adminService.conversionesPorTipo.and.returnValue(of([]));
        component.filtroEstado = 'COMPLETADO';
        component.cambiarFiltroTipo('AUDIO');
        expect(component.filtroTipo).toBe('AUDIO');
        expect(component.filtroEstado).toBe('');
        expect(adminService.conversionesPorTipo).toHaveBeenCalledWith('AUDIO');
    });

    it('cambiarFiltroTipo() con el mismo tipo activo limpia el filtro (toggle)', () => {
        adminService.listarConversiones.and.returnValue(of([]));
        component.filtroTipo = 'AUDIO';
        component.cambiarFiltroTipo('AUDIO');
        expect(component.filtroTipo).toBe('');
        expect(adminService.listarConversiones).toHaveBeenCalled();
    });

    it('cambiarFiltroEstado() limpia filtroTipo y recarga conversiones', () => {
        adminService.conversionesPorEstado.and.returnValue(of([]));
        component.filtroTipo = 'VIDEO';
        component.cambiarFiltroEstado('COMPLETADO');
        expect(component.filtroEstado).toBe('COMPLETADO');
        expect(component.filtroTipo).toBe('');
        expect(adminService.conversionesPorEstado).toHaveBeenCalledWith('COMPLETADO');
    });

    it('limpiarFiltros() reinicia ambos filtros y recarga conversiones', () => {
        adminService.listarConversiones.and.returnValue(of([]));
        component.filtroTipo = 'AUDIO';
        component.filtroEstado = 'COMPLETADO';
        component.limpiarFiltros();
        expect(component.filtroTipo).toBe('');
        expect(component.filtroEstado).toBe('');
        expect(adminService.listarConversiones).toHaveBeenCalled();
    });

    it('eliminarUsuario() no hace nada si el id es undefined', () => {
        component.eliminarUsuario(undefined);
        expect(adminService.eliminarUsuario).not.toHaveBeenCalled();
    });

    it('eliminarHistorialDeUsuario() no hace nada si usuarioId es undefined', () => {
        component.eliminarHistorialDeUsuario(undefined, 'alguien');
        expect(adminService.conversionesPorUsuario).not.toHaveBeenCalled();
    });

    it('activarTab() cambia la pestaña activa', () => {
        adminService.listarAuditoria.and.returnValue(of([]));
        component.activarTab('conversiones');
        expect(component.tabActiva).toBe('conversiones');
    });

    it('activarTab("auditoria") carga auditoría si la lista está vacía', () => {
        adminService.listarAuditoria.and.returnValue(of([]));
        component.auditoria = [];
        component.activarTab('auditoria');
        expect(adminService.listarAuditoria).toHaveBeenCalled();
    });

    it('cambiarFiltroAccion() activa el filtro y limpia filtroRolAuditoria', () => {
        adminService.auditoriaPorTipoAccion.and.returnValue(of([]));
        component.filtroRolAuditoria = 'ADMIN';
        component.cambiarFiltroAccion('LOGIN');
        expect(component.filtroAccion).toBe('LOGIN');
        expect(component.filtroRolAuditoria).toBe('');
    });

    it('cambiarFiltroAccion() con el mismo valor activo lo limpia (toggle)', () => {
        adminService.listarAuditoria.and.returnValue(of([]));
        component.filtroAccion = 'LOGIN';
        component.cambiarFiltroAccion('LOGIN');
        expect(component.filtroAccion).toBe('');
    });

    it('cambiarFiltroRolAuditoria() activa el filtro y limpia filtroAccion', () => {
        adminService.auditoriaPorRol.and.returnValue(of([]));
        component.filtroAccion = 'DELETE';
        component.cambiarFiltroRolAuditoria('ADMIN');
        expect(component.filtroRolAuditoria).toBe('ADMIN');
        expect(component.filtroAccion).toBe('');
    });

    it('limpiarFiltrosAuditoria() reinicia ambos filtros de auditoría', () => {
        adminService.listarAuditoria.and.returnValue(of([]));
        component.filtroAccion = 'LOGIN';
        component.filtroRolAuditoria = 'ADMIN';
        component.limpiarFiltrosAuditoria();
        expect(component.filtroAccion).toBe('');
        expect(component.filtroRolAuditoria).toBe('');
    });

    it('abrirModalEdicion() carga el usuario y muestra el modal', () => {
        const u: any = { id: 1, nombre: 'Ana', apellido: 'G', correo: 'a@m.com', nombreUsuario: 'ana', telefono: '300' };
        component.abrirModalEdicion(u);
        expect(component.mostrarModalEdicion).toBeTrue();
        expect(component.usuarioEditando?.nombre).toBe('Ana');
        expect(component.usuarioEditando?.contrasena).toBe('');
    });

    it('cerrarModalEdicion() oculta el modal y limpia el estado', () => {
        component.mostrarModalEdicion = true;
        component.usuarioEditando = { nombre:'a', apellido:'b', correo:'c', nombreUsuario:'d', telefono:'e' };
        component.cerrarModalEdicion();
        expect(component.mostrarModalEdicion).toBeFalse();
        expect(component.usuarioEditando).toBeNull();
    });

    it('guardarEdicion() no hace nada si usuarioEditando es null', () => {
        component.usuarioEditando = null;
        component.guardarEdicion();
        expect(adminService.editarUsuario).not.toHaveBeenCalled();
    });

    it('guardarEdicion() exitoso muestra exitoEdicion y cierra el modal', fakeAsync(() => {
        adminService.editarUsuario.and.returnValue(of('ok'));
        adminService.listarUsuarios.and.returnValue(of([]));
        component.usuarioEditando = { id: 1, nombre: 'Ana', apellido: 'G', correo: 'a@m.com', nombreUsuario: 'ana', telefono: '300' };
        component.guardarEdicion();
        tick(0);
        expect(component.exitoEdicion).toBe('Usuario actualizado correctamente.');
        tick(1200);
        expect(component.mostrarModalEdicion).toBeFalse();
    }));
});