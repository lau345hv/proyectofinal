package co.edu.unbosque.proyectofinal.util.enums;

/**
 * Enumeración que representa los tipos de acción que se pueden
 * registrar en el sistema de auditoría de la plataforma.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public enum TipoAccion {

	/** El usuario o admin creó un recurso (registro, conversión, etc.). */
	CREATE,

	/** El usuario o admin consultó información del sistema. */
	READ,

	/** El usuario o admin actualizó datos de un recurso existente. */
	UPDATE,

	/** El usuario o admin eliminó un recurso. */
	DELETE,

	/** El usuario inició sesión en la plataforma. */
	LOGIN,

	/** El usuario cerró sesión. */
	LOGOUT,

	/** El usuario solicitó una conversión de archivo. */
	CONVERSION
}