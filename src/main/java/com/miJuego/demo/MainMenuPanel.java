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
public class MainMenuPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

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

    // --- Audio y Animación ---
    private javax.sound.sampled.Clip bgMusic;
    private BufferedImage[] animFrames = new BufferedImage[5];
    private boolean isPlayingIntro = false;
    private long introStartTime = 0;
    private final long INTRO_DURATION_MS = 11000;
    private Timer renderTimer;
    private static boolean hasPlayedIntroThisSession = false;

    // Callback para cuando el usuario elige una opción
    private Runnable onJugar;
    private Runnable onOpciones;
    private Runnable onSalir;

    public MainMenuPanel() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);

        // Cargar frames de animación
        for (int i = 0; i < 5; i++) {
            try {
                java.net.URL frameUrl = getClass().getResource("/assets/menu/Animacion_intro" + i + ".png");
                if (frameUrl != null) animFrames[i] = ImageIO.read(frameUrl);
            } catch (Exception e) {}
        }

        // Cargar imagen de fondo de la portada
        try {
            java.net.URL portadaUrl = getClass().getResource("/assets/menu/portada.png");
            if (portadaUrl != null) {
                desktopBg = ImageIO.read(portadaUrl);
            } else {
                // Fallback si no está la portada aún
                desktopBg = ImageIO.read(getClass().getResourceAsStream("/assets/menu/xp_desktop.png"));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de portada: " + e.getMessage());
            desktopBg = null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
        startMusicAndAnimation();
    }

    private void startMusicAndAnimation() {
        if (bgMusic != null && bgMusic.isRunning()) return;
        try {
            java.net.URL url = getClass().getResource("/assets/menu/Buddy_Attacks! Main Theme.mp3");
            if (url == null) return;
            javax.sound.sampled.AudioInputStream in = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            javax.sound.sampled.AudioFormat baseFormat = in.getFormat();
            javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            javax.sound.sampled.AudioInputStream din = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, in);
            bgMusic = javax.sound.sampled.AudioSystem.getClip();
            bgMusic.open(din);
            bgMusic.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);

            if (!hasPlayedIntroThisSession) {
                isPlayingIntro = true;
                introStartTime = System.currentTimeMillis();
                hasPlayedIntroThisSession = true;
            } else {
                isPlayingIntro = false;
                // Si ya se jugó la intro, saltamos directamente al segundo 11
                bgMusic.setMicrosecondPosition(11000000);
            }
            
            if (renderTimer != null) renderTimer.stop();
            renderTimer = new Timer(33, e -> {
                if (isPlayingIntro) {
                    if (System.currentTimeMillis() - introStartTime >= INTRO_DURATION_MS) {
                        isPlayingIntro = false;
                        renderTimer.stop();
                    }
                    repaint();
                } else {
                    renderTimer.stop();
                    repaint();
                }
            });
            renderTimer.start();
        } catch (Exception e) {
            System.err.println("Error music: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.close();
            bgMusic = null;
        }
        if (renderTimer != null) renderTimer.stop();
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

        if (isPlayingIntro) {
            long elapsed = System.currentTimeMillis() - introStartTime;
            if (elapsed >= INTRO_DURATION_MS) {
                isPlayingIntro = false;
            } else {
                paintIntroAnimation(g2, w, h, elapsed);
                g2.dispose();
                return;
            }
        }

        // 1. Fondo (Portada)
        if (desktopBg != null) {
            g2.drawImage(desktopBg, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.WHITE);
            g2.drawString("Falta portada.png en assets/menu", 20, 20);
        }

        // 2. Botones del menú (Posicionados abajo al centro para encajar en el recuadro negro)
        int btnW = 180;
        int btnH = 30;
        int btnGap = 12;
        // Ajustamos la posición: un poco más arriba y un poco a la derecha del centro exacto
        int startY = h - 185; 
        int btnX = (w - btnW) / 2 + 45;

        for (int i = 0; i < 3; i++) {
            int btnY = startY + i * (btnH + btnGap);
            buttonRects[i] = new Rectangle(btnX, btnY, btnW, btnH);
            paintMenuButton(g2, btnX, btnY, btnW, btnH, buttonLabels[i], i == hoveredButton);
        }

        g2.dispose();
    }

    private void paintIntroAnimation(Graphics2D g2, int w, int h, long elapsed) {
        int frameIndex = (int) (elapsed / 2200);
        if (frameIndex > 4) frameIndex = 4;
        long timeInFrame = elapsed % 2200;
        
        BufferedImage img = animFrames[frameIndex];
        if (img != null) {
            g2.drawImage(img, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.WHITE);
            g2.drawString("Animación " + frameIndex, w/2, h/2);
        }
        
        // Destello (flash) al principio de cada frame
        if (timeInFrame < 150) {
            float alpha = 1.0f - (timeInFrame / 150f);
            g2.setColor(new Color(1f, 1f, 1f, alpha));
            g2.fillRect(0, 0, w, h);
        }
        // Fundido a negro al final de cada frame
        else if (timeInFrame > 1900) {
            float alpha = (timeInFrame - 1900f) / 300f;
            if (alpha > 1f) alpha = 1f;
            g2.setColor(new Color(0f, 0f, 0f, alpha));
            g2.fillRect(0, 0, w, h);
        }
    }

    private void paintMenuButton(Graphics2D g2, int x, int y, int w, int h, String label, boolean hovered) {
        // En lugar de botones estilo XP, hacemos botones sutiles que peguen con el diseño épico
        // Fondo semi-transparente cuando hay hover
        if (hovered) {
            g2.setColor(new Color(40, 160, 255, 60)); // Cyan suave
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(new Color(40, 160, 255));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, w, h, 8, 8);
            g2.setStroke(new BasicStroke(1f));
        }

        // Texto del botón
        g2.setColor(hovered ? new Color(150, 220, 255) : Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (w - fm.stringWidth(label)) / 2;
        int textY = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(label, textX, textY);
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
        if (isPlayingIntro) {
            skipIntro();
            return;
        }

        if (closeHovered) {
            if (onSalir != null) onSalir.run();
            return;
        }

        switch (hoveredButton) {
            case 0 -> { stopMusic(); if (onJugar != null) onJugar.run(); }
            case 1 -> { if (onOpciones != null) onOpciones.run(); }
            case 2 -> { if (onSalir != null) onSalir.run(); }
        }
    }

    private void skipIntro() {
        if (isPlayingIntro) {
            isPlayingIntro = false;
            if (bgMusic != null) {
                bgMusic.setMicrosecondPosition(11000000); // Salta al drop
            }
            if (renderTimer != null) renderTimer.stop();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            skipIntro();
        }
    }

    // ─── Unused events ─────────────────────────────────────────────
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) { hoveredButton = -1; closeHovered = false; repaint(); }
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
