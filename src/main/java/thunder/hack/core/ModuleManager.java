package thunder.hack.core;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.*;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.*;
import thunder.hack.modules.combat.*;
import thunder.hack.modules.misc.*;
import thunder.hack.modules.movement.Timer;
import thunder.hack.modules.movement.*;
import thunder.hack.modules.player.*;
import thunder.hack.modules.render.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static thunder.hack.modules.Module.mc;

public class ModuleManager {
    public ArrayList<Module> modules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();

    public static LevitationControl levitationControl = new LevitationControl();
    public static InventoryCleaner inventoryCleaner = new InventoryCleaner();
    public static NoCommentExploit noCommentExploit = new NoCommentExploit();
    public static AntiFriendAttack antiFriendAttack = new AntiFriendAttack();
    public static TotemPopCounter totemPopCounter = new TotemPopCounter();
    public static HotbarReplenish hotbarReplenish = new HotbarReplenish();
    public static DurabilityAlert durabilityAlert = new DurabilityAlert();
    public static TimerIndicator timerIndicator = new TimerIndicator();
    public static ThunderHackGui thunderHackGui = new ThunderHackGui();
    public static NoServerRotate noServerRotate = new NoServerRotate();
    public static BreakHighLight breakHighLight = new BreakHighLight();
    public static BlockHighLight blockHighLight = new BlockHighLight();
    public static AntiBadEffects antiBadEffects = new AntiBadEffects();
    public static PortalGodMode portalGodMode = new PortalGodMode();
    public static OptifineCapes optifineCapes = new OptifineCapes();
    public static Notifications notifications = new Notifications();
    public static NoEntityTrace noEntityTrace = new NoEntityTrace();
    public static MessageAppend messageAppend = new MessageAppend();
    public static EntityControl entityControl = new EntityControl();
    public static ElytraReplace elytraReplace = new ElytraReplace();
    public static ChorusExploit chorusExploit = new ChorusExploit();
    public static Trajectories trajectories = new Trajectories();
    public static TargetStrafe targetStrafe = new TargetStrafe();
    public static RadarRewrite radarRewrite = new RadarRewrite();
    public static PVPResources pvpResources = new PVPResources();
    public static NoServerSlot noServerSlot = new NoServerSlot();
    public static NoPitchLimit noPitchLimit = new NoPitchLimit();
    public static NoCameraClip noCameraClip = new NoCameraClip();
    public static MainSettings mainSettings = new MainSettings();
    public static ItemScroller itemScroller = new ItemScroller();
    public static HitParticles hitParticles = new HitParticles();
    public static HitBoxTricks hitBoxTricks = new HitBoxTricks();
    public static ElytraRecast elytraRecast = new ElytraRecast();
    public static EbatteSratte ebatteSratte = new EbatteSratte();
    public static ChestStealer chestStealer = new ChestStealer();
    public static AutoTpAccept autoTpAccept = new AutoTpAccept();
    public static AntiSurround antiSurround = new AntiSurround();
    public static AntiServerRP antiServerRP = new AntiServerRP();
    public static WorldTweaks worldTweaks = new WorldTweaks();
    public static VisualRange visualRange = new VisualRange();
    public static Speedometer speedometer = new Speedometer();
    public static ReverseStep reverseStep = new ReverseStep();
    public static NoJumpDelay noJumpDelay = new NoJumpDelay();
    public static NameProtect nameProtect = new NameProtect();
    public static MiddleClick middleClick = new MiddleClick();
    public static LogoutSpots logoutSpots = new LogoutSpots();
    public static LagNotifier lagNotifier = new LagNotifier();
    public static BreadCrumbs breadCrumbs = new BreadCrumbs();
    public static AutoRespawn autoRespawn = new AutoRespawn();
    public static AutoCrystal autoCrystal = new AutoCrystal();
    public static WaterSpeed waterSpeed = new WaterSpeed();
    public static TriggerBot triggerBot = new TriggerBot();
    public static TPSCounter tpsCounter = new TPSCounter();
    public static StorageEsp storageEsp = new StorageEsp();
    public static StaffBoard staffBoard = new StaffBoard();
    public static PistonPush pistonPush = new PistonPush();
    public static PistonAura pistonAura = new PistonAura();
    public static NoInteract noInteract = new NoInteract();
    public static ModuleList moduleList = new ModuleList();
    public static KillEffect killEffect = new KillEffect();
    public static JumpCircle jumpCircle = new JumpCircle();
    public static HoleAnchor holeAnchor = new HoleAnchor();
    public static Fullbright fullbright = new Fullbright();
    public static FpsCounter fpsCounter = new FpsCounter();
    public static FakePlayer fakePlayer = new FakePlayer();
    public static ElytraSwap elytraSwap = new ElytraSwap();
    public static ElytraPlus elytraPlus = new ElytraPlus();
    public static CevBreaker cevBreaker = new CevBreaker();
    public static AutoSprint autoSprint = new AutoSprint();
    public static AutoGApple autoGApple = new AutoGApple();
    public static AutoAnchor autoAnchor = new AutoAnchor();
    public static AntiHunger antiHunger = new AntiHunger();
    public static Animations animations = new Animations();
    public static DamageFly damageFly = new DamageFly();
    public static WayPoints wayPoints = new WayPoints();
    public static WaterMark waterMark = new WaterMark();
    public static ViewModel viewModel = new ViewModel();
    public static TunnelEsp tunnelEsp = new TunnelEsp();
    public static TickShift tickShift = new TickShift();
    public static TargetHud targetHud = new TargetHud();
    public static SpeedMine speedMine = new SpeedMine();
    public static PotionHud potionHud = new PotionHud();
    public static PearlBait pearlBait = new PearlBait();
    public static PacketFly packetFly = new PacketFly();
    public static Multitask multitask = new Multitask();
    public static LegacyHud legacyHud = new LegacyHud();
    public static HudEditor hudEditor = new HudEditor();
    public static ElytraFix elytraFix = new ElytraFix();
    public static Crosshair crosshair = new Crosshair();
    public static Criticals criticals = new Criticals();
    public static ChatUtils chatUtils = new ChatUtils();
    public static AutoTotem autoTotem = new AutoTotem();
    public static AutoLeave autoLeave = new AutoLeave();
    public static AutoFlyme autoFlyme = new AutoFlyme();
    public static AutoArmor autoArmor = new AutoArmor();
    public static ViewLock viewLock = new ViewLock();
    public static Velocity velocity = new Velocity();
    public static Tooltips tooltips = new Tooltips();
    public static Surround surround = new Surround();
    public static Scaffold scaffold = new Scaffold();
    public static PopChams popChams = new PopChams();
    public static NoRender noRender = new NoRender();
    public static NameTags nameTags = new NameTags();
    public static LongJump longJump = new LongJump();
    public static KeyBinds keyBinds = new KeyBinds();
    public static HoleSnap holeSnap = new HoleSnap();
    public static HoleFill holeFill = new HoleFill();
    public static HitSound hitSound = new HitSound();
    public static FGHelper fgHelper = new FGHelper();
    public static ExtraTab extraTab = new ExtraTab();
    public static EZbowPOP eZbowPOP = new EZbowPOP();
    public static ClickGui clickGui = new ClickGui();
    public static AutoTrap autoTrap = new AutoTrap();
    public static AutoTool autoTool = new AutoTool();
    public static AutoSoup autoSoup = new AutoSoup();
    public static AutoMend autoMend = new AutoMend();
    public static AutoFish autoFish = new AutoFish();
    public static AutoBuff autoBuff = new AutoBuff();
    public static AutoAuth autoAuth = new AutoAuth();
    public static ArmorHud armorHud = new ArmorHud();
    public static AirPlace airPlace = new AirPlace();
    public static VoidESP voidESP = new VoidESP();
    public static Tracker tracker = new Tracker();
    public static TpsSync tpsSync = new TpsSync();
    public static Spammer spammer = new Spammer();
    public static Shaders shaders = new Shaders();
    public static PingHud pingHud = new PingHud();
    public static ItemESP itemESP = new ItemESP();
    public static HoleESP holeESP = new HoleESP();
    public static GuiMove guiMove = new GuiMove();
    public static FreeCam freeCam = new FreeCam();
    public static FastUse fastUse = new FastUse();
    public static BowSpam bowSpam = new BowSpam();
    public static BoatFly boatFly = new BoatFly();
    public static Blocker blocker = new Blocker();
    public static AutoWeb autoWeb = new AutoWeb();
    public static AntiWeb antiWeb = new AntiWeb();
    public static AntiBot antiBot = new AntiBot();
    public static AntiAim antiAim = new AntiAim();
    public static DropAll dropAll = new DropAll();
    public static AutoSex autoSex = new AutoSex();
    public static XCarry xCarry = new XCarry();
    public static Trails trails = new Trails();
    public static Strafe strafe = new Strafe();
    public static Spider spider = new Spider();
    public static Search search = new Search();
    public static NoSlow noSlow = new NoSlow();
    public static NoFall noFall = new NoFall();
    public static Hotbar hotbar = new Hotbar();
    public static HitBox hitBox = new HitBox();
    public static Flight flight = new Flight();
    public static Coords coords = new Coords();
    public static Burrow burrow = new Burrow();
    public static AutoEZ autoEZ = new AutoEZ();
    public static AimBot aimBot = new AimBot();
    public static Timer timer = new Timer();
    public static Speed speed = new Speed();
    public static Reach reach = new Reach();
    public static Radar radar = new Radar();
    public static Nuker nuker = new Nuker();
    public static Media media = new Media();
    public static Ghost ghost = new Ghost();
    public static Chams chams = new Chams();
    public static Blink blink = new Blink();
    public static XRay xray = new XRay();
    public static Step step = new Step();
    public static Aura aura = new Aura();
    public static RPC rpc = new RPC();
    public static FOV fov = new FOV();
    public static ESP esp = new ESP();


