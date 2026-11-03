package com.hotel.view;

import com.hotel.exception.HotelException;
import com.hotel.exception.ValidasiException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Kerangka dialog form (template method): tombol Simpan/Batal, Enter = simpan, Esc = batal.
 * Jika simpan() melempar ValidasiException, pesan ditampilkan dan fokus pindah ke field yang salah.
 */
public abstract class AbstractFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    protected final FormPanel form = new FormPanel();
    /** Peta nama field (sama dengan ValidasiException#getField) ke komponen input. */
    protected final Map<String, JComponent> petaField = new HashMap<>();
    private final JButton btnSimpan = new JButton("Simpan");
    private final JButton btnBatal = new JButton("Batal");
    private boolean tersimpan;

    protected AbstractFormDialog(Window induk, String judul) {
        super(induk, judul, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panelTombol.add(btnBatal);
        panelTombol.add(btnSimpan);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(panelTombol, BorderLayout.SOUTH);

        btnSimpan.addActionListener(e -> prosesSimpan());
        btnBatal.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSimpan);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Proses penyimpanan sebenarnya (memanggil controller). */
    protected abstract void simpan() throws HotelException;

    /** Dipanggil subclass di akhir konstruktor. */
    protected void selesaiDibangun() {
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getOwner());
    }

    /** Menampilkan dialog (modal) dan mengembalikan true jika data berhasil disimpan. */
    public boolean tampilkan() {
        setVisible(true);
        return tersimpan;
    }

    private void prosesSimpan() {
        try {
            simpan();
            tersimpan = true;
            dispose();
        } catch (ValidasiException ex) {
            UiUtil.peringatan(this, ex.getMessage());
            fokusKe(ex.getField());
        } catch (HotelException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void fokusKe(String field) {
        JComponent c = field == null ? null : petaField.get(field);
        if (c == null) {
            return;
        }
        if (c instanceof JSpinner s && s.getEditor() instanceof JSpinner.DefaultEditor ed) {
            ed.getTextField().requestFocusInWindow();
            return;
        }
        c.requestFocusInWindow();
        if (c instanceof JTextField tf) {
            tf.selectAll();
        }
    }
}
