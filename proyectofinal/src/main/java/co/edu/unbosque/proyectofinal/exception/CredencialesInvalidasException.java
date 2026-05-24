package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando las credenciales ingresadas en el inicio
 * de sesión (nombre de usuario o contraseña) no son correctas.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class CredencialesInvalidasException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CredencialesInvalidasException() {
		super("El nombre de usuario o la contraseña son incorrectos. "
				+ "Por favor verifique sus credenciales e intente nuevamente.");
	}
}
