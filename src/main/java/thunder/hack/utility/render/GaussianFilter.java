package thunder.hack.utility.render;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Kernel;

public class GaussianFilter {
    protected float radius;
    protected Kernel kernel;

    public GaussianFilter(float radius) {
        setRadius(radius);
    }

    public static void convolveAndTranspose(@NotNull Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, boolean premultiply, boolean unpremultiply, int edgeAction) {
        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;
        for (int y = 0; y < height; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0.0F, g = 0.0F, b = 0.0F, a = 0.0F;
                int moffset = cols2;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[moffset + col];
                    if (f != 0.0F) {
                        int ix = x + col;
                        if (ix < 0) {
                            if (edgeAction == 1) {
                                ix = 0;
                            } else if (edgeAction == 2) {
                                ix = (x + width) % width;
                            }

                        } else if (ix >= width) {
                            if (edgeAction == 1) {
                                ix = width - 1;
                            } else if (edgeAction == 2) {
                                ix = (x + width) % width;
                            }
                        }

                        int rgb = inPixels[ioffset + ix];
                        int pa = rgb >> 24 & 0xFF;
                        int pr = rgb >> 16 & 0xFF;
                        int pg = rgb >> 8 & 0xFF;
                        int pb = rgb & 0xFF;

                        if (premultiply) {
                            float a255 = pa * 0.003921569F;
                            pr = (int) (pr * a255);
                            pg = (int) (pg * a255);
                            pb = (int) (pb * a255);
                        }

                        a += f * pa;
                        r += f * pr;
                        g += f * pg;
                        b += f * pb;
                    }
                }

                if (unpremultiply && a != 0.0F && a != 255.0F) {
                    float f = 255.0F / a;
                    r *= f;
                    g *= f;
                    b *= f;
                }

                int ia = alpha ? clamp((int) (a + 0.5D)) : 255;
                int ir = clamp((int) (r + 0.5D));
                int ig = clamp((int) (g + 0.5D));
                int ib = clamp((int) (b + 0.5D));
                outPixels[index] = ia << 24 | ir << 16 | ig << 8 | ib;
                index += height;
            }
        }
    }

    public static int clamp(int c) {
        if (c < 0) return 0;
        return Math.min(c, 255);
    }

    public static Kernel makeKernel(float radius) {
        int r = (int) Math.ceil(radius);
        int rows = r * 2 + 1;
        float[] matrix = new float[rows];
        float sigma = radius / 3.0F;
        float sigma22 = 2.0F * sigma * sigma;
        float sigmaPi2 = 6.2831855F * sigma;
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0.0F;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            float distance = (row * row);
            if (distance > radius2) {
                matrix[index] = 0.0F;
            } else {
                matrix[index] = (float) Math.exp((-distance / sigma22)) / sqrtSigmaPi2;
            }
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++) {
            matrix[i] = matrix[i] / total;
        }
        return new Kernel(rows, 1, matrix);
    }


    public void setRadius(float radius) {
        this.radius = radius;
        this.kernel = makeKernel(radius);
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        src.getRGB(0, 0, width, height, inPixels, 0, width);
        if (this.radius > 0.0F) {
            convolveAndTranspose(this.kernel, inPixels, outPixels, width, height, true, true, false, 1);
            convolveAndTranspose(this.kernel, outPixels, inPixels, height, width, true, false, true, 1);
        }
        dst.setRGB(0, 0, width, height, inPixels, 0, width);
        return dst;
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    public String toString() {
        return "Blur/Gaussian Blur...";
    }
}
