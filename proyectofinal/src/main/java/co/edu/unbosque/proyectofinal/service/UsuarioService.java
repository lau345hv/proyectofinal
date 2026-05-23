package co.edu.unbosque.proyectofinal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectofinal.dto.UsuarioDTO;
import co.edu.unbosque.proyectofinal.entity.HistorialConversion;
import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.exception.ContrasenaInvalidaException;
import co.edu.unbosque.proyectofinal.exception.CorreoDuplicadoException;
import co.edu.unbosque.proyectofinal.exception.DatoInvalidoException;
import co.edu.unbosque.proyectofinal.exception.FormatoCorreoInvalidoException;
import co.edu.unbosque.proyectofinal.exception.IdInvalidoException;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.exception.NombreUsuarioDuplicadoException;
import co.edu.unbosque.proyectofinal.exception.NumeroNegativoException;
import co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException;
import co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectofinal.exception.TextoDemasiadoLargoException;
import co.edu.unbosque.proyectofinal.exception.TextoVacioException;
import co.edu.unbosque.proyectofinal.repository.UsuarioRepository;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;

/**
 * Servicio encargado de gestionar los usuarios de la plataforma de
 * conversión de archivos.
 * <p>
 * Provee operaciones CRUD, búsquedas personalizadas y validación
 * exhaustiva de los datos de entrada, lanzando excepciones tipadas
 * mediante {@link LanzadorDeExcepcion} para retornar mensajes claros
 * al cliente HTTP. Todas las contraseñas se cifran con BCrypt antes de
 * persistirse en la base de datos.
 * </p>
 * <p>
 * La autenticación (login) ya no reside en este servicio: es
 * responsabilidad del {@code AuthController} usando el
 * {@code AuthenticationManager} de Spring Security.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 */
@Service
@Transactional
public class UsuarioService implements CRUDoperation<UsuarioDTO> {

	private static final String OPCIONES_ROL = "USUARIO, ADMIN";

	private static final int LONGITUD_MAXIMA_NOMBRE = 100;
	private static final int LONGITUD_MAXIMA_CORREO = 150;
	private static final int LONGITUD_MAXIMA_USUARIO = 50;
	private static final int LONGITUD_MINIMA_CONTRASENA = 8;
	private static final int LONGITUD_MAXIMA_CONTRASENA = 100;

	private static final java.util.regex.Pattern PATRON_NOMBRE =
			java.util.regex.Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$");

	private static final java.util.regex.Pattern PATRON_TELEFONO =
			java.util.regex.Pattern.compile("^3\\d{9}$");

	@Autowired
	private UsuarioRepository repo;

	@Autowired
	private LanzadorDeExcepcion lanzador;

	@Autowired
	private EmailValidacionService emailValidacionService;

	@Autowired
	private VerificacionCorreoService verificacionCorreoService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public UsuarioService() {
	}

	@Override
	public int create(UsuarioDTO data) {
		try {
			validarCamposBasicos(data, true);
			if (data.getNombreUsuario() != null
					&& "admin".equalsIgnoreCase(data.getNombreUsuario().trim())) {
				lanzador.lanzarUsuarioSinPermiso(
						"El nombre de usuario 'admin' está reservado para la cuenta de administrador "
								+ "y no puede ser utilizado al registrarse.");
			}
			validarUnicidad(data, null);

			RolUsuario rol = data.getRol() != null ? data.getRol() : RolUsuario.USUARIO;

			Usuario entity = new Usuario(
					data.getNombre().trim(),
					data.getApellido().trim(),
					data.getCorreo().trim(),
					data.getNombreUsuario().trim(),
					passwordEncoder.encode(data.getContrasena()),
					rol);
			entity.setTelefono(data.getTelefono());
			repo.save(entity);
		} catch (TextoVacioException | TextoDemasiadoLargoException
				| FormatoCorreoInvalidoException | OpcionNoValidaException
				| CorreoDuplicadoException | NombreUsuarioDuplicadoException
				| NumeroNegativoException
				| ContrasenaInvalidaException | DatoInvalidoException
				| RecursoNoEncontradoException | IdInvalidoException
				| co.edu.unbosque.proyectofinal.exception.UsuarioSinPermisoException
				| co.edu.unbosque.proyectofinal.exception.TelefonoInvalidoException
				| co.edu.unbosque.proyectofinal.exception.NombreInvalidoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al crear usuario: " + e.getMessage());
		}
		return 0;
	}

