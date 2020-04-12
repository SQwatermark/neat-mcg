package moe.gensoukyo.neat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.Set;

@SuppressWarnings("unused")
public class GuiFactory implements IModGuiFactory {
    public GuiFactory() {
    }

    public void initialize(Minecraft minecraftInstance) {
    }

    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public boolean hasConfigGui() {
        return true;
    }

    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new GuiFactory.ConfigGui(parentScreen);
    }

    public static class ConfigGui extends GuiConfig {
        public ConfigGui(GuiScreen parentScreen) {
            super(parentScreen, (new ConfigElement(NeatConfig.config.getCategory("general"))).getChildElements(), "neat", false, false, GuiConfig.getAbridgedConfigPath(NeatConfig.config.toString()));
        }
    }
}
