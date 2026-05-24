package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando el formato de un archivo no es soportado
 * por la plataforma de conversión o no corresponde al tipo indicado
 * (por ejemplo, intentar convertir un archivo .mp4 marcándolo como AUDIO).
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class FormatoArchivoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FormatoArchivoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
