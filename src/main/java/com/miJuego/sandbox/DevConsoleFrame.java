package com.miJuego.sandbox;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Ventana flotante de la Dev Console — estilo terminal.
 *
 * <p>Se abre/cierra con la tecla {@code ~} (tilde) desde el juego.
 * Tiene un área de log scrolleable y un campo de entrada de comandos.</p>
 */
public class DevConsoleFrame extends JFrame {

    // ─── Paleta de colores estilo terminal oscuro ─────────────────────────────
    private static final Color BG_CONSOLE    = new Color(15, 15, 20);
    private static final Color BG_INPUT      = new Color(25, 25, 32);
    private static final Color FG_OUTPUT     = new Color(200, 220, 200);
    private static final Color FG_INPUT      = new Color(120, 220, 130);
    private static final Color FG_ERROR      = new Color(255, 100, 100);
    private static final Color FG_SUCCESS    = new Color(80, 220, 120);
    private static final Color FG_INFO       = new Color(100, 180, 255);
    private static final Color FG_TIMESTAMP  = new Color(90, 90, 110);
    private static final Color FG_PROMPT     = new Color(60, 180, 255);
    private static final Color BORDER_COLOR  = new Color(50, 60, 80);
    private static final Color HEADER_BG     = new Color(20, 20, 30);

    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ─── Componentes ──────────────────────────────────────────────────────────
    private final JTextPane outputPane;
    private final StyledDocument doc;
    private JTextField inputField;
    private final DevCommandExecutor executor;

    // Historial de comandos (↑↓)
    private final java.util.List<String> history = new java.util.ArrayList<>();
    private int historyIndex = -1;

    public DevConsoleFrame(DevCommandExecutor executor) {
        this.executor = executor;

        setTitle("Dev Console — Tower Defense");
        setSize(750, 460);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(true);
        getContentPane().setBackground(BG_CONSOLE);
        setLayout(new BorderLayout(0, 0));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── Output area ───────────────────────────────────────────────────────
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(BG_CONSOLE);
        outputPane.setForeground(FG_OUTPUT);
        outputPane.setFont(MONO_FONT);
        outputPane.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        doc = outputPane.getStyledDocument();

        JScrollPane scroll = new JScrollPane(outputPane);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        scroll.setBackground(BG_CONSOLE);
        scroll.getViewport().setBackground(BG_CONSOLE);
        scroll.getVerticalScrollBar().setBackground(BG_CONSOLE);
        add(scroll, BorderLayout.CENTER);

        // ── Input row ─────────────────────────────────────────────────────────
        JPanel inputRow = buildInputRow();
        add(inputRow, BorderLayout.SOUTH);

        // ── Posición en pantalla ───────────────────────────────────────────────
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screen.width / 2 - getWidth() / 2, screen.height - getHeight() - 60);

