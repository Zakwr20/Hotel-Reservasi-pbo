package com.hotel.dao;

import com.hotel.exception.DataAksesException;
import com.hotel.exception.DataTidakDitemukanException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.Persistable;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO generik (GENERICS): T dibatasi hanya untuk objek yang Persistable.
 * Implementasi saat ini berbasis file (FileDao); bisa diganti JDBC tanpa mengubah Controller.
 */
public interface Dao<T extends Persistable> {

    void muat() throws DataAksesException;

    List<T> findAll();

    Optional<T> findById(String id);

    void insert(T item) throws ValidasiException, DataAksesException;

    void update(T item) throws DataTidakDitemukanException, DataAksesException;

    void delete(String id) throws DataTidakDitemukanException, DataAksesException;

    int count();
}
