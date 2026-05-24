package co.edu.unbosque.proyectofinal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.proyectofinal.dto.AuditoriaDTO;
import co.edu.unbosque.proyectofinal.entity.Auditoria;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.repository.AuditoriaRepository;
import co.edu.unbosque.proyectofinal.security.JwtAuthenticationFilter;
import co.edu.unbosque.proyectofinal.security.JwtUtil;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;

/**
 * Servicio encargado de registrar y consultar los registros de auditoría
 * de la plataforma de conversión de archivos.
 *
 * <p>El método central {@link #preAccion(TipoAccion, String)} extrae
 * automáticamente los datos del usuario autenticado (id, nombre de usuario
 * y rol) a partir del token JWT presente en el {@link ThreadLocal} del hilo
 * activo, sin necesidad de recibir esos datos como parámetro desde el
 * controlador.</p>
 *
 * <p>Implementa {@link CRUDoperation} para mantener la consistencia con el
 * resto de servicios del proyecto. La operación {@link #updateById} no está
 * permitida, ya que los registros de auditoría son inmutables por diseño.</p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 * @see AuditoriaDTO
 * @see JwtAuthenticationFilter
 * @see JwtUtil
 */
@Service
public class AuditoriaService implements CRUDoperation<AuditoriaDTO> {

	/** Repositorio JPA para persistir y consultar registros de auditoría. */
	@Autowired
	private AuditoriaRepository auditoriaRepository;

	/** Mapeador para convertir entre entidades {@link Auditoria} y {@link AuditoriaDTO}. */
	@Autowired
	private ModelMapper modelMapper;

	/** Utilidad para extraer claims del token JWT (id, nombre de usuario, rol). */
	@Autowired
	private JwtUtil jwtUtil;

	/**
	 * Filtro JWT del que se obtiene el token y el endpoint activos en el
	 * hilo de la petición HTTP actual.
	 */
	@Autowired
	private JwtAuthenticationFilter filtro;

	/** Componente para lanzar excepciones de negocio estandarizadas. */
	@Autowired
	private LanzadorDeExcepcion lanzador;

	/**
	 * Constructor sin argumentos requerido por Spring para la inyección de
	 * dependencias mediante {@code @Autowired}.
	 */
	public AuditoriaService() {
	}

	/**
	 * Construye un {@link AuditoriaDTO} listo para persistir, extrayendo
	 * automáticamente los datos del usuario desde el token JWT activo en el
	 * hilo de la petición HTTP actual.
	 *
	 * <p>Si no hay token presente (petición anónima o pública), los campos
	 * {@code usuarioId}, {@code nombreUsuario} y {@code rolUsuario} quedarán
	 * en {@code null}. Cualquier error al parsear el token se ignora
	 * silenciosamente para no interrumpir el flujo de la petición.</p>
	 *
	 * @param tipoAccion  tipo de acción que se está registrando (CREATE, READ,
	 *                    UPDATE, DELETE, LOGIN, LOGOUT o CONVERSION).
	 * @param descripcion descripción legible de la acción realizada
	 *                    (p. ej., {@code "Conversión de archivo video.mkv a mp4"}).
	 * @return {@link AuditoriaDTO} con todos los campos completados y la fecha
	 *         establecida al momento actual ({@link LocalDateTime#now()}).
	 */
	public AuditoriaDTO preAccion(TipoAccion tipoAccion, String descripcion) {
		Long usuarioId = null;
		String nombreUsuario = null;
		RolUsuario rolUsuario = null;

		String token = filtro.getTokenJwt();
		String endpoint = filtro.getEndpointActual();

		if (token != null && !token.isEmpty()) {
			try {
				usuarioId = jwtUtil.extractClaim(token,
						claims -> claims.get("id", Long.class));
				nombreUsuario = jwtUtil.extractUsername(token);
				String rolStr = jwtUtil.extractRol(token);
				rolUsuario = RolUsuario.valueOf(rolStr);
			} catch (Exception ignorado) {
				// Si el token es inválido o le faltan claims, se registra sin datos de usuario.
			}
		}

		return new AuditoriaDTO(usuarioId, nombreUsuario, rolUsuario,
				tipoAccion, descripcion, endpoint, LocalDateTime.now());
	}

	/**
	 * Persiste un nuevo registro de auditoría en la base de datos.
	 *
	 * @param data {@link AuditoriaDTO} con los datos del registro a guardar.
	 * @return {@code 0} siempre (convención de la interfaz {@link CRUDoperation}).
	 */
	@Override
	public int create(AuditoriaDTO data) {
		Auditoria entity = modelMapper.map(data, Auditoria.class);
		auditoriaRepository.save(entity);
		return 0;
	}

	/**
	 * Retorna todos los registros de auditoría existentes en la base de datos.
	 *
	 * @return lista de {@link AuditoriaDTO} con todos los registros;
	 *         lista vacía si no existe ninguno.
	 */
	@Override
	public List<AuditoriaDTO> getAll() {
		List<Auditoria> entityList = (List<Auditoria>) auditoriaRepository.findAll();
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		entityList.forEach(entity ->
				dtoList.add(modelMapper.map(entity, AuditoriaDTO.class)));
		return dtoList;
	}

