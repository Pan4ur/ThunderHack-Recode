package thunder.hack.features.modules.player;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class DurabilityAlert extends Module {
    public DurabilityAlert() {
        super("DurabilityAlert", Category.PLAYER);
    }

    private final Setting<Boolean> friends = new Setting<>("Friend message", true);
    private final Setting<Integer> percent = new Setting<>("Percent", 20, 1, 100);
    private boolean need_alert = false;
    private final Timer timer = new Timer();

    @Override
    public void onUpdate() {
        if (friends.getValue()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!Managers.FRIEND.isFriend(player)) continue;
                if (player == mc.player) continue;
                for (ItemStack stack : player.getInventory().armor) {
                    if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;
                    if (getDurability(stack) < percent.getValue() && timer.passedMs(30000)) {
                        mc.player.networkHandler.sendChatCommand("msg " + player.getName().getString() + (isRu() ? " Срочно чини броню!" : " Fix your armor right now!"));

                        timer.reset();
                    }
                }
            }
        }

        boolean flag = false;
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;
            if (getDurability(stack) < percent.getValue()) {
                need_alert = true;
                flag = true;
            }
        }
        if (!flag && need_alert) need_alert = false;
    }

    public void onRender2D(DrawContext context) {
        if (need_alert) {
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), isRu() ? "Срочно чини броню!" : "Fix your armor right now!", (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());

            Color c1 = new Color(0xFFDF00);
            RenderSystem.setShaderColor(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, 1f);
            context.drawTexture(TextureStorage.brokenShield, (int) (mc.getWindow().getScaledWidth() / 2f - 40), (int) (mc.getWindow().getScaledHeight() / 3f - 120), 80, 80, 0, 0, 80, 80, 80, 80);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    public static int getDurability(ItemStack stack) {
        return (int) ((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0f);
    }
}
