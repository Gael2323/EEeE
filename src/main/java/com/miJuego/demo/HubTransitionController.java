package com.miJuego.demo;

import com.miJuego.model.ProgresoJuego;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Controla toda la secuencia de transición post-nivel hacia el hub escritorio XP.
 */
public class HubTransitionController {

    public enum State {
        FADE_OUT_FROM_LEVEL,
        SWITCH_TO_DESKTOP,
        DESKTOP_FADE_IN,
        
        CLIPPY_MOVING_TO_CENTER,
        WAITING_CORRUPT_DIALOG,
        CLIPPY_MOVING_TO_BIN,
        RECYCLE_LOCK_BREAK,
        CLIPPY_ENTER_BIN,
        DELAY_NORMAL_CLIPPY,
        NORMAL_CLIPPY_APPEARING,
        WAITING_NORMAL_DIALOG,

        HUB_IDLE
    }

    private State currentState = State.FADE_OUT_FROM_LEVEL;
    private final int completedLevel;

    private final JFrame ownerFrame;
    private DesktopHubPanel hubPanel;
    private FadeGlassPane fadeGlass;

    private static final int TICK_MS = 16;
    private int tick = 0;
    private Timer animTimer;

    // Ticks para cada fase
    private static final int FADE_TICKS = 45;
    private static final int CLIPPY_MOVE_CENTER_TICKS = 60;
    private static final int CLIPPY_MOVE_BIN_TICKS = 60;
    private static final int LOCK_BREAK_TICKS = 45;
    private static final int ENTER_BIN_TICKS = 30;
    private static final int DELAY_TICKS = 120; // ~2 seconds
    private static final int NORMAL_APPEAR_TICKS = 60;

    // Coordenadas relativas guardadas
    private float wordRelX, wordRelY;
    private float binRelX, binRelY;
    private float centerRelX = 0.5f, centerRelY = 0.5f;

    public HubTransitionController(JFrame ownerFrame, int completedLevel) {
        this.ownerFrame = ownerFrame;
        this.completedLevel = completedLevel;
    }

    public void start() {
        fadeGlass = new FadeGlassPane();
        ownerFrame.setGlassPane(fadeGlass);
        fadeGlass.setVisible(true);

        currentState = State.FADE_OUT_FROM_LEVEL;
        tick = 0;

        animTimer = new Timer(TICK_MS, e -> advance());
        animTimer.start();
    }

    private void advance() {
        switch (currentState) {
            case FADE_OUT_FROM_LEVEL     -> tickFadeOut();
            case SWITCH_TO_DESKTOP       -> tickSwitchToDesktop();
            case DESKTOP_FADE_IN         -> tickFadeIn();
            
            case CLIPPY_MOVING_TO_CENTER -> tickMoveToCenter();
            case WAITING_CORRUPT_DIALOG  -> animTimer.stop(); // Se reanuda por callback de usuario
            case CLIPPY_MOVING_TO_BIN    -> tickMoveToBin();
            case RECYCLE_LOCK_BREAK      -> tickLockBreak();
            case CLIPPY_ENTER_BIN        -> tickEnterBin();
            case DELAY_NORMAL_CLIPPY     -> tickDelay();
            case NORMAL_CLIPPY_APPEARING -> tickNormalAppear();
            case WAITING_NORMAL_DIALOG   -> animTimer.stop(); // Se reanuda por callback
            
            case HUB_IDLE                -> animTimer.stop();
        }
        tick++;
    }

    private void tickFadeOut() {
        float alpha = Math.min(1f, (float) tick / FADE_TICKS);
        fadeGlass.setAlpha(alpha);
        if (tick >= FADE_TICKS) transitionTo(State.SWITCH_TO_DESKTOP);
    }

    private void tickSwitchToDesktop() {
        hubPanel = new DesktopHubPanel();
        configureHubCallbacks();
        hubPanel.setSize(ownerFrame.getContentPane().getSize());
        hubPanel.doLayout();

        ownerFrame.getContentPane().removeAll();
        ownerFrame.getContentPane().add(hubPanel);
        ownerFrame.getContentPane().revalidate();
        ownerFrame.getContentPane().repaint();

        // Calcular coordenadas una sola vez
        int w = hubPanel.getWidth() > 0 ? hubPanel.getWidth() : 1280;
        int h = hubPanel.getHeight() > 0 ? hubPanel.getHeight() : 760;
        Point wp = hubPanel.getWordIconCenter();
        Point bp = hubPanel.getRecycleBinIconCenter();
        wordRelX = (float) wp.x / w;
        wordRelY = (float) wp.y / h;
        // Offset the target position for Corrupt Clippy so it doesn't cover the lock animation
        binRelX  = (float) (bp.x - 70) / w;
        binRelY  = (float) (bp.y - 40) / h;

        transitionTo(State.DESKTOP_FADE_IN);
    }

