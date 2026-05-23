package co.edu.unbosque.proyectofinal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;

/**
 * Repositorio JPA para la entidad {@link Usuario}.
 * <p>
 * Hereda las operaciones básicas CRUD de {@link CrudRepository} y
 * declara los métodos derivados específicos del proyecto, siguiendo
 * la convención de nombres de Spring Data.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 */
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

	public Optional<Usuario> findByCorreo(String correo);

	public Optional<Usuario> findByNombreUsuario(String nombreUsuario);

	public Optional<List<Usuario>> findByNombre(String nombre);

	public Optional<List<Usuario>> findByApellido(String apellido);

	public Optional<List<Usuario>> findByRol(RolUsuario rol);
}