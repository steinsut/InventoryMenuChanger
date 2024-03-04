package me.steinsut.inventorymenuchanger.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.api.ApiConstants;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntryScrollPanel extends AbstractContainerEventHandler implements Renderable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft client;
    protected final int width;
    protected final int height;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    private boolean scrolling;
    protected float scrollDistance;
    protected float previousScroll;
    protected final int border = 2;

    private final int barSize = 2;
    private final int barPos;
    private final int bgColorFrom = 0xC0101010;
    private final int bgColorTo = 0xD0101010;
    private final int barBgColor = 0xFF000000;
    private final int barColor = 0xFF808080;
    private final int barBorderColor = 0xFFC0C0C0;

    private final int entrySize;
    private final int totalLength;
    private final List<EntryButton> entries;
    private final boolean horizontal;

    public EntryScrollPanel(int width, int height, int top, int left) {
        this(width, height, top, left, false);
    }

    public EntryScrollPanel(int width, int height, int top, int left, boolean horizontal)
    {
        this.client = Minecraft.getInstance();
        this.width = width;
        this.height = height;
        this.top = top;
        this.left = left;
        this.bottom = height + top;
        this.right = width + left;
        this.horizontal = horizontal;
        if(horizontal) {
            this.barPos = top + height - this.barSize;
            this.entrySize = height - 2* this.border - this.barSize;
            this.totalLength = width;
        }
        else {
            this.barPos = left + width - this.barSize;
            this.entrySize = width - 2* this.border - this.barSize;
            this.totalLength = height;
        }
        this.entries = new ArrayList<>();
    }

    public void addEntry(IChangerEntry entry, boolean isCurrent) {
        if(this.horizontal) {
            for(int i = 0; i < 10; i++) {
                EntryButton button = new EntryButton(entry, this.left + this.border + getContentSize(), this.top + this.border, this.entrySize, this.entrySize);
                button.isCurrentEntry = isCurrent;
                this.entries.add(button);
            }
        }
        else {
            for(int i = 0; i < 10; i++) {
                EntryButton button = new EntryButton(entry, this.left + this.border, this.top + this.border + getContentSize(), this.entrySize, this.entrySize);
                button.isCurrentEntry = isCurrent;
                this.entries.add(button);
            }
        }
    }

    private int getContentSize() {
        return this.entries.size() * this.entrySize + this.entries.size() * this.border;
    }

    private int getMaxScroll() {
        if(this.horizontal) {
            return this.getContentSize() - (this.width - this.border);
        }
        return this.getContentSize() - (this.height - this.border);
    }

    private int getScrollAmount()
    {
        return 20;
    }

    private void applyScrollLimits()
    {
        int max = getMaxScroll();

        if (max < 0)
        {
            max /= 2;
        }

        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > max)
        {
            this.scrollDistance = max;
        }
    }

    private int getBarLength()
    {
        int barLength = (this.totalLength * this.totalLength) / this.getContentSize();

        if (barLength < 32) { barLength = 32; }

        if (barLength > this.totalLength) { barLength = this.totalLength; }
        return barLength;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        if (scroll != 0)
        {
            this.scrollDistance += -scroll * getScrollAmount();
            applyScrollLimits();
            updateButtons();
            return true;
        }
        return false;
    }

    private void updateButtons() {
        for(EntryButton button : this.entries) {
            if(this.horizontal) {
                button.setX(button.getX() + (int) this.previousScroll - (int)this.scrollDistance);
                button.visible = (button.getX() + button.getWidth()) > this.left
                        && (button.getX()) < this.right;
            }
            else {
                button.setY(button.getY() + (int) this.previousScroll - (int)this.scrollDistance);
                button.visible = (button.getY() + button.getHeight()) > this.top
                        && (button.getY()) < this.bottom;
            }
        }
        this.previousScroll = this.scrollDistance;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return mouseX >= this.left && mouseX <= this.left + this.width &&
                mouseY >= this.top && mouseY <= this.bottom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(mouseX <= this.left || mouseX >= this.right || mouseY <= this.top || mouseY >= this.bottom) { return false; }
        if (super.mouseClicked(mouseX + this.scrollDistance, mouseY, button)) { return true; }
        this.scrolling = button == 0 &&
                ((mouseY >= this.barPos && mouseY <= this.barPos + this.barSize && this.horizontal) ||
                    mouseX >= this.barPos && mouseX <= this.barPos + this.barSize && !this.horizontal);
        return this.scrolling;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (super.mouseReleased(mouseX, mouseY, button)) { return true; }
        boolean ret = this.scrolling;
        this.scrolling = false;
        return ret;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (this.scrolling)
        {
            int maxScroll = this.totalLength - getBarLength();
            double moved;
            if(this.horizontal) {
                moved = deltaX / maxScroll;
            }
            else {
                 moved = deltaY / maxScroll;
            }
            this.scrollDistance += getMaxScroll() * moved;
            applyScrollLimits();
            updateButtons();
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();

        double scale = this.client.getWindow().getGuiScale();
        RenderSystem.enableScissor((int)(this.left * scale), (int)(this.client.getWindow().getHeight() - (this.bottom * scale)),
                (int)(this.width * scale), (int)(this.height * scale));

        guiGraphics.fillGradient(this.left, this.top, this.right, this.bottom, this.bgColorFrom, this.bgColorTo);

        for(EntryButton button: this.entries) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderSystem.disableDepthTest();

        if(this.horizontal) {
            renderBarHorizontal(guiGraphics, mouseX, mouseY, partialTick);
        }
        else {
            renderBarVertical(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderSystem.disableBlend();
        RenderSystem.disableScissor();
    }

    private void renderBarHorizontal(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();

        int extraWidth = (this.getContentSize() + this.border) - this.width;
        if (extraWidth > 0) {
            int barWidth = getBarLength();

            int barLeft = (int) this.scrollDistance * (this.width - barWidth) / extraWidth + this.left;
            if (barLeft < this.left) {
                barLeft = this.left;
            }

            int barBgAlpha = this.barBgColor >> 24 & 0xff;
            int barBgRed = this.barBgColor >> 16 & 0xff;
            int barBgGreen = this.barBgColor >> 8 & 0xff;
            int barBgBlue = this.barBgColor & 0xff;

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(this.left, this.barPos + this.barSize, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            builder.vertex(this.right, this.barPos + this.barSize, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            builder.vertex(this.right, this.barPos, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            builder.vertex(this.left, this.barPos, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            tess.end();

            int barAlpha = this.barColor >> 24 & 0xff;
            int barRed = this.barColor >> 16 & 0xff;
            int barGreen = this.barColor >> 8 & 0xff;
            int barBlue = this.barColor & 0xff;

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(barLeft, this.barPos + this.barSize, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            builder.vertex(barLeft + barWidth, this.barPos + this.barSize, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            builder.vertex(barLeft + barWidth, this.barPos, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            builder.vertex(barLeft, this.barPos, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            tess.end();

            int barBorderAlpha = this.barBorderColor >> 24 & 0xff;
            int barBorderRed = this.barBorderColor >> 16 & 0xff;
            int barBorderGreen = this.barBorderColor >> 8 & 0xff;
            int barBorderBlue = this.barBorderColor & 0xff;

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(barLeft, this.barPos + this.barSize - 1, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            builder.vertex(barLeft + barWidth - 1, this.barPos + this.barSize - 1, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            builder.vertex(barLeft + barWidth - 1, this.barPos, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            builder.vertex(barLeft, this.barPos, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            tess.end();
        }
    }

    private void renderBarVertical(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();

        int extraHeight = (this.getContentSize() + this.border) - this.height;
        if (extraHeight > 0)
        {
            int barHeight = getBarLength();

            int barTop = (int)this.scrollDistance * (this.height - barHeight) / extraHeight + this.top;
            if (barTop < this.top)
            {
                barTop = this.top;
            }

            int barBgAlpha = this.barBgColor >> 24 & 0xff;
            int barBgRed   = this.barBgColor >> 16 & 0xff;
            int barBgGreen = this.barBgColor >>  8 & 0xff;
            int barBgBlue  = this.barBgColor       & 0xff;

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(this.barPos,            this.bottom, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            builder.vertex(this.barPos + this.barSize, this.bottom, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            builder.vertex(this.barPos + this.barSize, this.top,    0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            builder.vertex(this.barPos,            this.top,    0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
            tess.end();

            int barAlpha = this.barColor >> 24 & 0xff;
            int barRed   = this.barColor >> 16 & 0xff;
            int barGreen = this.barColor >>  8 & 0xff;
            int barBlue  = this.barColor       & 0xff;

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(this.barPos,            barTop + barHeight, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            builder.vertex(this.barPos + this.barSize, barTop + barHeight, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            builder.vertex(this.barPos + this.barSize, barTop,             0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            builder.vertex(this.barPos,            barTop,             0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
            tess.end();

            int barBorderAlpha = this.barBorderColor >> 24 & 0xff;
            int barBorderRed   = this.barBorderColor >> 16 & 0xff;
            int barBorderGreen = this.barBorderColor >>  8 & 0xff;
            int barBorderBlue  = this.barBorderColor       & 0xff;

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(this.barPos,                barTop + barHeight - 1, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            builder.vertex(this.barPos + this.barSize - 1, barTop + barHeight - 1, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            builder.vertex(this.barPos + this.barSize - 1, barTop,                 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            builder.vertex(this.barPos,                barTop,                 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
            tess.end();
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return Collections.unmodifiableList(this.entries);
    }
}
