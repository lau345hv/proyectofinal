package co.edu.unbosque.proyectofinal.repository;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
 
import org.springframework.data.repository.CrudRepository;
 
import co.edu.unbosque.proyectofinal.entity.Auditoria;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;
 
/**
 * Repositorio JPA para la entidad {@link Auditoria}.
 * <p>
 * Hereda las operaciones básicas CRUD de {@link CrudRepository} y
 * declara consultas derivadas para filtrar los registros de auditoría
 * por usuario, tipo de acción, rol y rango de fechas.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public interface AuditoriaRepository extends CrudRepository<Auditoria, Long> {
 
	Optional<List<Auditoria>> findByUsuarioId(Long usuarioId);
 
	Optional<List<Auditoria>> findByNombreUsuario(String nombreUsuario);
 
	Optional<List<Auditoria>> findByTipoAccion(TipoAccion tipoAccion);
 
	Optional<List<Auditoria>> findByRolUsuario(RolUsuario rolUsuario);
 
	Optional<List<Auditoria>> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
 
	Optional<List<Auditoria>> findByUsuarioIdAndTipoAccion(Long usuarioId, TipoAccion tipoAccion);
}