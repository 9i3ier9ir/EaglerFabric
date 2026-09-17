package net.lax1dude.eaglercraft.mod;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

public class EbcInterpreterAdvancedTest {

    @Test
    public void executesAdvancedLifecycleAndTracksState() throws IOException {
        Map<String, byte[]> payloads = new LinkedHashMap<>();
        payloads.put("payload/main.ebc.fbm", assembleAdvancedProgram());

        ModPackage pkg = new ModPackage("demo_mod", "Demo", "description", "1.0.0", payloads);
        RecordingHost host = new RecordingHost();

        EbcInterpreter.execute(pkg, payloads.get("payload/main.ebc.fbm"), host);

        assertEquals("demo_mod", host.modId);
        assertEquals("running", host.stage);
        assertEquals(1, host.tickCount);
        assertTrue(host.commands.contains("hello"));
        assertTrue(host.messages.contains("mod ready"));
        assertTrue(host.events.contains("game_tick"));
        assertEquals("sidebar", host.config.get("hud_mode"));
    }

    @Test
    public void acceptsFabricStyleMetadataAliases() throws IOException {
        ByteArrayOutputStream archiveBytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(archiveBytes)) {
            zip.putNextEntry(new ZipEntry("mod.fbt"));
            zip.write("id=demo_mod\nname=Demo Mod\ndescription=A fabric-like mod\nversion=1.2.3\nsource=fabric\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("payload/main.ebc.fbm"));
            zip.write(assembleAdvancedProgram());
            zip.closeEntry();
        }

        ModPackage mod = ModPackageReader.read(new java.io.ByteArrayInputStream(archiveBytes.toByteArray()));
        assertEquals("demo_mod", mod.getId());
        assertEquals("Demo Mod", mod.getTitle());
        assertEquals("A fabric-like mod", mod.getDescription());
        assertEquals("1.2.3", mod.getVersion());
    }

    @Test
    public void supportsFabricStyleClientHooks() {
        RecordingHost host = new RecordingHost();

        host.registerConfig("demo_mod", "hud_mode", "sidebar");
        host.registerKeybind("demo_mod", "mod_menu_toggle", 76);
        host.onKeyPressed("demo_mod", "mod_menu_toggle", 76, true);
        host.openScreen("demo_mod", "mod_menu");

        assertEquals("hud_mode", host.configKeys.get(0));
        assertEquals("sidebar", host.configDefaults.get("hud_mode"));
        assertEquals("mod_menu_toggle", host.keybinds.get(0));
        assertEquals(76, host.keyCodes.get("mod_menu_toggle").intValue());
        assertEquals("mod_menu", host.screens.get(0));
    }

    private static byte[] assembleAdvancedProgram() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[] {0x45, 0x42, 0x43, 0x31, 0x01});
        out.write(new byte[] {
            0x04,
            0x01, 0x00, 0x09, 'm', 'o', 'd', ' ', 'r', 'e', 'a', 'd', 'y',
            0x02, 0x00, 0x05, 'h', 'e', 'l', 'l', 'o',
            0x03,
            0x06, 0x00, 0x09, 'g', 'a', 'm', 'e', '_', 't', 'i', 'c', 'k',
            0x07, 0x00, 0x08, 'h', 'u', 'd', '_', 'm', 'o', 'd', 'e',
            0x00, 0x07, 's', 'i', 'd', 'e', 'b', 'a', 'r',
            0x00
        });
        return out.toByteArray();
    }

    private static final class RecordingHost implements EaglerModHost {
        String modId;
        String stage = "created";
        int tickCount;
        final java.util.List<String> messages = new java.util.ArrayList<>();
        final java.util.List<String> commands = new java.util.ArrayList<>();
        final java.util.List<String> events = new java.util.ArrayList<>();
        final java.util.List<String> keybinds = new java.util.ArrayList<>();
        final java.util.Map<String, Integer> keyCodes = new java.util.HashMap<>();
        final java.util.List<String> screens = new java.util.ArrayList<>();
        final java.util.List<String> configKeys = new java.util.ArrayList<>();
        final java.util.Map<String, String> configDefaults = new java.util.HashMap<>();
        final java.util.Map<String, String> config = new java.util.HashMap<>();

        @Override
        public void registerConfig(String modId, String key, String defaultValue) {
            this.modId = modId;
            this.configKeys.add(key);
            this.configDefaults.put(key, defaultValue);
            this.config.putIfAbsent(key, defaultValue);
        }

        @Override
        public void registerKeybind(String modId, String keyName, int defaultKeyCode) {
            this.modId = modId;
            this.keybinds.add(keyName);
            this.keyCodes.put(keyName, defaultKeyCode);
        }

        @Override
        public void onKeyPressed(String modId, String keyName, int keyCode, boolean pressed) {
            this.modId = modId;
            if (pressed) {
                this.keyCodes.put(keyName, keyCode);
            }
        }

        @Override
        public void openScreen(String modId, String screenName) {
            this.modId = modId;
            this.screens.add(screenName);
        }

        @Override
        public void log(String modId, String message) {
            this.modId = modId;
            this.stage = "loaded";
            this.messages.add(message);
        }

        @Override
        public void registerCommand(String modId, String name) {
            this.modId = modId;
            this.commands.add(name);
        }

        @Override
        public void onTick(String modId) {
            this.modId = modId;
            this.tickCount++;
            this.stage = "running";
        }

        @Override
        public void onEvent(String modId, String event, String[] args) {
            this.modId = modId;
            this.events.add(event);
        }

        @Override
        public void setConfig(String modId, String key, String value) {
            this.modId = modId;
            this.config.put(key, value);
        }
    }
}
