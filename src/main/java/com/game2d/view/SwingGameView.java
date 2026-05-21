package com.game2d.view;

import com.game2d.model.FrameSnapshot;
import com.game2d.model.SessionState;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Implementación Swing de {@link GameView}. Los alumnos no deben modificar esta clase.
 */
public final class SwingGameView implements GameView {

    private static final Dimension GAME_SIZE = new Dimension(800, 600);
    private static final int MENU_BAR_HEIGHT = 72;
    private static final Dimension FRAME_SIZE = new Dimension(820, GAME_SIZE.height + MENU_BAR_HEIGHT + 20);

    private final JFrame frame;
    private final GamePanel gamePanel;
    private final MessageToastOverlay messageOverlay;
    private final NavBarPanel navBarPanel;
    private final MenuModal menuModal;
    private ViewListener listener;

    public SwingGameView() {
        this(ImageResolver.createDefault(), BackgroundSettings.getInstance());
    }

    public SwingGameView(ImageResolver imageResolver, BackgroundSettings background) {
        frame = new JFrame("Juego 2D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(FRAME_SIZE);

        navBarPanel = new NavBarPanel();
        navBarPanel.setPreferredSize(new Dimension(FRAME_SIZE.width, MENU_BAR_HEIGHT));
        navBarPanel.setMinimumSize(new Dimension(FRAME_SIZE.width, MENU_BAR_HEIGHT));

        gamePanel = new GamePanel(imageResolver, background);
        gamePanel.setPreferredSize(GAME_SIZE);
        gamePanel.setMinimumSize(GAME_SIZE);
        gamePanel.setMaximumSize(GAME_SIZE);

        messageOverlay = new MessageToastOverlay(gamePanel);
        menuModal = new MenuModal(frame);

        JPanel center = new JPanel(new BorderLayout());
        center.add(gamePanel, BorderLayout.CENTER);

        frame.add(navBarPanel, BorderLayout.NORTH);
        frame.add(center, BorderLayout.CENTER);
        frame.setSize(FRAME_SIZE);
        frame.setLocationRelativeTo(null);

        wireInput();
    }

    private void wireInput() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (listener != null) {
                    listener.onPointerDown(gamePanel.toWorldX(e.getX()), gamePanel.toWorldY(e.getY()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (listener != null) {
                    listener.onPointerUp(gamePanel.toWorldX(e.getX()), gamePanel.toWorldY(e.getY()));
                }
            }
        };
        gamePanel.addMouseListener(mouse);

        gamePanel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (listener != null) {
                    listener.onKeyPressed(e.getKeyCode(), KeyEvent.getKeyText(e.getKeyCode()));
                }
            }
        });
    }

    @Override
    public void render(FrameSnapshot frame) {
        SwingUtilities.invokeLater(() -> {
            navBarPanel.setViewListener(listener);
            menuModal.setViewListener(listener);
            navBarPanel.update(frame);
            menuModal.update(frame.getState(), frame.getMenu());
            gamePanel.setFrame(frame);
            if (frame.getState() == SessionState.RUNNING) {
                gamePanel.requestFocusInWindow();
            }
        });
    }

    @Override
    public void setViewListener(ViewListener listener) {
        this.listener = listener;
        navBarPanel.setViewListener(listener);
        menuModal.setViewListener(listener);
    }

    @Override
    public void setViewportSize(int widthPx, int heightPx) {
        // El tamaño del área de juego es fijo para evitar saltos de ventana.
    }

    @Override
    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    @Override
    public void successMessage(String message) {
        messageOverlay.successMessage(message);
    }

    @Override
    public void errorMessage(String message) {
        messageOverlay.errorMessage(message);
    }
}
