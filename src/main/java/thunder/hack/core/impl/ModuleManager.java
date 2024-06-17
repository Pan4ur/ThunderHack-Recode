package thunder.hack.core.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.IManager;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.gui.hud.impl.*;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.*;
import thunder.hack.modules.combat.*;
import thunder.hack.modules.misc.*;
import thunder.hack.modules.movement.Timer;
import thunder.hack.modules.movement.*;
import thunder.hack.modules.player.*;
import thunder.hack.modules.render.Particles;
import thunder.hack.modules.render.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ModuleManager implements IManager {
    public ArrayList<Module> modules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();
    public List<Integer> activeMouseKeys = new ArrayList<>();

    public static LevitationControl levitationControl = new LevitationControl();
    public static InventoryCleaner inventoryCleaner = new InventoryCleaner();
    public static NoCommentExploit noCommentExploit = new NoCommentExploit();
    public static NoWaterCollision noWaterCollision = new NoWaterCollision();
    public static PortalInventory portalInventory = new PortalInventory();
    public static TotemPopCounter totemPopCounter = new TotemPopCounter();
    public static HotbarReplenish hotbarReplenish = new HotbarReplenish();
    public static DurabilityAlert durabilityAlert = new DurabilityAlert();
    public static AutoCrystalBase autoCrystalBase = new AutoCrystalBase();
    public static CrosshairArrows crosshairArrows = new CrosshairArrows();
    public static PearlBlockThrow pearlBlockThrow = new PearlBlockThrow();
    public static ChatTranslator chatTranslator = new ChatTranslator();
    public static ClientSettings clientSettings = new ClientSettings();
    public static TimerIndicator timerIndicator = new TimerIndicator();
    public static ThunderHackGui thunderHackGui = new ThunderHackGui();
    public static NoServerRotate noServerRotate = new NoServerRotate();
    public static BreakHighLight breakHighLight = new BreakHighLight();
    public static BlockHighLight blockHighLight = new BlockHighLight();
    public static AntiBadEffects antiBadEffects = new AntiBadEffects();
    public static MouseElytraFix mouseElytraFix = new MouseElytraFix();
    public static TotemAnimation totemAnimation = new TotemAnimation();
    public static PortalGodMode portalGodMode = new PortalGodMode();
    public static OptifineCapes optifineCapes = new OptifineCapes();
    public static Notifications notifications = new Notifications();
    public static NoEntityTrace noEntityTrace = new NoEntityTrace();
    public static MessageAppend messageAppend = new MessageAppend();
    public static EntityControl entityControl = new EntityControl();
    public static ElytraReplace elytraReplace = new ElytraReplace();
    public static ChorusExploit chorusExploit = new ChorusExploit();
    public static MoreKnockback moreKnockback = new MoreKnockback();
    public static AntiServerAdd antiServerAdd = new AntiServerAdd();
    public static AntiLegitMiss antiLegitMiss = new AntiLegitMiss();
    public static Trajectories trajectories = new Trajectories();
    public static TargetStrafe targetStrafe = new TargetStrafe();
    public static RadarRewrite radarRewrite = new RadarRewrite();
    public static PVPResources pvpResources = new PVPResources();
    public static NoServerSlot noServerSlot = new NoServerSlot();
    public static NoCameraClip noCameraClip = new NoCameraClip();
    public static ItemScroller itemScroller = new ItemScroller();
    public static HitParticles hitParticles = new HitParticles();
    public static ElytraRecast elytraRecast = new ElytraRecast();
    public static EbatteSratte ebatteSratte = new EbatteSratte();
    public static ChestStealer chestStealer = new ChestStealer();
    public static AutoTpAccept autoTpAccept = new AutoTpAccept();
    public static AntiServerRP antiServerRP = new AntiServerRP();
    public static TotemCounter totemCounter = new TotemCounter();
    public static PerfectDelay perfectDelay = new PerfectDelay();
    public static ChestCounter chestCounter = new ChestCounter();
    public static StashLogger stashLogger = new StashLogger();
    public static FastLatency fastLatency = new FastLatency();
    public static PearlChaser pearlChaser = new PearlChaser();
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
    public static EntitySpeed entitySpeed = new EntitySpeed();
    public static AspectRatio aspectRatio = new AspectRatio();
    public static ClientSpoof clientSpoof = new ClientSpoof();
    public static LegitHelper legitHelper = new LegitHelper();
    public static AutoAnchor autoAnchor = new AutoAnchor();
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
    public static AutoSprint autoSprint = new AutoSprint();
    public static AutoGApple autoGApple = new AutoGApple();
    public static AntiHunger antiHunger = new AntiHunger();
    public static Animations animations = new Animations();
    public static DamageTint damageTint = new DamageTint();
    public static AntiAttack antiAttack = new AntiAttack();
    public static GapplesHud gapplesHud = new GapplesHud();
    public static HitBubbles hitBubbles = new HitBubbles();
    public static AutoTrader autoTrader = new AutoTrader();
    public static KillStats killStats = new KillStats();
    public static AutoAnvil autoAnvil = new AutoAnvil();
    public static CandleHud candleHud = new CandleHud();
    public static Particles particles = new Particles();
    public static ToolSaver toolSaver = new ToolSaver();
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
    public static MultiTask multitask = new MultiTask();
    public static LegacyHud legacyHud = new LegacyHud();
    public static HudEditor hudEditor = new HudEditor();
    public static Crosshair crosshair = new Crosshair();
    public static Criticals criticals = new Criticals();
    public static ChatUtils chatUtils = new ChatUtils();
    public static AutoTotem autoTotem = new AutoTotem();
    public static AutoLeave autoLeave = new AutoLeave();
    public static AutoFlyme autoFlyme = new AutoFlyme();
    public static AutoArmor autoArmor = new AutoArmor();
    public static Cooldowns cooldowns = new Cooldowns();
    public static TapeMouse tapeMouse = new TapeMouse();
    public static Rotations rotations = new Rotations();
    public static MemoryHud memoryHud = new MemoryHud();
    public static AutoGear autoGear = new AutoGear();
    public static ViewLock viewLock = new ViewLock();
    public static Velocity velocity = new Velocity();
    public static Tooltips tooltips = new Tooltips();
    public static Surround surround = new Surround();
    public static Scaffold scaffold = new Scaffold();
    public static PopChams popChams = new PopChams();
    public static NoRender noRender = new NoRender();
    public static FTHelper fTHelper = new FTHelper();
    public static NameTags nameTags = new NameTags();
    public static LongJump longJump = new LongJump();
    public static KeyBinds keyBinds = new KeyBinds();
    public static HoleSnap holeSnap = new HoleSnap();
    public static HoleFill holeFill = new HoleFill();
    public static FGHelper fgHelper = new FGHelper();
    public static ExtraTab extraTab = new ExtraTab();
    public static ClickGui clickGui = new ClickGui();
    public static AutoTrap autoTrap = new AutoTrap();
    public static AutoTool autoTool = new AutoTool();
    public static SoundESP soundESP = new SoundESP();
    public static AutoSoup autoSoup = new AutoSoup();
    public static AutoFish autoFish = new AutoFish();
    public static AutoBuff autoBuff = new AutoBuff();
    public static AutoAuth autoAuth = new AutoAuth();
    public static ArmorHud armorHud = new ArmorHud();
    public static AirPlace airPlace = new AirPlace();
    public static SelfTrap selfTrap = new SelfTrap();
    public static AntiVoid antiVoid = new AntiVoid();
    public static KillFeed killFeed = new KillFeed();
    public static AutoWalk autoWalk = new AutoWalk();
    public static AutoSign autoSign = new AutoSign();
    public static Windows windows = new Windows();
    public static Breaker breaker = new Breaker();
    public static AutoEat autoEat = new AutoEat();
    public static AntiAFK antiAFK = new AntiAFK();
    public static SoundFX soundFX = new SoundFX();
    public static AutoBed autoBed = new AutoBed();
    public static TNTAura tntAura = new TNTAura();
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
    public static AutoSex autoSex = new AutoSex();
    public static Tracers tracers = new Tracers();
    public static Parkour parkour = new Parkour();
    public static ClickTP clickTP = new ClickTP();
    public static Companion companion = new Companion();
    public static BadTrip badTrip = new BadTrip();
    public static BowPop bowPop = new BowPop();
    public static XCarry xCarry = new XCarry();
    public static Trails trails = new Trails();
    public static Strafe strafe = new Strafe();
    public static Spider spider = new Spider();
    public static BlockESP blockESP = new BlockESP();
    public static NoSlow noSlow = new NoSlow();
    public static NoFall noFall = new NoFall();
    public static Hotbar hotbar = new Hotbar();
    public static HitBox hitBox = new HitBox();
    public static Flight flight = new Flight();
    public static Coords coords = new Coords();
    public static Burrow burrow = new Burrow();
    public static AutoEZ autoEZ = new AutoEZ();
    public static AimBot aimBot = new AimBot();
    public static Quiver quiver = new Quiver();
    public static NoPush noPush = new NoPush();
    public static PacketCanceler packetCanceler = new PacketCanceler();
    public static UnHook unHook = new UnHook();
    public static Avoid avoid = new Avoid();
    public static Timer timer = new Timer();
    public static Regen regen = new Regen();
    public static Speed speed = new Speed();
    public static Reach reach = new Reach();
    public static Radar radar = new Radar();
    public static Nuker nuker = new Nuker();
    public static Media media = new Media();
    public static Ghost ghost = new Ghost();
    public static Chams chams = new Chams();
    public static Blink blink = new Blink();
    public static Phase phase = new Phase();
    public static NoBob noBob = new NoBob();
    public static Jesus jesus = new Jesus();
    public static XRay xray = new XRay();
    public static Step step = new Step();
    public static Aura aura = new Aura();
    public static FOV fov = new FOV();
    public static ESP esp = new ESP();
    public static RPC rpc = new RPC();


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
        try {
            ThunderHack.EVENT_BUS.unsubscribe(unHook);
        } catch (Exception ignored) {
        }
        unHook.setEnabled(false);

        modules.sort(Comparator.comparing(Module::getName));
        modules.stream().filter(Module::listening).forEach(ThunderHack.EVENT_BUS::subscribe);

        if (ConfigManager.firstLaunch) {
            ModuleManager.notifications.enable();
            rpc.enable();
            soundFX.enable();
        }
    }

    public void onUpdate() {
        if (Module.fullNullCheck()) return;
        modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
    }

    public void onRender2D(DrawContext context) {
        if(mc.getDebugHud().shouldShowDebugHud() || mc.options.hudHidden) return;
        HudElement.anyHovered = false;
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender2D(context));
        if (!HudElement.anyHovered && !ClickGUI.anyHovered)
            GLFW.glfwSetCursor(mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
        ThunderHack.core.onRender2D(context);
    }

    public void onRender3D(MatrixStack stack) {
        modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender3D(stack));
    }

    public void sortModules() {
        sortedModules = getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> FontRenderers.getModulesRenderer().getStringWidth(module.getFullArrayString()) * -1)).collect(Collectors.toList());
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
        if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickGUI) {
            return;
        }
        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey)
                module.toggle();
        });
    }

    public void onKeyReleased(int eventKey) {
        if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickGUI)
            return;

        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && module.getBind().isHold())
                module.disable();
        });
    }

    public void onMoseKeyPressed(int eventKey) {
        if (eventKey == -1 || mc.currentScreen instanceof ClickGUI) {
            return;
        }

        modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey)) {
                module.toggle();
            }
        });
    }

    public void onMoseKeyReleased(int eventKey) {
        if (eventKey == -1 || mc.currentScreen instanceof ClickGUI)
            return;

        activeMouseKeys.add(eventKey);

        modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey) && module.getBind().isHold())
                module.disable();
        });
    }

    public ArrayList<Module> getModulesSearch(String string) {
        ArrayList<Module> modulesCategory = new ArrayList<>();
        modules.forEach(module -> {
            if (module.getName().toLowerCase().contains(string.toLowerCase()))
                modulesCategory.add(module);
        });
        return modulesCategory;
    }
}
