package net.lax1dude.eaglercraft.mod;

/** Safe operations exposed to Eagler bytecode mods. */
public interface EaglerModHost {
    default void onLoad(String modId) {
    }

    default void onUnload(String modId) {
    }

    default void onCommand(String modId, String command, String[] args) {
    }

    default void onEvent(String modId, String event, String[] args) {
    }

    default String getConfig(String modId, String key, String defaultValue) {
        return defaultValue;
    }

    default void setConfig(String modId, String key, String value) {
    }

    default void registerConfig(String modId, String key, String defaultValue) {
    }

    default void log(String modId, String message) {
    }

    default void registerCommand(String modId, String name) {
    }

    default void registerKeybind(String modId, String keyName, int defaultKeyCode) {
    }

    default void onKeyPressed(String modId, String keyName, int keyCode, boolean pressed) {
    }

    default void openScreen(String modId, String screenName) {
    }

    default void onTick(String modId) {
    }
}
