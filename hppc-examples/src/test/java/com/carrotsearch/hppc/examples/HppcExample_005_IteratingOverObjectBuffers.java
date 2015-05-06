package com.carrotsearch.hppc.examples;

import static com.carrotsearch.hppc.examples.Helpers.*;

import org.junit.Test;

import com.carrotsearch.hppc.ObjectArrayList;

/**
 * Examples of how to iterate over HPPC lists with object (generic) buffers.
 */
public class HppcExample_005_IteratingOverObjectBuffers {
  public final ObjectArrayList<Integer> list = ObjectArrayList.from(1, 1, 2, 3, 5);

  /**
   * Compared to {@link HppcExample_001_IteratingOverLists#directBufferLoop()}, 
   * a direct buffer access for Object types is a bit trickier because the
   * buffer is actually Object[], not an array of the declared generic type.
   * 
   * A manual cast to the generic type (for every element) is required.
   */
  @Test
  public void directBufferLoop() throws Exception {
    final Object[] buffer = list.buffer;
    final int size = list.size();
    for (int i = 0; i < size; i++) {
      printfln("list[%d] = %d", i, buffer[i]);
    }
  }
}
