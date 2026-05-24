package co.edu.unbosque.proyectofinal.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;

/**
 * Objeto de transferencia de datos (DTO) para los registros de auditoría
 * de la plataforma.
 *
 * <p>Se utiliza tanto para exponer los registros al administrador a través
 * del controlador, como para construir nuevas entradas de auditoría desde
 * la capa de servicios.</p>
 *
 * <p>Cada registro captura quién realizó una acción ({@code usuarioId},
 * {@code nombreUsuario}, {@code rolUsuario}), qué acción fue
 * ({@code tipoAccion}, {@code descripcion}), dónde ocurrió ({@code endpoint})
 * y cuándo ({@code fecha}).</p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 * @see co.edu.unbosque.proyectofinal.util.enums.TipoAccion
 * @see co.edu.unbosque.proyectofinal.util.enums.RolUsuario
 */
public class AuditoriaDTO {

	/** Identificador único del registro de auditoría, generado por la base de datos. */
	private Long id;

	/** Identificador del usuario que realizó la acción auditada. */
	private Long usuarioId;

	/** Nombre de usuario que realizó la acción auditada. */
	private String nombreUsuario;

	/** Rol del usuario en el momento en que se ejecutó la acción. */
	private RolUsuario rolUsuario;

	/**
	 * Tipo de acción registrada.
	 *
	 * @see TipoAccion
	 */
	private TipoAccion tipoAccion;

	/** Descripción legible en lenguaje natural de la acción realizada. */
	private String descripcion;

	/**
	 * Ruta HTTP del endpoint invocado cuando se generó el registro
	 * (p. ej., {@code /autenticacion/login}).
	 */
	private String endpoint;

	/** Fecha y hora exacta en que ocurrió la acción auditada. */
	private LocalDateTime fecha;

	/**
	 * Constructor sin argumentos requerido por frameworks de serialización
	 * (Jackson, JPA, etc.).
	 */
	public AuditoriaDTO() {
	}

	/**
	 * Crea un registro de auditoría con todos los campos relevantes para
	 * documentar una acción del sistema.
	 *
	 * <p>El campo {@code id} queda sin asignar porque es generado
	 * automáticamente por la base de datos al persistir la entidad.</p>
	 *
	 * @param usuarioId     identificador del usuario que realizó la acción.
	 * @param nombreUsuario nombre de usuario que realizó la acción.
	 * @param rolUsuario    rol del usuario en el momento de la acción
	 *                      ({@code USUARIO} o {@code ADMIN}).
	 * @param tipoAccion    tipo de acción realizada; uno de
	 *                      {@code CREATE}, {@code READ}, {@code UPDATE},
	 *                      {@code DELETE}, {@code LOGIN}, {@code LOGOUT},
	 *                      {@code CONVERSION}.
	 * @param descripcion   descripción legible de la acción realizada.
	 * @param endpoint      ruta HTTP del endpoint invocado
	 *                      (p. ej., {@code /autenticacion/login}).
	 * @param fecha         fecha y hora exacta en que ocurrió la acción.
	 */
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

	/**
	 * Retorna el identificador único del registro de auditoría.
	 *
	 * @return id del registro, o {@code null} si aún no ha sido persistido.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Asigna el identificador único del registro de auditoría.
	 *
	 * @param id identificador generado por la base de datos.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Retorna el identificador del usuario que realizó la acción auditada.
	 *
	 * @return id del usuario.
	 */
	public Long getUsuarioId() {
		return usuarioId;
	}

	/**
	 * Asigna el identificador del usuario que realizó la acción auditada.
	 *
	 * @param usuarioId id del usuario.
	 */
	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	/**
	 * Retorna el nombre de usuario que realizó la acción auditada.
	 *
	 * @return nombre de usuario.
	 */
	public String getNombreUsuario() {
		return nombreUsuario;
	}

	/**
	 * Asigna el nombre de usuario que realizó la acción auditada.
	 *
	 * @param nombreUsuario nombre de usuario.
	 */
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	/**
	 * Retorna el rol del usuario en el momento en que se realizó la acción.
	 *
	 * @return rol del usuario ({@code USUARIO} o {@code ADMIN}).
	 */
	public RolUsuario getRolUsuario() {
		return rolUsuario;
	}

	/**
	 * Asigna el rol del usuario en el momento en que se realizó la acción.
	 *
	 * @param rolUsuario rol del usuario ({@code USUARIO} o {@code ADMIN}).
	 */
	public void setRolUsuario(RolUsuario rolUsuario) {
		this.rolUsuario = rolUsuario;
	}

	/**
	 * Retorna el tipo de acción registrada en la auditoría.
	 *
	 * @return tipo de acción; uno de {@code CREATE}, {@code READ},
	 *         {@code UPDATE}, {@code DELETE}, {@code LOGIN},
	 *         {@code LOGOUT}, {@code CONVERSION}.
	 */
	public TipoAccion getTipoAccion() {
		return tipoAccion;
	}

	/**
	 * Asigna el tipo de acción registrada en la auditoría.
	 *
	 * @param tipoAccion tipo de acción realizada.
	 */
	public void setTipoAccion(TipoAccion tipoAccion) {
		this.tipoAccion = tipoAccion;
	}

	/**
	 * Retorna la descripción legible de la acción realizada.
	 *
	 * @return descripción de la acción.
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Asigna la descripción legible de la acción realizada.
	 *
	 * @param descripcion descripción de la acción.
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Retorna la ruta HTTP del endpoint invocado cuando se generó el registro.
	 *
	 * @return ruta del endpoint (p. ej., {@code /autenticacion/login}).
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Asigna la ruta HTTP del endpoint invocado cuando se generó el registro.
	 *
	 * @param endpoint ruta del endpoint.
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Retorna la fecha y hora en que ocurrió la acción auditada.
	 *
	 * @return fecha y hora de la acción.
	 */
	public LocalDateTime getFecha() {
		return fecha;
	}

	/**
	 * Asigna la fecha y hora en que ocurrió la acción auditada.
	 *
	 * @param fecha fecha y hora de la acción.
	 */
	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	/**
	 * Retorna un código hash basado únicamente en el {@code id} del registro.
	 *
	 * @return código hash del registro de auditoría.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Compara este registro con otro objeto por igualdad.
	 *
	 * <p>Dos instancias de {@code AuditoriaDTO} se consideran iguales
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
		AuditoriaDTO other = (AuditoriaDTO) obj;
		return Objects.equals(id, other.id);
	}

	/**
	 * Retorna una representación en cadena del registro de auditoría
	 * con todos sus campos principales.
	 *
	 * @return cadena con los valores de {@code id}, {@code usuarioId},
	 *         {@code nombreUsuario}, {@code rolUsuario}, {@code tipoAccion},
	 *         {@code descripcion}, {@code endpoint} y {@code fecha}.
	 */
	@Override
	public String toString() {
		return "AuditoriaDTO [id=" + id + ", usuarioId=" + usuarioId
				+ ", nombreUsuario=" + nombreUsuario + ", rolUsuario=" + rolUsuario
				+ ", tipoAccion=" + tipoAccion + ", descripcion=" + descripcion
				+ ", endpoint=" + endpoint + ", fecha=" + fecha + "]";
	}
}