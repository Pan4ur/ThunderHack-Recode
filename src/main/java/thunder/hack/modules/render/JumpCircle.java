package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import thunder.hack.modules.Module;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AstolfoAnimation;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.modules.movement.ElytraPlus.lerp;


public class JumpCircle extends Module {

    public Setting<cmode> CMode = new Setting<>("ColorMode", cmode.Astolfo);
    public Setting<Integer> lifetime = new Setting<>("live", 3, 1, 10);
    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978));
    public final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(3646789));
    public final Setting<Integer> colorOffset1 = new Setting<>("ColorOffset", 10, 1, 20);

    public JumpCircle() {
        super("JumpCircle", "JumpCircle", Category.RENDER);
    }

    public static AstolfoAnimation astolfo = new AstolfoAnimation();
    static List<Circle> circles = new ArrayList<>();
    private Map<PlayerEntity, Boolean> cache = new ConcurrentHashMap<>();

    @Override
    public void onUpdate() {

        for(PlayerEntity pl : mc.world.getPlayers()){
            if(!cache.containsKey(pl) && pl.isOnGround())
                cache.put(pl, true);
        }

        cache.forEach((pl, pg) -> {
            if(pl != null){
                if(!pl.isOnGround()){
                    circles.add(new Circle(new Vec3d(pl.getX(),pl.getY() - 0.3f,pl.getZ())));
                    cache.remove(pl);
                }
            }
        });

        astolfo.update();
        for (Circle circle : circles) {
            circle.update();
        }
        circles.removeIf(Circle::update);
       // mc.player.distanceTraveled = 4f;
    }


    public void onPreRender3D(MatrixStack stack) {
        Collections.reverse(circles);
        try {
            for (Circle c : circles) {
                double x = c.position().x - mc.getEntityRenderDispatcher().camera.getPos().getX();
                double y = c.position().y - mc.getEntityRenderDispatcher().camera.getPos().getY();
                double z = c.position().z - mc.getEntityRenderDispatcher().camera.getPos().getZ();
                float k = (float) c.timer.getPassedTimeMs() / (float) (lifetime.getValue() * 1000);
                float start = k * 2.2f;
                float middle = (start + k) / 2;

                stack.push();
                stack.translate(x,y,z);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuffer();
                Render3DEngine.setup();
                RenderSystem.disableDepthTest();
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i <= 360; i += 5) {
                    int clr = getColor(i);
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * start, (float) 0, (float) v * start).color(Render2DEngine.injectAlpha(new Color(clr),0).getRGB()).next();
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DEngine.injectAlpha(new Color(clr), (int) (150f * (1.0F - (float) c.timer.getPassedTimeMs() / (float) (lifetime.getValue() * 1000)))).getRGB()).next();
                }
                tessellator.draw();
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i <= 360; i += 5) {
                    int clr = getColor(i);
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));

                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DEngine.injectAlpha(new Color(clr),(int) (150  * (1.0F - (float) c.timer.getPassedTimeMs() / (float) (lifetime.getValue() * 1000)))).getRGB()).next();
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * k, (float) 0, (float) v * k).color(Render2DEngine.injectAlpha(new Color(clr), 0).getRGB()).next();
                }
                tessellator.draw();

                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i <= 360; i += 5) {
                    int clr = getColor(i);
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));

                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DEngine.injectAlpha(new Color(clr),(int) (255f  * (1.0F - (float) c.timer.getPassedTimeMs() / (float) (lifetime.getValue() * 1000)))).getRGB()).next();
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * (middle - 0.04f), (float) 0, (float) v * (middle - 0.04f)).color(Render2DEngine.injectAlpha(new Color(clr), 0).getRGB()).next();
                }
                tessellator.draw();

                Render3DEngine.cleanup();
                RenderSystem.enableDepthTest();
                stack.translate(-x,-y,-z);
                stack.pop();
            }
        } catch (Exception e) {}
        Collections.reverse(circles);
    }


    public int getColor(int stage){
        if(CMode.getValue() == cmode.Astolfo){
            return astolfo.getColor(((stage + 90) / 360.));
        }
        else if(CMode.getValue() == cmode.Rainbow){
            return Render2DEngine.rainbow(stage,1f,1f).getRGB();
        }
        else if (CMode.getValue() == cmode.Custom){
            return color.getValue().getColorObject().getRGB();
        }
        else {
            return getColor2(color.getValue().getColorObject(),color2.getValue().getColorObject(),stage).getRGB();
        }
    }


    private Color getColor2(Color color1, Color color2, int offset){
        return TwoColoreffect(color1, color2, Math.abs(System.currentTimeMillis() / 10) / 100.0 + offset * ((20f - colorOffset1.getValue()) / 200) );
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed) {
        double thing = speed / 4.0 % 1.0;
        float val = MathHelper.clamp((float) Math.sin(Math.PI * 6 * thing) / 2.0f + 0.5f, 0.0f, 1.0f);
        return new Color(lerp((float) cl1.getRed() / 255.0f, (float) cl2.getRed() / 255.0f, val), lerp((float) cl1.getGreen() / 255.0f, (float) cl2.getGreen() / 255.0f, val), lerp((float) cl1.getBlue() / 255.0f, (float) cl2.getBlue() / 255.0f, val));
    }

    public enum cmode {
        Custom, Rainbow,TwoColor, Astolfo
    }

    class Circle {
        private final Vec3d vec;
        private final thunder.hack.utility.Timer timer = new thunder.hack.utility.Timer();

        Circle(Vec3d vec) {
            this.vec = vec;
            timer.reset();
        }

        Vec3d position() {
            return this.vec;
        }

        public boolean update() {
            return timer.passedMs(lifetime.getValue() * 10000);
        }
    }
}
