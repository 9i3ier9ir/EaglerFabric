package net.lax1dude.eaglercraft.mod;

/** Shared runtime context used by the Java-side mod API. */
public final class ModRuntimeContext {
    private static final ThreadLocal<EaglerModHost> HOST = new ThreadLocal<>();
    private static final ThreadLocal<String> MOD_ID = new ThreadLocal<>();

    private ModRuntimeContext() {
    }

    public static void setHost(EaglerModHost host) {
        HOST.set(host);
    }

    public static void clearHost() {
        HOST.remove();
    }

    public static void setModId(String modId) {
        MOD_ID.set(modId);
    }

    public static void clearModId() {
        MOD_ID.remove();
    }

    public static EaglerModHost getHost() {
        return HOST.get();
    }

    public static String getModId() {
        return MOD_ID.get();
    }
}
