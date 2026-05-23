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
 * Entidad que representa el historial de una conversión de archivo
 * realizada por un usuario en la plataforma.
 * <p>
 * Almacena toda la información relevante de cada conversión: tipo de
 * archivo, formato de origen, formato destino, nombres, rutas, fecha y
 * estado del proceso. Permite al usuario consultar sus conversiones
 * previas y volver a obtener tanto el archivo original como el resultado
 * convertido.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Entity
@Table(name = "proyectofinal_historial_conversion")
public class HistorialConversion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "fecha_conversion")
	private LocalDateTime fechaConversion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "tipo_archivo")
	private TipoArchivo tipoArchivo;

	@Column(nullable = false, name = "formato_origen", length = 20)
	private String formatoOrigen;

	@Column(nullable = false, name = "formato_destino", length = 20)
	private String formatoDestino;

	@Column(name = "nombre_archivo_original", length = 300)
	private String nombreArchivoOriginal;

	@Column(name = "nombre_archivo_convertido", length = 300)
	private String nombreArchivoConvertido;

	@Column(name = "ruta_archivo_original", length = 1000)
	private String rutaArchivoOriginal;

	@Column(name = "ruta_archivo_convertido", length = 1000)
	private String rutaArchivoConvertido;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoConversion estado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	public HistorialConversion() {
	}

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getFechaConversion() {
		return fechaConversion;
	}

	public void setFechaConversion(LocalDateTime fechaConversion) {
		this.fechaConversion = fechaConversion;
	}

	public TipoArchivo getTipoArchivo() {
		return tipoArchivo;
	}

	public void setTipoArchivo(TipoArchivo tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

	public String getFormatoOrigen() {
		return formatoOrigen;
	}

	public void setFormatoOrigen(String formatoOrigen) {
		this.formatoOrigen = formatoOrigen;
	}

	public String getFormatoDestino() {
		return formatoDestino;
	}

	public void setFormatoDestino(String formatoDestino) {
		this.formatoDestino = formatoDestino;
	}

	public String getNombreArchivoOriginal() {
		return nombreArchivoOriginal;
	}

	public void setNombreArchivoOriginal(String nombreArchivoOriginal) {
		this.nombreArchivoOriginal = nombreArchivoOriginal;
	}

	public String getNombreArchivoConvertido() {
		return nombreArchivoConvertido;
	}

	public void setNombreArchivoConvertido(String nombreArchivoConvertido) {
		this.nombreArchivoConvertido = nombreArchivoConvertido;
	}

	public String getRutaArchivoOriginal() {
		return rutaArchivoOriginal;
	}

	public void setRutaArchivoOriginal(String rutaArchivoOriginal) {
		this.rutaArchivoOriginal = rutaArchivoOriginal;
	}

	public String getRutaArchivoConvertido() {
		return rutaArchivoConvertido;
	}

	public void setRutaArchivoConvertido(String rutaArchivoConvertido) {
		this.rutaArchivoConvertido = rutaArchivoConvertido;
	}

	public EstadoConversion getEstado() {
		return estado;
	}

	public void setEstado(EstadoConversion estado) {
		this.estado = estado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		HistorialConversion other = (HistorialConversion) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "HistorialConversion [id=" + id + ", fechaConversion=" + fechaConversion
				+ ", tipoArchivo=" + tipoArchivo + ", formatoOrigen=" + formatoOrigen
				+ ", formatoDestino=" + formatoDestino + ", estado=" + estado + "]";
	}
}
