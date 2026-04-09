package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.block.*;
import com.glisco.numismaticoverhaul.currency.MoneyBagLootEntry;
import com.glisco.numismaticoverhaul.item.*;
import com.glisco.numismaticoverhaul.network.*;
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
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
            world, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), .75
        );
    });

    public static final ScreenHandlerType<ShopScreenHandler> SHOP_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(ShopScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES);
    public static final ScreenHandlerType<PiggyBankScreenHandler> PIGGY_BANK_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(PiggyBankScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    public static final SoundEvent PIGGY_BANK_BREAK = SoundEvent.of(id("piggy_bank_break"));
    public static final LootPoolEntryType MONEY_BAG_ENTRY = new LootPoolEntryType(MoneyBagLootEntry.CODEC);

    public static final TagKey<EntityType<?>> THE_BOURGEOISIE = TagKey.of(RegistryKeys.ENTITY_TYPE, id("the_bourgeoisie"));
    public static final TagKey<Block> VERY_HEAVY_BLOCKS = TagKey.of(RegistryKeys.BLOCK, id("very_heavy_blocks"));

    public static final GameRules.Key<GameRules.IntRule> MONEY_DROP_PERCENTAGE
        = GameRuleRegistry.register("moneyDropPercentage", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(10, 0, 100));

    public static final GameRules.Key<GameRules.IntRule> MONEY_MOB_DROP_VARIANCE
        = GameRuleRegistry.register("moneyMobDropVariancePercentage", GameRules.Category.MOBS, GameRuleFactory.createIntRule(50, 0, 100));

    public static final ComponentType<MoneyBagComponent> MONEY_BAG_COMPONENT = MoneyBagComponent.register();

    public static final OwoItemGroup NUMISMATIC_GROUP = OwoItemGroup.builder(
            NumismaticOverhaul.id("main"),
            () -> Icon.of(MoneyBagItem.fromRawValue(100)))
        .initializer(group -> {
            group.addButton(ItemGroupButton.modrinth(group, "https://modrinth.com/mod/numismatic-overhaul"));
            group.addButton(ItemGroupButton.curseforge(group, "https://www.curseforge.com/minecraft/mc-mods/numismatic-overhaul"));
            group.addButton(ItemGroupButton.github(group, "https://github.com/wisp-forest/numismatic-overhaul"));
            group.addButton(ItemGroupButton.discord(group, "https://discord.gg/xrwHKktV2d"));
        }).build();

    public static final Map<EntityType<?>, Integer> MOBS_IN_BOURGEOISIE = new HashMap<>();

    public static final com.glisco.numismaticoverhaul.NumismaticOverhaulConfig CONFIG = com.glisco.numismaticoverhaul.NumismaticOverhaulConfig.createAndLoad();

    @Override
    public void onInitialize() {
        // this type of code truly feels like DH code
        ServerLifecycleEvents.SERVER_STARTED.register(NumismaticOverhaul::loadMobDropConfig);

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
            reloadMobDropConfig();
        });

        NUMISMATIC_GROUP.initialize();

        if (CONFIG.generateCurrencyInChests()) {
            LootOps.injectItem(NumismaticOverhaulItems.GOLD_COIN, .01f, LootTables.STRONGHOLD_LIBRARY_CHEST.getValue(), LootTables.BASTION_TREASURE_CHEST.getValue(), LootTables.STRONGHOLD_CORRIDOR_CHEST.getValue(),
                LootTables.PILLAGER_OUTPOST_CHEST.getValue(), LootTables.BURIED_TREASURE_CHEST.getValue(), LootTables.SIMPLE_DUNGEON_CHEST.getValue(), LootTables.ABANDONED_MINESHAFT_CHEST.getValue());

            LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
                if (anyMatch(key.getValue(), LootTables.DESERT_PYRAMID_CHEST.getValue())) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(CONFIG.lootOptions.desertMinLoot(), CONFIG.lootOptions.desertMaxLoot()))
                        .conditionally(RandomChanceLootCondition.builder(0.45f)));
                } else if (anyMatch(key.getValue(), LootTables.SIMPLE_DUNGEON_CHEST.getValue(), LootTables.ABANDONED_MINESHAFT_CHEST.getValue())) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(CONFIG.lootOptions.dungeonMinLoot(), CONFIG.lootOptions.dungeonMaxLoot()))
                        .conditionally(RandomChanceLootCondition.builder(0.75f)));
                } else if (anyMatch(key.getValue(), LootTables.BASTION_TREASURE_CHEST.getValue(), LootTables.STRONGHOLD_CORRIDOR_CHEST.getValue(), LootTables.PILLAGER_OUTPOST_CHEST.getValue(), LootTables.BURIED_TREASURE_CHEST.getValue())) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(CONFIG.lootOptions.structureMinLoot(), CONFIG.lootOptions.structureMaxLoot()))
                        .conditionally(RandomChanceLootCondition.builder(0.75f)));
                } else if (anyMatch(key.getValue(), LootTables.STRONGHOLD_LIBRARY_CHEST.getValue())) {
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
        return Identifier.of(MOD_ID, path);
    }

    private static void loadMobDropConfig(MinecraftServer ignored) {
        CONFIG.mobsToBaseValues().forEach((s, baseValue) -> {
            if (s.startsWith("€")) {
                var potentialGroup = s.split("€")[1];
                try {
                    var group = SpawnGroup.valueOf(potentialGroup.toUpperCase(Locale.ROOT));
                    Registries.ENTITY_TYPE.forEach(entityType -> {
                        if (entityType.getSpawnGroup() == group) {
                            MOBS_IN_BOURGEOISIE.put(entityType, baseValue);
                        }
                    });
                } catch (IllegalArgumentException e) {
                    LOGGER.error("[Numismatic Overhaul] Could not find spawn group '{}' for mob drops", s);
                }
            }
            else if (s.startsWith("@")) {
                var namespace = s.split("@")[1];
                Registries.ENTITY_TYPE.getEntrySet().forEach(entry -> {
                    var entityNamespace = entry.getKey().getValue().getNamespace();
                    if (entityNamespace.equals(namespace)) {
                        MOBS_IN_BOURGEOISIE.put(entry.getValue(), baseValue);
                    }
                });
            }
            else if (s.startsWith("#")) {
                Registries.ENTITY_TYPE.getEntryList(TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(s.split("#")[1]))).ifPresentOrElse(registryEntries -> {
                    registryEntries.forEach(entityTypeRegistryEntry -> MOBS_IN_BOURGEOISIE.put(entityTypeRegistryEntry.value(), baseValue));
                }, () -> {
                    LOGGER.error("[Numismatic Overhaul] Could not find entity type for tag '{}' when applying mob drops", s);
                });
            } else {
                var entityOpt = Registries.ENTITY_TYPE.getOrEmpty(Identifier.of(s));
                entityOpt.ifPresentOrElse(entityType -> MOBS_IN_BOURGEOISIE.put(entityType, baseValue), () -> {
                    LOGGER.error("[Numismatic Overhaul] Could not find entity type '{}' when applying mob drops", s);
                });
            }
        });
    }

    private static void reloadMobDropConfig() {
        MOBS_IN_BOURGEOISIE.clear();
        CONFIG.load();
        loadMobDropConfig(null);
    }
}
