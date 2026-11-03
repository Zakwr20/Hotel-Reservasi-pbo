# Sistem Reservasi Hotel — Tugas Proyek PBO (Java Swing, MVC)

Aplikasi desktop untuk mengelola **kamar, tamu, dan reservasi** sebuah hotel (fiktif: *Hotel Puri Asri*).
Dibangun dengan Java Swing, arsitektur Model–View–Controller, dan penyimpanan file CSV.

---

## 1. Cara menjalankan di Eclipse

1. Ekstrak zip → **File ▸ Import ▸ General ▸ Existing Projects into Workspace** → pilih folder `HotelReservasi`.
2. Pastikan JDK **17 atau lebih baru** terpasang (project memakai `JavaSE-17`).
   Jika Eclipse memberi tanda merah pada JRE: klik kanan project ▸ *Properties ▸ Java Build Path ▸ Libraries* → ganti JRE ke JDK yang ada.
3. Jalankan `srl/app/Main.javac/com/hote` → *Run As ▸ Java Application*.
4. Login dengan akun demo:

| Username | Password | Peran | Hak akses |
|---|---|---|---|
| `admin` | `admin123` | Admin | Semua (termasuk kelola kamar & hapus data) |
| `resepsionis` | `resep123` | Resepsionis | Tamu & reservasi saja, tidak bisa hapus, kamar hanya lihat |

Folder `data/` (berisi `kamar.csv`, `tamu.csv`, `reservasi.csv`, `pengguna.csv`) dibuat otomatis beserta data contoh
saat pertama kali dijalankan. **Hapus folder `data/` untuk reset ke data awal.**

### Uji lewat konsol (Progres 1 — Minggu 2)
Jalankan `src/com/hotel/app/ConsoleTest.java`. Program ini menguji model, pewarisan, polimorfisme, validasi,
DAO/file, hak akses, dan cek bentrok jadwal tanpa GUI (memakai folder terpisah `data-uji/`). Hasil yang diharapkan:
`===== HASIL: 25 lulus, 0 gagal =====`.

---

## 2. Struktur package (MVC)

```
src/com/hotel/
├── app/         Main (titik masuk), ConsoleTest (uji konsol)
├── model/       MODEL — kelas domain & logika bisnis murni
│                Kamar (abstract) → KamarStandard, KamarDeluxe, KamarSuite
│                Pengguna (abstract) → Admin, Resepsionis
│                Tamu, Reservasi, KamarFactory, Persistable (interface)
│                enum: JenisKamar, StatusKamar, StatusReservasi, JenisKelamin, LayananTambahan, HakAkses
├── exception/   HotelException (induk) → ValidasiException, DataAksesException, KamarTidakTersediaException,
│                DataTidakDitemukanException, DataTerpakaiException, AksesDitolakException, LoginGagalException
├── dao/         Dao<T> (interface generik) → FileDao<T> (abstract) → KamarDao, TamuDao, ReservasiDao, PenggunaDao
│                DataContext (memuat semua DAO berurutan)
├── controller/  CONTROLLER — AuthController, KamarController, TamuController, ReservasiController, DashboardController
├── view/        VIEW — LoginFrame, MainFrame, DashboardPanel, KamarPanel, TamuPanel, ReservasiPanel,
│                KamarDialog, TamuDialog, ReservasiDialog + kerangka AbstractCrudPanel, AbstractFormDialog, BaseTableModel
└── util/        CsvUtil, FormatUtil (Rupiah/tanggal), PasswordUtil (SHA-256)
```

Alur MVC: **View** hanya memanggil **Controller** → Controller memvalidasi hak akses & aturan bisnis lalu memanggil
**DAO** → DAO menyimpan objek **Model** ke file. View tidak pernah mengakses DAO atau file secara langsung.

Class diagram: `docs/class-diagram.png` (sumber Mermaid: `docs/class-diagram.mmd`). Tangkapan layar: `docs/screenshots/`.

---

## 3. Pemetaan ke ketentuan tugas

### A. Konsep OOP

