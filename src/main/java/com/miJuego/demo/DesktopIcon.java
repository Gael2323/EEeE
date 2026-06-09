package com.miJuego.demo;

import java.awt.*;

/**
 * Modelo de un ícono del escritorio XP del hub principal.
 *
 * <p>El método {@link #drawPixelArt(Graphics2D, int, int, int)} intenta dibujar
 * los assets PNG (baseImage, lockImage) si están disponibles. Si no, hace un 
 * fallback a las figuras geométricas pixeladas generadas por código.</p>
 */
public class DesktopIcon {

    public enum IconType {
        WORD, RECYCLE_BIN, SOLITAIRE, EXPLORER, TERMINAL, GALLERY, MY_COMPUTER, WIZARD_CHRONICLE
    }

    // ── Posición en espacio de pantalla (usada por mouse listeners) ───────────
    public int x;
    public int y;
    public static final int ICON_SIZE = 48; // XP Classic size

    // ── Datos ─────────────────────────────────────────────────────────────────
    public final String   name;
    public final IconType type;
    public boolean        locked;
    public final Runnable onAction;
    public boolean        hovered = false;

    // ── Assets cargados externamente ──────────────────────────────────────────
    private final Image baseImage;
    private final Image lockImage;

    public DesktopIcon(String name, IconType type, int x, int y,
                       boolean locked, Runnable onAction,
                       Image baseImage, Image lockImage) {
        this.name      = name;
        this.type      = type;
        this.x         = x;
        this.y         = y;
        this.locked    = locked;
        this.onAction  = onAction;
        this.baseImage = baseImage;
        this.lockImage = lockImage;
    }

    public Rectangle getBounds()     { return new Rectangle(x, y, ICON_SIZE, ICON_SIZE); }
    public Rectangle getFullBounds() { return new Rectangle(x - 10, y - 4, ICON_SIZE + 20, ICON_SIZE + 35); }

    // ─────────────────────────────────────────────────────────────────────────
    //  DIBUJO PIXEL ART / ASSETS
    //  cx, cy, size están en coordenadas del canvas interno (baja resolución).
    // ─────────────────────────────────────────────────────────────────────────

    public void drawPixelArt(Graphics2D g, int cx, int cy, int size) {
        // ── Selección/hover: recuadro azul translúcido ──────────────────────
        if (hovered) {
            g.setColor(locked ? new Color(0xCC2222) : new Color(0x3060C8));
            g.fillRect(cx - 2, cy - 2, size + 4, size + 4);
            g.setColor(locked ? new Color(0xFF4444) : new Color(0x80A8FF));
            g.fillRect(cx - 1, cy - 1, size + 2, size + 2);
        }

        // ── Ícono principal ──────────────────────────────────────────────────
        if (baseImage != null) {
            g.drawImage(baseImage, cx, cy, size, size, null);
        } else {
            // Fallback procedimental
            switch (type) {
                case WORD        -> drawWord(g, cx, cy, size);
                case RECYCLE_BIN -> drawRecycleBin(g, cx, cy, size);
                case SOLITAIRE   -> drawSolitaire(g, cx, cy, size);
                case EXPLORER    -> drawExplorer(g, cx, cy, size);
                case TERMINAL    -> drawTerminal(g, cx, cy, size);
                case GALLERY     -> drawGallery(g, cx, cy, size);
            }
        }

        // ── Candado (Insignia inferior derecha) ───────────────────────────────
        if (locked) {
            int badgeSize = size / 2 + 4;
            int lx = cx + size - badgeSize;
            int ly = cy + size - badgeSize + 4;
            
            // Draw with Bilinear interpolation so the lock doesn't look overly pixelated
            Object oldHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            if (lockImage != null) {
                g.drawImage(lockImage, lx, ly, badgeSize, badgeSize, null);
            } else {
                drawPixelLock(g, lx, ly, badgeSize);
            }
            
            // Restore old hint
            if (oldHint != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
            } else {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            }
        }

        // ── Label con sombra tipo XP ──────────────────────────────────────────
        drawLabel(g, cx, cy, size);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ICONOS INDIVIDUALES — FALLBACKS POR CÓDIGO
    // ─────────────────────────────────────────────────────────────────────────

    private void drawWord(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(0x1565C0));
        g.fillRect(x, y, s, s);
        int fold = s / 5;
        g.setColor(new Color(0x0D47A1));
        int[] fxs = {x + s - fold, x + s, x + s};
        int[] fys = {y,             y,      y + fold};
        g.fillPolygon(fxs, fys, 3);
        g.setColor(new Color(0x5C8FE0));
        g.drawLine(x + s - fold, y, x + s, y + fold);
        g.setColor(new Color(0x0A3580));
        g.drawRect(x, y, s - 1, s - 1);
        int fs = Math.max(7, (s * 14) / 30);
        g.setColor(new Color(0, 0, 0, 80));
        g.setFont(new Font("Serif", Font.BOLD, fs));
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (s - fm.stringWidth("W")) / 2 + 1;
        int ty = y + s - s / 5 + 1;
        g.drawString("W", tx, ty);
        g.setColor(Color.WHITE);
        g.drawString("W", tx - 1, ty - 1);
        g.setColor(new Color(0xAAD0FF));
        int lx1 = x + s / 5, lx2 = x + s - s / 5;
        int lineH = s / 7;
        g.fillRect(lx1, y + s - lineH * 3, lx2 - lx1, 1);
        g.fillRect(lx1, y + s - lineH * 2, lx2 - lx1 - 3, 1);
        g.setColor(new Color(0xFFFFFF, true));
        g.fillRect(x + 1, y + 1, s - 2, 2);
    }

