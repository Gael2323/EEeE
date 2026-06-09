package com.miJuego.demo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.miJuego.model.ProgresoJuego;

public class MerchantDialog extends JDialog {
    private Image bgImage;
    private Image clippyPortrait;
    
    // Sprites
    private Image wizNeutral, wizDespidiendose, wizHablandoBajo, wizPensativo, wizRevisando;
    private Image currentWizardSprite;
    
    private boolean isClosing = false;
    private double irisRadius = -1;
    private Timer animTimer;
    private Timer idleTimer;

    private enum DialogState {
        INTRO, INTERACT_PROMPT, STORY, TOWER_CHOICE, REACT_IE, REACT_MSN, LORE_MENU, LORE_ANSWER, EXIT, DONE
    }
    private DialogState state = DialogState.INTRO;
    
    private static class DialogLine {
        String speaker;
        String text;
        Image sprite;
        public DialogLine(String s, String t, Image sp) { speaker = s; text = t; sprite = sp; }
    }
    
    private List<DialogLine> currentSequence;
    private int currentLine = 0;
    
    // Choices
    private String[] currentChoices;
    private Rectangle[] choiceBounds;
    private int hoveredChoice = -1;
    
    // Lore Menu tracking
    private boolean[] loreAsked = new boolean[6];
    private int currentLoreQuestion = -1;

    private JPanel panel;

    public MerchantDialog(JFrame parent) {
        super(parent, "Wizard's Refuge - Inside C:\\Recycler\\{DELETED}\\", true);
        
        try {
            bgImage = ImageIO.read(getClass().getClassLoader().getResource("assets/merchant/merchant_bg.jpg"));
            clippyPortrait = ImageIO.read(getClass().getClassLoader().getResource("assets/word/clippy_hablando.png"));
            
            wizNeutral = ImageIO.read(getClass().getClassLoader().getResource("assets/merchant/Wizard_MitadDelCuerpo_Neutro.png"));
            wizDespidiendose = ImageIO.read(getClass().getClassLoader().getResource("assets/merchant/Wizard_MitadDelCuerpo_Despidiendose.png"));
            wizHablandoBajo = ImageIO.read(getClass().getClassLoader().getResource("assets/merchant/Wizard_MitadDelCuerpo_HablandoBajo.png"));
            wizPensativo = ImageIO.read(getClass().getClassLoader().getResource("assets/merchant/Wizard_MitadDelCuerpo_Pensativo.png"));
            wizRevisando = ImageIO.read(getClass().getClassLoader().getResource("assets/merchant/Wizard_MitadDelCuerpo_Revisando unaHistoria.png"));
        } catch (Exception e) {
            System.err.println("Error leyendo imágenes del wizard: " + e.getMessage());
        }

        loadState(DialogState.INTRO);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                double imgW = bgImage != null ? bgImage.getWidth(null) : 1024.0;
                double imgH = bgImage != null ? bgImage.getHeight(null) : 682.0;

                int wWidth = currentWizardSprite != null ? currentWizardSprite.getWidth(null) : 200;
                int wHeight = currentWizardSprite != null ? currentWizardSprite.getHeight(null) : 300;
                
                int renderH = (int)(getHeight() * 0.45);
                int renderW = (int)(wWidth * ((double)renderH / wHeight));
                
                int wx = getWidth() / 2 + 50; 
                int targetBottomY = (int)(500.0 / imgH * getHeight()); 
                int wy = targetBottomY - renderH;
                
                double time = System.currentTimeMillis() / 1000.0;
                int hoverOffset = (int)(Math.sin(time * 2) * 5);

                boolean wizardRendered = false;

                if (currentWizardSprite != null && state != DialogState.INTERACT_PROMPT) {
                    wizardRendered = true;
                    Shape oldClip = g2.getClip();
                    Polygon deskClip = new Polygon();
                    deskClip.addPoint(0, 0);
                    deskClip.addPoint(getWidth(), 0);
                    
                    int px1 = (int)(495.0 / imgW * getWidth());
                    int py1 = (int)(466.0 / imgH * getHeight());
                    int px2 = (int)(825.0 / imgW * getWidth());
                    int py2 = (int)(503.0 / imgH * getHeight());
                    
                    if (px2 != px1) {
                        double slope = (double)(py2 - py1) / (px2 - px1);
                        int rightY = py2 + (int)(slope * (getWidth() - px2));
                        int leftY = py1 - (int)(slope * px1);
                        
                        deskClip.addPoint(getWidth(), rightY);
                        deskClip.addPoint(px2, py2);
                        deskClip.addPoint(px1, py1);
                        deskClip.addPoint(0, leftY);
                        
                        if (oldClip != null) {
                            Area combinedClip = new Area(oldClip);
                            combinedClip.intersect(new Area(deskClip));
                            g2.setClip(combinedClip);
                        } else {
                            g2.setClip(deskClip);
                        }
                    }

                    g2.drawImage(currentWizardSprite, wx, wy + hoverOffset, renderW, renderH, this);
                    g2.setClip(oldClip);
                }

                if (state == DialogState.INTERACT_PROMPT) {
                    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
                    String prompt = "Haz click sobre el Wizard para interactuar.";
                    FontMetrics fm = g2.getFontMetrics();
                    int promptX = (getWidth() - fm.stringWidth(prompt)) / 2;
                    int promptY = getHeight() / 2;
                    g2.setColor(Color.BLACK);
                    g2.drawString(prompt, promptX+2, promptY+2);
                    g2.setColor(Color.YELLOW);
                    g2.drawString(prompt, promptX, promptY);
                }

                if (!isClosing && currentSequence != null && currentLine < currentSequence.size()) {
                    DialogLine line = currentSequence.get(currentLine);
                    if ("Wizard".equals(line.speaker)) {
                        paintWizardBubble(g2, line.text, wx, wy + hoverOffset, renderW, getWidth(), getHeight());
                    } else if (line.speaker != null && !line.speaker.isEmpty()) {
                        paintRetroBox(g2, line.speaker, line.text, getWidth(), getHeight());
                    }
                }

                // Dibujar opciones si las hay
                if (currentChoices != null && currentLine >= currentSequence.size()) {
                    paintChoices(g2, getWidth(), getHeight());
                }

                if (isClosing && irisRadius >= 0) {
                    Area blackRect = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
                    double cx = getWidth() / 2.0;
                    double cy = getHeight() / 2.0;
                    Ellipse2D.Double hole = new Ellipse2D.Double(cx - irisRadius, cy - irisRadius, irisRadius * 2, irisRadius * 2);
                    blackRect.subtract(new Area(hole));
                    g2.setColor(Color.BLACK);
                    g2.fill(blackRect);
                }
                g2.dispose();
            }

