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

	public Usuario() {
		this.rol = RolUsuario.USUARIO;
	}

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

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
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

	public List<HistorialConversion> getHistorialConversiones() {
		return historialConversiones;
	}

	public void setHistorialConversiones(List<HistorialConversion> historialConversiones) {
		this.historialConversiones = historialConversiones;
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
		Usuario other = (Usuario) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nombre=" + nombre + ", apellido=" + apellido
				+ ", correo=" + correo + ", nombreUsuario=" + nombreUsuario
				+ ", rol=" + rol + "]";
	}
}