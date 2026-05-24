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

	/**
	 * Busca un usuario por su correo electrónico.
	 *
	 * @param correo correo electrónico único del usuario
	 * @return {@code Optional} con el usuario encontrado, o vacío si no existe
	 */
	public Optional<Usuario> findByCorreo(String correo);

	/**
	 * Busca un usuario por su nombre de usuario único.
	 *
	 * @param nombreUsuario nombre de usuario único utilizado para iniciar sesión
	 * @return {@code Optional} con el usuario encontrado, o vacío si no existe
	 */
	public Optional<Usuario> findByNombreUsuario(String nombreUsuario);

	/**
	 * Busca todos los usuarios que tengan un nombre exacto.
	 *
	 * @param nombre nombre del usuario a filtrar
	 * @return {@code Optional} con la lista de usuarios encontrados,
	 *         o vacío si no existe ninguno
	 */
	public Optional<List<Usuario>> findByNombre(String nombre);

	/**
	 * Busca todos los usuarios que tengan un apellido exacto.
	 *
	 * @param apellido apellido del usuario a filtrar
	 * @return {@code Optional} con la lista de usuarios encontrados,
	 *         o vacío si no existe ninguno
	 */
	public Optional<List<Usuario>> findByApellido(String apellido);

	/**
	 * Busca todos los usuarios que tengan un rol específico.
	 *
	 * @param rol rol del usuario a filtrar (USUARIO o ADMIN)
	 * @return {@code Optional} con la lista de usuarios encontrados,
	 *         o vacío si no existe ninguno
	 */
	public Optional<List<Usuario>> findByRol(RolUsuario rol);
}