package thunder.hack.utility.render.gaussianblur;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;


public abstract class AbstractBufferedImageOp implements BufferedImageOp, Cloneable {
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }

        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }


    public Rectangle2D getBounds2D(BufferedImage src) {
        /*  40 */
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }


    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }

        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }


    public RenderingHints getRenderingHints() {
        /*  56 */
        return null;
    }


    public int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();

        if (type == 2 || type == 1) {
            return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
        }

        return image.getRGB(x, y, width, height, pixels, 0, width);
    }

    public void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();

        if (type == 2 || type == 1) {

            image.getRaster().setDataElements(x, y, width, height, pixels);
        } else {

            image.setRGB(x, y, width, height, pixels, 0, width);
        }
    }


    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {

            return null;
        }
    }
}
