package com.hotel.model;

import com.hotel.exception.ValidasiException;
import com.hotel.util.FormatUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * ABSTRAKSI: kelas induk abstrak untuk semua jenis kamar.
 * Kelas turunan: KamarStandard, KamarDeluxe, KamarSuite.
 *
 * POLIMORFISME: hitungBiaya(int malam) dihitung berbeda oleh tiap turunan
 * dan dipanggil lewat referensi bertipe Kamar (lihat Reservasi#hitungBiayaKamar).
 *
 * OVERLOADING: hitungBiaya(int), hitungBiaya(int, int), hitungBiaya(LocalDate, LocalDate).
 */
public abstract class Kamar implements Persistable {

    public static final double BIAYA_EXTRA_BED = 150_000;
    public static final double HARGA_MAKSIMAL = 100_000_000;

    // ENKAPSULASI: semua atribut private, diakses lewat getter/setter yang memvalidasi
    private String nomor;
    private int lantai;
    private double hargaPerMalam;
    private int kapasitas;
    private StatusKamar status;

    protected Kamar(String nomor, int lantai, double hargaPerMalam, int kapasitas, StatusKamar status)
            throws ValidasiException {
        setNomor(nomor);
        setLantai(lantai);
        setHargaPerMalam(hargaPerMalam);
        setKapasitas(kapasitas);
        setStatus(status);
    }

    // ===== method abstrak: wajib diimplementasikan kelas turunan =====

    public abstract JenisKamar getJenis();

    /** Biaya kamar untuk sejumlah malam — rumusnya berbeda per jenis kamar. */
    public abstract double hitungBiaya(int malam);

    public abstract String getFasilitas();

    /** Atribut khusus milik kelas turunan, untuk disimpan ke file. */
    protected abstract String getAtributKhusus();

    // ===== overloading =====

    /** Overload: memperhitungkan extra bed jika jumlah tamu melebihi kapasitas. */
    public double hitungBiaya(int malam, int jumlahTamu) {
        int kelebihan = Math.max(0, jumlahTamu - kapasitas);
        return hitungBiaya(malam) + kelebihan * BIAYA_EXTRA_BED * malam;
    }

    /** Overload: menghitung jumlah malam dari tanggal check-in dan check-out. */
    public double hitungBiaya(LocalDate checkIn, LocalDate checkOut) {
        return hitungBiaya((int) ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    protected static void validasiMalam(int malam) {
        if (malam < 1) {
            throw new IllegalArgumentException("Jumlah malam minimal 1, diberikan: " + malam);
        }
    }

    /** Kapasitas maksimal termasuk 1 extra bed. */
    public int getKapasitasMaksimal() {
        return kapasitas + 1;
    }

    public boolean isBisaDipesan() {
        return status != StatusKamar.PERBAIKAN;
    }

    // ===== getter & setter dengan validasi =====

    @Override
    public String getId() {
        return nomor;
    }

    public String getNomor() {
        return nomor;
    }

    public final void setNomor(String nomor) throws ValidasiException {
        if (nomor == null || nomor.isBlank()) {
            throw new ValidasiException("nomor", "Nomor kamar wajib diisi.");
        }
        String n = nomor.trim().toUpperCase();
        if (!n.matches("[A-Z0-9-]{1,6}")) {
            throw new ValidasiException("nomor", "Nomor kamar hanya boleh huruf, angka, atau '-' (maks. 6 karakter).");
        }
        this.nomor = n;
    }

    public int getLantai() {
        return lantai;
    }

    public final void setLantai(int lantai) throws ValidasiException {
        if (lantai < 1 || lantai > 50) {
            throw new ValidasiException("lantai", "Lantai harus antara 1 sampai 50.");
        }
        this.lantai = lantai;
    }

    public double getHargaPerMalam() {
        return hargaPerMalam;
    }

    public final void setHargaPerMalam(double hargaPerMalam) throws ValidasiException {
        if (hargaPerMalam <= 0) {
            throw new ValidasiException("harga", "Harga per malam tidak boleh nol atau negatif.");
        }
        if (hargaPerMalam > HARGA_MAKSIMAL) {
            throw new ValidasiException("harga", "Harga per malam maksimal " + FormatUtil.rupiah(HARGA_MAKSIMAL) + ".");
        }
        this.hargaPerMalam = hargaPerMalam;
    }

    public int getKapasitas() {
        return kapasitas;
    }

    public final void setKapasitas(int kapasitas) throws ValidasiException {
        if (kapasitas < 1 || kapasitas > 10) {
            throw new ValidasiException("kapasitas", "Kapasitas kamar harus 1 sampai 10 orang.");
        }
        this.kapasitas = kapasitas;
    }

    public StatusKamar getStatus() {
        return status;
    }

    public final void setStatus(StatusKamar status) throws ValidasiException {
        if (status == null) {
            throw new ValidasiException("status", "Status kamar wajib dipilih.");
        }
        this.status = status;
    }

    @Override
    public String[] toRecord() {
        return new String[] {
            getJenis().name(),
            nomor,
            String.valueOf(lantai),
            String.valueOf(Math.round(hargaPerMalam)),
            String.valueOf(kapasitas),
            status.name(),
            getAtributKhusus()
        };
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Kamar k && Objects.equals(nomor, k.nomor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nomor);
    }

    @Override
    public String toString() {
        return nomor + " · " + getJenis() + " · " + FormatUtil.rupiah(hargaPerMalam)
                + "/malam · maks " + getKapasitasMaksimal() + " org";
    }
}
