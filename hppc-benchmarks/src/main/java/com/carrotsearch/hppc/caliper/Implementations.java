package com.carrotsearch.hppc.caliper;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * 
 */
public enum Implementations
{
    HPPC
    {
        public MapImplementation<?> getInstance()
        {
            return new HppcMap(IntIntOpenHashMap.newInstance());
        }
    },

    HPPC_NOPERTURBS
    {
        public MapImplementation<?> getInstance()
        {
            return new HppcMap(IntIntOpenHashMap.newInstanceWithoutPerturbations());
        }
    },

    FASTUTIL
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new FastUtilMap();
        }
    },

    JAVA
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new JavaMap();
        }
    },

    TROVE
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new TroveMap();
        }
    },

    MAHOUT
    {
        @Override
        public MapImplementation<?> getInstance()
        {
            return new MahoutMap();
        }
    };

    public abstract MapImplementation<?> getInstance();
}