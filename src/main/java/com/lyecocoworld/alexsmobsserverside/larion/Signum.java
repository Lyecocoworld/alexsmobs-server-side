package com.lyecocoworld.alexsmobsserverside.larion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * larion:signum — Sign function (-1, 0, or 1).
 * Implements DensityFunction directly (PureTransformer is package-private).
 */
public record Signum(DensityFunction input) implements DensityFunction {

    public static final MapCodec<Signum> CODEC_MAP = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            DensityFunction.DIRECT_CODEC.fieldOf("argument").forGetter(Signum::input)
        ).apply(instance, Signum::new)
    );

    public static final KeyDispatchDataCodec<? extends DensityFunction> CODEC =
        KeyDispatchDataCodec.of(CODEC_MAP);

    @Override
    public double compute(FunctionContext context) {
        return Math.signum(input.compute(context));
    }

    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        provider.fillAllDirectly(densities, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new Signum(input.mapAll(visitor)));
    }

    @Override public double minValue() { return -1; }
    @Override public double maxValue() { return 1; }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
}
