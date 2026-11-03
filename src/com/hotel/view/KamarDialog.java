package com.hotel.view;

import com.hotel.controller.KamarController;
import com.hotel.exception.HotelException;
import com.hotel.model.JenisKamar;
import com.hotel.model.Kamar;
import com.hotel.model.KamarDeluxe;
import com.hotel.model.KamarStandard;
import com.hotel.model.KamarSuite;
import com.hotel.model.StatusKamar;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.EnumMap;
import java.util.Map;

/** Form tambah/ubah kamar. Field khusus berubah sesuai jenis kamar (CardLayout). */
public class KamarDialog extends AbstractFormDialog {

    private static final long serialVersionUID = 1L;

    private final KamarController controller;
    private final Kamar lama;

    private final JComboBox<JenisKamar> comboJenis = new JComboBox<>(JenisKamar.values());
    private final JTextField fieldNomor = new JTextField(10);
    private final JSpinner spinnerLantai = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JTextField fieldHarga = new JTextField(12);
    private final JSpinner spinnerKapasitas = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    private final Map<StatusKamar, JRadioButton> radioStatus = new EnumMap<>(StatusKamar.class);

    // field khusus per jenis
    private final CardLayout kartuKhusus = new CardLayout();
    private final JPanel panelKhusus = new JPanel(kartuKhusus);
    private final JComboBox<String> comboKasur = new JComboBox<>(KamarStandard.TIPE_KASUR);
    private final JCheckBox cekSarapan = new JCheckBox("Termasuk sarapan untuk semua tamu");
    private final JSpinner spinnerRuang = new JSpinner(new SpinnerNumberModel(2, 1, 5, 1));
    private final JLabel labelFasilitas = new JLabel();

    public KamarDialog(Window induk, KamarController controller, Kamar lama) {
        super(induk, lama == null ? "Tambah Kamar" : "Ubah Kamar " + lama.getNomor());
        this.controller = controller;
        this.lama = lama;

        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ButtonGroup grup = new ButtonGroup();
        for (StatusKamar s : StatusKamar.values()) {
            JRadioButton rb = new JRadioButton(s.toString());
            grup.add(rb);
            radioStatus.put(s, rb);
            panelStatus.add(rb);
        }
        radioStatus.get(StatusKamar.TERSEDIA).setSelected(true);

        panelKhusus.add(barisKhusus("Tipe kasur:", comboKasur), JenisKamar.STANDARD.name());
        panelKhusus.add(barisKhusus("", cekSarapan), JenisKamar.DELUXE.name());
        panelKhusus.add(barisKhusus("Jumlah ruang:", spinnerRuang), JenisKamar.SUITE.name());
        fieldHarga.setToolTipText("Contoh: 350000 atau 350.000");

        form.tambahBaris("Jenis kamar", comboJenis);
        form.tambahBaris("Nomor kamar", fieldNomor);
        form.tambahBaris("Lantai", spinnerLantai);
        form.tambahBaris("Harga per malam (Rp)", fieldHarga);
        form.tambahBaris("Kapasitas (orang)", spinnerKapasitas);
        form.tambahBaris("Status", panelStatus);
        form.tambahBaris("Atribut khusus", panelKhusus);
        form.tambahBaris("Fasilitas", labelFasilitas);

        petaField.put("nomor", fieldNomor);
        petaField.put("lantai", spinnerLantai);
        petaField.put("harga", fieldHarga);
        petaField.put("kapasitas", spinnerKapasitas);
        petaField.put("tipeKasur", comboKasur);
        petaField.put("jumlahRuang", spinnerRuang);
        petaField.put("jenis", comboJenis);

        if (lama != null) {
            isiDari(lama);
        }

        comboJenis.addActionListener(e -> gantiJenis());
        cekSarapan.addActionListener(e -> perbaruiFasilitas());
        comboKasur.addActionListener(e -> perbaruiFasilitas());
        spinnerRuang.addChangeListener(e -> perbaruiFasilitas());
        gantiJenis();
        selesaiDibangun();
    }

    private static JPanel barisKhusus(String label, java.awt.Component komponen) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        if (!label.isEmpty()) {
            p.add(new JLabel(label));
        }
        p.add(komponen);
        return p;
    }

    private void isiDari(Kamar k) {
        comboJenis.setSelectedItem(k.getJenis());
        fieldNomor.setText(k.getNomor());
        fieldNomor.setEditable(false); // nomor = ID, tidak boleh diubah
        spinnerLantai.setValue(k.getLantai());
        fieldHarga.setText(String.valueOf(Math.round(k.getHargaPerMalam())));
        spinnerKapasitas.setValue(k.getKapasitas());
        radioStatus.get(k.getStatus()).setSelected(true);
        if (k instanceof KamarStandard s) {
            comboKasur.setSelectedItem(s.getTipeKasur());
        } else if (k instanceof KamarDeluxe d) {
            cekSarapan.setSelected(d.isTermasukSarapan());
        } else if (k instanceof KamarSuite su) {
            spinnerRuang.setValue(su.getJumlahRuang());
        }
    }

    private void gantiJenis() {
        JenisKamar jenis = (JenisKamar) comboJenis.getSelectedItem();
        kartuKhusus.show(panelKhusus, jenis.name());
        perbaruiFasilitas();
    }

    private void perbaruiFasilitas() {
        String teks = switch ((JenisKamar) comboJenis.getSelectedItem()) {
            case STANDARD -> "AC, TV, Wi-Fi, kasur " + comboKasur.getSelectedItem();
            case DELUXE -> "AC, Smart TV, Wi-Fi, minibar, bathtub" + (cekSarapan.isSelected() ? ", sarapan" : "");
            case SUITE -> spinnerRuang.getValue() + " ruang, ruang tamu, jacuzzi, butler 24 jam";
        };
        labelFasilitas.setText(teks);
    }

    private StatusKamar statusTerpilih() {
        return radioStatus.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private String atributKhusus(JenisKamar jenis) {
        return switch (jenis) {
            case STANDARD -> (String) comboKasur.getSelectedItem();
            case DELUXE -> String.valueOf(cekSarapan.isSelected());
            case SUITE -> String.valueOf(spinnerRuang.getValue());
        };
    }

    @Override
    protected void simpan() throws HotelException {
        JenisKamar jenis = (JenisKamar) comboJenis.getSelectedItem();
        int lantai = (Integer) spinnerLantai.getValue();
        int kapasitas = (Integer) spinnerKapasitas.getValue();
        if (lama == null) {
            controller.tambah(jenis, fieldNomor.getText(), lantai, fieldHarga.getText(), kapasitas,
                    statusTerpilih(), atributKhusus(jenis));
        } else {
            controller.ubah(jenis, lama.getNomor(), lantai, fieldHarga.getText(), kapasitas,
                    statusTerpilih(), atributKhusus(jenis));
        }
    }
}
