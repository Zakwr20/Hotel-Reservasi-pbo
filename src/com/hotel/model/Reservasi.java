package com.hotel.model;

import com.hotel.exception.ValidasiException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Reservasi kamar oleh seorang tamu, termasuk layanan tambahan. */
public class Reservasi implements Persistable {

    public static final int MAKS_MALAM = 30;

    private final String id;
    private Tamu tamu;
    private Kamar kamar;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int jumlahTamu;
    private StatusReservasi status;
    private final List<LayananTambahan> layanan = new ArrayList<>();

    public Reservasi(String id, Tamu tamu, Kamar kamar, LocalDate checkIn, LocalDate checkOut,
                     int jumlahTamu, List<LayananTambahan> layanan, StatusReservasi status) throws ValidasiException {
        if (id == null || id.isBlank()) {
            throw new ValidasiException("id", "ID reservasi wajib ada.");
        }
        this.id = id;
        setTamu(tamu);
        setKamar(kamar);
        setTanggal(checkIn, checkOut);
        setJumlahTamu(jumlahTamu);
        setLayanan(layanan);
        setStatus(status);
    }

    // ===== logika bisnis =====

    public int getJumlahMalam() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /**
     * POLIMORFISME: kamar bertipe Kamar (induk), tetapi yang dijalankan adalah
     * hitungBiaya() milik KamarStandard / KamarDeluxe / KamarSuite sesuai objek aslinya.
     */
    public double hitungBiayaKamar() {
        return kamar.hitungBiaya(getJumlahMalam(), jumlahTamu);
    }

    public double hitungBiayaLayanan() {
        int malam = getJumlahMalam();
        return layanan.stream().mapToDouble(l -> l.hitungBiaya(malam, jumlahTamu)).sum();
    }

    public double hitungDiskon() {
        return tamu.isMember() ? hitungBiayaKamar() * Tamu.DISKON_MEMBER : 0;
    }

    public double hitungTotal() {
        return hitungBiayaKamar() + hitungBiayaLayanan() - hitungDiskon();
    }

    /** True jika reservasi aktif ini tumpang tindih dengan rentang [masuk, keluar). */
    public boolean bentrokDengan(LocalDate masuk, LocalDate keluar) {
        return status.isAktif() && masuk.isBefore(checkOut) && keluar.isAfter(checkIn);
    }

    public boolean isAktif() {
        return status.isAktif();
    }

    /** Mengganti referensi ke objek kamar terbaru setelah data kamar diubah (ID sama). */
    public void sinkronkan(Kamar kamarBaru) {
        if (kamarBaru != null && kamarBaru.getNomor().equals(kamar.getNomor())) {
            this.kamar = kamarBaru;
        }
    }

    /** Overload: mengganti referensi ke objek tamu terbaru setelah data tamu diubah. */
    public void sinkronkan(Tamu tamuBaru) {
        if (tamuBaru != null && tamuBaru.getId().equals(tamu.getId())) {
            this.tamu = tamuBaru;
        }
    }

    // ===== getter & setter dengan validasi =====

    @Override
    public String getId() {
        return id;
    }

    public Tamu getTamu() {
        return tamu;
    }

    public final void setTamu(Tamu tamu) throws ValidasiException {
        if (tamu == null) {
            throw new ValidasiException("tamu", "Tamu wajib dipilih.");
        }
        this.tamu = tamu;
    }

    public Kamar getKamar() {
        return kamar;
    }

    public final void setKamar(Kamar kamar) throws ValidasiException {
        if (kamar == null) {
            throw new ValidasiException("kamar", "Kamar wajib dipilih.");
        }
        if (jumlahTamu > kamar.getKapasitasMaksimal()) {
            throw new ValidasiException("kamar", "Kamar " + kamar.getNomor() + " maksimal "
                    + kamar.getKapasitasMaksimal() + " tamu.");
        }
        cekKonflikSarapan(kamar, layanan);
        this.kamar = kamar;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public final void setTanggal(LocalDate checkIn, LocalDate checkOut) throws ValidasiException {
        if (checkIn == null) {
            throw new ValidasiException("checkIn", "Tanggal check-in wajib diisi.");
        }
        if (checkOut == null) {
            throw new ValidasiException("checkOut", "Tanggal check-out wajib diisi.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidasiException("checkOut", "Tanggal check-out harus setelah tanggal check-in.");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > MAKS_MALAM) {
            throw new ValidasiException("checkOut", "Lama menginap maksimal " + MAKS_MALAM + " malam per reservasi.");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public int getJumlahTamu() {
        return jumlahTamu;
    }

    public final void setJumlahTamu(int jumlahTamu) throws ValidasiException {
        if (jumlahTamu < 1) {
            throw new ValidasiException("jumlahTamu", "Jumlah tamu minimal 1 orang.");
        }
        if (jumlahTamu > kamar.getKapasitasMaksimal()) {
            throw new ValidasiException("jumlahTamu", "Kamar " + kamar.getNomor() + " (kapasitas " + kamar.getKapasitas()
                    + ") maksimal " + kamar.getKapasitasMaksimal() + " tamu termasuk extra bed.");
        }
        this.jumlahTamu = jumlahTamu;
    }

    public List<LayananTambahan> getLayanan() {
        return Collections.unmodifiableList(layanan);
    }

    public final void setLayanan(Collection<LayananTambahan> daftar) throws ValidasiException {
        Collection<LayananTambahan> unik = daftar == null ? List.of() : new LinkedHashSet<>(daftar);
        cekKonflikSarapan(kamar, unik);
        layanan.clear();
        layanan.addAll(unik);
    }

    private static void cekKonflikSarapan(Kamar kamar, Collection<LayananTambahan> daftar) throws ValidasiException {
        if (kamar instanceof KamarDeluxe deluxe && deluxe.isTermasukSarapan()
                && daftar.contains(LayananTambahan.SARAPAN)) {
            throw new ValidasiException("layanan", "Kamar " + kamar.getNomor()
                    + " sudah termasuk sarapan, layanan Sarapan tidak perlu dipilih.");
        }
    }

    public StatusReservasi getStatus() {
        return status;
    }

    public final void setStatus(StatusReservasi status) throws ValidasiException {
        if (status == null) {
            throw new ValidasiException("status", "Status reservasi wajib ada.");
        }
        this.status = status;
    }

    @Override
    public String[] toRecord() {
        return new String[] {
            id,
            tamu.getId(),
            kamar.getNomor(),
            checkIn.toString(),
            checkOut.toString(),
            String.valueOf(jumlahTamu),
            status.name(),
            layanan.stream().map(Enum::name).collect(Collectors.joining(","))
        };
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Reservasi r && Objects.equals(id, r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id + " · " + tamu.getNama() + " · kamar " + kamar.getNomor();
    }
}
