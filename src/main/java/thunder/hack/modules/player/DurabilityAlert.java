package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.Util;

import java.awt.*;

public class DurabilityAlert extends Module {
    public DurabilityAlert() {
        super("DurabilityAlert", "durability alert", Category.PLAYER);
    }

    public Setting<Boolean> friends = new Setting<>("Friend message", true);
    public Setting<Integer> percent = new Setting<>("Percent", 20, 1, 100);
    private final Identifier ICON = new Identifier("textures/broken_shield.png");
    private boolean need_alert = false;
    private Timer timer = new Timer();

    @Override
    public void onUpdate() {
        if(friends.getValue()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!Thunderhack.friendManager.isFriend(player)) continue;
                if (player == mc.player) continue;
                for (ItemStack stack : player.getInventory().armor) {
                    if (stack.isEmpty()) continue;
                    if (getDurability(stack) < percent.getValue() && timer.passedMs(30000)) {
                        if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                            mc.player.networkHandler.sendChatCommand("msg " + player.getName() + " Срочно чини броню!");
                        } else {
                            mc.player.networkHandler.sendChatCommand("/msg " + player.getName() + " Repair your armor immediately!");
                        }
                        timer.reset();
                    }
                }
            }
        }

        boolean flag = false;
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.isEmpty()) continue;
            if(getDurability(stack) < percent.getValue()){
                need_alert = true;
                flag = true;
            }
        }
        if(!flag && need_alert){
            need_alert = false;
        }

    }

    @Subscribe
    public void onRender2D(Render2DEvent e){
        if(need_alert) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                FontRenderers.sf_bold.drawCenteredString(e.getMatrixStack(),"Срочно чини броню!", (float) Util.getScaledResolution().getScaledWidth() / 2f, (float) Util.getScaledResolution().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            } else {
                FontRenderers.sf_bold.drawCenteredString(e.getMatrixStack(),"Repair your armor immediately!", (float) Util.getScaledResolution().getScaledWidth() / 2f, (float) Util.getScaledResolution().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());

                //new Color(0xFFDF00)
            }
            Color c1 = new Color(0xFFDF00);
            RenderSystem.setShaderColor(c1.getRed() / 255f,c1.getGreen() / 255f,c1.getBlue()/255f,1f);
            e.getContext().drawTexture(ICON, (int) (Util.getScaledResolution().getScaledWidth() / 2f - 40), (int) (Util.getScaledResolution().getScaledHeight() / 3f - 120), 80, 80,0,0,80,80,80,80);
            RenderSystem.setShaderColor(1f,1f,1f,1f);
        }
    }


    public static int getDurability(ItemStack stack) {
        return (int) ((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0f);
    }
}
