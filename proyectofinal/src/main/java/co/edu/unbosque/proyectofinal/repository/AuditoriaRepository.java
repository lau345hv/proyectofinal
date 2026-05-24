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
 
	/**
	 * Busca todos los registros de auditoría generados por un usuario específico.
	 *
	 * @param usuarioId ID del usuario cuyos registros se desean consultar
	 * @return {@code Optional} con la lista de registros encontrados,
	 *         o vacío si no existe ninguno
	 */
	Optional<List<Auditoria>> findByUsuarioId(Long usuarioId);
 
	/**
	 * Busca todos los registros de auditoría asociados a un nombre de usuario.
	 *
	 * @param nombreUsuario nombre de usuario a filtrar
	 * @return {@code Optional} con la lista de registros encontrados,
	 *         o vacío si no existe ninguno
	 */
	Optional<List<Auditoria>> findByNombreUsuario(String nombreUsuario);
 
	/**
	 * Busca todos los registros de auditoría de un tipo de acción determinado.
	 *
	 * @param tipoAccion tipo de acción a filtrar (CREATE, READ, UPDATE,
	 *                   DELETE, LOGIN, LOGOUT, CONVERSION)
	 * @return {@code Optional} con la lista de registros encontrados,
	 *         o vacío si no existe ninguno
	 */
	Optional<List<Auditoria>> findByTipoAccion(TipoAccion tipoAccion);
 
	/**
	 * Busca todos los registros de auditoría generados por usuarios con un rol específico.
	 *
	 * @param rolUsuario rol del usuario a filtrar (USUARIO o ADMIN)
	 * @return {@code Optional} con la lista de registros encontrados,
	 *         o vacío si no existe ninguno
	 */
	Optional<List<Auditoria>> findByRolUsuario(RolUsuario rolUsuario);
 
	/**
	 * Busca todos los registros de auditoría cuya fecha esté dentro del rango indicado.
	 *
	 * @param inicio fecha y hora de inicio del rango (inclusive)
	 * @param fin    fecha y hora de fin del rango (inclusive)
	 * @return {@code Optional} con la lista de registros encontrados,
	 *         o vacío si no existe ninguno
	 */
	Optional<List<Auditoria>> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
 
	/**
	 * Busca los registros de auditoría de un usuario filtrados además por tipo de acción.
	 *
	 * @param usuarioId  ID del usuario
	 * @param tipoAccion tipo de acción a filtrar
	 * @return {@code Optional} con la lista de registros encontrados,
	 *         o vacío si no existe ninguno
	 */
	Optional<List<Auditoria>> findByUsuarioIdAndTipoAccion(Long usuarioId, TipoAccion tipoAccion);
}