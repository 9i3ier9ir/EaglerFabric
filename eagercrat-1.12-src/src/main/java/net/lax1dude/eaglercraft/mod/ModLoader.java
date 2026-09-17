package net.lax1dude.eaglercraft.mod;

/** Executes validated package payloads when a compatible Eagler runtime is available. */
public interface ModLoader {
    boolean supports(ModPackage modPackage);

    void load(ModPackage modPackage) throws Exception;
}
