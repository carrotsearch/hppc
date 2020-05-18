package com.carrotsearch.hppc;

/**
 * Anything that could be accounted for memory usage
 *
 * Partly forked from Lucene
 * tag releases/lucene-solr/8.5.1
 */
public interface Accountable {
    /**
     * Memory usage estimation
     * @return Ram used in bytes
     */
    public long ramUsageBytes();

    /**
     * Return what fraction of internal storage is actually occupied
     * @return A float which is between 0 and 1 inclusive
     */
    public float occupancyRate();
}
