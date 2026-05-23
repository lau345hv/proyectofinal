import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ErrorHandlerService } from '../../services/error-handler.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  paso: 1 | 2 = 1;
  cargando = false;
  error = '';
  mensajeExito = '';
  mostrarContrasena = false;

  formCorreo: FormGroup;
  formRegistro: FormGroup;

  correoVerificado = '';

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

  volverACorreo(): void {
    this.paso = 1;
    this.error = '';
    this.mensajeExito = '';
    this.formRegistro.reset();
  }

  toggleContrasena(): void {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  registrar(): void {
    if (this.formRegistro.invalid) {
      this.formRegistro.markAllAsTouched();
      this.error = 'Por favor completa todos los campos correctamente.';
      return;
    }
    this.cargando = true;
    this.error = '';
    this.mensajeExito = '';

    const valores = this.formRegistro.value;

    this.auth.registrar({
      nombre: valores.nombre.trim(),
      apellido: valores.apellido.trim(),
      correo: this.correoVerificado,
      nombreUsuario: valores.nombreUsuario.trim(),
      contrasena: valores.contrasena,
      telefono: valores.telefono.trim(),
      codigoVerificacion: valores.codigoVerificacion.trim()
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

  private obtenerTextoErrorRaw(err: unknown): string {
    if (!err) return '';
    const errObj = err as Record<string, unknown>;
    const body = errObj?.['error'];
    if (typeof body === 'string') return body;
    if (body && typeof body === 'object') {
      return `${body.message ?? ''} ${body.trace ?? ''}`;
    }
    const errRecord = err as Record<string, unknown>;
    return errRecord?.['message'] as string || '';
  }
}
