package thunder.hack.core;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.Thunderhack;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.*;
import thunder.hack.modules.movement.Timer;

import thunder.hack.modules.Module;
import thunder.hack.gui.hud.impl.*;
import thunder.hack.modules.combat.*;
import thunder.hack.modules.misc.*;
import thunder.hack.modules.movement.*;
import thunder.hack.modules.player.*;
import thunder.hack.modules.render.*;


import java.util.*;
import java.util.stream.Collectors;

import static thunder.hack.modules.Module.mc;

public class ModuleManager {
    public ArrayList<Module> modules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();

    public static Tracker tracker;
    public static ClickGui clickGui;
    public static NoRender noRender;
    public static Chams chams;
    public static Notifications notifications;
    public static Aura aura;
    public static Media media;
    public static XRay xray;
    public static Tooltips tooltips;
    public static Shaders shaders;
    public static Fullbright fullbright;
    public static Reach reach;
    public static HitBox hitBox;
    public static NoEntityTrace noEntityTrace;
    public static AutoTool autoTool;
    public static Hotbar hotbar;
    public static Velocity velocity;
    public static OptifineCapes optifineCapes;
    public static EntityControl entityControl;
    public static NoCameraClip noCameraClip;
    public static NoSlow noSlow;
    public static NoInteract noInteract;
    public static SpeedMine speedMine;
    public static AntiBot antiBot;
    public static NameProtect nameProtect;
    public static AutoSprint autoSprint;
    public static FreeCam freeCam;
    public static Crosshair crosshair;
    public static NoPitchLimit noPitchLimit;
    public static NameTags nameTags;
    public static ViewModel viewModel;
    public static ItemScroller itemScroller;
    public static ExtraTab extraTab;
    public static Animations animations;
    public static AutoMend autoMend;
    public static PotionHud potionHud;
    public static AutoCrystal autoCrystal;
    public static FOV fov;
    public static TpsSync tpsSync;
    public static NoCommentExploit noCommentExploit;