	/**
	 * Elimina un registro de auditoría por su identificador único.
	 *
	 * @param id identificador del registro a eliminar.
	 * @return {@code 0} si la eliminación fue exitosa.
	 * @throws co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException
	 *         si no existe ningún registro con el {@code id} proporcionado.
	 */
	@Override
	public int deleteById(Long id) {
		if (!auditoriaRepository.existsById(id)) {
			lanzador.lanzarRecursoNoEncontrado("Registro de auditoría con id " + id);
		}
		auditoriaRepository.deleteById(id);
		return 0;
	}

	/**
	 * Operación no permitida. Los registros de auditoría son inmutables por diseño.
	 *
	 * @param id   identificador del registro (ignorado).
	 * @param data datos nuevos (ignorados).
	 * @return nunca retorna normalmente; siempre lanza una excepción.
	 * @throws co.edu.unbosque.proyectofinal.exception.DatoInvalidoException
	 *         siempre, indicando que la modificación de auditorías no está permitida.
	 */
	@Override
	public int updateById(Long id, AuditoriaDTO data) {
		lanzador.lanzarDatoInvalido("Los registros de auditoría no pueden modificarse.");
		return 1;
	}

	/**
	 * Retorna el número total de registros de auditoría en la base de datos.
	 *
	 * @return cantidad total de registros.
	 */
	@Override
	public long count() {
		return auditoriaRepository.count();
	}

	/**
	 * Verifica si existe un registro de auditoría con el identificador dado.
	 *
	 * @param id identificador a verificar.
	 * @return {@code true} si el registro existe; {@code false} en caso contrario.
	 */
	@Override
	public boolean exist(Long id) {
		return auditoriaRepository.existsById(id);
	}

	/**
	 * Retorna todos los registros de auditoría asociados a un usuario específico.
	 *
	 * @param usuarioId identificador del usuario a consultar.
	 * @return lista de {@link AuditoriaDTO} del usuario; lista vacía si no hay registros.
	 */
	public List<AuditoriaDTO> findByUsuarioId(Long usuarioId) {
		Optional<List<Auditoria>> resultado = auditoriaRepository.findByUsuarioId(usuarioId);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	/**
	 * Retorna todos los registros de auditoría asociados a un nombre de usuario.
	 *
	 * @param nombreUsuario nombre de usuario a consultar.
	 * @return lista de {@link AuditoriaDTO} del usuario; lista vacía si no hay registros.
	 */
	public List<AuditoriaDTO> findByNombreUsuario(String nombreUsuario) {
		Optional<List<Auditoria>> resultado =
				auditoriaRepository.findByNombreUsuario(nombreUsuario);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	/**
	 * Retorna todos los registros de auditoría filtrados por tipo de acción.
	 *
	 * @param tipoAccion tipo de acción a filtrar (CREATE, READ, UPDATE, DELETE,
	 *                   LOGIN, LOGOUT o CONVERSION).
	 * @return lista de {@link AuditoriaDTO} del tipo indicado;
	 *         lista vacía si no hay registros.
	 */
	public List<AuditoriaDTO> findByTipoAccion(TipoAccion tipoAccion) {
		Optional<List<Auditoria>> resultado = auditoriaRepository.findByTipoAccion(tipoAccion);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	/**
	 * Retorna todos los registros de auditoría filtrados por rol de usuario.
	 *
	 * @param rolUsuario rol a filtrar ({@code USUARIO} o {@code ADMIN}).
	 * @return lista de {@link AuditoriaDTO} del rol indicado;
	 *         lista vacía si no hay registros.
	 */
	public List<AuditoriaDTO> findByRolUsuario(RolUsuario rolUsuario) {
		Optional<List<Auditoria>> resultado = auditoriaRepository.findByRolUsuario(rolUsuario);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	/**
	 * Retorna todos los registros de auditoría cuya fecha se encuentra
	 * dentro del rango {@code [inicio, fin]} (ambos inclusive).
	 *
	 * @param inicio fecha y hora de inicio del rango (inclusive).
	 * @param fin    fecha y hora de fin del rango (inclusive).
	 * @return lista de {@link AuditoriaDTO} dentro del rango;
	 *         lista vacía si no hay registros.
	 */
	public List<AuditoriaDTO> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
		Optional<List<Auditoria>> resultado =
				auditoriaRepository.findByFechaBetween(inicio, fin);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	/**
	 * Retorna todos los registros de auditoría de un usuario filtrados además
	 * por tipo de acción.
	 *
	 * @param usuarioId  identificador del usuario a filtrar.
	 * @param tipoAccion tipo de acción a filtrar.
	 * @return lista de {@link AuditoriaDTO} que cumplen ambos criterios;
	 *         lista vacía si no hay resultados.
	 */
	public List<AuditoriaDTO> findByUsuarioIdAndTipoAccion(Long usuarioId,
			TipoAccion tipoAccion) {
		Optional<List<Auditoria>> resultado =
				auditoriaRepository.findByUsuarioIdAndTipoAccion(usuarioId, tipoAccion);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	/**
	 * Busca y retorna un registro de auditoría por su identificador único.
	 *
	 * @param id identificador del registro a buscar.
	 * @return {@link AuditoriaDTO} correspondiente al id proporcionado.
	 * @throws co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException
	 *         si no existe ningún registro con el {@code id} dado.
	 */
	public AuditoriaDTO findById(Long id) {
		Auditoria entity = auditoriaRepository.findById(id)
				.orElseThrow(() -> {
					lanzador.lanzarRecursoNoEncontrado("Registro de auditoría con id " + id);
					return new RuntimeException();
				});
		return modelMapper.map(entity, AuditoriaDTO.class);
	}
}