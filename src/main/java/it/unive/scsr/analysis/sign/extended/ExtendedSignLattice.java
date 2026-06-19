package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {
  private int element;

  public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(0);
  public static ExtendedSignLattice NEG = new ExtendedSignLattice(1);
  public static ExtendedSignLattice ZERO = new ExtendedSignLattice(2);
  public static ExtendedSignLattice POS = new ExtendedSignLattice(3);
  public static ExtendedSignLattice ZERONEG = new ExtendedSignLattice(4);
  public static ExtendedSignLattice NOTZERO = new ExtendedSignLattice(5);
  public static ExtendedSignLattice ZEROPOS = new ExtendedSignLattice(6);
  public static ExtendedSignLattice TOP = new ExtendedSignLattice(7);

  private ExtendedSignLattice(int element) {
    this.element = element;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (!(obj instanceof ExtendedSignLattice other))
      return false;

    return this.element == other.element;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(this.element);
  }

  @Override
  public ExtendedSignLattice top() {
    return ExtendedSignLattice.TOP;
  }

  @Override
  public ExtendedSignLattice bottom() {
    return ExtendedSignLattice.BOTTOM;
  }

  @Override
  public StructuredRepresentation representation() {
    if (this == ExtendedSignLattice.BOTTOM)
      return Lattice.bottomRepresentation();

    else if (this == ExtendedSignLattice.NEG)
      return new StringRepresentation("<0");

    else if (this == ExtendedSignLattice.ZERO)
      return new StringRepresentation("=0");

    else if (this == ExtendedSignLattice.POS)
      return new StringRepresentation(">0");

    else if (this == ExtendedSignLattice.ZERONEG)
      return new StringRepresentation("<=0");

    else if (this == ExtendedSignLattice.NOTZERO)
      return new StringRepresentation("!=0");

    else if (this == ExtendedSignLattice.ZEROPOS)
      return new StringRepresentation(">=0");

    return Lattice.topRepresentation();
  }

  @Override
  public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
    if (this == other || other == BOTTOM)
      return this;

    if (this == BOTTOM)
      return other;

    if ((this == ZERONEG || this == ZERO || this == NEG) &&
        (other == ZERONEG || other == ZERO || other == NEG))
      return ZERONEG;

    if ((this == ZEROPOS || this == ZERO || this == POS) &&
        (other == ZEROPOS || other == ZERO || other == POS))
      return ZEROPOS;

    if ((this == NOTZERO || this == NEG || this == POS) &&
        (other == NOTZERO || other == NEG || other == POS))
      return NOTZERO;

    return ExtendedSignLattice.TOP;
  }

  @Override
  public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
    if (this == other || this == BOTTOM || other == TOP)
      return true;

    if (other == BOTTOM)
      return false;

    if (this == NEG && (other == ZERONEG || other == NOTZERO))
      return true;

    if (this == POS && (other == ZEROPOS || other == NOTZERO))
      return true;

    return this == ZERO && (other == ZERONEG || other == ZEROPOS);

  }

  @Override
  public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
    if (this == other || other == TOP)
      return this;

    if (this == TOP)
      return other;

    if ((this == ZERO || this == ZERONEG || this == ZEROPOS) &&
        (other == ZERO || other == ZERONEG || other == ZEROPOS))
      return ZERO;

    if ((this == NEG || this == ZERONEG || this == NOTZERO) &&
        (other == NEG || other == ZERONEG || other == NOTZERO))
      return NEG;

    if ((this == POS || this == ZEROPOS || this == NOTZERO) &&
        (other == POS || other == ZEROPOS || other == NOTZERO))
      return POS;

    return BOTTOM;
  }

  public ExtendedSignLattice negation() {
    if (this == NEG)
      return POS;

    else if (this == POS)
      return NEG;

    else if (this == ZERONEG)
      return ZEROPOS;

    else if (this == ZEROPOS)
      return ZERONEG;

    return this;
  }

  public ExtendedSignLattice addition(ExtendedSignLattice other) {
    if (other == null)
      return TOP;

    if (this == BOTTOM || other == BOTTOM)
      return BOTTOM;

    if (this == TOP || other == TOP)
      return TOP;

    if (this == ZERO)
      return other;

    if (other == ZERO)
      return this;

    if (this == NOTZERO || other == NOTZERO)
      return TOP;

    if (this.isPositive() && other.isPositive()) {
      if (this == ZEROPOS && other == ZEROPOS)
        return ZEROPOS;

      return POS;
    }

    if (this.isNegative() && other.isNegative()) {
      if (this == ZERONEG && other == ZERONEG)
        return ZERONEG;

      return NEG;
    }

    return TOP;
  }

  public ExtendedSignLattice subtraction(ExtendedSignLattice other) {
    if (other == null)
      return TOP;

    return this.addition(other.opposite());
  }

  public ExtendedSignLattice multiplication(ExtendedSignLattice other) {
    if (other == null)
      return TOP;

    if (this == BOTTOM || other == BOTTOM)
      return BOTTOM;

    if (this == ZERO || other == ZERO)
      return ZERO;

    if (this == TOP || other == TOP)
      return TOP;

    if (this == POS)
      return other;

    if (other == POS)
      return this;

    if (this == NEG)
      return other.opposite();

    if (other == NEG)
      return this.opposite();

    if (this == NOTZERO && other == NOTZERO)
      return NOTZERO;

    if (this == ZEROPOS && other == ZEROPOS)
      return ZEROPOS;

    if (this == ZERONEG && other == ZERONEG)
      return ZEROPOS;

    if ((this == ZEROPOS && other == ZERONEG) ||
        (this == ZERONEG && other == ZEROPOS))
      return ZERONEG;

    return TOP;
  }

  public ExtendedSignLattice division(ExtendedSignLattice other) {
    if (other == null)
      return TOP;

    if (this == BOTTOM || other == BOTTOM)
      return BOTTOM;

    if (other == ZERO)
      return BOTTOM;

    if (this == ZERO)
      return ZERO;

    ExtendedSignLattice divisor = other.withoutZero();

    if (divisor == BOTTOM)
      return BOTTOM;

    return this.multiplication(divisor);
  }

  public Satisfiability eq(ExtendedSignLattice other) {
    if (other == null)
      return Satisfiability.UNKNOWN;

    if (this == BOTTOM || other == BOTTOM)
      return Satisfiability.BOTTOM;

    if (!this.canBeEqualTo(other))
      return Satisfiability.NOT_SATISFIED;

    if (this == ZERO && other == ZERO)
      return Satisfiability.SATISFIED;

    return Satisfiability.UNKNOWN;
  }

  public Satisfiability gt(ExtendedSignLattice other) {
    if (other == null)
      return Satisfiability.UNKNOWN;

    if (this == BOTTOM || other == BOTTOM)
      return Satisfiability.BOTTOM;

    if (this == TOP || other == TOP)
      return Satisfiability.UNKNOWN;

    if (this.isDefinitelyGreaterThan(other))
      return Satisfiability.SATISFIED;

    if (this.isDefinitelyLessOrEqualThan(other))
      return Satisfiability.NOT_SATISFIED;

    return Satisfiability.UNKNOWN;
  }

  private boolean canBeEqualTo(ExtendedSignLattice other) {
    if (this.containsNegative() && other.containsNegative())
      return true;

    if (this.containsZero() && other.containsZero())
      return true;

    return this.containsPositive() && other.containsPositive();
  }

  private boolean containsNegative() {
    return this == NEG || this == ZERONEG || this == NOTZERO || this == TOP;
  }

  private boolean containsPositive() {
    return this == POS || this == ZEROPOS || this == NOTZERO || this == TOP;
  }

  private boolean isDefinitelyGreaterThan(ExtendedSignLattice other) {
    if (this == POS && (other == NEG || other == ZERO || other == ZERONEG))
      return true;

    if (this == ZERO && other == NEG)
      return true;

    return this == ZEROPOS && other == NEG;
  }

  private boolean isDefinitelyLessOrEqualThan(ExtendedSignLattice other) {
    return (this == NEG || this == ZERO || this == ZERONEG) &&
        (other == ZERO || other == POS || other == ZEROPOS);
  }

  public boolean isPositive() {
    return this == POS || this == ZEROPOS;
  }

  public boolean isNegative() {
    return this == NEG || this == ZERONEG;
  }

  public boolean areOpposite(ExtendedSignLattice other) {
    return other == this.opposite();
  }

  public ExtendedSignLattice opposite() {
    if (this == NEG)
      return POS;

    if (this == POS)
      return NEG;

    if (this == ZERONEG)
      return ZEROPOS;

    if (this == ZEROPOS)
      return ZERONEG;

    if (this == ZERO)
      return ZERO;

    if (this == NOTZERO)
      return NOTZERO;

    if (this == TOP)
      return TOP;

    return BOTTOM;
  }

  public boolean containsZero() {
    return this == ZERO || this == ZERONEG || this == ZEROPOS || this == TOP;
  }

  public ExtendedSignLattice withoutZero() {
    if (this == ZERO)
      return BOTTOM;

    if (this == ZERONEG)
      return NEG;

    if (this == ZEROPOS)
      return POS;

    if (this == TOP)
      return NOTZERO;

    return this;
  }
}