        // ── Bienvenida ─────────────────────────────────────────────────────────
        appendMessage("╔══════════════════════════════════════════════════╗", FG_INFO);
        appendMessage("║        TOWER DEFENSE — DEV CONSOLE              ║", FG_INFO);
        appendMessage("║  Escribe 'help' para ver todos los comandos.    ║", FG_INFO);
        appendMessage("║  Tecla ~ para mostrar/ocultar esta ventana.     ║", FG_INFO);
        appendMessage("╚══════════════════════════════════════════════════╝", FG_INFO);
        appendMessage("", FG_OUTPUT);
    }

    // ── Construcción de subpaneles ────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JLabel title = new JLabel("  ▶  DEV CONSOLE");
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        title.setForeground(FG_PROMPT);
        header.add(title, BorderLayout.WEST);

        JLabel hint = new JLabel("[ ~ ] toggle   ");
        hint.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        hint.setForeground(FG_TIMESTAMP);
        header.add(hint, BorderLayout.EAST);

        header.setPreferredSize(new Dimension(0, 30));
        return header;
    }

    private JPanel buildInputRow() {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(BG_INPUT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Prompt label
        JLabel prompt = new JLabel("> ");
        prompt.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        prompt.setForeground(FG_PROMPT);
        row.add(prompt, BorderLayout.WEST);

        // Input field
        inputField = new JTextField();
        inputField.setFont(MONO_FONT);
        inputField.setBackground(BG_INPUT);
        inputField.setForeground(FG_INPUT);
        inputField.setCaretColor(FG_INPUT);
        inputField.setBorder(BorderFactory.createEmptyBorder());
        inputField.setOpaque(true);
        row.add(inputField, BorderLayout.CENTER);

        // Botón ejecutar
        JButton runBtn = new JButton("ENTER");
        runBtn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        runBtn.setBackground(new Color(30, 80, 50));
        runBtn.setForeground(FG_SUCCESS);
        runBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        runBtn.setFocusPainted(false);
        runBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runBtn.addActionListener(e -> executeCurrentInput());
        row.add(runBtn, BorderLayout.EAST);

        // Enter para ejecutar
        inputField.addActionListener(e -> executeCurrentInput());

        // ↑ ↓ para historial
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    navigateHistory(-1);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    navigateHistory(1);
                }
            }
        });

        return row;
    }

    // ── Ejecución de comandos ─────────────────────────────────────────────────

    private void executeCurrentInput() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) return;

        // Guardar en historial
        history.add(input);
        historyIndex = history.size();

        // Mostrar lo que el usuario escribió
        appendMessage("> " + input, FG_PROMPT);

        // Ejecutar
        String result = executor.execute(input);

        if ("CLEAR".equals(result)) {
            clearOutput();
        } else if (result != null && !result.isBlank()) {
            // Colorear según tipo de mensaje
            Color color = detectColor(result);
            appendMessage(result, color);
        }

        inputField.setText("");
        scrollToBottom();
    }

    private Color detectColor(String msg) {
        if (msg.startsWith("✓")) return FG_SUCCESS;
        if (msg.startsWith("⚠") || msg.toLowerCase().contains("error")) return FG_ERROR;
        if (msg.startsWith("ℹ") || msg.startsWith("──")) return FG_INFO;
        if (msg.startsWith("╔") || msg.startsWith("║") || msg.startsWith("╚")) return FG_INFO;
        return FG_OUTPUT;
    }

    private void navigateHistory(int direction) {
        if (history.isEmpty()) return;
        historyIndex = Math.max(0, Math.min(history.size() - 1, historyIndex + direction));
        inputField.setText(history.get(historyIndex));
        inputField.setCaretPosition(inputField.getText().length());
    }

    // ── Output helpers ────────────────────────────────────────────────────────

    public void appendMessage(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Timestamp
                Style tsStyle = outputPane.addStyle("ts", null);
                StyleConstants.setForeground(tsStyle, FG_TIMESTAMP);
                StyleConstants.setFontFamily(tsStyle, Font.MONOSPACED);
                StyleConstants.setFontSize(tsStyle, 11);
                doc.insertString(doc.getLength(), "[" + LocalTime.now().format(TIME_FMT) + "] ", tsStyle);

                // Mensaje
                Style msgStyle = outputPane.addStyle("msg", null);
                StyleConstants.setForeground(msgStyle, color);
                StyleConstants.setFontFamily(msgStyle, Font.MONOSPACED);
                StyleConstants.setFontSize(msgStyle, 13);

                // Las líneas múltiples van una a una
                for (String line : text.split("\n")) {
                    doc.insertString(doc.getLength(), line + "\n", msgStyle);
                }
            } catch (BadLocationException ignored) {}
        });
    }

    private void clearOutput() {
        SwingUtilities.invokeLater(() -> {
            try {
                doc.remove(0, doc.getLength());
                appendMessage("Consola limpiada.", FG_TIMESTAMP);
            } catch (BadLocationException ignored) {}
        });
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() ->
                outputPane.setCaretPosition(doc.getLength())
        );
    }

    /** Muestra u oculta la ventana. Llamado desde la tecla ~ del juego. */
    public void toggle() {
        if (isVisible()) {
            setVisible(false);
        } else {
            setVisible(true);
            toFront();
            inputField.requestFocusInWindow();
        }
    }

    /** Fuerza el foco al campo de entrada cuando la ventana ya estaba visible. */
    public void focusInput() {
        inputField.requestFocusInWindow();
    }
}
