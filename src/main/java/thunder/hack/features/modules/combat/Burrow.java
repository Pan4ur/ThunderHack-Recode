package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import java.lang.reflect.Field;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class Burrow extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Default);
    private final Setting<OffsetMode> offsetMode = new Setting<>("Mode", OffsetMode.Smart, v -> mode.getValue() == Mode.Default);
    private final Setting<Float> vClip = new Setting<>("VClip", -9.0F, -256.0F, 256.0F, v -> offsetMode.getValue() == OffsetMode.Constant && mode.getValue() == Mode.Default);
    private final Setting<Boolean> scaleDown = new Setting<>("ScaleDown", false, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> scaleVelocity = new Setting<>("ScaleVelocity", false, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> scaleExplosion = new Setting<>("Scale-xplosion", false, v -> mode.getValue() == Mode.Default);
    private final Setting<Float> scaleFactor = new Setting<>("ScaleFactor", 1.0F, 0.1F, 10.0F, v -> mode.getValue() == Mode.Default);
    private final Setting<Integer> scaleDelay = new Setting<>("ScaleDelay", 250, 0, 1000, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> attack = new Setting<>("Attack", true, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> placeDisable = new Setting<>("PlaceDisable", false, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> wait = new Setting<>("Wait", true);
    private final Setting<Boolean> evade = new Setting<>("Evade", false, v -> offsetMode.getValue() == OffsetMode.Constant && mode.getValue() == Mode.Default);
    private final Setting<Boolean> noVoid = new Setting<>("NoVoid", false, v -> offsetMode.getValue() == OffsetMode.Smart && mode.getValue() == Mode.Default);
    private final Setting<Boolean> onGround = new Setting<>("OnGround", true, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> allowUp = new Setting<>("IgnoreHeadBlock", false, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Boolean> discrete = new Setting<>("Discrete", true, v -> offsetMode.getValue() == OffsetMode.Smart && mode.getValue() == Mode.Default);
    private final Setting<Boolean> air = new Setting<>("Air", false, v -> offsetMode.getValue() == OffsetMode.Smart && mode.getValue() == Mode.Default);
    private final Setting<Boolean> fallback = new Setting<>("Fallback", true, v -> offsetMode.getValue() == OffsetMode.Smart && mode.getValue() == Mode.Default);
    private final Setting<Boolean> skipZero = new Setting<>("SkipZero", true, v -> offsetMode.getValue() == OffsetMode.Smart && mode.getValue() == Mode.Default);

    private double motionY;
    private BlockPos startPos;
    private volatile double last_x;
    private volatile double last_y;
    private volatile double last_z;
    private final Timer scaleTimer = new Timer();
    private final Timer timer = new Timer();

    public Burrow() {
        super("Burrow", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        startPos = getPlayerPos();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mode.getValue() != Mode.Default) return;
        if (event.getPacket() instanceof ExplosionS2CPacket) {
            if (scaleExplosion.getValue()) {
                motionY = ((ExplosionS2CPacket) event.getPacket()).getPlayerVelocityY();
                scaleTimer.reset();
            }
            if (scaleVelocity.getValue()) return;
            if (mc.player != null) {
                motionY = ((ExplosionS2CPacket) event.getPacket()).getPlayerVelocityY() / 8000.0;
                scaleTimer.reset();
            }
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet = event.getPacket();
            double x = packet.getX();
            double y = packet.getY();
            double z = packet.getZ();

            if (packet.getFlags().contains(PositionFlag.X)) x += mc.player.getX();
            if (packet.getFlags().contains(PositionFlag.Y)) y += mc.player.getY();
            if (packet.getFlags().contains(PositionFlag.Z)) z += mc.player.getZ();

            last_x = MathUtility.clamp(x, -3.0E7, 3.0E7);
            last_y = y;
            last_z = MathUtility.clamp(z, -3.0E7, 3.0E7);
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (wait.getValue()) {
            BlockPos currentPos = getPlayerPos();
            if (!currentPos.equals(startPos)) {
                disable(isRu() ? "Отключен из-за движения!" : "Disabled due to movement!");
                return;
            }
        }

        BlockPos pos = getPosition(mc.player);
        if (!mc.world.getBlockState(pos).isReplaceable()) {
            if (!wait.getValue())
                disable(isRu() ? "Невозможно поставить блок! Отключаю.." : "Can't place the block! Disabling..");
            return;
        }

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity != null && !mc.player.equals(entity)) {
                if (entity instanceof EndCrystalEntity && attack.getValue()) {
                    PlayerInteractEntityC2SPacket attackPacket = PlayerInteractEntityC2SPacket.attack(mc.player, ((mc.player)).isSneaking());
                    changeId(attackPacket, entity.getId());
                    sendPacket(attackPacket);
                    sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    continue;
                }
                if (!wait.getValue())
                    disable(isRu() ? "Невозможно поставить блок! Отключаю.." : "Can't place the block on! Disabling..");
                return;
            }
        }

        switch (mode.getValue()) {
            case Default -> handleDefault(pos);
            case Web -> handleWeb(pos);
            case Skull -> handleSkull(pos);
        }
    }

    private void handleWeb(BlockPos pos) {
        SearchInvResult webResult = InventoryUtility.findBlockInHotBar(Blocks.COBWEB);

        if (!webResult.found()) {
            disable(isRu() ? "Нет паутины!" : "No webs found!");
            return;
        }

        if (timer.passedMs(250)) {
            if (rotate.getValue()) sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), 90, onGround.getValue()));

            InventoryUtility.saveSlot();
            InteractionUtility.placeBlock(pos, InteractionUtility.Rotate.None, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Packet, webResult.slot(), false, true);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            InventoryUtility.returnSlot();
            if (!wait.getValue() || placeDisable.getValue())
                disable(isRu() ? "Успешно забурровился! Отключаю.." : "Successfully burrowed! Disabling..");
        }
    }

    private void handleSkull(BlockPos pos) {
        SearchInvResult skullResult = InventoryUtility.getSkull();

        if (!skullResult.found()) {
            disable(isRu() ? "Нет голов!" : "No heads found!");
            return;
        }

        if (timer.passedMs(250)) {
            if (rotate.getValue()) sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), 90, onGround.getValue()));
            InventoryUtility.saveSlot();
            InteractionUtility.placeBlock(pos, InteractionUtility.Rotate.None, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Normal, skullResult.slot(), false, true);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            InventoryUtility.returnSlot();
            if (!wait.getValue() || placeDisable.getValue())
                disable(isRu() ? "Успешно забурровился! Отключаю.." : "Successfully burrowed! Disabling..");
        }
    }

    public void handleDefault(BlockPos pos) {
        if ((mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().offset(Direction.UP, 0.2))).blocksMovement() || !mc.player.verticalCollision)) {
            return;
        }

        PlayerEntity rEntity = mc.player;

        BlockPos posHead = getPosition(rEntity).up().up();
        if (!mc.world.getBlockState(posHead).isReplaceable() && wait.getValue()) {
            return;
        }

        if (!allowUp.getValue()) {
            BlockPos upUp = pos.up(2);
            BlockState upState = mc.world.getBlockState(upUp);
            if (upState.blocksMovement()) {
                if (!wait.getValue())
                    disable(isRu() ? "Над головой блок, невозможно забурровиться! Отключаю.." : "Above the head block, impossible to burrow! Disabling..");
                return;
            }
        }

        SearchInvResult obbyResult = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN);
        SearchInvResult echestResult = InventoryUtility.findBlockInHotBar(Blocks.ENDER_CHEST);

        int slot = (!obbyResult.found() || mc.world.getBlockState(pos.down()).getBlock() == Blocks.ENDER_CHEST ? echestResult.slot() : obbyResult.slot());
        if (slot == -1) {
            disable(isRu() ? "Нет блоков!" : "No Block found!");
            return;
        }

        double y = applyScale(getY(rEntity, offsetMode.getValue()));
        if (Double.isNaN(y)) {
            return;
        }

        float[] r = InteractionUtility.getPlaceAngle(pos, InteractionUtility.Interact.Strict, true);

        if (mc.isInSingleplayer()) {
            disable(isRu() ? "Дебил! Ты в одиночке.." : "Retard! You're in singleplayer..");
            return;
        }

        if (timer.passedMs(1000)) {
            if (rotate.getValue()) {
                if (r != null) {
                    if (rEntity.getPos().equals(new Vec3d(last_x, last_y, last_z))) sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(r[0], r[1], onGround.getValue()));
                    else sendPacket(new PlayerMoveC2SPacket.Full(rEntity.getX(), rEntity.getY(), rEntity.getZ(), r[0], r[1], onGround.getValue()));
                }
            }

            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(rEntity.getX(), rEntity.getY() + 0.42, rEntity.getZ(), onGround.getValue()));
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(rEntity.getX(), rEntity.getY() + 0.75, rEntity.getZ(), onGround.getValue()));
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(rEntity.getX(), rEntity.getY() + 1.01, rEntity.getZ(), onGround.getValue()));
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(rEntity.getX(), rEntity.getY() + 1.16, rEntity.getZ(), onGround.getValue()));

            InventoryUtility.saveSlot();
            InteractionUtility.placeBlock(pos, InteractionUtility.Rotate.None, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Packet, slot, false, true);
            mc.player.swingHand(Hand.MAIN_HAND);
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(rEntity.getX(), y, rEntity.getZ(), false));
            timer.reset();
            InventoryUtility.returnSlot();

            if (!wait.getValue() || placeDisable.getValue())
                disable(isRu() ? "Успешно забурровился! Отключаю.." : "Successfully burrowed! Disabling..");
        }
    }

    public static void changeId(PlayerInteractEntityC2SPacket packet, int id) {
        try {
            Field field = PlayerInteractEntityC2SPacket.class.getDeclaredField("field_12870");
            field.setAccessible(true);
            field.setInt(packet, id);
        } catch (Exception ignored) {
        }
    }

    public double getY(Entity entity, OffsetMode mode) {
        if (mode == OffsetMode.Constant) {
            double y = entity.getY() + vClip.getValue();
            if (evade.getValue() && Math.abs(y) < 1) {
                y = -1;
            }
            return y;
        }

        double d = getY(entity, 3, 10, true);
        if (Double.isNaN(d)) {
            d = getY(entity, -3, -10, false);
            if (Double.isNaN(d)) {
                if (fallback.getValue()) {
                    return getY(entity, OffsetMode.Constant);
                }
            }
        }

        return d;
    }

    public static BlockPos getPosition(Entity entity) {
        double y = entity.getY();
        if (entity.getY() - Math.floor(entity.getY()) > 0.5) {
            y = Math.ceil(entity.getY());
        }

        return BlockPos.ofFloored(entity.getX(), y, entity.getZ());
    }

    public double getY(Entity entity, double min, double max, boolean add) {
        if (min > max && add || max > min && !add) {
            return Double.NaN;
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        boolean air = false;
        double lastOff = 0.0;
        BlockPos last = null;
        for (double off = min; add ? off < max : off > max; off = (add ? ++off : --off)) {
            BlockPos pos = BlockPos.ofFloored(x, y - off, z);
            if (noVoid.getValue() && pos.getY() < 0) {
                continue;
            }

            if (skipZero.getValue() && Math.abs(y) < 1) {
                air = false;
                last = pos;
                lastOff = y - off;
                continue;
            }

            BlockState state = mc.world.getBlockState(pos);
            if (!this.air.getValue() && !state.blocksMovement() || state.getBlock() == Blocks.AIR) {
                if (air) {
                    if (add) return discrete.getValue() ? pos.getY() : y - off;
                    else return discrete.getValue() ? last.getY() : lastOff;
                }
                air = true;
            } else air = false;
            last = pos;
            lastOff = y - off;
        }

        return Double.NaN;
    }

    protected double applyScale(double value) {
        if (value < mc.player.getY() && !scaleDown.getValue()
                || !scaleExplosion.getValue() && !scaleVelocity.getValue()
                || scaleTimer.passedMs(scaleDelay.getValue())
                || motionY == 0.0) {
            return value;
        }

        if (value < mc.player.getY()) value -= (motionY * scaleFactor.getValue());
        else value += (motionY * scaleFactor.getValue());

        return discrete.getValue() ? Math.floor(value) : value;
    }

    public static BlockPos getPlayerPos() {
        return Math.abs(mc.player.getVelocity().getY()) > 0.1 ? BlockPos.ofFloored(mc.player.getPos()) : getPosition(mc.player);
    }

    public enum OffsetMode {
        Constant,
        Smart
    }

    private enum Mode {
        Default, Skull, Web
    }
}
