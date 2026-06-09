package com.miJuego.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;

/**
 * Panel de la introducción — secuencia de páginas tipo cómic.
 *
 * <p>Muestra una imagen/texto por página. El jugador avanza con
 * click izquierdo, ESPACIO o ENTER. Puede volver atrás con click
 * derecho o BACKSPACE. Puede saltear toda la intro con ESC.</p>
 *
 * <p>Las páginas se agregan con {@link #addPage(IntroPage)} antes
 * de mostrar el panel.</p>
 */
public class IntroPanel extends JPanel implements MouseListener, KeyListener {

    // ─── Colores ─────────────────────────────────────────────────────────
    private static final Color BG_COLOR     = new Color(0, 0, 0);
    private static final Color TEXT_COLOR   = new Color(220, 220, 220);
    private static final Color HINT_COLOR   = new Color(130, 130, 130);
    private static final Color PAGE_COLOR   = new Color(80, 80, 80);

    // ─── Páginas ─────────────────────────────────────────────────────────
    private final List<IntroPage> pages = new ArrayList<>();
    private int currentPage = 0;

    // Callback cuando termina la intro
    private Runnable onFinished;

    // ─── Modelo de una página ────────────────────────────────────────────
    public static class IntroPage {
        String title;           // Texto grande (puede ser null)
        String subtitle;        // Texto chico debajo (puede ser null)
        String[] bodyLines;     // Líneas de texto (puede ser null)
        BufferedImage image;    // Imagen de fondo o ilustración (puede ser null)

        public IntroPage() {}

        public IntroPage title(String t)       { this.title = t; return this; }
        public IntroPage subtitle(String s)    { this.subtitle = s; return this; }
        public IntroPage body(String... lines) { this.bodyLines = lines; return this; }
        public IntroPage image(BufferedImage i){ this.image = i; return this; }

        /** Carga imagen desde resources. */
        public IntroPage imageFromResource(String path) {
            try {
                InputStream is = IntroPage.class.getResourceAsStream(path);
                if (is != null) this.image = ImageIO.read(is);
            } catch (IOException ignored) {}
            return this;
        }
    }

    // --- Audio ---
    private Clip bgMusic;
    private FloatControl volumeControl;
    private Timer fadeTimer;
    private float currentVolume = -40f;
    private final float targetVolume = -15f; // Volumen aceptable y no invasivo

    // --- Typewriter Effect ---
    private Timer typewriterTimer;
    private int currentDisplayedChars = 0;
    private int totalCharsInPage = 0;

    // --- Transition ---
    private boolean isTransitioningOut = false;
    private long transitionStartTime = 0;
    private BufferedImage transitionImage;

