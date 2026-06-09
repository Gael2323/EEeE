package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Optional;

/**
 * Representa visualmente a Clippy, su globo de diálogo estilo Microsoft Office clásico,
 * y la base protectora para evitar perderse contra el texto del documento.
 */
public class ClippyTutorialDrawable implements Drawable {

    private static final java.util.Map<String, BufferedImage> clippyExpressions = new java.util.HashMap<>();
    private static BufferedImage defaultClippy;

    static {
        // Cargar todas las expresiones de Clippy de manera estática para evitar I/O repetitivo
        defaultClippy = loadClippyImage("clippy_sprite.png");
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

    private static BufferedImage loadClippyImage(String filename) {
        try {
            java.io.InputStream is = ClippyTutorialDrawable.class.getResourceAsStream("/assets/word/" + filename);
            if (is != null) return javax.imageio.ImageIO.read(is);
        } catch (Exception e) {
            System.err.println("No se pudo cargar imagen de Clippy: " + filename);
        }
        return null;
    }

    private static void loadClippyExpression(String key, String filename) {
        BufferedImage img = loadClippyImage(filename);
        if (img != null) {
            clippyExpressions.put(key, img);
        }
    }

    private final ClippyTutorial.Estado estado;
    private final String[] lines;
    private final String expression;
    private final float x;
    private final float y;

    public ClippyTutorialDrawable(ClippyTutorial.Estado estado, String[] lines, String expression, float x, float y) {
        this.estado = estado;
        this.lines = lines;
        this.expression = expression;
        this.x = x;
        this.y = y;
    }

    @Override
    public String getId() {
        return "clippy-tutorial";
    }

    @Override
    public Float getX() {
        return x;
    }

    @Override
    public Float getY() {
        return y;
    }

    @Override
    public Float getWidth() {
        return 1.6f; // Tamaño lógico ancho en el grid
    }

    @Override
    public Float getHeight() {
        // En base a la altura visual de Clippy (2.0f en la grilla para que se distinga)
        return 2.0f;
    }

    @Override
    public int getLayer() {
        // Capa superior para que se dibuje por encima de las torres, enemigos y proyectiles
        return 100;
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.of("assets/word/clippy_sprite.png");
    }

    @Override
    public Optional<URL> getImageUrl() {
        return Optional.empty();
    }

    @Override
    public Color getFallbackColor() {
        return new Color(220, 180, 0);
    }

    @Override
    public FallbackShape getFallbackShape() {
        return FallbackShape.RECTANGLE;
    }

    /**
     * Dibuja a Clippy, su globo de diálogo Office Assistant y su minifondo protector en pantalla.
     */
    public void draw(Graphics2D g2, int cx, int cy, int cw, int ch) {
        // 1. Minifondo Protector (Bandeja / Desktop Tray translúcida)
        // Evita que Clippy se difumine con el Lorem Ipsum de fondo del documento de Word.
        g2.setColor(new Color(245, 245, 235, 180)); // Crema suave de Word
        g2.fillRoundRect(cx - 8, cy - 8, cw + 16, ch + 16, 12, 12);
        g2.setColor(new Color(160, 170, 180, 120)); // Borde gris claro
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(cx - 8, cy - 8, cw + 16, ch + 16, 12, 12);

        // Sombra de Clippy
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillOval(cx + cw / 4, cy + ch - 8, cw / 2, 6);

        // 2. Sprite de Clippy
        BufferedImage currentSprite = clippyExpressions.get(expression);
        if (currentSprite == null) {
            currentSprite = defaultClippy;
        }

        if (currentSprite != null) {
            g2.drawImage(currentSprite, cx, cy, cw, ch, null);
        } else {
            // Fallback vectorial de clip si no se cargan las texturas
            g2.setColor(new Color(220, 180, 0));
            g2.setStroke(new BasicStroke(3f));
            g2.drawArc(cx + cw / 4, cy + 10, cw / 2, ch / 2, 0, 270);
            g2.setStroke(new BasicStroke(1f));
        }

        // 3. Globo de Diálogo
        if (lines != null && lines.length > 0) {
            drawSpeechBubble(g2, cx, cy, cw, ch);
        }
    }

    private void drawSpeechBubble(Graphics2D g2, int clippyX, int clippyY, int clippyW, int clippyH) {
        int bw = 250;
        int bh = 30 + lines.length * 18 + 22;
        int by = clippyY - 10;
        int bx;

        // Determinar dinámicamente si poner el globo a la izquierda o derecha de Clippy
        // Si clippy está en el lado izquierdo del viewport, dibujamos el globo a la derecha
        boolean bubbleOnRight = (clippyX < 400);
        if (bubbleOnRight) {
            bx = clippyX + clippyW + 18;
        } else {
            bx = clippyX - bw - 18;
        }

        // Sombra arrojada del globo
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(bx + 3, by + 3, bw, bh, 10, 10);

        // Fondo del globo (Amarillo clásico asistente)
        g2.setColor(new Color(255, 255, 225));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);

        // Borde del globo
        g2.setColor(new Color(110, 105, 90));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(bx, by, bw, bh, 10, 10);

        // Colita del globo apuntando a Clippy
        int tailBaseY = by + 20;
        int tailTipX = clippyX + clippyW / 2;
        int tailTipY = clippyY + clippyH / 3;

        int[] px, py;
        if (bubbleOnRight) {
            px = new int[]{bx, bx, tailTipX};
            py = new int[]{tailBaseY, tailBaseY + 12, tailTipY};
        } else {
            px = new int[]{bx + bw, bx + bw, tailTipX};
            py = new int[]{tailBaseY, tailBaseY + 12, tailTipY};
        }

        g2.setColor(new Color(255, 255, 225));
        g2.fillPolygon(px, py, 3);
        g2.setColor(new Color(110, 105, 90));
        g2.drawLine(px[0], py[0], px[2], py[2]);
        g2.drawLine(px[1], py[1], px[2], py[2]);
        g2.setStroke(new BasicStroke(1f));

        // Escribir el texto
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        int ty = by + 22;
        for (String line : lines) {
            g2.drawString(line, bx + 14, ty);
            ty += 18;
        }

        // Pista o hint de avance inferior
        g2.setFont(new Font("Tahoma", Font.ITALIC, 9));
        String hint;
        switch (estado) {
            case WAIT_PLACE:
                g2.setColor(new Color(180, 50, 50));
                hint = "* Coloca la torre para continuar *";
                break;
            case WAIT_START_WAVE:
                g2.setColor(new Color(50, 120, 50));
                hint = "* Presiona ENTER para iniciar la oleada *";
                break;
            default:
                g2.setColor(new Color(100, 100, 100));
                hint = "Click o ESPACIO para continuar";
                break;
        }
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, bx + bw - fm.stringWidth(hint) - 10, by + bh - 6);
    }
}
