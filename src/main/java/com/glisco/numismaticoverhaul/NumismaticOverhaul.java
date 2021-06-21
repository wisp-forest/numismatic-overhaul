package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.block.ShopBlock;
import com.glisco.numismaticoverhaul.block.ShopBlockEntity;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.item.CoinItem;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.glisco.numismaticoverhaul.villagers.json.VillagerTradesHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
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

        ServerPlayNetworking.registerGlobalReceiver(RequestPurseActionC2SPacket.ID, RequestPurseActionC2SPacket::onPacket);
        ServerPlayNetworking.registerGlobalReceiver(ShopScreenHandlerRequestC2SPacket.ID, ShopScreenHandlerRequestC2SPacket::onPacket);

        VillagerTradesHandler.init();

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            VillagerTradesHandler.broadcastErrors(server);
        });
    }

}
