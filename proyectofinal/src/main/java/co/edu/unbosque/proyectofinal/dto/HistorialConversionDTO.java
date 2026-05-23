package co.edu.unbosque.proyectofinal.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Objeto de transferencia de datos (DTO) que representa el historial
 * de una conversión de archivo realizada en la plataforma.
 * <p>
 * El usuario propietario se representa únicamente con su ID para
 * evitar referencias circulares al serializar a JSON.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Schema(description = "Registro de una conversión realizada por un usuario")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistorialConversionDTO {

	@Schema(accessMode = Schema.AccessMode.READ_ONLY,
			description = "ID del registro generado automáticamente", example = "1")
	private Long id;

	@Schema(description = "Fecha y hora de la conversión",
			example = "2025-05-09T14:30:00")
	private LocalDateTime fechaConversion;

	@Schema(description = "Tipo de archivo procesado",
			example = "VIDEO",
			allowableValues = { "AUDIO", "VIDEO", "IMAGEN" })
	private TipoArchivo tipoArchivo;

	@Schema(description = "Formato del archivo original", example = "mkv")
	private String formatoOrigen;

	@Schema(description = "Formato al que se convirtió el archivo", example = "mp4")
	private String formatoDestino;

	@Schema(description = "Nombre original del archivo subido por el usuario",
			example = "entrenamiento.mkv")
	private String nombreArchivoOriginal;

	@Schema(description = "Nombre del archivo resultante",
			example = "entrenamiento.mp4")
	private String nombreArchivoConvertido;

	@Schema(description = "Ruta o URL del archivo original")
	private String rutaArchivoOriginal;

	@Schema(description = "URL de descarga del archivo convertido (generada por la API externa)")
	private String rutaArchivoConvertido;

	@Schema(description = "Estado actual del proceso de conversión",
			example = "COMPLETADO",
			allowableValues = { "PENDIENTE", "EN_PROCESO", "COMPLETADO", "FALLIDO" })
	private EstadoConversion estado;

	@Schema(description = "ID del usuario propietario de esta conversión", example = "1")
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
