package co.edu.unbosque.proyectofinal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectofinal.dto.AuditoriaDTO;
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
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;

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
 *
 * @author Equipo de desarrollo
 * @version 3.1
 */
@Service
@Transactional
public class UsuarioService implements CRUDoperation<UsuarioDTO> {

	/** Opciones de rol válidas para mensajes de error. */
	private static final String OPCIONES_ROL = "USUARIO, ADMIN";

	/** Longitud máxima permitida para nombre y apellido. */
	private static final int LONGITUD_MAXIMA_NOMBRE = 100;

	/** Longitud máxima permitida para el correo electrónico. */
	private static final int LONGITUD_MAXIMA_CORREO = 150;

	/** Longitud máxima permitida para el nombre de usuario. */
	private static final int LONGITUD_MAXIMA_USUARIO = 50;

	/** Longitud mínima requerida para la contraseña. */
	private static final int LONGITUD_MINIMA_CONTRASENA = 8;

	/** Longitud máxima permitida para la contraseña. */
	private static final int LONGITUD_MAXIMA_CONTRASENA = 100;

	/**
	 * Patrón que permite únicamente letras (incluyendo tildes y ñ) y espacios.
	 * Se usa para validar nombre y apellido.
	 */
	private static final java.util.regex.Pattern PATRON_NOMBRE =
			java.util.regex.Pattern.compile("^[A-ZÁÉÍÓÚÑÜ][A-Za-zÁÉÍÓÚáéíóúÑñÜü6 ]*$");

	/**
	 * Patrón para teléfonos colombianos de 10 dígitos que comienzan con 3.
	 */
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

	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Constructor sin argumentos requerido por Spring para la inyección
	 * de dependencias.
	 */
	public UsuarioService() {
	}

