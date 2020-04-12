package moe.gensoukyo.neat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ToggleKeybind {
    KeyBinding key = new KeyBinding("keybind.neat.toggle", 0, "key.categories.misc");
    boolean down;

    public ToggleKeybind() {
        ClientRegistry.registerKeyBinding(this.key);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        boolean wasDown = this.down;
        this.down = this.key.isKeyDown();
        if (mc.inGameHasFocus && this.down && !wasDown) {
            NeatConfig.draw = !NeatConfig.draw;
        }
    }
}
