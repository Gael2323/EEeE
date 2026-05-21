package com.game2d.view;

import com.game2d.model.Action;
import com.game2d.model.FrameSnapshot;
import com.game2d.model.GameStatus;
import com.game2d.model.Menu;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Barra superior: puntaje, oro, vidas y (opcional) botones del menú.
 */
final class NavBarPanel extends JPanel {

    private final JLabel scoreLabel = new JLabel();
    private final JLabel goldLabel = new JLabel();
    private final JLabel livesLabel = new JLabel();
    private final JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final List<JButton> buttons = new ArrayList<>();
    private final boolean menuUiEnabled = ViewSettings.MENU_UI_ENABLED;

    private ViewListener listener;
    private String lastStatusSignature = "";
    private String lastMenuSignature = "";

    NavBarPanel() {
        setLayout(new BorderLayout(12, 0));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        Font statFont = scoreLabel.getFont().deriveFont(Font.BOLD, 15f);
        scoreLabel.setFont(statFont);
        goldLabel.setFont(statFont);
        livesLabel.setFont(statFont);
        statsPanel.add(scoreLabel);
        statsPanel.add(goldLabel);
        statsPanel.add(livesLabel);

        add(statsPanel, BorderLayout.WEST);
        if (menuUiEnabled) {
            add(actionsPanel, BorderLayout.CENTER);
        } else {
            actionsPanel.setVisible(false);
        }
    }

    void setViewListener(ViewListener listener) {
        this.listener = listener;
    }

    void update(FrameSnapshot frame) {
        updateStatus(frame.getStatus());
        if (menuUiEnabled && frame.getMenu() != null) {
            updateMenu(frame.getMenu());
        }
    }

    private void updateStatus(GameStatus status) {
        String signature = status.getScore() + "|" + status.getGold() + "|" + status.getLives();
        if (signature.equals(lastStatusSignature)) {
            return;
        }
        lastStatusSignature = signature;
        scoreLabel.setText("Puntaje: " + status.getScore());
        goldLabel.setText("Oro: " + formatStat(status.getGold()));
        livesLabel.setText("Vidas: " + formatStat(status.getLives()));
    }

    private static String formatStat(int value) {
        return value < 0 ? "—" : String.valueOf(value);
    }

    private void updateMenu(Menu menu) {
        String signature = menuSignature(menu);
        if (signature.equals(lastMenuSignature)) {
            updateEnabledStates(menu);
            return;
        }
        lastMenuSignature = signature;
        actionsPanel.removeAll();
        buttons.clear();
        for (Action action : menu.getActions()) {
            JButton button = new JButton(action.getLabel());
            button.setFont(button.getFont().deriveFont(Font.PLAIN, 15f));
            button.setEnabled(action.isEnabled());
            String actionId = action.getId();
            button.addActionListener(e -> fireAction(actionId));
            buttons.add(button);
            actionsPanel.add(button);
        }
        actionsPanel.revalidate();
        actionsPanel.repaint();
        revalidate();
        repaint();
    }

    private void fireAction(String actionId) {
        if (listener != null) {
            listener.onActionInvoked(actionId);
        }
    }

    private void updateEnabledStates(Menu menu) {
        List<? extends Action> actions = menu.getActions();
        for (int i = 0; i < buttons.size() && i < actions.size(); i++) {
            buttons.get(i).setEnabled(actions.get(i).isEnabled());
        }
    }

    private String menuSignature(Menu menu) {
        return menu.getTitle() + "|" + menu.getActions().stream()
                .map(a -> a.getId() + ":" + a.getLabel())
                .collect(Collectors.joining(","));
    }
}
