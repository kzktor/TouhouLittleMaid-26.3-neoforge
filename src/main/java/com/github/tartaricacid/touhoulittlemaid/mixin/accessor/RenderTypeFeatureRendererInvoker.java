package com.github.tartaricacid.touhoulittlemaid.mixin.accessor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 暴露 {@link RenderTypeFeatureRenderer#getVertexBuilder} 给 {@code GizmoFeatureRenderer} 用。
 * <p>
 * 26.3 把 {@code buildTexts} 里取顶点缓冲的那句写在 {@code GizmoFeatureRenderer} 自己的方法体里，
 * 但 {@code getVertexBuilder} 是**父类** {@link RenderTypeFeatureRenderer} 声明的 protected final
 * 方法。Mixin 的 {@code @Shadow} 只查目标类自身声明的成员
 * （{@code TargetClassContext.findAliasedMethod} 只遍历 {@code classNode.methods}，外加已被 mixin
 * 注入的方法，**不沿父类链走**），所以在 {@code GizmoFeatureRenderer} 上写 {@code @Shadow} 会直接
 * 报 "was not located in the target class"。
 * <p>
 * 既然方法必须在声明处取，就把它作为接口式 {@code @Invoker} mixin 注入父类：父类被改造后实现了本接口，
 * 子类自然继承，于是 {@code GizmoFeatureRendererMixin} 里 `((RenderTypeFeatureRendererInvoker) this)`
 * 就能拿到同一句调用。本 mixin 目标类是客户端类，必须列在 mixins.json 的 {@code client} 段。
 */
@Mixin(RenderTypeFeatureRenderer.class)
public interface RenderTypeFeatureRendererInvoker {
    @Invoker("getVertexBuilder")
    VertexConsumer tlm$getVertexBuilder(RenderType renderType);
}
