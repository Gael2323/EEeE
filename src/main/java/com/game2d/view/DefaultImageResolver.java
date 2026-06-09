package com.game2d.view;

import com.game2d.model.Renderable;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class DefaultImageResolver implements ImageResolver {

    private final Map<String, Image> cache = new ConcurrentHashMap<>();

    @Override
    public Image resolve(Renderable renderable, int widthPx, int heightPx) {
        String cacheKey = cacheKey(renderable, widthPx, heightPx);
        return cache.computeIfAbsent(cacheKey, key -> loadOrFallback(renderable, widthPx, heightPx));
    }

    private Image loadOrFallback(Renderable renderable, int widthPx, int heightPx) {
        Optional<String> path = renderable.getImagePath();
        if (path.isPresent()) {
            Image fromPath = loadFromPath(path.get(), widthPx, heightPx);
            if (fromPath != null) {
                return fromPath;
            }
        }
        Optional<URL> url = renderable.getImageUrl();
        if (url.isPresent()) {
            Image fromUrl = loadFromUrl(url.get(), widthPx, heightPx);
            if (fromUrl != null) {
                return fromUrl;
            }
        }
        return createFallback(renderable, widthPx, heightPx);
    }

    private Image loadFromPath(String pathValue, int widthPx, int heightPx) {
        try {
            Path path = Path.of(pathValue);
            if (Files.isRegularFile(path)) {
                return scale(BufferedImage.class.cast(
                        javax.imageio.ImageIO.read(path.toFile())), widthPx, heightPx);
            }
            InputStream classpath = getClass().getClassLoader().getResourceAsStream(pathValue);
            if (classpath != null) {
                try (InputStream in = classpath) {
                    return scale(javax.imageio.ImageIO.read(in), widthPx, heightPx);
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
        return null;
    }

    private Image loadFromUrl(URL url, int widthPx, int heightPx) {
        try {
            return scale(javax.imageio.ImageIO.read(url), widthPx, heightPx);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private Image scale(BufferedImage source, int widthPx, int heightPx) {
        if (source == null) {
            return null;
        }
        int w = Math.max(1, widthPx);
        int h = Math.max(1, heightPx);
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();
        return buffer;
    }

    private String cacheKey(Renderable renderable, int widthPx, int heightPx) {
        String source = renderable.getImagePath().orElseGet(
                () -> renderable.getImageUrl().map(URL::toString).orElseGet(
                        () -> "fallback_" + renderable.getFallbackColor().getRGB() + "_" + renderable.getFallbackShape().name()
                )
        );
        return source + "@" + widthPx + "x" + heightPx;
    }
}
