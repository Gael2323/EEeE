package com.miJuego.demo;

import com.miJuego.model.ProgresoJuego;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Panel principal del escritorio hub estilo Windows XP.
 */
public class DesktopHubPanel extends JPanel {

    // ── Dimensiones virtuales para el layout ──────────────────────────────────
    private static final int VIRTUAL_W  = 1280;
    private static final int VIRTUAL_H  = 800;
    private static final int TASKBAR_H  = 44;

    // ── Íconos del escritorio ─────────────────────────────────────────────────
    private final List<DesktopIcon> icons = new ArrayList<>();

    // ── Animación de Clippy ───────────────────────────────────────────────────
    public enum AnimPhase { NONE, CLIPPY_MOVING, LOCK_BREAK, FINISHED }

    private AnimPhase animPhase    = AnimPhase.NONE;
    
    // Posición del Clippy Corrupto
    private float clippyAnimX      = -1f;
    private float clippyAnimY      = -1f;
    private float lockBreakAlpha   = 1f;

    // Posición del Clippy Normal
    private float clippyNormalX    = -1f;
    private float clippyNormalY    = -1f;
    
    // Opacidad de entrada a la papelera
    private float clippyCorruptAlpha = 1f;
    
    private final List<Point> corruptionTrail = new ArrayList<>();
    private int trailStep = 0;
    
    // Assets de animación
    private Image clippyCorruptoImg = null;
    private Image clippyNormalImg = null;

    // ── Sistema de Diálogos ───────────────────────────────────────────────────
    private String dialogSpeaker = null;
    private String dialogText = null;
    private boolean dialogIsCorrupt = false;
    private Runnable onDialogAdvance = null;

    // ── Tooltip y reloj ───────────────────────────────────────────────────────
    private String tooltipText = null;
    private int tooltipX = 0, tooltipY = 0;
    private String clockText = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

    // ── Callbacks ─────────────────────────────────────────────────────────────
    private Runnable onWordAction       = null;
    private Runnable onRecycleBinAction = null;
    private Runnable onMyComputerAction = null;
    private Runnable onWizardAction     = null;
    private Runnable onTestAction       = null;

    // ── Assets Cargados ───────────────────────────────────────────────────────
    private Image bgImage;
    private Image lockImage;
    private Image lockBrokenImg1, lockBrokenImg2, lockBrokenImg3;
    private final Map<DesktopIcon.IconType, Image> iconAssets = new HashMap<>();

