package it.unive.scsr.analysis.nullness;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class NullnessLattice implements BaseLattice<NullnessLattice> {

  public static final NullnessLattice BOTTOM = new NullnessLattice(0);
  public static final NullnessLattice NULL = new NullnessLattice(1);
  public static final NullnessLattice NOT_NULL = new NullnessLattice(2);
  public static final NullnessLattice TOP = new NullnessLattice(3);

  private final Integer state;

  private NullnessLattice(Integer state) {
    this.state = state;
  }

  @Override
  public NullnessLattice lubAux(NullnessLattice other) throws SemanticException {
    if (this.equals(BOTTOM))
      return other;
    if (other.equals(BOTTOM) || this.equals(other))
      return this;
    return TOP;
  }

  @Override
  public boolean lessOrEqualAux(NullnessLattice other) throws SemanticException {
    return this.equals(BOTTOM) || other.equals(TOP) || this.equals(other);
  }

  @Override
  public NullnessLattice top() {
    return TOP;
  }

  @Override
  public NullnessLattice bottom() {
    return BOTTOM;
  }

  @Override
  public StructuredRepresentation representation() {
    if (this.equals(TOP))
      return Lattice.topRepresentation();
    if (this.equals(BOTTOM))
      return Lattice.bottomRepresentation();
    return new StringRepresentation(this.equals(NULL) ? "NULL" : "NOT_NULL");
  }

  public boolean isDefinitelyNull() {
    return this.equals(NULL);
  }

  public boolean isMaybeNull() {
    return this.equals(TOP);
  }

  public boolean isDefinitelyNotNull() {
    return this.equals(NOT_NULL);
  }

  public boolean mayBeNull() {
    return this.equals(NULL) || this.equals(TOP);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof NullnessLattice other))
      return false;
    return Objects.equals(state, other.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(state);
  }
}