| Ketentuan | Implementasi (file) |
|---|---|
| ≥ 8 kelas model/logika | 30+ kelas di `model/`, `dao/`, `controller/`, `exception/`, `util/` |
| Enkapsulasi + validasi di setter | Semua atribut `private`; mis. `Kamar.setHargaPerMalam()` menolak harga ≤ 0, `Tamu.setTelepon()` cek format, `Reservasi.setTanggal()` cek check-out > check-in |
| Pewarisan + `super` | `Kamar` → `KamarStandard`/`KamarDeluxe`/`KamarSuite` (konstruktor memanggil `super(...)`); hierarki kedua `Pengguna` → `Admin`/`Resepsionis`; hierarki exception `HotelException` → 7 turunan |
| Abstract class + method abstrak | `Kamar` (`hitungBiaya(int)`, `getJenis()`, `getFasilitas()`), `Pengguna` (`getPeran()`, `getDaftarHak()`), `FileDao<T>` (`parse()`), `AbstractCrudPanel<T>`, `AbstractFormDialog` |
| Interface diimplementasikan > 1 kelas | `Persistable` ← `Kamar`, `Tamu`, `Reservasi`, `Pengguna`; `Dao<T>` ← `FileDao`; `Refreshable` ← `DashboardPanel`, `AbstractCrudPanel` |
| Overriding via referensi induk | `Reservasi.hitungBiayaKamar()` memanggil `kamar.hitungBiaya(...)` dengan `kamar` bertipe `Kamar` → hasil berbeda: Standard (harga × malam), Deluxe (+ sarapan per orang), Suite (diskon long-stay 15% + service 10%) |
| Overloading | `Kamar.hitungBiaya(int)` / `(int, int)` / `(LocalDate, LocalDate)`; `Pengguna.punyaAkses(HakAkses)` / `(HakAkses...)`; `ReservasiDao.perbaruiReferensi(Kamar)` / `(Tamu)`; `FormPanel.tambahBaris(...)` |
| Collection & generics | `Map<String, T>` di `FileDao<T>`, `List<LayananTambahan>`, `EnumMap`, `Optional<T>`, `BaseTableModel<T>` |
| Exception handling + custom exception | `try-catch` IOException di `FileDao.muat()/simpan()` (dibungkus `DataAksesException`), rollback memori jika gagal simpan; validasi input melempar `ValidasiException`; View menangkap `HotelException` dan menampilkan pesan |

### B. GUI

| Ketentuan | Implementasi |
|---|---|
| ≥ 3 tampilan, ada login | `LoginFrame`, `MainFrame` (Dashboard, Reservasi, Tamu, Kamar via CardLayout) + 3 dialog form |
| CRUD ≥ 2 entitas dalam JTable | **Kamar**, **Tamu**, **Reservasi** (tambah, lihat, ubah, hapus) + pencarian & filter |
| Input bervariasi | text field, password field, combo box (jenis/status/tamu/kamar), radio button (status kamar, jenis kelamin), checkbox (member, sarapan, layanan tambahan), spinner angka, **spinner tanggal** (date picker) |
| Menu bar / navigasi konsisten | Menu *Berkas ▸ Navigasi ▸ Bantuan* + sidebar; pintasan Ctrl/⌘ + 1–4, Ctrl+L logout, Ctrl+Q keluar |
| Konfirmasi hapus & pesan error | `UiUtil.konfirmasi()` sebelum hapus/check-in/check-out/batal; pesan validasi spesifik + fokus pindah ke field yang salah |
| Event handling dengan lambda, tidak menumpuk | Setiap listener lambda memanggil method kecil (`aksiHapus()`, `perbaruiEstimasi()`, `muatKamarTersedia()`, …) |
| Layout manager tepat (resize rapi) | BorderLayout, GridBagLayout (form & login), GridLayout (kartu dashboard), CardLayout (navigasi & atribut khusus kamar), FlowLayout, BoxLayout; ukuran minimum jendela diatur |