    private void drawRecycleBin(Graphics2D g, int x, int y, int s) {
        int bodyX  = x + s / 6;
        int bodyY  = y + s / 4;
        int bodyW  = s - s / 3;
        int bodyH  = s - s / 3 - 2;
        int lidH   = s / 7;
        int lidExt = 2;
        g.setColor(new Color(0xB0C4D8));
        g.fillRect(bodyX, bodyY, bodyW, bodyH);
        g.setColor(new Color(0xD4E4F4));
        g.fillRect(bodyX + 1, bodyY + 1, 2, bodyH - 2);
        g.setColor(new Color(0x88A4BC));
        g.fillRect(bodyX + bodyW - 2, bodyY + 1, 2, bodyH - 2);
        g.setColor(new Color(0x4A6880));
        g.drawRect(bodyX, bodyY, bodyW, bodyH);
        g.setColor(new Color(0x98B4CC));
        g.fillRect(bodyX - lidExt, bodyY - lidH, bodyW + lidExt * 2, lidH);
        g.setColor(new Color(0xC0D4E8));
        g.fillRect(bodyX - lidExt + 1, bodyY - lidH + 1, bodyW + lidExt * 2 - 2, 2);
        g.setColor(new Color(0x4A6880));
        g.drawRect(bodyX - lidExt, bodyY - lidH, bodyW + lidExt * 2, lidH);
        int hx = x + s / 2 - 4;
        g.setColor(new Color(0x7090A8));
        g.fillRect(hx, bodyY - lidH - 4, 7, 5);
        g.fillRect(hx + 1, bodyY - lidH - 6, 5, 3);
        g.setColor(new Color(0x4A6880));
        g.drawRect(hx, bodyY - lidH - 4, 7, 5);
        g.setColor(new Color(0x4A6880));
        for (int lx = bodyX + 4; lx < bodyX + bodyW - 2; lx += 4) {
            g.drawLine(lx, bodyY + 2, lx - 1, bodyY + bodyH - 2);
        }
        int mid = x + s / 2;
        int arY = bodyY + bodyH / 2;
        g.setColor(new Color(0x2A4A68));
        g.fillRect(mid - 4, arY - 3, 8, 2);
        g.fillRect(mid - 3, arY - 1, 2, 4);
        g.fillRect(mid + 1, arY - 1, 2, 4);
    }

