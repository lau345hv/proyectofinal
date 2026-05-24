package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando el archivo subido no corresponde con el tipo
 * de conversión seleccionado (ej: subió un audio pero seleccionó IMAGEN).
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class TipoArchivoNoCoincideException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TipoArchivoNoCoincideException(String tipoEsperado, String tipoRecibido) {
		super("El archivo subido no corresponde con el tipo de conversión seleccionado. "
				+ "Seleccionaste " + tipoEsperado + " pero el archivo es de tipo "
				+ tipoRecibido + ". Por favor sube un archivo que coincida con la "
				+ "categoría seleccionada.");
	}

	public TipoArchivoNoCoincideException(String mensaje) {
		super(mensaje);
	}
}
