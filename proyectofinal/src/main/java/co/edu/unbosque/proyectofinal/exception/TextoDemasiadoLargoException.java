package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando un campo de texto supera la longitud
 * máxima permitida por el sistema.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class TextoDemasiadoLargoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TextoDemasiadoLargoException(String campo, int longitudRecibida, int longitudMaxima) {
		super("El campo '" + campo + "' supera la longitud máxima permitida de " + longitudMaxima
				+ " caracteres. Longitud recibida: " + longitudRecibida
				+ ". Por favor acorte el texto ingresado.");
	}
}