    private void tickFadeIn() {
        float alpha = Math.max(0f, 1f - (float) tick / FADE_TICKS);
        fadeGlass.setAlpha(alpha);

        if (tick >= FADE_TICKS) {
            fadeGlass.setVisible(false);
            ownerFrame.setGlassPane(new JPanel());

            if (completedLevel == 1 && !ProgresoJuego.postLevel1HubAnimationSeen) {
                hubPanel.setAnimPhase(DesktopHubPanel.AnimPhase.CLIPPY_MOVING);
                transitionTo(State.CLIPPY_MOVING_TO_CENTER);
            } else {
                hubPanel.setIdleClippy(true);
                transitionTo(State.HUB_IDLE);
            }
        }
    }

    private void tickMoveToCenter() {
        float progress = Math.min(1f, (float) tick / CLIPPY_MOVE_CENTER_TICKS);
        float ease = easeInOut(progress);
        
        float rx = wordRelX + (centerRelX - wordRelX) * ease;
        float ry = wordRelY + (centerRelY - wordRelY) * ease;
        
        hubPanel.setClippyPosition(rx, ry);

        if (tick >= CLIPPY_MOVE_CENTER_TICKS) {
            transitionTo(State.WAITING_CORRUPT_DIALOG);
            hubPanel.showDialog("Clippy Corrupto", "Ahora si... Con este poder voy a poder liberarme.", true, () -> {
                // Callback cuando el usuario avanza el diálogo
                hubPanel.closeDialog();
                transitionTo(State.CLIPPY_MOVING_TO_BIN);
                animTimer.start();
            });
        }
    }

    private void tickMoveToBin() {
        float progress = Math.min(1f, (float) tick / CLIPPY_MOVE_BIN_TICKS);
        float ease = easeInOut(progress);
        
        float rx = centerRelX + (binRelX - centerRelX) * ease;
        float ry = centerRelY + (binRelY - centerRelY) * ease;
        
        hubPanel.setClippyPosition(rx, ry);

        if (tick >= CLIPPY_MOVE_BIN_TICKS) {
            hubPanel.setAnimPhase(DesktopHubPanel.AnimPhase.LOCK_BREAK);
            transitionTo(State.RECYCLE_LOCK_BREAK);
        }
    }

    private void tickLockBreak() {
        float progress = Math.min(1f, (float) tick / LOCK_BREAK_TICKS);
        // Alpha del candado va de 1 a 0
        hubPanel.setLockBreakAlpha(1f - progress);

        if (tick >= LOCK_BREAK_TICKS) {
            // Desbloqueamos iconos
            ProgresoJuego.unlockAfterLevel(completedLevel);
            ProgresoJuego.postLevel1HubAnimationSeen = true; // Prevents re-running

            hubPanel.unlockRecycleBinAndWizard();
            hubPanel.setClippyPosition(binRelX, binRelY); // Mantenerlo visualmente en la papelera
            transitionTo(State.CLIPPY_ENTER_BIN);
        }
    }

    private void tickEnterBin() {
        float progress = Math.min(1f, (float) tick / ENTER_BIN_TICKS);
        hubPanel.setClippyCorruptAlpha(1f - progress); // Fade out del corrupto

        if (tick >= ENTER_BIN_TICKS) {
            hubPanel.setAnimPhase(DesktopHubPanel.AnimPhase.FINISHED);
            hubPanel.setClippyPosition(-1f, -1f); // Ocultarlo por completo
            transitionTo(State.DELAY_NORMAL_CLIPPY);
        }
    }

    private void tickDelay() {
        if (tick >= DELAY_TICKS) {
            transitionTo(State.NORMAL_CLIPPY_APPEARING);
        }
    }

    private void tickNormalAppear() {
        float progress = Math.min(1f, (float) tick / NORMAL_APPEAR_TICKS);
        float ease = easeInOut(progress);
        
        // Sale de Word hacia la derecha
        float targetX = wordRelX + 0.15f; 
        float rx = wordRelX + (targetX - wordRelX) * ease;
        float ry = wordRelY;
        
        hubPanel.setClippyNormalPosition(rx, ry);

        if (tick >= NORMAL_APPEAR_TICKS) {
            transitionTo(State.WAITING_NORMAL_DIALOG);
            startPreWizardDialogue();
        }
    }

