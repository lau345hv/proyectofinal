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
 * @author Equipo de desarrollo
 * @version 2.0
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

	public List<Long> getHistorialConversionesId() {
		return historialConversionesId;
	}

	public void setHistorialConversionesId(List<Long> historialConversionesId) {
		this.historialConversionesId = historialConversionesId;
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