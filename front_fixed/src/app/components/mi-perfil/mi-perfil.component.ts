import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { UsuarioDTO } from '../../models/usuario-dto';

const API = 'http://localhost:8080';

/**
 * Componente que permite al usuario autenticado ver y editar su perfil.
 *
 * Carga los datos del perfil al inicializarse mediante `AuthService.miPerfil()`
 * y los envía actualizados al backend mediante un `PUT` a `/usuario/actualizar`.
 * Tras una actualización exitosa refresca el `localStorage` con los nuevos datos.
 */
@Component({
    selector: 'app-mi-perfil',
    templateUrl: './mi-perfil.component.html',
    styleUrls: ['./mi-perfil.component.css']
})
export class MiPerfilComponent implements OnInit {

    /** Datos del perfil del usuario activo, inicializados con campos vacíos. */
    perfil: UsuarioDTO = {
        nombre: '', apellido: '', correo: '',
        nombreUsuario: '', telefono: '', rol: ''
    };

    /** Nueva contraseña ingresada por el usuario (opcional al actualizar). */
    nuevaContrasena: string = '';

    /** Indica si la carga inicial del perfil está en curso. */
    cargando: boolean = true;

    /** Indica si el guardado del perfil está en curso. */
    guardando: boolean = false;

    /** Mensaje de error mostrado cuando la carga inicial del perfil falla. */
    errorCarga: string = '';

    /** Mensaje de éxito mostrado tras una actualización exitosa. */
    mensajeExito: string = '';

    /** Mensaje de error mostrado cuando el guardado del perfil falla. */
    mensajeError: string = '';

    /**
     * @param http Cliente HTTP de Angular para el `PUT` de actualización.
     * @param auth Servicio de autenticación para cargar el perfil actual.
     */
    constructor(private http: HttpClient, private auth: AuthService) {}

    /**
     * Carga el perfil del usuario al inicializar el componente.
     */
    ngOnInit(): void {
        this.cargarPerfil();
    }

    /**
     * Solicita al backend el perfil completo del usuario autenticado y
     * actualiza la propiedad `perfil`. En caso de error asigna un mensaje
     * a `errorCarga`.
     */
    cargarPerfil(): void {
        this.cargando = true;
        this.auth.miPerfil().subscribe({
            next: (data) => {
                this.perfil = { ...data };
                this.cargando = false;
            },
            error: () => {
                this.errorCarga = 'No se pudo cargar el perfil. Intenta de nuevo.';
                this.cargando = false;
            }
        });
    }

    /**
     * Valida los campos obligatorios y envía los datos actualizados al backend.
     *
     * Campos obligatorios: `nombre`, `apellido` y `nombreUsuario`.
     * La contraseña se incluye en el payload solo si `nuevaContrasena` no está vacía.
     * Tras una actualización exitosa refresca el perfil en `localStorage`,
     * limpia `nuevaContrasena` y muestra `mensajeExito` durante 4 segundos.
     * En caso de error asigna el mensaje correspondiente a `mensajeError`.
     */
    guardar(): void {
        this.mensajeExito = '';
        this.mensajeError = '';

        if (!this.perfil.nombre?.trim() || !this.perfil.apellido?.trim() || !this.perfil.nombreUsuario?.trim()) {
            this.mensajeError = 'Nombre, apellido y nombre de usuario son obligatorios.';
            return;
        }

        const payload: UsuarioDTO = {
            ...this.perfil,
            contrasena: this.nuevaContrasena.trim() || undefined
        };

        this.guardando = true;
        this.http.put(`${API}/usuario/actualizar`, payload, { responseType: 'text' }).subscribe({
            next: () => {
                this.mensajeExito = '¡Perfil actualizado correctamente!';
                this.nuevaContrasena = '';
                this.guardando = false;
                this.auth.miPerfil().subscribe({
                    next: (data) => {
                        const u: any = { ...data, rol: this.perfil.rol, roles: [this.perfil.rol] };
                        localStorage.setItem('usuario', JSON.stringify(u));
                    },
                    error: () => {}
                });
                setTimeout(() => this.mensajeExito = '', 4000);
            },
            error: (err) => {
                this.mensajeError = err?.error || 'Error al guardar. Intenta de nuevo.';
                this.guardando = false;
            }
        });
    }
}