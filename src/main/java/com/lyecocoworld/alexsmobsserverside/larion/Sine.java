package com.lyecocoworld.alexsmobsserverside.larion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * larion:sine — Sine of a density function. Returns -1..1.
 * Implements DensityFunction directly (PureTransformer is package-private).
 */
public record Sine(DensityFunction input) implements DensityFunction {

    public static final MapCodec<Sine> CODEC_MAP = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            DensityFunction.DIRECT_CODEC.fieldOf("argument").forGetter(Sine::input)
        ).apply(instance, Sine::new)
    );

    public static final KeyDispatchDataCodec<? extends DensityFunction> CODEC =
        KeyDispatchDataCodec.of(CODEC_MAP);

    @Override
    public double compute(FunctionContext context) {
        return Math.sin(input.compute(context));
    }

    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        provider.fillAllDirectly(densities, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new Sine(input.mapAll(visitor)));
    }

    @Override public double minValue() { return -1; }
    @Override public double maxValue() { return 1; }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
}
