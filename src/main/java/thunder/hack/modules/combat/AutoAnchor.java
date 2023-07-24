package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtil;

import static thunder.hack.utility.player.PlaceUtility.canSee;

public class AutoAnchor extends Module {
    public AutoAnchor() {
        super("AnchorAura", Category.COMBAT);
    }


    public Setting<Mode> mode = new Setting<>("Mode", Mode.Legit);
    public Setting<Integer> swapDelay = new Setting<>("SwapDelay", 100, 0, 1000);
    public Setting<Integer> charge = new Setting<>("Charge", 5, 1, 5);

    private enum Mode{
        Legit, Rage
    }

    @Subscribe
    public void onBlockPlace(EventPlaceBlock event){
        if(mode.getValue() == Mode.Rage) return;
        if(event.getBlock() == Blocks.RESPAWN_ANCHOR && mc.options.useKey.isPressed()){
            int glowSlot = InventoryUtil.getItemSlotHotbar(Items.GLOWSTONE);
            if(glowSlot == -1 ) return;
            new LegitThread(glowSlot,mc.player.getInventory().selectedSlot,swapDelay.getValue()).start();
        }
    }

    public class LegitThread extends Thread {
        int glowSlot,originalSlot,delay;

        public LegitThread(int glowSlot, int originalSlot, int delay) {
            this.glowSlot = glowSlot;
            this.originalSlot = originalSlot;
            this.delay = delay;
        }

        @Override
        public void run() {
            try {sleep(delay);} catch (Exception ignored) {}

            mc.player.getInventory().selectedSlot= glowSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(glowSlot));
            try {sleep(delay);} catch (Exception ignored) {}
            for (int i = 0; i < charge.getValue(); i++) {
                ((IMinecraftClient)mc).idoItemUse();
            }

            try {sleep(delay);} catch (Exception ignored) {}

