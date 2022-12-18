package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import com.glisco.numismaticoverhaul.block.PiggyBankScreenHandler;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.currency.MoneyBagLootEntry;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import com.glisco.numismaticoverhaul.villagers.data.VillagerTradesResourceListener;
import com.glisco.numismaticoverhaul.villagers.json.VillagerTradesHandler;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.ops.LootOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumismaticOverhaul implements ModInitializer {

    public static final String MOD_ID = "numismatic-overhaul";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(id("main"));
    private static final ParticleSystemController PARTICLE_SYSTEMS = new ParticleSystemController(id("particles"));
    public static final ParticleSystem<Integer> PIGGY_BANK_BROKEN = PARTICLE_SYSTEMS.register(Integer.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(6 * data);
        ClientParticles.randomizeVelocity(2);
        ClientParticles.spawnCenteredOnBlock(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, NumismaticOverhaulBlocks.PIGGY_BANK.getDefaultState()),
                world, new BlockPos(pos), .75
        );
    });

    public static final ScreenHandlerType<ShopScreenHandler> SHOP_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(ShopScreenHandler::new);
    public static final ScreenHandlerType<PiggyBankScreenHandler> PIGGY_BANK_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(PiggyBankScreenHandler::new);

    public static final SoundEvent PIGGY_BANK_BREAK = SoundEvent.of(id("piggy_bank_break"));
    public static final LootPoolEntryType MONEY_BAG_ENTRY = new LootPoolEntryType(new MoneyBagLootEntry.Serializer());

    public static final TagKey<EntityType<?>> THE_BOURGEOISIE = TagKey.of(RegistryKeys.ENTITY_TYPE, id("the_bourgeoisie"));
    public static final TagKey<Block> VERY_HEAVY_BLOCKS = TagKey.of(RegistryKeys.BLOCK, id("very_heavy_blocks"));

    public static final GameRules.Key<GameRules.IntRule> MONEY_DROP_PERCENTAGE
            = GameRuleRegistry.register("moneyDropPercentage", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(10, 0, 100));

    public static final OwoItemGroup NUMISMATIC_GROUP = OwoItemGroup.builder(
                    NumismaticOverhaul.id("main"),
                    () -> Icon.of(MoneyBagItem.createCombined(new long[]{0, 1, 0})))
            .initializer(group -> {
                group.addButton(ItemGroupButton.modrinth(group, "https://modrinth.com/mod/numismatic-overhaul"));
                group.addButton(ItemGroupButton.curseforge(group, "https://www.curseforge.com/minecraft/mc-mods/numismatic-overhaul"));
                group.addButton(ItemGroupButton.github(group, "https://github.com/wisp-forest/numismatic-overhaul"));
                group.addButton(ItemGroupButton.discord(group, "https://discord.gg/xrwHKktV2d"));
            }).build();

    public static final com.glisco.numismaticoverhaul.NumismaticOverhaulConfig CONFIG = com.glisco.numismaticoverhaul.NumismaticOverhaulConfig.createAndLoad();

    @Override
    public void onInitialize() {

        FieldRegistrationHandler.register(NumismaticOverhaulItems.class, MOD_ID, false);
        FieldRegistrationHandler.register(NumismaticOverhaulBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(NumismaticOverhaulBlocks.Entities.class, MOD_ID, false);

        Registry.register(Registries.SOUND_EVENT, PIGGY_BANK_BREAK.getId(), PIGGY_BANK_BREAK);
        Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, id("money_bag"), MONEY_BAG_ENTRY);

        Registry.register(Registries.SCREEN_HANDLER, id("shop"), SHOP_SCREEN_HANDLER_TYPE);
        Registry.register(Registries.SCREEN_HANDLER, id("piggy_bank"), PIGGY_BANK_SCREEN_HANDLER_TYPE);

        CHANNEL.registerServerbound(RequestPurseActionC2SPacket.class, RequestPurseActionC2SPacket::handle);
        CHANNEL.registerServerbound(ShopScreenHandlerRequestC2SPacket.class, ShopScreenHandlerRequestC2SPacket::handle);
        UpdateShopScreenS2CPacket.initialize();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new VillagerTradesResourceListener());
        VillagerTradesHandler.registerDefaultAdapters();

        CommandRegistrationCallback.EVENT.register(NumismaticCommand::register);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            VillagerTradesHandler.broadcastErrors(server);
        });

        NUMISMATIC_GROUP.initialize();

        if (CONFIG.generateCurrencyInChests()) {
            LootOps.injectItem(NumismaticOverhaulItems.GOLD_COIN, .01f, LootTables.STRONGHOLD_LIBRARY_CHEST, LootTables.BASTION_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST,
                    LootTables.PILLAGER_OUTPOST_CHEST, LootTables.BURIED_TREASURE_CHEST, LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST);

            LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
                if (anyMatch(id, LootTables.DESERT_PYRAMID_CHEST)) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(CONFIG.lootOptions.desertMinLoot(), CONFIG.lootOptions.desertMaxLoot()))
                            .conditionally(RandomChanceLootCondition.builder(0.45f)));
                } else if (anyMatch(id, LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST)) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(CONFIG.lootOptions.dungeonMinLoot(), CONFIG.lootOptions.dungeonMaxLoot()))
                            .conditionally(RandomChanceLootCondition.builder(0.75f)));
                } else if (anyMatch(id, LootTables.BASTION_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST, LootTables.PILLAGER_OUTPOST_CHEST, LootTables.BURIED_TREASURE_CHEST)) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(CONFIG.lootOptions.structureMinLoot(), CONFIG.lootOptions.structureMaxLoot()))
                            .conditionally(RandomChanceLootCondition.builder(0.75f)));
                } else if (anyMatch(id, LootTables.STRONGHOLD_LIBRARY_CHEST)) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(CONFIG.lootOptions.strongholdLibraryMinLoot(), CONFIG.lootOptions.strongholdLibraryMaxLoot()))
                            .conditionally(RandomChanceLootCondition.builder(0.85f)));
                }
            });
        }
    }

    private static boolean anyMatch(Identifier target, Identifier... comparisons) {
        for (Identifier comparison : comparisons) {
            if (target.equals(comparison)) return true;
        }
        return false;
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
