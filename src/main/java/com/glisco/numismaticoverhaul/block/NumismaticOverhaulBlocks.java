package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class NumismaticOverhaulBlocks implements BlockRegistryContainer {

    public static final Block SHOP = new ShopBlock(false);
    public static final Block INEXHAUSTIBLE_SHOP = new ShopBlock(true);
    public static final Block PIGGY_BANK = new PiggyBankBlock();

    @Override
    public BlockItem createBlockItem(Block block, String identifier) {
        if (block == INEXHAUSTIBLE_SHOP) {
            return new BlockItem(block, new Item.Settings().group(NumismaticOverhaul.NUMISMATIC_GROUP).rarity(Rarity.EPIC)) {
                @Override
                public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                    tooltip.add(Text.translatable(stack.getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
                }
            };
        } else if (block == PIGGY_BANK) {
            return new BlockItem(block, new FabricItemSettings().group(NumismaticOverhaul.NUMISMATIC_GROUP).equipmentSlot(stack -> EquipmentSlot.HEAD)) {
                @Override
                public Optional<TooltipData> getTooltipData(ItemStack stack) {
                    if (stack.hasNbt() && stack.getNbt().contains("BlockEntityTag")) {
                        var items = DefaultedList.ofSize(3, ItemStack.EMPTY);
                        Inventories.readNbt(stack.getSubNbt("BlockEntityTag"), items);

                        var values = new long[]{items.get(0).getCount(), items.get(1).getCount(), items.get(2).getCount()};
                        return Optional.of(new CurrencyTooltipData(values, new long[]{-1}));
                    } else {
                        return Optional.empty();
                    }
                }
            };
        }

        return new BlockItem(block, new Item.Settings().group(NumismaticOverhaul.NUMISMATIC_GROUP));
    }

    public static final class Entities implements AutoRegistryContainer<BlockEntityType<?>> {

        public static final BlockEntityType<ShopBlockEntity> SHOP =
                FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, NumismaticOverhaulBlocks.SHOP, NumismaticOverhaulBlocks.INEXHAUSTIBLE_SHOP).build();

        public static final BlockEntityType<PiggyBankBlockEntity> PIGGY_BANK =
                FabricBlockEntityTypeBuilder.create(PiggyBankBlockEntity::new, NumismaticOverhaulBlocks.PIGGY_BANK).build();

        @Override
        public Registry<BlockEntityType<?>> getRegistry() {
            return Registry.BLOCK_ENTITY_TYPE;
        }

        @Override
        public Class<BlockEntityType<?>> getTargetFieldType() {
            //noinspection unchecked
            return (Class<BlockEntityType<?>>) (Object) BlockEntityType.class;
        }
    }
}
