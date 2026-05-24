package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando un campo numérico supera el valor máximo
 * permitido por el sistema.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class NumeroDemasiadoGrandeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NumeroDemasiadoGrandeException(String campo, double valor, double maximo) {
		super("El campo '" + campo + "' supera el valor máximo permitido de " + maximo
				+ ". Valor recibido: " + valor
				+ ". Por favor ingrese un valor dentro del rango permitido.");
	}
}
