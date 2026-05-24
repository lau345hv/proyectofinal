package co.edu.unbosque.proyectofinal.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.proyectofinal.repository.UsuarioRepository;

/**
 * Implementación de {@link UserDetailsService} que carga los detalles de
 * un usuario desde la base de datos durante el proceso de autenticación
 * de Spring Security.
 * <p>
 * Spring Security llama a {@link #loadUserByUsername(String)} al intentar
 * autenticar a un usuario. Esta implementación busca el usuario en la
 * base de datos usando {@link UsuarioRepository} y retorna la entidad
 * {@code Usuario} directamente, ya que ésta implementa {@link UserDetails}.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	/**
	 * Carga un usuario por su nombre de usuario para el proceso de
	 * autenticación.
	 *
	 * @param nombreUsuario nombre de usuario a buscar
	 * @return los detalles del usuario encontrado
	 * @throws UsernameNotFoundException si no existe ningún usuario con
	 *                                   ese nombre
	 */
	@Override
	public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
		return usuarioRepository.findByNombreUsuario(nombreUsuario)
				.orElseThrow(() -> new UsernameNotFoundException(
						"No existe un usuario con el nombre de usuario: " + nombreUsuario));
	}
}