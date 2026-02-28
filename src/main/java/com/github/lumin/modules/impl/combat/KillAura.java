package com.github.lumin.modules.impl.combat;

import com.github.lumin.managers.Managers;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.settings.impl.BoolSetting;
import com.github.lumin.settings.impl.DoubleSetting;
import com.github.lumin.settings.impl.IntSetting;
import com.github.lumin.settings.impl.ModeSetting;
import com.github.lumin.utils.math.MathUtils;
import com.github.lumin.utils.rotation.MovementFix;
import com.github.lumin.utils.rotation.Priority;
import com.github.lumin.utils.rotation.RotationUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {

    public static final KillAura INSTANCE = new KillAura();

    public KillAura() {
        super("Killaura", "", "Auto fuck b", "", Category.COMBAT);
    }

    public ModeSetting movefix = modeSetting("Move Fix", "", "Silent", new String[]{"Silent", "Strict"});
    public ModeSetting targetMode = modeSetting("Target Mode", "", "Single", new String[]{"Single", "Switch", "Multi"});
    public DoubleSetting range = doubleSetting("Attack Range", "", 3.0, 1.0, 6.0, 0.01);
    public DoubleSetting aimRange = doubleSetting("Aim Range", " ", 4.0, 1.0, 6.0, 0.1);
    public IntSetting speed = intSetting("Rotation Speed", "", 10, 1, 10, 1);
    public DoubleSetting fov = doubleSetting("Fov", " ", 360.0, 10.0, 360.0, 1.0);
    public BoolSetting cooldownATK = boolSetting("1.9 CoolDown Attack", " ", false);
    public BoolSetting esp = boolSetting("Target ESP", "", false);
    public DoubleSetting cps = doubleSetting("Min APS", "", 10.0, 1.0, 20.0, 1.0);
    public DoubleSetting maxCps = doubleSetting("Max APS", " ", 12, 1, 20, 1);
    public BoolSetting player = boolSetting("Attack Player", "", true);
    public BoolSetting mob = boolSetting("Attack Mob", "", true);
    public BoolSetting animal = boolSetting("Attack Animal", "", true);
    public BoolSetting Invisible = boolSetting("Attack Invisible", "", true);

    public static LivingEntity target;
    public static List<LivingEntity> targets = new ArrayList<>();

    private int switchIndex = 0;
    public float attacks = 0;

    @Override
    protected void onDisable() {
        target = null;
        targets.clear();
        switchIndex = 0;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Pre e) {
        if (nullCheck()) return;

        targets.clear();
        updateTargets();

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (targetMode.is("Single")) {
            target = targets.getFirst();
        } else if (targetMode.is("Switch")) {
            if (switchIndex >= targets.size()) switchIndex = 0;
            target = targets.get(switchIndex);
        } else if (targetMode.is("Multi")) {
            target = targets.getFirst();
        }

        attacks += MathUtils.getRandom(cps.getValue().floatValue(), maxCps.getValue().floatValue()) / 20f;

        if (target != null) {
            float[] rotations = RotationUtils.getRotationsToEntity(target);
            boolean silent = movefix.is("Silent");
            Managers.ROTATION.setRotations(new Vector2f(rotations[0], rotations[1]), speed.getValue().floatValue(), MovementFix.ON, Priority.Medium);
        }
    }

    @SubscribeEvent
    public void onClick(ClientTickEvent.Pre e) {
        if (nullCheck()) return;
        if (target == null) return;
        if (mc.player.isUsingItem() || mc.player.isBlocking()) return;
        if (mc.player.getAttackStrengthScale(0.5f) < 1.0f && cooldownATK.getValue()) return;
        while (attacks >= 1) {
            if (targetMode.is("Multi")) {
                for (LivingEntity t : targets) {
                    if (RotationUtils.getEyeDistanceToEntity(t) <= range.getValue() && mc.hitResult.getType() == HitResult.Type.ENTITY) {
                        doAttack();
                    }
                }
                switchIndex++;
            } else {
                if (RotationUtils.getEyeDistanceToEntity(target) <= range.getValue() && mc.hitResult.getType() == HitResult.Type.ENTITY && mc.crosshairPickEntity.is(target)) {
                    doAttack();
                    if (targetMode.is("Switch")) switchIndex++;
                } else if (targetMode.is("Switch")) {
                    switchIndex++;
                }
            }
            attacks -= 1;
        }
    }

    private void doAttack() {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private void updateTargets() {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (!living.isAlive() || living.isDeadOrDying()) continue;
            //      if (AntiBot.isBot(entity)) continue;

            double dist = RotationUtils.getEyeDistanceToEntity(living);
            if (dist > aimRange.getValue()) continue;

            if (!isValidTarget(living)) continue;
            if (!RotationUtils.isInFov(living, fov.getValue().floatValue())) continue;
            targets.sort(Comparator.comparingDouble(o -> (double) o.distanceTo(mc.player)));
            targets.add(living);
        }
        targets.sort(Comparator.comparingDouble(RotationUtils::getEyeDistanceToEntity));
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof Player) {
            if (!player.getValue()) return false;
            return !entity.isInvisible() || Invisible.getValue();
        } else if (entity instanceof Animal || entity instanceof Villager) {
            return animal.getValue();
        } else if (entity instanceof Monster) {
            return mob.getValue();
        } else {
            return false;
        }
    }
}
