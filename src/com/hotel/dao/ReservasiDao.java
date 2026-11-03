package com.hotel.dao;

import com.hotel.exception.DataTidakDitemukanException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.Kamar;
import com.hotel.model.LayananTambahan;
import com.hotel.model.Reservasi;
import com.hotel.model.StatusReservasi;
import com.hotel.model.Tamu;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Reservasi menyimpan ID tamu dan nomor kamar, lalu menghubungkannya kembali ke objeknya saat dibaca. */
public class ReservasiDao extends FileDao<Reservasi> {

    private final KamarDao kamarDao;
    private final TamuDao tamuDao;

    public ReservasiDao(Path folder, KamarDao kamarDao, TamuDao tamuDao) {
        super(folder, "reservasi.csv");
        this.kamarDao = kamarDao;
        this.tamuDao = tamuDao;
    }

    public String buatIdBaru() {
        return idBerikutnya("R", 4);
    }

    public List<Reservasi> findByKamar(String nomorKamar) {
        return data.values().stream()
                .filter(r -> r.getKamar().getNomor().equals(nomorKamar))
                .collect(Collectors.toList());
    }

    public List<Reservasi> findByTamu(String idTamu) {
        return data.values().stream()
                .filter(r -> r.getTamu().getId().equals(idTamu))
                .collect(Collectors.toList());
    }

    /** Setelah objek Kamar diganti (edit), semua reservasi diarahkan ke objek yang baru. */
    public void perbaruiReferensi(Kamar kamarBaru) {
        data.values().forEach(r -> r.sinkronkan(kamarBaru));
    }

    /** Overload untuk Tamu. */
    public void perbaruiReferensi(Tamu tamuBaru) {
        data.values().forEach(r -> r.sinkronkan(tamuBaru));
    }

    @Override
    protected String header() {
        return "id;id_tamu;nomor_kamar;check_in;check_out;jumlah_tamu;status;layanan";
    }

    @Override
    protected Reservasi parse(String[] k) throws ValidasiException, DataTidakDitemukanException {
        Tamu tamu = tamuDao.findById(k[1])
                .orElseThrow(() -> new DataTidakDitemukanException("Tamu " + k[1] + " tidak ditemukan"));
        Kamar kamar = kamarDao.findById(k[2])
                .orElseThrow(() -> new DataTidakDitemukanException("Kamar " + k[2] + " tidak ditemukan"));
        List<LayananTambahan> layanan = new ArrayList<>();
        if (k.length > 7 && !k[7].isBlank()) {
            Arrays.stream(k[7].split(",")).map(String::trim).map(LayananTambahan::valueOf).forEach(layanan::add);
        }
        return new Reservasi(k[0], tamu, kamar, LocalDate.parse(k[3]), LocalDate.parse(k[4]),
                Integer.parseInt(k[5]), layanan, StatusReservasi.valueOf(k[6]));
    }

    @Override
    protected List<Reservasi> dataAwal() throws ValidasiException {
        LocalDate hari = LocalDate.now();
        List<Reservasi> awal = new ArrayList<>();
        awal.add(buatAwal("R0001", "T001", "101", hari.minusDays(2), hari.plusDays(1), 2,
                List.of(LayananTambahan.SARAPAN), StatusReservasi.CHECK_IN));
        awal.add(buatAwal("R0002", "T002", "201", hari, hari.plusDays(2), 2,
                List.of(LayananTambahan.LAUNDRY), StatusReservasi.DIPESAN));
        awal.add(buatAwal("R0003", "T003", "301", hari.minusDays(10), hari.minusDays(6), 3,
                List.of(LayananTambahan.SPA, LayananTambahan.ANTAR_JEMPUT), StatusReservasi.SELESAI));
        awal.add(buatAwal("R0004", "T004", "102", hari.plusDays(1), hari.plusDays(2), 1,
                List.of(), StatusReservasi.DIBATALKAN));
        awal.add(buatAwal("R0005", "T005", "202", hari.plusDays(5), hari.plusDays(8), 2,
                List.of(LayananTambahan.SARAPAN), StatusReservasi.DIPESAN));
        awal.add(buatAwal("R0006", "T001", "103", hari.minusDays(1), hari, 1,
                List.of(), StatusReservasi.CHECK_IN));
        return awal;
    }

    private Reservasi buatAwal(String id, String idTamu, String noKamar, LocalDate in, LocalDate out, int jumlah,
                               List<LayananTambahan> layanan, StatusReservasi status) throws ValidasiException {
        Tamu tamu = tamuDao.findById(idTamu).orElseThrow(() -> new ValidasiException("Tamu awal " + idTamu + " tidak ada"));
        Kamar kamar = kamarDao.findById(noKamar).orElseThrow(() -> new ValidasiException("Kamar awal " + noKamar + " tidak ada"));
        return new Reservasi(id, tamu, kamar, in, out, jumlah, layanan, status);
    }
}
