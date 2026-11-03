package com.hotel.dao;

import com.hotel.exception.DataAksesException;
import com.hotel.exception.DataTidakDitemukanException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.Persistable;
import com.hotel.util.CsvUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kelas abstrak DAO berbasis file teks (CSV, pemisah ';').
 * Data disimpan di memori dalam Map&lt;String, T&gt; lalu ditulis ulang ke file setiap ada perubahan.
 * Kelas turunan cukup menentukan cara parse satu baris dan data awal.
 */
public abstract class FileDao<T extends Persistable> implements Dao<T> {

    protected final Map<String, T> data = new LinkedHashMap<>();
    private final Path file;
    private int barisRusak;

    protected FileDao(Path folder, String namaFile) {
        this.file = folder.resolve(namaFile);
    }

    /** Mengubah kolom-kolom satu baris file menjadi objek. */
    protected abstract T parse(String[] kolom) throws ValidasiException, DataTidakDitemukanException;

    /** Judul kolom yang ditulis sebagai komentar di baris pertama file. */
    protected abstract String header();

    /** Data contoh yang dibuat saat file belum ada (pertama kali aplikasi dijalankan). */
    protected List<T> dataAwal() throws ValidasiException {
        return List.of();
    }

    @Override
    public void muat() throws DataAksesException {
        data.clear();
        barisRusak = 0;
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.toAbsolutePath().getParent());
                for (T item : dataAwal()) {
                    data.put(item.getId(), item);
                }
                simpan();
                return;
            }
            List<String> semuaBaris = Files.readAllLines(file, StandardCharsets.UTF_8);
            int nomorBaris = 0;
            for (String baris : semuaBaris) {
                nomorBaris++;
                if (baris.isBlank() || baris.startsWith("#")) {
                    continue;
                }
                try {
                    T item = parse(CsvUtil.pecah(baris));
                    data.put(item.getId(), item);
                } catch (ValidasiException | DataTidakDitemukanException | RuntimeException e) {
                    // baris rusak dilewati agar satu baris salah tidak membuat aplikasi gagal total
                    barisRusak++;
                    System.err.printf("[%s] baris %d dilewati: %s%n", file.getFileName(), nomorBaris, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new DataAksesException("Gagal membaca file " + file.toAbsolutePath() + ": " + e.getMessage(), e);
        } catch (ValidasiException e) {
            throw new DataAksesException("Data awal tidak valid: " + e.getMessage(), e);
        }
    }

    /** Menulis seluruh isi Map ke file sementara, lalu memindahkannya (agar file tidak rusak jika gagal di tengah). */
    protected void simpan() throws DataAksesException {
        List<String> baris = new ArrayList<>();
        baris.add("# " + header());
        for (T item : data.values()) {
            baris.add(CsvUtil.gabung(item.toRecord()));
        }
        Path sementara = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.write(sementara, baris, StandardCharsets.UTF_8);
            try {
                Files.move(sementara, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(sementara, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new DataAksesException("Gagal menyimpan file " + file.toAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(id == null ? null : data.get(id));
    }

    @Override
    public void insert(T item) throws ValidasiException, DataAksesException {
        if (data.containsKey(item.getId())) {
            throw new ValidasiException("id", "Data dengan ID/nomor " + item.getId() + " sudah ada.");
        }
        data.put(item.getId(), item);
        try {
            simpan();
        } catch (DataAksesException e) {
            data.remove(item.getId()); // rollback di memori
            throw e;
        }
    }

    @Override
    public void update(T item) throws DataTidakDitemukanException, DataAksesException {
        if (!data.containsKey(item.getId())) {
            throw new DataTidakDitemukanException("Data dengan ID " + item.getId() + " tidak ditemukan.");
        }
        T lama = data.put(item.getId(), item);
        try {
            simpan();
        } catch (DataAksesException e) {
            data.put(item.getId(), lama);
            throw e;
        }
    }

    @Override
    public void delete(String id) throws DataTidakDitemukanException, DataAksesException {
        T lama = data.remove(id);
        if (lama == null) {
            throw new DataTidakDitemukanException("Data dengan ID " + id + " tidak ditemukan.");
        }
        try {
            simpan();
        } catch (DataAksesException e) {
            data.put(id, lama);
            throw e;
        }
    }

    @Override
    public int count() {
        return data.size();
    }

    /** Membuat ID berikutnya, mis. prefix "T", digit 3 → T001, T002, ... */
    protected String idBerikutnya(String prefix, int digit) {
        int maks = data.keySet().stream()
                .filter(k -> k.startsWith(prefix))
                .mapToInt(k -> {
                    try {
                        return Integer.parseInt(k.substring(prefix.length()));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return prefix + String.format("%0" + digit + "d", maks + 1);
    }

    public int getBarisRusak() {
        return barisRusak;
    }

    public Path getFile() {
        return file;
    }
}
