package com.carrotsearch.hppc;

import com.carrotsearch.hppc.hash.MurmurHash3;

public class AnalyzeMurmurHash3
{
    public static void main(String [] args)
    {
        IntArrayList hashChain = new IntArrayList();

        int mask = 0x7fff;
        for (int i = 1; i != 0; i++)
        {
            int hash = MurmurHash3.hash(i) & mask;

            if (hash == 1)
            {
                hashChain.add(i);
                if (hashChain.size() > 1000)
                    break;
            }
        }
    }
}
