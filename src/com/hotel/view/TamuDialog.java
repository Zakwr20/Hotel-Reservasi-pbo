package com.hotel.view;

import com.hotel.controller.TamuController;
import com.hotel.exception.HotelException;
import com.hotel.model.JenisKelamin;
import com.hotel.model.Tamu;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.Window;

/** Form tambah/ubah tamu: text field, radio button jenis kelamin, checkbox member. */
public class TamuDialog extends AbstractFormDialog {

    private static final long serialVersionUID = 1L;

    private final TamuController controller;
    private final Tamu lama;

    private final JTextField fieldNama = new JTextField(24);
    private final JTextField fieldNoId = new JTextField(18);
    private final JTextField fieldTelepon = new JTextField(16);
    private final JTextField fieldEmail = new JTextField(24);
    private final JRadioButton radioLaki = new JRadioButton(JenisKelamin.LAKI_LAKI.toString());
    private final JRadioButton radioPerempuan = new JRadioButton(JenisKelamin.PEREMPUAN.toString());
    private final JCheckBox cekMember = new JCheckBox("Member (diskon 5% biaya kamar)");

    public TamuDialog(Window induk, TamuController controller, Tamu lama) {
        super(induk, lama == null ? "Tambah Tamu" : "Ubah Tamu " + lama.getId());
        this.controller = controller;
        this.lama = lama;

        ButtonGroup grup = new ButtonGroup();
        grup.add(radioLaki);
        grup.add(radioPerempuan);
        JPanel panelJk = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelJk.add(radioLaki);
        panelJk.add(radioPerempuan);

        fieldNoId.setToolTipText("16 digit NIK atau nomor paspor (contoh: A1234567)");
        fieldTelepon.setToolTipText("Contoh: 081234567890");

        if (lama != null) {
            form.tambahBaris("ID tamu", new JLabel(lama.getId()));
        }
        form.tambahBaris("Nama lengkap *", fieldNama);
        form.tambahBaris("No. identitas *", fieldNoId);
        form.tambahBaris("Telepon *", fieldTelepon);
        form.tambahBaris("Email", fieldEmail);
        form.tambahBaris("Jenis kelamin *", panelJk);
        form.tambahBaris("", cekMember);

        petaField.put("nama", fieldNama);
        petaField.put("noIdentitas", fieldNoId);
        petaField.put("telepon", fieldTelepon);
        petaField.put("email", fieldEmail);
        petaField.put("jenisKelamin", radioLaki);

        if (lama != null) {
            fieldNama.setText(lama.getNama());
            fieldNoId.setText(lama.getNoIdentitas());
            fieldTelepon.setText(lama.getTelepon());
            fieldEmail.setText(lama.getEmail());
            (lama.getJenisKelamin() == JenisKelamin.LAKI_LAKI ? radioLaki : radioPerempuan).setSelected(true);
            cekMember.setSelected(lama.isMember());
        }
        selesaiDibangun();
    }

    private JenisKelamin jenisKelaminTerpilih() {
        if (radioLaki.isSelected()) {
            return JenisKelamin.LAKI_LAKI;
        }
        return radioPerempuan.isSelected() ? JenisKelamin.PEREMPUAN : null;
    }

    @Override
    protected void simpan() throws HotelException {
        if (lama == null) {
            controller.tambah(fieldNama.getText(), fieldNoId.getText(), fieldTelepon.getText(),
                    fieldEmail.getText(), jenisKelaminTerpilih(), cekMember.isSelected());
        } else {
            controller.ubah(lama.getId(), fieldNama.getText(), fieldNoId.getText(), fieldTelepon.getText(),
                    fieldEmail.getText(), jenisKelaminTerpilih(), cekMember.isSelected());
        }
    }
}
