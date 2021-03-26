package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.item.CoinItem;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import com.glisco.numismaticoverhaul.villagers.VillagerTradesHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
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

    public static final Logger LOGGER = LogManager.getLogger("numismatic-overhaul");

    @Override
    public void onInitialize() {

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bronze_coin"), BRONZE_COIN);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "silver_coin"), SILVER_COIN);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gold_coin"), GOLD_COIN);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "money_bag"), MONEY_BAG);

        ServerPlayNetworking.registerGlobalReceiver(RequestPurseActionC2SPacket.ID, RequestPurseActionC2SPacket::onPacket);

        VillagerTradesHandler.init();

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            VillagerTradesHandler.broadcastErrors(server);
        });
    }

}
