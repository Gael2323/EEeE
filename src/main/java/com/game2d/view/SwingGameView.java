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

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
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

    private final JFrame frame;
    private final GamePanel gamePanel;
    private final MessageToastOverlay messageOverlay;
    private final NavBarPanel navBarPanel;
    private final MenuModal menuModal;
    private ViewListener listener;

    // Componentes de la Barra de Tareas XP (Abajo)
    private final JLabel startBtnLabel = new JLabel("  start");
    private final JProgressBar xpProgressBar = new JProgressBar();
    private final JLabel clockLabel = new JLabel();

    // Componentes de la Barra Lateral Izquierda
    private final JPanel leftSidebar = new JPanel();
    private final JPanel[] shopTowerCells = new JPanel[8];
    private final JLabel waveLabel = new JLabel("Oleada: —");
    private final JLabel waveEnemiesLabel = new JLabel("Enemigos: —");
    private final JProgressBar waveProgressBar = new JProgressBar();
    private final JButton startWaveBtn = new JButton("Iniciar Oleada");

    // Componentes de la Barra Lateral Derecha
    private final JPanel rightSidebar = new JPanel();
    private final JLabel detailNameLabel = new JLabel("Ninguna seleccionada", JLabel.CENTER);
    private final JLabel detailIconLabel = new JLabel();
    private final JLabel detailStatLabel = new JLabel("<html><center>Haz click en una torre construida<br>para ver su información.</center></html>", JLabel.CENTER);
    private final JButton upgradeBtn = new JButton("Mejorar (—)");
    private final JButton sellBtn = new JButton("Vender (—)");
    private final MinimapPanel minimapPanel = new MinimapPanel();
    
    // Panel de Notificaciones (Log de eventos)
    private final JPanel notifListPanel = new JPanel();
    private final List<String> notificationsList = new ArrayList<>();

    public SwingGameView() {
        this(ImageResolver.createDefault(), BackgroundSettings.getInstance());
    }

    public SwingGameView(ImageResolver imageResolver, BackgroundSettings background) {
        frame = new JFrame("Document1 - Tower Defense [Nivel 1: El Documento]");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.setMinimumSize(FRAME_SIZE);
        frame.setResizable(false);

        // ── 1. HUD Superior (Cabecera de Word) ──────────────────────
        navBarPanel = new NavBarPanel();
        navBarPanel.setPreferredSize(new Dimension(FRAME_SIZE.width, MENU_BAR_HEIGHT));
        navBarPanel.setMinimumSize(new Dimension(FRAME_SIZE.width, MENU_BAR_HEIGHT));

        // ── 2. Lienzo Central (El mapa) ─────────────────────────────
        gamePanel = new GamePanel(imageResolver, background);
        gamePanel.setPreferredSize(GAME_SIZE);
        gamePanel.setMinimumSize(GAME_SIZE);

        messageOverlay = new MessageToastOverlay(gamePanel);
        menuModal = new MenuModal(frame);

        // ── 3. Construir Barras Laterales y Barra de Tareas ──────────
        buildLeftSidebar();
        buildRightSidebar();
        JPanel taskbar = buildTaskbar();

        // ── 4. Ensamblar en el Frame Principal ───────────────────────
        frame.add(navBarPanel, BorderLayout.NORTH);
        frame.add(leftSidebar, BorderLayout.WEST);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(rightSidebar, BorderLayout.EAST);
        frame.add(taskbar, BorderLayout.SOUTH);

        frame.setSize(FRAME_SIZE);
        frame.setLocationRelativeTo(null);

        wireInput();
        startClock();
    }

    // ── BARRA LATERAL IZQUIERDA: Tienda de Torres y Oleada ────────────────────
    private void buildLeftSidebar() {
        leftSidebar.setLayout(new BoxLayout(leftSidebar, BoxLayout.Y_AXIS));
        leftSidebar.setPreferredSize(new Dimension(185, 0));
        leftSidebar.setBackground(new Color(212, 228, 252)); // Azul XP suave
        leftSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(160, 180, 210)));

        // --- MÓDULO 1: Tienda de Torres ---
        XPWindow shopWin = new XPWindow("Tienda de Torres", getDocIcon());
        shopWin.setPreferredSize(new Dimension(175, 334));
        shopWin.setMaximumSize(new Dimension(175, 334));
        shopWin.setMinimumSize(new Dimension(175, 334));
        shopWin.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel shopContent = shopWin.getContentPanel();
        shopContent.setLayout(new GridLayout(4, 2, 4, 4));
        shopContent.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        String[] towerNames = { "Común", "Área", "Cañón", "Fuerte", "Fuego", "Hielo", "Eléctrica", "McAfee" };
        int[] towerCosts = { 100, 150, 200, 250, 180, 150, 220, 175 };

        for (int i = 0; i < 8; i++) {
            final int idx = i;
            JPanel cell = new JPanel(new BorderLayout(2, 2));
            cell.setBackground(Color.WHITE);
            cell.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235), 1));
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel iconLbl = new JLabel(getTowerIconSmall(i + 1), JLabel.CENTER);
            JLabel nameLbl = new JLabel(towerNames[i], JLabel.CENTER);
            nameLbl.setFont(new Font("Tahoma", Font.BOLD, 10));
            nameLbl.setForeground(new Color(50, 70, 110));

            JLabel costLbl = new JLabel(towerCosts[i] + " O", JLabel.CENTER);
            costLbl.setFont(new Font("Tahoma", Font.BOLD, 9));
            costLbl.setForeground(new Color(180, 110, 0));

            cell.add(iconLbl, BorderLayout.NORTH);
            cell.add(nameLbl, BorderLayout.CENTER);
            cell.add(costLbl, BorderLayout.SOUTH);

            // Bindeo de selección de torre al clickear
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (listener != null) {
                        listener.onKeyPressed(KeyEvent.VK_1 + idx, String.valueOf(idx + 1));
                    }
                }
            });

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

        detailNameLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        detailNameLabel.setForeground(new Color(20, 80, 140));
        detailNameLabel.setHorizontalAlignment(JLabel.CENTER);
        infoContent.add(detailNameLabel, BorderLayout.NORTH);

        JPanel detailsCenter = new JPanel(new BorderLayout(4, 4));
        detailsCenter.setOpaque(false);
        detailIconLabel.setHorizontalAlignment(JLabel.CENTER);
        detailsCenter.add(detailIconLabel, BorderLayout.WEST);

        detailStatLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        detailStatLabel.setForeground(new Color(50, 50, 50));
        detailsCenter.add(detailStatLabel, BorderLayout.CENTER);
        infoContent.add(detailsCenter, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanel.setOpaque(false);

        upgradeBtn.setFont(new Font("Tahoma", Font.BOLD, 9));
        upgradeBtn.setBackground(new Color(210, 245, 215));
        upgradeBtn.setForeground(new Color(20, 110, 40));
        upgradeBtn.setFocusPainted(false);
        upgradeBtn.setFocusable(false);
        upgradeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        upgradeBtn.setEnabled(false);
        upgradeBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onKeyPressed(KeyEvent.VK_U, "U");
            }
        });

        sellBtn.setFont(new Font("Tahoma", Font.BOLD, 9));
        sellBtn.setBackground(new Color(255, 220, 220));
        sellBtn.setForeground(new Color(160, 30, 30));
        sellBtn.setFocusPainted(false);
        sellBtn.setFocusable(false);
        sellBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sellBtn.setEnabled(false);
        sellBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onKeyPressed(KeyEvent.VK_S, "S");
            }
        });

        btnPanel.add(upgradeBtn);
        btnPanel.add(sellBtn);
        infoContent.add(btnPanel, BorderLayout.SOUTH);

        // --- MÓDULO 2: Notificaciones ---
        XPWindow notifWin = new XPWindow("Notificaciones", getDocIcon());
        notifWin.setPreferredSize(new Dimension(185, 125));
        notifWin.setMaximumSize(new Dimension(185, 125));
        notifWin.setMinimumSize(new Dimension(185, 125));
        notifWin.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel notifContent = notifWin.getContentPanel();
        notifContent.setLayout(new BorderLayout());
        notifListPanel.setLayout(new BoxLayout(notifListPanel, BoxLayout.Y_AXIS));
        notifListPanel.setBackground(Color.WHITE);
        notifListPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

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
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setPreferredSize(new Dimension(FRAME_SIZE.width, 34));
        bar.setBackground(new Color(36, 95, 215)); // Azul Taskbar Luna XP
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 110, 240))); // Highlight arriba

        // 1. Botón start verde XP
        startBtnLabel.setFont(new Font("Trebuchet MS", Font.ITALIC | Font.BOLD, 15));
        startBtnLabel.setForeground(Color.WHITE);
        startBtnLabel.setOpaque(true);
        startBtnLabel.setBackground(new Color(60, 160, 60)); // Verde Start
        startBtnLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(40, 110, 40)),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        startBtnLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // El botón start abre la consola de desarrollador como easter egg
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

        JLabel xpLabel = new JLabel("NIVEL PROGRESS: ");
        xpLabel.setFont(new Font("Tahoma", Font.BOLD, 10));
        xpLabel.setForeground(Color.WHITE);

        xpProgressBar.setPreferredSize(new Dimension(420, 16));
        xpProgressBar.setForeground(new Color(46, 204, 113));
        xpProgressBar.setBackground(new Color(50, 50, 50));
        xpProgressBar.setStringPainted(true);
        xpProgressBar.setFont(new Font("Tahoma", Font.BOLD, 9));
        xpProgressBar.setForeground(new Color(0, 200, 80));

        xpProgressPanel.add(xpLabel);
        xpProgressPanel.add(xpProgressBar);
        bar.add(xpProgressPanel, BorderLayout.CENTER);

        // 3. System Tray (Esquina derecha con Reloj)
        JPanel tray = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        tray.setBackground(new Color(15, 117, 233)); // Azul sutilmente más claro
        tray.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(10, 90, 190)));

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

        // ── A. Actualizar Tienda de Torres (Left) ──
        int selectedType = juego.getSelectedTowerType();
        int[] towerCosts = { 100, 150, 200, 250, 180, 150, 220, 175 };

        for (int i = 0; i < 8; i++) {
            JPanel cell = shopTowerCells[i];
            JLabel costLbl = (JLabel) cell.getComponent(2);

            // Resaltar la torre seleccionada
            if (i == selectedType - 1) {
                cell.setBackground(new Color(254, 198, 109)); // Naranja seleccionado
                cell.setBorder(BorderFactory.createLineBorder(new Color(242, 149, 54), 1));
            } else {
                cell.setBackground(Color.WHITE);
                cell.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235), 1));
            }

            // Validar si el jugador puede costearla
            if (gold >= towerCosts[i]) {
                costLbl.setForeground(new Color(180, 110, 0)); // Dorado/marrón normal
            } else {
                costLbl.setForeground(Color.RED); // Rojo si no le alcanza
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
            detailNameLabel.setText("Ninguna seleccionada");
            detailIconLabel.setIcon(null);
            detailStatLabel.setText("<html><center>Haz click en una torre construida<br>para ver su información.</center></html>");
            upgradeBtn.setText("Mejorar (—)");
            upgradeBtn.setEnabled(false);
            sellBtn.setText("Vender (—)");
            sellBtn.setEnabled(false);
        } else {
            String typeName = selectedTower.getTowertype();
            int currentLvl = selectedTower.getNivelMejora();
            double range = selectedTower.getRango();
            double fireSpeed = selectedTower.getTiempoRecarga() / 1000.0;
            double damage = getDamageFor(selectedTower);

            int upgradeCost = (int) (selectedTower.GetCostoTorre() * 0.5);
            int sellReward = (int) (selectedTower.GetCostoTorre() * 0.5);

            detailNameLabel.setText("Torre " + typeName + " (Lvl " + currentLvl + ")");
            detailIconLabel.setIcon(getTowerIconLarge(selectedTower));

            String statText = String.format(
                "<html><b>Daño:</b> %.1f<br><b>Rango:</b> %.1f celda<br><b>Recarga:</b> %.2fs</html>",
                damage, range, fireSpeed
            );
            detailStatLabel.setText(statText);

            upgradeBtn.setText("Mejorar (" + upgradeCost + ")");
            upgradeBtn.setEnabled(gold >= upgradeCost);
            sellBtn.setText("Vender (" + sellReward + ")");
            sellBtn.setEnabled(true);
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
                JLabel lbl = new JLabel(n);
                lbl.setFont(new Font("Tahoma", Font.PLAIN, 10));
                lbl.setForeground(new Color(60, 70, 90));
                notifListPanel.add(lbl);
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

    private ImageIcon getTowerIconSmall(int type) {
        String path = (type == 8) ? "assets/ingame/torremc_reposo.png" : "assets/ingame/torrecomun4.png";
        try {
            java.net.URL imgUrl = getClass().getClassLoader().getResource(path);
            if (imgUrl != null) {
                java.awt.Image img = javax.imageio.ImageIO.read(imgUrl);
                return new ImageIcon(img.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ImageIcon getTowerIconLarge(Torre t) {
        String path = (t instanceof TorreMcAfee) ? "assets/ingame/torremc_reposo.png" : "assets/ingame/torrecomun0.png";
        try {
            java.net.URL imgUrl = getClass().getClassLoader().getResource(path);
            if (imgUrl != null) {
                java.awt.Image img = javax.imageio.ImageIO.read(imgUrl);
                return new ImageIcon(img.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── INPUT ROUTING Y DELEGATE DE CONTROLLER ─────────────────────────────────
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

            @Override
            public void mouseMoved(MouseEvent e) {
                float wx = gamePanel.toWorldX(e.getX());
                float wy = gamePanel.toWorldY(e.getY());
                int ix = (int) wx;
                int iy = (int) wy;
                if (ix >= 0 && ix < 20 && iy >= 0 && iy < 15) {
                    com.miJuego.model.ActualTowerContext.setHoverX(ix);
                    com.miJuego.model.ActualTowerContext.setHoverY(iy);
                } else {
                    com.miJuego.model.ActualTowerContext.setHoverX(-1);
                    com.miJuego.model.ActualTowerContext.setHoverY(-1);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                com.miJuego.model.ActualTowerContext.setHoverX(-1);
                com.miJuego.model.ActualTowerContext.setHoverY(-1);
            }
        };
        gamePanel.addMouseListener(mouse);
        gamePanel.addMouseMotionListener(mouse);

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
    public void setViewportSize(int widthPx, int heightPx) {}

    @Override
    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
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
            setBorder(BorderFactory.createLineBorder(new Color(0, 84, 227), 2)); // Borde azul XP

            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setBackground(new Color(0, 84, 227));
            titleBar.setPreferredSize(new Dimension(0, 22));
            titleBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            titleLabel = new JLabel(title, titleIcon, JLabel.LEFT);
            titleLabel.setFont(new Font("Tahoma", Font.BOLD, 10));
            titleLabel.setForeground(Color.WHITE);
            titleBar.add(titleLabel, BorderLayout.WEST);

            JButton closeBtn = new JButton("✕");
            closeBtn.setFont(new Font("Tahoma", Font.BOLD, 8));
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setBackground(new Color(230, 80, 80));
            closeBtn.setBorder(BorderFactory.createLineBorder(new Color(180, 50, 50)));
            closeBtn.setFocusPainted(false);
            closeBtn.setFocusable(false);
            closeBtn.setPreferredSize(new Dimension(14, 14));
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
     * Renderiza un minimapa simplificado en tiempo real con el trazado y las entidades.
     */
    private static class MinimapPanel extends JPanel {
        private List<float[]> path = new ArrayList<>();
        private List<Point2D> towers = new ArrayList<>();
        private List<Point2D> enemies = new ArrayList<>();

        MinimapPanel() {
            setPreferredSize(new Dimension(170, 110));
            setBackground(new Color(240, 248, 240));
            setBorder(BorderFactory.createLineBorder(new Color(180, 200, 180), 1));
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

            float sx = getWidth() / 20f;
            float sy = getHeight() / 15f;

            // Dibujar el camino como una pista color tierra
            if (path.size() > 1) {
                g2.setColor(new Color(205, 175, 140));
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < path.size() - 1; i++) {
                    float[] p1 = path.get(i);
                    float[] p2 = path.get(i + 1);
                    g2.drawLine(Math.round(p1[0] * sx), Math.round(p1[1] * sy),
                               Math.round(p2[0] * sx), Math.round(p2[1] * sy));
                }
            }

            // Dibujar torres (puntos verdes)
            g2.setColor(new Color(30, 150, 60));
            for (Point2D t : towers) {
                int tx = Math.round(t.x * sx);
                int ty = Math.round(t.y * sy);
                g2.fillOval(tx - 3, ty - 3, 6, 6);
            }

            // Dibujar enemigos (puntos rojos)
            g2.setColor(new Color(230, 40, 40));
            for (Point2D e : enemies) {
                int ex = Math.round(e.x * sx);
                int ey = Math.round(e.y * sy);
                g2.fillOval(ex - 2, ey - 2, 4, 4);
            }

            g2.dispose();
        }
    }
}
