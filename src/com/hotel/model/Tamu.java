package com.hotel.model;

import com.hotel.exception.ValidasiException;

import java.util.Objects;

/** Data tamu hotel. Member mendapat diskon 5% dari biaya kamar. */
public class Tamu implements Persistable {

    public static final double DISKON_MEMBER = 0.05;

    private final String id;
    private String nama;
    private String noIdentitas;
    private String telepon;
    private String email;
    private JenisKelamin jenisKelamin;
    private boolean member;

    public Tamu(String id, String nama, String noIdentitas, String telepon, String email,
                JenisKelamin jenisKelamin, boolean member) throws ValidasiException {
        if (id == null || id.isBlank()) {
            throw new ValidasiException("id", "ID tamu wajib ada.");
        }
        this.id = id;
        setNama(nama);
        setNoIdentitas(noIdentitas);
        setTelepon(telepon);
        setEmail(email);
        setJenisKelamin(jenisKelamin);
        setMember(member);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public final void setNama(String nama) throws ValidasiException {
        if (nama == null || nama.isBlank()) {
            throw new ValidasiException("nama", "Nama tamu wajib diisi.");
        }
        String n = nama.trim().replaceAll("\\s+", " ");
        if (n.length() < 3 || n.length() > 60) {
            throw new ValidasiException("nama", "Nama tamu harus 3 sampai 60 karakter.");
        }
        if (!n.matches("[\\p{L} .'-]+")) {
            throw new ValidasiException("nama", "Nama hanya boleh berisi huruf, spasi, titik, apostrof, atau tanda hubung.");
        }
        this.nama = n;
    }

    public String getNoIdentitas() {
        return noIdentitas;
    }

    public final void setNoIdentitas(String noIdentitas) throws ValidasiException {
        String n = noIdentitas == null ? "" : noIdentitas.trim().toUpperCase().replace(" ", "");
        if (!n.matches("\\d{16}|[A-Z]\\d{7}")) {
            throw new ValidasiException("noIdentitas",
                    "No. identitas harus 16 digit NIK atau nomor paspor (1 huruf + 7 angka).");
        }
        this.noIdentitas = n;
    }

    public String getTelepon() {
        return telepon;
    }

    public final void setTelepon(String telepon) throws ValidasiException {
        String t = telepon == null ? "" : telepon.trim().replaceAll("[\\s-]", "");
        if (!t.matches("(\\+62|0)8\\d{8,11}")) {
            throw new ValidasiException("telepon", "Nomor telepon tidak valid. Contoh: 081234567890 atau +6281234567890.");
        }
        this.telepon = t;
    }

    public String getEmail() {
        return email;
    }

    /** Email boleh kosong, tetapi jika diisi formatnya harus benar. */
    public final void setEmail(String email) throws ValidasiException {
        String e = email == null ? "" : email.trim();
        if (!e.isEmpty() && !e.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) {
            throw new ValidasiException("email", "Format email tidak valid. Contoh: nama@email.com");
        }
        this.email = e;
    }

    public JenisKelamin getJenisKelamin() {
        return jenisKelamin;
    }

    public final void setJenisKelamin(JenisKelamin jenisKelamin) throws ValidasiException {
        if (jenisKelamin == null) {
            throw new ValidasiException("jenisKelamin", "Jenis kelamin wajib dipilih.");
        }
        this.jenisKelamin = jenisKelamin;
    }

    public boolean isMember() {
        return member;
    }

    public final void setMember(boolean member) {
        this.member = member;
    }

    @Override
    public String[] toRecord() {
        return new String[] {id, nama, noIdentitas, telepon, email, jenisKelamin.name(), String.valueOf(member)};
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Tamu t && Objects.equals(id, t.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return nama + " (" + id + ")" + (member ? " ★" : "");
    }
}
