package com.game2d.view;

import com.game2d.model.FrameSnapshot;
import com.game2d.model.SessionState;
import com.game2d.model.Drawable;
import com.miJuego.model.Juego;
import com.miJuego.model.Torre;
import com.miJuego.model.TowerDefenseSnapshot;
import com.miJuego.model.Enemigo;
import com.miJuego.model.TorreComun;
import com.miJuego.model.TorreDeArea;
import com.miJuego.model.TorreDeFuego;
import com.miJuego.model.TorreDeHielo;
import com.miJuego.model.TorreElectrica;
import com.miJuego.model.TorreFuerte;
import com.miJuego.model.TorreMcAfee;
import com.miJuego.model.TorreInternetExplorer;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista Swing enriquecida para el juego de Tower Defense con resolución de 1280x800.
 * Cuenta con barra de tareas de Windows XP en la parte inferior y barras laterales
 * interactivas para gestionar torres y oleadas en tiempo real.
 */
public final class SwingGameView implements GameView {

    private static final Dimension GAME_SIZE = new Dimension(900, 660); // Tamaño del lienzo de juego
    private static final int MENU_BAR_HEIGHT = 96;
    private static final Dimension FRAME_SIZE = new Dimension(1280, 800);

    // Colores del tema XP Luna
    private static final Color XP_BLUE_DARK = new Color(0, 84, 227);
    private static final Color XP_BLUE_LIGHT = new Color(50, 150, 255);
    private static final Color XP_BG_SIDEBAR = new Color(212, 228, 252);
    private static final Color XP_BORDER_BLUE = new Color(0, 60, 180);
    private static final Color XP_GREEN_DARK = new Color(40, 150, 40);
    private static final Color XP_GREEN_LIGHT = new Color(110, 210, 110);
    private static final Color XP_RED_DARK = new Color(180, 40, 40);
    private static final Color XP_RED_LIGHT = new Color(255, 130, 130);
    private static final Color XP_SILVER_LIGHT = new Color(245, 245, 245);
    private static final Color XP_SILVER_DARK = new Color(210, 215, 230);
    private static final Color XP_ORANGE_BORDER = new Color(242, 149, 54);
    private static final Color XP_ORANGE_HOVER = new Color(255, 210, 130);
    private static final Color XP_ORANGE_HOVER_END = new Color(255, 170, 70);
    private static final Font XP_FONT_TAHOMA_BOLD = new Font("Tahoma", Font.BOLD, 10);
    private static final Font XP_FONT_TAHOMA_PLAIN = new Font("Tahoma", Font.PLAIN, 10);

    private final JFrame frame;
    private final GamePanel gamePanel;
    private final MessageToastOverlay messageOverlay;
    private final NavBarPanel navBarPanel;
    private final MenuModal menuModal;
    private ViewListener listener;

    // Componentes de la Barra de Tareas XP (Abajo)
    private final JLabel startBtnLabel = new JLabel("  start") {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            
            // Gradiente verde Start XP Luna
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(80, 190, 80),
                0, h, new Color(40, 110, 40)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w + 10, h - 1, 12, 12);
            
            g2.setColor(new Color(25, 80, 25));
            g2.drawRoundRect(0, 0, w + 9, h - 1, 12, 12);
            
            g2.setColor(new Color(255, 255, 255, 100));
            g2.drawLine(2, 2, w - 2, 2);
            
