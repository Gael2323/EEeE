package com.miJuego.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Panel del menú principal — estética Windows XP.
 *
 * <p>Dibuja el escritorio XP atenuado de fondo, y encima una ventana
 * estilo "TowerDefense_Setup.exe" con los botones JUGAR, OPCIONES y SALIR.</p>
 *
 * <p>Todo se pinta manualmente con {@link Graphics2D} para tener control
 * total del pixel art sin depender de Look&amp;Feel de Swing.</p>
 */
public class MainMenuPanel extends JPanel implements MouseListener, MouseMotionListener {

    // ─── Paleta de colores Windows XP ────────────────────────────────────
    private static final Color XP_TITLE_BAR_TOP    = new Color(0, 88, 238);
    private static final Color XP_TITLE_BAR_BOTTOM = new Color(0, 48, 160);
    private static final Color XP_WINDOW_BG        = new Color(236, 233, 216);
    private static final Color XP_WINDOW_BORDER    = new Color(0, 60, 165);
    private static final Color XP_BUTTON_BG        = new Color(236, 233, 216);
    private static final Color XP_BUTTON_BORDER    = new Color(0, 60, 116);
    private static final Color XP_BUTTON_HOVER     = new Color(180, 210, 255);
    private static final Color XP_BUTTON_TEXT       = new Color(0, 0, 0);
    private static final Color XP_TITLE_TEXT        = Color.WHITE;
    private static final Color XP_CLOSE_BTN_BG     = new Color(210, 60, 50);
    private static final Color XP_CLOSE_BTN_HOVER  = new Color(240, 80, 70);
    private static final Color OVERLAY_COLOR        = new Color(0, 0, 0, 150);

    // ─── Dimensiones de la ventana del instalador ────────────────────────
    private static final int DIALOG_W = 380;
    private static final int DIALOG_H = 300;
    private static final int TITLE_BAR_H = 28;
    private static final int BTN_W = 260;
    private static final int BTN_H = 40;
    private static final int BTN_GAP = 16;
    private static final int BTN_START_Y = 100; // Relativo al diálogo

    // ─── Estado ──────────────────────────────────────────────────────────
    private BufferedImage desktopBg;
    private int hoveredButton = -1; // -1 = ninguno, 0 = JUGAR, 1 = OPCIONES, 2 = SALIR
    private boolean closeHovered = false;

    private final String[] buttonLabels = {"JUGAR", "OPCIONES", "SALIR"};
    private final Rectangle[] buttonRects = new Rectangle[3];
    private Rectangle closeRect = new Rectangle();

    // Callback para cuando el usuario elige una opción
    private Runnable onJugar;
    private Runnable onOpciones;
    private Runnable onSalir;

