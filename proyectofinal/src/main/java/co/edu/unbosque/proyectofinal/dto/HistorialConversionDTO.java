package co.edu.unbosque.proyectofinal.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Objeto de transferencia de datos (DTO) que representa el historial
 * de una conversión de archivo realizada en la plataforma.
 * <p>
 * El usuario propietario se representa únicamente con su ID para
 * evitar referencias circulares al serializar a JSON.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.1
 */
public class HistorialConversionDTO {

	private Long id;

	private LocalDateTime fechaConversion;

	private TipoArchivo tipoArchivo;

	private String formatoOrigen;

	private String formatoDestino;

	private String nombreArchivoOriginal;

	private String nombreArchivoConvertido;

	private String rutaArchivoOriginal;

	private String rutaArchivoConvertido;

	private EstadoConversion estado;

	private Long usuarioId;

	public HistorialConversionDTO() {
	}

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

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
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
		HistorialConversionDTO other = (HistorialConversionDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "HistorialConversionDTO [id=" + id + ", fechaConversion=" + fechaConversion
				+ ", tipoArchivo=" + tipoArchivo + ", formatoOrigen=" + formatoOrigen
				+ ", formatoDestino=" + formatoDestino + ", estado=" + estado
				+ ", usuarioId=" + usuarioId + "]";
	}
}