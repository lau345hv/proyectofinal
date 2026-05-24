package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción que indica que un nombre o apellido contiene caracteres
 * inválidos (números, símbolos, etc). Sólo se permiten letras,
 * espacios y caracteres acentuados del español.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class NombreInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NombreInvalidoException(String mensaje) {
		super(mensaje);
	}
}