    public MainMenuPanel() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);

        // Cargar imagen de fondo
        try {
            desktopBg = ImageIO.read(
                    getClass().getResourceAsStream("/assets/menu/xp_desktop.png")
            );
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar xp_desktop.png: " + e.getMessage());
            desktopBg = null;
        }
    }

    // ─── Setters de callbacks ────────────────────────────────────────────

    public void setOnJugar(Runnable onJugar) { this.onJugar = onJugar; }
    public void setOnOpciones(Runnable onOpciones) { this.onOpciones = onOpciones; }
    public void setOnSalir(Runnable onSalir) { this.onSalir = onSalir; }

    // ─── Pintado ─────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // 1. Fondo del escritorio XP
        if (desktopBg != null) {
            g2.drawImage(desktopBg, 0, 0, w, h, null);
        } else {
            // Fallback: color teal clásico
            g2.setColor(new Color(58, 110, 165));
            g2.fillRect(0, 0, w, h);
        }

        // 2. Overlay oscuro semi-transparente
        g2.setColor(OVERLAY_COLOR);
        g2.fillRect(0, 0, w, h);

        // 3. Ventana del "instalador" centrada
        int dx = (w - DIALOG_W) / 2;
        int dy = (h - DIALOG_H) / 2;
        paintDialogWindow(g2, dx, dy);

        g2.dispose();
    }

    private void paintDialogWindow(Graphics2D g2, int x, int y) {
        // ── Sombra de la ventana ──────────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(x + 4, y + 4, DIALOG_W, DIALOG_H);

        // ── Borde exterior de la ventana ──────────────────────────────────
        g2.setColor(XP_WINDOW_BORDER);
        g2.fillRoundRect(x, y, DIALOG_W, DIALOG_H, 8, 8);

        // ── Barra de título (gradiente azul XP) ──────────────────────────
        GradientPaint titleGrad = new GradientPaint(
                x, y, XP_TITLE_BAR_TOP,
                x, y + TITLE_BAR_H, XP_TITLE_BAR_BOTTOM
        );
        g2.setPaint(titleGrad);
        g2.fillRoundRect(x + 2, y + 2, DIALOG_W - 4, TITLE_BAR_H, 6, 6);

        // Título
        g2.setColor(XP_TITLE_TEXT);
        g2.setFont(new Font("Tahoma", Font.BOLD, 13));
        g2.drawString("TowerDefense_Setup.exe", x + 10, y + 19);

        // Botón [X] de cerrar
        int closeX = x + DIALOG_W - 26;
        int closeY = y + 5;
        int closeSize = 18;
        closeRect = new Rectangle(closeX, closeY, closeSize, closeSize);

        g2.setColor(closeHovered ? XP_CLOSE_BTN_HOVER : XP_CLOSE_BTN_BG);
        g2.fillRect(closeX, closeY, closeSize, closeSize);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 12));
        g2.drawString("✕", closeX + 3, closeY + 14);

        // ── Cuerpo de la ventana ──────────────────────────────────────────
        g2.setColor(XP_WINDOW_BG);
        g2.fillRect(x + 2, y + TITLE_BAR_H + 2, DIALOG_W - 4, DIALOG_H - TITLE_BAR_H - 4);

        // ── Texto descriptivo ─────────────────────────────────────────────
        g2.setColor(XP_BUTTON_TEXT);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        String label = "Seleccione una tarea de instalación:";
        FontMetrics fm = g2.getFontMetrics();
        int labelX = x + (DIALOG_W - fm.stringWidth(label)) / 2;
        g2.drawString(label, labelX, y + TITLE_BAR_H + 40);

        // ── Línea separadora ──────────────────────────────────────────────
        g2.setColor(new Color(160, 160, 160));
        g2.drawLine(x + 20, y + TITLE_BAR_H + 55, x + DIALOG_W - 20, y + TITLE_BAR_H + 55);

        // ── Botones ──────────────────────────────────────────────────────
        int btnX = x + (DIALOG_W - BTN_W) / 2;
        for (int i = 0; i < 3; i++) {
            int btnY = y + BTN_START_Y + i * (BTN_H + BTN_GAP);
            buttonRects[i] = new Rectangle(btnX, btnY, BTN_W, BTN_H);
            paintButton(g2, btnX, btnY, buttonLabels[i], i == hoveredButton);
        }
    }

    private void paintButton(Graphics2D g2, int x, int y, String label, boolean hovered) {
        // Fondo del botón
        if (hovered) {
            GradientPaint hoverGrad = new GradientPaint(
                    x, y, new Color(200, 225, 255),
                    x, y + BTN_H, new Color(140, 185, 245)
            );
            g2.setPaint(hoverGrad);
        } else {
            GradientPaint normalGrad = new GradientPaint(
                    x, y, new Color(255, 255, 255),
                    x, y + BTN_H, new Color(220, 218, 200)
            );
            g2.setPaint(normalGrad);
        }
        g2.fillRoundRect(x, y, BTN_W, BTN_H, 4, 4);

        // Borde
        g2.setColor(hovered ? new Color(0, 80, 200) : XP_BUTTON_BORDER);
        g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
        g2.drawRoundRect(x, y, BTN_W, BTN_H, 4, 4);
        g2.setStroke(new BasicStroke(1f));

        // Texto
        g2.setColor(XP_BUTTON_TEXT);
        g2.setFont(new Font("Tahoma", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (BTN_W - fm.stringWidth(label)) / 2;
        int textY = y + (BTN_H + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(label, textX, textY);
    }

    // ─── Mouse events ────────────────────────────────────────────────────

    @Override
    public void mouseMoved(MouseEvent e) {
        int oldHovered = hoveredButton;
        boolean oldClose = closeHovered;

        hoveredButton = -1;
        closeHovered = false;

        for (int i = 0; i < 3; i++) {
            if (buttonRects[i] != null && buttonRects[i].contains(e.getPoint())) {
                hoveredButton = i;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                break;
            }
        }

        if (closeRect.contains(e.getPoint())) {
            closeHovered = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        if (hoveredButton == -1 && !closeHovered) {
            setCursor(Cursor.getDefaultCursor());
        }

        if (hoveredButton != oldHovered || closeHovered != oldClose) {
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (closeHovered) {
            if (onSalir != null) onSalir.run();
            return;
        }

        switch (hoveredButton) {
            case 0 -> { if (onJugar != null) onJugar.run(); }
            case 1 -> { if (onOpciones != null) onOpciones.run(); }
            case 2 -> { if (onSalir != null) onSalir.run(); }
        }
    }

    // ─── Unused mouse events ─────────────────────────────────────────────
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) { hoveredButton = -1; closeHovered = false; repaint(); }
    @Override public void mouseDragged(MouseEvent e) {}
}