    public ModuleManager() {
        for (Field field : getClass().getDeclaredFields()) {
            if (Module.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    modules.add((Module) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Module get(String name) {
        for (Module module : modules) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : modules) {
            if (!module.isEnabled()) continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesCategory = new ArrayList<>();
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
        modules.stream().filter(Module::listening).forEach((ThunderHack.EVENT_BUS)::subscribe);
        modules.forEach(Module::onLoad);

        if (ConfigManager.firstLaunch) {
            ModuleManager.notifications.enable();
            rpc.enable();
        }
    }

    public void onUpdate() {
        if (Module.fullNullCheck()) return;
        modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
    }

    public void onTick() {
        modules.stream().filter(Module::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(DrawContext context) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender2D(context));
        ThunderHack.core.onRender2D(context);
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

    public void onPostRender3D(MatrixStack stack) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onPostRender3D(stack));
        ThunderHack.shaderManager.renderShaders();
    }

    public void sortModules() {
        sortedModules = getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> FontRenderers.getSettingsRenderer().getStringWidth(module.getFullArrayString()) * -1)).collect(Collectors.toList());
    }

    public void onLogout() {
        modules.forEach(Module::onLogout);
    }

    public void onLogin() {
        modules.forEach(Module::onLogin);
    }

    public void onUnload() {
        modules.forEach(module -> {
            if (module.isEnabled()) {
                ThunderHack.EVENT_BUS.unsubscribe(module);
            }
        });
        modules.forEach(Module::onUnload);
    }

    public void onUnloadPost() {
        for (Module module : modules) {
            module.setEnabled(false);
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
            if (module.getName().toLowerCase().contains(string.toLowerCase()))
                modulesCategory.add(module);
        });

        modules.forEach(module -> {
            if (module.getDescription().toLowerCase().contains(string.toLowerCase()))
                modulesCategory.add(module);
        });

        return modulesCategory;
    }
}
