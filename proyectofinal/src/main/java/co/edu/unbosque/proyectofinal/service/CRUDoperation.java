package co.edu.unbosque.proyectofinal.service;

import java.util.List;

/**
 * Interfaz genérica que define las operaciones básicas CRUD (Create,
 * Read, Update, Delete) para los servicios del sistema.
 * <p>
 * Permite reutilizar el contrato de operaciones para distintos tipos de
 * datos mediante el uso de genéricos. Todos los servicios principales
 * del sistema implementan esta interfaz.
 * </p>
 *
 * @param <T> Tipo de dato (DTO) que será gestionado por el servicio.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public interface CRUDoperation<T> {

	/**
	 * Crea y persiste un nuevo registro en la base de datos.
	 *
	 * @param data DTO con los datos del recurso a crear
	 * @return 0 si la operación fue exitosa
	 */
	public int create(T data);

	/**
	 * Retorna todos los registros existentes en la base de datos.
	 *
	 * @return lista con todos los registros como DTOs
	 */
	public List<T> getAll();

	/**
	 * Elimina el registro identificado por el ID proporcionado.
	 *
	 * @param id identificador del registro a eliminar
	 * @return 0 si la operación fue exitosa
	 */
	public int deleteById(Long id);

	/**
	 * Actualiza el registro identificado por el ID con los datos del DTO.
	 *
	 * @param id   identificador del registro a actualizar
	 * @param data DTO con los nuevos valores
	 * @return 0 si la operación fue exitosa
	 */
	public int updateById(Long id, T data);

	/**
	 * Retorna el número total de registros en la base de datos.
	 *
	 * @return cantidad total de registros
	 */
	public long count();

	/**
	 * Verifica si existe un registro con el ID proporcionado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si el registro existe, {@code false} en caso contrario
	 */
	public boolean exist(Long id);
}