	/**
	 * Solicita un código de verificación para un correo, validando primero
	 * que el correo tenga formato válido, exista realmente (consulta API
	 * externa) y no esté ya registrado en el sistema.
	 *
	 * @param correo correo electrónico a verificar
	 */
	public void solicitarCodigoRegistro(String correo) {
		if (correo == null || correo.isBlank()) {
			lanzador.lanzarTextoVacio("correo");
		}
		if (!esCorreoValido(correo)) {
			lanzador.lanzarFormatoCorreoInvalido(correo);
		}
		Optional<Usuario> existente = repo.findByCorreo(correo.trim());
		if (existente.isPresent()) {
			lanzador.lanzarCorreoDuplicado(
					"Ya existe una cuenta registrada con el correo: " + correo);
		}
		emailValidacionService.validarExistenciaCorreo(correo);
		verificacionCorreoService.solicitarCodigo(correo);
	}

	/**
	 * Crea un usuario nuevo después de validar el código de verificación
	 * que se le envió por correo electrónico.
	 *
	 * @param data datos del usuario a crear
	 * @param codigoVerificacion código de 6 dígitos recibido por correo
	 */
	public void crearConVerificacion(UsuarioDTO data, String codigoVerificacion) {
		if (data == null || data.getCorreo() == null) {
			lanzador.lanzarDatoInvalido("Los datos del usuario son obligatorios.");
		}
		verificacionCorreoService.validarCodigo(data.getCorreo(), codigoVerificacion);
		create(data);
	}

	@Override
	public List<UsuarioDTO> getAll() {
		List<Usuario> lista = (List<Usuario>) repo.findAll();
		List<UsuarioDTO> dtoList = new ArrayList<>();
		lista.forEach(entity -> dtoList.add(mapToDTO(entity)));
		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		try {
			if (id == null || id <= 0) {
				lanzador.lanzarIdInvalido("usuario", id);
			}
			Optional<Usuario> encontrado = repo.findById(id);
			if (encontrado.isPresent()) {
				Usuario u = encontrado.get();
				if ("admin".equalsIgnoreCase(u.getNombreUsuario())) {
					lanzador.lanzarUsuarioSinPermiso(
							"La cuenta de administrador no puede ser eliminada.");
				}
				repo.delete(u);
				return 0;
			}
			lanzador.lanzarRecursoNoEncontrado("No existe un usuario con el ID: " + id);
		} catch (IdInvalidoException | RecursoNoEncontradoException
				| co.edu.unbosque.proyectofinal.exception.UsuarioSinPermisoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al eliminar usuario: " + e.getMessage());
		}
		return 1;
	}

