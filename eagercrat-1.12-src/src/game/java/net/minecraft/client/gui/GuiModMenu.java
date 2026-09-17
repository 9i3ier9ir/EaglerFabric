package net.minecraft.client.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.internal.FileChooserResult;
import net.lax1dude.eaglercraft.mod.ModPackage;
import net.lax1dude.eaglercraft.mod.ModPackageRegistry;
import net.lax1dude.eaglercraft.mod.ModLoader;
import net.lax1dude.eaglercraft.mod.EbcModLoader;
import net.lax1dude.eaglercraft.mod.EaglerModHost;

/** Session-local browser for imported Eagler .fmod.zip packages. */
public class GuiModMenu extends GuiScreen {
    private static final ModPackageRegistry REGISTRY = new ModPackageRegistry();
    private static ModLoader loader = new EbcModLoader(new EaglerModHost() {
        public void onLoad(String modId) {
            System.out.println("[EaglerMod/" + modId + "] loaded");
        }

        public void onUnload(String modId) {
            System.out.println("[EaglerMod/" + modId + "] unloaded");
        }

        public void log(String modId, String message) {
            System.out.println("[EaglerMod/" + modId + "] " + message);
        }

        public void registerCommand(String modId, String name) {
            System.out.println("[EaglerMod/" + modId + "] registered command: " + name);
        }

        public void onCommand(String modId, String command, String[] args) {
            System.out.println("[EaglerMod/" + modId + "] command: " + command + " args=" + java.util.Arrays.toString(args));
        }

        public void onTick(String modId) {
            System.out.println("[EaglerMod/" + modId + "] tick callback registered");
        }
    });
    private final GuiScreen parentScreen;
    private String status = "";

    public GuiModMenu(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public static void setLoader(ModLoader modLoader) {
        loader = modLoader;
    }

    public void initGui() {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 52, 98, 20, "Import mod"));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height - 52, 98, 20, "Back"));
    }

    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            EagRuntime.displayFileChooser("application/zip", "zip");
        } else if (button.id == 2) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    public void updateScreen() {
        if (!EagRuntime.fileChooserHasResult()) {
            return;
        }
        FileChooserResult result = EagRuntime.getFileChooserResult();
        if (result == null) {
            return;
        }
        try {
            ModPackage mod = REGISTRY.install(new ByteArrayInputStream(result.fileData));
            if (loader != null && loader.supports(mod)) {
                loader.load(mod);
                status = "Loaded " + mod.getTitle() + " " + mod.getVersion();
            } else {
                status = "Imported " + mod.getTitle() + " " + mod.getVersion() + " (loader unavailable)";
            }
        } catch (Exception exception) {
            status = "Import failed: " + exception.getMessage();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Eagler Mods", this.width / 2, 24, 0xFFFFFF);
        int y = 48;
        for (ModPackage mod : REGISTRY.getAll()) {
            this.drawCenteredString(this.fontRendererObj,
                    mod.getTitle() + " " + mod.getVersion() + " (" + mod.getId() + ")", this.width / 2, y,
                    0xE0E0E0);
            y += 12;
        }
        if (!status.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, status, this.width / 2, this.height - 76, 0xA0FFA0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}