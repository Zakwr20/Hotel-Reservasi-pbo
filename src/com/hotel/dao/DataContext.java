package com.hotel.dao;

import com.hotel.exception.DataAksesException;

import java.nio.file.Path;

/** Menyatukan semua DAO dan memuatnya dengan urutan yang benar (kamar & tamu sebelum reservasi). */
public class DataContext {

    private final Path folder;
    private final PenggunaDao penggunaDao;
    private final KamarDao kamarDao;
    private final TamuDao tamuDao;
    private final ReservasiDao reservasiDao;

    public DataContext(Path folder) {
        this.folder = folder;
        this.penggunaDao = new PenggunaDao(folder);
        this.kamarDao = new KamarDao(folder);
        this.tamuDao = new TamuDao(folder);
        this.reservasiDao = new ReservasiDao(folder, kamarDao, tamuDao);
    }

    public void muatSemua() throws DataAksesException {
        penggunaDao.muat();
        kamarDao.muat();
        tamuDao.muat();
        reservasiDao.muat();
    }

    public Path getFolder() {
        return folder;
    }

    public PenggunaDao getPenggunaDao() {
        return penggunaDao;
    }

    public KamarDao getKamarDao() {
        return kamarDao;
    }

    public TamuDao getTamuDao() {
        return tamuDao;
    }

    public ReservasiDao getReservasiDao() {
        return reservasiDao;
    }
}
