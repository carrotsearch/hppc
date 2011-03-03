package com.carrotsearch.hppc.caliper;

/**
 * 
 */
public enum Implementations
{
    HPPC
    {
        public MapImplementation<?> getInstance()
        {
            return new HppcMap();
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