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

	public Optional<List<HistorialConversion>> findByUsuarioId(Long usuarioId);

	public Optional<List<HistorialConversion>> findByTipoArchivo(TipoArchivo tipoArchivo);

	public Optional<List<HistorialConversion>> findByEstado(EstadoConversion estado);

	public Optional<List<HistorialConversion>> findByFormatoOrigen(String formatoOrigen);

	public Optional<List<HistorialConversion>> findByFormatoDestino(String formatoDestino);

	public Optional<List<HistorialConversion>> findByUsuarioIdAndTipoArchivo(Long usuarioId,
			TipoArchivo tipoArchivo);

	public Optional<List<HistorialConversion>> findByUsuarioIdAndEstado(Long usuarioId,
			EstadoConversion estado);

	public Optional<List<HistorialConversion>> findByFechaConversionBetween(LocalDateTime inicio,
			LocalDateTime fin);
}
