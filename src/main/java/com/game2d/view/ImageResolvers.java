package com.game2d.view;

/**
 * Singleton del cargador de imágenes compartido por toda la vista.
 */
public final class ImageResolvers {

    private static final ImageResolvers INSTANCE = new ImageResolvers();

    private final ImageResolver resolver = new DefaultImageResolver();

    private ImageResolvers() {
    }

    public static ImageResolvers getInstance() {
        return INSTANCE;
    }

    public ImageResolver getResolver() {
        return resolver;
    }
}
