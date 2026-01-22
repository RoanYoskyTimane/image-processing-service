package com.roanyosky.image_processing_service.filters;

import net.coobird.thumbnailator.filters.ImageFilter;
import java.awt.image.BufferedImage;

public class SepiaFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage sepia = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = img.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = (argb & 0xFF);

                int tr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int)(0.272 * r + 0.534 * g + 0.131 * b);

                r = Math.min(255, tr);
                g = Math.min(255, tg);
                b = Math.min(255, tb);

                sepia.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return sepia;
    }
}
