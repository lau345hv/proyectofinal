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
import co.edu.unbosque.proyectofinal.util.AESUtil;
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
 * <p>
 * Los campos sensibles que se persisten en la base de datos se encriptan
 * con AES/GCM antes de guardarse y se desencriptan automáticamente al
 * leer, de modo que ninguna capa superior recibe datos cifrados. Los
 * campos protegidos son:
 * </p>
 * <ul>
 *   <li>{@code nombreArchivoOriginal}: nombre del archivo subido por el usuario.</li>
 *   <li>{@code rutaArchivoOriginal}: URL o ruta del archivo original.</li>
 *   <li>{@code rutaArchivoConvertido}: URL de descarga generada por la API externa.</li>
 * </ul>
 *
 * @author Equipo de desarrollo
 * @version 2.0
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
					encriptarSiNoNulo(data.getNombreArchivoOriginal()),
					data.getNombreArchivoConvertido(),
					encriptarSiNoNulo(data.getRutaArchivoOriginal()),
					encriptarSiNoNulo(data.getRutaArchivoConvertido()),
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

	/**
	 * Actualiza un registro del historial identificado por su ID.
	 * Solo se modifican los campos que vengan con valor en el DTO.
	 * Los campos sensibles se re-encriptan antes de persistirse.
	 *
	 * @param id   ID del registro a actualizar.
	 * @param data DTO con los nuevos valores.
	 * @return 0 si la operación fue exitosa.
	 */
	// FIX JAVA-R1000: la lógica de actualización de campos se extrajo al método
	// privado aplicarCambiosHistorial para reducir la complejidad ciclomática.
	@Override
	public int updateById(Long id, HistorialConversionDTO data) {
		try {
			validarIdYObtenerEntidad(id);
			HistorialConversion entity = obtenerEntidadHistorial(id);
			actualizarUsuarioSiCorresponde(entity, data);
			aplicarCambiosHistorial(entity, data);
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
	 * Los campos sensibles se devuelven desencriptados.
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
	 * Los campos sensibles se devuelven desencriptados.
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


	/**
	 * Valida que el ID sea válido y que el registro exista.
	 *
	 * @param id ID a validar.
	 */
	private void validarIdYObtenerEntidad(Long id) {
		if (id == null || id <= 0) {
			lanzador.lanzarIdInvalido("historialConversion", id);
		}
		if (!repo.existsById(id)) {
			lanzador.lanzarRecursoNoEncontrado("No existe una conversión con el ID: " + id);
		}
	}

	/**
	 * Obtiene la entidad del historial por su ID (asume que ya fue validado).
	 *
	 * @param id ID del registro.
	 * @return Entidad encontrada.
	 */
	private HistorialConversion obtenerEntidadHistorial(Long id) {
		return repo.findById(id).orElseThrow(() ->
				new RuntimeException("No existe una conversión con el ID: " + id));
	}

	/**
	 * Actualiza el usuario de la entidad si el DTO trae un usuarioId distinto.
	 *
	 * @param entity Entidad a actualizar.
	 * @param data   DTO con los nuevos valores.
	 */
	private void actualizarUsuarioSiCorresponde(HistorialConversion entity,
			HistorialConversionDTO data) {
		if (data.getUsuarioId() != null && data.getUsuarioId() > 0) {
			Optional<Usuario> usuario = usuarioRepo.findById(data.getUsuarioId());
			if (!usuario.isPresent()) {
				lanzador.lanzarRecursoNoEncontrado(
						"No existe un usuario con el ID: " + data.getUsuarioId());
			}
			entity.setUsuario(usuario.get());
		}
	}

	/**
	 * Aplica los cambios del DTO a la entidad para los campos opcionales.
	 * Los campos sensibles se re-encriptan antes de asignarse a la entidad.
	 *
	 * @param entity Entidad a modificar.
	 * @param data   DTO con los nuevos valores.
	 */
	private void aplicarCambiosHistorial(HistorialConversion entity,
			HistorialConversionDTO data) {
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
			entity.setNombreArchivoOriginal(encriptarSiNoNulo(data.getNombreArchivoOriginal()));
		}
		if (data.getNombreArchivoConvertido() != null) {
			entity.setNombreArchivoConvertido(data.getNombreArchivoConvertido());
		}
		if (data.getRutaArchivoOriginal() != null) {
			entity.setRutaArchivoOriginal(encriptarSiNoNulo(data.getRutaArchivoOriginal()));
		}
		if (data.getRutaArchivoConvertido() != null) {
			entity.setRutaArchivoConvertido(encriptarSiNoNulo(data.getRutaArchivoConvertido()));
		}
		if (data.getEstado() != null) {
			entity.setEstado(data.getEstado());
		}
	}

	

	/**
	 * Encripta un valor con AES si no es nulo ni vacío.
	 * Si el valor es nulo, lo retorna tal cual para no romper campos opcionales.
	 *
	 * @param valor texto plano a encriptar.
	 * @return texto encriptado en Base64, o {@code null} si el valor era nulo.
	 */
	private String encriptarSiNoNulo(String valor) {
		if (valor == null || valor.isBlank()) {
			return valor;
		}
		return AESUtil.encrypt(valor);
	}

	/**
	 * Desencripta un valor con AES si no es nulo ni vacío.
	 * Si el valor es nulo o no se puede desencriptar (registro legacy o clave
	 * diferente), lo retorna tal cual sin lanzar excepción.
	 *
	 * @param valor texto encriptado en Base64.
	 * @return texto plano original, o el valor sin modificar si no estaba cifrado.
	 */
	private String desencriptarSiNoNulo(String valor) {
		if (valor == null || valor.isBlank()) {
			return valor;
		}
		try {
			return AESUtil.decrypt(valor);
		} catch (Exception e) {
			
			return valor;
		}
	}

	/**
	 * Mapea una entidad {@link HistorialConversion} a su DTO correspondiente,
	 * desencriptando los campos sensibles antes de exponerlos.
	 *
	 * @param entity Entidad a convertir.
	 * @return DTO con los datos desencriptados.
	 */
	private HistorialConversionDTO mapToDTO(HistorialConversion entity) {
		HistorialConversionDTO dto = new HistorialConversionDTO();
		dto.setId(entity.getId());
		dto.setFechaConversion(entity.getFechaConversion());
		dto.setTipoArchivo(entity.getTipoArchivo());
		dto.setFormatoOrigen(entity.getFormatoOrigen());
		dto.setFormatoDestino(entity.getFormatoDestino());
		dto.setNombreArchivoOriginal(desencriptarSiNoNulo(entity.getNombreArchivoOriginal()));
		dto.setNombreArchivoConvertido(entity.getNombreArchivoConvertido());
		dto.setRutaArchivoOriginal(desencriptarSiNoNulo(entity.getRutaArchivoOriginal()));
		dto.setRutaArchivoConvertido(desencriptarSiNoNulo(entity.getRutaArchivoConvertido()));
		dto.setEstado(entity.getEstado());
		if (entity.getUsuario() != null) {
			dto.setUsuarioId(entity.getUsuario().getId());
		}
		return dto;
	}
}