    private void startPreWizardDialogue() {
        hubPanel.showDialog("Clippy", "—Bueno… eso salió peor de lo que esperaba.", false, () -> {
            hubPanel.showDialog("Clippy", "—Primero aparece un clon mío. Después intenta evitar que revise los procesos. Y ahora se lleva el Administrador de tareas.", false, () -> {
                hubPanel.showDialog("Clippy", "—No quiero sonar dramático, pero… cuando algo le tiene miedo al Administrador de tareas, generalmente no es algo bueno.", false, () -> {
                    hubPanel.showDialog("Clippy", "—Espera… ¿ese ícono sigue ahí?", false, () -> {
                        hubPanel.showDialog("Clippy", "—¡No puede ser! Pensé que lo habían desinstalado hace años.", false, () -> {
                            String[] choices = {"¿Qué es?", "No tenemos tiempo.", "¿Es seguro?"};
                            hubPanel.showChoiceDialog("Jugador", "Elige una opción:", false, choices, (choiceIndex) -> {
                                if (choiceIndex == 0) {
                                    // ¿Qué es?
                                    hubPanel.showDialog("Clippy", "—Es un viejo amigo. Bueno… “amigo” es una palabra fuerte.", false, () -> {
                                        hubPanel.showDialog("Clippy", "—Es más como una enciclopedia con barba que cobra por hablar.", false, () -> {
                                            hubPanel.showDialog("Clippy", "—Le dicen el Wizard. Sabe cosas. Cosas antiguas. Cosas raras. Cosas que nadie debería saber y que aun así cuenta si le pagás lo suficiente.", false, () -> {
                                                finishPreWizardDialogue();
                                            });
                                        });
                                    });
                                } else if (choiceIndex == 1) {
                                    // No tenemos tiempo.
                                    hubPanel.showDialog("Clippy", "—Justamente por eso deberíamos ir.", false, () -> {
                                        hubPanel.showDialog("Clippy", "—Yo puedo reconocer un error común, una ventana falsa, incluso un asistente molesto a distancia.", false, () -> {
                                            hubPanel.showDialog("Clippy", "—Pero lo que nos atacó… no parecía solo un programa corrupto. Parecía algo que sabía actuar como yo.", false, () -> {
                                                finishPreWizardDialogue();
                                            });
                                        });
                                    });
                                } else {
                                    // ¿Es seguro?
                                    hubPanel.showDialog("Clippy", "—Seguro, seguro… no sé.", false, () -> {
                                        hubPanel.showDialog("Clippy", "—Pero si algo raro está pasando en este escritorio, él probablemente ya escuchó una historia sobre eso.", false, () -> {
                                            hubPanel.showDialog("Clippy", "—Y si no la escuchó… va a querer escucharla.", false, () -> {
                                                finishPreWizardDialogue();
                                            });
                                        });
                                    });
                                }
                            });
                        });
                    });
                });
            });
        });
    }

    private void finishPreWizardDialogue() {
        hubPanel.closeDialog();
        hubPanel.setClippyNormalPosition(-1f, -1f); 
        hubPanel.setIdleClippy(true); 
        transitionTo(State.HUB_IDLE);
        animTimer.start();
    }

    private void transitionTo(State newState) {
        currentState = newState;
        tick = 0;
    }

    private float easeInOut(float t) {
        if (t < 0.5f) return 4f * t * t * t;
        float f = 2f * t - 2f;
        return 0.5f * f * f * f + 1f;
    }

    private void configureHubCallbacks() {
        if (hubPanel == null) return;

        hubPanel.setOnWordAction(() -> {
            // Animación de fundido a negro (Fade In)
            class FadeState { float alpha = 0f; }
            final FadeState state = new FadeState();
            
            JPanel fadePanel = new JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    super.paintComponent(g);
                    g.setColor(new java.awt.Color(0, 0, 0, (int) (state.alpha * 255)));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            fadePanel.setOpaque(false);
            ownerFrame.setGlassPane(fadePanel);
            fadePanel.setVisible(true);

            javax.swing.Timer fadeTimer = new javax.swing.Timer(30, null);
            fadeTimer.addActionListener(e -> {
                state.alpha += 0.05f;
                if (state.alpha >= 1f) {
                    state.alpha = 1f;
                    fadeTimer.stop();
                    ownerFrame.setVisible(false);
                    fadePanel.setVisible(false);
                    WordLevelLauncher.launch();
                } else {
                    fadePanel.repaint();
                }
            });
            fadeTimer.start();
        });

        hubPanel.setOnRecycleBinAction(() -> {
            // Animación de fundido a negro (Fade In)
            class FadeState { float alpha = 0f; }
            final FadeState state = new FadeState();
            
            JPanel fadePanel = new JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    super.paintComponent(g);
                    g.setColor(new java.awt.Color(0, 0, 0, (int) (state.alpha * 255)));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            fadePanel.setOpaque(false);
            ownerFrame.setGlassPane(fadePanel);
            fadePanel.setVisible(true);

            javax.swing.Timer fadeTimer = new javax.swing.Timer(30, null);
            fadeTimer.addActionListener(e -> {
                state.alpha += 0.05f;
                if (state.alpha >= 1f) {
                    state.alpha = 1f;
                    fadeTimer.stop();
                    ownerFrame.setVisible(false);
                    fadePanel.setVisible(false);
                    RecycleBinLevelLauncher.launch();
                } else {
                    fadePanel.repaint();
                }
            });
            fadeTimer.start();
        });

        hubPanel.setOnWizardAction(() -> {
            MerchantDialog dialog = new MerchantDialog(ownerFrame);
            dialog.setVisible(true);
        });
    }

    public static void startPostLevelTransition(JFrame frame, int completedLevel) {
        SwingUtilities.invokeLater(() -> {
            HubTransitionController controller = new HubTransitionController(frame, completedLevel);
            controller.start();
        });
    }

    private static class FadeGlassPane extends JPanel {
        private float alpha = 0f;

        FadeGlassPane() {
            setOpaque(false);
            setFocusable(false);
            addMouseListener(new java.awt.event.MouseAdapter() {});
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});
        }

        void setAlpha(float a) {
            this.alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
