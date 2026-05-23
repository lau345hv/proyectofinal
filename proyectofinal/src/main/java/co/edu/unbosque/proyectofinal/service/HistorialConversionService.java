package co.edu.unbosque.proyectofinal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.proyectofinal.dto.HistorialConversionDTO;
import co.edu.unbosque.proyectofinal.entity.HistorialConversion;
import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.exception.DatoInvalidoException;
import co.edu.unbosque.proyectofinal.exception.IdInvalidoException;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException;
import co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectofinal.exception.TextoVacioException;
import co.edu.unbosque.proyectofinal.repository.HistorialConversionRepository;
import co.edu.unbosque.proyectofinal.repository.UsuarioRepository;
import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Servicio encargado de gestionar el historial de conversiones de la
 * plataforma.
 * <p>
 * Permite crear, consultar, actualizar y eliminar registros del historial,
 * así como realizar búsquedas filtradas por usuario, tipo de archivo,
 * estado, formato y rango de fechas.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Service
public class HistorialConversionService implements CRUDoperation<HistorialConversionDTO> {

	private static final String OPCIONES_TIPO_ARCHIVO = "AUDIO, VIDEO, IMAGEN";
	private static final String OPCIONES_ESTADO = "PENDIENTE, EN_PROCESO, COMPLETADO, FALLIDO";

	@Autowired
	private HistorialConversionRepository repo;

	@Autowired
	private UsuarioRepository usuarioRepo;

	@Autowired
	private LanzadorDeExcepcion lanzador;

	public HistorialConversionService() {
	}

