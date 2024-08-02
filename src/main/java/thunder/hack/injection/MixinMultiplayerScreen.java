package thunder.hack.injection;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.gui.windows.impl.*;

import static thunder.hack.core.manager.IManager.mc;

@Mixin(MultiplayerScreen.class)
public abstract class MixinMultiplayerScreen extends Screen {
    public MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void initHook(CallbackInfo ci) {
        ButtonWidget.Builder builder = ButtonWidget.builder(Text.literal("⚡"), button -> mc.setScreen(
                new WindowsScreen(
                        MacroWindow.get(ModuleManager.windows.macroPos.getValue().getX() * mc.getWindow().getScaledWidth(), ModuleManager.windows.macroPos.getValue().getY() * mc.getWindow().getScaledHeight(), ModuleManager.windows.macroPos),
                        ConfigWindow.get(ModuleManager.windows.configPos.getValue().getX() * mc.getWindow().getScaledWidth(), ModuleManager.windows.configPos.getValue().getY() * mc.getWindow().getScaledHeight(), ModuleManager.windows.configPos),
                        FriendsWindow.get(ModuleManager.windows.friendPos.getValue().getX() * mc.getWindow().getScaledWidth(), ModuleManager.windows.friendPos.getValue().getY() * mc.getWindow().getScaledHeight(), ModuleManager.windows.friendPos),
                        WaypointWindow.get(ModuleManager.windows.waypointPos.getValue().getX() * mc.getWindow().getScaledWidth(), ModuleManager.windows.waypointPos.getValue().getY() * mc.getWindow().getScaledHeight(), ModuleManager.windows.waypointPos),
                        ProxyWindow.get(ModuleManager.windows.proxyPos.getValue().getX() * mc.getWindow().getScaledWidth(), ModuleManager.windows.proxyPos.getValue().getY() * mc.getWindow().getScaledHeight(), ModuleManager.windows.proxyPos)
                ))).size(60, 20);
        if (!ModuleManager.unHook.isEnabled())
            addDrawableChild(builder.position(width - 65, height - 25).build());
    }
}