            g2.dispose();
            super.paintComponent(g);
        }
    };
    private final JProgressBar xpProgressBar = new XPProgressBar();
    private final JLabel clockLabel = new JLabel();

    // Componentes de la Barra Lateral Izquierda
    private final JPanel leftSidebar = new JPanel();
    private final JPanel[] shopTowerCells = new JPanel[8];
    private final JLabel waveLabel = new JLabel("Oleada: —");
    private final JLabel waveEnemiesLabel = new JLabel("Enemigos: —");
    private final JProgressBar waveProgressBar = new XPProgressBar();
    private final JButton startWaveBtn = new XPButton("▶ Iniciar Oleada", XP_GREEN_LIGHT, XP_GREEN_DARK, new Color(130, 230, 130), new Color(60, 180, 60), Color.WHITE);

    // Componentes de la Barra Lateral Derecha
    private final JPanel rightSidebar = new JPanel();
    private final JLabel detailNameLabel = new JLabel("Ninguna seleccionada", JLabel.CENTER);
    private final JLabel detailIconLabel = new JLabel();
    private final JLabel detailStatLabel = new JLabel("<html><center>Haz click en una torre construida<br>para ver su información.</center></html>", JLabel.CENTER);
    private final JButton upgradeBtn = new XPButton("Mejorar (—)", XP_GREEN_LIGHT, XP_GREEN_DARK, new Color(130, 230, 130), new Color(60, 180, 60), Color.WHITE);
    private final JButton sellBtn = new XPButton("Vender (—)", XP_RED_LIGHT, XP_RED_DARK, new Color(255, 160, 160), new Color(210, 60, 60), Color.WHITE);
    private final JButton avastModeBtn = new XPButton("Modo: Área", XP_BLUE_LIGHT, XP_BLUE_DARK, new Color(160, 200, 255), new Color(60, 130, 210), Color.WHITE);
    private final JButton avastTargetBtn = new XPButton("Fijar Blanco", XP_BLUE_LIGHT, XP_BLUE_DARK, new Color(160, 200, 255), new Color(60, 130, 210), Color.WHITE);
    private final JPanel avastBtnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
    private final MinimapPanel minimapPanel = new MinimapPanel();
    
    // Panel de Notificaciones (Log de eventos)
    private final JPanel notifListPanel = new JPanel();
    private final List<String> notificationsList = new ArrayList<>();
    private JPanel taskbar;
    private final java.util.Map<String, ImageIcon> largeIconCache = new java.util.HashMap<>();

    public SwingGameView() {
        this(ImageResolver.createDefault(), BackgroundSettings.getInstance());
    }

    public SwingGameView(ImageResolver imageResolver, BackgroundSettings background) {
        frame = new JFrame("Document1 - Tower Defense [Nivel 1: El Documento]");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.setMinimumSize(FRAME_SIZE);
        frame.setResizable(true);

        // ── 1. HUD Superior (Cabecera de Word) ──────────────────────
        navBarPanel = new NavBarPanel();
        navBarPanel.setPreferredSize(new Dimension(FRAME_SIZE.width, MENU_BAR_HEIGHT));

        // ── 2. Lienzo Central (El mapa) ─────────────────────────────
        // Sin preferred/minimum size fijo: BorderLayout.CENTER lo estira al espacio disponible
        gamePanel = new GamePanel(imageResolver, background);
        gamePanel.setMinimumSize(new Dimension(400, 300));

        messageOverlay = new MessageToastOverlay(gamePanel);
        menuModal = new MenuModal(frame);

        // ── 3. Construir Barras Laterales y Barra de Tareas ──────────
        buildLeftSidebar();
        buildRightSidebar();
        taskbar = buildTaskbar();

        // ── 4. Ensamblar en el Frame Principal ───────────────────────
        frame.add(navBarPanel, BorderLayout.NORTH);
        frame.add(leftSidebar, BorderLayout.WEST);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(rightSidebar, BorderLayout.EAST);
        frame.add(taskbar, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);

        wireInput();
        startClock();
    }

    // ── BARRA LATERAL IZQUIERDA: Tienda de Torres y Oleada ────────────────────
    private void buildLeftSidebar() {
        leftSidebar.setLayout(new BoxLayout(leftSidebar, BoxLayout.Y_AXIS));
        leftSidebar.setPreferredSize(new Dimension(185, 0));
        leftSidebar.setBackground(XP_BG_SIDEBAR);
        leftSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(160, 180, 210)));

        // --- MÓDULO 1: Tienda de Torres ---
        XPWindow shopWin = new XPWindow("Tienda de Torres", getDocIcon());
        shopWin.setPreferredSize(new Dimension(175, 450));
        shopWin.setMaximumSize(new Dimension(175, 450));
        shopWin.setMinimumSize(new Dimension(175, 450));
        shopWin.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel shopContent = shopWin.getContentPanel();
        shopContent.setLayout(new GridLayout(8, 1, 0, 4));
        shopContent.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        String[] towerNamesMap = { "", "Común", "McAfee", "Área", "Avast", "Fuerte", "Firefox", "I.Explorer", "Eléctrica" };
        int[] towerCostsMap = { 0, 100, 80, 150, 200, 250, 180, 175, 220 };
        String[] towerStatsMap = { "",
            "D: 15 / R: 3.5 / CD: 0.5s",
            "D: 17.2 / R: 3.5 / CD: 0.5s",
            "D: 10 / R: 2.5 / CD: 1.5s",
            "D: 15 / R: 4.0 / CD: 1.0s",
            "D: 35 / R: 3.0 / CD: 2.0s",
            "D: 5 / R: 2.0 / CD: 0.2s",
            "D: 2 / R: 2.5 / CD: 1.0s",
            "D: 12 / R: 4.0 / CD: 0.8s"
        };

        for (int i = 0; i < 8; i++) {
            int tType = com.miJuego.model.ProgresoJuego.getTowerTypeForSlot(i);

            // Celda con gradiente XP y hover effect
            JPanel cell = new JPanel(new BorderLayout(4, 0)) {
                private boolean hovered = false;
                { putClientProperty("hovered", false); }
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(); int h = getHeight();
                    Boolean locked = (Boolean) getClientProperty("locked");
                    Boolean selected = (Boolean) getClientProperty("selected");
                    Boolean hvr = (Boolean) getClientProperty("hovered");
                    if (Boolean.TRUE.equals(locked)) {
                        // Fondo gris para celdas bloqueadas
                        g2.setColor(new Color(225, 225, 225));
                        g2.fillRect(0, 0, w, h);
                    } else if (Boolean.TRUE.equals(selected)) {
                        // Naranja seleccionado
                        GradientPaint gp = new GradientPaint(0, 0, new Color(255, 220, 140), 0, h, new Color(248, 170, 70));
                        g2.setPaint(gp); g2.fillRect(0, 0, w, h);
                        // Brillo superior
                        g2.setColor(new Color(255, 255, 255, 120));
                        g2.drawLine(0, 0, w, 0);
                    } else if (Boolean.TRUE.equals(hvr)) {
                        // Hover: azul XP muy suave
                        GradientPaint gp = new GradientPaint(0, 0, new Color(230, 240, 255), 0, h, new Color(200, 220, 248));
                        g2.setPaint(gp); g2.fillRect(0, 0, w, h);
                    } else {
                        // Normal: blanco con sutil gradiente
                        GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, h, new Color(240, 244, 252));
                        g2.setPaint(gp); g2.fillRect(0, 0, w, h);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            cell.setOpaque(false);
            cell.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235), 1));
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.putClientProperty("selected", false);
            cell.putClientProperty("hovered", false);
            cell.putClientProperty("towerType", tType);

            JPanel leftCellPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 8));
            leftCellPanel.setOpaque(false);
            leftCellPanel.setPreferredSize(new Dimension(54, 40));
            
            JPanel keyBadge = createKeyBadge(String.valueOf(i + 1));
            JLabel iconLbl = new JLabel(getTowerIconSmall(tType));
            leftCellPanel.add(keyBadge);
            leftCellPanel.add(iconLbl);
            cell.add(leftCellPanel, BorderLayout.WEST);

            JPanel centerCellPanel = new JPanel(new GridLayout(2, 1, 0, 0));
            centerCellPanel.setOpaque(false);
            centerCellPanel.setBorder(BorderFactory.createEmptyBorder(6, 2, 4, 0));
            
            JLabel nameLbl = new JLabel(towerNamesMap[tType]);
            nameLbl.setFont(new Font("Tahoma", Font.BOLD, 10));
            nameLbl.setForeground(new Color(50, 70, 110));
            
            JLabel costLbl = new JLabel(towerCostsMap[tType] + " O");
            costLbl.setFont(new Font("Tahoma", Font.BOLD, 9));
            costLbl.setForeground(new Color(180, 110, 0));
            
            centerCellPanel.add(nameLbl);
            centerCellPanel.add(costLbl);
            cell.add(centerCellPanel, BorderLayout.CENTER);

            JLabel statsLbl = new JLabel();
            statsLbl.setFont(new Font("Tahoma", Font.PLAIN, 8));
            statsLbl.setForeground(new Color(90, 90, 110));
            statsLbl.setPreferredSize(new Dimension(48, 32));
            statsLbl.setHorizontalAlignment(JLabel.RIGHT);
            statsLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
            
            String dVal = towerStatsMap[tType].split("/")[0].replace("D:", "").trim();
            String rVal = towerStatsMap[tType].split("/")[1].replace("R:", "").trim();
            statsLbl.setText("<html>D: " + dVal + "<br>R: " + rVal + "</html>");
            cell.add(statsLbl, BorderLayout.EAST);

            final int finalSlotIndex = i;
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Boolean isCellLocked = (Boolean) cell.getClientProperty("locked");
                    if (isCellLocked != null && isCellLocked) return;
                    if (listener != null) {
                        listener.onKeyPressed(KeyEvent.VK_1 + finalSlotIndex, String.valueOf(finalSlotIndex + 1));
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    Boolean locked = (Boolean) cell.getClientProperty("locked");
                    if (!Boolean.TRUE.equals(locked)) {
                        cell.putClientProperty("hovered", true);
                        cell.repaint();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    cell.putClientProperty("hovered", false);
                    cell.repaint();
                }
            });

            cell.putClientProperty("costLabel", costLbl);
            cell.putClientProperty("statsLabel", statsLbl);
            cell.putClientProperty("nameLabel", nameLbl);
            cell.putClientProperty("iconLabel", iconLbl);
            cell.putClientProperty("originalIcon", getTowerIconSmall(tType));
            cell.putClientProperty("originalCostText", towerCostsMap[tType] + " O");
            cell.putClientProperty("originalStatsText", "<html>D: " + dVal + "<br>R: " + rVal + "</html>");
            cell.putClientProperty("locked", false);

            shopTowerCells[i] = cell;
            shopContent.add(cell);
        }

        // --- MÓDULO 2: Oleada ---
        XPWindow waveWin = new XPWindow("Control de Oleadas", getDocIcon());
        waveWin.setPreferredSize(new Dimension(175, 135));
        waveWin.setMaximumSize(new Dimension(175, 135));
        waveWin.setMinimumSize(new Dimension(175, 135));
        waveWin.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel waveContent = waveWin.getContentPanel();
        waveContent.setLayout(new BoxLayout(waveContent, BoxLayout.Y_AXIS));
        waveContent.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        waveLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        waveLabel.setForeground(new Color(40, 60, 100));
        waveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        waveEnemiesLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        waveEnemiesLabel.setForeground(new Color(60, 80, 110));
        waveEnemiesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        waveProgressBar.setPreferredSize(new Dimension(150, 12));
        waveProgressBar.setMaximumSize(new Dimension(150, 12));
        waveProgressBar.setForeground(new Color(46, 204, 113));
        waveProgressBar.setBackground(new Color(230, 235, 240));
        waveProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        startWaveBtn.setFont(new Font("Tahoma", Font.BOLD, 11));
        startWaveBtn.setBackground(new Color(230, 240, 255));
        startWaveBtn.setFocusPainted(false);
        startWaveBtn.setFocusable(false);
        startWaveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startWaveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startWaveBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onKeyPressed(KeyEvent.VK_ENTER, "Enter");
            }
        });

        waveContent.add(Box.createVerticalStrut(4));
        waveContent.add(waveLabel);
        waveContent.add(Box.createVerticalStrut(4));
        waveContent.add(waveEnemiesLabel);
        waveContent.add(Box.createVerticalStrut(6));
        waveContent.add(waveProgressBar);
        waveContent.add(Box.createVerticalStrut(8));
        waveContent.add(startWaveBtn);
        waveContent.add(Box.createVerticalStrut(4));

        leftSidebar.add(Box.createVerticalStrut(8));
        leftSidebar.add(shopWin);
        leftSidebar.add(Box.createVerticalStrut(8));
        leftSidebar.add(waveWin);
        leftSidebar.add(Box.createVerticalGlue());
    }

    // ── BARRA LATERAL DERECHA: Información, Notificaciones y Minimapa ────────
    private void buildRightSidebar() {
        rightSidebar.setLayout(new BoxLayout(rightSidebar, BoxLayout.Y_AXIS));
        rightSidebar.setPreferredSize(new Dimension(195, 0));
        rightSidebar.setBackground(new Color(212, 228, 252)); // Azul XP suave
        rightSidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(160, 180, 210)));

        // --- MÓDULO 1: Información detallada de torre seleccionada ---
        XPWindow infoWin = new XPWindow("Información", getDocIcon());
        infoWin.setPreferredSize(new Dimension(185, 125));
        infoWin.setMaximumSize(new Dimension(185, 125));
        infoWin.setMinimumSize(new Dimension(185, 125));
        infoWin.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel infoContent = infoWin.getContentPanel();
        infoContent.setLayout(new BorderLayout(4, 4));
        infoContent.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Cabecera del panel de info con gradiente XP azul
        JPanel detailHeaderPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(215, 232, 255), 0, getHeight(), new Color(180, 210, 245));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(160, 190, 225));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        detailHeaderPanel.setOpaque(false);
        detailHeaderPanel.setPreferredSize(new Dimension(0, 22));
        detailHeaderPanel.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        detailNameLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        detailNameLabel.setForeground(new Color(20, 80, 140));
        detailNameLabel.setHorizontalAlignment(JLabel.CENTER);
        detailHeaderPanel.add(detailNameLabel, BorderLayout.CENTER);
        infoContent.add(detailHeaderPanel, BorderLayout.NORTH);

        // Panel central: icono + stats con fondo biselado
        JPanel detailsCenter = new JPanel(new BorderLayout(4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(248, 250, 255));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        detailsCenter.setOpaque(false);
        detailsCenter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED,
                new Color(200, 215, 240), new Color(160, 180, 210)),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        detailIconLabel.setHorizontalAlignment(JLabel.CENTER);
        detailsCenter.add(detailIconLabel, BorderLayout.WEST);

        detailStatLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        detailStatLabel.setForeground(new Color(50, 50, 50));
        detailsCenter.add(detailStatLabel, BorderLayout.CENTER);
        infoContent.add(detailsCenter, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        upgradeBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onKeyPressed(KeyEvent.VK_U, "U");
            }
        });

        sellBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onKeyPressed(KeyEvent.VK_S, "S");
            }
        });

        btnPanel.add(upgradeBtn);
        btnPanel.add(sellBtn);

        avastModeBtn.addActionListener(e -> {
            if (listener != null) listener.onKeyPressed(KeyEvent.VK_M, "M"); // Mode toggle
        });
        avastTargetBtn.addActionListener(e -> {
            if (listener != null) listener.onKeyPressed(KeyEvent.VK_T, "T"); // Target set
        });

        avastBtnPanel.setOpaque(false);
        avastBtnPanel.add(avastModeBtn);
        avastBtnPanel.add(avastTargetBtn);
        avastBtnPanel.setVisible(false); // Oculto por defecto

        JPanel bottomControls = new JPanel(new GridLayout(2, 1, 0, 4));
        bottomControls.setOpaque(false);
        bottomControls.add(avastBtnPanel);
        bottomControls.add(btnPanel);
        
        infoContent.add(bottomControls, BorderLayout.SOUTH);

        // --- MÓDULO 2: Notificaciones ---
        XPWindow notifWin = new XPWindow("Notificaciones", getDocIcon());
        notifWin.setPreferredSize(new Dimension(185, 125));
        notifWin.setMaximumSize(new Dimension(185, 125));
        notifWin.setMinimumSize(new Dimension(185, 125));
        notifWin.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel notifContent = notifWin.getContentPanel();
        notifContent.setLayout(new BorderLayout());
        notifListPanel.setLayout(new BoxLayout(notifListPanel, BoxLayout.Y_AXIS));
        notifListPanel.setBackground(new Color(242, 246, 255));
        notifListPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED,
                new Color(200, 215, 240), new Color(160, 180, 210)),
            BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));

        notifContent.add(notifListPanel, BorderLayout.CENTER);

        // --- MÓDULO 3: Minimapa ---
        XPWindow minimapWin = new XPWindow("Minimapa", getDocIcon());
        minimapWin.setPreferredSize(new Dimension(185, 150));
        minimapWin.setMaximumSize(new Dimension(185, 150));
        minimapWin.setMinimumSize(new Dimension(185, 150));
        minimapWin.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel minimapContent = minimapWin.getContentPanel();
        minimapContent.setLayout(new BorderLayout());
        minimapContent.add(minimapPanel, BorderLayout.CENTER);

        rightSidebar.add(Box.createVerticalStrut(8));
        rightSidebar.add(infoWin);
        rightSidebar.add(Box.createVerticalStrut(8));
        rightSidebar.add(notifWin);
        rightSidebar.add(Box.createVerticalStrut(8));
        rightSidebar.add(minimapWin);
        rightSidebar.add(Box.createVerticalGlue());

        addNotification("✓ Partida cargada exitosamente.");
    }

    // ── BARRA DE TAREAS WINDOWS XP (Abajo) ────────────────────────────────────
    private JPanel buildTaskbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(36, 95, 215),
                    0, getHeight(), new Color(15, 75, 185)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.setColor(new Color(255, 255, 255, 120));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(FRAME_SIZE.width, 34));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 1. Botón start verde XP
        startBtnLabel.setFont(new Font("Trebuchet MS", Font.ITALIC | Font.BOLD, 15));
        startBtnLabel.setForeground(Color.WHITE);
        startBtnLabel.setOpaque(false);
        startBtnLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 0, 0),
            BorderFactory.createEmptyBorder(4, 12, 4, 20)
        ));
        startBtnLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        startBtnLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (listener != null) {
                    listener.onKeyPressed(KeyEvent.VK_F1, "F1");
                }
            }
        });
        bar.add(startBtnLabel, BorderLayout.WEST);

        // 2. Barra de Progreso XP del Nivel (Centro)
        JPanel xpProgressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        xpProgressPanel.setOpaque(false);

        JLabel xpLabel = new JLabel("PROGRESO DEL NIVEL: ");
        xpLabel.setFont(new Font("Tahoma", Font.BOLD, 10));
        xpLabel.setForeground(Color.WHITE);

        xpProgressBar.setPreferredSize(new Dimension(420, 16));
        xpProgressBar.setStringPainted(true);
        xpProgressBar.setFont(new Font("Tahoma", Font.BOLD, 9));

        xpProgressPanel.add(xpLabel);
        xpProgressPanel.add(xpProgressBar);
        bar.add(xpProgressPanel, BorderLayout.CENTER);

        // 3. System Tray (Esquina derecha con Reloj)
        JPanel tray = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        tray.setBackground(new Color(15, 117, 233));
        tray.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED, new Color(10, 90, 190), new Color(50, 130, 255)));

        clockLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        tray.add(clockLabel);
        bar.add(tray, BorderLayout.EAST);

        return bar;
    }

    private void startClock() {
        Timer clockTimer = new Timer(1000, e -> {
            clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        });
        clockTimer.start();
    }

    // ── LÓGICA DE ACTUALIZACIÓN DEL CONTENIDO EN CADA TICK (RENDERING) ─────────
    private void updateSidebarsAndTaskbar(TowerDefenseSnapshot snapshot) {
        Juego juego = snapshot.getJuego();
        if (juego == null) return;

        int gold = snapshot.getStatus().getGold();
        int score = snapshot.getStatus().getScore();
        int levelNum = juego.getNivelActual().getNumeroNivel();

        String targetTitle = (levelNum == 1) 
            ? "Document1 - Tower Defense [Nivel 1: El Documento]"
            : "Papelera de Reciclaje - Tower Defense [Nivel " + levelNum + "]";
        if (!frame.getTitle().equals(targetTitle)) {
            frame.setTitle(targetTitle);
        }

        // ── A. Actualizar Tienda de Torres (Left) ──
        int selectedType = juego.getSelectedTowerType();
        String[] towerNamesMap = { "", "Común", "McAfee", "Área", "Avast", "Fuerte", "Firefox", "I.Explorer", "Eléctrica" };
        int[] towerCostsMap = { 0, 100, 80, 150, 200, 250, 180, 175, 220 };
        String[] towerStatsMap = { "",
            "D: 15 / R: 3.5 / CD: 0.5s",
            "D: 17.2 / R: 3.5 / CD: 0.5s",
            "D: 10 / R: 2.5 / CD: 1.5s",
            "D: 15 / R: 4.0 / CD: 1.0s",
            "D: 35 / R: 3.0 / CD: 2.0s",
            "D: 5 / R: 2.0 / CD: 0.2s",
            "D: 2 / R: 2.5 / CD: 1.0s",
            "D: 12 / R: 4.0 / CD: 0.8s"
        };

        for (int i = 0; i < 8; i++) {
            JPanel cell = shopTowerCells[i];
            
            // Evaluamos dinámicamente qué torre debe ir en este slot
            int tType = com.miJuego.model.ProgresoJuego.getTowerTypeForSlot(i);
            cell.putClientProperty("towerType", tType);
            
            JLabel nameLbl = (JLabel) cell.getClientProperty("nameLabel");
            JLabel costLbl = (JLabel) cell.getClientProperty("costLabel");
            JLabel statsLbl = (JLabel) cell.getClientProperty("statsLabel");
            JLabel iconLbl = (JLabel) cell.getClientProperty("iconLabel");
            
            // Actualizamos los textos e iconos base por si hubo un cambio (ej. desbloqueo)
            nameLbl.setText(towerNamesMap[tType]);
            cell.putClientProperty("originalCostText", towerCostsMap[tType] + " O");
            String dVal = towerStatsMap[tType].split("/")[0].replace("D:", "").trim();
            String rVal = towerStatsMap[tType].split("/")[1].replace("R:", "").trim();
            cell.putClientProperty("originalStatsText", "<html>D: " + dVal + "<br>R: " + rVal + "</html>");
            cell.putClientProperty("originalIcon", getTowerIconSmall(tType));

            ImageIcon originalIcon = (ImageIcon) cell.getClientProperty("originalIcon");
            String originalCostText = (String) cell.getClientProperty("originalCostText");
            String originalStatsText = (String) cell.getClientProperty("originalStatsText");

            boolean isLocked = com.miJuego.model.ProgresoJuego.isTowerLocked(tType, levelNum);

            cell.putClientProperty("locked", isLocked);

            if (isLocked) {
                cell.putClientProperty("selected", false);
                cell.putClientProperty("hovered", false);
                cell.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
                cell.setCursor(Cursor.getDefaultCursor());
                cell.repaint();
                
                nameLbl.setForeground(new Color(130, 130, 130));
                costLbl.setText("Bloqueado");
                costLbl.setForeground(new Color(150, 70, 70));
                costLbl.setFont(new Font("Tahoma", Font.BOLD, 8));
                
                statsLbl.setText("");
                iconLbl.setIcon(getLockedTowerIcon());
            } else {
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                nameLbl.setForeground(new Color(50, 70, 110));
                costLbl.setText(originalCostText);
                costLbl.setFont(new Font("Tahoma", Font.BOLD, 9));
                statsLbl.setText(originalStatsText);
                iconLbl.setIcon(originalIcon);

                if (tType == selectedType) {
                    cell.putClientProperty("selected", true);
                    cell.setBorder(BorderFactory.createLineBorder(new Color(242, 149, 54), 2));
                } else {
                    cell.putClientProperty("selected", false);
                    cell.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235), 1));
                }
                cell.repaint();

                if (gold >= towerCostsMap[tType]) {
                    costLbl.setForeground(new Color(40, 130, 40)); // Verde si le alcanza
                } else {
                    costLbl.setForeground(Color.RED); // Rojo si no le alcanza
                }
            }
        }

        // ── B. Actualizar Módulo Oleada ──
        int totalEnemigos = juego.getNivelActual().getEnemigosRestantesCount();
        waveLabel.setText("Oleada: " + levelNum + " / 5");
        waveEnemiesLabel.setText("Enemigos: " + totalEnemigos);

        int maxEnemigos = juego.getNivelActual().getEnemigosRestantesCount(); // fallback
        int spawned = 0;
        try {
            // Reflejar progreso de spawning en la barra
            java.lang.reflect.Field fieldTotal = juego.getNivelActual().getClass().getDeclaredField("totalEnemigosOleada");
            fieldTotal.setAccessible(true);
            int total = (int) fieldTotal.get(juego.getNivelActual());
            if (total > 0) {
                int completed = total - totalEnemigos;
                waveProgressBar.setMaximum(total);
                waveProgressBar.setValue(completed);
            }
        } catch (Exception ignored) {}

        // ── C. Actualizar Barra de Tareas (Bottom) ──
        xpProgressBar.setMaximum(1000);
        xpProgressBar.setValue(Math.min(1000, score));
        xpProgressBar.setString("Nivel " + levelNum + "  ·  Score: " + score + " XP  ·  Oro: " + gold);

        // ── D. Actualizar Módulo de Información (Right) ──
        int lastX = snapshot.getLastClickedX();
        int lastY = snapshot.getLastClickedY();
        Torre selectedTower = null;

        if (lastX >= 0 && lastY >= 0) {
            for (Torre t : juego.getTorres()) {
                if (Math.round(t.getX()) == lastX && Math.round(t.getY()) == lastY) {
                    selectedTower = t;
                    break;
                }
            }
        }

        if (selectedTower == null) {
            detailNameLabel.setText("Información");
            detailIconLabel.setIcon(null);
            detailStatLabel.setText("<html><center style='font-family: Tahoma; font-size: 9px; color: #606060;'>" +
                "<b>Ninguna seleccionada</b><br><br>" +
                "Haz click en una torre<br>para ver sus detalles y<br>opciones de mejora/venta." +
                "</center></html>");
            upgradeBtn.setText("Mejorar (—)");
            upgradeBtn.setEnabled(false);
            sellBtn.setText("Vender (—)");
            sellBtn.setEnabled(false);
            avastBtnPanel.setVisible(false);
        } else {
            String typeName = selectedTower.getTowertype();
            int currentLvl = selectedTower.getNivelMejora();
            double range = selectedTower.getRango();
            double fireSpeed = selectedTower.getTiempoRecarga() / 1000.0;
            double damage = getDamageFor(selectedTower);

            int upgradeCost = (int) (selectedTower.GetCostoTorre() * 0.5);
            int sellReward = (int) (selectedTower.GetCostoTorre() * 0.5);

            detailNameLabel.setText(typeName + " (Nivel " + currentLvl + ")");
            detailIconLabel.setIcon(getTowerIconLarge(selectedTower));

            String statText = String.format(
                "<html><table style='font-size: 8px; font-family: Tahoma; color: #404040;'>" +
                "<tr><td><b>Daño:</b></td><td>&nbsp;%.1f</td></tr>" +
                "<tr><td><b>Rango:</b></td><td>&nbsp;%.1f</td></tr>" +
                "<tr><td><b>Recarga:</b></td><td>&nbsp;%.2fs</td></tr>" +
                "<tr><td><b>Prioridad:</b></td><td>&nbsp;Cercano</td></tr>" +
                "</table></html>",
                damage, range, fireSpeed
            );
            detailStatLabel.setText(statText);

            upgradeBtn.setText("Mejorar (" + upgradeCost + ")");
            upgradeBtn.setEnabled(gold >= upgradeCost);
            sellBtn.setText("Vender (" + sellReward + ")");
            sellBtn.setEnabled(true);

            if (selectedTower instanceof com.miJuego.model.TorreAvast avastTower) {
                avastBtnPanel.setVisible(true);
                if (avastTower.isManualTargetingMode()) {
                    avastModeBtn.setText("Modo: Área");
                    avastTargetBtn.setEnabled(true);
                } else {
                    avastModeBtn.setText("Modo: Enemigo");
                    avastTargetBtn.setEnabled(false);
                }
            } else {
                avastBtnPanel.setVisible(false);
            }
        }

        // ── E. Actualizar el Minimapa ──
        List<float[]> wps = juego.getNivelActual().getWaypoints();
        List<Point2D> towers = new ArrayList<>();
        for (Torre t : juego.getTorres()) {
            towers.add(new Point2D(t.getX(), t.getY()));
        }
        List<Point2D> enemies = new ArrayList<>();
        for (Enemigo e : juego.getNivelActual().getEnemigosRestantes()) {
            enemies.add(new Point2D(e.getX(), e.getY()));
        }
        minimapPanel.updateData(wps, towers, enemies);
    }

    private double getDamageFor(Torre t) {
        int lvl = t.getNivelMejora();
        if (t instanceof TorreComun) return 15.0 * lvl;
        if (t instanceof TorreDeArea) return 10.0 * lvl;
        if (t instanceof TorreDeFuego) return 5.0 * lvl;
        if (t instanceof TorreDeHielo) return 2.0 * lvl;
        if (t instanceof TorreElectrica) return 12.0 * lvl;
        if (t instanceof TorreFuerte) return 35.0 * lvl;
        if (t instanceof TorreMcAfee) return 18.0 * lvl;
        if (t.getClass().getSimpleName().contains("Cañon")) return 25.0 * lvl;
        return 10.0 * lvl;
    }

    private void updateNotificationsPanel() {
        SwingUtilities.invokeLater(() -> {
            notifListPanel.removeAll();
            for (String n : notificationsList) {
                String timeStr = "";
                String msgText = n;
                if (n.startsWith("[") && n.indexOf("]") > 0) {
                    int idx = n.indexOf("]");
                    timeStr = n.substring(0, idx + 1);
                    msgText = n.substring(idx + 1).trim();
                }

                // Determinar categoría
                Color dotColor = new Color(0, 80, 180);   // INFO = azul
                Color bgColor  = new Color(235, 242, 255); // fondo azul muy suave
                Color fgColor  = new Color(30, 60, 130);

                if (msgText.startsWith("✓")) {
                    msgText = msgText.substring(1).trim();
                }
                if (msgText.startsWith("⚠")) {
                    msgText = msgText.substring(1).trim();
                    dotColor = new Color(200, 120, 0);
                    bgColor  = new Color(255, 248, 225);
                    fgColor  = new Color(130, 75, 0);
                }

                String lower = msgText.toLowerCase();
                if (lower.contains("oleada")) {
                    dotColor = new Color(100, 30, 150);
                    bgColor  = new Color(245, 235, 255);
                    fgColor  = new Color(75, 20, 120);
                } else if (lower.contains("comprada") || lower.contains("mejorada") ||
                           lower.contains("vendida") || lower.contains("oro") ||
                           lower.contains("monedas") || lower.contains("recompensa")) {
                    dotColor = new Color(30, 140, 30);
                    bgColor  = new Color(230, 248, 230);
                    fgColor  = new Color(20, 100, 20);
                } else if (lower.contains("error") || lower.contains("ocupada") || lower.contains("no se puede")) {
                    dotColor = new Color(190, 40, 40);
                    bgColor  = new Color(255, 235, 235);
                    fgColor  = new Color(160, 30, 30);
                }

                final Color finalDot = dotColor;
                final Color finalFg  = fgColor;
                final Color finalBg  = bgColor;
                final String finalMsg = msgText;
                final String finalTime = timeStr;

                // Pill coloreado
                JPanel row = new JPanel(new BorderLayout(4, 0)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        // Fondo
                        g2.setColor(finalBg);
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                        // Borde izquierdo coloreado (franja)
                        g2.setColor(finalDot);
                        g2.fillRoundRect(0, 0, 4, getHeight(), 3, 3);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 4));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                // Tiempo en gris pequeño
                JLabel timeLbl = new JLabel(finalTime);
                timeLbl.setFont(new Font("Tahoma", Font.PLAIN, 8));
                timeLbl.setForeground(new Color(140, 140, 140));
                timeLbl.setPreferredSize(new Dimension(55, 14));

                // Mensaje
                JLabel msgLbl = new JLabel(finalMsg);
                msgLbl.setFont(new Font("Tahoma", Font.PLAIN, 9));
                msgLbl.setForeground(finalFg);

                row.add(timeLbl, BorderLayout.WEST);
                row.add(msgLbl, BorderLayout.CENTER);
                notifListPanel.add(row);
                notifListPanel.add(Box.createVerticalStrut(1));
            }
            notifListPanel.revalidate();
            notifListPanel.repaint();
        });
    }

    private void addNotification(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        notificationsList.add("[" + time + "] " + msg);
        if (notificationsList.size() > 5) {
            notificationsList.remove(0);
        }
        updateNotificationsPanel();
    }

    // ── ASSET RESOLVING ───────────────────────────────────────────────────────
    private ImageIcon getDocIcon() {
        try {
            java.net.URL imgUrl = getClass().getClassLoader().getResource("assets/word/doc_bullet.png");
            if (imgUrl == null) imgUrl = getClass().getClassLoader().getResource("assets/word/page_clippy.png");
            if (imgUrl != null) {
                java.awt.Image img = javax.imageio.ImageIO.read(imgUrl);
                return new ImageIcon(img.getScaledInstance(12, 12, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ImageIcon[] cachedSmallIcons = new ImageIcon[9];

    private ImageIcon getTowerIconSmall(int type) {
        if (type >= 0 && type <= 8 && cachedSmallIcons[type] != null) {
            return cachedSmallIcons[type];
        }
        String path = switch(type) {
            case 2 -> "assets/ingame/torremc_reposo.png";
            case 3 -> "assets/ingame/torre_area_reposo.png";
            case 4 -> "assets/ingame/torreavast_reposo.png";
            case 5 -> "assets/ingame/torrefuerte_reposo.png";
            case 6 -> "assets/ingame/torrefirefox_reposo.png";
            case 7 -> "assets/ingame/torreie_reposo.png";
            case 8 -> "assets/ingame/Sprite_TorreMessenger4.png";
            default -> "assets/ingame/torrecomun_reposo.png";
        };
        try {
            java.net.URL imgUrl = getClass().getClassLoader().getResource(path);
            if (imgUrl != null) {
                java.awt.Image img = javax.imageio.ImageIO.read(imgUrl);
                ImageIcon icon = new ImageIcon(img.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                if (type >= 0 && type <= 8) cachedSmallIcons[type] = icon;
                return icon;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Icon cachedLockedIcon = null;

    private Icon getLockedTowerIcon() {
        if (cachedLockedIcon != null) return cachedLockedIcon;
        try {
            java.net.URL imgUrl = getClass().getClassLoader().getResource("assets/hub/icons/candadoCerrado.png");
            if (imgUrl != null) {
                java.awt.Image img = javax.imageio.ImageIO.read(imgUrl);
                cachedLockedIcon = new ImageIcon(img.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
                return cachedLockedIcon;
            }
        } catch (Exception ignored) {}
        cachedLockedIcon = new LockIcon(16);
        return cachedLockedIcon;
    }

    private ImageIcon getTowerIconLarge(Torre t) {
        String path = (t instanceof TorreMcAfee) ? "assets/ingame/torremc_reposo.png" : "assets/ingame/torrecomun0.png";
        return largeIconCache.computeIfAbsent(path, p -> {
            try {
                java.net.URL imgUrl = getClass().getClassLoader().getResource(p);
                if (imgUrl != null) {
                    java.awt.Image img = javax.imageio.ImageIO.read(imgUrl);
                    return new ImageIcon(img.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                }
            } catch (Exception ignored) {}
            return null;
        });
    }

    // ── INPUT ROUTING Y DELEGATE DE CONTROLLER ─────────────────────────────────
    private void wireInput() {
        MouseAdapter mouse = new MouseAdapter() {
            private Point dragStartPoint = null;
            private float dragStartCamX = 0f;
            private float dragStartCamY = 0f;

            @Override
            public void mousePressed(MouseEvent e) {
                // Si es click izquierdo normal, despachar al modelo para interacción del juego
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (listener != null) {
                        listener.onPointerDown(gamePanel.toWorldX(e.getX()), gamePanel.toWorldY(e.getY()));
                    }
                } else if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    // Clic derecho o botón central inicia el paneo/desplazamiento de cámara
                    dragStartPoint = e.getPoint();
                    dragStartCamX = com.miJuego.model.CameraContext.getTargetCameraX();
                    dragStartCamY = com.miJuego.model.CameraContext.getTargetCameraY();
                    gamePanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (listener != null) {
                        listener.onPointerUp(gamePanel.toWorldX(e.getX()), gamePanel.toWorldY(e.getY()));
                    }
                } else {
                    if (dragStartPoint != null) {
                        dragStartPoint = null;
                        gamePanel.setCursor(Cursor.getDefaultCursor());
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                float wx = gamePanel.toWorldX(e.getX());
                float wy = gamePanel.toWorldY(e.getY());
                int ix = (int) wx;
                int iy = (int) wy;
                if (ix >= 0 && ix < com.miJuego.model.CameraContext.getWorldW() && iy >= 0 && iy < com.miJuego.model.CameraContext.getWorldH()) {
                    com.miJuego.model.ActualTowerContext.setHoverX(ix);
                    com.miJuego.model.ActualTowerContext.setHoverY(iy);
                } else {
                    com.miJuego.model.ActualTowerContext.setHoverX(-1);
                    com.miJuego.model.ActualTowerContext.setHoverY(-1);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStartPoint != null) {
                    // Calcular el desplazamiento en pixeles
                    int dx = e.getX() - dragStartPoint.x;
                    int dy = e.getY() - dragStartPoint.y;

                    // Traducir desplazamiento de pixeles a celdas lógicas usando valores target
                    float vpW = com.miJuego.model.CameraContext.getTargetViewportW();
                    float vpH = com.miJuego.model.CameraContext.getTargetViewportH();
                    float sx = gamePanel.getWidth() / vpW;
                    float sy = gamePanel.getHeight() / vpH;

                    // Restar el desplazamiento para que el mapa se mueva en la dirección del arrastre (paneo natural)
                    float targetX = dragStartCamX - (dx / sx);
                    float targetY = dragStartCamY - (dy / sy);

                    com.miJuego.model.CameraContext.setCameraX(targetX);
                    com.miJuego.model.CameraContext.setCameraY(targetY);
                } else {
                    mouseMoved(e);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                com.miJuego.model.ActualTowerContext.setHoverX(-1);
                com.miJuego.model.ActualTowerContext.setHoverY(-1);
            }

            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                int panelW = gamePanel.getWidth();
                int panelH = gamePanel.getHeight();
                if (panelW <= 0 || panelH <= 0) return;

                int mouseX = e.getX();
                int mouseY = e.getY();

                if (e.isControlDown()) {
                    // Zoom suave centrado en la posición del puntero
                    float targetCamX = com.miJuego.model.CameraContext.getTargetCameraX();
                    float targetCamY = com.miJuego.model.CameraContext.getTargetCameraY();
                    float targetVpW = com.miJuego.model.CameraContext.getTargetViewportW();
                    float targetVpH = com.miJuego.model.CameraContext.getTargetViewportH();

                    float worldX = targetCamX + (mouseX * targetVpW / (float) panelW);
                    float worldY = targetCamY + (mouseY * targetVpH / (float) panelH);

                    float zoomFactor = 1.08f;
                    int rotation = e.getWheelRotation();
                    float newVpW = targetVpW * (float) Math.pow(zoomFactor, rotation);

                    // Limitar tamaño del viewport
                    newVpW = Math.max(8f, Math.min(com.miJuego.model.CameraContext.getWorldW() * 2f, newVpW));
                    float newVpH = newVpW * 0.75f; // Mantener relación de aspecto 4:3 (16:12)

                    // Reajustar la cámara target para que el punto bajo el cursor mantenga la misma posición en pantalla
                    float newTargetCamX = worldX - (mouseX * newVpW / (float) panelW);
                    float newTargetCamY = worldY - (mouseY * newVpH / (float) panelH);

                    com.miJuego.model.CameraContext.setTargetViewport(newVpW, newVpH);
                    com.miJuego.model.CameraContext.setCameraX(newTargetCamX);
                    com.miJuego.model.CameraContext.setCameraY(newTargetCamY);
                } else {
                    // Paneo suave
                    float scrollAmount = (float) e.getPreciseWheelRotation() * 0.8f;
                    if (e.isShiftDown()) {
                        // Paneo horizontal
                        com.miJuego.model.CameraContext.setCameraX(com.miJuego.model.CameraContext.getTargetCameraX() + scrollAmount);
                    } else {
                        // Paneo vertical
                        com.miJuego.model.CameraContext.setCameraY(com.miJuego.model.CameraContext.getTargetCameraY() + scrollAmount);
                    }
                }
            }
        };
        gamePanel.addMouseListener(mouse);
        gamePanel.addMouseMotionListener(mouse);
        gamePanel.addMouseWheelListener(mouse);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                    if (activeWindow == frame) {
                        if (listener != null) {
                            listener.onKeyPressed(e.getKeyCode(), KeyEvent.getKeyText(e.getKeyCode()));
                        }
                    }
                }
                return false;
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

            // Actualizar barras laterales y barra de tareas XP si es nuestro snapshot
            if (frame instanceof TowerDefenseSnapshot) {
                updateSidebarsAndTaskbar((TowerDefenseSnapshot) frame);
            }

            // Ocultar HUD y sidebars en transición a cinemática de confrontación
            boolean isConfrontation = frame.getDrawables().stream().anyMatch(d -> d.getId().equals("clippy-confrontation"));
            if (isConfrontation) {
                if (navBarPanel.isVisible()) {
                    navBarPanel.setVisible(false);
                    leftSidebar.setVisible(false);
                    rightSidebar.setVisible(false);
                    if (taskbar != null) taskbar.setVisible(false);
                    SwingGameView.this.frame.getContentPane().revalidate();
                    SwingGameView.this.frame.getContentPane().repaint();
                }
            } else {
                if (!navBarPanel.isVisible()) {
                    navBarPanel.setVisible(true);
                    leftSidebar.setVisible(true);
                    rightSidebar.setVisible(true);
                    if (taskbar != null) taskbar.setVisible(true);
                    SwingGameView.this.frame.getContentPane().revalidate();
                    SwingGameView.this.frame.getContentPane().repaint();
                }
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
    public void setViewportSize(int widthPx, int heightPx) {}

    @Override
    public void show() {
        SwingUtilities.invokeLater(() -> {
            // Restore the original game layout in case it was replaced by the Hub
            frame.getContentPane().removeAll();
            frame.add(navBarPanel, BorderLayout.NORTH);
            frame.add(leftSidebar, BorderLayout.WEST);
            frame.add(gamePanel, BorderLayout.CENTER);
            frame.add(rightSidebar, BorderLayout.EAST);
            frame.add(taskbar, BorderLayout.SOUTH);
            frame.revalidate();
            frame.repaint();

            frame.setVisible(true);
            frame.requestFocus();
            
            // Usar Timer para garantizar que Windows procese la ventana antes de maximizar
            javax.swing.Timer maxTimer = new javax.swing.Timer(50, e -> {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            });
            maxTimer.setRepeats(false);
            maxTimer.start();
        });
    }

    @Override
    public void successMessage(String message) {
        messageOverlay.successMessage(message);
        addNotification("✓ " + message);
    }

    @Override
    public void errorMessage(String message) {
        messageOverlay.errorMessage(message);
        addNotification("⚠ " + message);
    }

    // ── SUBCLASES AUXILIARES ───────────────────────────────────────────────────

    private static class Point2D {
        float x, y;
        Point2D(float x, float y) { this.x = x; this.y = y; }
    }

    /**
     * Módulo que replica una tarjeta clásica con barra de cabecera azul y botón cerrar.
     */
    private static class XPWindow extends JPanel {
        private final JPanel content;
        private final JLabel titleLabel;

        XPWindow(String title, ImageIcon titleIcon) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(XP_BLUE_DARK, 2),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ));

            JPanel titleBar = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    GradientPaint gp = new GradientPaint(
                        0, 0, XP_BLUE_LIGHT,
                        0, getHeight(), XP_BLUE_DARK
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    g2.setColor(new Color(255, 255, 255, 100));
                    g2.drawLine(0, 0, getWidth(), 0);
                    g2.dispose();
                }
            };
            titleBar.setPreferredSize(new Dimension(0, 24));
            titleBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            titleLabel = new JLabel(title, titleIcon, JLabel.LEFT);
            titleLabel.setFont(new Font("Tahoma", Font.BOLD, 10));
            titleLabel.setForeground(Color.WHITE);
            titleBar.add(titleLabel, BorderLayout.WEST);

            JButton closeBtn = new JButton("✕") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    boolean isPressed = getModel().isPressed();
                    boolean isRollover = getModel().isRollover();
                    
                    Color cStart = isPressed ? new Color(180, 40, 40) : (isRollover ? new Color(255, 110, 110) : new Color(230, 80, 80));
                    Color cEnd = isPressed ? new Color(140, 20, 20) : (isRollover ? new Color(210, 60, 60) : new Color(180, 50, 50));
                    
                    GradientPaint gp = new GradientPaint(0, 0, cStart, 0, h, cEnd);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 4, 4);
                    
                    g2.setColor(new Color(150, 30, 30));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 4, 4);
                    
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            closeBtn.setOpaque(false);
            closeBtn.setContentAreaFilled(false);
            closeBtn.setFont(new Font("Tahoma", Font.BOLD, 8));
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFocusPainted(false);
            closeBtn.setFocusable(false);
            closeBtn.setPreferredSize(new Dimension(16, 16));
            titleBar.add(closeBtn, BorderLayout.EAST);

            add(titleBar, BorderLayout.NORTH);

            content = new JPanel();
            content.setBackground(Color.WHITE);
            add(content, BorderLayout.CENTER);
        }

        JPanel getContentPanel() {
            return content;
        }
    }

    /**
     * Botón personalizado estilo Windows XP Luna con estados hover/click y relieve 3D.
     */
    private static class XPButton extends JButton {
        private final Color startColor;
        private final Color endColor;
        private final Color hoverStart;
        private final Color hoverEnd;
        private final Color pressedColor;
        private final Color borderColor;
        
        XPButton(String text) {
            this(text, XP_SILVER_LIGHT, XP_SILVER_DARK, XP_ORANGE_HOVER, XP_ORANGE_HOVER_END, new Color(50, 50, 50));
        }

        XPButton(String text, Color start, Color end, Color hoverS, Color hoverE, Color fg) {
            super(text);
            this.startColor = start;
            this.endColor = end;
            this.hoverStart = hoverS;
            this.hoverEnd = hoverE;
            this.pressedColor = end.darker();
            this.borderColor = XP_BORDER_BLUE;
            
            setFont(new Font("Tahoma", Font.BOLD, 10));
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            boolean isPressed = getModel().isPressed();
            boolean isRollover = getModel().isRollover();
            boolean isEnabled = isEnabled();

            Color cStart = startColor;
            Color cEnd = endColor;
            Color cBorder = borderColor;

            if (!isEnabled) {
                cStart = new Color(230, 230, 230);
                cEnd = new Color(210, 210, 210);
                cBorder = new Color(180, 180, 180);
            } else if (isPressed) {
                cStart = pressedColor;
                cEnd = startColor;
            } else if (isRollover) {
                cStart = hoverStart;
                cEnd = hoverEnd;
                cBorder = XP_ORANGE_BORDER;
            }

            // Fondo gradiente redondeado
            GradientPaint gp = new GradientPaint(0, 0, cStart, 0, h, cEnd);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 6, 6);

            // Borde
            g2.setColor(cBorder);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

            // Brillo superior
            if (isEnabled && !isPressed) {
                g2.setColor(new Color(255, 255, 255, 120));
                g2.drawLine(2, 2, w - 3, 2);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Icono de candado dibujado con Graphics2D para evitar emojis.
     */
    private static class LockIcon implements Icon {
        private final int size;
        LockIcon(int size) { this.size = size; }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Cuerpo dorado
            g2.setColor(new Color(218, 165, 32));
            g2.fillRect(x + 2, y + size/2 - 1, size - 4, size/2);
            g2.setColor(new Color(184, 134, 11));
            g2.drawRect(x + 2, y + size/2 - 1, size - 4, size/2);
            
            // Grillete metálico
            g2.setColor(new Color(120, 120, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawArc(x + 4, y + 2, size - 9, size - 4, 0, 180);
            
            // Ojo de cerradura
            g2.setColor(Color.BLACK);
            g2.fillOval(x + size/2 - 1, y + size/2 + 2, 2, 2);
            
            g2.dispose();
        }
        
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    /**
     * Helper para crear un contenedor estilo tecla física para atajos de teclado.
     */
    private static JPanel createKeyBadge(String text) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                
                GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, h, new Color(220, 220, 220));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 4, 4);
                
                g2.setColor(new Color(150, 150, 150));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 4, 4);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new BorderLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        
        JLabel lbl = new JLabel(text, JLabel.CENTER);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 9));
        lbl.setForeground(new Color(50, 50, 50));
        badge.add(lbl, BorderLayout.CENTER);
        badge.setPreferredSize(new Dimension(18, 16));
        badge.setMinimumSize(new Dimension(18, 16));
        badge.setMaximumSize(new Dimension(18, 16));
        return badge;
    }

    /**
     * Barra de progreso estilo Windows XP con bloques segmentados verdes de gradiente.
     */
    private static class XPProgressBar extends JProgressBar {
        XPProgressBar() {
            setOpaque(false);
            setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            g2.setColor(new Color(240, 240, 240));
            g2.fillRect(0, 0, w, h);
            
            g2.setColor(new Color(180, 180, 180));
            g2.drawRect(0, 0, w - 1, h - 1);
            
            double percent = getPercentComplete();
            int progressW = (int) (percent * (w - 4));
            
            if (progressW > 0) {
                int blockSize = 8;
                int gap = 2;
                g2.setColor(new Color(40, 180, 40));
                for (int x = 2; x < 2 + progressW; x += (blockSize + gap)) {
                    int currentBlockW = Math.min(blockSize, 2 + progressW - x);
                    GradientPaint gp = new GradientPaint(
                        x, 2, new Color(110, 220, 110),
                        x, h - 4, new Color(30, 150, 30)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(x, 2, currentBlockW, h - 4);
                    
                    g2.setColor(new Color(20, 100, 20));
                    g2.drawRect(x, 2, currentBlockW - 1, h - 5);
                }
            }
            
            if (isStringPainted()) {
                g2.setColor(Color.BLACK);
                g2.setFont(getFont());
                String str = getString();
                FontMetrics fm = g2.getFontMetrics();
                int strW = fm.stringWidth(str);
                int strH = fm.getAscent() - fm.getDescent();
                g2.drawString(str, (w - strW) / 2, (h + strH) / 2 - 1);
            }
            
            g2.dispose();
        }
    }

    /**
     * Renderiza un minimapa simplificado en tiempo real con el trazado y las entidades.
     */
    private static class MinimapPanel extends JPanel {
        private List<float[]> path = new ArrayList<>();
        private List<Point2D> towers = new ArrayList<>();
        private List<Point2D> enemies = new ArrayList<>();

        MinimapPanel() {
            setPreferredSize(new Dimension(170, 110));
            setBackground(new Color(20, 25, 20)); // Fondo radar verde/negro oscuro
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED),
                BorderFactory.createLineBorder(new Color(40, 60, 40), 1)
            ));
        }

        void updateData(List<float[]> path, List<Point2D> towers, List<Point2D> enemies) {
            this.path = path;
            this.towers = towers;
            this.enemies = enemies;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Dibujar rejilla/cuadrícula del radar
            g2.setColor(new Color(0, 120, 0, 35));
            g2.setStroke(new BasicStroke(0.8f));
            for (int gridX = 0; gridX < w; gridX += 15) {
                g2.drawLine(gridX, 0, gridX, h);
            }
            for (int gridY = 0; gridY < h; gridY += 15) {
                g2.drawLine(0, gridY, w, gridY);
            }

            float sx = w / com.miJuego.model.CameraContext.getWorldW();
            float sy = h / com.miJuego.model.CameraContext.getWorldH();

            // Dibujar el camino como una pista de radar
            if (path.size() > 1) {
                g2.setColor(new Color(60, 110, 60));
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < path.size() - 1; i++) {
                    float[] p1 = path.get(i);
                    float[] p2 = path.get(i + 1);
                    g2.drawLine(Math.round(p1[0] * sx), Math.round(p1[1] * sy),
                               Math.round(p2[0] * sx), Math.round(p2[1] * sy));
                }
            }

            // Dibujar torres (puntos verdes brillante)
            g2.setColor(new Color(50, 220, 100));
            for (Point2D t : towers) {
                int tx = Math.round(t.x * sx);
                int ty = Math.round(t.y * sy);
                g2.fillOval(tx - 3, ty - 3, 6, 6);
            }

            // Dibujar enemigos (puntos rojos brillante)
            g2.setColor(new Color(255, 60, 60));
            for (Point2D e : enemies) {
                int ex = Math.round(e.x * sx);
                int ey = Math.round(e.y * sy);
                g2.fillOval(ex - 2, ey - 2, 4, 4);
            }

            // ── Indicador de viewport (rectángulo de la cámara) ──────────────
            float camX = com.miJuego.model.CameraContext.getCameraX();
            float camY = com.miJuego.model.CameraContext.getCameraY();
            float vpW  = com.miJuego.model.CameraContext.VIEWPORT_W;
            float vpH  = com.miJuego.model.CameraContext.VIEWPORT_H;
            int vx = Math.round(camX * sx);
            int vy = Math.round(camY * sy);
            int vw = Math.round(vpW  * sx);
            int vh = Math.round(vpH  * sy);
            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillRect(vx, vy, vw, vh);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRect(vx, vy, vw, vh);

            g2.dispose();
        }
    }

    @Override
    public void showPrizePopup(Runnable onClose) {
        SwingUtilities.invokeLater(() -> {
            AdwarePrizeDialog dialog = new AdwarePrizeDialog(frame, onClose);
            dialog.setVisible(true);
        });
    }

    private static class AdwarePrizeDialog extends JDialog {
        private final Runnable onCloseCallback;
        private Timer countdownTimer;
        private int remainingSeconds = 299; // 4:59
        private Image relicImage = null;

        public AdwarePrizeDialog(Frame parent, Runnable onCloseCallback) {
            super(parent, true);
            this.onCloseCallback = onCloseCallback;
            setUndecorated(true);
            setSize(760, 530);
            setLocationRelativeTo(parent);

            try {
                java.net.URL imgUrl = getClass().getClassLoader().getResource("assets/word/golden_word_relic.png");
                if (imgUrl != null) {
                    relicImage = javax.imageio.ImageIO.read(imgUrl);
                }
            } catch (Exception ignored) {}

            // Panel de contenido principal con diseño personalizado
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Fondo degradado de adware misterioso (violeta oscuro a negro)
                    GradientPaint gp = new GradientPaint(0, 0, new Color(26, 0, 45), 0, getHeight(), new Color(10, 0, 20));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Dibujar algunas lineas de error o virus matrix al azar (glitch)
                    g2.setColor(new Color(0, 255, 0, 30));
                    g2.setStroke(new BasicStroke(0.8f));
                    for (int i = 0; i < getWidth(); i += 40) {
                        g2.drawLine(i, 0, i, getHeight());
                    }
                    for (int i = 0; i < getHeight(); i += 40) {
                        g2.drawLine(0, i, getWidth(), i);
                    }

                    // Borde glitch (doble borde: verde brillante exterior y morado interior)
                    g2.setStroke(new BasicStroke(4f));
                    g2.setColor(new Color(50, 205, 50)); // Verde brillante
                    g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
                    
                    g2.setStroke(new BasicStroke(2f));
                    g2.setColor(new Color(180, 50, 220)); // Púrpura glitch
                    g2.drawRect(6, 6, getWidth() - 12, getHeight() - 12);

                    // Dibujar el escudo de peligro en la cabecera (exclamación ⚠️)
                    int sx = getWidth() / 2;
                    int sy = 30;
                    g2.setColor(new Color(255, 200, 0));
                    int[] tx = { sx, sx - 20, sx + 20 };
                    int[] ty = { sy - 18, sy + 18, sy + 18 };
                    g2.fillPolygon(tx, ty, 3);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 18));
                    g2.drawString("!", sx - 3, sy + 13);

                    g2.dispose();
                }
            };
            mainPanel.setLayout(null); // Layout nulo para posicionar con exactitud pixel-perfect

            // 1. TÍTULO PRINCIPAL DE LA CABECERA
            JLabel headerTitle = new JLabel("¡Te has ganado un premio!", JLabel.CENTER);
            headerTitle.setFont(new Font("Tahoma", Font.BOLD, 28));
            headerTitle.setForeground(new Color(255, 215, 0)); // Dorado
            headerTitle.setBounds(50, 45, 660, 40);
            mainPanel.add(headerTitle);

            // 2. SUBTÍTULO
            JLabel headerSubtitle = new JLabel("!!! ¡FELICITACIONES! Eres el usuario #1.000.000 elegido hoy. !!!", JLabel.CENTER);
            headerSubtitle.setFont(new Font("Tahoma", Font.BOLD, 12));
            headerSubtitle.setForeground(new Color(230, 200, 255));
            headerSubtitle.setBounds(50, 85, 660, 20);
            mainPanel.add(headerSubtitle);

            // ── COLUMNA 1: CLIPPY CORRUPTO Y SU DISCURSO (IZQUIERDA) ─────────────────
            // Cargar imagen de clippy corrupto
            BufferedImage clippyImg = null;
            try {
                java.io.InputStream is = getClass().getResourceAsStream("/assets/word/clippy_corrupto_posenormal.png");
                if (is == null) {
                    // Fallback
                    is = getClass().getResourceAsStream("/assets/word/clippy_sprite.png");
                }
                if (is != null) {
                    clippyImg = javax.imageio.ImageIO.read(is);
                }
            } catch (Exception ignored) {}

            final Image scaledClippy = clippyImg != null ? clippyImg.getScaledInstance(130, 160, Image.SCALE_SMOOTH) : null;
            
            // JComponent para Clippy y su bocadillo
            JComponent clippyBox = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Dibujar el bocadillo de diálogo (Globo blanco con borde negro)
                    int bx = 10;
                    int by = 10;
                    int bw = 170;
                    int bh = 55;

                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(bx, by, bw, bh, 12, 12);
                    g2.setColor(new Color(50, 205, 50));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(bx, by, bw, bh, 12, 12);

                    // Triángulo apuntando a Clippy (abajo a la derecha del bocadillo)
                    int[] tx = { bx + 120, bx + 130, bx + 140 };
                    int[] ty = { by + bh, by + bh + 12, by + bh };
                    g2.setColor(Color.WHITE);
                    g2.fillPolygon(tx, ty, 3);
                    g2.setColor(new Color(50, 205, 50));
                    g2.drawPolyline(tx, ty, 3);

                    // Escribir el texto de Clippy en el bocadillo
                    g2.setColor(new Color(0, 100, 0));
                    g2.setFont(new Font("Tahoma", Font.BOLD, 10));
                    g2.drawString("¡Este premio puede", bx + 15, by + 22);
                    g2.drawString("mejorar TU SISTEMA!", bx + 12, by + 38);

                    // Dibujar la imagen de clippy
                    if (scaledClippy != null) {
                        g2.drawImage(scaledClippy, 25, 80, null);
                    } else {
                        // Fallback dibujo
                        g2.setColor(new Color(50, 205, 50));
                        g2.fillOval(50, 110, 50, 100);
                        g2.setColor(Color.WHITE);
                        g2.drawString("Clippy", 60, 160);
                    }
                    g2.dispose();
                }
            };
            clippyBox.setBounds(30, 120, 200, 260);
            mainPanel.add(clippyBox);

            // ── COLUMNA 2: SHIELD MCAFEE VECTORIAL (CENTRO) ─────────────────────
            JComponent shieldBox = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (relicImage != null) {
                        g2.drawImage(relicImage, 0, 0, getWidth(), getHeight(), null);
                        g2.dispose();
                        return;
                    }

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2 - 10;

                    // 1. Dibujar destello/resplandor radial en el fondo
                    float[] dist = { 0.0f, 0.7f, 1.0f };
                    Color[] colors = { new Color(255, 215, 0, 160), new Color(255, 140, 0, 50), new Color(0, 0, 0, 0) };
                    RadialGradientPaint rgp = new RadialGradientPaint(cx, cy, 110, dist, colors);
                    g2.setPaint(rgp);
                    g2.fillOval(cx - 120, cy - 120, 240, 240);

                    // 2. Pedestal morado/azul neón
                    g2.setColor(new Color(75, 0, 130, 220)); // Morado oscuro
                    g2.fillOval(cx - 70, cy + 60, 140, 30);
                    g2.setColor(new Color(0, 255, 255, 150)); // Borde cian neón
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawOval(cx - 70, cy + 60, 140, 30);
                    
                    // Líneas de rejilla del pedestal
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.drawLine(cx - 50, cy + 68, cx + 50, cy + 68);
                    g2.drawLine(cx, cy + 60, cx, cy + 90);

                    // 3. Escudo McAfee dorado y rojo
                    // Escudo exterior (Dorado)
                    int sw = 90;
                    int sh = 110;
                    
                    // Path para el escudo
                    java.awt.geom.Path2D shieldOuter = new java.awt.geom.Path2D.Double();
                    shieldOuter.moveTo(cx, cy - sh/2);
                    shieldOuter.quadTo(cx + sw/2, cy - sh/2 + 5, cx + sw/2 - 5, cy + sh/6);
                    shieldOuter.quadTo(cx + sw/2 - 15, cy + sh/2 - 10, cx, cy + sh/2);
                    shieldOuter.quadTo(cx - sw/2 + 15, cy + sh/2 - 10, cx - sw/2 + 5, cy + sh/6);
                    shieldOuter.quadTo(cx - sw/2, cy - sh/2 + 5, cx, cy - sh/2);
                    shieldOuter.closePath();

                    g2.setPaint(new GradientPaint(cx - sw/2, cy - sh/2, new Color(255, 223, 0), cx + sw/2, cy + sh/2, new Color(184, 134, 11)));
                    g2.fill(shieldOuter);

                    // Escudo interior (Rojo)
                    int isw = 72;
                    int ish = 92;
                    java.awt.geom.Path2D shieldInner = new java.awt.geom.Path2D.Double();
                    shieldInner.moveTo(cx, cy - ish/2);
                    shieldInner.quadTo(cx + isw/2, cy - ish/2 + 4, cx + isw/2 - 4, cy + ish/6);
                    shieldInner.quadTo(cx + isw/2 - 12, cy + ish/2 - 8, cx, cy + ish/2);
                    shieldInner.quadTo(cx - isw/2 + 12, cy + ish/2 - 8, cx - isw/2 + 4, cy + ish/6);
                    shieldInner.quadTo(cx - isw/2, cy - ish/2 + 4, cx, cy - ish/2);
                    shieldInner.closePath();

                    g2.setPaint(new GradientPaint(cx - isw/2, cy - ish/2, new Color(240, 20, 20), cx + isw/2, cy + ish/2, new Color(130, 0, 0)));
                    g2.fill(shieldInner);

                    // Dibujar la letra M en el centro del escudo
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Dibujar una "M" estilizada estilo McAfee
                    int my = cy - 18;
                    int mh = 36;
                    int mw = 36;
                    g2.drawLine(cx - mw/2, my + mh, cx - mw/2, my);
                    g2.drawLine(cx - mw/2, my, cx, my + mh/2 + 4);
                    g2.drawLine(cx, my + mh/2 + 4, cx + mw/2, my);
                    g2.drawLine(cx + mw/2, my, cx + mw/2, my + mh);

                    g2.dispose();
                }
            };
            shieldBox.setBounds(240, 120, 250, 260);
            mainPanel.add(shieldBox);

            // ── COLUMNA 3: BENEFICIOS EXCLUSIVOS (DERECHA) ───────────────────────
            JPanel benefitsBox = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Fondo negro-azul
                    g2.setColor(new Color(10, 10, 25, 230));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Borde verde
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.setColor(new Color(50, 205, 50));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    
                    g2.dispose();
                }
            };
            benefitsBox.setLayout(null);
            benefitsBox.setBounds(500, 125, 230, 245);

            JLabel benefitsTitle = new JLabel("CONTENIDO EXCLUSIVO:", JLabel.CENTER);
            benefitsTitle.setFont(new Font("Tahoma", Font.BOLD, 12));
            benefitsTitle.setForeground(new Color(50, 220, 50));
            benefitsTitle.setBounds(10, 12, 210, 20);
            benefitsBox.add(benefitsTitle);

            String[] items = {
                "✓ Rendimiento infinito",
                "✓ Monedas ilimitadas",
                "✓ Sin anuncios",
                "✓ Poder supremo",
                "✓ Acceso VIP eterno"
            };
            int itemY = 42;
            for (String item : items) {
                JLabel lbl = new JLabel(item);
                lbl.setFont(new Font("Tahoma", Font.BOLD, 11));
                lbl.setForeground(Color.WHITE);
                lbl.setBounds(20, itemY, 200, 22);
                benefitsBox.add(lbl);
                itemY += 26;
            }

            JLabel onlyToday = new JLabel("¡SOLO HOY!", JLabel.CENTER);
            onlyToday.setFont(new Font("Tahoma", Font.BOLD, 16));
            onlyToday.setForeground(new Color(255, 20, 147)); // Rosa fucsia brillante
            onlyToday.setBounds(10, 178, 210, 25);
            benefitsBox.add(onlyToday);

            JLabel cautionLabel = new JLabel("[!]  Instalación automática requerida.", JLabel.CENTER);
            cautionLabel.setFont(new Font("Tahoma", Font.PLAIN, 9));
            cautionLabel.setForeground(new Color(255, 180, 0));
            cautionLabel.setBounds(5, 212, 220, 15);
            benefitsBox.add(cautionLabel);

            mainPanel.add(benefitsBox);

            // ── TEXTO DE ADVERTENCIA Y TEMPORIZADOR ABAJO ────────────────────────
            JLabel promoText = new JLabel("¡No dejes pasar esta oportunidad irrepetible!", JLabel.CENTER);
            promoText.setFont(new Font("Tahoma", Font.BOLD, 14));
            promoText.setForeground(Color.WHITE);
            promoText.setBounds(50, 385, 660, 20);
            mainPanel.add(promoText);

            JLabel timerLabel = new JLabel("La oferta expira en: 00:04:59", JLabel.CENTER);
            timerLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
            timerLabel.setForeground(new Color(50, 255, 50)); // Verde brillante neón
            timerLabel.setBounds(50, 410, 660, 20);
            mainPanel.add(timerLabel);

            // ── BOTONES DE ACCIÓN ────────────────────────────────────────────────
            // Botón RECLAMAR
            JButton claimBtn = new JButton("RECLAMAR PREMIO") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(new Color(34, 139, 34));
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(60, 230, 60));
                    } else {
                        g2.setColor(new Color(50, 205, 50)); // Verde brillante
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    
                    // Sparkles (estrellas/destellos simples dibujados a mano)
                    g2.setColor(Color.YELLOW);
                    g2.setFont(new Font("Default", Font.PLAIN, 12));
                    g2.drawString("✦", 8, 16);
                    g2.drawString("✦", getWidth() - 18, 28);

                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            claimBtn.setFont(new Font("Tahoma", Font.BOLD, 18));
            claimBtn.setForeground(Color.WHITE);
            claimBtn.setContentAreaFilled(false);
            claimBtn.setFocusPainted(false);
            claimBtn.setBorder(BorderFactory.createEmptyBorder());
            claimBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            claimBtn.setBounds(200, 440, 260, 40);
            claimBtn.addActionListener(e -> acceptReward());
            mainPanel.add(claimBtn);

            // Botón CANCELAR
            JButton cancelBtn = new JButton("Cancelar") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(new Color(100, 100, 100));
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(160, 160, 160));
                    } else {
                        g2.setColor(new Color(130, 130, 130));
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            cancelBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setContentAreaFilled(false);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setBorder(BorderFactory.createEmptyBorder());
            cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cancelBtn.setBounds(480, 440, 110, 40);
            cancelBtn.addActionListener(e -> cancelReward());
            mainPanel.add(cancelBtn);

            // ── FOOTER DE POLÍTICAS DE PRIVACIDAD ────────────────────────────────
            JLabel footerText = new JLabel("[!] Al aceptar, permites cambios en el sistema. | [!] Instalación automática requerida. | Política de privacidad", JLabel.CENTER);
            footerText.setFont(new Font("Tahoma", Font.PLAIN, 10));
            footerText.setForeground(new Color(130, 140, 160));
            footerText.setBounds(50, 495, 660, 20);
            mainPanel.add(footerText);

            // Temporizador para la cuenta regresiva
            countdownTimer = new Timer(1000, e -> {
                remainingSeconds--;
                if (remainingSeconds < 0) remainingSeconds = 0;
                int mins = remainingSeconds / 60;
                int secs = remainingSeconds % 60;
                timerLabel.setText(String.format("La oferta expira en: 00:%02d:%02d", mins, secs));
            });
            countdownTimer.start();

            // Asegurar que al cerrar la ventana por otros medios, se ejecute la acción
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cleanupAndClose();
                }
            });

            setContentPane(mainPanel);
        }

        private void acceptReward() {
            cleanupAndClose();
        }

        private void cancelReward() {
            cleanupAndClose();
        }

        private void cleanupAndClose() {
            if (countdownTimer != null) {
                countdownTimer.stop();
                countdownTimer = null;
            }
            dispose();
            onCloseCallback.run();
        }
    }

    @Override
    public void showPrizeResolutionPopup(Runnable onClose) {
        SwingUtilities.invokeLater(() -> {
            AdwareResolutionDialog dialog = new AdwareResolutionDialog(frame, onClose);
            dialog.setVisible(true);
        });
    }

    /**
     * Implementación de la transición post-nivel hacia el hub escritorio XP.
     *
     * <p>Delega toda la lógica al {@link com.miJuego.demo.HubTransitionController}
     * para mantener SwingGameView enfocada en la vista del juego.</p>
     */
    @Override
    public void showPostLevelHub(int completedLevel) {
        SwingUtilities.invokeLater(() -> {
            // Verificar que el frame del nivel esté visible antes de hacer fade
            if (!frame.isVisible()) return;
            com.miJuego.demo.HubTransitionController.startPostLevelTransition(frame, completedLevel);
        });
    }

    private static class AdwareResolutionDialog extends JDialog {
        private final Runnable onCloseCallback;
        private Image relicImage = null;

        public AdwareResolutionDialog(Frame parent, Runnable onCloseCallback) {
            super(parent, true);
            this.onCloseCallback = onCloseCallback;
            setUndecorated(true);
            setSize(760, 530);
            setLocationRelativeTo(parent);

            try {
                java.net.URL imgUrl = getClass().getClassLoader().getResource("assets/word/clippy_mcafee_relic.png");
                if (imgUrl != null) {
                    relicImage = javax.imageio.ImageIO.read(imgUrl);
                }
            } catch (Exception ignored) {}

            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Fondo crema-pergamino
                    g2.setColor(new Color(248, 245, 230));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Borde exterior doble dorado
                    g2.setStroke(new BasicStroke(4f));
                    g2.setColor(new Color(184, 134, 11)); // Oro viejo
                    g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
                    
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(new Color(218, 165, 32)); // Oro brillante
                    g2.drawRect(8, 8, getWidth() - 16, getHeight() - 16);

                    // Barra superior azul royal
                    int hbH = 75;
                    g2.setColor(new Color(10, 80, 205));
                    g2.fillRect(10, 10, getWidth() - 20, hbH);
                    g2.setColor(new Color(218, 165, 32));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(10, 10, getWidth() - 20, hbH);

                    // Adornos de esquina dorados
                    g2.setColor(new Color(218, 165, 32));
                    int cs = 14;
                    // Sup Izq
                    g2.fillOval(8, 8, cs, cs);
                    // Sup Der
                    g2.fillOval(getWidth() - 8 - cs, 8, cs, cs);
                    // Inf Izq
                    g2.fillOval(8, getHeight() - 8 - cs, cs, cs);
                    // Inf Der
                    g2.fillOval(getWidth() - 8 - cs, getHeight() - 8 - cs, cs, cs);

                    // Dibujar escudo Word W arriba en el centro (solapa el borde)
                    int sx = getWidth() / 2;
                    int sy = 10;
                    // Wreaths/Hojas alrededor del escudo
                    g2.setColor(new Color(218, 165, 32));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawArc(sx - 35, sy + 5, 20, 25, 120, 240);
                    g2.drawArc(sx + 15, sy + 5, 20, 25, 180, 240);

                    // Escudo
                    int[] shieldX = { sx, sx - 16, sx - 16, sx, sx + 16, sx + 16 };
                    int[] shieldY = { sy - 8, sy + 4, sy + 22, sy + 32, sy + 22, sy + 4 };
                    g2.fillPolygon(shieldX, shieldY, 6);
                    g2.setColor(new Color(10, 80, 205));
                    int[] shieldInnerX = { sx, sx - 12, sx - 12, sx, sx + 12, sx + 12 };
                    int[] shieldInnerY = { sy - 4, sy + 6, sy + 20, sy + 28, sy + 20, sy + 6 };
                    g2.fillPolygon(shieldInnerX, shieldInnerY, 6);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 14));
                    g2.drawString("W", sx - 6, sy + 18);

                    g2.dispose();
                }
            };
            mainPanel.setLayout(null);

            // 1. TÍTULO EN LA BARRA AZUL
            JLabel headerTitle = new JLabel("¡Te has ganado un premio!", JLabel.CENTER);
            headerTitle.setFont(new Font("Tahoma", Font.BOLD, 26));
            headerTitle.setForeground(Color.WHITE);
            headerTitle.setBounds(50, 42, 660, 35);
            mainPanel.add(headerTitle);

            // 2. SUBTÍTULO
            JLabel headerSubtitle = new JLabel("* * *  Has desbloqueado una reliquia especial.  * * *", JLabel.CENTER);
            headerSubtitle.setFont(new Font("Tahoma", Font.BOLD, 12));
            headerSubtitle.setForeground(new Color(100, 100, 120));
            headerSubtitle.setBounds(50, 95, 660, 20);
            mainPanel.add(headerSubtitle);

            // 3. ILUSTRACIÓN CENTRAL (CON MARCO, COLUMNAS, PEDESTAL, CLIPPY CON CAPA Y ESCUDO GLORIOSO)
            JPanel illustrationPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (relicImage != null) {
                        g2.drawImage(relicImage, 0, 0, getWidth(), getHeight(), null);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.setColor(new Color(200, 160, 70));
                        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                        g2.dispose();
                        return;
                    }

                    // Fondo degradado pergamino suave interno
                    GradientPaint gp = new GradientPaint(0, 0, new Color(255, 253, 245), 0, getHeight(), new Color(240, 235, 215));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Marco dorado fino
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(new Color(200, 160, 70));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2 + 10;

                    // A. Dibujar destello/brillo radial detrás de la reliquia
                    float[] dist = { 0.0f, 0.6f, 1.0f };
                    Color[] colors = { new Color(255, 225, 100, 200), new Color(255, 215, 0, 80), new Color(0, 0, 0, 0) };
                    RadialGradientPaint rgp = new RadialGradientPaint(cx, cy - 35, 120, dist, colors);
                    g2.setPaint(rgp);
                    g2.fillOval(cx - 120, cy - 155, 240, 240);
                    
                    // Rayos de luz radiales
                    g2.setColor(new Color(255, 223, 0, 120));
                    g2.setStroke(new BasicStroke(1.2f));
                    for (int i = 0; i < 16; i++) {
                        double angle = i * Math.PI / 8;
                        int rx1 = cx + (int) (18 * Math.cos(angle));
                        int ry1 = (cy - 35) + (int) (18 * Math.sin(angle));
                        int rx2 = cx + (int) (110 * Math.cos(angle));
                        int ry2 = (cy - 35) + (int) (110 * Math.sin(angle));
                        g2.drawLine(rx1, ry1, rx2, ry2);
                    }

                    // B. Columnas griegas / de piedra a los lados
                    drawStoneColumn(g2, 20, 20, 40, getHeight() - 40);
                    drawStoneColumn(g2, getWidth() - 60, 20, 40, getHeight() - 40);

                    // C. Pedestal de piedra en el centro
                    g2.setColor(new Color(175, 175, 185)); // Piedra
                    g2.fillOval(cx - 55, cy + 25, 110, 22);
                    g2.setColor(new Color(135, 135, 145));
                    g2.fillOval(cx - 45, cy + 18, 90, 18);
                    g2.setColor(new Color(200, 200, 210));
                    g2.fillOval(cx - 35, cy + 10, 70, 14);

                    // D. Clippy feliz con capa azul y corona/banda de sabio
                    int charX = cx;
                    int charY = cy - 2;

                    // Capa azul
                    g2.setColor(new Color(30, 80, 200));
                    java.awt.geom.Path2D cape = new java.awt.geom.Path2D.Double();
                    cape.moveTo(charX - 10, charY + 5);
                    cape.quadTo(charX - 35, charY + 15, charX - 30, charY + 32);
                    cape.lineTo(charX + 30, charY + 32);
                    cape.quadTo(charX + 35, charY + 15, charX + 10, charY + 5);
                    cape.closePath();
                    g2.fill(cape);

                    // Cuerpo de clip metálico (Clippy)
                    g2.setColor(new Color(150, 150, 160));
                    g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Dibujar bucle exterior del clip
                    g2.drawArc(charX - 10, charY - 20, 20, 20, 0, 180); // Cabeza
                    g2.drawLine(charX - 10, charY - 10, charX - 10, charY + 20); // Cuerpo izq
                    g2.drawArc(charX - 10, charY + 10, 20, 18, 180, 180); // Bucle bajo
                    g2.drawLine(charX + 10, charY + 19, charX + 10, charY - 8); // Cuerpo der
                    g2.drawArc(charX - 4, charY - 14, 14, 12, 0, 180); // Bucle interno superior
                    g2.drawLine(charX - 4, charY - 8, charX - 4, charY + 10);
                    g2.drawArc(charX - 4, charY + 5, 8, 10, 180, 180);

                    // Ojos googly
                    g2.setColor(Color.WHITE);
                    g2.fillOval(charX - 7, charY - 18, 7, 9);
                    g2.fillOval(charX, charY - 18, 7, 9);
                    g2.setColor(Color.BLACK);
                    g2.fillOval(charX - 5, charY - 14, 3, 3);
                    g2.fillOval(charX + 2, charY - 14, 3, 3);

                    // Cejas
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawLine(charX - 7, charY - 20, charX - 2, charY - 19);
                    g2.drawLine(charX + 1, charY - 19, charX + 6, charY - 20);

                    // Banda de la cabeza / corona de sabio dorada
                    g2.setColor(new Color(218, 165, 32));
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawArc(charX - 11, charY - 19, 22, 10, 20, 140);
                    g2.fillOval(charX - 12, charY - 17, 4, 4);
                    g2.fillOval(charX + 8, charY - 17, 4, 4);

                    // E. Brazos levantando el escudo
                    g2.setColor(new Color(150, 150, 160));
                    g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Brazo Izq
                    g2.drawLine(charX - 8, charY + 6, charX - 16, charY - 20);
                    // Brazo Der
                    g2.drawLine(charX + 8, charY + 6, charX + 16, charY - 20);

                    // F. Escudo de McAfee levantado gloriosamente
                    int shW = 55;
                    int shH = 68;
                    int shX = cx;
                    int shY = cy - 40;

                    // Dibujar el escudo dorado de McAfee
                    java.awt.geom.Path2D shieldOuter = new java.awt.geom.Path2D.Double();
                    shieldOuter.moveTo(shX, shY - shH/2);
                    shieldOuter.quadTo(shX + shW/2, shY - shH/2 + 4, shX + shW/2 - 3, shY + shH/6);
                    shieldOuter.quadTo(shX + shW/2 - 9, shY + shH/2 - 6, shX, shY + shH/2);
                    shieldOuter.quadTo(shX - shW/2 + 9, shY + shH/2 - 6, shX - shW/2 + 3, shY + shH/6);
                    shieldOuter.quadTo(shX - shW/2, shY - shH/2 + 4, shX, shY - shH/2);
                    shieldOuter.closePath();

                    g2.setPaint(new GradientPaint(shX - shW/2, shY - shH/2, new Color(255, 215, 0), shX + shW/2, shY + shH/2, new Color(184, 134, 11)));
                    g2.fill(shieldOuter);

                    // Escudo interior rojo
                    int isW = 43;
                    int isH = 55;
                    java.awt.geom.Path2D shieldInner = new java.awt.geom.Path2D.Double();
                    shieldInner.moveTo(shX, shY - isH/2);
                    shieldInner.quadTo(shX + isW/2, shY - isH/2 + 3, shX + isW/2 - 2, shY + isH/6);
                    shieldInner.quadTo(shX + isW/2 - 7, shY + isH/2 - 5, shX, shY + isH/2);
                    shieldInner.quadTo(shX - isW/2 + 7, shY + isH/2 - 5, shX - isW/2 + 2, shY + isH/6);
                    shieldInner.quadTo(shX - isW/2, shY - isH/2 + 3, shX, shY - isH/2);
                    shieldInner.closePath();

                    g2.setPaint(new GradientPaint(shX - isW/2, shY - isH/2, new Color(230, 20, 20), shX + isW/2, shY + isH/2, new Color(120, 0, 0)));
                    g2.fill(shieldInner);

                    // Letra M en el escudo
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(3.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int my = shY - 11;
                    int mh = 22;
                    int mw = 22;
                    g2.drawLine(shX - mw/2, my + mh, shX - mw/2, my);
                    g2.drawLine(shX - mw/2, my, shX, my + mh/2 + 2);
                    g2.drawLine(shX, my + mh/2 + 2, shX + mw/2, my);
                    g2.drawLine(shX + mw/2, my, shX + mw/2, my + mh);

                    g2.dispose();
                }

                private void drawStoneColumn(Graphics2D g2, int x, int y, int w, int h) {
                    g2.setColor(new Color(190, 190, 195)); // Gris piedra claro
                    g2.fillRect(x, y + 15, w, h - 30); // Fuste

                    // Líneas verticales de relieve
                    g2.setColor(new Color(150, 150, 155));
                    g2.setStroke(new BasicStroke(1f));
                    for (int lx = x + 6; lx < x + w; lx += 8) {
                        g2.drawLine(lx, y + 15, lx, y + h - 15);
                    }

                    // Capitel y Base (Molduras)
                    g2.setColor(new Color(140, 140, 145)); // Gris piedra oscuro
                    g2.fillRect(x - 6, y, w + 12, 15); // Capitel
                    g2.fillRect(x - 6, y + h - 15, w + 12, 15); // Base

                    g2.setColor(new Color(210, 210, 215));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRect(x - 6, y, w + 12, 15);
                    g2.drawRect(x - 6, y + h - 15, w + 12, 15);
                    
                    // Colgar pequeños estandartes azules "W"
                    g2.setColor(new Color(30, 80, 200, 180));
                    int[] flagX = { x + 6, x + 6, x + w - 6, x + w - 6, x + w/2 };
                    int[] flagY = { y + 30, y + 110, y + 110, y + 30, y + 95 };
                    g2.fillPolygon(flagX, flagY, 5);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 10));
                    g2.drawString("W", x + w/2 - 4, y + 55);
                }
            };
            illustrationPanel.setBounds(100, 125, 560, 255);
            mainPanel.add(illustrationPanel);

            // 4. CINTA/BANNER DE LA RELIQUIA
            JPanel bannerRibbon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Cinta dorada
                    g2.setPaint(new GradientPaint(0, 0, new Color(255, 239, 150), 0, getHeight(), new Color(218, 165, 32)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                    // Borde dorado
                    g2.setColor(new Color(184, 134, 11));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                    g2.dispose();
                }
            };
            bannerRibbon.setLayout(new BorderLayout());
            bannerRibbon.setBounds(200, 392, 360, 26);
            
            JLabel reliquiaLbl = new JLabel("Reliquia de Seguridad: Antivirus McAfee", JLabel.CENTER);
            reliquiaLbl.setFont(new Font("Tahoma", Font.BOLD, 12));
            reliquiaLbl.setForeground(new Color(139, 69, 19)); // Marrón café
            bannerRibbon.add(reliquiaLbl, BorderLayout.CENTER);
            mainPanel.add(bannerRibbon);

            // 5. TEXTO DE DESCRIPCIÓN
            JLabel descLabel = new JLabel("<html><center>Un antivirus legendario se ha instalado de forma automática en tu sistema.<br>Te servirá para combatir las amenazas cibernéticas a pesar de no haber aceptado la oferta.</center></html>", JLabel.CENTER);
            descLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
            descLabel.setForeground(new Color(40, 50, 90));
            descLabel.setBounds(50, 424, 660, 40);
            mainPanel.add(descLabel);

            // 6. BOTONES "Aceptar" y "Continuar"
            JButton acceptBtn = new JButton("Aceptar") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(new Color(220, 220, 220));
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(250, 250, 255));
                    } else {
                        g2.setColor(new Color(235, 235, 240));
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    
                    g2.setColor(new Color(10, 80, 205)); // Borde azul
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                    
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            acceptBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
            acceptBtn.setForeground(new Color(10, 80, 205));
            acceptBtn.setContentAreaFilled(false);
            acceptBtn.setFocusPainted(false);
            acceptBtn.setBorder(BorderFactory.createEmptyBorder());
            acceptBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            acceptBtn.setBounds(240, 476, 130, 36);
            acceptBtn.addActionListener(e -> closeDialog());
            mainPanel.add(acceptBtn);

            JButton continueBtn = new JButton("Continuar") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(new Color(180, 180, 180));
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(225, 225, 225));
                    } else {
                        g2.setColor(new Color(210, 210, 210));
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    
                    g2.setColor(new Color(140, 140, 145)); // Borde gris
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                    
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            continueBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
            continueBtn.setForeground(new Color(80, 80, 90));
            continueBtn.setContentAreaFilled(false);
            continueBtn.setFocusPainted(false);
            continueBtn.setBorder(BorderFactory.createEmptyBorder());
            continueBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            continueBtn.setBounds(390, 476, 130, 36);
            continueBtn.addActionListener(e -> closeDialog());
            mainPanel.add(continueBtn);

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    closeDialog();
                }
            });

            setContentPane(mainPanel);
        }

        private void closeDialog() {
            dispose();
            onCloseCallback.run();
        }
    }
}
