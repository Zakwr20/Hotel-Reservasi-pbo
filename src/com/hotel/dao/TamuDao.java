package com.hotel.dao;

import com.hotel.exception.ValidasiException;
import com.hotel.model.JenisKelamin;
import com.hotel.model.Tamu;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TamuDao extends FileDao<Tamu> {

    public TamuDao(Path folder) {
        super(folder, "tamu.csv");
    }

    public String buatIdBaru() {
        return idBerikutnya("T", 3);
    }

    public Optional<Tamu> findByNoIdentitas(String noIdentitas) {
        return data.values().stream()
                .filter(t -> t.getNoIdentitas().equalsIgnoreCase(noIdentitas))
                .findFirst();
    }

    @Override
    protected String header() {
        return "id;nama;no_identitas;telepon;email;jenis_kelamin;member";
    }

    @Override
    protected Tamu parse(String[] k) throws ValidasiException {
        return new Tamu(k[0], k[1], k[2], k[3], k[4], JenisKelamin.valueOf(k[5]), Boolean.parseBoolean(k[6]));
    }

    @Override
    protected List<Tamu> dataAwal() throws ValidasiException {
        return List.of(
                new Tamu("T001", "Budi Santoso", "3374011203900001", "081234567801", "budi.santoso@email.com", JenisKelamin.LAKI_LAKI, false),
                new Tamu("T002", "Siti Rahmawati", "3374025507920002", "081234567802", "siti.r@email.com", JenisKelamin.PEREMPUAN, true),
                new Tamu("T003", "Andreas Wijaya", "3171010808880003", "081234567803", "andreas.w@email.com", JenisKelamin.LAKI_LAKI, false),
                new Tamu("T004", "Dewi Lestari", "3374036112950004", "081234567804", "", JenisKelamin.PEREMPUAN, true),
                new Tamu("T005", "Rizky Pratama", "3301021504000005", "081234567805", "rizky.p@email.com", JenisKelamin.LAKI_LAKI, false));
    }
}
