package co.edu.unbosque.proyectofinal.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.proyectofinal.entity.HistorialConversion;
import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Repositorio JPA para la entidad {@link HistorialConversion}.
 * <p>
 * Hereda las operaciones básicas CRUD de {@link CrudRepository} y declara
 * consultas derivadas para filtrar el historial por usuario, tipo de
 * archivo, estado, formato y rango de fechas.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public interface HistorialConversionRepository
		extends CrudRepository<HistorialConversion, Long> {

	/**
	 * Busca todas las conversiones realizadas por un usuario específico.
	 *
	 * @param usuarioId ID del usuario propietario de las conversiones
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByUsuarioId(Long usuarioId);

	/**
	 * Busca todas las conversiones de un tipo de archivo determinado.
	 *
	 * @param tipoArchivo categoría del archivo (AUDIO, VIDEO o IMAGEN)
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByTipoArchivo(TipoArchivo tipoArchivo);

	/**
	 * Busca todas las conversiones que se encuentran en un estado específico.
	 *
	 * @param estado estado de la conversión (PENDIENTE, EN_PROCESO, COMPLETADO o FALLIDO)
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByEstado(EstadoConversion estado);

	/**
	 * Busca todas las conversiones cuyo formato de origen coincida con el indicado.
	 *
	 * @param formatoOrigen extensión del formato original (ej. mp4, wav, jpg)
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByFormatoOrigen(String formatoOrigen);

	/**
	 * Busca todas las conversiones cuyo formato de destino coincida con el indicado.
	 *
	 * @param formatoDestino extensión del formato de destino (ej. mp3, png, mkv)
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByFormatoDestino(String formatoDestino);

	/**
	 * Busca las conversiones de un usuario filtradas por tipo de archivo.
	 *
	 * @param usuarioId   ID del usuario propietario
	 * @param tipoArchivo categoría del archivo (AUDIO, VIDEO o IMAGEN)
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByUsuarioIdAndTipoArchivo(Long usuarioId,
			TipoArchivo tipoArchivo);

	/**
	 * Busca las conversiones de un usuario filtradas por estado.
	 *
	 * @param usuarioId ID del usuario propietario
	 * @param estado    estado de la conversión
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByUsuarioIdAndEstado(Long usuarioId,
			EstadoConversion estado);

	/**
	 * Busca todas las conversiones realizadas dentro de un rango de fechas.
	 *
	 * @param inicio fecha y hora de inicio del rango (inclusive)
	 * @param fin    fecha y hora de fin del rango (inclusive)
	 * @return {@code Optional} con la lista de conversiones encontradas,
	 *         o vacío si no existe ninguna
	 */
	public Optional<List<HistorialConversion>> findByFechaConversionBetween(LocalDateTime inicio,
			LocalDateTime fin);
}