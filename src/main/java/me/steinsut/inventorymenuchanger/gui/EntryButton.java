package me.steinsut.inventorymenuchanger.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import me.steinsut.inventorymenuchanger.network.NetworkHandler;
import me.steinsut.inventorymenuchanger.network.client.CPacketOpenEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class EntryButton extends AbstractButton {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IChangerEntry entry;
    public boolean isCurrentEntry;
    public EntryButton(IChangerEntry entry, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.entry = entry;
        this.isCurrentEntry = false;
    }

    public IChangerEntry getEntry() {
        return this.entry;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(!this.isCurrentEntry) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
        return false;
    }

    @Override
    public void onPress() {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            CPacketOpenEntry packet = new CPacketOpenEntry(player.containerMenu.getCarried(), this.entry.getEntryLocation());
            NetworkHandler.getInstance().sendToServer(packet);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int p_275505_, int p_275674_, float p_275696_) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();

        guiGraphics.blit(EntryScrollPanelNew.PANEL_ATLAS, getX(), getY(), 89, 0, 28, 28);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.entry.getIconLocation());
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(getX(), getY() + this.height, 0).uv(0, 1).endVertex();
        builder.vertex(getX() + this.width, getY() + this.height, 0).uv(1, 1).endVertex();
        builder.vertex(getX() + this.width, getY(), 0).uv(1, 0).endVertex();
        builder.vertex(getX(), getY(), 0).uv(0, 0).endVertex();
        tess.end();


        if(this.isCurrentEntry) {
            guiGraphics.fill(getX(), getY(),
                            getX() + this.width, getY() + this.height,
                            0x31FF0000);
        }
    }
}