	/**
	 * Crea y persiste un nuevo usuario en la base de datos.
	 * <p>
	 * Valida todos los campos del DTO, prohíbe el nombre de usuario
	 * reservado {@code admin}, verifica la unicidad de correo y nombre
	 * de usuario, y cifra la contraseña con BCrypt antes de persistir.
	 * Si no se indica rol, se asigna {@code USUARIO} por defecto.
	 * </p>
	 *
	 * @param data DTO con los datos del usuario a crear
	 * @return 0 si la operación fue exitosa
	 * @throws TextoVacioException             si algún campo obligatorio está vacío
	 * @throws TextoDemasiadoLargoException    si algún campo excede su longitud máxima
	 * @throws FormatoCorreoInvalidoException  si el correo no tiene formato válido
	 * @throws ContrasenaInvalidaException     si la contraseña no cumple los requisitos
	 * @throws CorreoDuplicadoException        si el correo ya está registrado
	 * @throws NombreUsuarioDuplicadoException si el nombre de usuario ya está registrado
	 * @throws co.edu.unbosque.proyectofinal.exception.UsuarioSinPermisoException
	 *         si se intenta usar el nombre de usuario reservado {@code admin}
	 */
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
	 * que el correo tenga formato válido, exista realmente y no esté ya
	 * registrado en el sistema.
	 * <p>
	 * El código se envía por correo si Brevo está configurado, y siempre
	 * se imprime en la consola del servidor para facilitar las pruebas.
	 * </p>
	 *
	 * @param correo dirección de correo electrónico para la que se solicita el código
	 * @throws TextoVacioException            si el correo está vacío o es nulo
	 * @throws FormatoCorreoInvalidoException si el correo no tiene formato válido
	 * @throws CorreoDuplicadoException       si el correo ya tiene una cuenta registrada
	 * @throws co.edu.unbosque.proyectofinal.exception.CorreoNoExisteException
	 *         si el dominio del correo no existe o es desechable
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
	 * <p>
	 * Si el código es válido y no ha expirado, delega la creación en
	 * {@link #create(UsuarioDTO)}.
	 * </p>
	 *
	 * @param data               DTO con los datos del usuario a crear
	 * @param codigoVerificacion código de 6 dígitos recibido por correo
	 * @throws DatoInvalidoException si el DTO o el correo son nulos
	 * @throws co.edu.unbosque.proyectofinal.exception.CodigoVerificacionInvalidoException
	 *         si el código no coincide o ha expirado
	 */
	public void crearConVerificacion(UsuarioDTO data, String codigoVerificacion) {
		if (data == null || data.getCorreo() == null) {
			lanzador.lanzarDatoInvalido("Los datos del usuario son obligatorios.");
		}
		verificacionCorreoService.validarCodigo(data.getCorreo(), codigoVerificacion);
		create(data);
	}

	/**
	 * Retorna todos los usuarios registrados en la base de datos y registra
	 * la consulta en la auditoría del sistema.
	 *
	 * @return lista de todos los usuarios como DTOs (sin contraseña)
	 */
	@Override
	public List<UsuarioDTO> getAll() {
		List<Usuario> lista = (List<Usuario>) repo.findAll();
		List<UsuarioDTO> dtoList = new ArrayList<>();
		lista.forEach(entity -> dtoList.add(mapToDTO(entity)));

		AuditoriaDTO auditoria = auditoriaService.preAccion(TipoAccion.READ,
				"Consulta de todos los usuarios del sistema");
		auditoriaService.create(auditoria);

		return dtoList;
	}

	/**
	 * Elimina el usuario identificado por el ID proporcionado.
	 * <p>
	 * La cuenta de administrador ({@code admin}) no puede ser eliminada.
	 * La operación queda registrada en la auditoría antes de ejecutarse.
	 * </p>
	 *
	 * @param id identificador del usuario a eliminar
	 * @return 0 si la operación fue exitosa
	 * @throws IdInvalidoException    si el ID es nulo o menor o igual a cero
	 * @throws RecursoNoEncontradoException si no existe un usuario con ese ID
	 * @throws co.edu.unbosque.proyectofinal.exception.UsuarioSinPermisoException
	 *         si se intenta eliminar la cuenta de administrador
	 */
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

				AuditoriaDTO auditoria = auditoriaService.preAccion(TipoAccion.DELETE,
						"Usuario eliminado: " + u.getNombreUsuario()
								+ " (id=" + u.getId() + ", correo=" + u.getCorreo() + ")");
				auditoriaService.create(auditoria);

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

	/**
	 * Actualiza los datos del usuario identificado por el ID proporcionado.
	 * <p>
	 * La cuenta de administrador ({@code admin}) no puede ser modificada.
	 * La contraseña solo se actualiza si viene con valor en el DTO.
	 * La operación queda registrada en la auditoría al finalizar.
	 * </p>
	 *
	 * @param id   identificador del usuario a actualizar
	 * @param data DTO con los nuevos valores
	 * @return 0 si la operación fue exitosa
	 * @throws IdInvalidoException             si el ID es nulo o menor o igual a cero
	 * @throws RecursoNoEncontradoException    si no existe un usuario con ese ID
	 * @throws TextoVacioException             si algún campo obligatorio está vacío
	 * @throws TextoDemasiadoLargoException    si algún campo excede su longitud máxima
	 * @throws FormatoCorreoInvalidoException  si el correo no tiene formato válido
	 * @throws ContrasenaInvalidaException     si la nueva contraseña no cumple los requisitos
	 * @throws CorreoDuplicadoException        si el nuevo correo ya pertenece a otro usuario
	 * @throws NombreUsuarioDuplicadoException si el nuevo nombre de usuario ya está en uso
	 * @throws co.edu.unbosque.proyectofinal.exception.UsuarioSinPermisoException
	 *         si se intenta modificar la cuenta de administrador
	 */
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

			
			AuditoriaDTO auditoria = auditoriaService.preAccion(TipoAccion.UPDATE,
					"Perfil actualizado para usuario: " + entity.getNombreUsuario()
							+ " (id=" + id + ")");
			auditoriaService.create(auditoria);

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

	/**
	 * Retorna el número total de usuarios registrados en la base de datos.
	 *
	 * @return cantidad total de usuarios
	 */
	@Override
	public long count() {
		return repo.count();
	}

	/**
	 * Verifica si existe un usuario con el ID proporcionado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si el usuario existe, {@code false} en caso contrario
	 */
	@Override
	public boolean exist(Long id) {
		return id != null && repo.existsById(id);
	}

	/**
	 * Verifica si existe un usuario con el nombre de usuario indicado.
	 *
	 * @param nombreUsuario nombre de usuario a verificar
	 * @return {@code true} si existe, {@code false} si no existe o el valor está vacío
	 */
	public boolean existsByNombreUsuario(String nombreUsuario) {
		if (nombreUsuario == null || nombreUsuario.isBlank()) {
			return false;
		}
		return repo.findByNombreUsuario(nombreUsuario.trim()).isPresent();
	}

	/**
	 * Verifica si existe un usuario con el correo electrónico indicado.
	 *
	 * @param correo correo electrónico a verificar
	 * @return {@code true} si existe, {@code false} si no existe o el valor está vacío
	 */
	public boolean existsByCorreo(String correo) {
		if (correo == null || correo.isBlank()) {
			return false;
		}
		return repo.findByCorreo(correo.trim()).isPresent();
	}

	/**
	 * Busca un usuario por su ID y lo retorna como DTO.
	 *
	 * @param id identificador del usuario
	 * @return DTO del usuario encontrado (sin contraseña)
	 * @throws IdInvalidoException          si el ID es nulo o menor o igual a cero
	 * @throws RecursoNoEncontradoException si no existe un usuario con ese ID
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
	 * Busca un usuario por su correo electrónico y lo retorna como DTO.
	 *
	 * @param correo correo electrónico del usuario
	 * @return DTO del usuario encontrado (sin contraseña)
	 * @throws TextoVacioException            si el correo está vacío o es nulo
	 * @throws FormatoCorreoInvalidoException si el correo no tiene formato válido
	 * @throws RecursoNoEncontradoException   si no existe un usuario con ese correo
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
				lanzador.lanzarRecursoNoEncontrado(
						"No existe un usuario con el correo: " + correo);
			}
			return mapToDTO(encontrado.get());
		} catch (TextoVacioException | FormatoCorreoInvalidoException
				| RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar por correo: " + e.getMessage());
		}
	}

	/**
	 * Busca un usuario por su nombre de usuario único y lo retorna como DTO.
	 *
	 * @param nombreUsuario nombre de usuario a buscar
	 * @return DTO del usuario encontrado (sin contraseña)
	 * @throws TextoVacioException          si el nombre de usuario está vacío o es nulo
	 * @throws RecursoNoEncontradoException si no existe un usuario con ese nombre de usuario
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
	 * Busca todos los usuarios cuyo nombre coincida exactamente con el indicado.
	 *
	 * @param nombre nombre a filtrar
	 * @return lista de DTOs de usuarios encontrados; vacía si no hay coincidencias
	 * @throws TextoVacioException si el nombre está vacío o es nulo
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
			throw new RuntimeException(
					"Error inesperado al buscar por nombre: " + e.getMessage());
		}
	}

	/**
	 * Busca todos los usuarios cuyo apellido coincida exactamente con el indicado.
	 *
	 * @param apellido apellido a filtrar
	 * @return lista de DTOs de usuarios encontrados; vacía si no hay coincidencias
	 * @throws TextoVacioException si el apellido está vacío o es nulo
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
			throw new RuntimeException(
					"Error inesperado al buscar por apellido: " + e.getMessage());
		}
	}

	/**
	 * Busca todos los usuarios que tengan el rol indicado.
	 *
	 * @param rol rol a filtrar (USUARIO o ADMIN)
	 * @return lista de DTOs de usuarios encontrados; vacía si no hay coincidencias
	 * @throws OpcionNoValidaException si el rol es {@code null}
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
			throw new RuntimeException(
					"Error inesperado al buscar por rol: " + e.getMessage());
		}
	}

	

	/**
	 * Valida los campos básicos del DTO de usuario.
	 * Opcionalmente valida la contraseña si se indica que se está creando
	 * o actualizando con una nueva.
	 *
	 * @param data              DTO con los datos a validar
	 * @param validarContrasena {@code true} si la contraseña debe validarse
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
	 * Valida que el nombre no esté vacío, no exceda la longitud máxima
	 * y solo contenga letras y espacios.
	 *
	 * @param nombre nombre del usuario a validar
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
					"El nombre solo puede contener letras y espacios (se permite el número 6). "
							+ "Debe comenzar con mayúscula. "
							+ "Recibido: '" + nombre + "'");
		}
	}

	/**
	 * Valida que el apellido no esté vacío, no exceda la longitud máxima
	 * y solo contenga letras y espacios.
	 *
	 * @param apellido apellido del usuario a validar
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
					"El apellido solo puede contener letras y espacios (se permite el número 6). "
							+ "Debe comenzar con mayúscula. "
							+ "Recibido: '" + apellido + "'");
		}
	}

	/**
	 * Valida que el correo no esté vacío, no exceda la longitud máxima
	 * y tenga formato válido.
	 *
	 * @param correo correo electrónico a validar
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
	 * Valida que el nombre de usuario no esté vacío y no exceda la longitud máxima.
	 *
	 * @param nombreUsuario nombre de usuario a validar
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
	 * Valida que la contraseña no esté vacía, no exceda la longitud máxima,
	 * tenga al menos {@value #LONGITUD_MINIMA_CONTRASENA} caracteres,
	 * contenga al menos una letra y al menos un número.
	 *
	 * @param contrasena contraseña a validar
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
				|| !contrasena.matches(".*[A-Z].*")
				|| !contrasena.matches(".*[a-z].*")
				|| !contrasena.matches(".*[0-9].*")) {
			lanzador.lanzarContrasenaInvalida();
		}
	}

	/**
	 * Valida que el teléfono no esté vacío y cumpla el formato colombiano
	 * (10 dígitos comenzando con 3, sin espacios ni guiones).
	 *
	 * @param telefono número de teléfono a validar
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

	/**
	 * Verifica que el correo y el nombre de usuario del DTO no estén ya
	 * registrados por otro usuario distinto al indicado en {@code idActual}.
	 *
	 * @param data     DTO con los valores a verificar
	 * @param idActual ID del usuario que se está actualizando, o {@code null}
	 *                 si se está creando uno nuevo
	 * @throws CorreoDuplicadoException        si el correo ya pertenece a otro usuario
	 * @throws NombreUsuarioDuplicadoException si el nombre de usuario ya pertenece a otro usuario
	 */
	private void validarUnicidad(UsuarioDTO data, Long idActual) {
		Optional<Usuario> porCorreo = repo.findByCorreo(data.getCorreo().trim());
		if (porCorreo.isPresent()
				&& (idActual == null || !porCorreo.get().getId().equals(idActual))) {
			lanzador.lanzarCorreoDuplicado(
					"Ya existe un usuario registrado con el correo: " + data.getCorreo());
		}
		Optional<Usuario> porNombreUsuario =
				repo.findByNombreUsuario(data.getNombreUsuario().trim());
		if (porNombreUsuario.isPresent()
				&& (idActual == null || !porNombreUsuario.get().getId().equals(idActual))) {
			lanzador.lanzarNombreUsuarioDuplicado(
					"Ya existe un usuario registrado con el nombre de usuario: "
							+ data.getNombreUsuario());
		}
	}

	/**
	 * Valida si un correo electrónico tiene formato correcto mediante expresión regular.
	 *
	 * @param correo correo electrónico a verificar
	 * @return {@code true} si el formato es válido, {@code false} en caso contrario
	 */
	private static boolean esCorreoValido(String correo) {
		if (correo == null) {
			return false;
		}
		return correo.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
	}

	/**
	 * Convierte una entidad {@link Usuario} a su DTO correspondiente.
	 * La contraseña se omite en el DTO resultante por seguridad.
	 * El historial de conversiones se representa como lista de IDs.
	 *
	 * @param entity entidad a convertir
	 * @return DTO con los datos del usuario (sin contraseña)
	 */
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