package com.lyecocoworld.alexsmobsserverside.larion;

import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunctions;

/**
 * Registers all custom Larion density function types into the vanilla
 * DensityFunctions registry using direct NMS access (paperweight).
 *
 * Runs in onLoad() — before any world loads, right after Bootstrap.bootStrap()
 * has frozen the registry. We unfreeze, register, refreeze.
 *
 * This produces REAL Larion terrain (not placeholders).
 * Works on Paper, Folia, Canvas — no external Mixin loader needed.
 */
public class LarionRegistryHack {

    /**
     * Registers all 8 Larion density function types.
     *
     * @return number of types registered
     */
    public static int tryRegisterLarionTypes() {
        // The density function types registry stores MapCodec<? extends DensityFunction>
        var registry = net.minecraft.core.registries.BuiltInRegistries.DENSITY_FUNCTION_TYPE;

        // Unfreeze via reflection (the field is private and final)
        unfreeze(registry);

        int count = 0;
        count += register(registry, "larion", "div", Division.CODEC_MAP);
        count += register(registry, "larion", "sine", Sine.CODEC_MAP);
        count += register(registry, "larion", "signum", Signum.CODEC_MAP);
        count += register(registry, "larion", "sqrt", Sqrt.CODEC_MAP);
        count += register(registry, "larion", "flat_domain_warp", FlatDomainWarp.CODEC_MAP);
        count += register(registry, "larion", "x", MapCodec.unit(new CoordFunctions.XCoord()));
        count += register(registry, "larion", "z", MapCodec.unit(new CoordFunctions.ZCoord()));

        // Refreeze
        freeze(registry);

        return count;
    }

    private static int register(
            net.minecraft.core.Registry<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.levelgen.DensityFunction>> registry,
            String namespace, String path,
            com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.levelgen.DensityFunction> codec) {
        var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(namespace, path);
        net.minecraft.core.Registry.register(registry, id, codec);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    // Registry freeze manipulation via reflection
    // ═══════════════════════════════════════════════════════════

    private static void unfreeze(Object registry) {
        try {
            var field = findField(registry.getClass(), "frozen");
            if (field != null) {
                field.setBoolean(registry, false);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to unfreeze density function registry", e);
        }
    }

    private static void freeze(Object registry) {
        try {
            var field = findField(registry.getClass(), "frozen");
            if (field != null) {
                field.setBoolean(registry, true);
            }
        } catch (Exception e) {
            // Non-fatal — the registry will be used read-only from now on
        }
    }

    private static java.lang.reflect.Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            for (var f : clazz.getDeclaredFields()) {
                if (f.getName().equals(name)) {
                    f.setAccessible(true);
                    return f;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
