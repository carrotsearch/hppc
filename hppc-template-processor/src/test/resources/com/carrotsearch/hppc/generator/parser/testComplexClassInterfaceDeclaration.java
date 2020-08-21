public class ComplexSignatures {
  public void dottedTypeCasts() {
    Object foo = (A<Integer>.B.C<Double>) null;
  }
}