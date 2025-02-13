package me.javivi.kkeys;

import com.mojang.logging.LogUtils;
import me.javivi.kkeys.commands.KKeysCommand;
import me.javivi.kkeys.network.PacketHandler;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(kkeys.MODID)
public class kkeys {
    public static final String MODID = "kkeys";
    static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static kkeys INSTANCE;
    private final Config config = new Config();

    public kkeys() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        INSTANCE = this;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketHandler.init();
        config.loadConfig();
        LOGGER.info("KKeys initialized");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            config.loadConfig();
            config.startConfigWatcher();
        });
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        KKeysCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server - KKeys");
    }

    public static kkeys getInstance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return this.config;
    }
}
