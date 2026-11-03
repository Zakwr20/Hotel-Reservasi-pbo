package com.hotel.app;

import com.hotel.controller.AuthController;
import com.hotel.controller.KamarController;
import com.hotel.controller.ReservasiController;
import com.hotel.controller.TamuController;
import com.hotel.dao.DataContext;
import com.hotel.exception.AksesDitolakException;
import com.hotel.exception.HotelException;
import com.hotel.exception.KamarTidakTersediaException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.JenisKelamin;
import com.hotel.model.Kamar;
import com.hotel.model.KamarStandard;
import com.hotel.model.LayananTambahan;
import com.hotel.model.Pengguna;
import com.hotel.model.Reservasi;
import com.hotel.model.StatusKamar;
import com.hotel.model.Tamu;
import com.hotel.util.FormatUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Pengujian lewat konsol untuk Progres 1 (Minggu 2): model, pewarisan, polimorfisme, DAO & file.
 * Memakai folder terpisah "data-uji" agar data aplikasi utama tidak tersentuh.
 * Jalankan: klik kanan ConsoleTest.java -> Run As -> Java Application.
 */
public class ConsoleTest {

    private static int lulus;
    private static int gagal;

    public static void main(String[] args) throws Exception {
        Path folder = Paths.get("data-uji");
        hapusFolder(folder);

        judul("1. Memuat DataContext (file dibuat otomatis dengan data awal)");
        DataContext ctx = new DataContext(folder);
        ctx.muatSemua();
        cek("10 kamar awal termuat", ctx.getKamarDao().count() == 10);
        cek("5 tamu awal termuat", ctx.getTamuDao().count() == 5);
        cek("6 reservasi awal termuat", ctx.getReservasiDao().count() == 6);
        cek("file kamar.csv dibuat", Files.exists(folder.resolve("kamar.csv")));

        judul("2. Polimorfisme: hitungBiaya(3) lewat referensi Kamar");
        List<Kamar> daftar = ctx.getKamarDao().findAll();
        for (Kamar k : daftar) {
            System.out.printf("   %-4s %-9s harga %-12s -> 3 malam = %s%n", k.getNomor(), k.getJenis(),
                    FormatUtil.rupiah(k.getHargaPerMalam()), FormatUtil.rupiah(k.hitungBiaya(3)));
        }
        Kamar standard = ctx.getKamarDao().findById("102").orElseThrow();
        Kamar deluxe = ctx.getKamarDao().findById("201").orElseThrow();
        Kamar suite = ctx.getKamarDao().findById("301").orElseThrow();
        cek("Standard 102: 350.000 x 3 = 1.050.000", standard.hitungBiaya(3) == 1_050_000);
        cek("Deluxe 201 (sarapan 2 org): 650.000x3 + 50.000x2x3 = 2.250.000", deluxe.hitungBiaya(3) == 2_250_000);
        cek("Suite 301: 1.500.000x3 -15% +10% = 4.207.500", Math.abs(suite.hitungBiaya(3) - 4_207_500) < 0.01);

        judul("3. Overloading hitungBiaya");
        cek("hitungBiaya(3, 3) menambah extra bed 150.000x3", standard.hitungBiaya(3, 3) == 1_050_000 + 450_000);
        LocalDate a = LocalDate.of(2026, 12, 1);
        cek("hitungBiaya(LocalDate, LocalDate) = hitungBiaya(3)", standard.hitungBiaya(a, a.plusDays(3)) == standard.hitungBiaya(3));

        judul("4. Enkapsulasi & custom exception (validasi setter)");
        harusGagal("harga negatif ditolak", () -> new KamarStandard("999", 1, -50_000, 2, StatusKamar.TERSEDIA, "Single"));
        harusGagal("kapasitas 0 ditolak", () -> standard.setKapasitas(0));
        harusGagal("telepon salah ditolak", () -> new Tamu("T999", "Coba", "3374011203900099", "12345", "", JenisKelamin.LAKI_LAKI, false));
        harusGagal("check-out sebelum check-in ditolak", () -> new Reservasi("RX", ctx.getTamuDao().findById("T001").orElseThrow(),
                standard, a, a.minusDays(1), 1, List.of(), com.hotel.model.StatusReservasi.DIPESAN));
        harusGagal("sarapan ganda di Deluxe 201 ditolak", () -> new Reservasi("RX", ctx.getTamuDao().findById("T001").orElseThrow(),
                deluxe, a, a.plusDays(1), 1, List.of(LayananTambahan.SARAPAN), com.hotel.model.StatusReservasi.DIPESAN));

        judul("5. Login & hak akses");
        AuthController auth = new AuthController(ctx.getPenggunaDao());
        harusGagal("password salah ditolak", () -> auth.login("admin", "salah".toCharArray()));
        Pengguna resepsionis = auth.login("resepsionis", "resep123".toCharArray());
        cek("login resepsionis berhasil", resepsionis != null);
        KamarController kamarCtrl = new KamarController(ctx.getKamarDao(), ctx.getReservasiDao(), auth);
        try {
            kamarCtrl.hapus("105");
            cek("resepsionis tidak boleh hapus kamar", false);
        } catch (AksesDitolakException e) {
            cek("resepsionis tidak boleh hapus kamar -> " + e.getMessage(), true);
        }
        auth.login("admin", "admin123".toCharArray());

        judul("6. CRUD Tamu lewat controller + persistensi file");
        TamuController tamuCtrl = new TamuController(ctx.getTamuDao(), ctx.getReservasiDao(), auth);
        Tamu baru = tamuCtrl.tambah("Uji Coba Konsol", "3374019999990001", "081299990000", "uji@email.com",
                JenisKelamin.PEREMPUAN, true);
        cek("tamu baru mendapat ID " + baru.getId(), baru.getId().equals("T006"));
        tamuCtrl.ubah(baru.getId(), "Uji Coba Diubah", baru.getNoIdentitas(), baru.getTelepon(), "", JenisKelamin.PEREMPUAN, false);
        DataContext ctx2 = new DataContext(folder);
        ctx2.muatSemua();
        cek("perubahan tersimpan di file (dibaca ulang)",
                ctx2.getTamuDao().findById("T006").map(Tamu::getNama).orElse("").equals("Uji Coba Diubah"));
        tamuCtrl.hapus("T006");
        cek("tamu terhapus", ctx.getTamuDao().findById("T006").isEmpty());

        judul("7. Reservasi: cek bentrok jadwal & alur check-in/out");
        ReservasiController resCtrl = new ReservasiController(ctx.getReservasiDao(), ctx.getKamarDao(), ctx.getTamuDao(), auth);
        LocalDate hariIni = LocalDate.now();
        try {
            resCtrl.buat("T003", "201", hariIni, hariIni.plusDays(1), 1, List.of());
            cek("kamar 201 bentrok dengan R0002 harus ditolak", false);
        } catch (KamarTidakTersediaException e) {
            cek("bentrok ditolak -> " + e.getMessage(), true);
        }
        Reservasi r = resCtrl.buat("T003", "302", hariIni, hariIni.plusDays(3), 4,
                List.of(LayananTambahan.SARAPAN, LayananTambahan.ANTAR_JEMPUT));
        System.out.printf("   %s total %s (kamar %s + layanan %s)%n", r.getId(), FormatUtil.rupiah(r.hitungTotal()),
                FormatUtil.rupiah(r.hitungBiayaKamar()), FormatUtil.rupiah(r.hitungBiayaLayanan()));
        resCtrl.checkIn(r.getId());
        cek("setelah check-in kamar 302 berstatus Terisi", ctx.getKamarDao().findById("302").orElseThrow().getStatus() == StatusKamar.TERISI);
        resCtrl.checkOut(r.getId());
        cek("setelah check-out kamar 302 kembali Tersedia", ctx.getKamarDao().findById("302").orElseThrow().getStatus() == StatusKamar.TERSEDIA);
        cek("kamar 302 tersedia lagi untuk hari ini", resCtrl.kamarTersedia(hariIni, hariIni.plusDays(1), null).stream()
                .anyMatch(k -> k.getNomor().equals("302")));

        judul("8. Baris file rusak dilewati tanpa membuat aplikasi crash");
        Files.writeString(folder.resolve("kamar.csv"), "STANDARD;777;1;-5;2;TERSEDIA;Single\n",
                java.nio.file.StandardOpenOption.APPEND);
        DataContext ctx3 = new DataContext(folder);
        ctx3.muatSemua();
        cek("1 baris rusak terdeteksi", ctx3.getKamarDao().getBarisRusak() == 1);

        System.out.printf("%n===== HASIL: %d lulus, %d gagal =====%n", lulus, gagal);
        if (gagal > 0) {
            System.exit(1);
        }
    }

    // ===== helper kecil =====

    @FunctionalInterface
    private interface Aksi {
        void jalankan() throws HotelException;
    }

    private static void harusGagal(String nama, Aksi aksi) {
        try {
            aksi.jalankan();
            cek(nama, false);
        } catch (ValidasiException | com.hotel.exception.LoginGagalException e) {
            cek(nama + " -> \"" + e.getMessage() + "\"", true);
        } catch (HotelException e) {
            cek(nama + " (exception tak terduga: " + e + ")", false);
        }
    }

    private static void cek(String nama, boolean ok) {
        if (ok) {
            lulus++;
        } else {
            gagal++;
        }
        System.out.println("   [" + (ok ? "LULUS" : "GAGAL") + "] " + nama);
    }

    private static void judul(String teks) {
        System.out.println();
        System.out.println(teks);
    }

    private static void hapusFolder(Path folder) throws IOException {
        if (Files.notExists(folder)) {
            return;
        }
        try (Stream<Path> isi = Files.walk(folder)) {
            for (Path p : isi.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }
}
