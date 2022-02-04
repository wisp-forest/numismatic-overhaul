package com.glisco.numismaticoverhaul.entity;

import io.wispforest.owo.util.VectorRandomUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {

    private final TaxCollectorEntity taxCollector;
    private PlayerEntity target = null;
    private int outOfRangeTicks = 0;

    public FollowPlayerGoal(TaxCollectorEntity taxCollector) {
        this.taxCollector = taxCollector;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public void start() {
        taxCollector.getNavigation().startMovingTo(target,
                taxCollector.getTarget() == null ?
                        MathHelper.clamp(target.distanceTo(taxCollector) * .05, .3, 1) : .75);
    }

    @Override
    public void tick() {
        final var squaredDistanceToTarget = target.squaredDistanceTo(taxCollector);
        if (squaredDistanceToTarget > 100) outOfRangeTicks++;
        else outOfRangeTicks = 0;

        if (outOfRangeTicks < 300 && squaredDistanceToTarget < 900 /* 30 */) return;

        for (int i = 0; i < 10; i++) {
            var teleportTarget = new BlockPos(VectorRandomUtils.getRandomOffsetSpecific(target.world, target.getPos(), 6, 3, 6));

            if (LandPathNodeMaker.getLandNodeType(target.world, teleportTarget.mutableCopy()) != PathNodeType.WALKABLE) continue;
            if (!target.world.getBlockState(teleportTarget.up()).isAir()) continue;

            taxCollector.refreshPositionAndAngles(teleportTarget, taxCollector.getYaw(), taxCollector.getPitch());
            taxCollector.getNavigation().stop();
            break;
        }
    }

    @Override
    public boolean canStart() {
        if (target != null) return taxCollector.getTarget() != null || target.squaredDistanceTo(taxCollector) > 4;

        target = taxCollector.world.getNonSpectatingEntities(PlayerEntity.class, taxCollector.getBoundingBox().expand(10))
                .stream().findAny().orElse(null);
        return target != null && (taxCollector.getTarget() != null || target.squaredDistanceTo(taxCollector) > 4);
    }

    @Override
    public boolean shouldContinue() {
        return !taxCollector.getNavigation().isIdle();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }
}
