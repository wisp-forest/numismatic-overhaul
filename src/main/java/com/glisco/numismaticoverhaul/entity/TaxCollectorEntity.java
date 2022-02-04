package com.glisco.numismaticoverhaul.entity;

import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.HoldInHandsGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TaxCollectorEntity extends HostileEntity implements Angerable {

    private int angerTime = 0;
    @Nullable private UUID angryAt = null;

    private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.NOTCHED_10);

    public TaxCollectorEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new HoldInHandsGoal<>(this, new ItemStack(NumismaticOverhaulItems.MONEY_BAG), SoundEvents.AMBIENT_CAVE, taxCollectorEntity -> {
            return true;
        }));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(2, new FollowPlayerGoal(this));

        this.targetSelector.add(0, new RevengeGoal(this, TaxCollectorEntity.class));
    }

    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setAngerTime(int ticks) {
        this.angerTime = ticks;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        Angerable.super.writeAngerToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        Angerable.super.readAngerFromNbt(this.world, nbt);
        if (this.hasCustomName()) this.bossBar.setName(this.getDisplayName());
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (this.getTarget() != null && this.bossBar.getColor() != BossBar.Color.RED) this.bossBar.setColor(BossBar.Color.RED);
        if (this.getTarget() == null && this.bossBar.getColor() != BossBar.Color.GREEN) this.bossBar.setColor(BossBar.Color.GREEN);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID uuid) {
        this.angryAt = uuid;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.angerTime = Integer.MAX_VALUE;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (world.isClient()) return;
        Angerable.super.tickAngerLogic((ServerWorld) this.world, true);
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }
}