    // ── Textura de fallback (semilla fija) ────────────────────────────────────
    private static final int[] GRASS_TX, GRASS_TY;
    private static final int[] GLITCH_DX, GLITCH_DY;
    static {
        Random rng = new Random(0xDEADBEEFL);
        GRASS_TX = new int[800]; GRASS_TY = new int[800];
        for (int i = 0; i < 800; i++) {
            GRASS_TX[i] = rng.nextInt(VIRTUAL_W);
            GRASS_TY[i] = rng.nextInt(160);
        }
        Random rng2 = new Random(0xCAFEBABEL);
        GLITCH_DX = new int[40]; GLITCH_DY = new int[40];
        for (int i = 0; i < 40; i++) {
            GLITCH_DX[i] = rng2.nextInt(24) - 8;
            GLITCH_DY[i] = rng2.nextInt(12) - 6;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    public DesktopHubPanel() {
        setPreferredSize(new Dimension(1280, 800));
        setLayout(null);
        setOpaque(true);
        setFocusable(true); // Para recibir KeyEvents
        loadAssets();
        buildIcons();
        wireListeners();
        startClock();
    }

    // ── Carga de Assets ──────────────────────────────────────────────────────

    private void loadAssets() {
        clippyCorruptoImg = loadImage("assets/word/clippy_completamenteCorrupto_mirandoderecha..png");
        clippyNormalImg   = loadImage("assets/word/clippy_hablando.png");
        
        bgImage           = loadImage("assets/hub/desktop_xp_pixel_bg.png");
        lockImage         = loadImage("assets/hub/icons/candadoCerrado.png");
        lockBrokenImg1    = loadImage("assets/hub/icons/candadoCerrado.png");
        lockBrokenImg2    = loadImage("assets/hub/icons/candadoLigeramenteAbierto.png");
        lockBrokenImg3    = loadImage("assets/hub/icons/CandadoTotalmenteAbierto.png");

        iconAssets.put(DesktopIcon.IconType.WORD,             loadImage("assets/hub/icons/word.png"));
        iconAssets.put(DesktopIcon.IconType.RECYCLE_BIN,      loadImage("assets/hub/icons/recycle_bin.png"));
        iconAssets.put(DesktopIcon.IconType.SOLITAIRE,        loadImage("assets/hub/icons/solitaire.png"));
        iconAssets.put(DesktopIcon.IconType.EXPLORER,         loadImage("assets/hub/icons/explorer.png"));
        iconAssets.put(DesktopIcon.IconType.TERMINAL,         loadImage("assets/hub/icons/terminal.png"));
        iconAssets.put(DesktopIcon.IconType.GALLERY,          loadImage("assets/hub/icons/gallery.png"));
        iconAssets.put(DesktopIcon.IconType.MY_COMPUTER,      loadImage("assets/hub/icons/my_computer.png"));
        iconAssets.put(DesktopIcon.IconType.WIZARD_CHRONICLE, loadImage("assets/hub/icons/wizard_chronicle.png"));
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(path);
            if (url != null) return javax.imageio.ImageIO.read(url);
        } catch (Exception ignored) {}
        return null;
    }

    // ── Configuración de callbacks ───────────────────────────────────────────

    public void setOnWordAction(Runnable r)       { this.onWordAction = r; }
    public void setOnRecycleBinAction(Runnable r) { this.onRecycleBinAction = r; }
    public void setOnMyComputerAction(Runnable r) { this.onMyComputerAction = r; }
    public void setOnWizardAction(Runnable r)     { this.onWizardAction = r; }
    public void setOnTestAction(Runnable r)       { this.onTestAction = r; }

    // ── Controles de Animación y Diálogo ─────────────────────────────────────

    public void setAnimPhase(AnimPhase phase) { this.animPhase = phase; repaint(); }
    public AnimPhase getAnimPhase()           { return animPhase; }

    public void setClippyPosition(float relX, float relY) {
        this.clippyAnimX = relX;
        this.clippyAnimY = relY;
        trailStep++;
        if (trailStep % 3 == 0) {
            int cx = Math.round(relX * getWidth());
            int cy = Math.round(relY * (getHeight() - TASKBAR_H));
            corruptionTrail.add(new Point(cx, cy));
            if (corruptionTrail.size() > 60) corruptionTrail.remove(0); // Rastro más largo
        }
        repaint();
    }
    
    public void setClippyCorruptAlpha(float alpha) {
        this.clippyCorruptAlpha = alpha;
        repaint();
    }

    public void setClippyNormalPosition(float relX, float relY) {
        this.clippyNormalX = relX;
        this.clippyNormalY = relY;
        repaint();
    }

    public void showDialog(String speaker, String text, boolean isCorrupt, Runnable onAdvance) {
        this.dialogSpeaker = speaker;
        this.dialogText = text;
        this.dialogIsCorrupt = isCorrupt;
        this.onDialogAdvance = onAdvance;
        this.dialogChoices = null;
        this.onChoiceSelected = null;
        this.choiceBounds = null;
        repaint();
    }

    private String[] dialogChoices = null;
    private java.util.function.Consumer<Integer> onChoiceSelected = null;
    private Rectangle[] choiceBounds = null;
    private int hoveredChoice = -1;

    public void showChoiceDialog(String speaker, String text, boolean isCorrupt, String[] choices, java.util.function.Consumer<Integer> onSelected) {
        this.dialogSpeaker = speaker;
        this.dialogText = text;
        this.dialogIsCorrupt = isCorrupt;
        this.dialogChoices = choices;
        this.onChoiceSelected = onSelected;
        this.onDialogAdvance = null;
        this.choiceBounds = new Rectangle[choices.length];
        this.hoveredChoice = -1;
        repaint();
    }

    public void closeDialog() {
        this.dialogText = null;
        this.onDialogAdvance = null;
        this.dialogChoices = null;
        this.onChoiceSelected = null;
        this.choiceBounds = null;
        repaint();
    }

    public void setLockBreakAlpha(float alpha) { this.lockBreakAlpha = alpha; repaint(); }

    public void unlockRecycleBinAndWizard() {
        for (DesktopIcon icon : icons) {
            if (icon.type == DesktopIcon.IconType.RECYCLE_BIN) icon.locked = false;
            if (icon.type == DesktopIcon.IconType.WIZARD_CHRONICLE) icon.locked = false;
        }
        corruptionTrail.clear();
        clippyAnimX = -1f;
        clippyAnimY = -1f;
        repaint();
    }

    public Point getWordIconCenter() {
        for (DesktopIcon icon : icons) {
            if (icon.type == DesktopIcon.IconType.WORD)
                return new Point(icon.x + DesktopIcon.ICON_SIZE / 2, icon.y + DesktopIcon.ICON_SIZE / 2);
        }
        return new Point(20 + 24, 100 + 24);
    }

    public Point getRecycleBinIconCenter() {
        for (DesktopIcon icon : icons) {
            if (icon.type == DesktopIcon.IconType.RECYCLE_BIN)
                return new Point(icon.x + DesktopIcon.ICON_SIZE / 2, icon.y + DesktopIcon.ICON_SIZE / 2);
        }
        return new Point(20 + 24, 660 + 24);
    }

    // ── Construcción de íconos y layout ──────────────────────────────────────

    private void buildIcons() {
        int startX = 20;
        int startY = 20;
        int gapY   = 80; // Separación vertical para íconos de 48px

        icons.add(new DesktopIcon("Mi PC", DesktopIcon.IconType.MY_COMPUTER, startX, startY,
                false, // Mi PC siempre desbloqueado
                () -> { if (onMyComputerAction != null) onMyComputerAction.run(); else showMyComputerPlaceholder(); },
                iconAssets.get(DesktopIcon.IconType.MY_COMPUTER), lockImage));

        icons.add(new DesktopIcon("Word", DesktopIcon.IconType.WORD, startX, startY + gapY * 1,
                !ProgresoJuego.wordUnlocked,
                () -> { if (onWordAction != null) onWordAction.run(); },
                iconAssets.get(DesktopIcon.IconType.WORD), lockImage));

        icons.add(new DesktopIcon("Explorador", DesktopIcon.IconType.EXPLORER, startX, startY + gapY * 2,
                !ProgresoJuego.explorerUnlocked,
                () -> showLockedMessage("Explorador"),
                iconAssets.get(DesktopIcon.IconType.EXPLORER), lockImage));

        icons.add(new DesktopIcon("Galería", DesktopIcon.IconType.GALLERY, startX, startY + gapY * 3,
                !ProgresoJuego.galleryUnlocked,
                () -> showLockedMessage("Galería"),
                iconAssets.get(DesktopIcon.IconType.GALLERY), lockImage));

        icons.add(new DesktopIcon("Terminal", DesktopIcon.IconType.TERMINAL, startX, startY + gapY * 4,
                !ProgresoJuego.terminalUnlocked,
                () -> showLockedMessage("Terminal"),
                iconAssets.get(DesktopIcon.IconType.TERMINAL), lockImage));

        icons.add(new DesktopIcon("Solitario", DesktopIcon.IconType.SOLITAIRE, startX, startY + gapY * 5,
                !ProgresoJuego.solitaireUnlocked,
                () -> showLockedMessage("Solitario"),
                iconAssets.get(DesktopIcon.IconType.SOLITAIRE), lockImage));

        icons.add(new DesktopIcon("Wizard Chronicles", DesktopIcon.IconType.WIZARD_CHRONICLE, startX, startY + gapY * 6,
                !ProgresoJuego.wizardUnlocked,
                () -> { if (onWizardAction != null) onWizardAction.run(); else showLockedMessage("Wizard Chronicles"); },
                iconAssets.get(DesktopIcon.IconType.WIZARD_CHRONICLE), lockImage));

        icons.add(new DesktopIcon("Papelera", DesktopIcon.IconType.RECYCLE_BIN, startX, startY + gapY * 8,
                !ProgresoJuego.recycleBinUnlocked,
                () -> { if (onRecycleBinAction != null) onRecycleBinAction.run(); },
                iconAssets.get(DesktopIcon.IconType.RECYCLE_BIN), lockImage));

        // Nivel de prueba separado
        icons.add(new DesktopIcon("Nivel Prueba", DesktopIcon.IconType.TERMINAL, VIRTUAL_W - 100, startY,
                false,
                () -> { if (onTestAction != null) onTestAction.run(); },
                iconAssets.get(DesktopIcon.IconType.TERMINAL), null));
    }

    // ── Listeners ────────────────────────────────────────────────────────────

    private void wireListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (dialogChoices != null && onChoiceSelected != null) {
                    if (hoveredChoice >= 0 && hoveredChoice < dialogChoices.length) {
                        onChoiceSelected.accept(hoveredChoice);
                    }
                    return;
                }
                // Si hay diálogo activo sin opciones, el clic avanza el diálogo
                if (dialogText != null && onDialogAdvance != null) {
                    onDialogAdvance.run();
                    return;
                }
                
                // Ignorar clics durante animaciones
                if (animPhase != AnimPhase.FINISHED && animPhase != AnimPhase.NONE) return;
                
                Point p = e.getPoint();
                if (idleClippy && idleClippyBounds.contains(p)) {
                    String[] phrases = {
                        "¿Necesitas ayuda para organizar tus íconos?",
                        "Recuerda visitar el Wizard Chronicle para prepararte.",
                        "Esa papelera me da muy mala espina...",
                        "Mi función principal es asistir, pero siento que algo terrible se avecina."
                    };
                    String randomText = phrases[(int)(Math.random() * phrases.length)];
                    showDialog("Clippy", randomText, false, () -> {
                        closeDialog();
                    });
                    return;
                }

                for (DesktopIcon icon : icons) {
                    if (icon.getFullBounds().contains(p)) {
                        if (icon.locked) showLockedMessage(icon.name);
                        else             icon.onAction.run();
                        return;
                    }
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (dialogText != null && onDialogAdvance != null) {
                        onDialogAdvance.run();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (dialogText != null) {
                    if (choiceBounds != null) {
                        int oldHover = hoveredChoice;
                        hoveredChoice = -1;
                        for (int i = 0; i < choiceBounds.length; i++) {
                            if (choiceBounds[i] != null && choiceBounds[i].contains(e.getPoint())) {
                                hoveredChoice = i;
                                break;
                            }
                        }
                        if (oldHover != hoveredChoice) repaint();
                    }
                    return; // No tooltips durante diálogo
                }
                
                Point p = e.getPoint();
                boolean changed = false;
                String oldTooltip = tooltipText;
                tooltipText = null;
                
                for (DesktopIcon icon : icons) {
                    boolean was = icon.hovered;
                    icon.hovered = icon.getFullBounds().contains(p);
                    if (icon.hovered) {
                        tooltipX = p.x + 14;
                        tooltipY = p.y - 8;
                        tooltipText = icon.locked
                                ? icon.name + " [Bloqueado] - Completa el nivel anterior."
                                : icon.name + " - Click para entrar";
                    }
                    if (was != icon.hovered) changed = true;
                }
                
                if (idleClippy && idleClippyBounds.contains(p)) {
                    tooltipText = "Clippy - Click para hablar";
                    tooltipX = p.x + 14;
                    tooltipY = p.y - 8;
                }
                
                if (changed || (oldTooltip != null && tooltipText == null) || (oldTooltip == null && tooltipText != null)) {
                    repaint();
                }
            }
        });
    }

    private void startClock() {
        Timer t = new Timer(30_000, e -> {
            clockText = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            repaint();
        });
        t.start();
        
        // Timer rápido para la animación idle de Clippy (y parpadeos)
        Timer animT = new Timer(33, e -> { // ~30 FPS
            if (idleClippy || dialogText != null) {
                repaint();
            }
        });
        animT.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PINTURA NATIVA
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int screenW = getWidth();
        int screenH = getHeight();

        paintWallpaper(g2, screenW, screenH);
        paintIcons(g2);
        paintCorruptionTrail(g2);

        // Dibujar el candado rompiéndose encima del ícono si corresponde
        if (animPhase == AnimPhase.LOCK_BREAK) {
            paintLockBreak(g2);
        }

        // Dibujar Clippy Corrupto
        if (clippyAnimX >= 0) {
            paintClippyCorrupto(g2, screenW, screenH);
        }
        
        // Dibujar Clippy Normal
        if (clippyNormalX >= 0) {
            paintClippyNormal(g2, screenW, screenH);
        } else if (idleClippy) {
            paintIdleClippy(g2, screenW, screenH);
        }

        paintTaskbar(g2, screenW, screenH);

        if (tooltipText != null) paintScreenTooltip(g2);
        
        // Dibujar el diálogo RPG si está activo
        if (dialogText != null) paintDialogBubble(g2, screenW, screenH);
    }

    private void paintIdleClippy(Graphics2D g, int w, int h) {
        int sz = 128;
        int cx = w - 80;
        int cy = 60;
        idleClippyBounds = new Rectangle(cx - sz/2 - 10, cy - sz/2 - 10, sz + 20, sz + 20);
        
        // Recuadro oscuro para que no se pierda en el cielo
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(idleClippyBounds.x, idleClippyBounds.y, idleClippyBounds.width, idleClippyBounds.height);
        
        // Efecto hover sobre el recuadro
        Point mousePos = getMousePosition();
        boolean isHovered = (mousePos != null && idleClippyBounds.contains(mousePos));
        if (isHovered) {
            g.setColor(new Color(0x80A8FF));
            g.drawRect(idleClippyBounds.x, idleClippyBounds.y, idleClippyBounds.width - 1, idleClippyBounds.height - 1);
            tooltipText = "Clippy - Click para hablar";
            tooltipX = mousePos.x + 14;
            tooltipY = mousePos.y - 8;
        } else {
            g.setColor(new Color(255, 255, 255, 100));
            g.drawRect(idleClippyBounds.x, idleClippyBounds.y, idleClippyBounds.width - 1, idleClippyBounds.height - 1);
        }
        
        // Animación de baile (Bounce + Sway)
        long time = System.currentTimeMillis();
        int bounceY = (int) (Math.sin(time / 120.0) * 5); // Salto arriba y abajo
        double rotation = Math.sin(time / 150.0) * 0.15;  // Inclinación
        
        Graphics2D gDance = (Graphics2D) g.create();
        gDance.translate(cx, cy + bounceY);
        gDance.rotate(rotation);
        
        if (clippyNormalImg != null) {
            gDance.drawImage(clippyNormalImg, -sz/2, -sz/2, sz, sz, null);
        } else {
            gDance.setColor(new Color(0xC0C0C0)); gDance.fillRect(-14, -14, 28, 28);
        }
        gDance.dispose();
    }

    private void paintWallpaper(Graphics2D g, int w, int h) {
        int deskH = h - TASKBAR_H;
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, w, deskH, null);
            return;
        }
        g.setColor(new Color(0x3A9E30));
        g.fillRect(0, 0, w, deskH);
    }

    private void paintTaskbar(Graphics2D g, int w, int h) {
        int ty = h - TASKBAR_H;
        
        // 1. Fondo de la barra de tareas (Gradiente azul Luna)
        GradientPaint taskbarGrad = new GradientPaint(
            0, ty, new Color(0x245EDC), 
            0, h, new Color(0x0F38A0)
        );
        g.setPaint(taskbarGrad);
        g.fillRect(0, ty, w, TASKBAR_H);
        
        // Brillo superior azul
        g.setColor(new Color(0x3B79F2));
        g.fillRect(0, ty, w, 1);
        g.setColor(new Color(0x286AE2));
        g.fillRect(0, ty + 1, w, 2);
        
        // 2. Bandeja del sistema (System Tray) - Celeste con gradiente
        int trayW = 140;
        GradientPaint trayGrad = new GradientPaint(
            w - trayW, ty, new Color(0x139EEF), 
            w - trayW, h, new Color(0x0E68CE)
        );
        g.setPaint(trayGrad);
        g.fillRect(w - trayW, ty, trayW, TASKBAR_H);
        
        // Brillo superior de la bandeja
        g.setColor(new Color(0x26B5FF));
        g.fillRect(w - trayW, ty, trayW, 1);
        
        // Borde izquierdo curvo de la bandeja
        g.setColor(new Color(0x103A8A));
        g.drawLine(w - trayW - 1, ty, w - trayW - 1, h);
        
        // Texto de la hora
        g.setColor(Color.WHITE); 
        g.setFont(new Font("Tahoma", Font.PLAIN, 15));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(clockText, w - trayW + (trayW - fm.stringWidth(clockText)) / 2, ty + TASKBAR_H / 2 + 5);

        // 3. Botón Start (Verde con gradiente y brillos)
        int sbX = 0, sbY = ty, sbW = 120, sbH = TASKBAR_H;
        
        GradientPaint startGrad = new GradientPaint(
            sbX, sbY, new Color(0x3CB832), 
            sbX, sbY + sbH, new Color(0x1E8816)
        );
        g.setPaint(startGrad);
        
        // Forma de píldora (solo curvado a la derecha)
        g.fillRoundRect(sbX, sbY, sbW, sbH, 20, 20);
        g.fillRect(sbX, sbY, 10, sbH); // Rellenar la izquierda para que sea recta
        
        // Brillo superior del botón Start
        g.setColor(new Color(0x5AE54E));
        g.fillRoundRect(sbX + 2, sbY + 1, sbW - 6, 4, 16, 16);
        g.fillRect(sbX, sbY + 1, 10, 4);
        
        // Sombra inferior del botón Start
        g.setColor(new Color(0x145A0E));
        g.drawRoundRect(sbX, sbY, sbW - 1, sbH - 1, 20, 20);
        g.drawLine(sbX, sbY, sbX, sbY + sbH);

        // Texto Start (Cursiva y Bold)
        g.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 22));
        
        // Sombra del texto
        g.setColor(new Color(0, 0, 0, 140));
        g.drawString("start", sbX + 38, sbY + sbH / 2 + 8);
        
        // Texto principal
        g.setColor(Color.WHITE);
        g.drawString("start", sbX + 37, sbY + sbH / 2 + 7);
        
        // 4. Logo falso de Windows XP (4 cuadraditos de colores)
        int logoX = sbX + 12;
        int logoY = sbY + sbH / 2 - 8;
        
        // Rojo
        g.setColor(new Color(0xF24822));
        g.fillPolygon(new int[]{logoX, logoX + 6, logoX + 6, logoX}, new int[]{logoY, logoY + 1, logoY + 7, logoY + 6}, 4);
        // Verde
        g.setColor(new Color(0x56C215));
        g.fillPolygon(new int[]{logoX + 7, logoX + 13, logoX + 13, logoX + 7}, new int[]{logoY + 1, logoY, logoY + 6, logoY + 7}, 4);
        // Azul
        g.setColor(new Color(0x00A3F4));
        g.fillPolygon(new int[]{logoX, logoX + 6, logoX + 6, logoX}, new int[]{logoY + 7, logoY + 8, logoY + 14, logoY + 13}, 4);
        // Amarillo
        g.setColor(new Color(0xFFC70A));
        g.fillPolygon(new int[]{logoX + 7, logoX + 13, logoX + 13, logoX + 7}, new int[]{logoY + 8, logoY + 7, logoY + 13, logoY + 14}, 4);
    }

    private void paintIcons(Graphics2D g) {
        for (DesktopIcon icon : icons) {
            syncIconLockState(icon);
            icon.drawPixelArt(g, icon.x, icon.y, DesktopIcon.ICON_SIZE);
        }
    }

    private void syncIconLockState(DesktopIcon icon) {
        switch (icon.type) {
            case WORD        -> icon.locked = !ProgresoJuego.wordUnlocked;
            case RECYCLE_BIN -> icon.locked = !ProgresoJuego.recycleBinUnlocked;
            case SOLITAIRE   -> icon.locked = !ProgresoJuego.solitaireUnlocked;
            case EXPLORER    -> icon.locked = !ProgresoJuego.explorerUnlocked;
            case TERMINAL    -> icon.locked = !icon.name.equals("Nivel Prueba") && !ProgresoJuego.terminalUnlocked;
            case GALLERY     -> icon.locked = !ProgresoJuego.galleryUnlocked;
            case WIZARD_CHRONICLE -> icon.locked = !ProgresoJuego.wizardUnlocked;
            case MY_COMPUTER -> icon.locked = false;
        }
    }

    private void paintCorruptionTrail(Graphics2D g) {
        for (int i = 0; i < corruptionTrail.size(); i++) {
            Point pt = corruptionTrail.get(i);
            int idx = i % GLITCH_DX.length;
            g.setColor(new Color(0x7800B4));
            g.fillRect(pt.x - 4, pt.y - 4, 8, 8);
            g.fillRect(pt.x + GLITCH_DX[idx], pt.y + GLITCH_DY[idx], 6, 4);
            if (i % 4 == 0) {
                g.setColor(new Color(0x00CC44));
                g.fillRect(pt.x + GLITCH_DX[(idx + 5) % GLITCH_DX.length] + 8, pt.y + GLITCH_DY[(idx + 3) % GLITCH_DY.length], 12, 4);
            }
        }
    }

    private void paintClippyCorrupto(Graphics2D g, int w, int h) {
        int cx = Math.round(clippyAnimX * w);
        int cy = Math.round(clippyAnimY * (h - TASKBAR_H));
        int sz = 128;
        
        Graphics2D gFade = (Graphics2D) g.create();
        gFade.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clippyCorruptAlpha));

        if (clippyCorruptoImg != null) {
            gFade.drawImage(clippyCorruptoImg, cx - sz/2, cy - sz/2, sz, sz, null);
        } else {
            gFade.setColor(new Color(0x00CC00)); gFade.fillRect(cx - 14, cy - 14, 28, 28);
        }
        gFade.dispose();
    }

    private void paintClippyNormal(Graphics2D g, int w, int h) {
        int cx = Math.round(clippyNormalX * w);
        int cy = Math.round(clippyNormalY * (h - TASKBAR_H));
        int sz = 128;
        if (clippyNormalImg != null) {
            g.drawImage(clippyNormalImg, cx - sz/2, cy - sz/2, sz, sz, null);
        } else {
            g.setColor(new Color(0xC0C0C0)); g.fillRect(cx - 14, cy - 14, 28, 28);
        }
    }

    private void paintLockBreak(Graphics2D g) {
        for (DesktopIcon icon : icons) {
            if (icon.type != DesktopIcon.IconType.RECYCLE_BIN) continue;
            int cx = icon.x + DesktopIcon.ICON_SIZE / 2;
            int cy = icon.y + DesktopIcon.ICON_SIZE / 2;

            Image brokenImg = lockBrokenImg1;
            if (lockBreakAlpha < 0.66f) brokenImg = lockBrokenImg2;
            if (lockBreakAlpha < 0.33f) brokenImg = lockBrokenImg3;

            if (brokenImg != null) {
                int shakeX = (int) ((Math.random() - 0.5) * 6);
                int shakeY = (int) ((Math.random() - 0.5) * 6);
                g.drawImage(brokenImg, cx - DesktopIcon.ICON_SIZE/2 + shakeX, cy - DesktopIcon.ICON_SIZE/2 + shakeY, DesktopIcon.ICON_SIZE, DesktopIcon.ICON_SIZE, null);
            }
            break;
        }
    }

    private void paintScreenTooltip(Graphics2D g) {
        if (tooltipText == null) return;
        g.setFont(new Font("Tahoma", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(tooltipText) + 12, th = fm.getHeight() + 6;
        int tx = Math.min(tooltipX, getWidth() - tw - 4), ty = Math.max(tooltipY - th, 4);
        g.setColor(new Color(0xFFFFE1)); g.fillRect(tx, ty, tw, th);
        g.setColor(Color.BLACK); g.drawRect(tx, ty, tw - 1, th - 1);
        g.drawString(tooltipText, tx + 6, ty + fm.getAscent() + 3);
    }
    
    private boolean idleClippy = false;
    private Rectangle idleClippyBounds = new Rectangle(-1, -1, 0, 0);

    public void setIdleClippy(boolean idle) {
        this.idleClippy = idle;
        repaint();
    }

    private void paintDialogBubble(Graphics2D g, int w, int h) {
        int bubbleW = 500;
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        
        // Calcular líneas dinámicamente
        String[] words = dialogText.split(" ");
        List<String> lines = new ArrayList<>();
        String currentLine = "";
        for (String word : words) {
            if (fm.stringWidth(currentLine + word) > bubbleW - 40) {
                lines.add(currentLine);
                currentLine = word + " ";
            } else {
                currentLine += word + " ";
            }
        }
        lines.add(currentLine);
        
        int bubbleH = 70 + (lines.size() * 24); // Altura dinámica según la cantidad de texto
        if (dialogChoices != null) {
            bubbleH += 10 + (dialogChoices.length * 24);
        }
        int bx = (w - bubbleW) / 2;
        int by = h - TASKBAR_H - bubbleH - 40; // Un poco arriba de la barra de tareas

        // Desactivar anti-aliasing para texto y formas para lograr estética pixel art
        Object oldAA = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // Fondo y borde pixelado (fillRect/drawRect en vez de fillRoundRect)
        g.setColor(dialogIsCorrupt ? new Color(0x1A051A) : new Color(0xFFFFAA));
        g.fillRect(bx, by, bubbleW, bubbleH);
        
        g.setColor(dialogIsCorrupt ? new Color(0x00FF44) : new Color(0x333333));
        g.setStroke(new BasicStroke(4));
        g.drawRect(bx, by, bubbleW, bubbleH);
        
        // Bordes internos para dar un estilo "retro UI"
        g.setColor(dialogIsCorrupt ? new Color(0x008822) : new Color(0xAAAA66));
        g.setStroke(new BasicStroke(2));
        g.drawRect(bx + 4, by + 4, bubbleW - 8, bubbleH - 8);
        g.setStroke(new BasicStroke(1));

        // Speaker name (Fuente Monospaced para estética retro)
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(dialogIsCorrupt ? new Color(0x00FF44) : new Color(0x111111));
        g.drawString(dialogSpeaker, bx + 20, by + 30);

        // Text
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(dialogIsCorrupt ? Color.WHITE : Color.BLACK);
        
        int yOffset = by + 60;
        for (String lineText : lines) {
            g.drawString(lineText, bx + 20, yOffset);
            yOffset += 24;
        }

        if (dialogChoices != null) {
            yOffset += 10;
            for (int i = 0; i < dialogChoices.length; i++) {
                String choice = "> " + dialogChoices[i];
                int cw = fm.stringWidth(choice) + 20;
                choiceBounds[i] = new Rectangle(bx + 20, yOffset - 18, cw, 24);
                
                if (i == hoveredChoice) {
                    g.setColor(dialogIsCorrupt ? new Color(0x00FF44) : new Color(0x333333));
                    g.fillRect(choiceBounds[i].x, choiceBounds[i].y, choiceBounds[i].width, choiceBounds[i].height);
                    g.setColor(dialogIsCorrupt ? new Color(0x1A051A) : new Color(0xFFFFAA));
                } else {
                    g.setColor(dialogIsCorrupt ? Color.WHITE : Color.BLACK);
                }
                g.drawString(choice, bx + 25, yOffset);
                yOffset += 24;
            }
        }

        // Blinking indicator pixelado (cuadradito en vez de triángulo suave)
        if (dialogChoices == null && (System.currentTimeMillis() / 400) % 2 == 0) {
            g.setColor(dialogIsCorrupt ? new Color(0x00FF44) : new Color(0x333333));
            g.fillRect(bx + bubbleW - 24, by + bubbleH - 24, 12, 12);
        }

        // Restaurar anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAA != null ? oldAA : RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private void showLockedMessage(String iconName) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "<html><b>Acceso bloqueado</b><br><br>" + iconName + " no está disponible todavía.</html>",
                "Sistema", JOptionPane.WARNING_MESSAGE);
    }

    private void showMyComputerPlaceholder() {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "<html><b>Mi PC</b><br><br>Unidad C: está asegurada. No se han detectado amenazas críticas aquí.</html>",
                "Mi PC", JOptionPane.INFORMATION_MESSAGE);
    }
}
