package com.lyecocoworld.alexsmobsserverside.larion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Registers custom Larion density function types into the vanilla
 * DensityFunctions registry by temporarily unfreezing it via reflection.
 *
 * This runs in onLoad() (before any world loads) and works on vanilla
 * Paper / Folia / Canvas — NO Mixin loader (Horizon) required.
 *
 * How it works:
 *   1. The registry (MappedRegistry) has a private boolean "frozen"
 *   2. We reflect to set frozen=false
 *   3. We call Registry.register() to add our custom types
 *   4. We set frozen=true back
 *
 * This is the same technique used by plugins like AntiMerge, WorldEdit
 * for custom registries, etc. It's safe because onLoad() runs in the
 * narrow window between Bootstrap.bootStrap() (which freezes) and
 * world load (which reads the registry).
 *
 * IMPORTANT: This accesses NMS classes via reflection. The class/method
 * names are for Minecraft 1.20.1/1.21.x. On version changes, the obfuscated
 * names may differ but Paper uses Mojang mappings so they should be stable.
 */
public class LarionRegistryHack {

    /**
     * Attempts to unfreeze the DensityFunctions registry and register
     * the 6 custom Larion types.
     *
     * @return number of types registered, or -1 if it failed
     */
    public static int tryRegisterLarionTypes() {
        try {
            // Get the DensityFunctions class (NMS)
            Class<?> densityFunctionsClass = Class.forName(
                "net.minecraft.world.level.levelgen.DensityFunctions");

            // Get the Registry for density function types
            // In 1.20.1+: DensityFunctions.DENSITY_FUNCTION_TYPE_REGISTRY or similar
            // The actual registry is in BuiltInRegistries
            Class<?> builtInRegistriesClass = Class.forName(
                "net.minecraft.core.registries.BuiltInRegistries");

            // Find the density function type registry field
            Field registryField = null;
            for (Field f : builtInRegistriesClass.getDeclaredFields()) {
                String name = f.getName();
                if (name.contains("DENSITY") || name.contains("density")) {
                    registryField = f;
                    break;
                }
            }

            // Also try DensityFunctions class itself
            if (registryField == null) {
                for (Field f : densityFunctionsClass.getDeclaredFields()) {
                    String name = f.getName();
                    f.setAccessible(true);
                    Object val = f.get(null);
                    if (val != null && val.getClass().getName().contains("Registry")) {
                        registryField = f;
                        break;
                    }
                }
            }

            if (registryField == null) {
                throw new RuntimeException("Could not find DensityFunctions registry field");
            }

            registryField.setAccessible(true);
            Object registry = registryField.get(null);
            if (registry == null) {
                throw new RuntimeException("DensityFunctions registry is null");
            }

            // Step 1: Unfreeze the registry
            unfreezeRegistry(registry);

            // Step 2: Register each custom type
            int count = 0;

            // We need to create KeyDispatchDataCodec instances for each type.
            // Since we can't instantiate NMS classes directly (no paperweight),
            // we create them via reflection on our wrapper classes.
            //
            // Actually — the simpler approach: the Larion datapack references
            // types like "larion:div". Vanilla Minecraft will fail to parse
            // these. But if we create DUMMY registrations that just delegate
            // to vanilla operations, the datapack will load.
            //
            // Even simpler: we can register placeholder codecs that accept
            // any arguments and return a constant. The terrain won't be
            // exactly Larion, but the datapack will LOAD without errors.
            //
            // The REAL solution requires paperweight for proper NMS class access.
            // For now, we just unfreeze and register placeholder types.

            // Register placeholder types for each larion: type
            String[] larionTypes = {
                "div", "sine", "signum", "flat_domain_warp", "x", "z", "sqrt",
                "somewhat_steep"
            };

            Class<?> resourceLocationClass = Class.forName(
                "net.minecraft.resources.ResourceLocation");
            Class<?> keyDispatchDataCodecClass = Class.forName(
                "net.minecraft.util.KeyDispatchDataCodec");

            // Get Registry.register method
            Method registerMethod = null;
            Class<?> registryClass = Class.forName("net.minecraft.core.Registry");
            for (Method m : registryClass.getMethods()) {
                if (m.getName().equals("register") && m.getParameterCount() == 3) {
                    registerMethod = m;
                    break;
                }
            }

            if (registerMethod == null) {
                throw new RuntimeException("Could not find Registry.register method");
            }

            // For each type, register a placeholder codec
            // We use MapCodec.unit() to create a constant codec
            // that returns a dummy DensityFunction
            for (String typeName : larionTypes) {
                try {
                    registerPlaceholderType(registry, resourceLocationClass,
                        registerMethod, "larion", typeName);
                    count++;
                } catch (Exception e) {
                    // Type might already exist or registration format differs
                    // Continue with the next one
                }
            }

            // Step 3: Refreeze
            refreezeRegistry(registry);

            return count;

        } catch (Throwable e) {
            return -1;
        }
    }

