package com.vaultdisplay.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vaultdisplay.block.StoragePanelBlock;
import com.vaultdisplay.blockentity.StoragePanelBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class StoragePanelRenderer implements BlockEntityRenderer<StoragePanelBlockEntity> {

    private final Font font;

    public StoragePanelRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(StoragePanelBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(StoragePanelBlock.FACING);
        AttachFace face  = state.getValue(StoragePanelBlock.FACE);

        poseStack.pushPose();

        final float F = 14f / 16f + 0.002f;
        final float B = 1f - F;

        float tx, ty, tz, yRot;

        switch (face) {
            case FLOOR -> {
                tx = 0.5f; tz = 0.5f; ty = F;
                yRot = switch (facing) {
                    case SOUTH -> 180f; case WEST -> 90f; case EAST -> 270f; default -> 0f;
                };
                poseStack.translate(tx, ty, tz);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                poseStack.translate(0, 0, -0.74);
                float scaleFl = 1f / 64f;
                poseStack.scale(scaleFl, -scaleFl, scaleFl);
            }
            case CEILING -> {
                tx = 0.5f; tz = 0.5f; ty = B;
                yRot = switch (facing) {
                    case SOUTH -> 0f; case NORTH -> 180f; case WEST -> 270f; case EAST -> 90f; default -> 0f;
                };
                poseStack.translate(tx, ty, tz);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                poseStack.mulPose(Axis.YP.rotationDegrees(180f));
                poseStack.translate(0, 0, -0.74);
                float scaleCl = 1f / 64f;
                poseStack.scale(scaleCl, -scaleCl, scaleCl);
            }
            default -> {
                ty = 0.5f;
                switch (facing) {
                    case NORTH -> { tx = 0.5f; tz = B;    yRot = 180f; }
                    case SOUTH -> { tx = 0.5f; tz = F;    yRot = 0f;   }
                    case WEST  -> { tx = B;    tz = 0.5f; yRot = 270f; }
                    case EAST  -> { tx = F;    tz = 0.5f; yRot = 90f;  }
                    default    -> { tx = 0.5f; tz = B;    yRot = 180f; }
                }
                poseStack.translate(tx, ty, tz);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.translate(0, 0, -0.74);
                float scaleW = 1f / 64f;
                poseStack.scale(scaleW, -scaleW, scaleW);
            }
        }

        // scale уже применён внутри каждого case

        Matrix4f matrix = poseStack.last().pose();

        if (!be.isVaultFound()) {
            renderCenteredText(matrix, bufferSource, "No Vault", 0, -4, 0xFFAAAAAA, packedLight);
        } else {
            int current = be.getCurrentItems();
            int max     = be.getMaxItems();
            float pct   = be.getFillPercent();

            // Сверху: текущее количество (крупно)
            renderCenteredText(matrix, bufferSource, formatCount(current), 0, -24, 0xFFFFFFFF, packedLight);

            // Процент
            String pctText = String.format("%.1f%%", pct * 100f);
            renderCenteredText(matrix, bufferSource, pctText, 0, -10, getPercentColor(pct), packedLight);

            // Прогресс-бар
            renderProgressBar(matrix, bufferSource, pct, packedLight);

            // Снизу: максимум (серым)
            renderCenteredText(matrix, bufferSource, formatCount(max), 0, 18, 0xFFAAAAAA, packedLight);        }

        poseStack.popPose();
    }

    private void renderCenteredText(Matrix4f matrix, MultiBufferSource buffer,
                                     String text, float x, float y,
                                     int color, int light) {
        int width = font.width(text);
        font.drawInBatch(text, x - width / 2f, y, color, false,
                matrix, buffer, Font.DisplayMode.NORMAL, 0, light);
    }

    private void renderProgressBar(Matrix4f matrix, MultiBufferSource buffer,
                                    float fill, int light) {
        // Рисуем прогресс-бар символами █ через font — без квадов, без z-fighting
        int totalChars  = 5;
        int filledChars = Math.round(fill * totalChars);

        StringBuilder filledSb = new StringBuilder();
        StringBuilder emptySb  = new StringBuilder();
        for (int i = 0; i < filledChars; i++)          filledSb.append('█');
        for (int i = filledChars; i < totalChars; i++) emptySb.append('█');

        String filledStr = filledSb.toString();
        String emptyStr  = emptySb.toString();

        int totalWidth = font.width(filledStr + emptyStr);
        float startX   = -totalWidth / 2f;
        float y        = 3f;

        if (!filledStr.isEmpty()) {
            font.drawInBatch(filledStr, startX, y, getBarColor(fill), false,
                    matrix, buffer, Font.DisplayMode.NORMAL, 0, light);
        }
        if (!emptyStr.isEmpty()) {
            float emptyX = startX + font.width(filledStr);
            font.drawInBatch(emptyStr, emptyX, y, 0xFF444444, false,
                    matrix, buffer, Font.DisplayMode.NORMAL, 0, light);
        }
    }

    private int getBarColor(float fill) {
        if (fill < 0.5f) {
            int r = (int)(fill * 2f * 255);
            return 0xFF000000 | (r << 16) | (0xFF << 8);
        } else {
            int g = (int)((1f - fill) * 2f * 255);
            return 0xFF000000 | (0xFF << 16) | (g << 8);
        }
    }

    private int getPercentColor(float fill) {
        if (fill < 0.5f)  return 0xFF55FF55;
        if (fill < 0.85f) return 0xFFFFFF55;
        return 0xFFFF5555;
    }

    private String formatCount(int count) {
        if (count >= 1_000_000) return String.format("%.1fM", count / 1_000_000f);
        if (count >= 1_000)     return String.format("%.1fk", count / 1_000f);
        return String.valueOf(count);
    }
}
