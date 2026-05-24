import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ErrorHandlerService } from '../../services/error-handler.service';

/**
 * Componente que gestiona el flujo de registro en dos pasos:
 *
 * - **Paso 1:** El usuario ingresa su correo y solicita un código de verificación.
 * - **Paso 2:** El usuario completa sus datos personales e ingresa el código
 *   recibido por correo para crear la cuenta.
 *
 * Tras un registro exitoso redirige a `/login` después de 1 800 ms.
 */
@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {

    /** Paso actual del flujo de registro: `1` = correo, `2` = datos personales. */
    paso: 1 | 2 = 1;

    /** Indica si hay una petición en curso. */
    cargando: boolean = false;

    /** Mensaje de error a mostrar al usuario. */
    error: string = '';

    /** Mensaje de éxito a mostrar al usuario. */
    mensajeExito: string = '';

    /** Controla si la contraseña se muestra en texto plano o enmascarada. */
    mostrarContrasena: boolean = false;

    /** Formulario del paso 1: solo el campo correo. */
    formCorreo: FormGroup;

    /** Formulario del paso 2: datos personales y código de verificación. */
    formRegistro: FormGroup;

    /** Correo validado en el paso 1, usado al enviar el registro en el paso 2. */
    correoVerificado: string = '';

    /**
     * @param fb           Constructor de formularios reactivos de Angular.
     * @param auth         Servicio de autenticación.
     * @param router       Router de Angular para la navegación post-registro.
     * @param errorHandler Servicio centralizado de manejo de errores HTTP.
     */
    constructor(
        private fb: FormBuilder,
        private auth: AuthService,
        private router: Router,
        private errorHandler: ErrorHandlerService
    ) {
        this.formCorreo = this.fb.group({
            correo: ['', [Validators.required, Validators.email]]
        });

        this.formRegistro = this.fb.group({
            nombre: ['', [Validators.required, Validators.minLength(2)]],
            apellido: ['', [Validators.required, Validators.minLength(2)]],
            nombreUsuario: ['', [Validators.required, Validators.minLength(3)]],
            contrasena: ['', [Validators.required, Validators.minLength(8)]],
            telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
            codigoVerificacion: ['', [Validators.required, Validators.minLength(4)]]
        });
    }

    /**
     * Solicita el envío del código de verificación al correo ingresado.
     *
     * Si el campo `correo` es inválido, muestra un error y no llama al
     * servicio. Ante éxito avanza al paso 2 guardando el correo validado.
     */
    solicitarCodigo(): void {
        if (this.formCorreo.invalid) {
            this.error = 'Ingresa un correo electrónico válido.';
            return;
        }
        this.cargando = true;
        this.error = '';
        this.mensajeExito = '';

        const correo = this.formCorreo.value.correo.trim();

        this.auth.solicitarCodigo(correo).subscribe({
            next: () => {
                this.cargando = false;
                this.correoVerificado = correo;
                this.paso = 2;
                this.mensajeExito = `Se envió un código de verificación a ${correo}. Revisa tu bandeja de entrada (y la carpeta de spam).`;
            },
            error: (err) => {
                this.cargando = false;
                this.error = this.errorHandler.extraerMensaje(
                    err,
                    'No se pudo enviar el código. Verifica que el correo sea válido y que no esté ya registrado.'
                );
            }
        });
    }

    /**
     * Reenvía el código de verificación al correo ya validado en el paso 1.
     * No hace nada si `correoVerificado` está vacío.
     */
    reenviarCodigo(): void {
        if (!this.correoVerificado) return;
        this.cargando = true;
        this.error = '';
        this.mensajeExito = '';

        this.auth.solicitarCodigo(this.correoVerificado).subscribe({
            next: () => {
                this.cargando = false;
                this.mensajeExito = 'Código reenviado correctamente. Revisa tu correo.';
            },
            error: (err) => {
                this.cargando = false;
                this.error = this.errorHandler.extraerMensaje(err, 'No se pudo reenviar el código.');
            }
        });
    }

    /**
     * Regresa al paso 1, limpia los mensajes y resetea el formulario de registro.
     */
    volverACorreo(): void {
        this.paso = 1;
        this.error = '';
        this.mensajeExito = '';
        this.formRegistro.reset();
    }

    /**
     * Alterna la visibilidad de la contraseña entre texto plano y enmascarado.
     */
    toggleContrasena(): void {
        this.mostrarContrasena = !this.mostrarContrasena;
    }

    /**
     * Envía el formulario de registro al backend con los datos del paso 2.
     *
     * Si el formulario es inválido muestra un error sin llamar al servicio.
     * Si el backend indica que el código expiró, limpia el campo del código
     * y reenvía uno nuevo automáticamente.
     * Tras un registro exitoso redirige a `/login` después de 1 800 ms.
     */
    registrar(): void {
        if (this.formRegistro.invalid) {
            this.formRegistro.markAllAsTouched();
            this.error = 'Por favor completa todos los campos correctamente.';
            return;
        }
        this.cargando = true;
        this.error = '';
        this.mensajeExito = '';

        const v = this.formRegistro.value;

        this.auth.registrar({
            nombre: v.nombre.trim(),
            apellido: v.apellido.trim(),
            correo: this.correoVerificado,
            nombreUsuario: v.nombreUsuario.trim(),
            contrasena: v.contrasena,
            telefono: v.telefono.trim(),
            codigoVerificacion: v.codigoVerificacion.trim()
        }).subscribe({
            next: () => {
                this.cargando = false;
                this.mensajeExito = '¡Cuenta creada con éxito! Redirigiendo al login...';
                setTimeout(() => this.router.navigate(['/login']), 1800);
            },
            error: (err) => {
                this.cargando = false;
                const msg = this.errorHandler.extraerMensaje(
                    err,
                    'No se pudo crear la cuenta. Revisa que el código sea correcto y que el nombre de usuario no esté en uso.'
                );

                const errorTexto = this.obtenerTextoErrorRaw(err).toLowerCase();

                if (errorTexto.includes('no hay ning') && errorTexto.includes('digo pendiente')) {
                    this.error = 'El código de verificación expiró o no se encontró. Se reenviará un nuevo código a tu correo automáticamente.';
                    this.formRegistro.patchValue({ codigoVerificacion: '' });
                    this.reenviarCodigoSilencioso();
                } else {
                    this.error = msg;
                }
            }
        });
    }

    /**
     * Reenvía el código de verificación de forma silenciosa (sin modificar
     * el estado de `cargando`) para no interrumpir la experiencia del usuario.
     * Actualiza `mensajeExito` o `error` según el resultado.
     */
    private reenviarCodigoSilencioso(): void {
        if (!this.correoVerificado) return;
        this.auth.solicitarCodigo(this.correoVerificado).subscribe({
            next: () => {
                this.mensajeExito = 'Se envió un nuevo código a tu correo. Ingrésalo arriba.';
            },
            error: () => {
                this.error = 'No se pudo reenviar el código. Haz clic en "← Cambiar correo" y repite el proceso.';
            }
        });
    }

    /**
     * Extrae el texto crudo del cuerpo del error HTTP para detectar
     * mensajes específicos del backend (ej. código expirado).
     *
     * @param err Error capturado en el `subscribe`.
     * @returns Texto concatenado del cuerpo del error.
     */
    private obtenerTextoErrorRaw(err: any): string {
        if (!err) return '';
        const body = err?.error;
        if (typeof body === 'string') return body;
        if (body && typeof body === 'object') {
            return (body.message || '') + ' ' + (body.trace || '');
        }
        return err?.message || '';
    }
}