package co.edu.unbosque.proyectofinal.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidad que representa a un usuario dentro de la plataforma de
 * conversión de archivos.
 * <p>
 * Implementa {@link UserDetails} de Spring Security, lo que permite que
 * esta entidad sea utilizada directamente en el proceso de autenticación
 * y autorización vía JWT sin necesidad de un adaptador adicional.
 * </p>
 * <p>
 * Cada usuario tiene un único rol que determina sus permisos en el
 * sistema. Los usuarios con rol {@code USUARIO} pueden subir archivos
 * y solicitar conversiones, mientras que los usuarios con rol
 * {@code ADMIN} pueden además gestionar a los demás usuarios y consultar
 * el historial completo de conversiones de toda la plataforma.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 */
@Entity
@Table(name = "proyectofinal_usuario")
public class Usuario implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String apellido;

	@Column(unique = true, nullable = false)
	private String correo;

	@Column(unique = true, nullable = false, name = "nombre_usuario")
	private String nombreUsuario;

	@Column(nullable = false)
	private String contrasena;

	@Column(nullable = true, length = 20)
	private String telefono;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RolUsuario rol;

	@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL,
			fetch = FetchType.LAZY, orphanRemoval = true)
	private List<HistorialConversion> historialConversiones = new ArrayList<>();

	/**
	 * Constructor sin argumentos requerido por JPA para la instanciación
	 * de entidades al cargarlas desde la base de datos.
	 * Asigna el rol {@code USUARIO} por defecto.
	 */
	public Usuario() {
		this.rol = RolUsuario.USUARIO;
	}

	/**
	 * Crea un usuario con los datos principales. El historial de conversiones
	 * se inicializa como una lista vacía y el teléfono queda sin asignar.
	 *
	 * @param nombre        nombre del usuario
	 * @param apellido      apellido del usuario
	 * @param correo        correo electrónico único
	 * @param nombreUsuario nombre de usuario único para iniciar sesión
	 * @param contrasena    contraseña cifrada con BCrypt
	 * @param rol           rol asignado al usuario (USUARIO o ADMIN)
	 */
	public Usuario(String nombre, String apellido, String correo, String nombreUsuario,
			String contrasena, RolUsuario rol) {
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.rol = rol;
	}

	/**
	 * Retorna la autoridad (rol) asignada al usuario para Spring Security.
	 * El prefijo {@code ROLE_} es requerido por Spring Security para el
	 * reconocimiento de roles en las anotaciones de autorización.
	 *
	 * @return colección con la autoridad {@code ROLE_<NOMBRE_ROL>}
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
	}

	/**
	 * Retorna la contraseña cifrada del usuario para que Spring Security
	 * la compare contra la enviada en el login.
	 *
	 * @return contraseña cifrada con BCrypt
	 */
	@Override
	public String getPassword() {
		return contrasena;
	}

	/**
	 * Retorna el nombre de usuario único usado como identificador en el
	 * proceso de autenticación.
	 *
	 * @return nombre de usuario
	 */
	@Override
	public String getUsername() {
		return nombreUsuario;
	}

	/**
	 * Indica si la cuenta del usuario no ha expirado.
	 * Siempre retorna {@code true} ya que esta plataforma no implementa
	 * expiración de cuentas.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * Indica si la cuenta del usuario no está bloqueada.
	 * Siempre retorna {@code true} ya que esta plataforma no implementa
	 * bloqueo de cuentas.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * Indica si las credenciales del usuario no han expirado.
	 * Siempre retorna {@code true} ya que esta plataforma no implementa
	 * expiración de credenciales.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * Indica si la cuenta del usuario está habilitada.
	 * Siempre retorna {@code true} ya que esta plataforma no implementa
	 * deshabilitación de cuentas.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Retorna el identificador único del usuario.
	 *
	 * @return id generado por la base de datos
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Asigna el identificador único del usuario.
	 *
	 * @param id identificador generado por la base de datos
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Retorna el nombre del usuario.
	 *
	 * @return nombre del usuario
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Asigna el nombre del usuario.
	 *
	 * @param nombre nombre del usuario
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Retorna el apellido del usuario.
	 *
	 * @return apellido del usuario
	 */
	public String getApellido() {
		return apellido;
	}

	/**
	 * Asigna el apellido del usuario.
	 *
	 * @param apellido apellido del usuario
	 */
	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	/**
	 * Retorna el correo electrónico único del usuario.
	 *
	 * @return correo electrónico del usuario
	 */
	public String getCorreo() {
		return correo;
	}

	/**
	 * Asigna el correo electrónico único del usuario.
	 *
	 * @param correo correo electrónico del usuario
	 */
	public void setCorreo(String correo) {
		this.correo = correo;
	}

	/**
	 * Retorna el nombre de usuario único utilizado para iniciar sesión.
	 *
	 * @return nombre de usuario
	 */
	public String getNombreUsuario() {
		return nombreUsuario;
	}

	/**
	 * Asigna el nombre de usuario único utilizado para iniciar sesión.
	 *
	 * @param nombreUsuario nombre de usuario
	 */
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	/**
	 * Retorna la contraseña cifrada del usuario almacenada en la base de datos.
	 *
	 * @return contraseña cifrada con BCrypt
	 */
	public String getContrasena() {
		return contrasena;
	}

	/**
	 * Asigna la contraseña (ya cifrada con BCrypt) del usuario.
	 *
	 * @param contrasena contraseña cifrada con BCrypt
	 */
	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	/**
	 * Retorna el número de teléfono del usuario.
	 *
	 * @return número de teléfono, o {@code null} si no fue proporcionado
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * Asigna el número de teléfono del usuario.
	 *
	 * @param telefono número de teléfono (10 dígitos comenzando con 3, Colombia)
	 */
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	/**
	 * Retorna el rol asignado al usuario en el sistema.
	 *
	 * @return rol del usuario (USUARIO o ADMIN)
	 */
	public RolUsuario getRol() {
		return rol;
	}

	/**
	 * Asigna el rol del usuario en el sistema.
	 *
	 * @param rol rol del usuario (USUARIO o ADMIN)
	 */
	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	/**
	 * Retorna la lista de conversiones realizadas por el usuario.
	 * La relación se carga de forma perezosa ({@code LAZY}).
	 *
	 * @return lista de {@link HistorialConversion} del usuario
	 */
	public List<HistorialConversion> getHistorialConversiones() {
		return historialConversiones;
	}

	/**
	 * Asigna la lista de conversiones del usuario realizando una copia defensiva.
	 * <p>
	 * Se almacena una copia interna para que modificaciones posteriores sobre
	 * la lista recibida no afecten el estado interno de la entidad
	 * (corrección JAVA-E1086). Si se pasa {@code null}, se asigna una lista vacía.
	 * </p>
	 *
	 * @param historialConversiones lista de conversiones a asignar; puede ser {@code null}
	 */
	// FIX JAVA-E1086: se copia la lista recibida para evitar que referencias
	// externas puedan modificar el estado interno de la entidad.
	public void setHistorialConversiones(List<HistorialConversion> historialConversiones) {
		this.historialConversiones = historialConversiones != null
				? new ArrayList<>(historialConversiones)
				: new ArrayList<>();
	}

	/**
	 * Retorna un código hash basado únicamente en el {@code id} del usuario.
	 *
	 * @return código hash del usuario
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Compara esta entidad con otro objeto. Dos instancias de {@code Usuario}
	 * son iguales si tienen el mismo {@code id}.
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
		Usuario other = (Usuario) obj;
		return Objects.equals(id, other.id);
	}

	/**
	 * Retorna una representación en cadena de la entidad con los campos principales.
	 * La contraseña no se incluye por seguridad.
	 *
	 * @return cadena con los valores de la entidad
	 */
	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nombre=" + nombre + ", apellido=" + apellido
				+ ", correo=" + correo + ", nombreUsuario=" + nombreUsuario
				+ ", rol=" + rol + "]";
	}
}