	@Override
	public int updateById(Long id, UsuarioDTO data) {
		try {
			if (id == null || id <= 0) {
				lanzador.lanzarIdInvalido("usuario", id);
			}
			Optional<Usuario> encontrado = repo.findById(id);
			if (!encontrado.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado("No existe un usuario con el ID: " + id);
			}
			Usuario entity = encontrado.get();
			if ("admin".equalsIgnoreCase(entity.getNombreUsuario())) {
				lanzador.lanzarUsuarioSinPermiso(
						"La cuenta de administrador no puede ser modificada.");
			}
			boolean contrasenaSeActualiza = data.getContrasena() != null
					&& !data.getContrasena().isBlank();
			validarCamposBasicos(data, contrasenaSeActualiza);
			validarUnicidad(data, id);
			entity.setNombre(data.getNombre().trim());
			entity.setApellido(data.getApellido().trim());
			entity.setCorreo(data.getCorreo().trim());
			entity.setNombreUsuario(data.getNombreUsuario().trim());
			if (contrasenaSeActualiza) {
				entity.setContrasena(passwordEncoder.encode(data.getContrasena()));
			}
			entity.setTelefono(data.getTelefono());
			if (data.getRol() != null) {
				entity.setRol(data.getRol());
			}
			repo.save(entity);
		} catch (IdInvalidoException | RecursoNoEncontradoException
				| TextoVacioException | TextoDemasiadoLargoException
				| FormatoCorreoInvalidoException | OpcionNoValidaException
				| CorreoDuplicadoException | NombreUsuarioDuplicadoException
				| NumeroNegativoException
				| ContrasenaInvalidaException | DatoInvalidoException
				| co.edu.unbosque.proyectofinal.exception.UsuarioSinPermisoException
				| co.edu.unbosque.proyectofinal.exception.TelefonoInvalidoException
				| co.edu.unbosque.proyectofinal.exception.NombreInvalidoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al actualizar usuario: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public long count() {
		return repo.count();
	}

	@Override
	public boolean exist(Long id) {
		return id != null && repo.existsById(id);
	}

	/**
	 * Verifica si existe un usuario con el nombre de usuario dado.
	 *
	 * @param nombreUsuario nombre de usuario a buscar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 */
	public boolean existsByNombreUsuario(String nombreUsuario) {
		if (nombreUsuario == null || nombreUsuario.isBlank()) {
			return false;
		}
		return repo.findByNombreUsuario(nombreUsuario.trim()).isPresent();
	}

	/**
	 * Verifica si existe un usuario con el correo dado.
	 *
	 * @param correo correo a buscar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 */
	public boolean existsByCorreo(String correo) {
		if (correo == null || correo.isBlank()) {
			return false;
		}
		return repo.findByCorreo(correo.trim()).isPresent();
	}

	/**
	 * Busca un usuario por su ID.
	 *
	 * @param id ID del usuario.
	 * @return DTO del usuario encontrado.
	 */
	public UsuarioDTO findById(Long id) {
		try {
			if (id == null || id <= 0) {
				lanzador.lanzarIdInvalido("usuario", id);
			}
			Optional<Usuario> encontrado = repo.findById(id);
			if (!encontrado.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado("No existe un usuario con el ID: " + id);
			}
			return mapToDTO(encontrado.get());
		} catch (IdInvalidoException | RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al buscar por ID: " + e.getMessage());
		}
	}

	/**
	 * Busca un usuario por su correo electrónico.
	 *
	 * @param correo Correo a buscar.
	 * @return DTO del usuario encontrado.
	 */
	public UsuarioDTO findByCorreo(String correo) {
		try {
			if (correo == null || correo.isBlank()) {
				lanzador.lanzarTextoVacio("correo");
			}
			if (!esCorreoValido(correo)) {
				lanzador.lanzarFormatoCorreoInvalido(correo);
			}
			Optional<Usuario> encontrado = repo.findByCorreo(correo.trim());
			if (!encontrado.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado("No existe un usuario con el correo: " + correo);
			}
			return mapToDTO(encontrado.get());
		} catch (TextoVacioException | FormatoCorreoInvalidoException
				| RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al buscar por correo: " + e.getMessage());
		}
	}

	/**
	 * Busca un usuario por su nombre de usuario único.
	 *
	 * @param nombreUsuario Nombre de usuario a buscar.
	 * @return DTO del usuario encontrado.
	 */
	public UsuarioDTO findByNombreUsuario(String nombreUsuario) {
		try {
			if (nombreUsuario == null || nombreUsuario.isBlank()) {
				lanzador.lanzarTextoVacio("nombreUsuario");
			}
			Optional<Usuario> encontrado = repo.findByNombreUsuario(nombreUsuario.trim());
			if (!encontrado.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado(
						"No existe un usuario con el nombre de usuario: " + nombreUsuario);
			}
			return mapToDTO(encontrado.get());
		} catch (TextoVacioException | RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar por nombre de usuario: " + e.getMessage());
		}
	}

	/**
	 * Busca usuarios por su nombre.
	 *
	 * @param nombre Nombre a buscar.
	 * @return Lista de DTOs encontrados.
	 */
	public List<UsuarioDTO> findByNombre(String nombre) {
		try {
			if (nombre == null || nombre.isBlank()) {
				lanzador.lanzarTextoVacio("nombre");
			}
			Optional<List<Usuario>> encontrados = repo.findByNombre(nombre.trim());
			List<UsuarioDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (TextoVacioException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al buscar por nombre: " + e.getMessage());
		}
	}

	/**
	 * Busca usuarios por su apellido.
	 *
	 * @param apellido Apellido a buscar.
	 * @return Lista de DTOs encontrados.
	 */
	public List<UsuarioDTO> findByApellido(String apellido) {
		try {
			if (apellido == null || apellido.isBlank()) {
				lanzador.lanzarTextoVacio("apellido");
			}
			Optional<List<Usuario>> encontrados = repo.findByApellido(apellido.trim());
			List<UsuarioDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (TextoVacioException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al buscar por apellido: " + e.getMessage());
		}
	}

	/**
	 * Busca usuarios que tengan un rol específico.
	 *
	 * @param rol Rol a buscar.
	 * @return Lista de DTOs encontrados.
	 */
	public List<UsuarioDTO> findByRol(RolUsuario rol) {
		try {
			if (rol == null) {
				lanzador.lanzarOpcionNoValida("rol", "null", OPCIONES_ROL);
			}
			Optional<List<Usuario>> encontrados = repo.findByRol(rol);
			List<UsuarioDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (OpcionNoValidaException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al buscar por rol: " + e.getMessage());
		}
	}

	// =====================================================================
	// Métodos privados de validación
	// =====================================================================

	/**
	 * Valida todos los campos básicos del DTO de usuario.
	 * FIX JAVA-R1000: la validación se dividió en métodos privados por campo
	 * para reducir la complejidad ciclomática de 27 a menos de 10.
	 *
	 * @param data              DTO con los datos del usuario.
	 * @param validarContrasena indica si se debe validar la contraseña.
	 */
	private void validarCamposBasicos(UsuarioDTO data, boolean validarContrasena) {
		if (data == null) {
			lanzador.lanzarDatoInvalido("Los datos del usuario no pueden ser nulos.");
		}
		validarNombre(data.getNombre());
		validarApellido(data.getApellido());
		validarCorreo(data.getCorreo());
		validarNombreUsuario(data.getNombreUsuario());
		if (validarContrasena) {
			validarContrasena(data.getContrasena());
		}
		validarTelefono(data.getTelefono());
	}

	/**
	 * Valida el campo nombre.
	 *
	 * @param nombre valor a validar.
	 */
	private void validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			lanzador.lanzarTextoVacio("nombre");
		}
		if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
			lanzador.lanzarTextoDemasiadoLargo("nombre", nombre.length(),
					LONGITUD_MAXIMA_NOMBRE);
		}
		if (!PATRON_NOMBRE.matcher(nombre.trim()).matches()) {
			lanzador.lanzarNombreInvalido(
					"El nombre solo puede contener letras y espacios. "
							+ "No se permiten números ni caracteres especiales. "
							+ "Recibido: '" + nombre + "'");
		}
	}

	/**
	 * Valida el campo apellido.
	 *
	 * @param apellido valor a validar.
	 */
	private void validarApellido(String apellido) {
		if (apellido == null || apellido.isBlank()) {
			lanzador.lanzarTextoVacio("apellido");
		}
		if (apellido.length() > LONGITUD_MAXIMA_NOMBRE) {
			lanzador.lanzarTextoDemasiadoLargo("apellido", apellido.length(),
					LONGITUD_MAXIMA_NOMBRE);
		}
		if (!PATRON_NOMBRE.matcher(apellido.trim()).matches()) {
			lanzador.lanzarNombreInvalido(
					"El apellido solo puede contener letras y espacios. "
							+ "No se permiten números ni caracteres especiales. "
							+ "Recibido: '" + apellido + "'");
		}
	}

	/**
	 * Valida el campo correo.
	 *
	 * @param correo valor a validar.
	 */
	private void validarCorreo(String correo) {
		if (correo == null || correo.isBlank()) {
			lanzador.lanzarTextoVacio("correo");
		}
		if (correo.length() > LONGITUD_MAXIMA_CORREO) {
			lanzador.lanzarTextoDemasiadoLargo("correo", correo.length(),
					LONGITUD_MAXIMA_CORREO);
		}
		if (!esCorreoValido(correo)) {
			lanzador.lanzarFormatoCorreoInvalido(correo);
		}
	}

	/**
	 * Valida el campo nombreUsuario.
	 *
	 * @param nombreUsuario valor a validar.
	 */
	private void validarNombreUsuario(String nombreUsuario) {
		if (nombreUsuario == null || nombreUsuario.isBlank()) {
			lanzador.lanzarTextoVacio("nombreUsuario");
		}
		if (nombreUsuario.length() > LONGITUD_MAXIMA_USUARIO) {
			lanzador.lanzarTextoDemasiadoLargo("nombreUsuario",
					nombreUsuario.length(), LONGITUD_MAXIMA_USUARIO);
		}
	}

	/**
	 * Valida el campo contrasena.
	 *
	 * @param contrasena valor a validar.
	 */
	private void validarContrasena(String contrasena) {
		if (contrasena == null || contrasena.isBlank()) {
			lanzador.lanzarTextoVacio("contrasena");
		}
		if (contrasena.length() > LONGITUD_MAXIMA_CONTRASENA) {
			lanzador.lanzarTextoDemasiadoLargo("contrasena",
					contrasena.length(), LONGITUD_MAXIMA_CONTRASENA);
		}
		if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA
				|| !contrasena.matches(".*[a-zA-Z].*")
				|| !contrasena.matches(".*[0-9].*")) {
			lanzador.lanzarContrasenaInvalida();
		}
	}

	/**
	 * Valida el campo teléfono.
	 *
	 * @param telefono valor a validar.
	 */
	private void validarTelefono(String telefono) {
		if (telefono == null || telefono.isBlank()) {
			lanzador.lanzarTelefonoInvalido(
					"El teléfono es obligatorio. Debe ser un número colombiano "
							+ "de 10 dígitos que comience con 3 (sin el +57).");
		}
		String telefonoLimpio = telefono.trim().replace(" ", "");
		if (!PATRON_TELEFONO.matcher(telefonoLimpio).matches()) {
			lanzador.lanzarTelefonoInvalido(
					"El teléfono debe ser un número colombiano de 10 dígitos "
							+ "que comience con 3 (ej: 3001234567). Sin espacios, "
							+ "guiones ni el prefijo +57. Recibido: '"
							+ telefono + "'");
		}
	}

	private void validarUnicidad(UsuarioDTO data, Long idActual) {
		Optional<Usuario> porCorreo = repo.findByCorreo(data.getCorreo().trim());
		if (porCorreo.isPresent()
				&& (idActual == null || !porCorreo.get().getId().equals(idActual))) {
			lanzador.lanzarCorreoDuplicado(
					"Ya existe un usuario registrado con el correo: " + data.getCorreo());
		}
		Optional<Usuario> porNombreUsuario = repo.findByNombreUsuario(data.getNombreUsuario().trim());
		if (porNombreUsuario.isPresent()
				&& (idActual == null || !porNombreUsuario.get().getId().equals(idActual))) {
			lanzador.lanzarNombreUsuarioDuplicado(
					"Ya existe un usuario registrado con el nombre de usuario: "
							+ data.getNombreUsuario());
		}
	}

	private static boolean esCorreoValido(String correo) {
		if (correo == null) {
			return false;
		}
		return correo.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
	}


	private UsuarioDTO mapToDTO(Usuario entity) {
		UsuarioDTO dto = new UsuarioDTO();
		dto.setId(entity.getId());
		dto.setNombre(entity.getNombre());
		dto.setApellido(entity.getApellido());
		dto.setCorreo(entity.getCorreo());
		dto.setNombreUsuario(entity.getNombreUsuario());
		dto.setTelefono(entity.getTelefono());
		dto.setRol(entity.getRol());
		dto.setContrasena(null);
		List<Long> ids = new ArrayList<>();
		if (entity.getHistorialConversiones() != null) {
			for (HistorialConversion h : entity.getHistorialConversiones()) {
				ids.add(h.getId());
			}
		}
		dto.setHistorialConversionesId(ids);
		return dto;
	}
}