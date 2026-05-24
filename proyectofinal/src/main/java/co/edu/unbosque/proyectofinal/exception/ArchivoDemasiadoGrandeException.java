package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando el archivo enviado para conversión supera
 * el tamaño máximo permitido por el sistema.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class ArchivoDemasiadoGrandeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ArchivoDemasiadoGrandeException(long tamanoBytes, long maximoBytes) {
		super("El archivo supera el tamaño máximo permitido. "
				+ "Tamaño recibido: " + (tamanoBytes / (1024 * 1024)) + " MB. "
				+ "Tamaño máximo permitido: " + (maximoBytes / (1024 * 1024)) + " MB. "
				+ "Por favor seleccione un archivo más pequeño.");
	}
}
