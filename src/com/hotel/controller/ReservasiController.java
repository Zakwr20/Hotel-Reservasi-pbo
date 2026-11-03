package com.hotel.controller;

import com.hotel.dao.KamarDao;
import com.hotel.dao.ReservasiDao;
import com.hotel.dao.TamuDao;
import com.hotel.exception.AksesDitolakException;
import com.hotel.exception.DataAksesException;
import com.hotel.exception.DataTerpakaiException;
import com.hotel.exception.DataTidakDitemukanException;
import com.hotel.exception.KamarTidakTersediaException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.HakAkses;
import com.hotel.model.Kamar;
import com.hotel.model.LayananTambahan;
import com.hotel.model.Reservasi;
import com.hotel.model.StatusKamar;
import com.hotel.model.StatusReservasi;
import com.hotel.model.Tamu;
import com.hotel.util.FormatUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Logika bisnis reservasi: cek ketersediaan, buat/ubah, check-in, check-out, batal, hapus. */
public class ReservasiController {

    private final ReservasiDao reservasiDao;
    private final KamarDao kamarDao;
    private final TamuDao tamuDao;
    private final AuthController auth;

    public ReservasiController(ReservasiDao reservasiDao, KamarDao kamarDao, TamuDao tamuDao, AuthController auth) {
        this.reservasiDao = reservasiDao;
        this.kamarDao = kamarDao;
        this.tamuDao = tamuDao;
        this.auth = auth;
    }

    public List<Reservasi> getSemua() {
        return urut(reservasiDao.findAll());
    }

