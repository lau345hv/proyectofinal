package co.edu.unbosque.proyectofinal.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Objeto de transferencia de datos (DTO) que representa un usuario de la plataforma.
 *
 * <p>Transfiere la información del usuario entre las capas del sistema y se utiliza
 * también como cuerpo de petición en los endpoints de login, registro y actualización
 * del perfil. Las conversiones del usuario se representan como una lista de IDs para
 * evitar referencias circulares al serializar a JSON.</p>
 *
 * <p>Los campos {@code null} se excluyen de la serialización JSON gracias a
 * {@code @JsonInclude(NON_NULL)}, lo que permite, por ejemplo, omitir la contraseña
 * en las respuestas hacia el cliente.</p>
 *
 * <h3>Corrección aplicada (DeepSource JAVA-E1086) — Copia defensiva:</h3>
 * <p>El setter {@code setHistorialConversionesId} anteriormente asignaba directamente
 * la referencia de la lista recibida al campo interno, permitiendo que código externo
 * modificara el estado del DTO sin pasar por ningún método controlado.</p>
 * <p><strong>Solución:</strong> tanto el getter como el setter operan sobre copias
 * independientes mediante {@code new ArrayList<>(lista)}, aislando completamente
 * el estado interno.</p>
 *
 * <pre>
 * // Antes de la corrección:
 * List&lt;Long&gt; ids = new ArrayList&lt;&gt;(List.of(1L, 2L));
 * dto.setHistorialConversionesId(ids);
 * ids.clear(); // ¡Vaciaba también la lista interna del DTO!
 *
 * // Después de la corrección:
 * dto.setHistorialConversionesId(ids);
 * ids.clear(); // El DTO conserva su copia con [1, 2].
 * </pre>
 *
 * @author Equipo de desarrollo
 * @version 2.1
 * @see co.edu.unbosque.proyectofinal.util.enums.RolUsuario
 */
