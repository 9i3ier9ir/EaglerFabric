package example;

import net.lax1dude.eaglercraft.mod.FMod;

public final class ExampleMod {
    public static void init() {
        FMod.onLoad();
        FMod.registerEvent("game_tick");
        FMod.setConfig("hud_mode", "sidebar");
        FMod.log("Example mod loaded");
        FMod.registerCommand("hello");
        FMod.onTick();
    }
}
