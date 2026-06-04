package com.miJuego.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Panel de tutorial (Word). Por ahora es un placeholder que muestra
 * texto básico y un botón "Continuar" que vuelve al escritorio.
 */
public class TutorialPanel extends JPanel implements ActionListener {

    private final JButton continueBtn = new JButton("Continuar");
    private Runnable onFinish;

    public TutorialPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(640, 480));
        JLabel title = new JLabel("Tutorial – Word", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setText(
                "Clippy aparece y explica los conceptos básicos mientras los primeros enemigos \n" +
                "atacan el documento. El objetivo es colocar torres, administrar oro y \n" +
                "proteger la integridad del texto.\n\n" +
                "(Esta es una versión placeholder del tutorial. Se rellenará con la \n" +
                "lógica real del nivel Word más adelante.)");
        txt.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(txt, BorderLayout.CENTER);

        continueBtn.addActionListener(this);
        JPanel south = new JPanel();
        south.add(continueBtn);
        add(south, BorderLayout.SOUTH);
    }

    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == continueBtn && onFinish != null) {
            onFinish.run();
        }
    }
}
