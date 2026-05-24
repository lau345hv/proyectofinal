package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando un campo numérico que solo admite valores
 * positivos recibe un cero o un número negativo.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class NumeroNegativoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NumeroNegativoException(String campo, double valor) {
		super("El campo '" + campo + "' no admite valores negativos ni cero. Valor recibido: " + valor
				+ ". Por favor ingrese un número mayor que cero.");
	}
}
