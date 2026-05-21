package com.game2d.view;

import com.game2d.model.Action;
import com.game2d.model.Menu;
import com.game2d.model.SessionState;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.stream.Collectors;

/**
 * Ventana con el menú cuando el juego está en pausa (Continuar / Reiniciar).
 */
final class MenuModal {

    private final JDialog dialog;
    private final JLabel titleLabel;
    private final JPanel actionsPanel;
    private ViewListener listener;
    private String lastSignature = "";
    private boolean wasVisible;

    MenuModal(Frame owner) {
        dialog = new JDialog(owner, false);
        dialog.setLayout(new BorderLayout(8, 8));

        titleLabel = new JLabel("", JLabel.CENTER);
        dialog.add(titleLabel, BorderLayout.NORTH);

        actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dialog.add(actionsPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dialog.setVisible(false));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(closeButton);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }

    void setViewListener(ViewListener listener) {
        this.listener = listener;
    }

    void update(SessionState state, Menu menu) {
        if (!ViewSettings.MENU_UI_ENABLED) {
            if (wasVisible) {
                dialog.setVisible(false);
                wasVisible = false;
            }
            return;
        }
        boolean show = state == SessionState.PAUSED;
        if (!show) {
            if (wasVisible) {
                dialog.setVisible(false);
                wasVisible = false;
            }
            lastSignature = "";
            return;
        }

        String signature = state + "|" + menuSignature(menu);
        if (signature.equals(lastSignature) && wasVisible) {
            return;
        }
        lastSignature = signature;
        wasVisible = true;

        actionsPanel.removeAll();
        if (menu != null) {
            String title = menu.getTitle();
            String resolved = title == null || title.isBlank() ? "Menú" : title;
            dialog.setTitle(resolved);
            titleLabel.setText(resolved);
            for (Action action : menu.getActions()) {
                JButton button = new JButton(action.getLabel());
                button.setEnabled(action.isEnabled());
                button.addActionListener(e -> {
                    if (listener != null) {
                        listener.onActionInvoked(action.getId());
                    }
                });
                actionsPanel.add(button);
            }
        }
        actionsPanel.revalidate();
        if (!dialog.isVisible()) {
            dialog.pack();
            dialog.setLocationRelativeTo(dialog.getOwner());
            dialog.setVisible(true);
        }
    }

    private String menuSignature(Menu menu) {
        if (menu == null) {
            return "";
        }
        return menu.getActions().stream()
                .map(a -> a.getId() + ":" + a.getLabel() + ":" + a.isEnabled())
                .collect(Collectors.joining(","));
    }
}
