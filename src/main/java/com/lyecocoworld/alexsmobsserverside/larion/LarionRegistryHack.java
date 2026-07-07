package com.lyecocoworld.alexsmobsserverside.larion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Registers custom Larion density function types by unfreezing the
 * DensityFunctions registry via pure reflection.
 *
 * No NMS imports — works with Paper API only.
 * Registers placeholder codecs (constant 0) so the Larion datapack
 * LOADS without crashing. For real terrain, see LarionDensityFunctions
 * (requires paperweight/Horizon).
 *
 * Works on Paper, Folia, Canvas — vanilla, no external deps.
 */
public class LarionRegistryHack {

    public static int tryRegisterLarionTypes() {
        try {
            // Find BuiltInRegistries
            Class<?> builtInRegistries = Class.forName(
                "net.minecraft.core.registries.BuiltInRegistries");

            // Find the density function type registry field
            Field registryField = null;
            for (Field f : builtInRegistries.getDeclaredFields()) {
                String name = f.getName();
                if (name.contains("DENSITY_FUNCTION") || name.contains("density_function")) {
                    registryField = f;
                    break;
                }
            }
            if (registryField == null) {
                // Try lowercase
                for (Field f : builtInRegistries.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(null);
                    if (val != null) {
                        String className = val.getClass().getName();
                        if (className.contains("Registry") && f.getName().toLowerCase().contains("density")) {
                            registryField = f;
                            break;
                        }
                    }
                }
            }
            if (registryField == null) return -1;

            registryField.setAccessible(true);
            Object registry = registryField.get(null);
            if (registry == null) return -1;

            // Unfreeze
            setFrozen(registry, false);

            // Get Registry.register static method
            Class<?> registryClass = Class.forName("net.minecraft.core.Registry");
            Method registerMethod = null;
            for (Method m : registryClass.getMethods()) {
                if (m.getName().equals("register") && m.getParameterCount() == 3) {
                    registerMethod = m;
                    break;
                }
            }
            if (registerMethod == null) return -1;

            // Get ResourceLocation and MapCodec classes
            Class<?> rlClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Class<?> mapCodecClass = Class.forName("com.mojang.serialization.MapCodec");

            // Get a constant density function to use as placeholder
            Class<?> dfClass = Class.forName("net.minecraft.world.level.levelgen.DensityFunction");
            Class<?> dfTypesClass = Class.forName("net.minecraft.world.level.levelgen.DensityFunctions");

            Object zeroConstant = null;
            for (Field f : dfTypesClass.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getName().equals("ZERO") || f.getName().equals("zero")) {
                    zeroConstant = f.get(null);
                    break;
                }
            }
            // Fallback: find any constant
            if (zeroConstant == null) {
                for (Field f : dfTypesClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(null);
                    if (val != null && val.getClass().getSimpleName().contains("Constant")) {
                        zeroConstant = val;
                        break;
                    }
                }
            }
            if (zeroConstant == null) return -1;

            // Create MapCodec.unit(constant) for each type
            Method unitMethod = null;
            for (Method m : mapCodecClass.getMethods()) {
                if (m.getName().equals("unit") && m.getParameterCount() == 1) {
                    unitMethod = m;
                    break;
                }
            }
            if (unitMethod == null) return -1;

            // Register each Larion type
            String[] types = {"div", "sine", "signum", "sqrt", "flat_domain_warp", "x", "z"};
            int count = 0;

            var rlConstructor = rlClass.getConstructor(String.class, String.class);

            for (String type : types) {
                try {
                    Object rl = rlConstructor.newInstance("larion", type);
                    Object codec = unitMethod.invoke(null, zeroConstant);
                    registerMethod.invoke(null, registry, rl, codec);
                    count++;
                } catch (Exception e) {
                    // Skip this type
                }
            }

            // Refreeze
            setFrozen(registry, true);

            return count;

        } catch (Throwable e) {
            return -1;
        }
    }

    private static void setFrozen(Object registry, boolean frozen) {
        try {
            Class<?> clazz = registry.getClass();
            while (clazz != null) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.getName().equals("frozen") && f.getType() == boolean.class) {
                        f.setAccessible(true);
                        f.setBoolean(registry, frozen);
                        return;
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception ignored) {}
    }
}
