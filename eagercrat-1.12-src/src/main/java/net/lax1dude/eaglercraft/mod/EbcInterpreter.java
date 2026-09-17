package net.lax1dude.eaglercraft.mod;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Executes versioned Eagler bytecode without exposing JVM or browser APIs. */
public final class EbcInterpreter {
    private static final int MAGIC = 0x45424331;
    private static final int VERSION = 1;
    private static final int OP_LOG = 1;
    private static final int OP_REGISTER_COMMAND = 2;
    private static final int OP_TICK = 3;
    private static final int OP_ON_LOAD = 4;
    private static final int OP_ON_UNLOAD = 5;
    private static final int OP_REGISTER_EVENT = 6;
    private static final int OP_SET_CONFIG = 7;
    private static final int OP_REGISTER_KEYBIND = 8;
    private static final int OP_ON_KEY_PRESSED = 9;
    private static final int OP_OPEN_SCREEN = 10;
    private static final int OP_REGISTER_CONFIG = 11;
    private static final int OP_END = 0;
    private static final int MAX_INSTRUCTIONS = 4096;
    private static final int MAX_STRING_BYTES = 4096;

    private EbcInterpreter() {
    }

    public static void execute(ModPackage modPackage, byte[] bytecode, EaglerModHost host) throws IOException {
        if (modPackage == null || bytecode == null || host == null) {
            throw new IOException("EBC execution requires a package, bytecode, and host");
        }
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytecode));
        if (input.readInt() != MAGIC || input.readUnsignedByte() != VERSION) {
            throw new IOException("Unsupported EBC header");
        }
        int instructions = 0;
        boolean ended = false;
        while (!ended) {
            if (++instructions > MAX_INSTRUCTIONS) {
                throw new IOException("EBC instruction limit exceeded");
            }
            int opcode;
            try {
                opcode = input.readUnsignedByte();
            } catch (EOFException exception) {
                throw new IOException("EBC program has no END instruction", exception);
            }
            switch (opcode) {
            case OP_LOG:
                host.log(modPackage.getId(), readString(input));
                break;
            case OP_REGISTER_COMMAND:
                host.registerCommand(modPackage.getId(), readString(input));
                break;
            case OP_TICK:
                host.onTick(modPackage.getId());
                break;
            case OP_ON_LOAD:
                host.onLoad(modPackage.getId());
                break;
            case OP_ON_UNLOAD:
                host.onUnload(modPackage.getId());
                break;
            case OP_REGISTER_EVENT:
                host.onEvent(modPackage.getId(), readString(input), new String[0]);
                break;
            case OP_SET_CONFIG:
                String configKey = readString(input);
                String configValue = readString(input);
                host.setConfig(modPackage.getId(), configKey, configValue);
                break;
            case OP_REGISTER_KEYBIND:
                host.registerKeybind(modPackage.getId(), readString(input), input.readInt());
                break;
            case OP_ON_KEY_PRESSED:
                String keyName = readString(input);
                int keyCode = input.readInt();
                boolean pressed = input.readUnsignedByte() != 0;
                host.onKeyPressed(modPackage.getId(), keyName, keyCode, pressed);
                break;
            case OP_OPEN_SCREEN:
                host.openScreen(modPackage.getId(), readString(input));
                break;
            case OP_REGISTER_CONFIG:
                String registeredConfigKey = readString(input);
                String defaultValue = readString(input);
                host.registerConfig(modPackage.getId(), registeredConfigKey, defaultValue);
                break;
            case OP_END:
                ended = true;
                break;
            default:
                throw new IOException("Unknown EBC opcode: " + opcode);
            }
        }
        if (input.available() != 0) {
            throw new IOException("Trailing bytes after EBC END instruction");
        }
    }

    private static String readString(DataInputStream input) throws IOException {
        int length = input.readUnsignedShort();
        if (length > MAX_STRING_BYTES) {
            throw new IOException("EBC string is too long");
        }
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
