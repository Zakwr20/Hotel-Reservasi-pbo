package com.hotel.view;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Panel form dua kolom (label : input) memakai GridBagLayout.
 * Kolom input melebar mengikuti ukuran jendela (weightx = 1).
 */
public class FormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private int baris;

    public FormPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
    }

    public void tambahBaris(String label, JComponent komponen) {
        tambahBaris(label, komponen, false);
    }

    /** Overload: jika tumbuh = true, komponen ikut membesar secara vertikal (mis. text area). */
    public void tambahBaris(String label, JComponent komponen, boolean tumbuh) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = baris;
        l.anchor = tumbuh ? GridBagConstraints.FIRST_LINE_START : GridBagConstraints.LINE_START;
        l.insets = new Insets(5, 0, 5, 12);
        add(new JLabel(label), l);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = baris;
        c.weightx = 1;
        c.weighty = tumbuh ? 1 : 0;
        c.fill = tumbuh ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        add(komponen, c);
        baris++;
    }
}
