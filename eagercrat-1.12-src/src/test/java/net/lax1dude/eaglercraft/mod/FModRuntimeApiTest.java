package net.lax1dude.eaglercraft.mod;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class FModRuntimeApiTest {

    @Test
    public void runtimeHostReceivesJavaModCallbacks() {
        RecordingHost host = new RecordingHost();
        ModRuntimeContext.setHost(host);
        try {
            FMod.onLoad();
            FMod.registerCommand("hello");
            FMod.registerConfig("hud_mode", "sidebar");
            FMod.setConfig("hud_mode", "compact");
            FMod.log("Loaded");
            FMod.onTick();

            assertEquals("running", host.stage);
            assertTrue(host.commands.contains("hello"));
            assertEquals("compact", host.config.get("hud_mode"));
            assertTrue(host.messages.contains("Loaded"));
            assertEquals(1, host.tickCount);
        } finally {
            ModRuntimeContext.clearHost();
            ModRuntimeContext.clearModId();
        }
    }

    private static final class RecordingHost implements EaglerModHost {
        String stage = "created";
        final List<String> commands = new ArrayList<>();
        final List<String> messages = new ArrayList<>();
        final java.util.Map<String, String> config = new java.util.HashMap<>();
        int tickCount;

        @Override
        public void registerCommand(String modId, String name) {
            commands.add(name);
        }

        @Override
        public void registerConfig(String modId, String key, String defaultValue) {
            config.putIfAbsent(key, defaultValue);
        }

        @Override
        public void setConfig(String modId, String key, String value) {
            config.put(key, value);
        }

        @Override
        public void log(String modId, String message) {
            messages.add(message);
            stage = "loaded";
        }

        @Override
        public void onTick(String modId) {
            tickCount++;
            stage = "running";
        }

        @Override
        public void onLoad(String modId) {
            stage = "loaded";
        }
    }
}
