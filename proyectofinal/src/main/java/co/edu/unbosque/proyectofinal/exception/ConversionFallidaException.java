package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando una conversión de archivo falla durante
 * el proceso, ya sea porque la API externa rechazó el trabajo o
 * porque la tarea terminó en estado de error.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class ConversionFallidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConversionFallidaException(String mensaje) {
		super("La conversión del archivo no pudo completarse. " + mensaje);
	}
}