            mc.player.getInventory().selectedSlot = originalSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));

            try {sleep(delay);} catch (Exception ignored) {}
            if(charge.getValue() < 5) ((IMinecraftClient)mc).idoItemUse();

            super.run();
        }
    }
    /*
    public Setting<TimingMode> timingMode = new Setting<>("Timing", TimingMode.Sequential);
    public Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public Setting<Boolean> yawStep = new Setting<>("YawStep", false, v-> rotate.getValue());
    public Setting<Integer> yawAngle = new Setting<>("YawAngle", 54, 5, 180, v-> rotate.getValue() && yawStep.getValue());
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection", true);
    public Setting<Integer> breakDelay = new Setting<>("ExplodeDelay", 0, 0, 1000);
    public Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 0, 0, 1000);
    public Setting<SyncMode> sync = new Setting<>("Sync", SyncMode.Strict);
    public Setting<Float> breakRange = new Setting<>("ExplodeRange", 4.3F, 1F, 6F);
    public Setting<Float> breakWallsRange = new Setting<>("ExplodeWalls", 3F, 1F, 6F);
    public Setting<Float> placeRange = new Setting<>("PlaceRange", 4F, 1F, 6F);
    public Setting<Float> placeWallsRange = new Setting<>("PlaceWalls", 1.5F, 1F, 6F);
    public Setting<AutoSwapMode> autoSwap = new Setting<>("AutoSwap", AutoSwapMode.Normal);
    public Setting<PriorityMode> priorityMode = new Setting<>("PlacePriority", PriorityMode.MaxDamage);
    public Setting<Float> enemyRange = new Setting<>("TargetRange", 8F, 4F, 20F);
    public static Setting<Integer> predictTicks = new Setting<>("PredictTicks", 3, 0, 10);
    public Setting<Float> minDamage = new Setting<>("MinDamage", 6F, 0F, 20F);
    public Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDmg", 12F, 0F, 20F);
    public Setting<Float> faceplaceHealth = new Setting<>("FaceplaceHP", 4F, 0F, 20F);
    public Setting<Boolean> pauseWhileMining = new Setting<>("PauseWhenMining", false);
    public Setting<Boolean> pauseWhileGapping = new Setting<>("PauseWhenGapping", false);
    public Setting<Boolean> pauseWhenAura = new Setting<>("PauseWhenAura", true);
    public Setting<Float> pauseHealth = new Setting<>("PauseHealth", 2f, 0f, 10f);
    public Setting<Boolean> render = new Setting<>("Render", true);

    private enum TimingMode {Sequential, Vanilla}

    private enum ConfirmMode {OFF, SEMI, FULL}

    private enum SyncMode {Strict, Merge, Adaptive}

    private enum PriorityMode {MaxDamage, Balance}

    private enum AutoSwapMode {None, Normal, Silent}

    private final ConcurrentHashMap<BlockPos, Long> placeLocations = new ConcurrentHashMap<>();

    private Vec3d rotations;
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer cacheTimer = new Timer();


    private int prev_crystals_ammount;
    private int crys_speed,inv_timer;

    private BlockPos cachePos = null;
    private BlockPos threadedBp = null;

    private boolean lastBroken = false;
    public static Entity CAtarget;

    private float renderDmg;

    @Override
    public void onEnable() {
        lastBroken = false;
        rotations = null;
        cachePos = null;
       // lastExplosionVec = null;
        crys_speed = 0;
        prev_crystals_ammount = InventoryUtil.getItemCount(Items.RESPAWN_ANCHOR);
        threadedBp = null;
    }

    @Override
    public void onDisable() {
        CAtarget = null;
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (timingMode.getValue() == TimingMode.Vanilla && check()) {
            if (!generateBreak()) generatePlace();
        }
    }

    @Subscribe
    public void onPostTick(EventPostTick event) {
        if (timingMode.getValue() == TimingMode.Vanilla && check()) {
            if (!generateBreak()) generatePlace();
        }
    }

    @Subscribe
    public void onEntitySync(EventSync event) {
        if(mc.player == null || mc.world == null) return;
        if(inv_timer++ >= 20){
            crys_speed = prev_crystals_ammount - InventoryUtil.getItemCount(Items.RESPAWN_ANCHOR);
            prev_crystals_ammount = InventoryUtil.getItemCount(Items.RESPAWN_ANCHOR);
            inv_timer = 0;
        }

        placeLocations.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 1500) {
                placeLocations.remove(pos);
            }
        });

        if (timingMode.getValue() == TimingMode.Sequential) {
            if (check()) {
                if (!generateBreak()) {
                    generatePlace();
                }
            }
        }

        if (rotate.getValue() && rotations != null) {
            float[] yp = PlaceUtility.calculateAngle(rotations);
            if (yawStep.getValue()) {
                float yawDiff = MathHelper.wrapDegrees(yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw());
                if (Math.abs(yawDiff) > yawAngle.getValue()) {
                    yp[0] = ((IClientPlayerEntity) ((mc.player))).getLastYaw() + (yawDiff * (yawAngle.getValue() / Math.abs(yawDiff)));
                    PlaceManager.trailingBreakAction = null; PlaceManager.trailingPlaceAction = null;
                }
            }

            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            yp[0] = (float) (yp[0] - (yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            yp[1] = (float) (yp[1] - (yp[1] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);

            PlaceManager.setTrailingRotation(yp);
        }
    }


    private boolean check() {
        if ((pauseWhileMining.getValue() && mc.interactionManager.isBreakingBlock()) || (pauseWhileGapping.getValue() && mc.player.getActiveItem().getItem() instanceof EnchantedGoldenAppleItem) || (mc.player.getHealth() + mc.player.getAbsorptionAmount() < pauseHealth.getValue()) || (pauseWhenAura.getValue() && Thunderhack.moduleManager.get(Aura.class).isEnabled())) {
            return false;
        }
        if (pauseWhileGapping.getValue() && mc.options.useKey.isPressed() && mc.player.getInventory().getMainHandStack().getItem() instanceof EnchantedGoldenAppleItem) {
                return false;
        }
        if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof EndCrystalItem)) {
            if (autoSwap.getValue() == AutoSwapMode.None) return false;
            else return InventoryUtil.getCrystalSlot() != -1;
        }
        return true;
    }

    private void generatePlace() {
        if (placeTimer.passedMs(placeDelay.getValue())) {
            BlockPos candidatePos = threadedBp;
            if (candidatePos != null) {
                PlaceManager.trailingPlaceAction = () -> {
                    if (PlaceUtility.canPlaceBlock(cachePos, strictDirection.getValue(), false)) {
                        PlaceUtility.forcePlace(cachePos, strictDirection.getValue(), Hand.MAIN_HAND, -1, false);
                        placeTimer.reset();
                    }
                };
            }
        }
    }


    private boolean generateBreak() {
        List<PlayerEntity> targetsInRange = getTargetsInRange();
        BlockPos bestAnchor = findAnchorTarget(targetsInRange);

        if (bestAnchor != null) {
                if (rotate.getValue()) {
                    rotations = bestAnchor.toCenterPos();
                }
                if (breakTimer.passedMs(breakDelay.getValue() + 20)) {
                    if (lastBroken) {
                        lastBroken = false;
                        if (sync.getValue() == SyncMode.Strict) {
                            return false;
                        }
                    }
                    PlaceManager.trailingBreakAction = () -> {
                        if (explodeAnchor(bestAnchor)) {
                            lastBroken = true;
                            breakTimer.reset();
                        }
                        if (sync.getValue() != SyncMode.Strict && check()) {
                            generatePlace();
                        }
                    };
                    if (timingMode.getValue() == TimingMode.Vanilla) {
                        PlaceManager.trailingBreakAction.run();
                        PlaceManager.trailingBreakAction = null;
                    }

            }
            return true;
        }
        return false;
    }

    private boolean explodeAnchor(BlockPos anchorPos){
        return true;
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if(render.getValue())
            placeLocations.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time < 500) {
                    int alpha = (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)));
                    Render3DEngine.drawFilledBox(event.getMatrixStack(),new Box(pos), Render2DEngine.injectAlpha(HudEditor.getColor(0), alpha));
                    Render3DEngine.drawBoxOutline(new Box(pos), Render2DEngine.injectAlpha(HudEditor.getColor(0), alpha), 2);
                    Render3DEngine.drawTextIn3D(String.valueOf(MathUtil.round2(renderDmg)),pos.toCenterPos(),0,0.1,0,Render2DEngine.injectAlpha(Color.WHITE, alpha));
                }
            });
    }

    @Override
    public void onThread() {
        threadedBp = findPlacePosition();
    }

    private BlockPos findPlacePosition() {
        List<PlayerEntity> targetss = getTargetsInRange();

        List<BlockPos> blocks = findAnchorBlocks();

        if(blocks == null) return null;

        BlockPos bestPos = null;

        PlayerEntity bestTarget = null;

        float bestDamage = 0.0f;

        float bestBalance = 0.0f;

        if (targetss.isEmpty()) return null;

        for (BlockPos block : blocks) {
            Vec3d blockVec = new Vec3d(block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5);
            float damage = 0.0F;
            PlayerEntity target = null;
            float damageToSelf = ExplosionUtil.getSelfExplosionDamage(blockVec);

            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= damageToSelf + 2F) {
                continue;
            }

            if (damageToSelf > maxSelfDamage.getValue()) {
                continue;
            }

            for (PlayerEntity player : targetss) {
                boolean facePlace = false;

                float damageToTarget = ExplosionUtil.getExplosionDamage2(blockVec, player);

                if (damageToTarget >= 0.5D) {
                    if (player.getHealth() + player.getAbsorptionAmount() - damageToTarget <= 0 || player.getHealth() + player.getAbsorptionAmount() < faceplaceHealth.getValue()) {
                        facePlace = true;
                    }
                }

                if (mc.options.sneakKey.isPressed()) {
                    facePlace = true;
                }

                if (damageToTarget > damage && (damageToTarget >= minDamage.getValue() || facePlace)) {
                    damage = damageToTarget;
                    target = player;
                }
            }

            if (priorityMode.getValue() == PriorityMode.MaxDamage) {
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestPos = block;
                    bestTarget = target;
                }
            } else {
                if (damage / damageToSelf > bestBalance) {
                    bestDamage = damage;
                    bestBalance = damage / damageToSelf;
                    bestPos = block;
                    bestTarget = target;
                }
            }
        }

        if (bestTarget != null && bestPos != null) {
            CAtarget = bestTarget;
            renderDmg = bestDamage;
        } else {
            CAtarget = null;
        }

        cachePos = bestPos;
        cacheTimer.reset();
        return bestPos;
    }

    public void onAnchorPacket(double x,double y, double z){
        placeLocations.forEach((pos, time) -> {
            if (MathUtil.getSqrDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, x,  y - 1, z) < 1) {
                placeLocations.remove(pos);
                cachePos = null;

                if (!check()) return;

                Vec3d spawnVec = new Vec3d(x, y, z);

                if (PlaceUtility.getEyesPos(((mc.player))).distanceTo(spawnVec) > breakRange.getValue()) return;

                if (!(breakTimer.passedMs(breakDelay.getValue()))) return;

                if (ExplosionUtil.getSelfExplosionDamage(spawnVec) + 2F >= mc.player.getHealth() + mc.player.getAbsorptionAmount())
                    return;

                // explode


                breakTimer.reset();
                lastBroken = true;
                if (sync.getValue() == SyncMode.Adaptive) {
                    generatePlace();
                }
            }
        });
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getPacket() instanceof BlockUpdateS2CPacket pac){
            if (pac.getState() == Blocks.RESPAWN_ANCHOR.getDefaultState()) {
                onAnchorPacket(pac.getPos().getX(), pac.getPos().getY(), pac.getPos().getZ());
            }
        }
    }

    private List<BlockPos> findAnchorBlocks() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = ((mc.player)).getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (PlaceUtility.canPlaceBlock(pos,strictDirection.getValue(),false)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }


    public boolean setAnchorSlot() {
        int crystalSlot = InventoryUtil.getCrystalSlot();
        if (crystalSlot == -1) {
            return false;
        } else if (mc.player.getInventory().selectedSlot != crystalSlot) {
            if(autoSwap.getValue() != AutoSwapMode.Silent) {
                mc.player.getInventory().selectedSlot = crystalSlot;
            }
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(crystalSlot));
        }
        return true;
    }

    private BlockPos findAnchorTarget(List<PlayerEntity> targetsInRange) {
        BlockPos bestAnchor = null;

        List<BlockPos> anchorsInRange = getAnchorsInRange();

        if (anchorsInRange.isEmpty()) return null;
        double bestDamage = 0.0D;

        for (BlockPos anchor : anchorsInRange) {
            if (anchor.toCenterPos().distanceTo(PlaceUtility.getEyesPos(((mc.player)))) < breakWallsRange.getValue() || canSee(anchor.toCenterPos(),anchor.toCenterPos())) {

                double selfDamage = ExplosionUtil.getSelfExplosionDamage(anchor.toCenterPos());

                if (!placeLocations.containsKey(anchor) && selfDamage > maxSelfDamage.getValue()) {
                    continue;
                }

                double damage = 0.0D;

                for (PlayerEntity target : targetsInRange) {
                    double targetDamage = ExplosionUtil.getExplosionDamage2(anchor.toCenterPos(), target);
                    damage += targetDamage;
                }

                if (!placeLocations.containsKey(anchor) && (damage < minDamage.getValue() || damage < selfDamage))
                    continue;

                if (damage > bestDamage || bestDamage == 0D) {
                    bestDamage = damage;
                    bestAnchor = anchor;
                }
            }
        }
        return bestAnchor;
    }


    private List<BlockPos> getAnchorsInRange() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = ((mc.player)).getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR && isValidAnchorTarget(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    private boolean isValidAnchorTarget(BlockPos bp) {
        if (PlaceUtility.getEyesPos(((mc.player))).distanceTo(bp.toCenterPos()) > breakRange.getValue()) return false;
        return !(ExplosionUtil.getSelfExplosionDamage(bp.toCenterPos().offset(Direction.DOWN,0.5)) + 2F >= mc.player.getHealth() + mc.player.getAbsorptionAmount());
    }

    private List<PlayerEntity> getTargetsInRange() {
        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity player : Thunderhack.asyncManager.getAsyncPlayers()) {
            if (Thunderhack.friendManager.isFriend(player)) continue;
            if (player == mc.player) continue;
            if (player.distanceTo(((mc.player))) > enemyRange.getValue()) continue;
            if (player.isDead()) continue;
            if (player.getHealth() + player.getAbsorptionAmount() <= 0) continue;
            list.add(player);
        }
        return list.stream().sorted(Comparator.comparing(e -> (e.distanceTo(((mc.player)))))).limit(1).collect(Collectors.toList());
    }

     */
}
