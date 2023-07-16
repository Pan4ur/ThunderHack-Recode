package thunder.hack.core;

import com.google.common.eventbus.EventBus;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.*;
import thunder.hack.notification.NotificationManager;
import thunder.hack.utility.Util;

import thunder.hack.modules.Module;
import thunder.hack.gui.hud.impl.*;
import thunder.hack.modules.combat.*;
import thunder.hack.modules.misc.*;
import thunder.hack.modules.movement.*;
import thunder.hack.modules.player.*;
import thunder.hack.modules.render.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    public ArrayList<Module> modules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new ClickGui());
        modules.add(new MainSettings());
        modules.add(new RPC());
        modules.add(new HudEditor());
        modules.add(new RadarRewrite());
        modules.add(new GuiMove());
        modules.add(new NoSlow());
        modules.add(new AutoSprint());
        modules.add(new Timer());
        modules.add(new ViewModel());
        modules.add(new TpsSync());
        modules.add(new Scaffold());
        modules.add(new Notifications());
        modules.add(new FpsCounter());
        modules.add(new TPSCounter());
        modules.add(new Coords());
        modules.add(new Speedometer());
        modules.add(new Speed());
        modules.add(new Velocity());
        modules.add(new Aura());
        modules.add(new AntiBot());
        modules.add(new Spammer());
        modules.add(new AutoFlyme());
        modules.add(new NoRender());
        modules.add(new FastUse());
        modules.add(new NoJumpDelay());
        modules.add(new BowSpam());
        modules.add(new FreeCam());
        modules.add(new AutoCrystal());
        modules.add(new FakePlayer());
        modules.add(new ModuleList());
        modules.add(new NameTags());
        modules.add(new PacketFly());
        modules.add(new Strafe());
        modules.add(new Surround());
        modules.add(new AutoTotem());
        modules.add(new Criticals());
        modules.add(new Fullbright());
        modules.add(new NoEntityTrace());
        modules.add(new EZbowPOP());
        modules.add(new AutoBuff());
        modules.add(new PingHud());
        modules.add(new TargetHud());
        modules.add(new KeyBinds());
        modules.add(new WaterMark());
        modules.add(new PotionHud());
        modules.add(new MiddleClick());
        modules.add(new NoServerSlot());
        modules.add(new Trails());
        modules.add(new AutoTpAccept());
        modules.add(new AutoAuth());
        modules.add(new JumpCircle());
        modules.add(new NoInteract());
        modules.add(new SpeedMine());
        modules.add(new NoCameraClip());
        modules.add(new HitSound());
        modules.add(new HitParticles());
        modules.add(new StorageEsp());
        modules.add(new AutoGApple());
        modules.add(new ItemScroller());
        modules.add(new TargetStrafe());
        modules.add(new Animations());
        modules.add(new AntiAim());
        modules.add(new AimBot());
        modules.add(new StaffBoard());
        modules.add(new AutoTool());
        modules.add(new Search());
        modules.add(new ChestStealer());
        modules.add(new Tooltips());
        modules.add(new AutoFish());
        modules.add(new WorldTweaks());
        modules.add(new ItemESP());
        modules.add(new AutoLeave());
        modules.add(new FGHelper());
        modules.add(new ElytraPlus());
        modules.add(new AutoArmor());
        modules.add(new TimerIndicator());
        modules.add(new ChorusExploit());
        modules.add(new Hotbar());
        modules.add(new Blink());
        modules.add(new AutoTrap());
        modules.add(new AutoWeb());
        modules.add(new HoleFill());
        modules.add(new VelocityIndicator());
        modules.add(new NameProtect());
        modules.add(new TestHud());
        modules.add(new BoatFly());
        modules.add(new HitBoxDesync());
        modules.add(new HoleEsp());
        modules.add(new ArmorHud());
        modules.add(new Step());
        modules.add(new ReverseStep());
        modules.add(new Flight());
        modules.add(new LevitationControl());
        modules.add(new AntiFriendAttack());
        modules.add(new AutoAnchor());
        modules.add(new PVPResources());
        modules.add(new Blocker());
        modules.add(new NoPitchLimit());
        modules.add(new HotbarReplenish());
        modules.add(new HoleAnchor());
        modules.add(new PopChams());
        modules.add(new PistonAura());
        modules.add(new Burrow());
        modules.add(new AntiSurround());
        modules.add(new LongJump());
        modules.add(new Chams());
        modules.add(new ThunderHackGui());
        modules.add(new NoServerRotate());
        modules.add(new AutoEZ());
        modules.add(new EbatteSratte());
        modules.add(new MessageAppend());
        modules.add(new AntiBadEffects());
        modules.add(new Ghost());
        modules.add(new ExtraTab());
        modules.add(new TriggerBot());
        modules.add(new Reach());
        modules.add(new HitBox());
        modules.add(new TotemPopCounter());
        modules.add(new Crosshair());
        modules.add(new XRay());
        modules.add(new Multitask());
        modules.add(new TunnelEsp());
        modules.add(new ElytraSwap());
        modules.add(new OptifineCapes());
        modules.add(new Radar());
        modules.add(new DurabilityAlert());
        modules.add(new LogoutSpots());
        modules.add(new EntityControl());
        modules.add(new PortalGodMode());
        modules.add(new BlockHighLight());
        modules.add(new BreakHighLight());

    }

    public Module get(String name) {
        for (Module module : modules) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public <T extends Module> T get(Class<T> clazz) {
        for (Module module : modules) {
            if (!clazz.isInstance(module)) continue;
            return (T) module;
        }
        return null;
    }

    public Module getModuleByDisplayName(String displayName) {
        for (Module module : modules) {
            if (!module.getDisplayName().equalsIgnoreCase(displayName)) continue;
            return module;
        }
        return null;
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<Module>();
        for (Module module : modules) {
            if (!module.isEnabled()) continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesCategory = new ArrayList<Module>();
        modules.forEach(module -> {
            if (module.getCategory() == category) {
                modulesCategory.add(module);
            }
        });
        return modulesCategory;
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        modules.sort(Comparator.comparing(Module::getName));
        modules.stream().filter(Module::listening).forEach(((EventBus) Thunderhack.EVENT_BUS)::register);
        modules.forEach(Module::onLoad);

        if(Thunderhack.configManager.firstLaunch){
            Thunderhack.moduleManager.get(Notifications.class).setEnabled(true);
            Thunderhack.moduleManager.get(RPC.class).setEnabled(true);
        }
    }

    public void onUpdate() {
        if(Module.fullNullCheck()) return;
        modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
    }

    public void onTick() {
        modules.stream().filter(Module::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender3D(event));
    }

    public void sortModules() {
        sortedModules = getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> FontRenderers.getRenderer().getStringWidth(module.getFullArrayString()) * -1)).collect(Collectors.toList());
    }

    public void onLogout() {
        modules.forEach(Module::onLogout);
    }

    public void onLogin() {
        modules.forEach(Module::onLogin);
    }

    public void onUnload() {
        modules.forEach(module -> {
            if(module.isEnabled()){
                Thunderhack.EVENT_BUS.unregister(module);
            }
        });
        modules.forEach(Module::onUnload);
    }

    public void onUnloadPost() {
        for (Module module : modules) {
            module.enabled.setValue(false);
        }
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey == 0 || Util.mc.currentScreen instanceof ClickUI) {
            return;
        }
        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
                module.toggle();
            }
        });
    }

    public ArrayList<Module> getModulesSearch(String string) {
        ArrayList<Module> modulesCategory = new ArrayList<>();
        modules.forEach(module -> {
            if (module.getName().toLowerCase().contains(string.toLowerCase())) {
                modulesCategory.add(module);
            }
        });

        modules.forEach(module -> {
            if (module.getDescription().toLowerCase().contains(string.toLowerCase())) {
                modulesCategory.add(module);
            }
        });

        return modulesCategory;
    }
}