    private void drawSolitaire(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(0x1A7A28));
        g.fillRect(x, y, s, s);
        g.setColor(new Color(0x0E5018));
        g.drawRect(x, y, s - 1, s - 1);
        g.setColor(new Color(0x166622));
        for (int gy = y + 3; gy < y + s; gy += 4) g.fillRect(x + 1, gy, s - 2, 1);
        int cx = x + 4, cy2 = y + 5;
        int cw = s - 10, ch = s - 10;
        g.setColor(new Color(0xF0F0F0));
        g.fillRect(cx, cy2, cw, ch);
        g.setColor(new Color(0xC8C8C8));
        g.drawRect(cx, cy2, cw, ch);
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRect(cx + 2, cy2 + 2, cw, ch);
        int hx = cx + cw / 2 - 4;
        int hy = cy2 + ch / 2 - 3;
        g.setColor(new Color(0xCC1818));
        int u = Math.max(1, s / 12);
        g.fillRect(hx + u,     hy,         u * 2, u);
        g.fillRect(hx + u * 4, hy,         u * 2, u);
        g.fillRect(hx,         hy + u,     u * 7, u);
        g.fillRect(hx,         hy + u * 2, u * 7, u);
        g.fillRect(hx + u,     hy + u * 3, u * 5, u);
        g.fillRect(hx + u * 2, hy + u * 4, u * 3, u);
        g.fillRect(hx + u * 3, hy + u * 5, u,     u);
        g.setColor(new Color(0xFF6060));
        g.fillRect(hx + u, hy + u, u, u);
        g.fillRect(hx + u * 4, hy + u, u, u);
        g.setColor(new Color(0xCC1818));
        g.setFont(new Font("Tahoma", Font.BOLD, Math.max(5, s / 6)));
        g.drawString("A", cx + 2, cy2 + g.getFontMetrics().getAscent() + 1);
    }

    private void drawExplorer(Graphics2D g, int x, int y, int s) {
        int bx = x + 2, by = y + s / 4;
        int bw = s - 4, bh = s - s / 3;
        g.setColor(new Color(0xF0C020));
        g.fillRect(bx, by - s / 7, s / 3, s / 7 + 1);
        g.setColor(new Color(0xC89010));
        g.drawRect(bx, by - s / 7, s / 3 - 1, s / 7);
        g.setColor(new Color(0xF0C020));
        g.fillRect(bx, by, bw, bh);
        g.setColor(new Color(0xFFE060));
        g.fillRect(bx + 1, by + 1, bw - 2, 3);
        g.setColor(new Color(0xB07808));
        g.fillRect(bx + bw - 2, by + 2, 2, bh - 2);
        g.fillRect(bx + 2, by + bh - 2, bw - 2, 2);
        g.setColor(new Color(0x806000));
        g.drawRect(bx, by, bw, bh);
        int dx = bx + bw / 2 - 4, dy = by + bh / 3;
        int dw = 8, dh = 10;
        g.setColor(Color.WHITE);
        g.fillRect(dx, dy, dw, dh);
        g.setColor(new Color(0xAAAAA0));
        g.drawRect(dx, dy, dw, dh);
        g.setColor(new Color(0x888880));
        g.fillRect(dx + 2, dy + 3, dw - 4, 1);
        g.fillRect(dx + 2, dy + 5, dw - 4, 1);
        g.fillRect(dx + 2, dy + 7, dw - 6, 1);
    }

    private void drawTerminal(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(0x404040));
        g.fillRect(x, y, s, s);
        g.setColor(new Color(0x808080));
        g.fillRect(x + 1, y + 1, s - 2, s / 6);
        g.setColor(new Color(0xC04040));
        g.fillRect(x + s - 5, y + 2, 4, 4);
        g.setColor(new Color(0xA0A0A0));
        g.fillRect(x + s - 10, y + 2, 4, 4);
        g.setColor(Color.BLACK);
        g.fillRect(x + 1, y + s / 6 + 1, s - 2, s - s / 6 - 2);
        g.setColor(new Color(0x202020));
        g.drawRect(x, y, s - 1, s - 1);
        int fs = Math.max(4, s / 8);
        g.setFont(new Font("Monospaced", Font.BOLD, fs));
        g.setColor(new Color(0x00CC00));
        int lineY = y + s / 6 + fs + 3;
        g.drawString("C:\\>_", x + 3, lineY);
        g.setColor(new Color(0x008800));
        g.drawString("dir /s", x + 3, lineY + fs + 2);
        if ((System.currentTimeMillis() / 400) % 2 == 0) {
            g.setColor(new Color(0x00CC00));
            g.fillRect(x + 3 + fs * 3, lineY + fs + 3, fs / 2, fs);
        }
    }

    private void drawGallery(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(0xC8C8C8));
        g.fillRect(x, y, s, s);
        g.setColor(new Color(0xA0A0A0));
        g.drawRect(x, y, s - 1, s - 1);
        int m = s / 7;
        g.setColor(new Color(0x808080));
        g.drawRect(x + m - 1, y + m - 1, s - m * 2 + 1, s - m * 2 + 1);
        int px = x + m, py2 = y + m, pw = s - m * 2, ph = s - m * 2;
        g.setColor(new Color(0x5090E8));
        g.fillRect(px, py2, pw, ph / 2);
        g.setColor(new Color(0x88B8F8));
        g.fillRect(px, py2 + ph / 4, pw, ph / 4);
        g.setColor(new Color(0xF8D020));
        g.fillRect(px + 2, py2 + 2, ph / 4, ph / 4);
        g.setColor(new Color(0xFFEC60));
        g.fillRect(px + 3, py2 + 3, ph / 4 - 2, ph / 4 - 2);
        g.setColor(new Color(0x40A030));
        g.fillRect(px, py2 + ph / 2, pw, ph / 2);
        g.setColor(new Color(0x58C840));
        for (int lx = 0; lx < pw; lx++) {
            int hillY = (int)(Math.sin(lx * 0.25f) * (ph / 8)) + ph * 5 / 8;
            g.fillRect(px + lx, py2 + hillY, 1, ph - hillY);
        }
        g.setColor(new Color(0xE8E8E8));
        g.fillRect(x + 1, y + 1, s - 2, 2);
        g.fillRect(x + 1, y + 1, 2, s - 2);
        g.setColor(new Color(0x909090));
        g.fillRect(x + s - 3, y + 1, 2, s - 2);
        g.fillRect(x + 1, y + s - 3, s - 2, 2);
    }

    private void drawPixelLock(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(x, y, size, size);
        int lw = Math.max(8, size / 3);
        int lh = Math.max(7, size / 3);
        int lx = x + size / 2 - lw / 2;
        int ly = y + size / 2;
        g.setColor(new Color(0xC89010));
        g.fillRect(lx, ly, lw, lh);
        g.setColor(new Color(0xF8C828));
        g.fillRect(lx + 1, ly + 1, lw - 2, lh / 3);
        g.setColor(new Color(0x886000));
        g.fillRect(lx, ly + lh - 2, lw, 2);
        g.fillRect(lx + lw - 2, ly, 2, lh);
        g.setColor(new Color(0x404000));
        g.drawRect(lx, ly, lw - 1, lh - 1);
        int gx  = lx + lw / 4;
        int gw  = lw - lw / 2;
        int gh  = lh * 3 / 4;
        g.setColor(new Color(0xC0C0C0));
        g.fillRect(gx, ly - gh, 2, gh);
        g.fillRect(gx + gw - 2, ly - gh, 2, gh);
        g.fillRect(gx, ly - gh, gw, 2);
        g.setColor(new Color(0x808080));
        g.fillRect(gx + 1, ly - gh + 1, 1, gh - 1);
        g.fillRect(gx + gw - 2, ly - gh + 1, 1, gh - 1);
        int ex = lx + lw / 2 - 1;
        int ey = ly + lh / 3;
        g.setColor(new Color(0x202000));
        g.fillRect(ex, ey, 3, 3);
        g.fillRect(ex + 1, ey + 2, 1, 2);
    }

    private void drawLabel(Graphics2D g, int cx, int cy, int size) {
        g.setFont(new Font("Tahoma", Font.BOLD, Math.max(5, size / 4)));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(name);
        int tx = cx + (size - textW) / 2;
        int ty = cy + size + fm.getAscent() + 2;
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(name, tx + 1, ty + 1);
        g.setColor(Color.WHITE);
        g.drawString(name, tx, ty);
    }
}
