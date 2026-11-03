package com.hotel.dao;

import com.hotel.exception.ValidasiException;
import com.hotel.model.JenisKamar;
import com.hotel.model.Kamar;
import com.hotel.model.KamarDeluxe;
import com.hotel.model.KamarFactory;
import com.hotel.model.KamarStandard;
import com.hotel.model.KamarSuite;
import com.hotel.model.StatusKamar;

import java.nio.file.Path;
import java.util.List;

public class KamarDao extends FileDao<Kamar> {

    public KamarDao(Path folder) {
        super(folder, "kamar.csv");
    }

    @Override
    protected String header() {
        return "jenis;nomor;lantai;harga_per_malam;kapasitas;status;atribut_khusus";
    }

    @Override
    protected Kamar parse(String[] k) throws ValidasiException {
        return KamarFactory.buat(
                JenisKamar.valueOf(k[0]),
                k[1],
                Integer.parseInt(k[2]),
                Double.parseDouble(k[3]),
                Integer.parseInt(k[4]),
                StatusKamar.valueOf(k[5]),
                k[6]);
    }

    @Override
    protected List<Kamar> dataAwal() throws ValidasiException {
        return List.of(
                new KamarStandard("101", 1, 350_000, 2, StatusKamar.TERISI, "Double"),
                new KamarStandard("102", 1, 350_000, 2, StatusKamar.TERSEDIA, "Twin"),
                new KamarStandard("103", 1, 300_000, 1, StatusKamar.TERISI, "Single"),
                new KamarStandard("104", 1, 350_000, 2, StatusKamar.PERBAIKAN, "Double"),
                new KamarStandard("105", 1, 320_000, 2, StatusKamar.TERSEDIA, "Twin"),
                new KamarDeluxe("201", 2, 650_000, 2, StatusKamar.TERSEDIA, true),
                new KamarDeluxe("202", 2, 600_000, 2, StatusKamar.TERSEDIA, false),
                new KamarDeluxe("203", 2, 700_000, 3, StatusKamar.TERSEDIA, true),
                new KamarSuite("301", 3, 1_500_000, 3, StatusKamar.TERSEDIA, 2),
                new KamarSuite("302", 3, 2_200_000, 4, StatusKamar.TERSEDIA, 3));
    }
}