@Schema(description = "Datos de un usuario de la plataforma")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDTO {

	/** Identificador único del usuario, generado automáticamente por la base de datos. */
	@Schema(accessMode = Schema.AccessMode.READ_ONLY,
			description = "ID generado automáticamente por la base de datos", example = "1")
	private Long id;

	/** Nombre del usuario. */
	@Schema(description = "Nombre del usuario", example = "Carlos")
	private String nombre;

	/** Apellido del usuario. */
	@Schema(description = "Apellido del usuario", example = "Rodríguez")
	private String apellido;

	/** Correo electrónico único del usuario. */
	@Schema(description = "Correo electrónico único", example = "carlos@correo.com")
	private String correo;

	/** Nombre de usuario único utilizado para iniciar sesión. */
	@Schema(description = "Nombre de usuario único para iniciar sesión", example = "carlos.r")
	private String nombreUsuario;

	/**
	 * Contraseña del usuario.
	 * <p>En las respuestas al cliente este campo se omite si es {@code null}
	 * gracias a {@code @JsonInclude(NON_NULL)}, evitando exponer el hash.</p>
	 */
	@Schema(description = "Contraseña (mínimo 8 caracteres, al menos una letra y un número)",
			example = "MiClave123")
	private String contrasena;

	/** Número de teléfono del usuario (10 dígitos, Colombia). */
	@Schema(description = "Número de teléfono (10 dígitos comenzando con 3, Colombia)",
			example = "3001234567")
	private String telefono;

	/** Rol del usuario en el sistema ({@code USUARIO} o {@code ADMIN}). */
	@Schema(description = "Rol del usuario en el sistema",
			example = "USUARIO",
			allowableValues = { "USUARIO", "ADMIN" })
	private RolUsuario rol;

	/**
	 * Lista de IDs del historial de conversiones del usuario.
	 * <p>Se inicializa como lista vacía para evitar {@code NullPointerException}
	 * antes de que se asigne un valor explícito.</p>
	 */
	@Schema(accessMode = Schema.AccessMode.READ_ONLY,
			description = "IDs del historial de conversiones del usuario")
	private List<Long> historialConversionesId = new ArrayList<>();

	/**
	 * Constructor sin argumentos requerido por frameworks de serialización
	 * (Jackson, JPA, etc.).
	 */
	public UsuarioDTO() {
	}

	/**
	 * Crea un DTO con las credenciales mínimas para el inicio de sesión.
	 *
	 * @param nombreUsuario nombre de usuario para iniciar sesión.
	 * @param contrasena    contraseña del usuario.
	 */
	public UsuarioDTO(String nombreUsuario, String contrasena) {
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
	}

	/**
	 * Crea un DTO con los datos principales del usuario, sin incluir
	 * teléfono ni historial de conversiones.
	 *
	 * @param nombre        nombre del usuario.
	 * @param apellido      apellido del usuario.
	 * @param correo        correo electrónico único.
	 * @param nombreUsuario nombre de usuario único para iniciar sesión.
	 * @param contrasena    contraseña del usuario.
	 * @param rol           rol asignado al usuario ({@code USUARIO} o {@code ADMIN}).
	 */
	public UsuarioDTO(String nombre, String apellido, String correo, String nombreUsuario,
			String contrasena, RolUsuario rol) {
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.rol = rol;
	}

	/**
	 * Retorna el identificador único del usuario.
	 *
	 * @return id del usuario, o {@code null} si aún no ha sido persistido.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Asigna el identificador único del usuario.
	 *
	 * @param id identificador generado por la base de datos.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Retorna el nombre del usuario.
	 *
	 * @return nombre del usuario.
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Asigna el nombre del usuario.
	 *
	 * @param nombre nombre del usuario.
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Retorna el apellido del usuario.
	 *
	 * @return apellido del usuario.
	 */
	public String getApellido() {
		return apellido;
	}

	/**
	 * Asigna el apellido del usuario.
	 *
	 * @param apellido apellido del usuario.
	 */
	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	/**
	 * Retorna el correo electrónico único del usuario.
	 *
	 * @return correo electrónico del usuario.
	 */
	public String getCorreo() {
		return correo;
	}

	/**
	 * Asigna el correo electrónico único del usuario.
	 *
	 * @param correo correo electrónico del usuario.
	 */
	public void setCorreo(String correo) {
		this.correo = correo;
	}

	/**
	 * Retorna el nombre de usuario único utilizado para iniciar sesión.
	 *
	 * @return nombre de usuario.
	 */
	public String getNombreUsuario() {
		return nombreUsuario;
	}

	/**
	 * Asigna el nombre de usuario único utilizado para iniciar sesión.
	 *
	 * @param nombreUsuario nombre de usuario.
	 */
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	/**
	 * Retorna la contraseña del usuario.
	 *
	 * <p>En las respuestas hacia el cliente este campo puede ser {@code null}
	 * gracias a {@code @JsonInclude(NON_NULL)}, evitando exponer el hash
	 * de la contraseña almacenada.</p>
	 *
	 * @return contraseña del usuario, o {@code null} si fue omitida.
	 */
	public String getContrasena() {
		return contrasena;
	}

	/**
	 * Asigna la contraseña del usuario.
	 *
	 * @param contrasena contraseña del usuario (mínimo 8 caracteres,
	 *                   al menos una letra y un número).
	 */
	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	/**
	 * Retorna el número de teléfono del usuario.
	 *
	 * @return número de teléfono (10 dígitos, Colombia).
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * Asigna el número de teléfono del usuario.
	 *
	 * @param telefono número de teléfono (10 dígitos comenzando con 3, Colombia).
	 */
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	/**
	 * Retorna el rol asignado al usuario en el sistema.
	 *
	 * @return rol del usuario ({@code USUARIO} o {@code ADMIN}).
	 */
	public RolUsuario getRol() {
		return rol;
	}

	/**
	 * Asigna el rol del usuario en el sistema.
	 *
	 * @param rol rol del usuario ({@code USUARIO} o {@code ADMIN}).
	 */
	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	/**
	 * Devuelve una copia defensiva de la lista de IDs del historial de conversiones.
	 *
	 * <p>Se retorna una copia nueva para que el código externo no pueda modificar
	 * la lista interna del DTO directamente (corrección JAVA-E1086).</p>
	 *
	 * @return nueva lista con los mismos IDs del historial de conversiones.
	 */
	public List<Long> getHistorialConversionesId() {
		return new ArrayList<>(historialConversionesId);
	}

	/**
	 * Asigna la lista de IDs del historial de conversiones realizando una copia defensiva.
	 *
	 * <p>Se almacena una copia interna de la lista recibida para que modificaciones
	 * posteriores sobre la lista original no afecten el estado interno del DTO
	 * (corrección JAVA-E1086). Si se pasa {@code null}, se asigna una lista vacía.</p>
	 *
	 * @param historialConversionesId lista de IDs a asignar; puede ser {@code null}.
	 */
	public void setHistorialConversionesId(List<Long> historialConversionesId) {
		this.historialConversionesId = (historialConversionesId != null)
				? new ArrayList<>(historialConversionesId)
				: new ArrayList<>();
	}

	/**
	 * Retorna un código hash basado únicamente en el {@code id} del usuario.
	 *
	 * @return código hash del usuario.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Compara este DTO con otro objeto por igualdad.
	 *
	 * <p>Dos instancias de {@code UsuarioDTO} se consideran iguales si y solo si
	 * tienen el mismo valor de {@code id}.</p>
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
		UsuarioDTO other = (UsuarioDTO) obj;
		return Objects.equals(id, other.id);
	}

	/**
	 * Retorna una representación en cadena del DTO con los campos principales.
	 *
	 * <p>La contraseña no se incluye por razones de seguridad.</p>
	 *
	 * @return cadena con los valores de {@code id}, {@code nombre}, {@code apellido},
	 *         {@code correo}, {@code nombreUsuario} y {@code rol}.
	 */
	@Override
	public String toString() {
		return "UsuarioDTO [id=" + id + ", nombre=" + nombre + ", apellido=" + apellido
				+ ", correo=" + correo + ", nombreUsuario=" + nombreUsuario
				+ ", rol=" + rol + "]";
	}
}