package com.lyecocoworld.alexsmobsserverside.larion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * larion:sqrt — Square root of a density function.
 * Source credit: https://github.com/klinbee/More-Density-Functions
 */
public record Sqrt(DensityFunction input) implements DensityFunction {

    public static final MapCodec<Sqrt> CODEC_MAP = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            DensityFunction.DIRECT_CODEC.fieldOf("argument").forGetter(Sqrt::input)
        ).apply(instance, Sqrt::new)
    );

    public static final KeyDispatchDataCodec<Sqrt> CODEC = KeyDispatchDataCodec.of(CODEC_MAP);

    @Override
    public double compute(FunctionContext context) {
        return Math.sqrt(input.compute(context));
    }

    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        provider.fillAllDirectly(densities, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new Sqrt(input.mapAll(visitor)));
    }

    @Override
    public double minValue() { return Math.sqrt(input.minValue()); }

    @Override
    public double maxValue() { return Math.sqrt(input.maxValue()); }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
}
