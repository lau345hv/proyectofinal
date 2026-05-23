package co.edu.unbosque.proyectofinal.configuration;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.repository.UsuarioRepository;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación.
 * <p>
 * Se asegura de que la cuenta de administrador exista siempre en el
 * sistema. Las credenciales del administrador se leen de
 * {@code application.properties} bajo las claves {@code app.admin.*}.
 * </p>
 * <p>
 * Si la cuenta admin ya existe, no se toca para no sobrescribir cambios
 * que el propio admin haya hecho. Si no existe, se crea con la contraseña
 * cifrada con BCrypt.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 */
@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	@Value("${app.admin.nombre}")
	private String adminNombre;

	@Value("${app.admin.apellido}")
	private String adminApellido;

	@Value("${app.admin.correo}")
	private String adminCorreo;

	@Value("${app.admin.nombre-usuario}")
	private String adminNombreUsuario;

	@Value("${app.admin.contrasena}")
	private String adminContrasena;

	@Value("${app.admin.telefono}")
	private String adminTelefono;

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		Optional<Usuario> existente = usuarioRepository.findByNombreUsuario(adminNombreUsuario);
		if (existente.isPresent()) {
			log.info("La cuenta de administrador '{}' ya existe, omitiendo la creación.",
					adminNombreUsuario);
			return;
		}
		Usuario admin = new Usuario();
		admin.setNombre(adminNombre);
		admin.setApellido(adminApellido);
		admin.setCorreo(adminCorreo);
		admin.setNombreUsuario(adminNombreUsuario);
		admin.setContrasena(passwordEncoder.encode(adminContrasena));
		admin.setTelefono(adminTelefono);
		admin.setRol(RolUsuario.ADMIN);
		usuarioRepository.save(admin);
		log.info("Cuenta de administrador '{}' creada exitosamente.", adminNombreUsuario);
	}
}