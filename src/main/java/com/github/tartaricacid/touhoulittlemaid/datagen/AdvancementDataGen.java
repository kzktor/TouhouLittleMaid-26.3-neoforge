package com.github.tartaricacid.touhoulittlemaid.datagen;

import com.github.tartaricacid.touhoulittlemaid.advancements.rewards.GiveSmartSlabConfigTrigger;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.BaseAdvancement;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.ChallengeAdvancement;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.FavorabilityAdvancement;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.MaidBaseAdvancement;
import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;

import java.util.List;

/**
 * 26.3 把 advancement 从「普通 DataProvider 写 json 文件」改成了数据包注册表
 * （{@code Registries.ADVANCEMENT} 已进 {@code RegistryDataLoader.RELOADABLE_REGISTRIES}），
 * 所以这里不再是 DataProvider，而是 {@link SingleRegistryBootstrap}，由
 * {@link RegistryDataGenerator#RELOADABLE_BUILDER} 挂到 {@code RegistrySetBuilder} 上。
 *
 * <p>子提供者同时从接口变成了抽象类：构造时接收 {@link BootstrapContext}，{@code generate()} 无参，
 * 落盘改走 {@code Advancement.Builder.save(BootstrapContext, String)}（字符串仍是完整 id）。
 */
public class AdvancementDataGen {
    public static final SingleRegistryBootstrap<Advancement> PROVIDER = new AdvancementProvider(List.of(
            MainAdvancement::new,
            GiveSmartSlab::new
    ));

    private static final class GiveSmartSlab extends AdvancementSubProvider {
        private GiveSmartSlab(BootstrapContext<Advancement> context) {
            super(context);
        }

        @Override
        public void generate() {
            Advancement.Builder.advancement()
                    .addCriterion("tick", GiveSmartSlabConfigTrigger.create())
                    .rewards(AdvancementRewards.Builder.loot(this.output.lookup(Registries.LOOT_TABLE).getOrThrow(LootTableGenerator.GIVE_SMART_SLAB)))
                    .save(this.output, IdentifierUtil.modLoc("give_smart_slab").toString());
        }
    }

    private static final class MainAdvancement extends AdvancementSubProvider {
        private MainAdvancement(BootstrapContext<Advancement> context) {
            super(context);
        }

        @Override
        public void generate() {
            BaseAdvancement.generate(this.output);
            MaidBaseAdvancement.generate(this.output);
            FavorabilityAdvancement.generate(this.output);
            ChallengeAdvancement.generate(this.output);
        }
    }
}
