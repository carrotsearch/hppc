package com.carrotsearch.tests.benchmarks;

import java.util.Locale;

/**
 * Average with standard deviation.
 */
class Average
{
    final double avg;
    final double stddev;

    public Average(double avg, double stddev)
    {
        this.avg = avg;
        this.stddev = stddev;
    }

    public String toString(double multiplier)
    {
        return String.format(Locale.ENGLISH, "%.2f [+- %.2f]", avg * multiplier,
            stddev * multiplier);
    }

    public static Average from(long [] values)
    {
        long sum = 0;
        long sumSquares = 0;

        for (long l : values)
        {
            sum += l;
            sumSquares += l * l;
        }

        double avg = sum / (double) values.length;
        return new Average(sum / (double) values.length, Math.sqrt(sumSquares
            / (double) values.length - avg * avg));
    }
}