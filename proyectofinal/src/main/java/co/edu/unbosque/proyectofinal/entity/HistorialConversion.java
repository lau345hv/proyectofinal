package co.edu.unbosque.proyectofinal.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa el historial de una conversión de archivo
 * realizada por un usuario en la plataforma.
 *
 * <p>Almacena toda la información relevante de cada conversión: categoría
 * del archivo, formato de origen, formato de destino, nombres y rutas de
 * los archivos involucrados, fecha de ejecución y estado del proceso.
 * Permite al usuario consultar sus conversiones previas y volver a obtener
 * tanto el archivo original como el resultado convertido.</p>
 *
 * <p>Mapeada a la tabla {@code proyectofinal_historial_conversion} de la
 * base de datos. La relación con {@link Usuario} se carga de forma
 * perezosa ({@link FetchType#LAZY}) para optimizar el rendimiento.</p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 * @see co.edu.unbosque.proyectofinal.util.enums.EstadoConversion
 * @see co.edu.unbosque.proyectofinal.util.enums.TipoArchivo
 * @see Usuario
 */
@Entity
@Table(name = "proyectofinal_historial_conversion")
public class HistorialConversion {

	/** Identificador único del registro, generado automáticamente por la base de datos. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Fecha y hora en que se realizó la conversión. No puede ser {@code null}. */
	@Column(nullable = false, name = "fecha_conversion")
	private LocalDateTime fechaConversion;

	/** Categoría del archivo procesado (AUDIO, VIDEO o IMAGEN). No puede ser {@code null}. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "tipo_archivo")
	private TipoArchivo tipoArchivo;

	/**
	 * Extensión del formato original del archivo antes de la conversión
	 * (p. ej., {@code mp4}, {@code wav}, {@code jpg}). No puede ser {@code null}.
	 */
	@Column(nullable = false, name = "formato_origen", length = 20)
	private String formatoOrigen;

	/**
	 * Extensión del formato al que se convirtió el archivo
	 * (p. ej., {@code mp3}, {@code png}, {@code mkv}). No puede ser {@code null}.
	 */
	@Column(nullable = false, name = "formato_destino", length = 20)
	private String formatoDestino;

	/** Nombre original del archivo subido por el usuario. Longitud máxima: 300 caracteres. */
	@Column(name = "nombre_archivo_original", length = 300)
	private String nombreArchivoOriginal;

	/** Nombre del archivo resultante tras la conversión. Longitud máxima: 300 caracteres. */
	@Column(name = "nombre_archivo_convertido", length = 300)
	private String nombreArchivoConvertido;

	/**
	 * Ruta o URL de almacenamiento del archivo original.
	 * Longitud máxima: 2000 caracteres para soportar URLs largas de servicios en la nube.
	 */
	@Column(name = "ruta_archivo_original", length = 2000)
	private String rutaArchivoOriginal;

	/**
	 * Ruta o URL de almacenamiento del archivo convertido.
	 * Longitud máxima: 2000 caracteres para soportar URLs largas de servicios en la nube.
	 */
	@Column(name = "ruta_archivo_convertido", length = 2000)
	private String rutaArchivoConvertido;

	/**
	 * Estado actual del proceso de conversión
	 * (PENDIENTE, EN_PROCESO, COMPLETADO o FALLIDO). No puede ser {@code null}.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoConversion estado;

	/**
	 * Usuario propietario de esta conversión.
	 * <p>Relación {@code ManyToOne} cargada de forma perezosa para optimizar
	 * el rendimiento; la clave foránea se almacena en la columna
	 * {@code usuario_id} y no puede ser {@code null}.</p>
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	/**
	 * Constructor sin argumentos requerido por JPA y frameworks de
	 * serialización (Jackson, etc.).
	 */
	public HistorialConversion() {
	}

	/**
	 * Crea un registro de historial con todos los campos relevantes.
	 *
	 * <p>El campo {@code id} queda sin asignar porque es generado
	 * automáticamente por la base de datos al persistir la entidad.</p>
	 *
	 * @param fechaConversion         fecha y hora en que se realizó la conversión.
	 * @param tipoArchivo             categoría del archivo ({@code AUDIO},
	 *                                {@code VIDEO} o {@code IMAGEN}).
	 * @param formatoOrigen           extensión del formato original del archivo.
	 * @param formatoDestino          extensión del formato al que se convirtió.
	 * @param nombreArchivoOriginal   nombre original del archivo subido.
	 * @param nombreArchivoConvertido nombre del archivo resultante tras la conversión.
	 * @param rutaArchivoOriginal     ruta o URL del archivo original almacenado.
	 * @param rutaArchivoConvertido   ruta o URL del archivo convertido almacenado.
	 * @param estado                  estado del proceso ({@code PENDIENTE},
	 *                                {@code EN_PROCESO}, {@code COMPLETADO}
	 *                                o {@code FALLIDO}).
	 * @param usuario                 entidad {@link Usuario} propietaria de la conversión.
	 */
	public HistorialConversion(LocalDateTime fechaConversion, TipoArchivo tipoArchivo,
			String formatoOrigen, String formatoDestino, String nombreArchivoOriginal,
			String nombreArchivoConvertido, String rutaArchivoOriginal,
			String rutaArchivoConvertido, EstadoConversion estado, Usuario usuario) {
		this.fechaConversion = fechaConversion;
		this.tipoArchivo = tipoArchivo;
		this.formatoOrigen = formatoOrigen;
		this.formatoDestino = formatoDestino;
		this.nombreArchivoOriginal = nombreArchivoOriginal;
		this.nombreArchivoConvertido = nombreArchivoConvertido;
		this.rutaArchivoOriginal = rutaArchivoOriginal;
		this.rutaArchivoConvertido = rutaArchivoConvertido;
		this.estado = estado;
		this.usuario = usuario;
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
	 * Retorna la entidad {@link Usuario} propietaria de esta conversión.
	 *
	 * @return usuario propietario de la conversión.
	 */
	public Usuario getUsuario() {
		return usuario;
	}

	/**
	 * Asigna la entidad {@link Usuario} propietaria de esta conversión.
	 *
	 * @param usuario usuario propietario de la conversión.
	 */
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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
	 * <p>Dos instancias de {@code HistorialConversion} se consideran iguales
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
		HistorialConversion other = (HistorialConversion) obj;
		return Objects.equals(id, other.id);
	}

	/**
	 * Retorna una representación en cadena del registro con los campos principales.
	 *
	 * <p>Por brevedad, se omiten {@code nombreArchivoOriginal},
	 * {@code nombreArchivoConvertido}, {@code rutaArchivoOriginal},
	 * {@code rutaArchivoConvertido} y {@code usuario}.</p>
	 *
	 * @return cadena con los valores de {@code id}, {@code fechaConversion},
	 *         {@code tipoArchivo}, {@code formatoOrigen}, {@code formatoDestino}
	 *         y {@code estado}.
	 */
	@Override
	public String toString() {
		return "HistorialConversion [id=" + id + ", fechaConversion=" + fechaConversion
				+ ", tipoArchivo=" + tipoArchivo + ", formatoOrigen=" + formatoOrigen
				+ ", formatoDestino=" + formatoDestino + ", estado=" + estado + "]";
	}
}