    public ModuleManager() {
        tracker = new Tracker();
        clickGui = new ClickGui();
        noRender = new NoRender();
        chams = new Chams();
        notifications = new Notifications();
        aura = new Aura();
        media = new Media();
        xray = new XRay();
        shaders = new Shaders();
        tooltips = new Tooltips();
        fullbright = new Fullbright();
        noEntityTrace = new NoEntityTrace();
        hitBox = new HitBox();
        reach = new Reach();
        autoTool = new AutoTool();
        hotbar = new Hotbar();
        velocity = new Velocity();
        optifineCapes = new OptifineCapes();
        entityControl = new EntityControl();
        noCameraClip = new NoCameraClip();
        noSlow = new NoSlow();
        noInteract = new NoInteract();
        speedMine = new SpeedMine();
        antiBot = new AntiBot();
        nameProtect = new NameProtect();
        crosshair = new Crosshair();
        freeCam = new FreeCam();
        autoSprint = new AutoSprint();
        nameTags = new NameTags();
        noPitchLimit = new NoPitchLimit();
        itemScroller = new ItemScroller();
        viewModel = new ViewModel();
        extraTab = new ExtraTab();
        animations = new Animations();
        autoMend = new AutoMend();
        potionHud = new PotionHud();
        autoCrystal = new AutoCrystal();
        fov = new FOV();
        tpsSync = new TpsSync();
        noCommentExploit = new NoCommentExploit();

        modules.add(clickGui);
        modules.add(new MainSettings());
        modules.add(new RPC());
        modules.add(new HudEditor());
        modules.add(new RadarRewrite());
        modules.add(new GuiMove());
        modules.add(noSlow);
        modules.add(autoSprint);
        modules.add(new Timer());
        modules.add(viewModel);
        modules.add(tpsSync);
        modules.add(new Scaffold());
        modules.add(notifications);
        modules.add(new FpsCounter());
        modules.add(new TPSCounter());
        modules.add(new Coords());
        modules.add(new Speedometer());
        modules.add(new Speed());
        modules.add(velocity);
        modules.add(aura);
        modules.add(antiBot);
        modules.add(new Spammer());
        modules.add(new AutoFlyme());
        modules.add(noRender);
        modules.add(new FastUse());
        modules.add(new NoJumpDelay());
        modules.add(new BowSpam());
        modules.add(freeCam);
        modules.add(autoCrystal);
        modules.add(new FakePlayer());
        modules.add(new ModuleList());
        modules.add(nameTags);
        modules.add(new PacketFly());
        modules.add(new Strafe());
        modules.add(new Surround());
        modules.add(new AutoTotem());
        modules.add(new Criticals());
        modules.add(new CevBreaker());
        modules.add(fullbright);
        modules.add(noEntityTrace);
        modules.add(new EZbowPOP());
        modules.add(new AutoBuff());
        modules.add(new PingHud());
        modules.add(new TargetHud());
        modules.add(new KeyBinds());
        modules.add(new WaterMark());
        modules.add(potionHud);
        modules.add(new MiddleClick());
        modules.add(new NoServerSlot());
        modules.add(new Trails());
        modules.add(new AutoTpAccept());
        modules.add(new AutoAuth());
        modules.add(new JumpCircle());
        modules.add(noInteract);
        modules.add(speedMine);
        modules.add(noCameraClip);
        modules.add(new HitSound());
        modules.add(new HitParticles());
        modules.add(new StorageEsp());
        modules.add(new LagNotifier());
        modules.add(new AutoGApple());
        modules.add(itemScroller);
        modules.add(new TargetStrafe());
        modules.add(animations);
        modules.add(new AntiAim());
        modules.add(new AimBot());
        modules.add(new StaffBoard());
        modules.add(autoTool);
        modules.add(new Search());
        modules.add(new ChestStealer());
        modules.add(tooltips);
        modules.add(new AutoFish());
        modules.add(new WorldTweaks());
        modules.add(new ItemESP());
        modules.add(new AutoLeave());
        modules.add(new FGHelper());
        modules.add(new ElytraPlus());
        modules.add(new AutoArmor());
        modules.add(new TimerIndicator());
        modules.add(new ChorusExploit());
        modules.add(hotbar);
        modules.add(new Blink());
        modules.add(new AutoTrap());
        modules.add(new AutoWeb());
        modules.add(new HoleFill());
        modules.add(nameProtect);
        modules.add(new TestHud());
        modules.add(new BoatFly());
        modules.add(new HitBoxDesync());
        modules.add(new HoleESP());
        modules.add(new ArmorHud());
        modules.add(new Step());
        modules.add(new ReverseStep());
        modules.add(new Flight());
        modules.add(new LevitationControl());
        modules.add(new AntiFriendAttack());
        modules.add(new AutoAnchor());
        modules.add(new PVPResources());
        modules.add(new Blocker());
        modules.add(noPitchLimit);
        modules.add(new HotbarReplenish());
        modules.add(new HoleAnchor());
        modules.add(new PopChams());
        modules.add(new PistonAura());
        modules.add(new Burrow());
        modules.add(new AntiSurround());
        modules.add(new LongJump());
        modules.add(chams);
        modules.add(new ThunderHackGui());
        modules.add(new NoServerRotate());
        modules.add(new AutoEZ());
        modules.add(new EbatteSratte());
        modules.add(new MessageAppend());
        modules.add(new AntiBadEffects());
        modules.add(new Ghost());
        modules.add(extraTab);
        modules.add(new TriggerBot());
        modules.add(reach);
        modules.add(hitBox);
        modules.add(new TotemPopCounter());
        modules.add(crosshair);
        modules.add(xray);
        modules.add(new Multitask());
        modules.add(new TunnelEsp());
        modules.add(new ElytraSwap());
        modules.add(optifineCapes);
        modules.add(new Radar());
        modules.add(new DurabilityAlert());
        modules.add(new LogoutSpots());
        modules.add(entityControl);
        modules.add(new PortalGodMode());
        modules.add(new BlockHighLight());
        modules.add(new BreakHighLight());
        modules.add(new AutoRespawn());
        modules.add(shaders);
        modules.add(new Trajectories());
        modules.add(media);
        modules.add(tracker);
        modules.add(new BreadCrumbs());
        modules.add(new VisualRange());
        modules.add(new WayPoints());
        modules.add(new Nuker());
        modules.add(new AntiHunger());
        modules.add(new AutoSoup());
        modules.add(new VoidESP());
        modules.add(fov);
        modules.add(new PearlBait());
        modules.add(new XCarry());
        modules.add(autoMend);
        modules.add(new Spider());
        modules.add(new HoleSnap());
        modules.add(new ChatUtils());
        modules.add(new ESP());
        modules.add(new ElytraFix());
        modules.add(new AirPlace());
        modules.add(noCommentExploit);
        modules.add(new PistonPush());
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
        modules.stream().filter(Module::listening).forEach((Thunderhack.EVENT_BUS)::subscribe);
        modules.forEach(Module::onLoad);

        if(Thunderhack.configManager.firstLaunch){
            ModuleManager.notifications.setEnabled(true);
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

    public void onRender2D(DrawContext context) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender2D(context));
    }

    public void onRenderShaders(DrawContext context) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRenderShaders(context));
    }

    public void onRender3D(MatrixStack stack) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender3D(stack));
    }

    public void onPreRender3D(MatrixStack stack) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onPreRender3D(stack));
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
                Thunderhack.EVENT_BUS.unsubscribe(module);
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
        if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickUI) {
            return;
        }
        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
                module.toggle();
            }
        });
    }

    public void onKeyReleased(int eventKey) {
        if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickUI) {
            return;
        }
        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && module.getBind().isHold()) {
                module.disable();
            }
        });
    }

    public void onMoseKeyPressed(int eventKey) {
        if (eventKey == -1 || mc.currentScreen instanceof ClickUI) {
            return;
        }
        modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey)) {
                module.toggle();
            }
        });
    }

    public void onMoseKeyReleased(int eventKey) {
        if (eventKey == -1 || mc.currentScreen instanceof ClickUI) {
            return;
        }
        modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey) && module.getBind().isHold()) {
                module.disable();
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
