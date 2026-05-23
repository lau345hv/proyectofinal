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
 * <p>
 * El método central {@link #preAccion(TipoAccion, String)} extrae
 * automáticamente los datos del usuario autenticado (id, nombre de
 * usuario y rol) a partir del token JWT presente en el hilo actual,
 * sin necesidad de recibir esos datos como parámetro desde el controlador.
 * </p>
 * <p>
 * Este servicio implementa {@link CRUDoperation} para mantener la
 * consistencia con el resto de servicios del proyecto.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Service
public class AuditoriaService implements CRUDoperation<AuditoriaDTO> {

	@Autowired
	private AuditoriaRepository auditoriaRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private JwtAuthenticationFilter filtro;

	@Autowired
	private LanzadorDeExcepcion lanzador;

	public AuditoriaService() {
	}

	/**
	 * Construye un {@link AuditoriaDTO} listo para persistir, extrayendo
	 * automáticamente los datos del usuario desde el token JWT activo en
	 * el hilo de la petición HTTP actual.
	 * <p>
	 * Si no hay token (petición anónima o pública), los campos
	 * {@code usuarioId}, {@code nombreUsuario} y {@code rolUsuario}
	 * quedarán en {@code null}.
	 * </p>
	 *
	 * @param tipoAccion  Tipo de acción que se está registrando.
	 * @param descripcion Descripción legible de lo que hizo el usuario.
	 *                    Ejemplo: "Conversión de archivo video.mkv a mp4".
	 * @return {@link AuditoriaDTO} con todos los campos completos y la fecha
	 *         establecida al momento actual.
	 */
	public AuditoriaDTO preAccion(TipoAccion tipoAccion, String descripcion) {
		Long usuarioId = null;
		String nombreUsuario = null;
		RolUsuario rolUsuario = null;

		String token = filtro.getTokenJwt();

		if (token != null && !token.isEmpty()) {
			try {
				// Se extrae el claim "id" (Long) del token
				usuarioId = jwtUtil.extractClaim(token,
						claims -> claims.get("id", Long.class));
				nombreUsuario = jwtUtil.extractUsername(token);
				String rolStr = jwtUtil.extractRol(token);
				rolUsuario = RolUsuario.valueOf(rolStr);
			} catch (Exception ignorado) {
				// Si el token está expirado o malformado, dejamos los campos en null
			}
		}

		return new AuditoriaDTO(usuarioId, nombreUsuario, rolUsuario,
				tipoAccion, descripcion, null, LocalDateTime.now());
	}

	@Override
	public int create(AuditoriaDTO data) {
		Auditoria entity = modelMapper.map(data, Auditoria.class);
		auditoriaRepository.save(entity);
		return 0;
	}

	@Override
	public List<AuditoriaDTO> getAll() {
		List<Auditoria> entityList = (List<Auditoria>) auditoriaRepository.findAll();
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		entityList.forEach(entity ->
				dtoList.add(modelMapper.map(entity, AuditoriaDTO.class)));
		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		if (!auditoriaRepository.existsById(id)) {
			lanzador.lanzarRecursoNoEncontrado("Registro de auditoría con id " + id);
		}
		auditoriaRepository.deleteById(id);
		return 0;
	}

	@Override
	public int updateById(Long id, AuditoriaDTO data) {
		// La auditoría es inmutable: los registros no se editan
		lanzador.lanzarDatoInvalido("Los registros de auditoría no pueden modificarse.");
		return 1;
	}

	@Override
	public long count() {
		return auditoriaRepository.count();
	}

	@Override
	public boolean exist(Long id) {
		return auditoriaRepository.existsById(id);
	}

	public List<AuditoriaDTO> findByUsuarioId(Long usuarioId) {
		Optional<List<Auditoria>> resultado = auditoriaRepository.findByUsuarioId(usuarioId);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	public List<AuditoriaDTO> findByNombreUsuario(String nombreUsuario) {
		Optional<List<Auditoria>> resultado =
				auditoriaRepository.findByNombreUsuario(nombreUsuario);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	public List<AuditoriaDTO> findByTipoAccion(TipoAccion tipoAccion) {
		Optional<List<Auditoria>> resultado = auditoriaRepository.findByTipoAccion(tipoAccion);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	public List<AuditoriaDTO> findByRolUsuario(RolUsuario rolUsuario) {
		Optional<List<Auditoria>> resultado = auditoriaRepository.findByRolUsuario(rolUsuario);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	public List<AuditoriaDTO> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
		Optional<List<Auditoria>> resultado =
				auditoriaRepository.findByFechaBetween(inicio, fin);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	public List<AuditoriaDTO> findByUsuarioIdAndTipoAccion(Long usuarioId,
			TipoAccion tipoAccion) {
		Optional<List<Auditoria>> resultado =
				auditoriaRepository.findByUsuarioIdAndTipoAccion(usuarioId, tipoAccion);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		resultado.ifPresent(lista ->
				lista.forEach(e -> dtoList.add(modelMapper.map(e, AuditoriaDTO.class))));
		return dtoList;
	}

	public AuditoriaDTO findById(Long id) {
		Auditoria entity = auditoriaRepository.findById(id)
				.orElseThrow(() -> {
					lanzador.lanzarRecursoNoEncontrado("Registro de auditoría con id " + id);
					return new RuntimeException();
				});
		return modelMapper.map(entity, AuditoriaDTO.class);
	}
}