/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.util; // from org.apache.solr.util rev 555343

/**  A variety of high efficiency bit twiddling routines.
 */
public class BitUtil_popNtzJRE {

  /*** Returns the number of set bits in an array of longs. */
  public static long pop_array(long A[], int wordOffset, int numWords) {
    /*
    * Robert Harley and David Seal's bit counting algorithm, as documented
    * in the revisions of Hacker's Delight
    * http://www.hackersdelight.org/revisions.pdf
    * http://www.hackersdelight.org/HDcode/newCode/pop_arrayHS.cc
    *
    * This function was adapted to Java, and extended to use 64 bit words.
    * if only we had access to wider registers like SSE from java...
    *
    * This function can be transformed to compute the popcount of other functions
    * on bitsets via something like this:
    * sed 's/A\[\([^]]*\)\]/\(A[\1] \& B[\1]\)/g'
    *
    */
    int n = wordOffset+numWords;
    long tot=0, tot8=0;
    long ones=0, twos=0, fours=0;

    int i;
    for (i = wordOffset; i <= n - 8; i+=8) {
      /***  C macro from Hacker's Delight
       #define CSA(h,l, a,b,c) \
       {unsigned u = a ^ b; unsigned v = c; \
       h = (a & b) | (u & v); l = u ^ v;}
       ***/

      long twosA,twosB,foursA,foursB,eights;

      // CSA(twosA, ones, ones, A[i], A[i+1])
      {
        long b=A[i], c=A[i+1];
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, A[i+2], A[i+3])
      {
        long b=A[i+2], c=A[i+3];
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursA, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      //CSA(twosA, ones, ones, A[i+4], A[i+5])
      {
        long b=A[i+4], c=A[i+5];
        long u=ones^b;
        twosA=(ones&b)|(u&c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, A[i+6], A[i+7])
      {
        long b=A[i+6], c=A[i+7];
        long u=ones^b;
        twosB=(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursB, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursB=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }

      //CSA(eights, fours, fours, foursA, foursB)
      {
        long u=fours^foursA;
        eights=(fours&foursA)|(u&foursB);
        fours=u^foursB;
      }
      tot8 += Long.bitCount(eights);
    }

    // handle trailing words in a binary-search manner...
    // derived from the loop above by setting specific elements to 0.
    // the original method in Hackers Delight used a simple for loop:
    //   for (i = i; i < n; i++)      // Add in the last elements
    //  tot = tot + pop(A[i]);

    if (i<=n-4) {
      long twosA, twosB, foursA, eights;
      {
        long b=A[i], c=A[i+1];
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      {
        long b=A[i+2], c=A[i+3];
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=4;
    }

    if (i<=n-2) {
      long b=A[i], c=A[i+1];
      long u=ones ^ b;
      long twosA=(ones & b)|( u & c);
      ones=u^c;

      long foursA=twos&twosA;
      twos=twos^twosA;

      long eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=2;
    }

    if (i<n) {
      tot += Long.bitCount(A[i]);
    }

    tot += (Long.bitCount(fours)<<2)
            + (Long.bitCount(twos)<<1)
            + Long.bitCount(ones)
            + (tot8<<3);

    return tot;
  }

  /** Returns the popcount or cardinality of the two sets after an intersection.
   * Neither array is modified.
   */
  public static long pop_intersect(long A[], long B[], int wordOffset, int numWords) {
    // generated from pop_array via sed 's/A\[\([^]]*\)\]/\(A[\1] \& B[\1]\)/g'
    int n = wordOffset+numWords;
    long tot=0, tot8=0;
    long ones=0, twos=0, fours=0;

    int i;
    for (i = wordOffset; i <= n - 8; i+=8) {
      long twosA,twosB,foursA,foursB,eights;

      // CSA(twosA, ones, ones, (A[i] & B[i]), (A[i+1] & B[i+1]))
      {
        long b=(A[i] & B[i]), c=(A[i+1] & B[i+1]);
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, (A[i+2] & B[i+2]), (A[i+3] & B[i+3]))
      {
        long b=(A[i+2] & B[i+2]), c=(A[i+3] & B[i+3]);
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursA, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      //CSA(twosA, ones, ones, (A[i+4] & B[i+4]), (A[i+5] & B[i+5]))
      {
        long b=(A[i+4] & B[i+4]), c=(A[i+5] & B[i+5]);
        long u=ones^b;
        twosA=(ones&b)|(u&c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, (A[i+6] & B[i+6]), (A[i+7] & B[i+7]))
      {
        long b=(A[i+6] & B[i+6]), c=(A[i+7] & B[i+7]);
        long u=ones^b;
        twosB=(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursB, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursB=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }

      //CSA(eights, fours, fours, foursA, foursB)
      {
        long u=fours^foursA;
        eights=(fours&foursA)|(u&foursB);
        fours=u^foursB;
      }
      tot8 += Long.bitCount(eights);
    }


    if (i<=n-4) {
      long twosA, twosB, foursA, eights;
      {
        long b=(A[i] & B[i]), c=(A[i+1] & B[i+1]);
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      {
        long b=(A[i+2] & B[i+2]), c=(A[i+3] & B[i+3]);
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=4;
    }

    if (i<=n-2) {
      long b=(A[i] & B[i]), c=(A[i+1] & B[i+1]);
      long u=ones ^ b;
      long twosA=(ones & b)|( u & c);
      ones=u^c;

      long foursA=twos&twosA;
      twos=twos^twosA;

      long eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=2;
    }

    if (i<n) {
      tot += Long.bitCount((A[i] & B[i]));
    }

    tot += (Long.bitCount(fours)<<2)
            + (Long.bitCount(twos)<<1)
            + Long.bitCount(ones)
            + (tot8<<3);

    return tot;
  }

  /** Returns the popcount or cardinality of the union of two sets.
    * Neither array is modified.
    */
   public static long pop_union(long A[], long B[], int wordOffset, int numWords) {
     // generated from pop_array via sed 's/A\[\([^]]*\)\]/\(A[\1] \| B[\1]\)/g'
     int n = wordOffset+numWords;
     long tot=0, tot8=0;
     long ones=0, twos=0, fours=0;

     int i;
     for (i = wordOffset; i <= n - 8; i+=8) {
       /***  C macro from Hacker's Delight
        #define CSA(h,l, a,b,c) \
        {unsigned u = a ^ b; unsigned v = c; \
        h = (a & b) | (u & v); l = u ^ v;}
        ***/

       long twosA,twosB,foursA,foursB,eights;

       // CSA(twosA, ones, ones, (A[i] | B[i]), (A[i+1] | B[i+1]))
       {
         long b=(A[i] | B[i]), c=(A[i+1] | B[i+1]);
         long u=ones ^ b;
         twosA=(ones & b)|( u & c);
         ones=u^c;
       }
       // CSA(twosB, ones, ones, (A[i+2] | B[i+2]), (A[i+3] | B[i+3]))
       {
         long b=(A[i+2] | B[i+2]), c=(A[i+3] | B[i+3]);
         long u=ones^b;
         twosB =(ones&b)|(u&c);
         ones=u^c;
       }
       //CSA(foursA, twos, twos, twosA, twosB)
       {
         long u=twos^twosA;
         foursA=(twos&twosA)|(u&twosB);
         twos=u^twosB;
       }
       //CSA(twosA, ones, ones, (A[i+4] | B[i+4]), (A[i+5] | B[i+5]))
       {
         long b=(A[i+4] | B[i+4]), c=(A[i+5] | B[i+5]);
         long u=ones^b;
         twosA=(ones&b)|(u&c);
         ones=u^c;
       }
       // CSA(twosB, ones, ones, (A[i+6] | B[i+6]), (A[i+7] | B[i+7]))
       {
         long b=(A[i+6] | B[i+6]), c=(A[i+7] | B[i+7]);
         long u=ones^b;
         twosB=(ones&b)|(u&c);
         ones=u^c;
       }
       //CSA(foursB, twos, twos, twosA, twosB)
       {
         long u=twos^twosA;
         foursB=(twos&twosA)|(u&twosB);
         twos=u^twosB;
       }

       //CSA(eights, fours, fours, foursA, foursB)
       {
         long u=fours^foursA;
         eights=(fours&foursA)|(u&foursB);
         fours=u^foursB;
       }
       tot8 += Long.bitCount(eights);
     }


     if (i<=n-4) {
       long twosA, twosB, foursA, eights;
       {
         long b=(A[i] | B[i]), c=(A[i+1] | B[i+1]);
         long u=ones ^ b;
         twosA=(ones & b)|( u & c);
         ones=u^c;
       }
       {
         long b=(A[i+2] | B[i+2]), c=(A[i+3] | B[i+3]);
         long u=ones^b;
         twosB =(ones&b)|(u&c);
         ones=u^c;
       }
       {
         long u=twos^twosA;
         foursA=(twos&twosA)|(u&twosB);
         twos=u^twosB;
       }
       eights=fours&foursA;
       fours=fours^foursA;

       tot8 += Long.bitCount(eights);
       i+=4;
     }

     if (i<=n-2) {
       long b=(A[i] | B[i]), c=(A[i+1] | B[i+1]);
       long u=ones ^ b;
       long twosA=(ones & b)|( u & c);
       ones=u^c;

       long foursA=twos&twosA;
       twos=twos^twosA;

       long eights=fours&foursA;
       fours=fours^foursA;

       tot8 += Long.bitCount(eights);
       i+=2;
     }

     if (i<n) {
       tot += Long.bitCount((A[i] | B[i]));
     }

     tot += (Long.bitCount(fours)<<2)
             + (Long.bitCount(twos)<<1)
             + Long.bitCount(ones)
             + (tot8<<3);

     return tot;
   }

  /** Returns the popcount or cardinality of A & ~B
   * Neither array is modified.
   */
  public static long pop_andnot(long A[], long B[], int wordOffset, int numWords) {
    // generated from pop_array via sed 's/A\[\([^]]*\)\]/\(A[\1] \& ~B[\1]\)/g'
    int n = wordOffset+numWords;
    long tot=0, tot8=0;
    long ones=0, twos=0, fours=0;

    int i;
    for (i = wordOffset; i <= n - 8; i+=8) {
      /***  C macro from Hacker's Delight
       #define CSA(h,l, a,b,c) \
       {unsigned u = a ^ b; unsigned v = c; \
       h = (a & b) | (u & v); l = u ^ v;}
       ***/

      long twosA,twosB,foursA,foursB,eights;

      // CSA(twosA, ones, ones, (A[i] & ~B[i]), (A[i+1] & ~B[i+1]))
      {
        long b=(A[i] & ~B[i]), c=(A[i+1] & ~B[i+1]);
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, (A[i+2] & ~B[i+2]), (A[i+3] & ~B[i+3]))
      {
        long b=(A[i+2] & ~B[i+2]), c=(A[i+3] & ~B[i+3]);
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursA, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      //CSA(twosA, ones, ones, (A[i+4] & ~B[i+4]), (A[i+5] & ~B[i+5]))
      {
        long b=(A[i+4] & ~B[i+4]), c=(A[i+5] & ~B[i+5]);
        long u=ones^b;
        twosA=(ones&b)|(u&c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, (A[i+6] & ~B[i+6]), (A[i+7] & ~B[i+7]))
      {
        long b=(A[i+6] & ~B[i+6]), c=(A[i+7] & ~B[i+7]);
        long u=ones^b;
        twosB=(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursB, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursB=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }

      //CSA(eights, fours, fours, foursA, foursB)
      {
        long u=fours^foursA;
        eights=(fours&foursA)|(u&foursB);
        fours=u^foursB;
      }
      tot8 += Long.bitCount(eights);
    }


    if (i<=n-4) {
      long twosA, twosB, foursA, eights;
      {
        long b=(A[i] & ~B[i]), c=(A[i+1] & ~B[i+1]);
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      {
        long b=(A[i+2] & ~B[i+2]), c=(A[i+3] & ~B[i+3]);
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=4;
    }

    if (i<=n-2) {
      long b=(A[i] & ~B[i]), c=(A[i+1] & ~B[i+1]);
      long u=ones ^ b;
      long twosA=(ones & b)|( u & c);
      ones=u^c;

      long foursA=twos&twosA;
      twos=twos^twosA;

      long eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=2;
    }

    if (i<n) {
      tot += Long.bitCount((A[i] & ~B[i]));
    }

    tot += (Long.bitCount(fours)<<2)
            + (Long.bitCount(twos)<<1)
            + Long.bitCount(ones)
            + (tot8<<3);

    return tot;
  }

  public static long pop_xor(long A[], long B[], int wordOffset, int numWords) {
    int n = wordOffset+numWords;
    long tot=0, tot8=0;
    long ones=0, twos=0, fours=0;

    int i;
    for (i = wordOffset; i <= n - 8; i+=8) {
      /***  C macro from Hacker's Delight
       #define CSA(h,l, a,b,c) \
       {unsigned u = a ^ b; unsigned v = c; \
       h = (a & b) | (u & v); l = u ^ v;}
       ***/

      long twosA,twosB,foursA,foursB,eights;

      // CSA(twosA, ones, ones, (A[i] ^ B[i]), (A[i+1] ^ B[i+1]))
      {
        long b=(A[i] ^ B[i]), c=(A[i+1] ^ B[i+1]);
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, (A[i+2] ^ B[i+2]), (A[i+3] ^ B[i+3]))
      {
        long b=(A[i+2] ^ B[i+2]), c=(A[i+3] ^ B[i+3]);
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursA, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      //CSA(twosA, ones, ones, (A[i+4] ^ B[i+4]), (A[i+5] ^ B[i+5]))
      {
        long b=(A[i+4] ^ B[i+4]), c=(A[i+5] ^ B[i+5]);
        long u=ones^b;
        twosA=(ones&b)|(u&c);
        ones=u^c;
      }
      // CSA(twosB, ones, ones, (A[i+6] ^ B[i+6]), (A[i+7] ^ B[i+7]))
      {
        long b=(A[i+6] ^ B[i+6]), c=(A[i+7] ^ B[i+7]);
        long u=ones^b;
        twosB=(ones&b)|(u&c);
        ones=u^c;
      }
      //CSA(foursB, twos, twos, twosA, twosB)
      {
        long u=twos^twosA;
        foursB=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }

      //CSA(eights, fours, fours, foursA, foursB)
      {
        long u=fours^foursA;
        eights=(fours&foursA)|(u&foursB);
        fours=u^foursB;
      }
      tot8 += Long.bitCount(eights);
    }


    if (i<=n-4) {
      long twosA, twosB, foursA, eights;
      {
        long b=(A[i] ^ B[i]), c=(A[i+1] ^ B[i+1]);
        long u=ones ^ b;
        twosA=(ones & b)|( u & c);
        ones=u^c;
      }
      {
        long b=(A[i+2] ^ B[i+2]), c=(A[i+3] ^ B[i+3]);
        long u=ones^b;
        twosB =(ones&b)|(u&c);
        ones=u^c;
      }
      {
        long u=twos^twosA;
        foursA=(twos&twosA)|(u&twosB);
        twos=u^twosB;
      }
      eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=4;
    }

    if (i<=n-2) {
      long b=(A[i] ^ B[i]), c=(A[i+1] ^ B[i+1]);
      long u=ones ^ b;
      long twosA=(ones & b)|( u & c);
      ones=u^c;

      long foursA=twos&twosA;
      twos=twos^twosA;

      long eights=fours&foursA;
      fours=fours^foursA;

      tot8 += Long.bitCount(eights);
      i+=2;
    }

    if (i<n) {
      tot += Long.bitCount((A[i] ^ B[i]));
    }

    tot += (Long.bitCount(fours)<<2)
            + (Long.bitCount(twos)<<1)
            + Long.bitCount(ones)
            + (tot8<<3);

    return tot;
  }

  /* python code to generate ntzTable
  def ntz(val):
    if val==0: return 8
    i=0
    while (val&0x01)==0:
      i = i+1
      val >>= 1
    return i
  print ','.join([ str(ntz(i)) for i in range(256) ])
  ***/
  /** table of number of trailing zeros in a byte */
  public static final byte[] ntzTable = {8,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,6,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,7,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,6,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0};


  /** Returns number of trailing zeros in a 64 bit long value. */
  public static int ntz(long val) {
    return Long.numberOfTrailingZeros(val);
  }

  /** Returns number of trailing zeros in a 32 bit int value. */
  public static int ntz(int val) {
    return Integer.numberOfTrailingZeros(val);
  }

  /** returns true if v is a power of two or zero*/
  public static boolean isPowerOfTwo(int v) {
    return ((v & (v-1)) == 0);
  }

  /** returns true if v is a power of two or zero*/
  public static boolean isPowerOfTwo(long v) {
    return ((v & (v-1)) == 0);
  }

  /** returns the next highest power of two, or the current value if it's already a power of two or zero*/
  public static int nextHighestPowerOfTwo(int v) {
    v--;
    v |= v >> 1;
    v |= v >> 2;
    v |= v >> 4;
    v |= v >> 8;
    v |= v >> 16;
    v++;
    return v;
  }

  /** returns the next highest power of two, or the current value if it's already a power of two or zero*/
   public static long nextHighestPowerOfTwo(long v) {
    v--;
    v |= v >> 1;
    v |= v >> 2;
    v |= v >> 4;
    v |= v >> 8;
    v |= v >> 16;
    v |= v >> 32;
    v++;
    return v;
  }

}
