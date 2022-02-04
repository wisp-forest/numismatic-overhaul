package com.glisco.numismaticoverhaul.client.entity;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.NumismaticOverhaulClient;
import com.glisco.numismaticoverhaul.entity.TaxCollectorEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.util.Identifier;

public class TaxCollectorEntityRenderer extends MobEntityRenderer<TaxCollectorEntity, VillagerResemblingModel<TaxCollectorEntity>> {

    public TaxCollectorEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel<>(context.getPart(NumismaticOverhaulClient.TAX_COLLECTOR_LAYER)), 0.5F);
        this.addFeature(new HeadFeatureRenderer<>(this, context.getModelLoader()));
        this.addFeature(new VillagerHeldItemFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(TaxCollectorEntity entity) {
        return NumismaticOverhaul.id("textures/entity/tax_collector.png");
    }
}
