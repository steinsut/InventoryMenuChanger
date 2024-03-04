package me.steinsut.inventorymenuchanger.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.api.ApiConstants;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntryScrollPanelNew extends AbstractContainerEventHandler implements Renderable {
    public static final ResourceLocation PANEL_ATLAS = new ResourceLocation(ApiConstants.MOD_ID, "textures/gui/panel_24x.png");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Rect2i VERTICAL_TEX_RECT = new Rect2i(0, 0, 49, 40);
    private static final Rect2i VERTICAL_MIDDLE_TEX_RECT = new Rect2i(6, 6, 28, 28);
    private static final Rect2i HORIZONTAL_TEX_RECT = new Rect2i(49, 0, 40, 49);
    private static final Rect2i HORIZONTAL_MIDDLE_TEX_RECT = new Rect2i(55, 6, 28, 28);
    private static final Rect2i VERTICAL_BAR_TEX_RECT = new Rect2i(117, 0, 9, 15);
    private static final Rect2i HORIZONTAL_BAR_TEX_RECT = new Rect2i(126, 0, 15, 9);
    private final Minecraft client;
    private int width;
    private int height;
    private int top;
    private int bottom;
    private int right;
    private int left;
    private boolean scrolling;
    private float scrollDistance;
    private float previousScroll;
    private final int border = 6;
    private final int barBorder = 3;
    private final int barSize = 9;
    private final int barAnchorPos;
    private final int entrySize;
    private int panelLength;
    private final List<EntryButton> entries;
    private final boolean horizontal;
    private Rect2i tlCornerTexRect;
    private Rect2i trCornerTexRect;
    private Rect2i blCornerTexRect;
    private Rect2i brCornerTexRect;
    private Rect2i tSideTexRect;
    private Rect2i lSideTexRect;
    private Rect2i bSideTexRect;
    private Rect2i rSideTexRect;

    public EntryScrollPanelNew(int top, int left) {
        this(top, left, false);
    }

    public EntryScrollPanelNew(int top, int left, boolean horizontal)
    {
        this.client = Minecraft.getInstance();
        this.top = top;
        this.left = left;
        this.horizontal = horizontal;
        calculateSlices();
        this.width = getTextureRect().getWidth();
        this.height = getTextureRect().getHeight();
        this.right = this.left + this.width;
        this.bottom = this.top + this.height;
        if(horizontal) {
            this.entrySize = getMiddleSliceRect().getHeight();
            this.barAnchorPos = top + this.border + this.entrySize;
            this.panelLength = this.width;
        }
        else {
            this.entrySize = getMiddleSliceRect().getWidth();
            this.barAnchorPos = left + this.border + this.entrySize;
            this.panelLength = this.height;
        }
        this.entries = new ArrayList<>();
    }

    private Rect2i getTextureRect() {
        if(this.horizontal) {
            return HORIZONTAL_TEX_RECT;
        }
        else {
            return VERTICAL_TEX_RECT;
        }
    }

    private Rect2i getMiddleSliceRect() {
        if(this.horizontal) {
            return HORIZONTAL_MIDDLE_TEX_RECT;
        }
        else {
            return VERTICAL_MIDDLE_TEX_RECT;
        }
    }


    private Rect2i getBarTextureRect() {
        if(this.horizontal) {
            return HORIZONTAL_BAR_TEX_RECT;
        }
        else {
            return VERTICAL_BAR_TEX_RECT;
        }
    }

    private void calculateSlices() {
        Rect2i texRect = getTextureRect();
        Rect2i middleTexRect = getMiddleSliceRect();

        this.tlCornerTexRect = new Rect2i(texRect.getX(), texRect.getY(),
                middleTexRect.getX() - texRect.getX(), middleTexRect.getY() - texRect.getY());
        this.trCornerTexRect = new Rect2i(middleTexRect.getX() + middleTexRect.getWidth(), texRect.getY(),
                texRect.getWidth() - (this.tlCornerTexRect.getWidth() + middleTexRect.getWidth()), this.tlCornerTexRect.getHeight());
        this.blCornerTexRect = new Rect2i(texRect.getX(), middleTexRect.getY() + middleTexRect.getHeight(),
                this.tlCornerTexRect.getWidth(), texRect.getHeight() - (middleTexRect.getHeight() + this.tlCornerTexRect.getHeight()));
        this.brCornerTexRect = new Rect2i(this.trCornerTexRect.getX(), this.blCornerTexRect.getY(),
                this.trCornerTexRect.getWidth(), this.blCornerTexRect.getHeight());

        this.tSideTexRect = new Rect2i(middleTexRect.getX(), texRect.getY(), middleTexRect.getWidth(), this.tlCornerTexRect.getHeight());
        this.lSideTexRect = new Rect2i(texRect.getX(), middleTexRect.getY(), this.tlCornerTexRect.getWidth(), middleTexRect.getHeight());
        this.bSideTexRect = new Rect2i(middleTexRect.getX(), this.blCornerTexRect.getY(), middleTexRect.getWidth(), this.blCornerTexRect.getHeight());
        this.rSideTexRect = new Rect2i(this.trCornerTexRect.getX(), middleTexRect.getY(), this.trCornerTexRect.getWidth(), middleTexRect.getHeight());
    }

    private int getContentSize() {
        return this.entries.size() * getMiddleSliceRect().getWidth();
    }

    private int getInvisibleContentSize() {
        return this.getContentSize() - (this.panelLength - 2 * this.border);
    }

    private int getMaxScroll() {
        return this.panelLength - getBarLength() - 2 * this.barBorder;
    }

    private int getScrollAmount()
    {
        return 20;
    }

    private void applyScrollLimits()
    {
        int max = getInvisibleContentSize();

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
        int barLength = ((this.panelLength - this.barBorder * 2) * (this.panelLength - this.border * 2)) / this.getContentSize();

        if (barLength < 32) { barLength = 32; }

        if (barLength > this.panelLength - 2 * this.barBorder) { barLength = this.panelLength - 2 * this.barBorder; }
        return barLength;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        if (scroll != 0)
        {
            this.scrollDistance += -scroll * getScrollAmount();
            applyScrollLimits();
            scrollButtons();
            return true;
        }
        return false;
    }

    private void scrollButtons() {
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
        return mouseX >= this.left && mouseX <= this.right &&
                mouseY >= this.top && mouseY <= this.bottom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(mouseX <= this.left || mouseX >= this.right || mouseY <= this.top || mouseY >= this.bottom) { return false; }
        if (super.mouseClicked(mouseX + this.scrollDistance, mouseY, button)) { return true; }
        this.scrolling = button == 0 &&
                ((mouseY >= this.barAnchorPos && mouseY <= this.barAnchorPos + this.barSize && this.horizontal) ||
                        (mouseX >= this.barAnchorPos && mouseX <= this.barAnchorPos + this.barSize && !this.horizontal));
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
            int maxScroll = getMaxScroll();
            double moved;
            if(this.horizontal) {
                moved = deltaX / maxScroll;
            }
            else {
                 moved = deltaY / maxScroll;
            }
            this.scrollDistance += getInvisibleContentSize() * moved;
            applyScrollLimits();
            scrollButtons();
            return true;
        }
        return false;
    }

    public void addEntry(IChangerEntry entry, boolean isCurrent) {
        if(this.horizontal) {
            if(this.entries.size() < 7) {
                this.width += this.entrySize;
                this.left -= this.entrySize / 2;
                this.right = this.left + this.width;
                this.panelLength = this.width;
                for(EntryButton button: this.entries) {
                    button.setX(button.getX() - this.entrySize / 2);
                }
            }
            for(int i = 0; i < 10; i++) {
                EntryButton button = new EntryButton(entry, this.left + this.border + getContentSize(), this.top + this.border, this.entrySize, this.entrySize);
                button.isCurrentEntry = isCurrent;
                this.entries.add(button);
            }
        }
        else {
            if(this.entries.size() < 4) {
                this.height += this.entrySize;
                this.top -= this.entrySize / 2;
                this.bottom = this.top + this.height;
                this.panelLength = this.height;
                for(EntryButton button: this.entries) {
                    button.setY(button.getY() - this.entrySize / 2);
                }
            }
            for(int i = 0; i < 10; i++) {
                EntryButton button = new EntryButton(entry, this.left + this.border, this.top + this.border + getContentSize(), this.entrySize, this.entrySize);
                button.isCurrentEntry = isCurrent;
                this.entries.add(button);
            }
        }
        this.scrollDistance = 0;
        scrollButtons();
    }

    private void renderCorners(GuiGraphics guiGraphics, float partialTicks) {
        guiGraphics.blit(PANEL_ATLAS, this.left, this.top, this.tlCornerTexRect.getX(), this.tlCornerTexRect.getY(), this.tlCornerTexRect.getWidth(), this.tlCornerTexRect.getHeight());
        guiGraphics.blit(PANEL_ATLAS, this.left, this.top + this.height - this.blCornerTexRect.getHeight(), this.blCornerTexRect.getX(), this.blCornerTexRect.getY(), this.blCornerTexRect.getWidth(), this.blCornerTexRect.getHeight());
        guiGraphics.blit(PANEL_ATLAS, this.left + this.width - this.brCornerTexRect.getWidth(), this.top + this.height - this.brCornerTexRect.getHeight(), this.brCornerTexRect.getX(), this.brCornerTexRect.getY(), this.brCornerTexRect.getWidth(), this.brCornerTexRect.getHeight());
        guiGraphics.blit(PANEL_ATLAS, this.left + this.width - this.trCornerTexRect.getWidth(), this.top, this.trCornerTexRect.getX(), this.trCornerTexRect.getY(), this.trCornerTexRect.getWidth(), this.trCornerTexRect.getHeight());
    }

    private void renderSides(GuiGraphics guiGraphics, float partialTicks) {
        guiGraphics.blitRepeating(PANEL_ATLAS, this.left, this.top + this.tlCornerTexRect.getHeight(),
                this.lSideTexRect.getWidth(), this.height - (this.tlCornerTexRect.getHeight() + this.blCornerTexRect.getHeight()),
                this.lSideTexRect.getX(), this.lSideTexRect.getY(), this.lSideTexRect.getWidth(), this.lSideTexRect.getHeight());

        guiGraphics.blitRepeating(PANEL_ATLAS, this.left + this.blCornerTexRect.getWidth(), this.top + this.height - this.blCornerTexRect.getHeight(),
                this.width - (this.blCornerTexRect.getWidth() + this.brCornerTexRect.getWidth()), this.bSideTexRect.getHeight(),
                this.bSideTexRect.getX(), this.bSideTexRect.getY(), this.bSideTexRect.getWidth(), this.bSideTexRect.getHeight());

        guiGraphics.blitRepeating(PANEL_ATLAS, this.left + this.width - this.trCornerTexRect.getWidth(), this.top + this.tlCornerTexRect.getHeight(),
                this.rSideTexRect.getWidth(), this.height - (this.tlCornerTexRect.getHeight() + this.blCornerTexRect.getHeight()),
                this.rSideTexRect.getX(), this.rSideTexRect.getY(), this.rSideTexRect.getWidth(), this.rSideTexRect.getHeight());

        guiGraphics.blitRepeating(PANEL_ATLAS, this.left + this.blCornerTexRect.getWidth(), this.top,
                this.width - (this.tlCornerTexRect.getWidth() + this.trCornerTexRect.getWidth()), this.tSideTexRect.getHeight(),
                this.tSideTexRect.getX(), this.tSideTexRect.getY(), this.tSideTexRect.getWidth(), this.tSideTexRect.getHeight());
    }

    private void renderBackground(GuiGraphics guiGraphics, float partialTicks) {
        renderCorners(guiGraphics, partialTicks);
        renderSides(guiGraphics, partialTicks);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();

        renderBackground(guiGraphics, partialTick);

        double scale = this.client.getWindow().getGuiScale();
        RenderSystem.enableScissor((int)(this.left * scale), (int)(this.client.getWindow().getHeight() - ((this.bottom - this.border) * scale)),
                (int)(this.width * scale), (int)((this.height - 2 * this.border) * scale));

        for(EntryButton button: this.entries) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderSystem.disableScissor();
        RenderSystem.disableDepthTest();

        int posOffset = 0;
        int barLength = this.panelLength - 2 * this.barBorder;
        int barPartLength = Math.max(getBarTextureRect().getWidth() / 3, getBarTextureRect().getHeight() / 3);
        int barMiddleLength = barLength - 2 * barPartLength;
        int extraLength = getInvisibleContentSize();
        if(extraLength > 0) {
            barLength = getBarLength();
            barMiddleLength = barLength - 2 * barPartLength;
            posOffset = (int) this.scrollDistance * (this.panelLength - 2 * this.barBorder - barLength) / extraLength;
        }
        if(this.horizontal) {
            int barLeft = this.left + this.barBorder + posOffset;
            guiGraphics.blit(PANEL_ATLAS, barLeft, this.barAnchorPos, getBarTextureRect().getX(), getBarTextureRect().getY(),
                    this.barSize, barPartLength);
            guiGraphics.blitRepeating(PANEL_ATLAS, barLeft + barPartLength, this.barAnchorPos, barMiddleLength, this.barSize,
                    getBarTextureRect().getX() + barPartLength, getBarTextureRect().getY(), barPartLength, getBarTextureRect().getHeight());
            guiGraphics.blit(PANEL_ATLAS, barLeft + barPartLength + barMiddleLength, this.barAnchorPos,
                    getBarTextureRect().getX() + 2 * barPartLength, getBarTextureRect().getY(), this.barSize, barPartLength);
        }
        else {
            int barTop = this.top + this.barBorder + posOffset;
            guiGraphics.blit(PANEL_ATLAS, this.barAnchorPos, barTop, getBarTextureRect().getX(), getBarTextureRect().getY(),
                    this.barSize, barPartLength);
            guiGraphics.blitRepeating(PANEL_ATLAS, this.barAnchorPos, barTop + barPartLength, this.barSize, barMiddleLength,
                    getBarTextureRect().getX(), getBarTextureRect().getY() + barPartLength, getBarTextureRect().getWidth(), barPartLength);
            guiGraphics.blit(PANEL_ATLAS, this.barAnchorPos, barTop + barPartLength + barMiddleLength,
                    getBarTextureRect().getX(), getBarTextureRect().getY() + 2 * barPartLength, this.barSize, barPartLength);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return Collections.unmodifiableList(this.entries);
    }
}