    public List<Reservasi> cari(String kataKunci, StatusReservasi status) {
        String k = kataKunci == null ? "" : kataKunci.trim().toLowerCase();
        return urut(reservasiDao.findAll().stream()
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> k.isEmpty()
                        || r.getId().toLowerCase().contains(k)
                        || r.getTamu().getNama().toLowerCase().contains(k)
                        || r.getKamar().getNomor().toLowerCase().contains(k))
                .collect(Collectors.toList()));
    }

    public List<Tamu> getDaftarTamu() {
        return tamuDao.findAll();
    }

    /** Kamar yang bisa dipesan pada rentang tanggal tertentu (abaikanId: reservasi yang sedang diedit). */
    public List<Kamar> kamarTersedia(LocalDate checkIn, LocalDate checkOut, String abaikanId) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return List.of();
        }
        return kamarDao.findAll().stream()
                .filter(Kamar::isBisaDipesan)
                .filter(k -> cariBentrok(k.getNomor(), checkIn, checkOut, abaikanId) == null)
                .sorted(Comparator.comparing(Kamar::getNomor))
                .collect(Collectors.toList());
    }

    /** Membuat objek reservasi sementara (tidak disimpan) untuk menampilkan estimasi biaya di form. */
    public Reservasi simulasi(String idLama, Tamu tamu, Kamar kamar, LocalDate checkIn, LocalDate checkOut,
                              int jumlahTamu, List<LayananTambahan> layanan) throws ValidasiException {
        return new Reservasi(idLama == null ? "PREVIEW" : idLama, tamu, kamar, checkIn, checkOut,
                jumlahTamu, layanan, StatusReservasi.DIPESAN);
    }

    public Reservasi buat(String idTamu, String nomorKamar, LocalDate checkIn, LocalDate checkOut,
                          int jumlahTamu, List<LayananTambahan> layanan)
            throws ValidasiException, KamarTidakTersediaException, DataAksesException,
            AksesDitolakException, DataTidakDitemukanException {
        auth.wajibAkses(HakAkses.KELOLA_RESERVASI);
        if (checkIn != null && checkIn.isBefore(LocalDate.now())) {
            throw new ValidasiException("checkIn", "Tanggal check-in tidak boleh sebelum hari ini.");
        }
        Tamu tamu = ambilTamu(idTamu);
        Kamar kamar = ambilKamar(nomorKamar);
        Reservasi baru = new Reservasi(reservasiDao.buatIdBaru(), tamu, kamar, checkIn, checkOut,
                jumlahTamu, layanan, StatusReservasi.DIPESAN);
        cekKetersediaan(kamar, checkIn, checkOut, null);
        reservasiDao.insert(baru);
        return baru;
    }

    public Reservasi ubah(String id, String idTamu, String nomorKamar, LocalDate checkIn, LocalDate checkOut,
                          int jumlahTamu, List<LayananTambahan> layanan)
            throws ValidasiException, KamarTidakTersediaException, DataAksesException,
            AksesDitolakException, DataTidakDitemukanException {
        auth.wajibAkses(HakAkses.KELOLA_RESERVASI);
        Reservasi lama = ambil(id);
        if (lama.getStatus() != StatusReservasi.DIPESAN) {
            throw new ValidasiException("Hanya reservasi berstatus Dipesan yang dapat diubah (status sekarang: "
                    + lama.getStatus() + ").");
        }
        if (checkIn != null && !checkIn.equals(lama.getCheckIn()) && checkIn.isBefore(LocalDate.now())) {
            throw new ValidasiException("checkIn", "Tanggal check-in baru tidak boleh sebelum hari ini.");
        }
        Kamar kamar = ambilKamar(nomorKamar);
        Reservasi baru = new Reservasi(id, ambilTamu(idTamu), kamar, checkIn, checkOut,
                jumlahTamu, layanan, lama.getStatus());
        cekKetersediaan(kamar, checkIn, checkOut, id);
        reservasiDao.update(baru);
        return baru;
    }

    public Reservasi checkIn(String id)
            throws ValidasiException, DataAksesException, AksesDitolakException, DataTidakDitemukanException {
        auth.wajibAkses(HakAkses.KELOLA_RESERVASI);
        Reservasi r = ambil(id);
        if (r.getStatus() != StatusReservasi.DIPESAN) {
            throw new ValidasiException("Check-in hanya untuk reservasi berstatus Dipesan.");
        }
        LocalDate hariIni = LocalDate.now();
        if (hariIni.isBefore(r.getCheckIn())) {
            throw new ValidasiException("Check-in baru dapat dilakukan mulai " + FormatUtil.tanggal(r.getCheckIn()) + ".");
        }
        if (!hariIni.isBefore(r.getCheckOut())) {
            throw new ValidasiException("Tanggal menginap sudah lewat. Batalkan reservasi ini atau buat reservasi baru.");
        }
        gantiStatus(r, StatusReservasi.CHECK_IN);
        gantiStatusKamar(r.getKamar(), StatusKamar.TERISI);
        return r;
    }

    public Reservasi checkOut(String id)
            throws ValidasiException, DataAksesException, AksesDitolakException, DataTidakDitemukanException {
        auth.wajibAkses(HakAkses.KELOLA_RESERVASI);
        Reservasi r = ambil(id);
        if (r.getStatus() != StatusReservasi.CHECK_IN) {
            throw new ValidasiException("Check-out hanya untuk tamu yang sedang menginap (status Check-in).");
        }
        gantiStatus(r, StatusReservasi.SELESAI);
        gantiStatusKamar(r.getKamar(), StatusKamar.TERSEDIA);
        return r;
    }

    public Reservasi batalkan(String id)
            throws ValidasiException, DataAksesException, AksesDitolakException, DataTidakDitemukanException {
        auth.wajibAkses(HakAkses.KELOLA_RESERVASI);
        Reservasi r = ambil(id);
        if (r.getStatus() != StatusReservasi.DIPESAN) {
            throw new ValidasiException("Hanya reservasi berstatus Dipesan yang dapat dibatalkan.");
        }
        gantiStatus(r, StatusReservasi.DIBATALKAN);
        return r;
    }

    public void hapus(String id)
            throws AksesDitolakException, DataTerpakaiException, DataTidakDitemukanException, DataAksesException {
        auth.wajibAkses(HakAkses.HAPUS_DATA);
        Reservasi r = ambil(id);
        if (r.getStatus() == StatusReservasi.CHECK_IN) {
            throw new DataTerpakaiException("Tamu masih menginap. Lakukan check-out sebelum menghapus reservasi.");
        }
        reservasiDao.delete(id);
    }

    // ===== helper =====

    private void cekKetersediaan(Kamar kamar, LocalDate checkIn, LocalDate checkOut, String abaikanId)
            throws KamarTidakTersediaException {
        if (!kamar.isBisaDipesan()) {
            throw new KamarTidakTersediaException("Kamar " + kamar.getNomor() + " sedang dalam perbaikan.");
        }
        Reservasi bentrok = cariBentrok(kamar.getNomor(), checkIn, checkOut, abaikanId);
        if (bentrok != null) {
            throw new KamarTidakTersediaException("Kamar " + kamar.getNomor() + " sudah dipesan ("
                    + bentrok.getId() + ") pada " + FormatUtil.tanggal(bentrok.getCheckIn()) + " s.d. "
                    + FormatUtil.tanggal(bentrok.getCheckOut()) + ".");
        }
    }

    private Reservasi cariBentrok(String nomorKamar, LocalDate in, LocalDate out, String abaikanId) {
        return reservasiDao.findByKamar(nomorKamar).stream()
                .filter(r -> !r.getId().equals(abaikanId))
                .filter(r -> r.bentrokDengan(in, out))
                .findFirst()
                .orElse(null);
    }

    private void gantiStatus(Reservasi r, StatusReservasi baru)
            throws ValidasiException, DataAksesException, DataTidakDitemukanException {
        StatusReservasi lama = r.getStatus();
        r.setStatus(baru);
        try {
            reservasiDao.update(r);
        } catch (DataAksesException | DataTidakDitemukanException e) {
            r.setStatus(lama); // kembalikan jika gagal disimpan
            throw e;
        }
    }

    private void gantiStatusKamar(Kamar kamar, StatusKamar status)
            throws ValidasiException, DataAksesException, DataTidakDitemukanException {
        kamar.setStatus(status);
        kamarDao.update(kamar);
    }

    private Reservasi ambil(String id) throws DataTidakDitemukanException {
        return reservasiDao.findById(id)
                .orElseThrow(() -> new DataTidakDitemukanException("Reservasi " + id + " tidak ditemukan."));
    }

    private Tamu ambilTamu(String id) throws DataTidakDitemukanException {
        return tamuDao.findById(id)
                .orElseThrow(() -> new DataTidakDitemukanException("Tamu " + id + " tidak ditemukan."));
    }

    private Kamar ambilKamar(String nomor) throws DataTidakDitemukanException {
        return kamarDao.findById(nomor)
                .orElseThrow(() -> new DataTidakDitemukanException("Kamar " + nomor + " tidak ditemukan."));
    }

    private static List<Reservasi> urut(List<Reservasi> daftar) {
        daftar.sort(Comparator.comparing(Reservasi::getCheckIn).reversed().thenComparing(Reservasi::getId));
        return daftar;
    }
}
