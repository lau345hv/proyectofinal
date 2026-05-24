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

	/**
	 * Constructor sin argumentos requerido por JPA para la instanciación
	 * de entidades al cargarlas desde la base de datos.
	 */
	public Auditoria() {
	}

	/**
	 * Crea un registro de auditoría con todos los campos relevantes.
	 * El campo {@code id} queda sin asignar porque lo genera la base de datos
	 * al persistir la entidad.
	 *
	 * @param usuarioId     ID del usuario que realizó la acción; puede ser
	 *                      {@code null} si la acción fue anónima
	 * @param nombreUsuario nombre de usuario en el momento de la acción;
	 *                      puede ser {@code null} si la acción fue anónima
	 * @param rolUsuario    rol del usuario en el momento de ejecutar la acción
	 * @param tipoAccion    tipo de acción ejecutada (CREATE, READ, UPDATE,
	 *                      DELETE, LOGIN, LOGOUT, CONVERSION)
	 * @param descripcion   descripción legible de la acción realizada
	 * @param endpoint      ruta HTTP del endpoint invocado
	 * @param fecha         fecha y hora exacta en que se registró la acción
	 */
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

	/**
	 * Retorna el identificador único del registro de auditoría.
	 *
	 * @return id generado por la base de datos
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Asigna el identificador único del registro de auditoría.
	 *
	 * @param id identificador generado por la base de datos
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Retorna el ID del usuario que realizó la acción auditada.
	 *
	 * @return id del usuario, o {@code null} si la acción fue anónima
	 */
	public Long getUsuarioId() {
		return usuarioId;
	}

	/**
	 * Asigna el ID del usuario que realizó la acción auditada.
	 *
	 * @param usuarioId id del usuario; puede ser {@code null}
	 */
	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	/**
	 * Retorna el nombre de usuario en el momento en que se realizó la acción.
	 *
	 * @return nombre de usuario, o {@code null} si la acción fue anónima
	 */
	public String getNombreUsuario() {
		return nombreUsuario;
	}

	/**
	 * Asigna el nombre de usuario en el momento en que se realizó la acción.
	 *
	 * @param nombreUsuario nombre de usuario; puede ser {@code null}
	 */
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	/**
	 * Retorna el rol del usuario en el momento de ejecutar la acción.
	 *
	 * @return rol del usuario (USUARIO o ADMIN), o {@code null} si fue anónimo
	 */
	public RolUsuario getRolUsuario() {
		return rolUsuario;
	}

	/**
	 * Asigna el rol del usuario en el momento de ejecutar la acción.
	 *
	 * @param rolUsuario rol del usuario (USUARIO o ADMIN)
	 */
	public void setRolUsuario(RolUsuario rolUsuario) {
		this.rolUsuario = rolUsuario;
	}

	/**
	 * Retorna el tipo de acción ejecutada y registrada en la auditoría.
	 *
	 * @return tipo de acción (CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, CONVERSION)
	 */
	public TipoAccion getTipoAccion() {
		return tipoAccion;
	}

	/**
	 * Asigna el tipo de acción ejecutada y registrada en la auditoría.
	 *
	 * @param tipoAccion tipo de acción realizada
	 */
	public void setTipoAccion(TipoAccion tipoAccion) {
		this.tipoAccion = tipoAccion;
	}

	/**
	 * Retorna la descripción legible de la acción realizada.
	 * Ejemplo: "Conversión de video.mkv a mp4", "Perfil actualizado".
	 *
	 * @return descripción de la acción
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Asigna la descripción legible de la acción realizada.
	 *
	 * @param descripcion descripción de la acción
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Retorna el endpoint o recurso sobre el que se realizó la acción.
	 *
	 * @return ruta del endpoint (ej. {@code /autenticacion/login})
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Asigna el endpoint o recurso sobre el que se realizó la acción.
	 *
	 * @param endpoint ruta del endpoint
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Retorna la fecha y hora exacta en que se registró la acción.
	 *
	 * @return fecha y hora del registro de auditoría
	 */
	public LocalDateTime getFecha() {
		return fecha;
	}

	/**
	 * Asigna la fecha y hora exacta en que se registró la acción.
	 *
	 * @param fecha fecha y hora del registro de auditoría
	 */
	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	/**
	 * Retorna un código hash basado únicamente en el {@code id} del registro.
	 *
	 * @return código hash del registro
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Compara esta entidad con otro objeto. Dos instancias de
	 * {@code Auditoria} son iguales si tienen el mismo {@code id}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si los ids son iguales, {@code false} en caso contrario
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Auditoria other = (Auditoria) obj;
		return Objects.equals(id, other.id);
	}

	/**
	 * Retorna una representación en cadena del registro de auditoría con
	 * todos sus campos principales.
	 *
	 * @return cadena con los valores de la entidad
	 */
	@Override
	public String toString() {
		return "Auditoria [id=" + id + ", usuarioId=" + usuarioId
				+ ", nombreUsuario=" + nombreUsuario + ", rolUsuario=" + rolUsuario
				+ ", tipoAccion=" + tipoAccion + ", descripcion=" + descripcion
				+ ", endpoint=" + endpoint + ", fecha=" + fecha + "]";
	}
}