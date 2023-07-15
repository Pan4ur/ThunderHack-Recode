package thunder.hack.utility.render.gaussianblur;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Kernel;


public class ConvolveFilter
        extends AbstractBufferedImageOp {

    /*  37 */   public static int CLAMP_EDGES = 1;


    /*  42 */   public static int WRAP_EDGES = 2;


    /*  47 */   protected Kernel kernel = null;


    protected boolean alpha = true;


    protected boolean premultiplyAlpha = true;


    /*  62 */   private final int edgeAction = CLAMP_EDGES;


    public ConvolveFilter() {
        /*  69 */
        this(new float[9]);
    }


    public ConvolveFilter(float[] matrix) {
        /*  78 */
        this(new Kernel(3, 3, matrix));
    }

    public ConvolveFilter(Kernel kernel) {
        /*  98 */
        this.kernel = kernel;
    }

    public static void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        /* 268 */
        if (kernel.getHeight() == 1) {

            /* 270 */
            convolveH(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        }
        /* 272 */
        else if (kernel.getWidth() == 1) {

            /* 274 */
            convolveV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        } else {

            /* 278 */
            convolveHV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        }
    }

    public static void convolveHV(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        /* 294 */
        int index = 0;
        /* 295 */
        float[] matrix = kernel.getKernelData(null);
        /* 296 */
        int rows = kernel.getHeight();
        /* 297 */
        int cols = kernel.getWidth();
        /* 298 */
        int rows2 = rows / 2;
        /* 299 */
        int cols2 = cols / 2;

        /* 301 */
        for (int y = 0; y < height; y++) {

            /* 303 */
            for (int x = 0; x < width; x++) {

                /* 305 */
                float r = 0.0F, g = 0.0F, b = 0.0F, a = 0.0F;

                /* 307 */
                for (int row = -rows2; row <= rows2; row++) {

                    /* 309 */
                    int ioffset, iy = y + row;


                    /* 312 */
                    if (0 <= iy && iy < height) {

                        /* 314 */
                        ioffset = iy * width;
                    }
                    /* 316 */
                    else if (edgeAction == CLAMP_EDGES) {

                        /* 318 */
                        ioffset = y * width;
                    }
                    /* 320 */
                    else if (edgeAction == WRAP_EDGES) {

                        /* 322 */
                        ioffset = (iy + height) % height * width;
                    } else {
                        continue;
                    }



                    /* 329 */
                    int moffset = cols * (row + rows2) + cols2;

                    /* 331 */
                    for (int col = -cols2; col <= cols2; col++) {

                        /* 333 */
                        float f = matrix[moffset + col];

                        /* 335 */
                        if (f != 0.0F) {

                            /* 337 */
                            int ix = x + col;

                            /* 339 */
                            if (0 > ix || ix >= width) {
                                /* 341 */
                                if (edgeAction == CLAMP_EDGES) {

                                    /* 343 */
                                    ix = x;
                                }
                                /* 345 */
                                else if (edgeAction == WRAP_EDGES) {

                                    /* 347 */
                                    ix = (x + width) % width;
                                } else {
                                    continue;
                                }
                            }



                            /* 355 */
                            int rgb = inPixels[ioffset + ix];
                            /* 356 */
                            a += f * (rgb >> 24 & 0xFF);
                            /* 357 */
                            r += f * (rgb >> 16 & 0xFF);
                            /* 358 */
                            g += f * (rgb >> 8 & 0xFF);
                            /* 359 */
                            b += f * (rgb & 0xFF);
                        }
                        continue;
                    }
                    continue;
                }
                /* 364 */
                int ia = alpha ? clamp((int) (a + 0.5D)) : 255;
                /* 365 */
                int ir = clamp((int) (r + 0.5D));
                /* 366 */
                int ig = clamp((int) (g + 0.5D));
                /* 367 */
                int ib = clamp((int) (b + 0.5D));
                /* 368 */
                outPixels[index++] = ia << 24 | ir << 16 | ig << 8 | ib;
            }
        }
    }

    public static void convolveH(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        /* 385 */
        int index = 0;
        /* 386 */
        float[] matrix = kernel.getKernelData(null);
        /* 387 */
        int cols = kernel.getWidth();
        /* 388 */
        int cols2 = cols / 2;

        /* 390 */
        for (int y = 0; y < height; y++) {

            /* 392 */
            int ioffset = y * width;

            /* 394 */
            for (int x = 0; x < width; x++) {

                /* 396 */
                float r = 0.0F, g = 0.0F, b = 0.0F, a = 0.0F;
                /* 397 */
                int moffset = cols2;

                /* 399 */
                for (int col = -cols2; col <= cols2; col++) {

                    /* 401 */
                    float f = matrix[moffset + col];

                    /* 403 */
                    if (f != 0.0F) {

                        /* 405 */
                        int ix = x + col;

                        /* 407 */
                        if (ix < 0) {

                            /* 409 */
                            if (edgeAction == CLAMP_EDGES) {
                                /* 411 */
                                ix = 0;
                            }
                            /* 413 */
                            else if (edgeAction == WRAP_EDGES) {
                                /* 415 */
                                ix = (x + width) % width;
                            }

                            /* 418 */
                        } else if (ix >= width) {

                            /* 420 */
                            if (edgeAction == CLAMP_EDGES) {

                                /* 422 */
                                ix = width - 1;
                            }
                            /* 424 */
                            else if (edgeAction == WRAP_EDGES) {

                                /* 426 */
                                ix = (x + width) % width;
                            }
                        }

                        /* 430 */
                        int rgb = inPixels[ioffset + ix];
                        /* 431 */
                        a += f * (rgb >> 24 & 0xFF);
                        /* 432 */
                        r += f * (rgb >> 16 & 0xFF);
                        /* 433 */
                        g += f * (rgb >> 8 & 0xFF);
                        /* 434 */
                        b += f * (rgb & 0xFF);
                    }
                }

                /* 438 */
                int ia = alpha ? clamp((int) (a + 0.5D)) : 255;
                /* 439 */
                int ir = clamp((int) (r + 0.5D));
                /* 440 */
                int ig = clamp((int) (g + 0.5D));
                /* 441 */
                int ib = clamp((int) (b + 0.5D));
                /* 442 */
                outPixels[index++] = ia << 24 | ir << 16 | ig << 8 | ib;
            }
        }
    }

    public static void convolveV(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        /* 459 */
        int index = 0;
        /* 460 */
        float[] matrix = kernel.getKernelData(null);
        /* 461 */
        int rows = kernel.getHeight();
        /* 462 */
        int rows2 = rows / 2;

        /* 464 */
        for (int y = 0; y < height; y++) {

            /* 466 */
            for (int x = 0; x < width; x++) {

                /* 468 */
                float r = 0.0F, g = 0.0F, b = 0.0F, a = 0.0F;

                /* 470 */
                for (int row = -rows2; row <= rows2; row++) {

                    /* 472 */
                    int ioffset, iy = y + row;


                    /* 475 */
                    if (iy < 0) {

                        /* 477 */
                        if (edgeAction == CLAMP_EDGES) {
                            /* 479 */
                            ioffset = 0;
                        }
                        /* 481 */
                        else if (edgeAction == WRAP_EDGES) {
                            /* 483 */
                            ioffset = (y + height) % height * width;
                        } else {
                            /* 487 */
                            ioffset = iy * width;
                        }

                        /* 490 */
                    } else if (iy >= height) {

                        /* 492 */
                        if (edgeAction == CLAMP_EDGES) {
                            /* 494 */
                            ioffset = (height - 1) * width;
                        }
                        /* 496 */
                        else if (edgeAction == WRAP_EDGES) {
                            /* 498 */
                            ioffset = (y + height) % height * width;
                        } else {
                            /* 502 */
                            ioffset = iy * width;
                        }

                    } else {

                        /* 507 */
                        ioffset = iy * width;
                    }

                    /* 510 */
                    float f = matrix[row + rows2];

                    /* 512 */
                    if (f != 0.0F) {

                        /* 514 */
                        int rgb = inPixels[ioffset + x];
                        /* 515 */
                        a += f * (rgb >> 24 & 0xFF);
                        /* 516 */
                        r += f * (rgb >> 16 & 0xFF);
                        /* 517 */
                        g += f * (rgb >> 8 & 0xFF);
                        /* 518 */
                        b += f * (rgb & 0xFF);
                    }
                }

                /* 522 */
                int ia = alpha ? clamp((int) (a + 0.5D)) : 255;
                /* 523 */
                int ir = clamp((int) (r + 0.5D));
                /* 524 */
                int ig = clamp((int) (g + 0.5D));
                /* 525 */
                int ib = clamp((int) (b + 0.5D));
                /* 526 */
                outPixels[index++] = ia << 24 | ir << 16 | ig << 8 | ib;
            }
        }
    }

    public static int clamp(int c) {
        if (c < 0) {
            return 0;
        }

        return Math.min(c, 255);
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        /* 183 */
        int width = src.getWidth();
        /* 184 */
        int height = src.getHeight();

        /* 186 */
        if (dst == null) {
            /* 188 */
            dst = createCompatibleDestImage(src, null);
        }

        /* 191 */
        int[] inPixels = new int[width * height];
        /* 192 */
        int[] outPixels = new int[width * height];
        /* 193 */
        getRGB(src, 0, 0, width, height, inPixels);

        /* 195 */
        if (this.premultiplyAlpha) {
            /* 197 */
            ImageMath.premultiply(inPixels, 0, inPixels.length);
        }

        /* 200 */
        convolve(this.kernel, inPixels, outPixels, width, height, this.alpha, this.edgeAction);

        /* 202 */
        if (this.premultiplyAlpha) {
            /* 204 */
            ImageMath.unpremultiply(outPixels, 0, outPixels.length);
        }

        /* 207 */
        setRGB(dst, 0, 0, width, height, outPixels);
        /* 208 */
        return dst;
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        /* 213 */
        if (dstCM == null) {
            /* 215 */
            dstCM = src.getColorModel();
        }

        /* 218 */
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
        /* 223 */
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        /* 228 */
        if (dstPt == null) {
            /* 230 */
            dstPt = new Point2D.Double();
        }

        /* 233 */
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        /* 234 */
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        /* 239 */
        return null;
    }

    public String toString() {
        /* 533 */
        return "Blur/Convolve...";
    }
}


