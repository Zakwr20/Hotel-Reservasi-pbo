package com.hotel.controller;

import com.hotel.dao.ReservasiDao;
import com.hotel.dao.TamuDao;
import com.hotel.exception.AksesDitolakException;
import com.hotel.exception.DataAksesException;
import com.hotel.exception.DataTerpakaiException;
import com.hotel.exception.DataTidakDitemukanException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.HakAkses;
import com.hotel.model.JenisKelamin;
import com.hotel.model.Tamu;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TamuController {

    private final TamuDao tamuDao;
    private final ReservasiDao reservasiDao;
    private final AuthController auth;

    public TamuController(TamuDao tamuDao, ReservasiDao reservasiDao, AuthController auth) {
        this.tamuDao = tamuDao;
        this.reservasiDao = reservasiDao;
        this.auth = auth;
    }

    public List<Tamu> getSemua() {
        return tamuDao.findAll();
    }

    public List<Tamu> cari(String kataKunci) {
        String k = kataKunci == null ? "" : kataKunci.trim().toLowerCase();
        if (k.isEmpty()) {
            return getSemua();
        }
        return tamuDao.findAll().stream()
                .filter(t -> t.getId().toLowerCase().contains(k)
                        || t.getNama().toLowerCase().contains(k)
                        || t.getNoIdentitas().toLowerCase().contains(k)
                        || t.getTelepon().contains(k)
                        || t.getEmail().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    public Tamu tambah(String nama, String noIdentitas, String telepon, String email,
                       JenisKelamin jenisKelamin, boolean member)
            throws ValidasiException, DataAksesException, AksesDitolakException {
        auth.wajibAkses(HakAkses.KELOLA_TAMU);
        Tamu tamu = new Tamu(tamuDao.buatIdBaru(), nama, noIdentitas, telepon, email, jenisKelamin, member);
        cekIdentitasUnik(tamu);
        tamuDao.insert(tamu);
        return tamu;
    }

    public Tamu ubah(String id, String nama, String noIdentitas, String telepon, String email,
                     JenisKelamin jenisKelamin, boolean member)
            throws ValidasiException, DataAksesException, AksesDitolakException, DataTidakDitemukanException {
        auth.wajibAkses(HakAkses.KELOLA_TAMU);
        Tamu baru = new Tamu(id, nama, noIdentitas, telepon, email, jenisKelamin, member);
        cekIdentitasUnik(baru);
        tamuDao.update(baru);
        reservasiDao.perbaruiReferensi(baru);
        return baru;
    }

    public void hapus(String id)
            throws AksesDitolakException, DataTerpakaiException, DataTidakDitemukanException, DataAksesException {
        auth.wajibAkses(HakAkses.HAPUS_DATA);
        int jumlah = reservasiDao.findByTamu(id).size();
        if (jumlah > 0) {
            throw new DataTerpakaiException("Tamu ini memiliki " + jumlah
                    + " reservasi. Hapus reservasinya terlebih dahulu.");
        }
        tamuDao.delete(id);
    }

    private void cekIdentitasUnik(Tamu tamu) throws ValidasiException {
        Optional<Tamu> lain = tamuDao.findByNoIdentitas(tamu.getNoIdentitas());
        if (lain.isPresent() && !lain.get().getId().equals(tamu.getId())) {
            throw new ValidasiException("noIdentitas", "No. identitas sudah terdaftar atas nama "
                    + lain.get().getNama() + " (" + lain.get().getId() + ").");
        }
    }
}
