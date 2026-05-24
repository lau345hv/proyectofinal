package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando un usuario intenta ejecutar una acción
 * para la cual no tiene los permisos necesarios (por ejemplo, un
 * cliente intentando acceder a una ruta exclusiva del administrador).
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class UsuarioSinPermisoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsuarioSinPermisoException(String mensaje) {
		super("No tiene permisos suficientes para realizar esta acción. " + mensaje);
	}
}
