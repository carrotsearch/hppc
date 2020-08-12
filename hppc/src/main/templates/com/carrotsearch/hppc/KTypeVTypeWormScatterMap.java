/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

/**
 * Same as {@link KTypeVTypeWormMap} but does not implement per-instance
 * key mixing strategy and uses a simpler (faster) bit distribution function.
 *
 * <p><strong>Note:</strong> read about
 * <a href="{@docRoot}/overview-summary.html#scattervshash">important differences
 * between hash and scatter sets</a>.</p> */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeWormScatterMap<KType, VType> extends KTypeVTypeWormMap<KType, VType>
{
    /**
     * Constructs a {@link KTypeVTypeWormScatterMap} with the default initial capacity.
     */
    public KTypeVTypeWormScatterMap() {
        super();
    }

    /**
     * Constructs a {@link KTypeVTypeWormScatterMap}.
     *
     * @param expectedElements The expected number of elements. Based on it the capacity of the map is calculated.
     */
    public KTypeVTypeWormScatterMap(int expectedElements) {
        super(expectedElements);
    }

    public static <KType, VType> KTypeVTypeWormScatterMap<KType, VType> from(KType[] keys, VType[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
        }
        KTypeVTypeWormScatterMap<KType, VType> map = new KTypeVTypeWormScatterMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /*! #if ($templateonly) !*/
    @Override
    public
    /*! #else protected #end !*/
    int hashKey(KType key) {
        return BitMixer.mixPhi(key);
    }
}
