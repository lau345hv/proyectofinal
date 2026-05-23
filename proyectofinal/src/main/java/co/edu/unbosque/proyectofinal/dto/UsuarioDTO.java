package co.edu.unbosque.proyectofinal.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Objeto de transferencia de datos (DTO) que representa un usuario.
 * <p>
 * Transfiere la información del usuario entre las capas del sistema y
 * se utiliza también como cuerpo de petición en los endpoints de login,
 * registro y actualización del perfil. Las conversiones del usuario se
 * representan como una lista de IDs para evitar referencias circulares
 * al serializar a JSON.
 * </p>
 *
 * <h3>Corrección aplicada (DeepSource JAVA-E1086):</h3>
 * <p>
 * El setter {@code setHistorialConversionesId} asignaba directamente la
 * referencia de la lista recibida como parámetro al campo interno. Esto
 * permite que el código externo que llamó al setter mantenga una
 * referencia al mismo objeto {@code List} y pueda modificar el estado
 * interno del DTO sin pasar por ningún método controlado, lo que
 * constituye una violación del principio de encapsulamiento y puede
 * causar comportamientos inesperados (bug risk).
 * </p>
 * <p>
 * <strong>Solución — Copia defensiva:</strong> Tanto el setter como el
 * getter ahora trabajan con copias independientes de la lista mediante
 * {@code new ArrayList<>(lista)}. El setter crea una copia interna al
 * recibir el parámetro, y el getter devuelve una copia al exponer el
 * campo. De esta forma ningún código externo puede alterar la lista
 * interna del DTO directamente.
 * </p>
 * <p>
 * Ejemplo del problema antes de la corrección:
 * <pre>
 *   List&lt;Long&gt; ids = new ArrayList&lt;&gt;(List.of(1L, 2L));
 *   dto.setHistorialConversionesId(ids);
 *   ids.clear(); // ¡Esto también vaciaba la lista interna del DTO!
 * </pre>
 * Después de la corrección:
 * <pre>
 *   List&lt;Long&gt; ids = new ArrayList&lt;&gt;(List.of(1L, 2L));
 *   dto.setHistorialConversionesId(ids);
 *   ids.clear(); // El DTO conserva su copia con [1, 2] sin verse afectado.
 * </pre>
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 2.1
 */
@Schema(description = "Datos de un usuario de la plataforma")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDTO {

	@Schema(accessMode = Schema.AccessMode.READ_ONLY,
			description = "ID generado automáticamente por la base de datos", example = "1")
	private Long id;

	@Schema(description = "Nombre del usuario", example = "Carlos")
	private String nombre;

	@Schema(description = "Apellido del usuario", example = "Rodríguez")
	private String apellido;

	@Schema(description = "Correo electrónico único", example = "carlos@correo.com")
	private String correo;

	@Schema(description = "Nombre de usuario único para iniciar sesión", example = "carlos.r")
	private String nombreUsuario;

	@Schema(description = "Contraseña (mínimo 8 caracteres, al menos una letra y un número)",
			example = "MiClave123")
	private String contrasena;

	@Schema(description = "Número de teléfono (10 dígitos comenzando con 3, Colombia)",
			example = "3001234567")
	private String telefono;

	@Schema(description = "Rol del usuario en el sistema",
			example = "USUARIO",
			allowableValues = { "USUARIO", "ADMIN" })
	private RolUsuario rol;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY,
			description = "IDs del historial de conversiones del usuario")
	private List<Long> historialConversionesId = new ArrayList<>();

	public UsuarioDTO() {
	}

	public UsuarioDTO(String nombreUsuario, String contrasena) {
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
	}

	public UsuarioDTO(String nombre, String apellido, String correo, String nombreUsuario,
			String contrasena, RolUsuario rol) {
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.rol = rol;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public RolUsuario getRol() {
		return rol;
	}

	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	/**
	 * Devuelve una copia defensiva de la lista de IDs del historial.
	 * <p>
	 * Se retorna una copia nueva para que el código externo no pueda modificar
	 * la lista interna del DTO directamente (corrección JAVA-E1086).
	 * </p>
	 *
	 * @return nueva lista con los mismos IDs del historial de conversiones.
	 */
	public List<Long> getHistorialConversionesId() {
		// Copia defensiva: el llamador recibe su propia lista independiente.
		return new ArrayList<>(historialConversionesId);
	}

	/**
	 * Asigna la lista de IDs del historial realizando una copia defensiva.
	 * <p>
	 * Se almacena una copia interna de la lista recibida para que
	 * modificaciones posteriores sobre la lista original no afecten el
	 * estado interno del DTO (corrección JAVA-E1086).
	 * Si se pasa {@code null}, se asigna una lista vacía.
	 * </p>
	 *
	 * @param historialConversionesId lista de IDs a asignar; puede ser {@code null}.
	 */
	public void setHistorialConversionesId(List<Long> historialConversionesId) {
		// Copia defensiva: se guarda una nueva lista para aislar el estado interno.
		this.historialConversionesId = (historialConversionesId != null)
				? new ArrayList<>(historialConversionesId)
				: new ArrayList<>();
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
		UsuarioDTO other = (UsuarioDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "UsuarioDTO [id=" + id + ", nombre=" + nombre + ", apellido=" + apellido
				+ ", correo=" + correo + ", nombreUsuario=" + nombreUsuario
				+ ", rol=" + rol + "]";
	}
}
