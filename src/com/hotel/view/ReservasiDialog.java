package com.hotel.view;

import com.hotel.controller.ReservasiController;
import com.hotel.exception.HotelException;
import com.hotel.exception.ValidasiException;
import com.hotel.model.Kamar;
import com.hotel.model.LayananTambahan;
import com.hotel.model.Reservasi;
import com.hotel.model.Tamu;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Form reservasi: combo tamu & kamar, spinner tanggal (date picker), spinner jumlah tamu,
 * checkbox layanan tambahan, dan estimasi biaya yang diperbarui otomatis.
 */
public class ReservasiDialog extends AbstractFormDialog {

    private static final long serialVersionUID = 1L;

    private final ReservasiController controller;
    private final Reservasi lama;

    private final JComboBox<Tamu> comboTamu;
    private final JSpinner spinnerCheckIn;
    private final JSpinner spinnerCheckOut;
    private final JComboBox<Kamar> comboKamar = new JComboBox<>();
    private final JSpinner spinnerJumlahTamu = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final Map<LayananTambahan, JCheckBox> cekLayanan = new EnumMap<>(LayananTambahan.class);
    private final JTextArea areaRincian = new JTextArea(8, 46);
    private boolean sedangMemuat;

    public ReservasiDialog(Window induk, ReservasiController controller, Reservasi lama) {
        super(induk, lama == null ? "Reservasi Baru" : "Ubah Reservasi " + lama.getId());
        this.controller = controller;
        this.lama = lama;

        comboTamu = new JComboBox<>(controller.getDaftarTamu().toArray(new Tamu[0]));
        LocalDate masuk = lama == null ? LocalDate.now() : lama.getCheckIn();
        LocalDate keluar = lama == null ? masuk.plusDays(1) : lama.getCheckOut();
        spinnerCheckIn = UiUtil.buatSpinnerTanggal(masuk);
        spinnerCheckOut = UiUtil.buatSpinnerTanggal(keluar);

        JPanel panelLayanan = new JPanel(new GridLayout(0, 2, 8, 2));
        for (LayananTambahan l : LayananTambahan.values()) {
            JCheckBox cb = new JCheckBox(l.getKeterangan());
            cekLayanan.put(l, cb);
            panelLayanan.add(cb);
        }

        areaRincian.setEditable(false);
        areaRincian.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaRincian.setBackground(UiUtil.WARNA_LATAR);
        areaRincian.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        form.tambahBaris("Tamu", comboTamu);
        form.tambahBaris("Check-in", spinnerCheckIn);
        form.tambahBaris("Check-out", spinnerCheckOut);
        form.tambahBaris("Kamar tersedia", comboKamar);
        form.tambahBaris("Jumlah tamu", spinnerJumlahTamu);
        form.tambahBaris("Layanan tambahan", panelLayanan);
        form.tambahBaris("Estimasi biaya", new JScrollPane(areaRincian), true);

        petaField.put("tamu", comboTamu);
        petaField.put("checkIn", spinnerCheckIn);
        petaField.put("checkOut", spinnerCheckOut);
        petaField.put("kamar", comboKamar);
        petaField.put("jumlahTamu", spinnerJumlahTamu);

        if (lama != null) {
            comboTamu.setSelectedItem(lama.getTamu());
            spinnerJumlahTamu.setValue(lama.getJumlahTamu());
            lama.getLayanan().forEach(l -> cekLayanan.get(l).setSelected(true));
        }

        // event: tiap perubahan input memanggil method kecil
        spinnerCheckIn.addChangeListener(e -> saatTanggalBerubah());
        spinnerCheckOut.addChangeListener(e -> saatTanggalBerubah());
        comboKamar.addActionListener(e -> perbaruiEstimasi());
        comboTamu.addActionListener(e -> perbaruiEstimasi());
        spinnerJumlahTamu.addChangeListener(e -> perbaruiEstimasi());
        cekLayanan.values().forEach(cb -> cb.addItemListener(e -> perbaruiEstimasi()));

        muatKamarTersedia(lama == null ? null : lama.getKamar());
        perbaruiEstimasi();
        selesaiDibangun();
    }

    private LocalDate tanggalMasuk() {
        return UiUtil.ambilTanggal(spinnerCheckIn);
    }

    private LocalDate tanggalKeluar() {
        return UiUtil.ambilTanggal(spinnerCheckOut);
    }

    private void saatTanggalBerubah() {
        if (sedangMemuat) {
            return;
        }
        if (!tanggalKeluar().isAfter(tanggalMasuk())) {
            sedangMemuat = true;
            UiUtil.aturTanggal(spinnerCheckOut, tanggalMasuk().plusDays(1)); // geser otomatis
            sedangMemuat = false;
        }
        muatKamarTersedia((Kamar) comboKamar.getSelectedItem());
        perbaruiEstimasi();
    }

    /** Mengisi combo kamar hanya dengan kamar yang kosong pada tanggal terpilih. */
    private void muatKamarTersedia(Kamar pilihanSebelumnya) {
        sedangMemuat = true;
        try {
            List<Kamar> daftar = controller.kamarTersedia(tanggalMasuk(), tanggalKeluar(),
                    lama == null ? null : lama.getId());
            comboKamar.setModel(new DefaultComboBoxModel<>(daftar.toArray(new Kamar[0])));
            int indeks = pilihanSebelumnya == null ? -1 : daftar.indexOf(pilihanSebelumnya);
            if (indeks >= 0) {
                comboKamar.setSelectedIndex(indeks);
            }
        } finally {
            sedangMemuat = false;
        }
    }

    private List<LayananTambahan> layananTerpilih() {
        return cekLayanan.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void perbaruiEstimasi() {
        if (sedangMemuat) {
            return;
        }
        Kamar kamar = (Kamar) comboKamar.getSelectedItem();
        Tamu tamu = (Tamu) comboTamu.getSelectedItem();
        if (kamar == null) {
            tampilkanPesan("Tidak ada kamar yang tersedia pada rentang tanggal ini.", true);
            return;
        }
        try {
            Reservasi preview = controller.simulasi(lama == null ? null : lama.getId(), tamu, kamar,
                    tanggalMasuk(), tanggalKeluar(), (Integer) spinnerJumlahTamu.getValue(), layananTerpilih());
            tampilkanPesan(StrukFormatter.rincian(preview), false);
        } catch (ValidasiException ex) {
            tampilkanPesan("! " + ex.getMessage(), true);
        }
    }

    private void tampilkanPesan(String teks, boolean error) {
        areaRincian.setForeground(error ? UiUtil.WARNA_ERROR : UIManager.getColor("TextArea.foreground"));
        areaRincian.setText(teks);
        areaRincian.setCaretPosition(0);
    }

    @Override
    protected void simpan() throws HotelException {
        Tamu tamu = (Tamu) comboTamu.getSelectedItem();
        Kamar kamar = (Kamar) comboKamar.getSelectedItem();
        if (tamu == null) {
            throw new ValidasiException("tamu", "Pilih tamu terlebih dahulu.");
        }
        if (kamar == null) {
            throw new ValidasiException("kamar", "Tidak ada kamar yang dipilih/tersedia untuk tanggal tersebut.");
        }
        int jumlah = (Integer) spinnerJumlahTamu.getValue();
        if (lama == null) {
            controller.buat(tamu.getId(), kamar.getNomor(), tanggalMasuk(), tanggalKeluar(), jumlah, layananTerpilih());
        } else {
            controller.ubah(lama.getId(), tamu.getId(), kamar.getNomor(), tanggalMasuk(), tanggalKeluar(),
                    jumlah, layananTerpilih());
        }
    }
}
