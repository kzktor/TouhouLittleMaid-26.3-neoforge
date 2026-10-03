package com.github.tartaricacid.touhoulittlemaid.mixin.client;

import com.github.tartaricacid.touhoulittlemaid.api.mixin.IDrawableGizmoPrimitives$TextMixin;
import com.github.tartaricacid.touhoulittlemaid.mixin.accessor.RenderTypeFeatureRendererInvoker;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.feature.GizmoFeatureRenderer;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 让公告板文字（{@code DisplayModeTextGizmo}）能用上自定义的 {@link Font.DisplayMode}。
 * <p>
 * 26.1 时原版把文字画在 {@code DrawableGizmoPrimitives.Group.renderTexts} 里、硬编码
 * {@code Font.DisplayMode.NORMAL}，所以当时是包住那句 {@code Font.drawInBatch} 来换模式。
 * 26.3 渲染改成提交式之后 {@code Group} 只剩数据、{@code renderTexts} 和 {@code drawInBatch}
 * 都没了，真正画字的地方换成了 {@code GizmoFeatureRenderer.buildTexts}，而 DisplayMode 依旧
 * 硬编码 NORMAL。
 * <p>
 * 于是改成接管 {@code Font.PreparedText.visit}：原版那句 INVOKE 的调用点在 buildTexts 自己的
 * 方法体里（匿名内部类只是入参），所以能直接从 buildTexts 拿到循环变量 {@code text}，按 TLM
 * 记下的模式把字重画一遍。模式是 NORMAL 时走原路径，行为与原版完全一致。
 * <p>
 * 画字要取顶点缓冲的那句 {@code getVertexBuilder} 是**父类** {@code RenderTypeFeatureRenderer}
 * 声明的，而 {@code @Shadow} 只在目标类自身声明的成员里查——所以这里改用
 * {@link RenderTypeFeatureRendererInvoker}（注入父类的接口式 {@code @Invoker}）来调它。
 */
@Mixin(GizmoFeatureRenderer.class)
public abstract class GizmoFeatureRendererMixin {
    @Shadow
    private PoseStack poseStack;

    @WrapOperation(
            method = "buildTexts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font$PreparedText;visit(Lnet/minecraft/client/gui/Font$GlyphVisitor;)V"
            )
    )
    private void tlm$renderTextWithDisplayMode(Font.PreparedText prepared, Font.GlyphVisitor visitor,
                                               Operation<Void> original,
                                               @Local DrawableGizmoPrimitives.Text text) {
        Font.DisplayMode mode = ((IDrawableGizmoPrimitives$TextMixin) (Object) text).tlm$getDisplayMode();
        if (mode == null || mode == Font.DisplayMode.NORMAL) {
            original.call(prepared, visitor);
            return;
        }

        // 这里 poseStack 已经 translate/rotate/scale 过、还没 popPose，取到的就是原版那个 pose 局部量。
        Matrix4f pose = this.poseStack.last().pose();
        prepared.visit(new Font.GlyphVisitor() {
            @Override
            public void acceptRenderable(TextRenderable renderable) {
                // GlyphVisitor 全是 default 方法，没法用 lambda，只能老老实实写匿名类。
                // 亮度 15728880 / flat=false 与原版一致。
                // 匿名类里的 this 是 GlyphVisitor，要显式限定到外层 mixin 实例
                VertexConsumer buffer = ((RenderTypeFeatureRendererInvoker) GizmoFeatureRendererMixin.this)
                        .tlm$getVertexBuilder(renderable.renderType(mode));
                renderable.render(pose, buffer, 15728880, false);
            }
        });
    }
}