            private void paintChoices(Graphics2D g, int w, int h) {
                g.setFont(new Font("Monospaced", Font.BOLD, 18));
                FontMetrics fm = g.getFontMetrics();
                
                int boxW = w - 100;
                int bx = 50;
                int by = h - 50 - (currentChoices.length * 35) - 20;

                g.setColor(new Color(0x222222));
                g.fillRect(bx, by, boxW, currentChoices.length * 35 + 30);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(3));
                g.drawRect(bx, by, boxW, currentChoices.length * 35 + 30);

                if (choiceBounds == null || choiceBounds.length != currentChoices.length) {
                    choiceBounds = new Rectangle[currentChoices.length];
                }

                int cy = by + 30;
                for (int i = 0; i < currentChoices.length; i++) {
                    String text = (i == hoveredChoice ? "> " : "  ") + currentChoices[i];
                    
                    if (state == DialogState.LORE_MENU && i < 6 && loreAsked[i]) {
                        g.setColor(Color.GRAY);
                    } else if (i == hoveredChoice) {
                        g.setColor(Color.YELLOW);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    
                    g.drawString(text, bx + 20, cy);
                    choiceBounds[i] = new Rectangle(bx + 20, cy - 20, boxW - 40, 30);
                    cy += 35;
                }
            }

