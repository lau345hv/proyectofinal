package co.edu.unbosque.proyectofinal.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que registra cada acción relevante realizada por los usuarios
 * dentro de la plataforma de conversión de archivos.
 * <p>
 * Permite al administrador llevar un rastro completo de todas las
 * operaciones efectuadas: conversiones, accesos, modificaciones de
 * perfil, registros, eliminaciones, etc. Los datos del usuario se
 * almacenan como campos planos (id y nombre de usuario) en lugar de una
 * relación {@code @ManyToOne} para conservar el registro incluso si el
 * usuario es eliminado posteriormente.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Entity
@Table(name = "proyectofinal_auditoria")
public class Auditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** ID del usuario que realizó la acción (puede ser null si fue anónimo). */
	@Column(name = "usuario_id")
	private Long usuarioId;

	/** Nombre de usuario en el momento de la acción (puede ser null si fue anónimo). */
	@Column(name = "nombre_usuario", length = 100)
	private String nombreUsuario;

	/** Rol del usuario en el momento de ejecutar la acción. */
	@Enumerated(EnumType.STRING)
	@Column(name = "rol_usuario")
	private RolUsuario rolUsuario;

	/** Tipo de acción ejecutada (CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, CONVERSION). */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "tipo_accion")
	private TipoAccion tipoAccion;

	/**
	 * Descripción legible de la acción realizada.
	 * Ejemplo: "Conversión de video.mkv a mp4", "Perfil actualizado".
	 */
	@Column(name = "descripcion", length = 500)
	private String descripcion;

	/** Endpoint o recurso sobre el que se realizó la acción. */
	@Column(name = "endpoint", length = 200)
	private String endpoint;

	/** Fecha y hora exacta en que se registró la acción. */
	@Column(nullable = false, name = "fecha")
	private LocalDateTime fecha;

	public Auditoria() {
	}

	public Auditoria(Long usuarioId, String nombreUsuario, RolUsuario rolUsuario,
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
		Auditoria other = (Auditoria) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "Auditoria [id=" + id + ", usuarioId=" + usuarioId
				+ ", nombreUsuario=" + nombreUsuario + ", rolUsuario=" + rolUsuario
				+ ", tipoAccion=" + tipoAccion + ", descripcion=" + descripcion
				+ ", endpoint=" + endpoint + ", fecha=" + fecha + "]";
	}
}