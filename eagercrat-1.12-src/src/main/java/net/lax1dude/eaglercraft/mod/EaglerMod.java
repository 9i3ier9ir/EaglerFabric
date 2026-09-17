package net.lax1dude.eaglercraft.mod;

/** Compatibility alias for the simpler Java-side mod API. */
public final class EaglerMod {
    private EaglerMod() {
    }

    public static void log(String message) {
        FMod.log(message);
    }

    public static void registerCommand(String name) {
        FMod.registerCommand(name);
    }

    public static void registerKeybind(String keyName, int defaultKeyCode) {
        FMod.registerKeybind(keyName, defaultKeyCode);
    }

    public static void onKeyPressed(String keyName, int keyCode, boolean pressed) {
        FMod.onKeyPressed(keyName, keyCode, pressed);
    }

    public static void openScreen(String screenName) {
        FMod.openScreen(screenName);
    }

    public static void onTick() {
        FMod.onTick();
    }

    public static void onLoad() {
        FMod.onLoad();
    }

    public static void onUnload() {
        FMod.onUnload();
    }

    public static void registerEvent(String event) {
        FMod.registerEvent(event);
    }

    public static void setConfig(String key, String value) {
        FMod.setConfig(key, value);
    }

    public static void registerConfig(String key, String defaultValue) {
        FMod.registerConfig(key, defaultValue);
    }

    public static String getConfig(String key, String defaultValue) {
        return FMod.getConfig(key, defaultValue);
    }
}
