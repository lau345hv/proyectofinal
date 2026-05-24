package co.edu.unbosque.proyectofinal.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Objeto de transferencia de datos (DTO) que representa el historial
 * de una conversión de archivo realizada en la plataforma.
 *
 * <p>Encapsula toda la información asociada a una conversión: categoría
 * del archivo, formatos de origen y destino, nombres y rutas de los
 * archivos involucrados, estado del proceso y fecha de ejecución.</p>
 *
 * <p>El usuario propietario se representa únicamente con su {@code usuarioId}
 * para evitar referencias circulares al serializar a JSON.</p>
 *
 * @author Equipo de desarrollo
 * @version 1.1
 * @see co.edu.unbosque.proyectofinal.util.enums.EstadoConversion
 * @see co.edu.unbosque.proyectofinal.util.enums.TipoArchivo
 */
public class HistorialConversionDTO {

	/** Identificador único del registro de historial, generado por la base de datos. */
	private Long id;

	/** Fecha y hora en que se realizó la conversión. */
	private LocalDateTime fechaConversion;

	/** Categoría del archivo procesado (AUDIO, VIDEO o IMAGEN). */
	private TipoArchivo tipoArchivo;

	/** Extensión del formato original del archivo antes de la conversión (p. ej., {@code mp4}, {@code wav}). */
	private String formatoOrigen;

	/** Extensión del formato al que se convirtió el archivo (p. ej., {@code mp3}, {@code png}). */
	private String formatoDestino;

	/** Nombre original del archivo subido por el usuario. */
	private String nombreArchivoOriginal;

	/** Nombre del archivo resultante tras la conversión. */
	private String nombreArchivoConvertido;

	/** Ruta o URL de almacenamiento del archivo original. */
	private String rutaArchivoOriginal;

	/** Ruta o URL de almacenamiento del archivo convertido. */
	private String rutaArchivoConvertido;

	/** Estado actual del proceso de conversión (PENDIENTE, EN_PROCESO, COMPLETADO o FALLIDO). */
	private EstadoConversion estado;

	/** Identificador del usuario propietario de esta conversión. */
	private Long usuarioId;

	/**
	 * Constructor sin argumentos requerido por frameworks de serialización
	 * (Jackson, JPA, etc.).
	 */
	public HistorialConversionDTO() {
	}

	/**
	 * Crea un registro de historial con todos los campos relevantes.
	 *
	 * <p>El campo {@code id} queda sin asignar porque es generado
	 * automáticamente por la base de datos al persistir la entidad.</p>
	 *
	 * @param fechaConversion         fecha y hora en que se realizó la conversión.
	 * @param tipoArchivo             categoría del archivo ({@code AUDIO}, {@code VIDEO}
	 *                                o {@code IMAGEN}).
	 * @param formatoOrigen           extensión del formato original del archivo
	 *                                (p. ej., {@code mp4}, {@code wav}, {@code jpg}).
	 * @param formatoDestino          extensión del formato al que se convirtió
	 *                                (p. ej., {@code mp3}, {@code png}, {@code mkv}).
	 * @param nombreArchivoOriginal   nombre original del archivo subido por el usuario.
	 * @param nombreArchivoConvertido nombre del archivo resultante tras la conversión.
	 * @param rutaArchivoOriginal     ruta o URL del archivo original almacenado.
	 * @param rutaArchivoConvertido   ruta o URL del archivo convertido almacenado.
	 * @param estado                  estado del proceso ({@code PENDIENTE},
	 *                                {@code EN_PROCESO}, {@code COMPLETADO}
	 *                                o {@code FALLIDO}).
	 * @param usuarioId               identificador del usuario propietario.
	 */
	public HistorialConversionDTO(LocalDateTime fechaConversion, TipoArchivo tipoArchivo,
			String formatoOrigen, String formatoDestino, String nombreArchivoOriginal,
			String nombreArchivoConvertido, String rutaArchivoOriginal,
			String rutaArchivoConvertido, EstadoConversion estado, Long usuarioId) {
		this.fechaConversion = fechaConversion;
		this.tipoArchivo = tipoArchivo;
		this.formatoOrigen = formatoOrigen;
		this.formatoDestino = formatoDestino;
		this.nombreArchivoOriginal = nombreArchivoOriginal;
		this.nombreArchivoConvertido = nombreArchivoConvertido;
		this.rutaArchivoOriginal = rutaArchivoOriginal;
		this.rutaArchivoConvertido = rutaArchivoConvertido;
		this.estado = estado;
		this.usuarioId = usuarioId;
	}

