package co.edu.unbosque.proyectofinal.util.enums;

/**
 * Enumeración que representa los posibles estados de una conversión
 * de archivo en el sistema.
 * <p>
 * Permite rastrear el ciclo de vida de cada conversión desde que
 * es solicitada hasta que finaliza o falla.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public enum EstadoConversion {

	/** La conversión ha sido registrada y está esperando ser procesada. */
	PENDIENTE,

	/** La conversión está siendo procesada actualmente por la API externa. */
	EN_PROCESO,

	/** La conversión finalizó exitosamente y el archivo está disponible. */
	COMPLETADO,

	/** La conversión falló por algún error durante el proceso. */
	FALLIDO
}
