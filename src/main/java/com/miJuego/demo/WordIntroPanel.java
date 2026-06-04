package com.miJuego.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

/**
 * Escena interactiva "dentro de Word".
 *
 * <p>Flujo:</p>
 * <ol>
 *   <li>Word visible, jugador guarda con Ctrl+S o botón disquete.</li>
 *   <li>3 errores XP estilo Windows (modal, sin overlay).</li>
 *   <li>Clippy aparece flotando sobre el documento, globo clásico de Office.</li>
 *   <li>Cinemática de los Pop-Ups → cuadro RPG (fondo gris semi-opaco).</li>
 *   <li>Callback {@link #setOnFinished(Runnable)} → tutorial de torres.</li>
 * </ol>
 */
public class WordIntroPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

    // ─── Estados ──────────────────────────────────────────────────────────
    private enum Scene {
        WORD_IDLE,       // Esperando Ctrl+S / click disquete
        INTERNAL_DIALOG, // Diálogo interno del protagonista (estilo Undertale)
        CLIPPY_CONTROL,  // Clippy tomando el control y moviendo el mouse
        CLIPPY_BUBBLE,   // Clippy flotando, globo Office clásico
        RPG_DIALOG,      // Cuadro RPG para la cinemática de Pop-Ups
        DONE
    }

    private Scene scene = Scene.WORD_IDLE;
    private int saveAttempts = 0;
    private static final int MAX_ERRORS = 3;

    // Diálogo interno del protagonista
    private String internalDialogueText = "";

    // Animación de Clippy tomando control
    private Timer clippyAnimationTimer;
    private float virtualMouseX = 300f;
    private float virtualMouseY = 300f;
    private boolean showVirtualMouse = false;
    private int clippyControlStep = 0;

    // ─── Assets ───────────────────────────────────────────────────────────
    private BufferedImage wordBg;
    private BufferedImage clippySprite;
    private BufferedImage systemSprite;
    private BufferedImage[] wordErrorBgs = new BufferedImage[3];

    // Expresiones de Clippy dinámicas
    private final java.util.Map<String, BufferedImage> clippyExpressions = new java.util.HashMap<>();

    // ─── Globo clásico de Clippy — múltiples "páginas" ───────────────────
    private final String[][] clippyBubblePages = {
        {
            "¡Hola! Soy Clippy, tu asistente.",
            "Parece que estás intentando",
            "guardar un documento..."
        },
        {
            "¿Te gustaría recibir mi ayuda",
            "para solucionar este problema?"
        }
    };
    private final String[] clippyBubbleExpressions = {
        "hablando",      // ¡Hola! Soy Clippy, tu asistente...
        "solicitando"    // ¿Te gustaría recibir mi ayuda...
    };
    private int clippyPageIndex = 0;

    // ─── Diálogo RPG (cinemática Pop-Ups) ────────────────────────────────
    private final String[][] rpgLines = {
        {"Clippy",   "¿Eh? Qué raro que otra aplicación esté usando este programa...", "sorprendido"},
        {"Clippy",   "Déjame verificar los procesos activos en segundo plano...", "pensando"},
        {"Clippy",   "¿buddy.exe? Qué raro... Nunca había visto ese archivo.", "sorprendido"},
        {"Clippy",   "¡Atención! Algo muy raro está sucediendo, mira eso en la pantalla...", "preocupado"},
        {"Clippy",   "Esas ventanas emergentes van a destruir todo el documento.", "triste"},
        {"Clippy",   "Ten, toma esto. Yo te guiaré sobre cómo enfrentar esto.", "feliz"},
        {"Sistema",  "¡Primera torre desbloqueada: Torre Antivirus!", "alerta"},
        {"Sistema",  "Usa el mouse para defender el documento del malware.", "alerta"}
    };
    private int rpgLineIndex = 0;

    // ─── Área del botón disquete — se calcula en addNotify ───────────────
    private Rectangle saveButtonRect = new Rectangle(56, 50, 20, 20);
    private boolean saveHover = false;

    // ─── Botón Saltar intro ────────────────────────────────────────────────
    private final Rectangle skipButtonRect = new Rectangle(0, 0, 130, 26); // se recalcula al pintar
    private boolean skipHover = false;

    // ─── Botón Aceptar para diálogo RPG ────────────────────────────────────
    private final Rectangle okButtonRect = new Rectangle(0, 0, 85, 24);
    private boolean dialogOkHover = false;

    // ─── Clippy flotante — posición ───────────────────────────────────────
    private static final int CLIPPY_W = 80;
    private static final int CLIPPY_H = 100;

    // ─── Colores ──────────────────────────────────────────────────────────
    // XP error dialog
    private static final Color XP_TITLE_TOP   = new Color(0, 88, 238);
    private static final Color XP_TITLE_BOT   = new Color(0, 48, 160);
    private static final Color XP_DIALOG_BG   = new Color(236, 233, 216);
    private static final Color XP_DIALOG_BDR  = new Color(0, 60, 165);
    private static final Color XP_ERROR_ICON  = new Color(200, 30, 30);

    // Globo Clippy
    private static final Color BUBBLE_BG      = new Color(255, 255, 225);
    private static final Color BUBBLE_BDR     = new Color(80, 80, 80);
    private static final Color BUBBLE_TEXT    = new Color(0, 0, 0);
    private static final Color BUBBLE_HINT    = new Color(120, 120, 120);

    // RPG dialog
    private static final Color RPG_OVERLAY    = new Color(0, 0, 0, 160);
    private static final Color RPG_BOX_BG     = new Color(30, 30, 30, 230);
    private static final Color RPG_BOX_BDR    = new Color(220, 220, 220);
    private static final Color RPG_TEXT       = new Color(255, 255, 255);
    private static final Color RPG_NAME_CL    = new Color(80, 180, 255);
    private static final Color RPG_NAME_SYS   = new Color(190, 190, 190);
    private static final Color RPG_HINT_COL   = new Color(120, 120, 120);

    // Colores del nuevo diseño de ventana de diálogo (Concepto)
    private static final Color PALETTE_BG     = new Color(13, 27, 42);     // #0D1B2A
    private static final Color PALETTE_PANEL  = new Color(30, 42, 56);     // #1E2A38
    private static final Color PALETTE_BORDER = new Color(47, 64, 84);     // #2F4054
    private static final Color PALETTE_ACCENT = new Color(64, 136, 255);   // #4088FF
    private static final Color PALETTE_TEXT   = new Color(237, 237, 237);   // #EDEDED
    private static final Color PALETTE_CLOSE  = new Color(255, 59, 48);    // #FF3B30
    private static final Color PALETTE_DETAIL = new Color(138, 138, 138);   // #8A8A8A

    // ─── Callback ─────────────────────────────────────────────────────────
    private Runnable onFinished;

    // ─────────────────────────────────────────────────────────────────────
    public WordIntroPanel() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(new Color(180, 180, 180));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        loadAssets();
    }

    private void loadAssets() {
        wordBg       = loadImage("/assets/word/word_background.png");
        clippySprite = loadImage("/assets/word/clippy_sprite.png");
        systemSprite = loadImage("/assets/word/popup_error_f0.png");
        wordErrorBgs[0] = loadImage("/assets/word/word_error_bg0.png");
        wordErrorBgs[1] = loadImage("/assets/word/word_error_bg1.png");
        wordErrorBgs[2] = loadImage("/assets/word/word_error_bg2.png");

        // Cargar expresiones de Clippy
        loadClippyExpression("neutro", "clippy_neutro.png");
        loadClippyExpression("feliz", "clippy_feliz.png");
        loadClippyExpression("preocupado", "clippy_preocupado.png");
        loadClippyExpression("pensando", "clippy_pensando.png");
        loadClippyExpression("sorprendido", "clippySorprendido.png");
        loadClippyExpression("triste", "clippy_triste.png");
        loadClippyExpression("aprobando", "clippy_aprobando.png");
        loadClippyExpression("entusiasmado", "clippy_entusiasmado.png");
        loadClippyExpression("hablando", "clippy_hablando.png");
        loadClippyExpression("interesado", "clippy_interesado.png");
        loadClippyExpression("leyendo", "clippy_leyendo.png");
        loadClippyExpression("solicitando", "clippy_solicitando.png");
    }

    private void loadClippyExpression(String key, String filename) {
        BufferedImage img = loadImage("/assets/word/" + filename);
        if (img != null) {
            clippyExpressions.put(key, img);
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("No se pudo cargar: " + path);
        }
        return null;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    // ─── Pintado ──────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // ── Fondo de Word (SIEMPRE visible) ──────────────────────────────
        if (wordBg != null) {
            g2.drawImage(wordBg, 0, 0, w, h, null);
        } else {
            drawFallbackWord(g2, w, h);
        }

        // ── Highlight del botón disquete en modo IDLE ─────────────────────
        if (scene == Scene.WORD_IDLE) {
            // Resaltar el botón según posición relativa al panel
            recalcSaveButton(w, h);
            if (saveHover) {
                // Relleno gris semi-transparente estilo hover
                g2.setColor(new Color(128, 128, 128, 60));
                g2.fillRect(saveButtonRect.x, saveButtonRect.y,
                        saveButtonRect.width, saveButtonRect.height);
                // Borde gris más oscuro
                g2.setColor(new Color(100, 100, 100, 180));
                g2.drawRect(saveButtonRect.x, saveButtonRect.y,
                        saveButtonRect.width, saveButtonRect.height);
            }
            drawSaveHintBar(g2, w, h);
        }

        // ── Escenas superpuestas ──────────────────────────────────────────
        switch (scene) {
            case INTERNAL_DIALOG -> drawInternalDialog(g2, w, h, internalDialogueText);
            case CLIPPY_CONTROL  -> drawClippyControlScene(g2, w, h);
            case CLIPPY_BUBBLE   -> drawClippyScene(g2, w, h);
            case RPG_DIALOG      -> drawRpgDialog(g2, w, h);
            default -> {}
        }

        // ── Botón "Saltar intro" (siempre visible salvo DONE) ─────────────
        if (scene != Scene.DONE && scene != Scene.RPG_DIALOG) {
            drawSkipButton(g2, w, h);
        }

        g2.dispose();
    }

    /** Dibuja el botón "Saltar intro [ESC]" en la esquina inferior derecha. */
    private void drawSkipButton(Graphics2D g2, int w, int h) {
        int bw = 130, bh = 26;
        int bx = w - bw - 10;
        int by = h - bh - 8;
        skipButtonRect.setBounds(bx, by, bw, bh);

        // Fondo estilo XP
        GradientPaint gp = skipHover
                ? new GradientPaint(bx, by, new Color(200, 220, 255), bx, by + bh, new Color(140, 180, 240))
                : new GradientPaint(bx, by, new Color(245, 245, 245), bx, by + bh, new Color(215, 213, 200));
        g2.setPaint(gp);
        g2.fillRoundRect(bx, by, bw, bh, 4, 4);
        g2.setColor(skipHover ? new Color(0, 80, 200) : new Color(100, 100, 100));
        g2.setStroke(new BasicStroke(skipHover ? 1.5f : 1f));
        g2.drawRoundRect(bx, by, bw - 1, bh - 1, 4, 4);
        g2.setStroke(new BasicStroke(1f));

        // Texto
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        String label = "Saltar intro  [ESC]";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, bx + (bw - fm.stringWidth(label)) / 2, by + (bh + fm.getAscent() - fm.getDescent()) / 2);
    }

    /**
     * Recalcula el área del botón disquete según el tamaño real del panel.
     *
     * <p>En la imagen de 640x480, el disquete (3.º icono de la toolbar) está
     * aproximadamente en x=42, y=49, w=20, h=20.
     * Se escala proporcionalmente al tamaño actual del panel.</p>
     */
    private void recalcSaveButton(int w, int h) {
        // Coordenadas base en la imagen original 1024x1024
        final int BASE_X = 134, BASE_Y = 108, BASE_W = 29, BASE_H = 27;
        final int BASE_IW = 1024, BASE_IH = 1024;
        int bx = BASE_X * w / BASE_IW;
        int by = BASE_Y * h / BASE_IH;
        int bw = BASE_W * w / BASE_IW;
        int bh = BASE_H * h / BASE_IH;
        saveButtonRect = new Rectangle(bx, by, Math.max(bw, 18), Math.max(bh, 18));
    }

    private void drawSaveHintBar(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(w / 2 - 170, h - 36, 340, 26, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 13));
        String hint = "Ctrl+S para guardar  ·  o click en el disquete";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, w / 2 - fm.stringWidth(hint) / 2, h - 17);
    }

    // ─── Clippy flotante estilo Office Assistant ──────────────────────────
    private void drawClippyScene(Graphics2D g2, int w, int h) {
        // SIN overlay oscuro — el documento sigue 100% visible

        // Posición de Clippy: esquina inferior derecha
        int cx = w - CLIPPY_W - 20;
        int cy = h - CLIPPY_H - 20;

        // Primero dibujar el globo (encima del doc, debajo de Clippy)
        drawOfficeBubble(g2, cx, cy, clippyBubblePages[clippyPageIndex]);

        BufferedImage currentClippy = null;
        if (clippyPageIndex >= 0 && clippyPageIndex < clippyBubbleExpressions.length) {
            currentClippy = clippyExpressions.get(clippyBubbleExpressions[clippyPageIndex]);
        }
        if (currentClippy == null) {
            currentClippy = clippySprite;
        }

        // Luego dibujar a Clippy encima del globo
        if (currentClippy != null) {
            g2.drawImage(currentClippy, cx, cy, CLIPPY_W, CLIPPY_H, null);
        } else {
            // Fallback: paperclip amarillo
            g2.setColor(new Color(220, 180, 0));
            g2.setStroke(new BasicStroke(4f));
            g2.drawArc(cx + 15, cy + 5, 50, 60, 0, 270);
            g2.drawArc(cx + 25, cy + 30, 30, 40, 0, 270);
            g2.setStroke(new BasicStroke(1f));
            // Ojos
            g2.setColor(Color.WHITE);
            g2.fillOval(cx + 28, cy + 15, 14, 12);
            g2.fillOval(cx + 46, cy + 15, 14, 12);
            g2.setColor(Color.BLACK);
            g2.fillOval(cx + 32, cy + 18, 6, 6);
            g2.fillOval(cx + 50, cy + 18, 6, 6);
        }
    }

    /** Globo de diálogo estilo Office Assistant clásico. */
    private void drawOfficeBubble(Graphics2D g2, int clippyX, int clippyY, String[] lines) {
        int bw = 240;
        int bh = 30 + lines.length * 20 + 28;
        int bx = clippyX - bw - 10;
        int by = clippyY + 5;

        // Si sale por la izquierda, ajustar
        if (bx < 10) bx = 10;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(bx + 3, by + 3, bw, bh, 12, 12);

        // Fondo del globo
        g2.setColor(BUBBLE_BG);
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(BUBBLE_BDR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        // Cola del globo apuntando a Clippy (triángulo en borde derecho)
        int tailBaseY = by + 20;
        int tailTipX  = clippyX + 10;
        int tailTipY  = clippyY + 30;
        int[] px = { bx + bw, bx + bw, tailTipX };
        int[] py = { tailBaseY, tailBaseY + 14, tailTipY };
        g2.setColor(BUBBLE_BG);
        g2.fillPolygon(px, py, 3);
        g2.setColor(BUBBLE_BDR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(px[0], py[0], px[2], py[2]);
        g2.drawLine(px[1], py[1], px[2], py[2]);
        g2.setStroke(new BasicStroke(1f));

        // Texto
        g2.setColor(BUBBLE_TEXT);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 13));
        int ty = by + 26;
        for (String line : lines) {
            g2.drawString(line, bx + 14, ty);
            ty += 20;
        }

        // Hint de avance
        g2.setColor(BUBBLE_HINT);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
        String hint = clippyPageIndex == clippyBubblePages.length - 1
                ? "Click o ESPACIO — Continuar"
                : "Click o ESPACIO — Siguiente";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, bx + bw - fm.stringWidth(hint) - 10, by + bh - 8);
    }

    // ─── Diálogo estilo Ventana de Sistema (Diseño Concepto) ──────────────
    private void drawSystemDialogBox(Graphics2D g2, int w, int h, String speakerName, String messageText, String expression) {
        // Caja de diálogo centrada
        int boxW = 540;
        int boxH = 190;
        int boxX = (w - boxW) / 2;
        int boxY = h - boxH - 25;

        // Sombra de la ventana
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(boxX + 4, boxY + 4, boxW, boxH);

        // Fondo de la ventana (Oscuro Palette)
        g2.setColor(PALETTE_BG);
        g2.fillRect(boxX, boxY, boxW, boxH);

        // Borde exterior
        g2.setColor(PALETTE_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(boxX, boxY, boxW - 1, boxH - 1);
        g2.drawRect(boxX + 1, boxY + 1, boxW - 3, boxH - 3);
        g2.setStroke(new BasicStroke(1f));

        // Línea de relieve interior (brillo superior e izquierdo)
        g2.setColor(PALETTE_PANEL);
        g2.drawLine(boxX + 2, boxY + 2, boxX + boxW - 3, boxY + 2);
        g2.drawLine(boxX + 2, boxY + 2, boxX + 2, boxY + boxH - 3);

        // Barra de título
        int titleBarH = 26;
        GradientPaint gp = new GradientPaint(
                boxX, boxY, PALETTE_ACCENT,
                boxX, boxY + titleBarH, new Color(20, 80, 180));
        g2.setPaint(gp);
        g2.fillRect(boxX + 2, boxY + 2, boxW - 4, titleBarH - 1);

        // Icono de la barra de título
        g2.setColor(new Color(80, 160, 240));
        g2.fillRect(boxX + 8, boxY + 6, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 9));
        g2.drawString("W", boxX + 11, boxY + 17);

        // Texto del título
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 11));
        g2.drawString(speakerName, boxX + 28, boxY + 17);

        // Botones en la barra de título (Estilo Retro)
        int btnW = 18;
        int btnH = 18;
        // 1. Minimizar
        int minX = boxX + boxW - 68;
        int minY = boxY + 5;
        g2.setColor(PALETTE_BORDER);
        g2.fillRect(minX, minY, btnW, btnH);
        g2.setColor(PALETTE_TEXT);
        g2.fillRect(minX + 4, minY + btnH - 6, btnW - 8, 2);

        // 2. Maximizar
        int maxX = boxX + boxW - 46;
        int maxY = boxY + 5;
        g2.setColor(PALETTE_BORDER);
        g2.fillRect(maxX, maxY, btnW, btnH);
        g2.setColor(PALETTE_TEXT);
        g2.drawRect(maxX + 4, maxY + 4, btnW - 9, btnH - 9);

        // 3. Cerrar
        int closeX = boxX + boxW - 24;
        int closeY = boxY + 5;
        g2.setColor(PALETTE_CLOSE);
        g2.fillRect(closeX, closeY, btnW, btnH);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 11));
        g2.drawString("✕", closeX + 4, closeY + 13);

        // Área de personaje a la izquierda
        int charW = 75;
        int charH = 94;
        int charX = boxX + 16;
        int charY = boxY + titleBarH + 12;

        if (speakerName.equals("Clippy")) {
            BufferedImage currentClippy = clippyExpressions.get(expression);
            if (currentClippy == null) {
                currentClippy = clippySprite;
            }
            if (currentClippy != null) {
                g2.drawImage(currentClippy, charX, charY, charW, charH, null);
            } else {
                drawFallbackClippy(g2, charX, charY, charW, charH);
            }
        } else {
            // Dibujar el enemigo de error caminando (personaje de error)
            if (systemSprite != null) {
                g2.drawImage(systemSprite, charX, charY + 10, charW, (int)(charW * 0.95), null);
            } else {
                // Icono de información estilo Windows XP
                drawXpInfoIcon(g2, charX + 15, charY + 20, 36);
            }
        }

        // Burbuja de diálogo (panel de texto) a la derecha
        int bubbleX = boxX + 110;
        int bubbleY = boxY + titleBarH + 15;
        int bubbleW = boxW - 130;
        int bubbleH = boxH - titleBarH - 58;

        // Fondo de la burbuja
        g2.setColor(PALETTE_PANEL);
        g2.fillRoundRect(bubbleX, bubbleY, bubbleW, bubbleH, 6, 6);
        g2.setColor(PALETTE_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bubbleX, bubbleY, bubbleW, bubbleH, 6, 6);
        g2.setStroke(new BasicStroke(1f));

        // Colita de la burbuja apuntando al personaje
        int tailX = bubbleX;
        int tailY = bubbleY + bubbleH / 2;
        g2.setColor(PALETTE_PANEL);
        int[] txPoints = { tailX + 2, tailX - 10, tailX + 2 };
        int[] tyPoints = { tailY - 8, tailY, tailY + 8 };
        g2.fillPolygon(txPoints, tyPoints, 3);

        g2.setColor(PALETTE_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(tailX + 1, tailY - 8, tailX - 10, tailY);
        g2.drawLine(tailX - 10, tailY, tailX + 1, tailY + 8);
        g2.setStroke(new BasicStroke(1f));

        // Scrollbar decorativo a la derecha de la burbuja
        int sbX = bubbleX + bubbleW - 20;
        int sbY = bubbleY + 4;
        int sbW = 16;
        int sbH = bubbleH - 8;

        g2.setColor(PALETTE_BG);
        g2.fillRect(sbX, sbY, sbW, sbH);
        g2.setColor(PALETTE_BORDER);
        g2.drawRect(sbX, sbY, sbW - 1, sbH - 1);

        // Flecha arriba
        g2.setColor(PALETTE_PANEL);
        g2.fillRect(sbX + 2, sbY + 2, sbW - 4, 12);
        g2.setColor(PALETTE_TEXT);
        int[] upX = {sbX + sbW/2, sbX + 4, sbX + sbW - 5};
        int[] upY = {sbY + 5, sbY + 10, sbY + 10};
        g2.fillPolygon(upX, upY, 3);

        // Flecha abajo
        g2.setColor(PALETTE_PANEL);
        g2.fillRect(sbX + 2, sbY + sbH - 14, sbW - 4, 12);
        g2.setColor(PALETTE_TEXT);
        int[] downX = {sbX + sbW/2, sbX + 4, sbX + sbW - 5};
        int[] downY = {sbY + sbH - 6, sbY + sbH - 11, sbY + sbH - 11};
        g2.fillPolygon(downX, downY, 3);

        // Slider handle
        int sliderH = 32;
        int sliderY = sbY + 18 + (sbH - 36 - sliderH) / 2;
        g2.setColor(PALETTE_ACCENT);
        g2.fillRect(sbX + 2, sliderY, sbW - 4, sliderH);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawLine(sbX + 3, sliderY + 1, sbX + 3, sliderY + sliderH - 2);

        // Texto del mensaje
        int textX = bubbleX + 15;
        int textY = bubbleY + 20;
        int textW = bubbleW - 45;
        drawWrappedText(g2, messageText, textX, textY, textW, 18);

        // Botón "Saltar intro [ESC]" estilo Concepto
        int skipW = 120;
        int skipH = 23;
        int skipX = bubbleX;
        int skipY = boxY + boxH - 31;
        skipButtonRect.setBounds(skipX, skipY, skipW, skipH);

        g2.setColor(PALETTE_BG);
        g2.fillRect(skipX, skipY, skipW, skipH);
        g2.setColor(skipHover ? PALETTE_ACCENT : PALETTE_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(skipX, skipY, skipW - 1, skipH - 1, 3, 3);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(PALETTE_TEXT);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
        FontMetrics skipFm = g2.getFontMetrics();
        String skipText = "Saltar intro [ESC]";
        g2.drawString(skipText, skipX + (skipW - skipFm.stringWidth(skipText)) / 2, skipY + (skipH + skipFm.getAscent() - skipFm.getDescent()) / 2);

        // Botón "Aceptar [ESPACIO]" estilo Concepto
        int acceptBtnW = 120;
        int acceptBtnH = 23;
        int btnX = boxX + boxW - acceptBtnW - 20;
        int btnY = boxY + boxH - 31;
        okButtonRect.setBounds(btnX, btnY, acceptBtnW, acceptBtnH);

        g2.setColor(PALETTE_BG);
        g2.fillRect(btnX, btnY, acceptBtnW, acceptBtnH);
        g2.setColor(dialogOkHover ? PALETTE_ACCENT : PALETTE_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(btnX, btnY, acceptBtnW - 1, acceptBtnH - 1, 3, 3);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(PALETTE_TEXT);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        String btnText = "Aceptar [ESPACIO]";
        g2.drawString(btnText, btnX + (acceptBtnW - fm.stringWidth(btnText)) / 2, btnY + (acceptBtnH + fm.getAscent() - fm.getDescent()) / 2);
    }

    private void drawRpgDialog(Graphics2D g2, int w, int h) {
        // Overlay gris oscuro semi-transparente — el documento se sigue viendo
        g2.setColor(RPG_OVERLAY);
        g2.fillRect(0, 0, w, h);

        String speakerName = "Asistente";
        String messageText = "";
        String expression = "neutro";
        if (rpgLineIndex < rpgLines.length) {
            speakerName = rpgLines[rpgLineIndex][0];
            messageText = rpgLines[rpgLineIndex][1];
            if (rpgLines[rpgLineIndex].length > 2) {
                expression = rpgLines[rpgLineIndex][2];
            }
        }
        drawSystemDialogBox(g2, w, h, speakerName, messageText, expression);
    }

    private void drawXpInfoIcon(Graphics2D g2, int x, int y, int size) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Círculo azul info
        g2.setColor(new Color(0, 88, 238));
        g2.fillOval(x, y, size, size);
        g2.setColor(new Color(0, 48, 160));
        g2.drawOval(x, y, size - 1, size - 1);

        // Letra "i"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, (int)(size * 0.65)));
        FontMetrics fm = g2.getFontMetrics();
        String letter = "i";
        g2.drawString(letter, x + (size - fm.stringWidth(letter)) / 2, y + (size + fm.getAscent() - fm.getDescent()) / 2 - 2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawFallbackClippy(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(220, 180, 0));
        g2.setStroke(new BasicStroke(3f));
        g2.drawArc(x + w/4, y + h/8, w/2, h/2, 0, 270);
        g2.drawArc(x + w/3, y + h/3, w/3, h/3, 0, 270);
        g2.setStroke(new BasicStroke(1f));
        // Ojos
        g2.setColor(Color.WHITE);
        g2.fillOval(x + w/3, y + h/6, w/6, w/6);
        g2.fillOval(x + w/2 + 2, y + h/6, w/6, w/6);
        g2.setColor(Color.BLACK);
        g2.fillOval(x + w/3 + 2, y + h/6 + 2, w/12, w/12);
        g2.fillOval(x + w/2 + 4, y + h/6 + 2, w/12, w/12);
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        int currentY = y;
        FontMetrics fm = g2.getFontMetrics();

        for (String word : words) {
            String testLine = sb.toString() + (sb.length() > 0 ? " " : "") + word;
            if (fm.stringWidth(testLine) > maxWidth) {
                g2.drawString(sb.toString(), x, currentY);
                sb = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                sb.append(sb.length() > 0 ? " " : "").append(word);
            }
        }
        if (sb.length() > 0) {
            g2.drawString(sb.toString(), x, currentY);
        }
    }

    private void drawInternalDialog(Graphics2D g2, int w, int h, String text) {
        int boxW = 500;
        int boxH = 100;
        int boxX = (w - boxW) / 2;
        int boxY = h - boxH - 25;

        // Fondo oscuro semi-transparente y redondeado
        g2.setColor(new Color(20, 20, 20, 220));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Borde fino gris claro
        g2.setColor(new Color(180, 180, 180, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g2.setStroke(new BasicStroke(1f));

        // Título del diálogo: "Pensamiento" en azul suave Windows XP
        g2.setColor(new Color(80, 160, 240));
        g2.setFont(new Font("Tahoma", Font.BOLD, 11));
        g2.drawString("Pensamiento", boxX + 20, boxY + 22);

        // Texto en Tahoma
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        g2.drawString(text, boxX + 20, boxY + 52);

        // Hint de avance
        g2.setFont(new Font("Tahoma", Font.PLAIN, 10));
        g2.setColor(Color.LIGHT_GRAY);
        String hint = "[ESPACIO o CLICK] Continuar";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, boxX + boxW - fm.stringWidth(hint) - 20, boxY + boxH - 12);
    }

    private void drawClippyControlScene(Graphics2D g2, int w, int h) {
        int cx = w - CLIPPY_W - 20;
        // Clippy flota sobre el diálogo del sistema para evitar solapamientos
        int cy = h - CLIPPY_H - 220; 

        // Mantener la caja de diálogo del sistema visible
        drawSystemDialogBox(g2, w, h, "Sistema", "Clippy tomó el control del sistema.", "alerta");

        // Dibujar a Clippy
        if (clippySprite != null) {
            g2.drawImage(clippySprite, cx, cy, CLIPPY_W, CLIPPY_H, null);
        } else {
            drawFallbackClippy(g2, cx, cy, CLIPPY_W, CLIPPY_H);
        }

        // Dibujar el cursor de mouse virtual
        if (showVirtualMouse) {
            drawVirtualCursor(g2, virtualMouseX, virtualMouseY);
        }
    }

    private void drawVirtualCursor(Graphics2D g2, float fx, float fy) {
        int x = Math.round(fx);
        int y = Math.round(fy);
        int[] cursorX = { x, x, x + 4, x + 8, x + 11, x + 7, x + 12 };
        int[] cursorY = { y, y + 17, y + 13, y + 21, y + 19, y + 12, y + 12 };

        g2.setColor(Color.WHITE);
        g2.fillPolygon(cursorX, cursorY, 7);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(cursorX, cursorY, 7);
        g2.setStroke(new BasicStroke(1f));
    }

    private void startClippyControlAnimation() {
        scene = Scene.CLIPPY_CONTROL;
        clippyControlStep = 0;
        virtualMouseX = getWidth() / 2f;
        virtualMouseY = getHeight() / 2f;
        showVirtualMouse = false;

        clippyAnimationTimer = new Timer(30, new ActionListener() {
            private int frameCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (clippyControlStep == 0) {
                    // Esperar a que el usuario avance manualmente
                    return;
                }
                frameCount++;
                if (clippyControlStep == 1) {
                    // Mover mouse virtual
                    float targetX = saveButtonRect.x + saveButtonRect.width / 2f;
                    float targetY = saveButtonRect.y + saveButtonRect.height / 2f;
                    float dx = targetX - virtualMouseX;
                    float dy = targetY - virtualMouseY;
                    float dist = (float) Math.hypot(dx, dy);

                    if (dist < 4) {
                        virtualMouseX = targetX;
                        virtualMouseY = targetY;
                        clippyControlStep = 2;
                        frameCount = 0;
                    } else {
                        float speed = 8f;
                        virtualMouseX += (dx / dist) * speed;
                        virtualMouseY += (dy / dist) * speed;
                    }
                } else if (clippyControlStep == 2) {
                    if (frameCount > 10) {
                        clippyAnimationTimer.stop();
                        showVirtualMouse = false;

                        // Mostrar tercer error
                        showXpErrorDialog();
                        saveAttempts++;

                        // Pasar al diálogo RPG de Clippy preocupado
                        scene = Scene.RPG_DIALOG;
                        rpgLineIndex = 0;
                    }
                }
                repaint();
            }
        });
        clippyAnimationTimer.start();
    }

    // ─── Diálogo de error estilo Windows XP ──────────────────────────────
    private void showXpErrorDialog() {
        String[] titles = {
            "Microsoft Word",
            "Microsoft Word",
            "Microsoft Word — Error crítico"
        };
        String[][] msgs = {
            {
                "No se puede guardar el archivo.",
                "Es posible que otro proceso esté usando",
                "el archivo o que no tenga permisos.",
                "Código de error: 0x80070005 — Acceso denegado."
            },
            {
                "Se produjo un error al escribir en el disco.",
                "Compruebe que el disco no esté lleno",
                "y vuelva a intentarlo.",
                "Código de error: 0xC0000034"
            },
            {
                "Proceso desconocido detectado.",
                "Un proceso externo está interfiriendo con",
                "el guardado de este documento.",
                "Código de error: 0x000000FF — UNKNOWN_PROCESS"
            }
        };

        int idx = Math.min(saveAttempts, 2);
        showXpModal(titles[idx], msgs[idx], idx);
    }

    private void showXpModal(String title, String[] msgLines, int attemptIndex) {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);

        int dlgW = 400, dlgH = 175;

        JPanel content = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                BufferedImage currentBg = (attemptIndex >= 0 && attemptIndex < 3) ? wordErrorBgs[attemptIndex] : null;
                if (currentBg != null) {
                    // Dibujar la imagen del diálogo (que ya contiene los textos y marcos integrados)
                    g2.drawImage(currentBg, 0, 0, dlgW, dlgH, null);
                } else {
                    // Fallback: Dibujar programáticamente si no existe la imagen
                    // Fondo
                    g2.setColor(XP_DIALOG_BG);
                    g2.fillRect(0, 0, dlgW, dlgH);
                    g2.setColor(XP_DIALOG_BDR);
                    g2.drawRect(0, 0, dlgW - 1, dlgH - 1);

                    // Barra de título
                    GradientPaint gp = new GradientPaint(
                            0, 0, XP_TITLE_TOP,
                            0, 28, XP_TITLE_BOT);
                    g2.setPaint(gp);
                    g2.fillRect(1, 1, dlgW - 2, 27);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 12));
                    g2.drawString(title, 28, 18);

                    // Ícono de ventana pequeño (cuadrado azul claro)
                    g2.setColor(new Color(80, 160, 240));
                    g2.fillRect(8, 7, 14, 14);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 10));
                    g2.drawString("W", 11, 18);

                    // Botón X
                    g2.setColor(new Color(200, 50, 40));
                    g2.fillRect(dlgW - 22, 5, 16, 16);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 11));
                    g2.drawString("✕", dlgW - 19, 17);

                    // Ícono de error (círculo rojo con X)
                    g2.setColor(XP_ERROR_ICON);
                    g2.fillOval(14, 38, 36, 36);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 22));
                    g2.drawString("✕", 20, 63);

                    // Línea separadora
                    g2.setColor(new Color(210, 208, 196));
                    g2.drawLine(0, dlgH - 40, dlgW, dlgH - 40);

                    // Texto del mensaje
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    int ty = 50;
                    for (String line : msgLines) {
                        g2.drawString(line, 62, ty);
                        ty += 18;
                    }
                }

                g2.dispose();
            }
        };
        content.setPreferredSize(new Dimension(dlgW, dlgH));

        // Botón Aceptar estilo XP
        JButton okBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean hover = getModel().isRollover();
                GradientPaint gp = hover
                        ? new GradientPaint(0, 0, new Color(200, 225, 255),
                                0, getHeight(), new Color(140, 185, 245))
                        : new GradientPaint(0, 0, Color.WHITE,
                                0, getHeight(), new Color(220, 218, 200));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.setColor(hover ? new Color(0, 80, 200) : new Color(0, 60, 116));
                g2.setStroke(new BasicStroke(hover ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 3, 3);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("Aceptar",
                        (getWidth() - fm.stringWidth("Aceptar")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        okBtn.setBounds(dlgW / 2 - 40, dlgH - 32, 80, 24);
        okBtn.setBorderPainted(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> dialog.dispose());
        content.add(okBtn);

        dialog.add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ─── Fallback Word (sin imagen) ───────────────────────────────────────
    private void drawFallbackWord(Graphics2D g2, int w, int h) {
        // Fondo gris de Word
        g2.setColor(new Color(180, 180, 180));
        g2.fillRect(0, 0, w, h);

        // Barra de título
        GradientPaint tp = new GradientPaint(0, 0, XP_TITLE_TOP, 0, 26, XP_TITLE_BOT);
        g2.setPaint(tp);
        g2.fillRect(0, 0, w, 26);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 12));
        g2.drawString("Documento1 - Microsoft Word", 30, 17);

        // Menú bar
        g2.setColor(new Color(236, 233, 216));
        g2.fillRect(0, 26, w, 22);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        String[] menus = {"Archivo", "Editar", "Ver", "Insertar", "Formato", "Herramientas", "Tabla", "Ventana", "?"};
        int mx = 6;
        for (String m : menus) {
            g2.drawString(m, mx, 42);
            mx += g2.getFontMetrics().stringWidth(m) + 10;
        }

        // Toolbar
        g2.setColor(new Color(236, 233, 216));
        g2.fillRect(0, 48, w, 26);
        // Botón disquete
        g2.setColor(new Color(0, 80, 160));
        g2.fillRect(saveButtonRect.x + 2, saveButtonRect.y + 2, 14, 10);
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(saveButtonRect.x + 2, saveButtonRect.y + 12, 14, 5);

        // Hoja de documento
        g2.setColor(Color.WHITE);
        g2.fillRect(80, 80, w - 160, h - 120);
        g2.setColor(new Color(180, 180, 180));
        g2.drawRect(80, 80, w - 160, h - 120);

        // Texto de prueba
        g2.setColor(new Color(40, 40, 40));
        g2.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        String[] lorem = {
            "Querida abuela:",
            "",
            "Hoy encontré esta computadora en el cuarto de papá.",
            "Está exactamente igual a como él la dejó.",
            "Quería dejar estas palabras aquí, como una cápsula del tiempo.",
            "",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            "Sed do eiusmod tempor incididunt ut labore et dolore magna.",
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco.",
            "Duis aute irure dolor in reprehenderit in voluptate velit."
        };
        int ly = 110;
        for (String l : lorem) {
            g2.drawString(l, 100, ly);
            ly += 22;
        }
    }

    // ─── Lógica ───────────────────────────────────────────────────────────
    private void handleSaveAttempt() {
        if (scene != Scene.WORD_IDLE) return;
        
        if (saveAttempts == 0) {
            showXpErrorDialog();
            scene = Scene.INTERNAL_DIALOG;
            internalDialogueText = "¿...?";
            saveAttempts++;
            repaint();
        } else if (saveAttempts == 1) {
            showXpErrorDialog();
            scene = Scene.INTERNAL_DIALOG;
            internalDialogueText = "¿Qué raro, no?";
            saveAttempts++;
            repaint();
        } else if (saveAttempts == 2) {
            scene = Scene.CLIPPY_BUBBLE;
            clippyPageIndex = 0;
            repaint();
        }
    }

    /** Salta toda la cinemática e inicia el juego directamente. */
    private void skipAll() {
        if (clippyAnimationTimer != null && clippyAnimationTimer.isRunning()) {
            clippyAnimationTimer.stop();
        }
        scene = Scene.DONE;
        if (onFinished != null) onFinished.run();
    }

    private void advance() {
        switch (scene) {
            case CLIPPY_BUBBLE -> {
                clippyPageIndex++;
                if (clippyPageIndex >= clippyBubblePages.length) {
                    startClippyControlAnimation();
                }
                repaint();
            }
            case RPG_DIALOG -> {
                rpgLineIndex++;
                if (rpgLineIndex >= rpgLines.length) {
                    scene = Scene.DONE;
                    if (onFinished != null) onFinished.run();
                } else {
                    repaint();
                }
            }
            default -> {}
        }
    }

    // ─── Eventos ─────────────────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            skipAll();
            return;
        }
        if (scene == Scene.WORD_IDLE) {
            if ((code == KeyEvent.VK_S && e.isControlDown()) || code == KeyEvent.VK_F5) {
                handleSaveAttempt();
            }
        } else if (scene == Scene.INTERNAL_DIALOG) {
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                scene = Scene.WORD_IDLE;
                repaint();
            }
        } else if (scene == Scene.CLIPPY_CONTROL) {
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                if (clippyControlStep == 0) {
                    clippyControlStep = 1;
                    showVirtualMouse = true;
                    repaint();
                }
            }
        } else {
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                advance();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (skipButtonRect.contains(e.getPoint())) {
            skipAll();
            return;
        }
        if (scene == Scene.WORD_IDLE) {
            if (saveButtonRect.contains(e.getPoint())) {
                handleSaveAttempt();
            }
        } else if (scene == Scene.INTERNAL_DIALOG) {
            scene = Scene.WORD_IDLE;
            repaint();
        } else if (scene == Scene.CLIPPY_CONTROL) {
            if (clippyControlStep == 0) {
                clippyControlStep = 1;
                showVirtualMouse = true;
                repaint();
            }
        } else {
            advance();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean nowSkip = skipButtonRect.contains(e.getPoint());
        if (nowSkip != skipHover) {
            skipHover = nowSkip;
            repaint();
        }
        if (scene == Scene.WORD_IDLE) {
            boolean nowHover = saveButtonRect.contains(e.getPoint());
            if (nowHover != saveHover) {
                saveHover = nowHover;
                setCursor(Cursor.getPredefinedCursor(
                        saveHover ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                repaint();
            }
        } else if (scene == Scene.RPG_DIALOG) {
            boolean nowOkHover = (okButtonRect != null && okButtonRect.contains(e.getPoint()));
            if (nowOkHover != dialogOkHover) {
                dialogOkHover = nowOkHover;
                setCursor(Cursor.getPredefinedCursor(
                        dialogOkHover ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                repaint();
            }
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) { 
        saveHover = false; 
        dialogOkHover = false; 
    }
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
