package com.lyecocoworld.alexsmobsserverside.larion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * larion:div — Division of two density functions.
 * Source: Larion by Badgerson (Apache-2.0) — com.badgerson.larion.density_function_types.Division
 * Original credit: https://github.com/klinbee/More-Density-Functions
 */
public record Division(DensityFunction argument1, DensityFunction argument2) implements DensityFunction {

    public static final MapCodec<Division> CODEC_MAP = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            DensityFunction.DIRECT_CODEC.fieldOf("argument1").forGetter(Division::argument1),
            DensityFunction.DIRECT_CODEC.fieldOf("argument2").forGetter(Division::argument2)
        ).apply(instance, Division::new)
    );

    public static final KeyDispatchDataCodec<Division> CODEC = KeyDispatchDataCodec.of(CODEC_MAP);

    @Override
    public double compute(FunctionContext context) {
        double divisor = argument2.compute(context);
        if (divisor == 0) return 0.0;
        return argument1.compute(context) / divisor;
    }

    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        provider.fillAllDirectly(densities, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new Division(argument1.mapAll(visitor), argument2.mapAll(visitor)));
    }

    @Override
    public double minValue() {
        return argument2.minValue() == 0 ? 0 : argument1.minValue() / argument2.minValue();
    }

    @Override
    public double maxValue() {
        return argument2.maxValue() == 0 ? 0 : argument1.maxValue() / argument2.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
