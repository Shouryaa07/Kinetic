package com.kinetic.dao;

import com.kinetic.model.Entity;
import java.util.List;
import java.util.Optional;

/**
 * Generic data-access contract shared by every DAO.
 *
 * @param <T>  the entity type
 * @param <ID> the primary key type
 */
public interface Dao<T extends Entity, ID> {
    Optional<T> findById(ID id);

    List<T> findAll();

    /** Inserts a new row (id is null) or updates an existing one, and returns the saved entity. */
    T save(T entity);

    boolean deleteById(ID id);
}
