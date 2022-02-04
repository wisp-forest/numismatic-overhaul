package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.currency.MoneyBagLootEntry;
import com.glisco.numismaticoverhaul.entity.TaxCollectorEntity;
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
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumismaticOverhaul implements ModInitializer {

    public static final String MOD_ID = "numismatic-overhaul";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(id("main"));

    public static final EntityType<TaxCollectorEntity> TAX_COLLECTOR = FabricEntityTypeBuilder
            .<TaxCollectorEntity>createMob()
            .entityFactory(TaxCollectorEntity::new)
            .defaultAttributes(TaxCollectorEntity::createHostileAttributes)
            .dimensions(new EntityDimensions(.6f, 1.95f, false))
            .build();


    public static final ScreenHandlerType<ShopScreenHandler> SHOP_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(id("shop"), ShopScreenHandler::new);
    public static final LootPoolEntryType MONEY_BAG_ENTRY = new LootPoolEntryType(new MoneyBagLootEntry.Serializer());
    public static final Tag<EntityType<?>> THE_BOURGEOISIE = TagFactory.ENTITY_TYPE.create(id("the_bourgeoisie"));

    public static final GameRules.Key<GameRules.IntRule> MONEY_DROP_PERCENTAGE
            = GameRuleRegistry.register("moneyDropPercentage", GameRules.Category.DROPS, GameRuleFactory.createIntRule(10, 0, 100));

    @Override
    public void onInitialize() {

        FieldRegistrationHandler.register(NumismaticOverhaulItems.class, MOD_ID, false);
        FieldRegistrationHandler.register(NumismaticOverhaulBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(NumismaticOverhaulBlocks.Entities.class, MOD_ID, false);

        Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, id("money_bag"), MONEY_BAG_ENTRY);
        Registry.register(Registry.ENTITY_TYPE, id("tax_collector"), TAX_COLLECTOR);

        CHANNEL.registerServerbound(RequestPurseActionC2SPacket.class, RequestPurseActionC2SPacket::handle);
        CHANNEL.registerServerbound(ShopScreenHandlerRequestC2SPacket.class, ShopScreenHandlerRequestC2SPacket::handle);
        UpdateShopScreenS2CPacket.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new VillagerTradesResourceListener());
        VillagerTradesHandler.registerDefaultAdapters();

        CommandRegistrationCallback.EVENT.register(NumismaticCommand::register);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            VillagerTradesHandler.broadcastErrors(server);
        });

        LootOps.injectItem(NumismaticOverhaulItems.GOLD_COIN, .01f, LootTables.STRONGHOLD_LIBRARY_CHEST, LootTables.BASTION_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST,
                LootTables.PILLAGER_OUTPOST_CHEST, LootTables.BURIED_TREASURE_CHEST, LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST);

        LootTableLoadingCallback.EVENT.register((resourceManager, manager, id, supplier, setter) -> {
            if (anyMatch(id, LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST)) {
                supplier.withPool(FabricLootPoolBuilder.builder().withEntry(MoneyBagLootEntry.builder(500, 2000).build()).conditionally(RandomChanceLootCondition.builder(0.75f))
                        .build());
            } else if (anyMatch(id, LootTables.BASTION_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST, LootTables.PILLAGER_OUTPOST_CHEST, LootTables.BURIED_TREASURE_CHEST)) {
                supplier.withPool(FabricLootPoolBuilder.builder().withEntry(MoneyBagLootEntry.builder(1500, 4000).build()).conditionally(RandomChanceLootCondition.builder(0.75f))
                        .build());
            } else if (anyMatch(id, LootTables.STRONGHOLD_LIBRARY_CHEST)) {
                supplier.withPool(FabricLootPoolBuilder.builder().withEntry(MoneyBagLootEntry.builder(2000, 6000).build()).conditionally(RandomChanceLootCondition.builder(0.85f))
                        .build());
            }
        });
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
