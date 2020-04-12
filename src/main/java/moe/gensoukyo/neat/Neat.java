package moe.gensoukyo.neat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = Neat.MOD_ID,
        name = Neat.MOD_NAME,
        version = Neat.VERSION,
        guiFactory = Neat.GUI_FACTORY,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.12.2]"
)
public class Neat {
    public static final String MOD_ID = "neatmcg";
    public static final String MOD_NAME = "NeatMCG";
    public static final String VERSION = "1.0";
    public static final String GUI_FACTORY = "moe.gensoukyo.neat.GuiFactory";

    public Neat() {
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NeatConfig.init(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(new ToggleKeybind());
        MinecraftForge.EVENT_BUS.register(new HealthBarRenderer());
    }
}
