package com.sigoc.sigoc_web.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Renderiza el reto CAPTCHA en una imagen PNG distorsionada (base64 data URI)
 * para evitar exponer el código en texto plano en la respuesta JSON (QA-02).
 */
@Component
public class CaptchaImagenRenderer {

    private static final int ANCHO = 190;
    private static final int ALTO = 54;
    private final SecureRandom random = new SecureRandom();

    static {
        System.setProperty("java.awt.headless", "true");
    }

    public String renderizarPngDataUri(String codigo) {
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(241, 245, 249), ANCHO, ALTO, new Color(226, 232, 240)));
            g.fillRect(0, 0, ANCHO, ALTO);

            // Líneas de interferencia
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(148 + random.nextInt(60), 163 + random.nextInt(50), 184 + random.nextInt(50)));
                g.setStroke(new BasicStroke(1.2f + random.nextFloat()));
                g.drawLine(random.nextInt(ANCHO), random.nextInt(ALTO), random.nextInt(ANCHO), random.nextInt(ALTO));
            }

            // Caracteres con rotación y desplazamiento vertical aleatorio
            Font baseFont = new Font(Font.SANS_SERIF, Font.BOLD, 26);
            int pasoX = (ANCHO - 24) / Math.max(1, codigo.length());
            for (int i = 0; i < codigo.length(); i++) {
                double angulo = (random.nextDouble() - 0.5) * 0.55;
                int x = 14 + i * pasoX + random.nextInt(5);
                int y = 34 + random.nextInt(10) - 5;
                AffineTransform orig = g.getTransform();
                g.setFont(baseFont);
                g.setColor(new Color(15 + random.nextInt(55), 35 + random.nextInt(65), 75 + random.nextInt(90)));
                g.rotate(angulo, x, y);
                g.drawString(String.valueOf(codigo.charAt(i)), x, y);
                g.setTransform(orig);
            }

            // Puntos de ruido
            for (int i = 0; i < 65; i++) {
                g.setColor(new Color(100 + random.nextInt(100), 116 + random.nextInt(100), 139 + random.nextInt(100)));
                g.fillRect(random.nextInt(ANCHO), random.nextInt(ALTO), 2, 2);
            }
        } finally {
            g.dispose();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar la imagen CAPTCHA.", ex);
        }
    }
}
