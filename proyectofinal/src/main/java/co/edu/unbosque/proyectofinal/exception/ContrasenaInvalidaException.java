package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando una contraseña no cumple con las reglas
 * de seguridad del sistema (mínimo 8 caracteres, al menos una letra
 * y al menos un número).
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class ContrasenaInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ContrasenaInvalidaException() {
		super("La contraseña no cumple con las reglas de seguridad. "
				+ "Debe tener mínimo 8 caracteres, al menos una letra y al menos un número.");
	}

	public ContrasenaInvalidaException(String mensaje) {
		super(mensaje);
	}
}
