package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	public enum ParityEnum {
		TOP(0),
		BTM(1),
		ODD(2),
		EVEN(3);

		private final int value;

		private ParityEnum(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static ParityEnum fromInt(int value) {
			switch (value) {
				case 0: return TOP;
				case 1: return BTM;
				case 2: return ODD;
				case 3: return EVEN;
				default: throw new IllegalArgumentException("Invalid value");
			}
		}

		@Override
		public String toString() {
			return switch (value) {
				case 0 -> "Top";
				case 1 -> "Bottom";
				case 2 -> "Odd";
				case 3 -> "Even";
				default -> throw new IllegalStateException("Unexpected value: " + value);
			};
		}
	}

	public ParityEnum element;

	public static final ParityLattice TOP = new ParityLattice(ParityEnum.TOP);
	public static final ParityLattice BTM = new ParityLattice(ParityEnum.BTM);
	public static final ParityLattice ODD = new ParityLattice(ParityEnum.ODD);
	public static final ParityLattice EVEN = new ParityLattice(ParityEnum.EVEN);

	public ParityLattice(int elem) {
		element = ParityEnum.fromInt(elem);
	}

	public ParityLattice(ParityEnum elem) {
		element = elem;
	}

	@Override
	public ParityLattice top() {
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		return ParityLattice.BTM;
	}

	@Override
	public StructuredRepresentation representation() {
		return switch (element) {
			case TOP -> Lattice.topRepresentation();
			case BTM -> Lattice.bottomRepresentation();
			case ODD -> new StringRepresentation("Odd");
			case EVEN -> new StringRepresentation("Even");
            default -> throw new IllegalStateException("Unexpected value: " + element);
        };
	}

	private ParityLattice parityToParityLattice (ParityEnum par) {
		return switch (par) {
			case TOP -> ParityLattice.TOP;
			case BTM -> ParityLattice.BTM;
			case ODD -> ParityLattice.ODD;
			case EVEN -> ParityLattice.EVEN;
		};
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// if (this.element == other.element)
		// 	return parityToParityLattice(element);
		// else if (this.element == ParityEnum.BTM)
		// 	return other;
		// else if (other.element == ParityEnum.BTM)
		// 	return parityToParityLattice(this.element);
		// else if (this.element == ParityEnum.TOP || other.element == ParityEnum.TOP)
		// 	return ParityLattice.TOP;
		// else
		// 	return ParityLattice.TOP;
		return ParityLattice.TOP; // non-Aux function already does all the checks, other than id they are different
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return false; // same as before
	}
}