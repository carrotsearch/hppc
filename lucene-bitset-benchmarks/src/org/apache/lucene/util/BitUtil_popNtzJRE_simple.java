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
public class BitUtil_popNtzJRE_simple {

  /*** Returns the number of set bits in an array of longs. */
  public static long pop_array(long A[], int wordOffset, int numWords) {
    final int n = wordOffset + numWords;
    long tot = 0;
    for (int i = wordOffset; i < n; i++)
        tot += Long.bitCount(A[i]);
    return tot;
  }

  /** Returns the popcount or cardinality of the two sets after an intersection.
   * Neither array is modified.
   */
  public static long pop_intersect(long A[], long B[], int wordOffset, int numWords) {
    final int n = wordOffset + numWords;
    long tot = 0;
    for (int i = wordOffset; i < n; i++)
        tot += Long.bitCount(A[i] & B[i]);
    return tot;
  }

  /** Returns the popcount or cardinality of the union of two sets.
    * Neither array is modified.
    */
   public static long pop_union(long A[], long B[], int wordOffset, int numWords) {
     final int n = wordOffset + numWords;
     long tot = 0;
     for (int i = wordOffset; i < n; i++)
         tot += Long.bitCount(A[i] | B[i]);
     return tot;
   }

  /** Returns the popcount or cardinality of A & ~B
   * Neither array is modified.
   */
  public static long pop_andnot(long A[], long B[], int wordOffset, int numWords) {
    final int n = wordOffset + numWords;
    long tot = 0;
    for (int i = wordOffset; i < n; i++)
        tot += Long.bitCount(A[i] & ~B[i]);
    return tot;
  }

  public static long pop_xor(long A[], long B[], int wordOffset, int numWords) {
    final int n = wordOffset + numWords;
    long tot = 0;
    for (int i = wordOffset; i < n; i++)
        tot += Long.bitCount(A[i] ^ B[i]);
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
