package net.lax1dude.eaglercraft.mod;

public final class FMod {
    public static final String DEFAULT_MOD_ID = "eagler_mod";

    private FMod() {
    }

    public static void onLoad() {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.onLoad(currentModId());
        }
    }

    public static void onUnload() {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.onUnload(currentModId());
        }
    }

    public static void onTick() {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.onTick(currentModId());
        }
    }

    public static void log(String message) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.log(currentModId(), message);
        }
    }

    public static void registerCommand(String name) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.registerCommand(currentModId(), name);
        }
    }

    public static void registerEvent(String event) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.onEvent(currentModId(), event, new String[0]);
        }
    }

    public static void setConfig(String key, String value) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.setConfig(currentModId(), key, value);
        }
    }

    public static String getConfig(String key, String defaultValue) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            return host.getConfig(currentModId(), key, defaultValue);
        }
        return defaultValue;
    }

    public static void registerConfig(String key, String defaultValue) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.registerConfig(currentModId(), key, defaultValue);
        }
    }

    public static void registerKeybind(String keyName, int defaultKeyCode) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.registerKeybind(currentModId(), keyName, defaultKeyCode);
        }
    }

    public static void onKeyPressed(String keyName, int keyCode, boolean pressed) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.onKeyPressed(currentModId(), keyName, keyCode, pressed);
        }
    }

    public static void openScreen(String screenName) {
        EaglerModHost host = ModRuntimeContext.getHost();
        if (host != null) {
            host.openScreen(currentModId(), screenName);
        }
    }

    public static void setModId(String modId) {
        ModRuntimeContext.setModId(modId);
    }

    public static void clearModId() {
        ModRuntimeContext.clearModId();
    }

    private static String currentModId() {
        String modId = ModRuntimeContext.getModId();
        return modId != null && !modId.isEmpty() ? modId : DEFAULT_MOD_ID;
    }
}
