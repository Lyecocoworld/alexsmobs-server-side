package com.lyecocoworld.alexsmobsserverside.larion;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * larion:x — Returns the block X coordinate as a density value.
 * larion:z — Returns the block Z coordinate as a density value.
 */
public class CoordFunctions {

    public record XCoord() implements DensityFunction.SimpleFunction {
        public static final KeyDispatchDataCodec<XCoord> CODEC =
            KeyDispatchDataCodec.of(MapCodec.unit(new XCoord()));

        @Override
        public double compute(FunctionContext context) {
            return Math.clamp(context.blockX(), minValue(), maxValue());
        }

        @Override public double minValue() { return -30_000_000; }
        @Override public double maxValue() { return 30_000_000; }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
    }

    public record ZCoord() implements DensityFunction.SimpleFunction {
        public static final KeyDispatchDataCodec<ZCoord> CODEC =
            KeyDispatchDataCodec.of(MapCodec.unit(new ZCoord()));

        @Override
        public double compute(FunctionContext context) {
            return Math.clamp(context.blockZ(), minValue(), maxValue());
        }

        @Override public double minValue() { return -30_000_000; }
        @Override public double maxValue() { return 30_000_000; }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
    }
}
