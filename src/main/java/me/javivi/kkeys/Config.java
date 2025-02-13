package me.javivi.kkeys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = kkeys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private ArrayList<String> hiddenCategories = new ArrayList<>();
    private ArrayList<String> hiddenKeybinds = new ArrayList<>();
    private boolean consoleLogs = false;
    private Set<String> disabledKeys = new HashSet<>();
    private static long lastModified;
    
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    static final ForgeConfigSpec SPEC = BUILDER.build();

    public ArrayList<String> getHiddenKeybinds() {
        return hiddenKeybinds;
    }

    public void setHiddenKeybinds(Set<String> hiddenKeybinds) {
        this.hiddenKeybinds = new ArrayList<>(hiddenKeybinds);
    }

    public Set<String> getDisabledKeys() {
        return this.disabledKeys;
    }

    public void setDisabledKeys(Set<String> disabledKeys) {
        this.disabledKeys = disabledKeys;
    }

    public boolean isKeyDisabled(String keyName) {
        return this.disabledKeys != null && this.disabledKeys.contains(keyName);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isCategoryHidden(String category) {
        return hiddenCategories.contains(category);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isKeybindHidden(String keybind) {
        return hiddenKeybinds.contains(keybind);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isKeybindHidden(KeyMapping keyMapping) {
        return isCategoryHidden(keyMapping.getCategory()) || isKeybindHidden(keyMapping.getName());
    }

    public void saveConfig() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configPath = configDir.resolve("kkeys.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            // Inicializar colecciones si son null
            if (this.hiddenCategories == null) this.hiddenCategories = new ArrayList<>();
            if (this.hiddenKeybinds == null) this.hiddenKeybinds = new ArrayList<>();
            if (this.disabledKeys == null) this.disabledKeys = new HashSet<>();
            
            Files.writeString(configPath, gson.toJson(this));
            lastModified = Files.getLastModifiedTime(configPath).toMillis();
        } catch (IOException e) {
            kkeys.LOGGER.error("Error saving config file: " + e.getMessage());
        }
    }

    public void loadConfig() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            Path configPath = configDir.resolve("kkeys.json");
            
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (!Files.exists(configPath)) {
                saveConfig();
                return;
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = Files.readString(configPath);
            
            if (jsonContent.isEmpty()) {
                saveConfig();
            } else {
                Config config = gson.fromJson(jsonContent, Config.class);
                this.hiddenCategories = config.hiddenCategories != null ? config.hiddenCategories : new ArrayList<>();
                this.hiddenKeybinds = config.hiddenKeybinds != null ? config.hiddenKeybinds : new ArrayList<>();
                this.consoleLogs = config.consoleLogs;
                this.disabledKeys = config.disabledKeys != null ? config.disabledKeys : new HashSet<>();
                lastModified = Files.getLastModifiedTime(configPath).toMillis();
            }
        } catch (IOException e) {
            kkeys.LOGGER.error("Error loading config file: " + e.getMessage());
            // Inicializar con valores por defecto si hay error
            this.hiddenCategories = new ArrayList<>();
            this.hiddenKeybinds = new ArrayList<>();
            this.disabledKeys = new HashSet<>();
            this.consoleLogs = false;
            // Intentar crear el archivo de configuración por defecto
            saveConfig();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void filterKeyBindings() {
        KeyMapping[] keyMappings = Minecraft.getInstance().options.keyMappings;
        List<KeyMapping> visibleKeyBindings = new ArrayList<>();

        for (KeyMapping keyMapping : keyMappings) {
            if (!isKeybindHidden(keyMapping)) {
                visibleKeyBindings.add(keyMapping);
            }
        }

        Minecraft.getInstance().options.keyMappings = visibleKeyBindings.toArray(new KeyMapping[0]);
    }

    @OnlyIn(Dist.CLIENT)
    public void startConfigWatcher() {
        Thread watcherThread = new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000L);
                    Path path = FMLPaths.CONFIGDIR.get().resolve("kkeys.json");
                    if (Files.exists(path)) {
                        long currentModified = Files.getLastModifiedTime(path).toMillis();
                        if (currentModified != lastModified) {
                            loadConfig();
                            lastModified = currentModified;
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    kkeys.LOGGER.error("Error in config watcher: " + e.getMessage());
                    return;
                }
            }
        });
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // Config loading is now handled through JSON
    }
}
