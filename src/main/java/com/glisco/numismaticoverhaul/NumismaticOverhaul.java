package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.block.ShopBlock;
import com.glisco.numismaticoverhaul.block.ShopBlockEntity;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.currency.MoneyBagLootEntry;
import com.glisco.numismaticoverhaul.item.CoinItem;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.glisco.numismaticoverhaul.villagers.data.VillagerTradesResourceListener;
import com.glisco.numismaticoverhaul.villagers.json.VillagerTradesHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumismaticOverhaul implements ModInitializer {

    public static final String MOD_ID = "numismatic-overhaul";

    public static final Item BRONZE_COIN = new CoinItem(CurrencyResolver.Currency.BRONZE);
    public static final Item SILVER_COIN = new CoinItem(CurrencyResolver.Currency.SILVER);
    public static final Item GOLD_COIN = new CoinItem(CurrencyResolver.Currency.GOLD);

    public static final Item MONEY_BAG = new MoneyBagItem();

    public static final Block SHOP_BLOCK = new ShopBlock();
    public static BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY;
    public static final ScreenHandlerType<ShopScreenHandler> SHOP_SCREEN_HANDLER_TYPE;

    public static final LootPoolEntryType MONEY_BAG_ENTRY = new LootPoolEntryType(new MoneyBagLootEntry.Serializer());

    static {
        SHOP_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "shop"), ShopScreenHandler::new);
    }

    public static final Logger LOGGER = LogManager.getLogger("numismatic-overhaul");

    @Override
    public void onInitialize() {

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bronze_coin"), BRONZE_COIN);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "silver_coin"), SILVER_COIN);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gold_coin"), GOLD_COIN);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "money_bag"), MONEY_BAG);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "shop"), SHOP_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "shop"), new BlockItem(SHOP_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
        SHOP_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "shop"), FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, SHOP_BLOCK).build(null));

        Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new Identifier(MOD_ID, "money_bag"), MONEY_BAG_ENTRY);

        ServerPlayNetworking.registerGlobalReceiver(RequestPurseActionC2SPacket.ID, RequestPurseActionC2SPacket::onPacket);
        ServerPlayNetworking.registerGlobalReceiver(ShopScreenHandlerRequestC2SPacket.ID, ShopScreenHandlerRequestC2SPacket::onPacket);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new VillagerTradesResourceListener());
        VillagerTradesHandler.registerDefaultAdapters();

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            VillagerTradesHandler.broadcastErrors(server);
        });

        LootTableLoadingCallback.EVENT.register((resourceManager, manager, id, supplier, setter) -> {

            if (anyMatch(id, LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST)) {
                supplier.withPool(FabricLootPoolBuilder.builder().withEntry(MoneyBagLootEntry.builder(500, 2000).build()).conditionally(RandomChanceLootCondition.builder(0.75f))
                        .withEntry(ItemEntry.builder(GOLD_COIN).conditionally(RandomChanceLootCondition.builder(0.01f)).build())
                        .build());
            } else if (anyMatch(id, LootTables.BASTION_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST, LootTables.PILLAGER_OUTPOST_CHEST, LootTables.BURIED_TREASURE_CHEST)) {
                supplier.withPool(FabricLootPoolBuilder.builder().withEntry(MoneyBagLootEntry.builder(1500, 4000).build()).conditionally(RandomChanceLootCondition.builder(0.75f))
                        .withEntry(ItemEntry.builder(GOLD_COIN).conditionally(RandomChanceLootCondition.builder(0.01f)).build())
                        .build());
            } else if (anyMatch(id, LootTables.STRONGHOLD_LIBRARY_CHEST)) {
                supplier.withPool(FabricLootPoolBuilder.builder().withEntry(MoneyBagLootEntry.builder(2000, 6000).build()).conditionally(RandomChanceLootCondition.builder(0.85f))
                        .withEntry(ItemEntry.builder(GOLD_COIN).conditionally(RandomChanceLootCondition.builder(0.01f)).build())
                        .build());
            } else if (id.equals(new Identifier("entities/pillager"))) {
                supplier.withPool(FabricLootPoolBuilder.builder()
                        .withEntry(ItemEntry.builder(BRONZE_COIN).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(9, 34))).build())
                        .withEntry(ItemEntry.builder(SILVER_COIN).conditionally(RandomChanceLootCondition.builder(0.4f)).build())
                        .conditionally(RandomChanceLootCondition.builder(0.5f))
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
}
