package me.steinsut.inventorymenuchanger.mixin;

import me.steinsut.inventorymenuchanger.event.MenuEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Final private static Logger LOGGER;

    private MinecraftMixin() {}

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V"))
    private void init(Screen screen, CallbackInfo ci) {
        if(screen == null) {
            return;
        }
        if(screen instanceof MenuAccess<?> access) {
            try {
                AbstractContainerMenu menu = access.getMenu();
                MenuType<?> type = menu.getType();
                MenuEvent.PreDisplay event = new MenuEvent.PreDisplay(menu,  type, Minecraft.getInstance().player.containerMenu.containerId);
                MinecraftForge.EVENT_BUS.post(event);
            }
            catch (UnsupportedOperationException ignored) {

            }
        }
    }

}