### Fitur tambahan (nilai plus saat demo)
- Cek bentrok jadwal: kamar yang sudah dipesan pada rentang tanggal sama tidak muncul / ditolak (`KamarTidakTersediaException`).
- Estimasi biaya **real-time** di form reservasi; struk otomatis saat check-out.
- Alur status: Dipesan → Check-in → Selesai, atau Dibatalkan; status kamar ikut berubah.
- Hak akses per peran (Admin vs Resepsionis) dicek di Controller **dan** disembunyikan di View.
- Password disimpan sebagai hash SHA-256; login dikunci 10 detik setelah 3x gagal.
- Penulisan file atomik (tulis ke `.tmp` lalu pindah) dan baris file rusak dilewati tanpa crash.

---

## 4. Pembagian kerja 3 anggota (saran)

Setiap anggota memegang satu "potongan vertikal" agar commit-nya jelas terlihat di Git:

| Anggota | Tanggung jawab | File utama |
|---|---|---|
| **Anggota 1 — Kamar** | Hierarki `Kamar` + polimorfisme `hitungBiaya()`, `KamarFactory`, `KamarDao`, `KamarController`, `KamarPanel`, `KamarDialog` | `model/Kamar*.java`, `dao/KamarDao.java`, `controller/KamarController.java`, `view/Kamar*.java` |
| **Anggota 2 — Tamu & fondasi data** | `Persistable`, `Dao<T>`, `FileDao<T>`, `CsvUtil`, semua exception, `Tamu`, `TamuDao`, `TamuController`, `TamuPanel`, `TamuDialog`, `ConsoleTest` | `dao/Dao.java`, `dao/FileDao.java`, `exception/*`, `model/Tamu.java`, `view/Tamu*.java` |
| **Anggota 3 — Reservasi & aplikasi** | `Reservasi`, `LayananTambahan`, `Pengguna`/`Admin`/`Resepsionis`, `ReservasiController`, `AuthController`, `DashboardController`, `LoginFrame`, `MainFrame`, `DashboardPanel`, `ReservasiPanel`, `ReservasiDialog` | `model/Reservasi.java`, `model/Pengguna.java`, `controller/*`, `view/Login*`, `view/Main*`, `view/Reservasi*` |

Kerangka bersama (`AbstractCrudPanel`, `AbstractFormDialog`, `BaseTableModel`, `UiUtil`) dibuat bersama di Minggu 2.

### Alur Git yang disarankan
```bash
git init && git add .gitignore README.md && git commit -m "Inisialisasi project"
git branch fitur-kamar ; git branch fitur-tamu ; git branch fitur-reservasi
# tiap anggota bekerja di branch-nya, commit kecil & sering, misal:
git commit -m "Tambah KamarSuite dengan diskon long stay"
# lalu merge/pull request ke main setelah direview anggota lain
```
Tip: tulis ulang/ketik sendiri bagian yang menjadi tanggung jawab masing-masing sambil memahaminya —
saat tanya jawab, dosen biasanya meminta tiap anggota menjelaskan kodenya sendiri.

---

## 5. Skenario demo 10 menit

1. Login sebagai **resepsionis** → tunjukkan menu Kamar hanya bisa dilihat (hak akses). Logout.
2. Login **admin** → Dashboard (statistik + agenda hari ini).
3. **Kamar**: tambah kamar Suite baru → coba harga `-1000` → muncul pesan error & fokus ke field harga. Ganti jenis di combo → field khusus berubah.
4. **Tamu**: tambah tamu, coba telepon `12345` → ditolak. Cari tamu lewat kotak pencarian.
5. **Reservasi**: buat reservasi baru → ubah tanggal → daftar kamar tersedia berubah; centang layanan → estimasi biaya berubah. Tunjukkan perbedaan biaya Standard vs Deluxe vs Suite (**polimorfisme**).
6. Check-in R0002 → status kamar 201 jadi *Terisi*; check-out R0006 → muncul struk.
7. Hapus data → dialog konfirmasi; coba hapus kamar yang punya reservasi → ditolak dengan pesan jelas.
8. Tutup aplikasi, buka lagi → data tetap ada (buka `data/reservasi.csv` untuk menunjukkan penyimpanan permanen).
9. Jalankan `ConsoleTest` → 25 lulus.