            private void paintWizardBubble(Graphics2D g2, String text, int wizardX, int wizardY, int wizardW, int w, int h) {
                int bubbleW = 350;
                
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                
                // Pre-calculate wrapped lines to find dynamic height
                java.util.List<String> wrappedLines = new java.util.ArrayList<>();
                String[] explicitLines = text.split("\n");
                for (String explicitLine : explicitLines) {
                    String[] words = explicitLine.split(" ");
                    String currentL = "";
                    for (String word : words) {
                        if (fm.stringWidth(currentL + word) < bubbleW - 40) {
                            currentL += word + " ";
                        } else {
                            wrappedLines.add(currentL);
                            currentL = word + " ";
                        }
                    }
                    wrappedLines.add(currentL);
                }
                
                // Each line takes fm.getHeight(). Adding 40 for top/bottom padding.
                int bubbleH = 40 + (wrappedLines.size() * fm.getHeight());
                if (bubbleH < 100) bubbleH = 100; // Minimum height to look good
                
                int bx = wizardX - bubbleW + 50; 
                // Anchor the bottom of the bubble at wizardY + 60
                int bottomY = wizardY + 60;
                int by = bottomY - bubbleH;
                if (bx < 20) bx = 20;

                g2.setColor(new Color(255, 255, 180));
                g2.fill(new RoundRectangle2D.Float(bx, by, bubbleW, bubbleH, 20, 20));
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(bx, by, bubbleW, bubbleH, 20, 20));

                int tailTargetX = wizardX + wizardW / 4; 
                int tailTargetY = wizardY + 60; 

                Polygon tail = new Polygon();
                tail.addPoint(bx + bubbleW - 60, bottomY);
                tail.addPoint(tailTargetX, tailTargetY);
                tail.addPoint(bx + bubbleW - 40, bottomY);
                g2.setColor(new Color(255, 255, 180));
                g2.fill(tail);
                g2.setColor(Color.BLACK);
                g2.drawLine(bx + bubbleW - 60, bottomY, tailTargetX, tailTargetY);
                g2.drawLine(tailTargetX, tailTargetY, bx + bubbleW - 40, bottomY);

                int tx = bx + 20;
                int ty = by + 20 + fm.getAscent();
                
