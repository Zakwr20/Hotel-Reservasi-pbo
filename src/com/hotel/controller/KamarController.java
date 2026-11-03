package com.hotel.controller;

import com.hotel.dao.KamarDao;
import com.hotel.dao.ReservasiDao;
import com.hotel.exception.AksesDitolakException;
import com.hotel.exception.DataAksesException;
import com.hotel.exception.DataTerpakaiException;
import com.hotel.exception.DataTidakDitemukanException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.HakAkses;
import com.hotel.model.JenisKamar;
import com.hotel.model.Kamar;
import com.hotel.model.KamarFactory;
import com.hotel.model.Reservasi;
import com.hotel.model.StatusKamar;
import com.hotel.util.FormatUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KamarController {

    private final KamarDao kamarDao;
    private final ReservasiDao reservasiDao;
    private final AuthController auth;

    public KamarController(KamarDao kamarDao, ReservasiDao reservasiDao, AuthController auth) {
        this.kamarDao = kamarDao;
        this.reservasiDao = reservasiDao;
        this.auth = auth;
    }

    public List<Kamar> getSemua() {
        return urut(kamarDao.findAll());
    }

    /** Pencarian berdasarkan kata kunci (nomor/fasilitas) dengan filter jenis & status (null = semua). */
    public List<Kamar> cari(String kataKunci, JenisKamar jenis, StatusKamar status) {
        String kunci = kataKunci == null ? "" : kataKunci.trim().toLowerCase();
        return urut(kamarDao.findAll().stream()
                .filter(k -> jenis == null || k.getJenis() == jenis)
                .filter(k -> status == null || k.getStatus() == status)
                .filter(k -> kunci.isEmpty()
                        || k.getNomor().toLowerCase().contains(kunci)
                        || k.getFasilitas().toLowerCase().contains(kunci))
                .collect(Collectors.toList()));
    }

    public Kamar tambah(JenisKamar jenis, String nomor, int lantai, String hargaTeks, int kapasitas,
                        StatusKamar status, String atributKhusus)
            throws ValidasiException, DataAksesException, AksesDitolakException {
        auth.wajibAkses(HakAkses.KELOLA_KAMAR);
        double harga = FormatUtil.parseAngka(hargaTeks, "harga", "Harga per malam");
        Kamar kamar = KamarFactory.buat(jenis, nomor, lantai, harga, kapasitas, status, atributKhusus);
        kamarDao.insert(kamar);
        return kamar;
    }

    public Kamar ubah(JenisKamar jenis, String nomor, int lantai, String hargaTeks, int kapasitas,
                      StatusKamar status, String atributKhusus)
            throws ValidasiException, DataAksesException, AksesDitolakException, DataTidakDitemukanException {
        auth.wajibAkses(HakAkses.KELOLA_KAMAR);
        double harga = FormatUtil.parseAngka(hargaTeks, "harga", "Harga per malam");
        Kamar baru = KamarFactory.buat(jenis, nomor, lantai, harga, kapasitas, status, atributKhusus);
        kamarDao.findById(baru.getNomor())
                .orElseThrow(() -> new DataTidakDitemukanException("Kamar " + nomor + " tidak ditemukan."));

        for (Reservasi r : reservasiDao.findByKamar(baru.getNomor())) {
            if (!r.isAktif()) {
                continue;
            }
            if (r.getJumlahTamu() > baru.getKapasitasMaksimal()) {
                throw new ValidasiException("kapasitas", "Kapasitas terlalu kecil: reservasi aktif " + r.getId()
                        + " berisi " + r.getJumlahTamu() + " tamu.");
            }
            if (status == StatusKamar.PERBAIKAN) {
                throw new ValidasiException("status", "Kamar masih memiliki reservasi aktif (" + r.getId()
                        + "), tidak bisa diubah ke status Perbaikan.");
            }
        }
        kamarDao.update(baru);
        reservasiDao.perbaruiReferensi(baru);
        return baru;
    }

    public void hapus(String nomor)
            throws AksesDitolakException, DataTerpakaiException, DataTidakDitemukanException, DataAksesException {
        auth.wajibAkses(HakAkses.KELOLA_KAMAR);
        int jumlah = reservasiDao.findByKamar(nomor).size();
        if (jumlah > 0) {
            throw new DataTerpakaiException("Kamar " + nomor + " memiliki " + jumlah
                    + " riwayat reservasi sehingga tidak bisa dihapus.\nUbah statusnya menjadi Perbaikan jika kamar tidak dipakai.");
        }
        kamarDao.delete(nomor);
    }

    private static List<Kamar> urut(List<Kamar> daftar) {
        daftar.sort(Comparator.comparing(Kamar::getNomor));
        return daftar;
    }
}
