package net.lax1dude.eaglercraft.mod;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ModRuntimeFoundationTest {

    @Test
    public void registryTracksEnabledStateAndConfig() {
        ModRuntimeRegistry registry = new ModRuntimeRegistry();
        ModPackage pkg = new ModPackage("demo_mod", "Demo Mod", "Desc", "1.0.0",
                Collections.singletonMap("payload/main.ebc.fbm", new byte[] { 0x45, 0x42, 0x43, 0x31, 0x01, 0x00 }));

        registry.install(pkg);
        registry.setEnabled("demo_mod", true);
        registry.getConfigStore().register("demo_mod", "hud_mode", "sidebar");
        registry.getConfigStore().set("demo_mod", "hud_mode", "compact");

        assertTrue(registry.isEnabled("demo_mod"));
        assertEquals("compact", registry.getConfigStore().get("demo_mod", "hud_mode"));
        assertEquals("compact", registry.getConfigStore().snapshot("demo_mod").get("hud_mode"));
    }

    @Test
    public void eventBusDispatchesToRegisteredListeners() {
        ModEventBus bus = new ModEventBus();
        final List<String> events = new ArrayList<>();

        bus.registerListener("game_tick", (modId, args) -> events.add(modId + ":" + String.join(",", args)));
        bus.emit("game_tick", "demo_mod", new String[] { "frame=1" });

        assertEquals(1, events.size());
        assertEquals("demo_mod:frame=1", events.get(0));
    }
}
