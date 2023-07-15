package thunder.hack.utility.render.gaussianblur;

public class ImageMath {


    public static float clamp(float x, float a, float b) {
        return (x < a) ? a : (Math.min(x, b));
    }

    public static int clamp(int x, int a, int b) {
        return (x < a) ? a : (Math.min(x, b));
    }

    public static void premultiply(int[] p, int offset, int length) {
        length += offset;

        for (int i = offset; i < length; i++) {

            int rgb = p[i];
            int a = rgb >> 24 & 0xFF;
            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;
            float f = a * 0.003921569F;
            r = (int) (r * f);
            g = (int) (g * f);
            b = (int) (b * f);
            p[i] = a << 24 | r << 16 | g << 8 | b;
        }
    }

    public static void unpremultiply(int[] p, int offset, int length) {
        length += offset;

        for (int i = offset; i < length; i++) {

            int rgb = p[i];
            int a = rgb >> 24 & 0xFF;
            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;

            if (a != 0 && a != 255) {

                float f = 255.0F / a;
                r = (int) (r * f);
                g = (int) (g * f);
                b = (int) (b * f);

                if (r > 255) {
                    r = 255;
                }

                if (g > 255) {
                    g = 255;
                }

                if (b > 255) {
                    b = 255;
                }

                p[i] = a << 24 | r << 16 | g << 8 | b;
            }
        }
    }
}