	@Override
	public int create(HistorialConversionDTO data) {
		try {
			if (data == null) {
				lanzador.lanzarDatoInvalido("Los datos del historial no pueden ser nulos.");
			}
			if (data.getUsuarioId() == null || data.getUsuarioId() <= 0) {
				lanzador.lanzarIdInvalido("usuario", data.getUsuarioId());
			}
			Optional<Usuario> usuario = usuarioRepo.findById(data.getUsuarioId());
			if (!usuario.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado(
						"No existe un usuario con el ID: " + data.getUsuarioId());
			}
			if (data.getTipoArchivo() == null) {
				lanzador.lanzarOpcionNoValida("tipoArchivo", "null", OPCIONES_TIPO_ARCHIVO);
			}
			if (data.getFormatoOrigen() == null || data.getFormatoOrigen().isBlank()) {
				lanzador.lanzarTextoVacio("formatoOrigen");
			}
			if (data.getFormatoDestino() == null || data.getFormatoDestino().isBlank()) {
				lanzador.lanzarTextoVacio("formatoDestino");
			}

			HistorialConversion entity = new HistorialConversion(
					data.getFechaConversion() != null
							? data.getFechaConversion()
							: LocalDateTime.now(),
					data.getTipoArchivo(),
					data.getFormatoOrigen().toLowerCase(),
					data.getFormatoDestino().toLowerCase(),
					data.getNombreArchivoOriginal(),
					data.getNombreArchivoConvertido(),
					data.getRutaArchivoOriginal(),
					data.getRutaArchivoConvertido(),
					data.getEstado() != null ? data.getEstado() : EstadoConversion.PENDIENTE,
					usuario.get());
			repo.save(entity);
		} catch (IdInvalidoException | RecursoNoEncontradoException
				| OpcionNoValidaException | TextoVacioException
				| DatoInvalidoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al registrar la conversión: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public List<HistorialConversionDTO> getAll() {
		List<HistorialConversion> lista = (List<HistorialConversion>) repo.findAll();
		List<HistorialConversionDTO> dtoList = new ArrayList<>();
		lista.forEach(entity -> dtoList.add(mapToDTO(entity)));
		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		try {
			if (id == null || id <= 0) {
				lanzador.lanzarIdInvalido("historialConversion", id);
			}
			Optional<HistorialConversion> encontrado = repo.findById(id);
			if (encontrado.isPresent()) {
				repo.delete(encontrado.get());
				return 0;
			}
			lanzador.lanzarRecursoNoEncontrado("No existe una conversión con el ID: " + id);
		} catch (IdInvalidoException | RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al eliminar la conversión: " + e.getMessage());
		}
		return 1;
	}

	@Override
	public int updateById(Long id, HistorialConversionDTO data) {
		try {
			if (id == null || id <= 0) {
				lanzador.lanzarIdInvalido("historialConversion", id);
			}
			Optional<HistorialConversion> encontrado = repo.findById(id);
			if (!encontrado.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado("No existe una conversión con el ID: " + id);
			}
			HistorialConversion entity = encontrado.get();
			if (data.getUsuarioId() != null && data.getUsuarioId() > 0) {
				Optional<Usuario> usuario = usuarioRepo.findById(data.getUsuarioId());
				if (!usuario.isPresent()) {
					lanzador.lanzarRecursoNoEncontrado(
							"No existe un usuario con el ID: " + data.getUsuarioId());
				}
				entity.setUsuario(usuario.get());
			}
			if (data.getFechaConversion() != null) {
				entity.setFechaConversion(data.getFechaConversion());
			}
			if (data.getTipoArchivo() != null) {
				entity.setTipoArchivo(data.getTipoArchivo());
			}
			if (data.getFormatoOrigen() != null && !data.getFormatoOrigen().isBlank()) {
				entity.setFormatoOrigen(data.getFormatoOrigen().toLowerCase());
			}
			if (data.getFormatoDestino() != null && !data.getFormatoDestino().isBlank()) {
				entity.setFormatoDestino(data.getFormatoDestino().toLowerCase());
			}
			if (data.getNombreArchivoOriginal() != null) {
				entity.setNombreArchivoOriginal(data.getNombreArchivoOriginal());
			}
			if (data.getNombreArchivoConvertido() != null) {
				entity.setNombreArchivoConvertido(data.getNombreArchivoConvertido());
			}
			if (data.getRutaArchivoOriginal() != null) {
				entity.setRutaArchivoOriginal(data.getRutaArchivoOriginal());
			}
			if (data.getRutaArchivoConvertido() != null) {
				entity.setRutaArchivoConvertido(data.getRutaArchivoConvertido());
			}
			if (data.getEstado() != null) {
				entity.setEstado(data.getEstado());
			}
			repo.save(entity);
		} catch (IdInvalidoException | RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al actualizar la conversión: " + e.getMessage());
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
	 * Busca un registro del historial por su ID.
	 *
	 * @param id ID del registro.
	 * @return DTO del registro encontrado.
	 */
	public HistorialConversionDTO findById(Long id) {
		try {
			if (id == null || id <= 0) {
				lanzador.lanzarIdInvalido("historialConversion", id);
			}
			Optional<HistorialConversion> encontrado = repo.findById(id);
			if (!encontrado.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado("No existe una conversión con el ID: " + id);
			}
			return mapToDTO(encontrado.get());
		} catch (IdInvalidoException | RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al buscar por ID: " + e.getMessage());
		}
	}

	/**
	 * Obtiene el historial completo de conversiones de un usuario específico.
	 *
	 * @param usuarioId ID del usuario.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByUsuarioId(Long usuarioId) {
		try {
			if (usuarioId == null || usuarioId <= 0) {
				lanzador.lanzarIdInvalido("usuario", usuarioId);
			}
			if (!usuarioRepo.existsById(usuarioId)) {
				lanzador.lanzarRecursoNoEncontrado("No existe un usuario con el ID: " + usuarioId);
			}
			Optional<List<HistorialConversion>> encontrados = repo.findByUsuarioId(usuarioId);
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (IdInvalidoException | RecursoNoEncontradoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar historial por usuario: " + e.getMessage());
		}
	}

	/**
	 * Busca conversiones filtradas por tipo de archivo.
	 *
	 * @param tipoArchivo Tipo de archivo.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByTipoArchivo(TipoArchivo tipoArchivo) {
		try {
			if (tipoArchivo == null) {
				lanzador.lanzarOpcionNoValida("tipoArchivo", "null", OPCIONES_TIPO_ARCHIVO);
			}
			Optional<List<HistorialConversion>> encontrados = repo.findByTipoArchivo(tipoArchivo);
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (OpcionNoValidaException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar por tipo de archivo: " + e.getMessage());
		}
	}

	/**
	 * Busca conversiones filtradas por estado.
	 *
	 * @param estado Estado de la conversión.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByEstado(EstadoConversion estado) {
		try {
			if (estado == null) {
				lanzador.lanzarOpcionNoValida("estado", "null", OPCIONES_ESTADO);
			}
			Optional<List<HistorialConversion>> encontrados = repo.findByEstado(estado);
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (OpcionNoValidaException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error inesperado al buscar por estado: " + e.getMessage());
		}
	}

	/**
	 * Busca conversiones filtradas por formato de origen.
	 *
	 * @param formatoOrigen Formato de origen.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByFormatoOrigen(String formatoOrigen) {
		try {
			if (formatoOrigen == null || formatoOrigen.isBlank()) {
				lanzador.lanzarTextoVacio("formatoOrigen");
			}
			Optional<List<HistorialConversion>> encontrados = repo
					.findByFormatoOrigen(formatoOrigen.toLowerCase());
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (TextoVacioException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar por formato origen: " + e.getMessage());
		}
	}

	/**
	 * Busca conversiones filtradas por formato destino.
	 *
	 * @param formatoDestino Formato destino.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByFormatoDestino(String formatoDestino) {
		try {
			if (formatoDestino == null || formatoDestino.isBlank()) {
				lanzador.lanzarTextoVacio("formatoDestino");
			}
			Optional<List<HistorialConversion>> encontrados = repo
					.findByFormatoDestino(formatoDestino.toLowerCase());
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (TextoVacioException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar por formato destino: " + e.getMessage());
		}
	}

	/**
	 * Busca conversiones de un usuario específico filtradas por tipo de archivo.
	 *
	 * @param usuarioId   ID del usuario.
	 * @param tipoArchivo Tipo de archivo.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByUsuarioIdAndTipoArchivo(Long usuarioId,
			TipoArchivo tipoArchivo) {
		try {
			if (usuarioId == null || usuarioId <= 0) {
				lanzador.lanzarIdInvalido("usuario", usuarioId);
			}
			if (tipoArchivo == null) {
				lanzador.lanzarOpcionNoValida("tipoArchivo", "null", OPCIONES_TIPO_ARCHIVO);
			}
			Optional<List<HistorialConversion>> encontrados = repo
					.findByUsuarioIdAndTipoArchivo(usuarioId, tipoArchivo);
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (IdInvalidoException | OpcionNoValidaException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar historial filtrado: " + e.getMessage());
		}
	}

	/**
	 * Busca conversiones de un usuario filtradas por estado.
	 *
	 * @param usuarioId ID del usuario.
	 * @param estado    Estado de la conversión.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByUsuarioIdAndEstado(Long usuarioId,
			EstadoConversion estado) {
		try {
			if (usuarioId == null || usuarioId <= 0) {
				lanzador.lanzarIdInvalido("usuario", usuarioId);
			}
			if (estado == null) {
				lanzador.lanzarOpcionNoValida("estado", "null", OPCIONES_ESTADO);
			}
			Optional<List<HistorialConversion>> encontrados = repo
					.findByUsuarioIdAndEstado(usuarioId, estado);
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (IdInvalidoException | OpcionNoValidaException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar historial filtrado: " + e.getMessage());
		}
	}

	/**
	 * Busca conversiones realizadas en un rango de fechas.
	 *
	 * @param inicio Fecha inicial.
	 * @param fin    Fecha final.
	 * @return Lista de DTOs encontrados.
	 */
	public List<HistorialConversionDTO> findByFechaConversionBetween(LocalDateTime inicio,
			LocalDateTime fin) {
		try {
			if (inicio == null) {
				lanzador.lanzarDatoInvalido("La fecha de inicio no puede ser nula.");
			}
			if (fin == null) {
				lanzador.lanzarDatoInvalido("La fecha de fin no puede ser nula.");
			}
			if (inicio.isAfter(fin)) {
				lanzador.lanzarDatoInvalido(
						"La fecha de inicio no puede ser posterior a la fecha de fin.");
			}
			Optional<List<HistorialConversion>> encontrados = repo
					.findByFechaConversionBetween(inicio, fin);
			List<HistorialConversionDTO> dtoList = new ArrayList<>();
			if (encontrados.isPresent()) {
				encontrados.get().forEach(entity -> dtoList.add(mapToDTO(entity)));
			}
			return dtoList;
		} catch (DatoInvalidoException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error inesperado al buscar por rango de fechas: " + e.getMessage());
		}
	}

	private HistorialConversionDTO mapToDTO(HistorialConversion entity) {
		HistorialConversionDTO dto = new HistorialConversionDTO();
		dto.setId(entity.getId());
		dto.setFechaConversion(entity.getFechaConversion());
		dto.setTipoArchivo(entity.getTipoArchivo());
		dto.setFormatoOrigen(entity.getFormatoOrigen());
		dto.setFormatoDestino(entity.getFormatoDestino());
		dto.setNombreArchivoOriginal(entity.getNombreArchivoOriginal());
		dto.setNombreArchivoConvertido(entity.getNombreArchivoConvertido());
		dto.setRutaArchivoOriginal(entity.getRutaArchivoOriginal());
		dto.setRutaArchivoConvertido(entity.getRutaArchivoConvertido());
		dto.setEstado(entity.getEstado());
		if (entity.getUsuario() != null) {
			dto.setUsuarioId(entity.getUsuario().getId());
		}
		return dto;
	}
}
