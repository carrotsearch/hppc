package com.carrotsearch.hppc;

/**
 * Anything that could be accounted for memory usage
 *
 * Partly forked from Lucene
 * tag releases/lucene-solr/8.5.1
 */
public interface Accountable {
    /**
     * Allocated memory estimation
     * @return Ram allocated in bytes
     */
    public long ramBytesAllocated();

    /**
     * Bytes that is actually been used
     * @return Ram used in bytes
     */
    public long ramBytesUsed();
}
