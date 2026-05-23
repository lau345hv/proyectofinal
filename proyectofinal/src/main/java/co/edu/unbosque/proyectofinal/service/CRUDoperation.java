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

	public int create(T data);

	public List<T> getAll();

	public int deleteById(Long id);

	public int updateById(Long id, T data);

	public long count();

	public boolean exist(Long id);
}