	/**
	 * Retorna el identificador único del registro de historial.
	 *
	 * @return id del registro, o {@code null} si aún no ha sido persistido.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Asigna el identificador único del registro de historial.
	 *
	 * @param id identificador generado por la base de datos.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Retorna la fecha y hora en que se realizó la conversión.
	 *
	 * @return fecha y hora de la conversión.
	 */
	public LocalDateTime getFechaConversion() {
		return fechaConversion;
	}

	/**
	 * Asigna la fecha y hora en que se realizó la conversión.
	 *
	 * @param fechaConversion fecha y hora de la conversión.
	 */
	public void setFechaConversion(LocalDateTime fechaConversion) {
		this.fechaConversion = fechaConversion;
	}

	/**
	 * Retorna la categoría del archivo procesado.
	 *
	 * @return tipo de archivo ({@code AUDIO}, {@code VIDEO} o {@code IMAGEN}).
	 */
	public TipoArchivo getTipoArchivo() {
		return tipoArchivo;
	}

	/**
	 * Asigna la categoría del archivo procesado.
	 *
	 * @param tipoArchivo tipo de archivo ({@code AUDIO}, {@code VIDEO} o {@code IMAGEN}).
	 */
	public void setTipoArchivo(TipoArchivo tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

	/**
	 * Retorna la extensión del formato original del archivo antes de la conversión.
	 *
	 * @return formato de origen (p. ej., {@code mp4}, {@code wav}, {@code jpg}).
	 */
	public String getFormatoOrigen() {
		return formatoOrigen;
	}

	/**
	 * Asigna la extensión del formato original del archivo.
	 *
	 * @param formatoOrigen formato de origen (p. ej., {@code mp4}, {@code wav}, {@code jpg}).
	 */
	public void setFormatoOrigen(String formatoOrigen) {
		this.formatoOrigen = formatoOrigen;
	}

	/**
	 * Retorna la extensión del formato al que se convirtió el archivo.
	 *
	 * @return formato de destino (p. ej., {@code mp3}, {@code png}, {@code mkv}).
	 */
	public String getFormatoDestino() {
		return formatoDestino;
	}

	/**
	 * Asigna la extensión del formato al que se convirtió el archivo.
	 *
	 * @param formatoDestino formato de destino (p. ej., {@code mp3}, {@code png}, {@code mkv}).
	 */
	public void setFormatoDestino(String formatoDestino) {
		this.formatoDestino = formatoDestino;
	}

	/**
	 * Retorna el nombre original del archivo subido por el usuario.
	 *
	 * @return nombre del archivo original.
	 */
	public String getNombreArchivoOriginal() {
		return nombreArchivoOriginal;
	}

	/**
	 * Asigna el nombre original del archivo subido por el usuario.
	 *
	 * @param nombreArchivoOriginal nombre del archivo original.
	 */
	public void setNombreArchivoOriginal(String nombreArchivoOriginal) {
		this.nombreArchivoOriginal = nombreArchivoOriginal;
	}

	/**
	 * Retorna el nombre del archivo resultante tras la conversión.
	 *
	 * @return nombre del archivo convertido.
	 */
	public String getNombreArchivoConvertido() {
		return nombreArchivoConvertido;
	}

	/**
	 * Asigna el nombre del archivo resultante tras la conversión.
	 *
	 * @param nombreArchivoConvertido nombre del archivo convertido.
	 */
	public void setNombreArchivoConvertido(String nombreArchivoConvertido) {
		this.nombreArchivoConvertido = nombreArchivoConvertido;
	}

	/**
	 * Retorna la ruta o URL del archivo original almacenado.
	 *
	 * @return ruta o URL del archivo original.
	 */
	public String getRutaArchivoOriginal() {
		return rutaArchivoOriginal;
	}

	/**
	 * Asigna la ruta o URL del archivo original almacenado.
	 *
	 * @param rutaArchivoOriginal ruta o URL del archivo original.
	 */
	public void setRutaArchivoOriginal(String rutaArchivoOriginal) {
		this.rutaArchivoOriginal = rutaArchivoOriginal;
	}

	/**
	 * Retorna la ruta o URL del archivo convertido almacenado.
	 *
	 * @return ruta o URL del archivo convertido.
	 */
	public String getRutaArchivoConvertido() {
		return rutaArchivoConvertido;
	}

	/**
	 * Asigna la ruta o URL del archivo convertido almacenado.
	 *
	 * @param rutaArchivoConvertido ruta o URL del archivo convertido.
	 */
	public void setRutaArchivoConvertido(String rutaArchivoConvertido) {
		this.rutaArchivoConvertido = rutaArchivoConvertido;
	}

	/**
	 * Retorna el estado actual del proceso de conversión.
	 *
	 * @return estado de la conversión ({@code PENDIENTE}, {@code EN_PROCESO},
	 *         {@code COMPLETADO} o {@code FALLIDO}).
	 */
	public EstadoConversion getEstado() {
		return estado;
	}

	/**
	 * Asigna el estado actual del proceso de conversión.
	 *
	 * @param estado estado de la conversión.
	 */
	public void setEstado(EstadoConversion estado) {
		this.estado = estado;
	}

	/**
	 * Retorna el identificador del usuario propietario de esta conversión.
	 *
	 * @return id del usuario propietario.
	 */
	public Long getUsuarioId() {
		return usuarioId;
	}

	/**
	 * Asigna el identificador del usuario propietario de esta conversión.
	 *
	 * @param usuarioId id del usuario propietario.
	 */
	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	/**
	 * Retorna un código hash basado únicamente en el {@code id} del registro.
	 *
	 * @return código hash del registro de historial.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Compara este registro con otro objeto por igualdad.
	 *
	 * <p>Dos instancias de {@code HistorialConversionDTO} se consideran iguales
	 * si y solo si tienen el mismo valor de {@code id}.</p>
	 *
	 * @param obj objeto a comparar con esta instancia.
	 * @return {@code true} si ambos objetos tienen el mismo {@code id};
	 *         {@code false} en caso contrario.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		HistorialConversionDTO other = (HistorialConversionDTO) obj;
		return Objects.equals(id, other.id);
	}

	/**
	 * Retorna una representación en cadena del registro con los campos principales.
	 *
	 * <p>Por brevedad, se omiten {@code nombreArchivoOriginal},
	 * {@code nombreArchivoConvertido}, {@code rutaArchivoOriginal} y
	 * {@code rutaArchivoConvertido}.</p>
	 *
	 * @return cadena con los valores de {@code id}, {@code fechaConversion},
	 *         {@code tipoArchivo}, {@code formatoOrigen}, {@code formatoDestino},
	 *         {@code estado} y {@code usuarioId}.
	 */
	@Override
	public String toString() {
		return "HistorialConversionDTO [id=" + id + ", fechaConversion=" + fechaConversion
				+ ", tipoArchivo=" + tipoArchivo + ", formatoOrigen=" + formatoOrigen
				+ ", formatoDestino=" + formatoDestino + ", estado=" + estado
				+ ", usuarioId=" + usuarioId + "]";
	}
}