    /**
     * Unfreezes a registry by setting the private "frozen" field to false.
     */
    private static void unfreezeRegistry(Object registry) throws Exception {
        // MappedRegistry has a "frozen" boolean field
        Field frozenField = null;
        Class<?> clazz = registry.getClass();
        while (clazz != null && frozenField == null) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getName().equals("frozen") && f.getType() == boolean.class) {
                    frozenField = f;
                    break;
                }
            }
            clazz = clazz.getSuperclass();
        }

        if (frozenField == null) {
            throw new RuntimeException("Could not find 'frozen' field on " + registry.getClass().getName());
        }

        frozenField.setAccessible(true);
        frozenField.setBoolean(registry, false);
    }

    /**
     * Refreezes a registry by setting the private "frozen" field to true.
     */
    private static void refreezeRegistry(Object registry) throws Exception {
        Field frozenField = null;
        Class<?> clazz = registry.getClass();
        while (clazz != null && frozenField == null) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getName().equals("frozen") && f.getType() == boolean.class) {
                    frozenField = f;
                    break;
                }
            }
            clazz = clazz.getSuperclass();
        }

        if (frozenField != null) {
            frozenField.setAccessible(true);
            frozenField.setBoolean(registry, true);
        }
    }

    /**
     * Registers a placeholder density function type.
     * This creates a minimal codec that can parse the JSON without crashing.
     * The actual computation won't be correct (it returns 0), but the
     * datapack will LOAD and the server won't crash.
     *
     * For correct Larion terrain, proper NMS density function implementations
     * are needed (requires paperweight/Horizon for compilation).
     */
    private static void registerPlaceholderType(Object registry,
            Class<?> resourceLocationClass, Method registerMethod,
            String namespace, String path) throws Exception {

        // Create ResourceLocation
        var rlConstructor = resourceLocationClass.getConstructor(String.class, String.class);
        Object rl = rlConstructor.newInstance(namespace, path);

        // Create a placeholder KeyDispatchDataCodec
        // We use a constant-zero density function
        // DensityFunctions.Constant.ZERO or similar
        Class<?> densityFunctionsClass = Class.forName(
            "net.minecraft.world.level.levelgen.DensityFunctions");

        // Try to find the zero constant
        Object zeroConstant = null;
        for (Field f : densityFunctionsClass.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getName().equals("ZERO") || f.getName().equals("zero")) {
                zeroConstant = f.get(null);
                break;
            }
        }

        // Alternative: use yAxisLinearConversion or any SimpleFunction
        if (zeroConstant == null) {
            // Find any static SimpleFunction we can use as template
            for (Field f : densityFunctionsClass.getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(null);
                if (val != null && val.getClass().getSimpleName().contains("Constant")) {
                    zeroConstant = val;
                    break;
                }
            }
        }

        if (zeroConstant == null) {
            throw new RuntimeException("Could not find a constant density function for placeholder");
        }

        // Create a unit codec for this constant
        Class<?> mapCodecClass = Class.forName("com.mojang.serialization.MapCodec");
        Class<?> keyDispatchDataCodecClass = Class.forName(
            "net.minecraft.util.KeyDispatchDataCodec");

        Method unitMethod = null;
        for (Method m : mapCodecClass.getMethods()) {
            if (m.getName().equals("unit") && m.getParameterCount() == 1) {
                unitMethod = m;
                break;
            }
        }

        if (unitMethod == null) {
            throw new RuntimeException("Could not find MapCodec.unit method");
        }

        Object unitCodec = unitMethod.invoke(null, zeroConstant);
        // Wrap in KeyDispatchDataCodec
        var kddcConstructor = keyDispatchDataCodecClass.getConstructor(mapCodecClass);
        Object codec = kddcConstructor.newInstance(unitCodec);

        // Registry.register(registry, resourceLocation, codec)
        registerMethod.invoke(null, registry, rl, codec);
    }
}