                for (String line : wrappedLines) {
                    g2.drawString(line, tx, ty);
                    ty += fm.getHeight();
                }
            }

            private void paintRetroBox(Graphics2D g, String speaker, String text, int w, int h) {
                int boxW = w - 100;
                g.setFont(new Font("Monospaced", Font.PLAIN, 16));
                FontMetrics fm = g.getFontMetrics();
                
                int textW = boxW - 40 - ("Clippy".equals(speaker) && clippyPortrait != null ? 120 : 0);
                String[] words = text.split(" ");
                java.util.List<String> lines = new java.util.ArrayList<>();
                String currentL = "";
                for (String word : words) {
                    if (fm.stringWidth(currentL + word) > textW) {
                        lines.add(currentL);
                        currentL = word + " ";
                    } else {
                        currentL += word + " ";
                    }
                }
                lines.add(currentL);
                
                int boxH = Math.max(100, 70 + (lines.size() * 24));
                int bx = 50;
                int by = h - boxH - 30;

                Color bgColor = "Clippy".equals(speaker) ? new Color(0xFFFFAA) : new Color(0xAAFFFF);
                Color borderColor = new Color(0x333333);
                Color innerColor = "Clippy".equals(speaker) ? new Color(0xAAAA66) : new Color(0x66AAAA);

                g.setColor(bgColor);
                g.fillRect(bx, by, boxW, boxH);
                g.setColor(borderColor);
                g.setStroke(new BasicStroke(4));
                g.drawRect(bx, by, boxW, boxH);
                g.setColor(innerColor);
                g.setStroke(new BasicStroke(2));
                g.drawRect(bx + 4, by + 4, boxW - 8, boxH - 8);
                
                int textX = bx + 20;
                if ("Clippy".equals(speaker) && clippyPortrait != null) {
                    g.drawImage(clippyPortrait, bx + 15, by + 15, 70, 70, null);
                    textX += 90;
                }

                g.setFont(new Font("Monospaced", Font.BOLD, 18));
                g.setColor(new Color(0x111111));
                g.drawString(speaker, textX, by + 30);
                g.setFont(new Font("Monospaced", Font.PLAIN, 16));
                int ty = by + 55;
                for (String lineText : lines) {
                    g.drawString(lineText, textX, ty);
                    ty += 24;
                }
            }
        };

        int w = bgImage != null ? bgImage.getWidth(null) : 800;
        int h = bgImage != null ? bgImage.getHeight(null) : 600;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (w > screenSize.width * 0.9) w = (int)(screenSize.width * 0.9);
        if (h > screenSize.height * 0.9) h = (int)(screenSize.height * 0.9);

        panel.setPreferredSize(new Dimension(w, h));

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentChoices != null && currentLine >= currentSequence.size()) {
                    int oldHover = hoveredChoice;
                    hoveredChoice = -1;
                    if (choiceBounds != null) {
                        for (int i = 0; i < choiceBounds.length; i++) {
                            if (choiceBounds[i] != null && choiceBounds[i].contains(e.getPoint())) {
                                hoveredChoice = i; break;
                            }
                        }
                    }
                    if (oldHover != hoveredChoice) panel.repaint();
                }
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isClosing) return;

                if (state == DialogState.INTERACT_PROMPT) {
                    loadState(DialogState.STORY);
                    panel.repaint();
                    return;
                }

                if (currentLine < currentSequence.size() - 1) {
                    currentLine++;
                    Image sp = currentSequence.get(currentLine).sprite;
                    if (sp != null) currentWizardSprite = sp;
                    panel.repaint();
                    return;
                }
                
                if (currentLine == currentSequence.size() - 1) {
                    currentLine++;
                    // Sequence ended, auto-advance
                    if (state == DialogState.INTRO) {
                        loadState(DialogState.INTERACT_PROMPT);
                    } else if (state == DialogState.STORY) {
                        loadState(DialogState.TOWER_CHOICE);
                    } else if (state == DialogState.REACT_IE || state == DialogState.REACT_MSN || state == DialogState.LORE_ANSWER) {
                        loadState(DialogState.LORE_MENU);
                    } else if (state == DialogState.EXIT) {
                        closeDialog();
                    }
                    panel.repaint();
                    return;
                }

                if (currentChoices != null && hoveredChoice >= 0) {
                    handleChoice(hoveredChoice);
                    return;
                }
            }
        });

        idleTimer = new Timer(50, evt -> {
            if (!isClosing) panel.repaint();
        });
        idleTimer.start();

        setContentPane(panel);
        pack();
        setLocationRelativeTo(parent);
    }

    private void handleChoice(int choice) {
        if (state == DialogState.TOWER_CHOICE) {
            if (choice == 0) {
                ProgresoJuego.ieTowerUnlocked = true;
                loadState(DialogState.REACT_IE);
            } else {
                ProgresoJuego.messengerTowerUnlocked = true;
                loadState(DialogState.REACT_MSN);
            }
        } else if (state == DialogState.LORE_MENU) {
            if (choice == 6) {
                loadState(DialogState.EXIT);
            } else {
                loreAsked[choice] = true;
                currentLoreQuestion = choice;
                loadState(DialogState.LORE_ANSWER);
            }
        }
        panel.repaint();
    }

    private void loadState(DialogState s) {
        state = s;
        currentLine = 0;
        currentChoices = null;
        choiceBounds = null;
        hoveredChoice = -1;

        Image N = wizNeutral, P = wizPensativo, H = wizHablandoBajo, R = wizRevisando, D = wizDespidiendose;
        
        switch (state) {
            case INTRO -> {
                currentWizardSprite = N;
                currentSequence = Arrays.asList(
                    new DialogLine("Clippy", "Ahí está. El Wizard.", null),
                    new DialogLine("Clippy", "Tiene respuestas, historias… y una costumbre terrible de cobrar por ambas cosas.", null),
                    new DialogLine("Wizard", "Vaya, vaya…", N),
                    new DialogLine("Wizard", "Si no es mi viejo amigo, el clip más insistente del escritorio.", N),
                    new DialogLine("Clippy", "Hola, Wizard.", null),
                    new DialogLine("Wizard", "Hace años que no escuchaba ese sonido de metal doblado entrando sin pedir permiso.", P),
                    new DialogLine("Clippy", "Necesitamos ayuda.", null),
                    new DialogLine("Wizard", "Eso también lo escuché muchas veces.", H),
                    new DialogLine("Wizard", "“Parece que necesitas ayuda para escribir una carta.”", H),
                    new DialogLine("Clippy", "Era mi trabajo.", null),
                    new DialogLine("Wizard", "Lo decías cada vez que alguien respiraba cerca de un documento.", H),
                    new DialogLine("Clippy", "Tal vez deberías hablar con él directamente.", null),
                    new DialogLine("Clippy", "Haz click sobre el Wizard. Pero cuidado: si empieza a contar una historia muy larga, no lo mires demasiado a los ojos.", null)
                );
            }
            case INTERACT_PROMPT -> {
                currentSequence = new ArrayList<>(); // Vacío, espera clic
            }
            case STORY -> {
                currentWizardSprite = N;
                currentSequence = Arrays.asList(
                    new DialogLine("Wizard", "Ah, un jugador curioso. Excelente.", N),
                    new DialogLine("Wizard", "¿Sabes? Antes, Clippy y yo recorríamos archivos antiguos, manuales olvidados y páginas que tardaban media vida en cargar.", N),
                    new DialogLine("Clippy", "Eran buenos tiempos.", null),
                    new DialogLine("Wizard", "Eran tiempos lentos.", H),
                    new DialogLine("Clippy", "Bueno… Internet no era tan rápido antes.", null),
                    new DialogLine("Wizard", "Internet Explorer era nuestra puerta al mundo. Una ventana azul hacia conocimientos, mapas, historias, foros abandonados y tutoriales escritos por desconocidos.", P),
                    new DialogLine("Wizard", "Pero cada respuesta venía con un precio.", H),
                    new DialogLine("Clippy", "¿Monedas?", null),
                    new DialogLine("Wizard", "No. Espera.", H),
                    new DialogLine("Wizard", "Cargando página… cargando imagen… cargando barra… cargando la paciencia del usuario.", H),
                    new DialogLine("Clippy", "Era parte de la experiencia.", null),
                    new DialogLine("Wizard", "No, Clippy. Era una prueba espiritual.", H),
                    new DialogLine("Wizard", "Y mientras uno esperaba, aparecías tú.", N),
                    new DialogLine("Clippy", "Yo solo intentaba ayudar.", null),
                    new DialogLine("Wizard", "“Parece que necesitas ayuda para buscar información.”\n“Parece que necesitas escribir una carta.”\n“Parece que necesitas entender lo que acabas de leer.”", H),
                    new DialogLine("Clippy", "Bueno, a veces sí necesitaban ayuda.", null),
                    new DialogLine("Wizard", "A veces. Pero tú preguntabas siempre.", H),
                    new DialogLine("Clippy", "…", null),
                    new DialogLine("Wizard", "Aunque debo admitirlo… después llegaron cosas peores.", P),
                    new DialogLine("Clippy", "¿Peores que yo?", null),
                    new DialogLine("Wizard", "Messenger.", H),
                    new DialogLine("Wizard", "Messenger no esperaba. Messenger irrumpía.", H),
                    new DialogLine("Wizard", "BZZZT. Una ventana temblando. Un mensaje urgente que casi nunca era urgente.", H),
                    new DialogLine("Clippy", "Los zumbidos…", null),
                    new DialogLine("Wizard", "Exacto. Internet Explorer era la espera eterna. Messenger era la interrupción inmediata.", N),
                    new DialogLine("Clippy", "O sea… uno te congelaba el tiempo y el otro te sacudía la pantalla.", null),
                    new DialogLine("Wizard", "Ahora empiezas a entender.", N),
                    new DialogLine("Wizard", "De esos viejos tiempos nacieron dos herramientas útiles para sobrevivir a lo que viene.", R),
                    new DialogLine("Wizard", "La primera: Internet Explorer.", R),
                    new DialogLine("Wizard", "Lento, pesado, anticuado… pero perfecto para hacer que tus enemigos también esperen.", R),
                    new DialogLine("Clippy", "Una torre que ralentiza.", null),
                    new DialogLine("Wizard", "Exacto. Una torre de hielo. Congela el avance, alarga el camino, compra tiempo.", N),
                    new DialogLine("Wizard", "La segunda: Messenger.", R),
                    new DialogLine("Wizard", "Ruidoso, eléctrico, imposible de ignorar.", R),
                    new DialogLine("Clippy", "Una torre de rayos.", null),
                    new DialogLine("Wizard", "Una torre de zumbidos. Golpea, salta entre enemigos y les recuerda que alguien quiere llamar su atención.", N),
                    new DialogLine("Wizard", "Así que dime, jugador…", P),
                    new DialogLine("Wizard", "¿Prefieres detener al enemigo con la paciencia infinita de una página que nunca carga…", N),
                    new DialogLine("Wizard", "…o sacudirlo con la ansiedad eléctrica de un zumbido de Messenger?", N)
                );
            }
            case TOWER_CHOICE -> {
                currentSequence = new ArrayList<>();
                currentChoices = new String[]{ "Internet Explorer — Ralentiza enemigos", "Messenger — Descargas eléctricas en cadena" };
            }
            case REACT_IE -> {
                currentWizardSprite = N;
                currentSequence = Arrays.asList(
                    new DialogLine("Clippy", "Internet Explorer…", null),
                    new DialogLine("Clippy", "No era rápido, pero cuando se quedaba cargando, todos quedaban esperando.", null),
                    new DialogLine("Wizard", "Y ahora tus enemigos también.", N)
                );
            }
            case REACT_MSN -> {
                currentWizardSprite = N;
                currentSequence = Arrays.asList(
                    new DialogLine("Clippy", "Messenger…", null),
                    new DialogLine("Clippy", "Esto va a hacer ruido, ¿verdad?", null),
                    new DialogLine("Wizard", "Mucho.", N),
                    new DialogLine("Clippy", "Perfecto. Una torre con ansiedad.", null)
                );
            }
            case LORE_MENU -> {
                currentWizardSprite = P;
                boolean isFirstLore = true;
                for (boolean b : loreAsked) if (b) isFirstLore = false;
                
                if (isFirstLore) {
                    currentSequence = Arrays.asList(
                        new DialogLine("Wizard", "Antes de que partan, puedo responder algunas preguntas.", N),
                        new DialogLine("Clippy", "No es obligatorio. Pero… si alguien entiende por qué este escritorio terminó así, probablemente sea él.", null),
                        new DialogLine("Wizard", "Cobro caro, pero hoy haré una excepción. Me caen bien las tragedias con forma de tutorial.", P)
                    );
                } else {
                    currentSequence = Arrays.asList(
                        new DialogLine("Wizard", "¿Alguna otra duda, jugador?", P)
                    );
                }
                currentChoices = new String[]{
                    "¿Por qué está todo así?", 
                    "¿Quién era el dueño de este escritorio?", 
                    "¿Qué son esos virus que nos atacan?", 
                    "¿Por qué hay aplicaciones convertidas en torres?", 
                    "¿Qué sabes del clon de Clippy?", 
                    "¿Qué es este lugar realmente?", 
                    "No quiero saber más por ahora."
                };
            }
            case LORE_ANSWER -> {
                switch(currentLoreQuestion) {
                    case 0 -> currentSequence = Arrays.asList(
                        new DialogLine("Player", "¿Por qué está todo así?", null),
                        new DialogLine("Wizard", "Ah… la pregunta correcta.", P),
                        new DialogLine("Wizard", "Este escritorio no cayó por una sola tragedia. Cayó por acumulación.", R),
                        new DialogLine("Clippy", "¿Acumulación?", null),
                        new DialogLine("Wizard", "Descargas sospechosas. Juegos pirateados. Instaladores con nombres como “100%_seguro_final_final_ahora_si.exe”.", N),
                        new DialogLine("Clippy", "Eso suena horrible.", null),
                        new DialogLine("Wizard", "El dueño de este lugar no era precisamente amigo de lo legal. Quería juegos gratis, programas gratis, trucos gratis, ventajas gratis.", H),
                        new DialogLine("Wizard", "Y cada vez que abría una puerta prohibida, algo entraba con él.", H),
                        new DialogLine("Clippy", "Virus…", null),
                        new DialogLine("Wizard", "Virus, barras de búsqueda, instaladores falsos, extensiones rotas, asistentes corruptos, procesos escondidos.", N),
                        new DialogLine("Wizard", "Al principio eran molestias. Ventanas raras. Publicidades. Archivos que aparecían donde no debían.", H),
                        new DialogLine("Wizard", "Pero con el tiempo, el sistema dejó de defenderse. Y cuando un lugar deja de defenderse… las cosas que viven en él empiezan a organizarse.", H),
                        new DialogLine("Clippy", "Entonces esto no está infectado.", null),
                        new DialogLine("Wizard", "No, pequeño clip. Esto es peor.", H),
                        new DialogLine("Wizard", "Esto está habitado.", H)
                    );
                    case 1 -> currentSequence = Arrays.asList(
                        new DialogLine("Player", "¿Quién era el dueño de este escritorio?", null),
                        new DialogLine("Wizard", "Un usuario curioso. Impaciente. Bastante imprudente.", P),
                        new DialogLine("Clippy", "Eso describe a muchos usuarios.", null),
                        new DialogLine("Wizard", "Este era especial. Quería todo rápido. Todo gratis. Todo sin leer los avisos.", N),
                        new DialogLine("Clippy", "¿Ni siquiera leía los términos?", null),
                        new DialogLine("Wizard", "Nadie lee los términos, Clippy.", H),
                        new DialogLine("Clippy", "Buen punto.", null),
                        new DialogLine("Wizard", "Pero él iba más lejos. Si una página decía “desactivar antivirus para instalar”, lo hacía. Si un archivo prometía desbloquear un juego completo, lo abría. Si algo brillaba y decía “descargar ahora”, confiaba.", N),
                        new DialogLine("Wizard", "No era malvado. Solo descuidado.", H),
                        new DialogLine("Clippy", "A veces eso alcanza para romper algo.", null),
                        new DialogLine("Wizard", "Exactamente.", N)
                    );
                    case 2 -> currentSequence = Arrays.asList(
                        new DialogLine("Player", "¿Qué son esos virus que nos atacan?", null),
                        new DialogLine("Wizard", "No todos son virus en el mismo sentido.", N),
                        new DialogLine("Clippy", "¿Cómo que no?", null),
                        new DialogLine("Wizard", "Algunos son malware puro: programas hechos para romper, robar o esconderse.", N),
                        new DialogLine("Wizard", "Otros son restos. Pedazos de instalaciones fallidas. Accesos directos sin destino. Archivos temporales que nadie limpió.", R),
                        new DialogLine("Clippy", "¿Y los peores?", null),
                        new DialogLine("Wizard", "Los peores son los que aprendieron a parecer útiles.", H),
                        new DialogLine("Clippy", "Como el que se parecía a mí.", null),
                        new DialogLine("Wizard", "Exacto. Un enemigo que se muestra como amenaza es fácil de reconocer.", N),
                        new DialogLine("Wizard", "Uno que se presenta como ayuda… ese es más peligroso.", H)
                    );
                    case 3 -> currentSequence = Arrays.asList(
                        new DialogLine("Player", "¿Por qué las aplicaciones se pueden usar como torres?", null),
                        new DialogLine("Wizard", "Porque toda aplicación deja una idea detrás.", N),
                        new DialogLine("Clippy", "¿Una idea?", null),
                        new DialogLine("Wizard", "Internet Explorer dejó la espera. Messenger dejó la interrupción. El antivirus dejó la defensa. El reproductor dejó el ritmo. La papelera dejó el descarte.", N),
                        new DialogLine("Clippy", "Entonces no usamos la aplicación como era antes.", null),
                        new DialogLine("Wizard", "No. Usan lo que representa.", N),
                        new DialogLine("Wizard", "En un sistema tan roto, los íconos ya no son simples accesos directos. Son recuerdos comprimidos.", R),
                        new DialogLine("Clippy", "Eso explica por qué una “torre de navegador lento” puede congelar enemigos.", null),
                        new DialogLine("Wizard", "Exactamente. La nostalgia también tiene efectos secundarios.", N)
                    );
                    case 4 -> currentSequence = Arrays.asList(
                        new DialogLine("Player", "¿Qué sabes del clon de Clippy?", null),
                        new DialogLine("Wizard", "Poco. Y eso es lo que me preocupa.", H),
                        new DialogLine("Clippy", "¿Por qué?", null),
                        new DialogLine("Wizard", "Porque todo en este escritorio tiene historia. Incluso los virus. Incluso los errores. Incluso las ventanas que nadie recuerda haber abierto.", N),
                        new DialogLine("Wizard", "Pero tu copia… no aparece en mis registros.", P),
                        new DialogLine("Clippy", "Eso no suena bien.", null),
                        new DialogLine("Wizard", "No. Significa una de dos cosas.", H),
                        new DialogLine("Clippy", "¿Cuáles?", null),
                        new DialogLine("Wizard", "O nació hace muy poco…", H),
                        new DialogLine("Wizard", "…o alguien borró cuidadosamente su historia.", H),
                        new DialogLine("Clippy", "¿Y cuál crees que es?", null),
                        new DialogLine("Wizard", "Si tuviera que apostar, diría que la segunda.", H)
                    );
                    case 5 -> currentSequence = Arrays.asList(
                        new DialogLine("Player", "¿Qué es este lugar realmente?", null),
                        new DialogLine("Wizard", "Un escritorio, sí. Pero también una memoria.", P),
                        new DialogLine("Clippy", "¿Una memoria?", null),
                        new DialogLine("Wizard", "Cada archivo abierto, cada programa instalado, cada error ignorado, cada acceso directo olvidado… todo deja una marca.", N),
                        new DialogLine("Wizard", "El usuario cree que borra cosas. Cree que cerrar una ventana termina una historia.", P),
                        new DialogLine("Wizard", "Pero algunas historias quedan ejecutándose en segundo plano.", H),
                        new DialogLine("Clippy", "Como procesos ocultos.", null),
                        new DialogLine("Wizard", "Exacto.", N),
                        new DialogLine("Wizard", "Este lugar es lo que queda cuando nadie limpia sus errores durante demasiado tiempo.", H)
                    );
                }
                if (currentSequence != null && !currentSequence.isEmpty() && currentSequence.get(0).sprite != null) {
                    currentWizardSprite = currentSequence.get(0).sprite;
                }
            }
            case EXIT -> {
                currentWizardSprite = D;
                currentSequence = Arrays.asList(
                    new DialogLine("Player", "No quiero saber más por ahora.", null),
                    new DialogLine("Wizard", "Sabia decisión. Demasiado conocimiento puede ralentizar más que Internet Explorer.", N),
                    new DialogLine("Clippy", "Tenemos una torre nueva y una pista. Eso ya es más de lo que teníamos hace cinco minutos.", null),
                    new DialogLine("Wizard", "Vuelvan cuando tengan monedas, preguntas o heridas narrativas.", D),
                    new DialogLine("Clippy", "¿Heridas narrativas?", null),
                    new DialogLine("Wizard", "Ya las tendrán.", D)
                );
            }
        }
        
        if (currentSequence != null && !currentSequence.isEmpty()) {
            if (currentSequence.get(0).sprite != null) {
                currentWizardSprite = currentSequence.get(0).sprite;
            }
        } else if (state == DialogState.INTERACT_PROMPT) {
            // Keep current sprite
        } else {
            currentSequence = new ArrayList<>();
        }
    }

    private void closeDialog() {
        isClosing = true;
        irisRadius = Math.hypot(panel.getWidth() / 2.0, panel.getHeight() / 2.0);
        double shrinkSpeed = irisRadius / 20.0;
        animTimer = new Timer(30, evt -> {
            irisRadius -= shrinkSpeed;
            if (irisRadius <= 0) {
                irisRadius = 0;
                animTimer.stop();
                dispose();
            } else {
                panel.repaint();
            }
        });
        animTimer.start();
    }
}
