package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.currency.MoneyBagLootEntry;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import com.glisco.numismaticoverhaul.villagers.data.VillagerTradesResourceListener;
import com.glisco.numismaticoverhaul.villagers.json.VillagerTradesHandler;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.ops.LootOps;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumismaticOverhaul implements ModInitializer {

    public static final String MOD_ID = "numismatic-overhaul";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(id("main"));

    public static final ScreenHandlerType<ShopScreenHandler> SHOP_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(id("shop"), ShopScreenHandler::new);
    public static final LootPoolEntryType MONEY_BAG_ENTRY = new LootPoolEntryType(new MoneyBagLootEntry.Serializer());
    public static final TagKey<EntityType<?>> THE_BOURGEOISIE = TagKey.of(Registry.ENTITY_TYPE_KEY, id("the_bourgeoisie"));

    public static final GameRules.Key<GameRules.IntRule> MONEY_DROP_PERCENTAGE
            = GameRuleRegistry.register("moneyDropPercentage", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(10, 0, 100));

    public static final com.glisco.numismaticoverhaul.NumismaticOverhaulConfig CONFIG = com.glisco.numismaticoverhaul.NumismaticOverhaulConfig.createAndLoad();

    @Override
    public void onInitialize() {

        FieldRegistrationHandler.register(NumismaticOverhaulItems.class, MOD_ID, false);
        FieldRegistrationHandler.register(NumismaticOverhaulBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(NumismaticOverhaulBlocks.Entities.class, MOD_ID, false);

        Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, id("money_bag"), MONEY_BAG_ENTRY);

        CHANNEL.registerServerbound(RequestPurseActionC2SPacket.class, RequestPurseActionC2SPacket::handle);
        CHANNEL.registerServerbound(ShopScreenHandlerRequestC2SPacket.class, ShopScreenHandlerRequestC2SPacket::handle);
        UpdateShopScreenS2CPacket.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new VillagerTradesResourceListener());
        VillagerTradesHandler.registerDefaultAdapters();


        CommandRegistrationCallback.EVENT.register(NumismaticCommand::register);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            VillagerTradesHandler.broadcastErrors(server);
        });

        if (CONFIG.generateCurrencyInChests()) {
            LootOps.injectItem(NumismaticOverhaulItems.GOLD_COIN, .01f, LootTables.STRONGHOLD_LIBRARY_CHEST, LootTables.BASTION_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST,
                    LootTables.PILLAGER_OUTPOST_CHEST, LootTables.BURIED_TREASURE_CHEST, LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST);

            LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
                if (anyMatch(id, LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST)) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(500, 2000)).conditionally(RandomChanceLootCondition.builder(0.75f)));
                } else if (anyMatch(id, LootTables.BASTION_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST, LootTables.PILLAGER_OUTPOST_CHEST, LootTables.BURIED_TREASURE_CHEST)) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(1500, 4000)).conditionally(RandomChanceLootCondition.builder(0.75f)));
                } else if (anyMatch(id, LootTables.STRONGHOLD_LIBRARY_CHEST)) {
                    tableBuilder.pool(LootPool.builder().with(MoneyBagLootEntry.builder(2000, 6000)).conditionally(RandomChanceLootCondition.builder(0.85f)));
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
