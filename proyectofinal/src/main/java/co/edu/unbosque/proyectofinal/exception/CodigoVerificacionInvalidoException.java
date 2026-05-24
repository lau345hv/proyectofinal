package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción que indica que el código de verificación enviado por
 * correo electrónico es inválido, ha expirado, o no corresponde
 * al correo registrado.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class CodigoVerificacionInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CodigoVerificacionInvalidoException(String mensaje) {
		super(mensaje);
	}
}
