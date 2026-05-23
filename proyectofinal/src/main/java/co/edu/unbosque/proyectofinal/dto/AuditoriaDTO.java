package co.edu.unbosque.proyectofinal.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;

/**
 * Objeto de transferencia de datos (DTO) para los registros de auditoría
 * de la plataforma.
 * <p>
 * Se usa tanto para exponer los registros al administrador en el
 * controlador como para construir nuevas entradas de auditoría desde
 * los servicios.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class AuditoriaDTO {

	private Long id;

	private Long usuarioId;

	private String nombreUsuario;

	private RolUsuario rolUsuario;

	private TipoAccion tipoAccion;

	private String descripcion;

	private String endpoint;

	private LocalDateTime fecha;

	public AuditoriaDTO() {
	}

	public AuditoriaDTO(Long usuarioId, String nombreUsuario, RolUsuario rolUsuario,
			TipoAccion tipoAccion, String descripcion, String endpoint, LocalDateTime fecha) {
		this.usuarioId = usuarioId;
		this.nombreUsuario = nombreUsuario;
		this.rolUsuario = rolUsuario;
		this.tipoAccion = tipoAccion;
		this.descripcion = descripcion;
		this.endpoint = endpoint;
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public RolUsuario getRolUsuario() {
		return rolUsuario;
	}

	public void setRolUsuario(RolUsuario rolUsuario) {
		this.rolUsuario = rolUsuario;
	}

	public TipoAccion getTipoAccion() {
		return tipoAccion;
	}

	public void setTipoAccion(TipoAccion tipoAccion) {
		this.tipoAccion = tipoAccion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
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
		AuditoriaDTO other = (AuditoriaDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "AuditoriaDTO [id=" + id + ", usuarioId=" + usuarioId
				+ ", nombreUsuario=" + nombreUsuario + ", rolUsuario=" + rolUsuario
				+ ", tipoAccion=" + tipoAccion + ", descripcion=" + descripcion
				+ ", endpoint=" + endpoint + ", fecha=" + fecha + "]";
	}
}