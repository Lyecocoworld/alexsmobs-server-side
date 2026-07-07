package com.lyecocoworld.alexsmobsserverside.larion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * larion:flat_domain_warp — Warps the sampling coordinates (X,Z only).
 * Computes: input.compute(blockX + warpX, blockY, blockZ + warpZ)
 */
public record FlatDomainWarp(DensityFunction input, DensityFunction warpX, DensityFunction warpZ)
        implements DensityFunction {

    public static final MapCodec<FlatDomainWarp> CODEC_MAP = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(FlatDomainWarp::input),
            DensityFunction.DIRECT_CODEC.fieldOf("warp_x").forGetter(FlatDomainWarp::warpX),
            DensityFunction.DIRECT_CODEC.fieldOf("warp_z").forGetter(FlatDomainWarp::warpZ)
        ).apply(instance, FlatDomainWarp::new)
    );

    public static final KeyDispatchDataCodec<FlatDomainWarp> CODEC = KeyDispatchDataCodec.of(CODEC_MAP);

    @Override
    public double compute(FunctionContext context) {
        int shiftedX = context.blockX() + (int) warpX.compute(context);
        int shiftedZ = context.blockZ() + (int) warpZ.compute(context);
        return input.compute(new ShiftedContext(context, shiftedX, shiftedZ));
    }

    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        provider.fillAllDirectly(densities, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new FlatDomainWarp(
            input.mapAll(visitor), warpX.mapAll(visitor), warpZ.mapAll(visitor)));
    }

    @Override
    public double minValue() { return input.minValue(); }

    @Override
    public double maxValue() { return input.maxValue(); }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }

    /** Wraps a context with shifted blockX/blockZ. */
    private record ShiftedContext(FunctionContext parent, int x, int z) implements FunctionContext {
        @Override public int blockX() { return x; }
        @Override public int blockY() { return parent.blockY(); }
        @Override public int blockZ() { return z; }
    }
}