    public IntroPanel() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(BG_COLOR);
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
        startMusic();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startTypewriter();
    }
    
    private void calculateTotalChars() {
        totalCharsInPage = 0;
        if (pages.isEmpty()) return;
        IntroPage page = pages.get(currentPage);
        if (page.bodyLines != null) {
            for (String line : page.bodyLines) {
                totalCharsInPage += line.length();
            }
        }
    }

    private void startTypewriter() {
        if (typewriterTimer != null) typewriterTimer.stop();
        currentDisplayedChars = 0;
        calculateTotalChars();
        
        typewriterTimer = new Timer(30, e -> {
            if (currentDisplayedChars < totalCharsInPage) {
                currentDisplayedChars++;
                repaint();
            } else {
                typewriterTimer.stop();
            }
        });
        typewriterTimer.start();
    }
    
    private void startMusic() {
        try {
            java.net.URL url = IntroPanel.class.getResource("/assets/intro/Intro.mp3");
            if (url == null) return;
            AudioInputStream in = AudioSystem.getAudioInputStream(url);
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
            bgMusic = AudioSystem.getClip();
            bgMusic.open(din);

            volumeControl = (FloatControl) bgMusic.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(currentVolume);

            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);

            fadeTimer = new Timer(100, e -> {
                if (currentVolume < targetVolume) {
                    currentVolume += 0.5f;
                    if (currentVolume > targetVolume) {
                        currentVolume = targetVolume;
                    }
                    if (volumeControl != null) {
                        try {
                            volumeControl.setValue(currentVolume);
                        } catch (Exception ex) {}
                    }
                } else {
                    if (fadeTimer != null) fadeTimer.stop();
                }
            });
            fadeTimer.start();
        } catch (Exception e) {
            System.err.println("No se pudo reproducir la música: " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (fadeTimer != null) fadeTimer.stop();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.close();
        }
    }

    public void addPage(IntroPage page) {
        pages.add(page);
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    // ─── Pintado ─────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // Fondo negro
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, w, h);

        if (isTransitioningOut) {
            long elapsed = System.currentTimeMillis() - transitionStartTime;
            float progress = Math.min(1.0f, elapsed / 2000f);
            
            if (transitionImage != null) {
                int imgW = transitionImage.getWidth();
                int imgH = transitionImage.getHeight();
                double scale = 1.0 + (2.0 * progress);
                
                double baseScale = Math.min((double) w / imgW, (double) h / imgH);
                int baseDrawW = (int) (imgW * baseScale);
                int baseDrawH = (int) (imgH * baseScale);
                
                int currentDrawW = (int) (baseDrawW * scale);
                int currentDrawH = (int) (baseDrawH * scale);
                int drawX = (w - currentDrawW) / 2;
                int drawY = (h - currentDrawH) / 2;
                
                g2.drawImage(transitionImage, drawX, drawY, currentDrawW, currentDrawH, null);
            }
            
            g2.setColor(new Color(0f, 0f, 0f, progress));
            g2.fillRect(0, 0, w, h);
            
            g2.dispose();
            return;
        }

        if (pages.isEmpty()) {
            g2.dispose();
            return;
        }

        IntroPage page = pages.get(currentPage);

        // Imagen de fondo (si tiene)
        if (page.image != null) {
            // Centrar y escalar manteniendo aspecto
            int imgW = page.image.getWidth();
            int imgH = page.image.getHeight();
            double scale = Math.min((double) w / imgW, (double) h / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int drawX = (w - drawW) / 2;
            int drawY = (h - drawH) / 2;
            g2.drawImage(page.image, drawX, drawY, drawW, drawH, null);

            // Ya no dibujamos overlay de pantalla completa
        }

        int y = h / 3;

        // Título
        if (page.title != null) {
            g2.setColor(TEXT_COLOR);
            g2.setFont(new Font("Tahoma", Font.BOLD, 32));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(page.title)) / 2;
            g2.drawString(page.title, textX, y);
            y += 50;
        }

        // Subtítulo
        if (page.subtitle != null) {
            g2.setColor(HINT_COLOR);
            g2.setFont(new Font("Tahoma", Font.ITALIC, 16));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(page.subtitle)) / 2;
            g2.drawString(page.subtitle, textX, y);
            y += 40;
        }

        // Dibujar texto como un recuadro de diálogo RPG
        if (page.bodyLines != null) {
            int boxW = w - 100;
            int boxH = 120;
            int boxX = (w - boxW) / 2;
            int boxY = h - boxH - 20;

            // Sombra
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRoundRect(boxX + 4, boxY + 4, boxW, boxH, 10, 10);

            // Fondo negro semi-opaco
            g2.setColor(new Color(15, 15, 15, 230));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

            // Borde cyan/azul brillante
            g2.setColor(new Color(40, 160, 255));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);
            g2.setStroke(new BasicStroke(1f));

            // Título "Narrador"
            g2.setColor(new Color(100, 200, 255));
            g2.setFont(new Font("Tahoma", Font.BOLD, 15));
            g2.drawString("Narrador", boxX + 20, boxY + 25);

            // Cuerpo del texto
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 15));
            int textY = boxY + 55;
            int charsToDraw = currentDisplayedChars;

            for (String line : page.bodyLines) {
                if (charsToDraw <= 0) break;
                String textToDraw = line;
                if (textToDraw.length() > charsToDraw) {
                    textToDraw = textToDraw.substring(0, charsToDraw);
                }
                g2.drawString(textToDraw, boxX + 20, textY);
                textY += 22;
                charsToDraw -= line.length();
            }

            // Hint de continuar dentro del recuadro (abajo a la derecha)
            g2.setColor(new Color(100, 200, 255));
            g2.setFont(new Font("Tahoma", Font.BOLD, 12));
            String continueHint = "Avanzar ►";
            FontMetrics fmHint = g2.getFontMetrics();
            g2.drawString(continueHint, boxX + boxW - fmHint.stringWidth(continueHint) - 15, boxY + boxH - 15);

            // Indicador de página dentro del recuadro (abajo a la izquierda)
            g2.setColor(new Color(150, 150, 150));
            g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
            String pageIndicator = (currentPage + 1) + " / " + pages.size();
            g2.drawString(pageIndicator, boxX + 20, boxY + boxH - 15);
        }

        // ── Indicadores y botones flotantes ───────────────────────────────────
        
        // 1. Botón "Saltar [ESC]" (Arriba a la derecha, siempre visible)
        int skipW = 100;
        int skipH = 26;
        int skipX = w - skipW - 20;
        int skipY = 20;
        
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(skipX, skipY, skipW, skipH, 8, 8);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(skipX, skipY, skipW, skipH, 8, 8);
        g2.setStroke(new BasicStroke(1f));
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 12));
        String skipText = "Saltar [ESC]";
        FontMetrics fmSkip = g2.getFontMetrics();
        g2.drawString(skipText, skipX + (skipW - fmSkip.stringWidth(skipText)) / 2, skipY + 18);

        // Si NO hay cuadro de diálogo, mostramos Avanzar flotante y la página flotante
        if (page.bodyLines == null) {
            int nextW = 120;
            int nextH = 26;
            int nextX = w - nextW - 20;
            int nextY = h - nextH - 20;
            
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(nextX, nextY, nextW, nextH, 8, 8);
            g2.setColor(new Color(100, 200, 255, 150));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(nextX, nextY, nextW, nextH, 8, 8);
            g2.setStroke(new BasicStroke(1f));
            
            g2.setColor(new Color(220, 240, 255));
            g2.setFont(new Font("Tahoma", Font.BOLD, 12));
            String nextText = "Avanzar ►";
            FontMetrics fmNext = g2.getFontMetrics();
            g2.drawString(nextText, nextX + (nextW - fmNext.stringWidth(nextText)) / 2, nextY + 18);
            
            g2.setColor(new Color(200, 200, 200, 150));
            g2.setFont(new Font("Tahoma", Font.BOLD, 14));
            String pageIndicator = (currentPage + 1) + " / " + pages.size();
            g2.drawString(pageIndicator, 25, h - 25);
        }

        g2.dispose();
    }

    // ─── Navegación ──────────────────────────────────────────────────────

    private void nextPage() {
        if (currentDisplayedChars < totalCharsInPage) {
            // Si no terminó de escribir, lo mostramos todo de golpe
            currentDisplayedChars = totalCharsInPage;
            if (typewriterTimer != null) typewriterTimer.stop();
            repaint();
            return;
        }

        if (currentPage < pages.size() - 1) {
            currentPage++;
            startTypewriter();
            repaint();
        } else {
            if (!isTransitioningOut) {
                startTransitionOut();
            }
        }
    }

    private void startTransitionOut() {
        isTransitioningOut = true;
        transitionStartTime = System.currentTimeMillis();
        if (typewriterTimer != null) typewriterTimer.stop();
        
        if (!pages.isEmpty()) {
            transitionImage = pages.get(pages.size() - 1).image;
        }
        
        Timer transitionTimer = new Timer(33, e -> {
            long elapsed = System.currentTimeMillis() - transitionStartTime;
            if (elapsed >= 2000) { // 2 seconds transition
                ((Timer)e.getSource()).stop();
                finish();
            } else {
                repaint();
            }
        });
        transitionTimer.start();
    }

    private void prevPage() {
        if (isTransitioningOut) return;
        if (currentPage > 0) {
            currentPage--;
            startTypewriter();
            repaint();
        }
    }

    private void finish() {
        if (typewriterTimer != null) typewriterTimer.stop();
        stopMusic();
        if (onFinished != null) onFinished.run();
    }

    // ─── Eventos ─────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isTransitioningOut) return;
        if (e.getButton() == MouseEvent.BUTTON1) {
            nextPage();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            prevPage();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isTransitioningOut && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            finish();
            return;
        }
        if (isTransitioningOut) return;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE, KeyEvent.VK_ENTER, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN -> nextPage();
            case KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT, KeyEvent.VK_UP -> prevPage();
            case KeyEvent.VK_ESCAPE -> finish();
        }
    }

    // ── Unused ────────────────────────────────────────────────────────────
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
