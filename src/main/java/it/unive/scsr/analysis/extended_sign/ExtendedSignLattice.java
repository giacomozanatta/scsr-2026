package it.unive.scsr.analysis.extended_sign;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;


/**
 * Lattice for the extended sign abstract domain.
 *
 * The idea is to represent each element of the lattice as a <em>subset</em> 
 * 	of the three atomic signs {NEG, ZERO, POS},
 * 	encoded as a 3-bit integer where bit 0 = NEG, bit 1 = ZERO, bit 2 = POS.
 * 	This powerset construction yields eight elements:
 *
 * <pre>
 *   element  bits  meaning
 *   BOTTOM    000  unreachable / no concrete value
 *   NEG       001  strictly negative  (&lt;0)
 *   ZERO      010  exactly zero       (=0)
 *   NON_POS   011  negative or zero   (&lt;=0)
 *   POS       100  strictly positive  (&gt;0)
 *   NON_ZERO  101  non-zero           (!=0)
 *   NON_NEG   110  zero or positive   (&gt;=0)
 *   TOP       111  any integer
 * </pre>
 *
 * The Hasse diagram of the lattice (arrows point upward):
 *
 * <pre>
 *               TOP (111)
 *             /    |    \
 *           /      |      \
 *         /        |        \
 *  NON_POS(011) NON_ZERO(101) NON_NEG(110)
 *  |      \    /    \    /      |
 *  |       \  /      \  /       |
 *  |        X          X        |
 *  |       /  \      /  \       |
 *  |      /    \    /    \      |
 *  NEG(001)   ZERO(010)   POS(100)
 *        \       |       /
 *         \      |      /
 *          BOTTOM(000)
 * </pre>
 *
 * Because the encoding is a powerset, all lattice operations reduce to
 * bitwise arithmetic:
 * <ul>
 *   <li>{@code lub(a, b) = a | b}</li>
 *   <li>{@code glb(a, b) = a &amp; b}</li>
 *   <li>{@code a ⊑ b  <==>  (a &amp; b) == a}</li>
 * </ul>
 * 
 * @author Gianmaria Pizzo 872966
 */
public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {
	private final int element;

	public static final ExtendedSignLattice BOTTOM = new ExtendedSignLattice(0);
	public static final ExtendedSignLattice NEG = new ExtendedSignLattice(1);
	public static final ExtendedSignLattice ZERO = new ExtendedSignLattice(2);
	public static final ExtendedSignLattice NON_POS = new ExtendedSignLattice(3);
	public static final ExtendedSignLattice POS = new ExtendedSignLattice(4);
	public static final ExtendedSignLattice NON_ZERO= new ExtendedSignLattice(5);
	public static final ExtendedSignLattice NON_NEG = new ExtendedSignLattice(6);
	public static final ExtendedSignLattice TOP = new ExtendedSignLattice(7);

	private ExtendedSignLattice(int element) {
		this.element = element;
	}

	/**
	 * Returns the 3-bit integer encoding of this element.
	 * 
	 * @note Bit 0 = NEG, bit 1 = ZERO, bit 2 = POS.
	 *
	 * @return integer in [0, 7]
	 */
	public int getElement() {
		return this.element;
	}

	@Override
	public ExtendedSignLattice top() {
		return TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return BOTTOM;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getElement());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}

		if (obj == null || getClass() != obj.getClass()){
			return false;
		}

		ExtendedSignLattice other = (ExtendedSignLattice) obj;

		// Two elements are equal iff their bitmask encodings are identical
		return getElement() == other.getElement();
	}

	@Override
	public StructuredRepresentation representation() {
		switch (getElement()) {
			case 0: return Lattice.bottomRepresentation();
			case 1: return new StringRepresentation("<0");
			case 2: return new StringRepresentation("0");
			case 3: return new StringRepresentation("<=0");
			case 4: return new StringRepresentation(">0");
			case 5: return new StringRepresentation("!=0");
			case 6: return new StringRepresentation(">=0");
			case 7: return Lattice.topRepresentation();
			default: return new StringRepresentation("?");
		}
	}

	/**
     * Converts an integer representation back into an ExtendedSignLattice element. 
     * 
     * @note This method is used to create new lattice elements based on the result of bitwise operations.
     * 
     * @param element the integer representation of the lattice element, where:
     *  - 0: BOTTOM (000)
     *  - 1: NEG (001)
     *  - 2: ZERO (010)
     *  - 3: NON_POS (011)
     *  - 4: POS (100)
     *  - 5: NON_ZERO (101)
     *  - 6: NON_NEG (110)
     *  - 7: TOP (111)
     * 
     * @return the corresponding ExtendedSignLattice element
     */
	public static ExtendedSignLattice fromElement(int e) {
		switch (e) {
			case 0: return BOTTOM;
			case 1: return NEG;
			case 2: return ZERO;
			case 3: return NON_POS;
			case 4: return POS;
			case 5: return NON_ZERO;
			case 6: return NON_NEG;
			case 7: return TOP;
			default: return TOP;
		}
	}

	/** 
     * Computes the least upper bound (the union) of this element and another element.
     * 
     * @brief The function, first, extracts the integer representations of both elements, 
     *  which encode the presence of NEG, ZERO, and POS using bits. Then, it performs a 
     *  bitwise OR operation on these integer representations to combine the information
     *  from both elements. Finally, it converts the resulting integer back into an 
     *  ExtendedSignLattice element using the fromElement method.
     * 
	 * @example If this element is NEG (001) and the other element is POS (100), 
	 * 	the bitwise OR will yield 101, which corresponds to NON_ZERO. 
	 * 	Similarly, if this element is ZERO (010) and the other element is POS (100), 
	 * 	the bitwise OR will yield 110, which corresponds to NON_NEG.
	 * 
     * @param other the other element
     * 
     * @return the least upper bound
     * 
     * @throws SemanticException if the operation cannot be performed
     */
	@Override
	public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
		return fromElement(this.getElement() | other.getElement());
	}

	/**
     * Computes the greatest lower bound (intersection) of this element and another element.
     * 
     * @brief The function, first, extracts the integer representations of both elements,
     * which encode the presence of NEG, ZERO, and POS using bits. Then, it performs a
     * bitwise AND operation on these integer representations to find the common information
     * between the two elements. Finally, it converts the resulting integer back into an
     * ExtendedSignLattice element using the fromElement method.
     * 
	 * @example If this element is NON_NEG (110) and the other element is NON_POS (011),
	 * 	the bitwise AND will yield 010, which corresponds to ZERO. 
	 * 	Similarly, if this element is NON_ZERO (101) and the other element is NON_NEG (110),
	 * 	the bitwise AND will yield 100, which corresponds to POS.
     * 
     * @param other the other element
     * 
     * @return the greatest lower bound
     * 
     * @throws SemanticException if the operation cannot be performed
     */
	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
		return fromElement(this.getElement() & other.getElement());
	}

	/** 
     * Computes whether this element is less or equal to another element.
     * 
     * @brief The function, first, extracts the integer representations of both elements, 
     *  which encode the presence of NEG, ZERO, and POS using bits. Then, it performs a 
     *  bitwise AND operation on these integer representations to check if all bits set 
     *  in this element are also set in the other element. Finally, it converts the 
     *  resulting integer back into an ExtendedSignLattice element using the fromElement 
     *  method.
     * 
     * @param other the other element
     * 
     * @return the least upper bound
     * 
     * @throws SemanticException if the operation cannot be performed
     */
	@Override
	public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
		return (this.getElement() & other.getElement()) == this.getElement();
	}

	/** 
	 * Returns whether this element's set includes strictly negative values. 
	 * 
	 * @note The presence of strictly negative values is indicated by bit 0 
	 * 	of the element's integer encoding. It is tested by performing a 
	 * 	bitwise AND with 1 (which isolates bit 0) and checking if the 
	 * 	result is non-zero.
	 * 
	 * @return {@code true} if this element's set includes strictly negative values, 
	 * 	{@code false} otherwise.
	*/
	public boolean hasNegative() { 
		return (getElement() & 1) != 0; 
	}

	/** 
	 * Returns whether this element's set includes zero.
	 * 
	 * @note The presence of zero is indicated by bit 1 of the element's integer encoding.
	 * 	It is tested by performing a bitwise AND with 2 (which isolates bit 1) and 
	 * 	checking if the result is non-zero.
	 * 
	 * @return {@code true} if this element's set includes zero, {@code false} otherwise.
	*/
	public boolean hasZero() {
		return (getElement() & 2) != 0; 
	}

	/** 
	 * Returns whether this element's set includes strictly positive values.
	 * 
	 * @note The presence of strictly positive values is indicated by bit 2 
	 * 	of the element's integer encoding. It is tested by performing a bitwise
	 * 	AND with 4 (which isolates bit 2) and checking if the result is non-zero.
	 * 
	 * @return {@code true} if this element's set includes strictly positive values, 
	 * 	{@code false} otherwise.
	 */
	public boolean hasPositive() { 
		return (getElement() & 4) != 0; 
	}

	/**
	 * Returns the satisfiability of {@code this == other}.
	 *
	 * @param other the right-hand side
	 * 
	 * @return {@link Satisfiability} of the equality
	 */
	public Satisfiability eq(ExtendedSignLattice other) {
		// If either element is BOTTOM, the equality is unreachable
		if (this.isBottom() || other.isBottom()){
			return Satisfiability.BOTTOM;
		}

		// If either element is TOP, the equality may or may not hold, so we return UNKNOWN
		if (this.isTop() || other.isTop()){
			return Satisfiability.UNKNOWN;
		}

		// If the bitwise AND of the two elements is zero, they represent disjoint sets of signs
		if ((this.getElement() & other.getElement()) == 0){
			return Satisfiability.NOT_SATISFIED;
		}

		// If both elements are exactly ZERO, the equality is always satisfied
		if (this.getElement() == 2 && other.getElement() == 2){
			return Satisfiability.SATISFIED;
		}

		// For all other cases, sets overlap but may or may not contain the 
		// same concrete value. Therefore, equality is only possible, but 
		// not guaranteed :(
		return Satisfiability.UNKNOWN;
	}

	/**
	 * Returns the satisfiability of {@code this > other}.
	 *
	 * @param other the right-hand side
	 * 
	 * @return {@link Satisfiability} of the strict inequality
	 */
	public Satisfiability gt(ExtendedSignLattice other) {
		// If either element is BOTTOM, the inequality is unreachable
		if (this.isBottom() || other.isBottom()){
			return Satisfiability.BOTTOM;
		}

		// If either element is TOP, the inequality may or may not hold, so we return UNKNOWN
		if (this.isTop() || other.isTop()){
			return Satisfiability.UNKNOWN;
		}

		// Get the presence of NEG, ZERO, and POS in both elements
		boolean hasNegThis = this.hasNegative();
		boolean hasZeroThis = this.hasZero();
		boolean hasPosThis = this.hasPositive();
		boolean hasNegOther = other.hasNegative();
		boolean hasZeroOther = other.hasZero();
		boolean hasPosOther = other.hasPositive();

		// If min(this) >= 0 and max(other) <= 0, and not (both have 0)
		if (!hasNegThis && !hasPosOther && !(hasZeroThis && hasZeroOther)){
			// this is always > other
			return Satisfiability.SATISFIED;
		}

		// If max(this) <= 0 and min(other) >= 0
		if (!hasPosThis && !hasNegOther){
			// this is never > other
			return Satisfiability.NOT_SATISFIED;
		}

		return Satisfiability.UNKNOWN;
	}

	/**
	 * Returns the abstract result of the negation of this lattice element.
	 *
	 * @brief Swaps the NEG and POS bits, while leaving the ZERO bit unchanged.
	 * 
	 * @return abstract value of {@code -this}
	 */
	public ExtendedSignLattice negation() {
		// If this is BOTTOM or TOP, negation does not change anything
		if (this.isBottom()) {
			return BOTTOM;
		}

		if (this.isTop()) {
			return TOP;
		}

		// Auxiliary var
		int r = 0;

		// If NEG bit (bit 0) is set
		if (hasNegative()){
			// Set POS bit in result (bit 2)
			r |= 4;
		}

		// If ZERO bit (bit 1) is set
		if (hasZero()) {
			// ZERO remains ZERO if newgated
			r |= 2;
		}

		// If POS bit (bit 2) is set
		if (hasPositive()) {
			// Set NEG bit in result (bit 0)
			r |= 1;
		}

		// Return the element corresponding to the new bitmask
		return fromElement(r);
	}

	/**
	 * Returns the abstract result of the addition operation between this and another lattice element.
	 *
	 * @param other addend
	 * 
	 * @return abstract value of {@code this + other}
	 */
	public ExtendedSignLattice addition(ExtendedSignLattice other) {
		// If either element is BOTTOM, the sum is unreachable
		if (this.isBottom() || other.isBottom()) {
			return BOTTOM;
		}

		// If either element is TOP, the sum can be any sign, so we return TOP
		if (this.isTop() || other.isTop()) {
			return TOP;
		}

		boolean hasNegThis = this.hasNegative();
        boolean hasZeroThis = this.hasZero();
        boolean hasPosThis = this.hasPositive();
        boolean hasNegOther = other.hasNegative();
        boolean hasZeroOther = other.hasZero();
        boolean hasPosOther = other.hasPositive();

		// Auxiliary var
		int r = 0;

		// If we sum two NEGs, or a NEG and a ZERO, or a ZERO and a NEG
		// the result can be NEG
		if (
			(hasNegThis && hasNegOther) || 
			(hasNegThis && hasZeroOther) || 
			(hasZeroThis && hasNegOther	)
		) {
			// Set NEG bit in result (bit 0)
			r |= 1;
		}

		// If we sum two ZEROs
		// the result is ZERO
		if (hasZeroThis && hasZeroOther) {
			// Set ZERO bit in result (bit 1)
			r |= 2;
		}

		// If we sum two POSs, or a POS and a ZERO, or a ZERO and a POS
		// the result can be POS
		if (
			(hasPosThis && hasPosOther) ||
			(hasPosThis && hasZeroOther) ||
			(hasZeroThis && hasPosOther)
		) {
			// Set POS bit in result (bit 2)
			r |= 4;
		}

		// If this and other have opposite signs
		// the sum can be any sign
		if (
			(hasNegThis && hasPosOther) ||
			(hasPosThis && hasNegOther)
		) {
			// Set all bits in result (bit 0, bit 1, bit 2)
			r |= 7;
		}
		return fromElement(r);
	}

	/**
	 * Returns the abstract result of the subtraction operation between this and another lattice element.
	 * 
	 * @param other subtrahend
	 * 
	 * @return abstract value of {@code this - other}
	 */
	public ExtendedSignLattice subtraction(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom()) {
			return BOTTOM;
		}

		if (this.isTop() || other.isTop()) {
			return TOP;
		}

		return this.addition(other.negation());
	}

	/**
	 * Returns the abstract result of the multiplication operation between this and another lattice element.
	 *
	 * @param other factor
	 * 
	 * @return abstract value of {@code this * other}
	 */
	public ExtendedSignLattice multiplication(ExtendedSignLattice other) {
		// If either element is BOTTOM, the product is unreachable
		if (this.isBottom() || other.isBottom()){
			return BOTTOM;
		}

		// If either element is TOP, the product can be any sign, so we return TOP
		
		boolean hasNegThis = this.hasNegative();
        boolean hasZeroThis = this.hasZero();
        boolean hasPosThis = this.hasPositive();
        boolean hasNegOther = other.hasNegative();
        boolean hasZeroOther = other.hasZero();
        boolean hasPosOther = other.hasPositive();

		// Auxiliary var
		int r = 0;

		// If this and other have opposite signs
		// the product can be NEG
		if (
			(hasNegThis && hasPosOther) ||
			(hasPosThis && hasNegOther)
		) {
			// Set NEG bit in result (bit 0)
			r |= 1;
		}

		// If either this or other can be ZERO
		// the product is ZERO
		if (hasZeroThis || hasZeroOther) {
			// Set ZERO bit in result (bit 1)
			r |= 2;
		}

		// If this and other have the same sign
		// the product can be POS
		if (
			(hasNegThis && hasNegOther) || 
			(hasPosThis && hasPosOther)
		) {
			// Set POS bit in result (bit 2)
			r |= 4; // product is POS
		}

		return fromElement(r);
	}

	/**
	 * Returns the abstract result of the integer division operation between this and another lattice element.
	 *
	 * @param other divisor
	 * 
	 * @return abstract value of {@code this / other}
	 */
	public ExtendedSignLattice division(ExtendedSignLattice other) {
		// If either element is BOTTOM, the division is unreachable
		if (this.isBottom() || other.isBottom()) {
			return BOTTOM;
		}

		// If the divisor is purely zero, the division is unreachable
		if (!other.hasNegative() && !other.hasPositive()) {
			return BOTTOM;
		}

		boolean hasNegThis  = this.hasNegative();
		boolean hasZeroThis = this.hasZero();
		boolean hasPosThis  = this.hasPositive();
		boolean hasNegOther = other.hasNegative();
		boolean hasPosOther = other.hasPositive();

		int r = 0;

		// NEG / NEG -> {POS, ZERO}  (e.g. -2/-1=2, -1/-2=0)
		// If this has NEG component and other has NEG component
		if (hasNegThis && hasNegOther){
			// Set both POS and ZERO bits in result
			r |= 4 | 2;
		}

		// NEG / POS -> {NEG, ZERO}  (e.g. -2/1=-2, -1/2=0)
		// If this has NEG component and other has POS component
		if (hasNegThis && hasPosOther) {
			// Set both NEG and ZERO bits in result
			r |= 1 | 2;
		}

		// ZERO / (NEG or POS) -> ZERO
		// If this has ZERO component and other has either NEG or POS component
		if (hasZeroThis && (hasNegOther || hasPosOther)) {
			// Set ZERO bit in result
			r |= 2;
		}

		// POS / NEG -> {NEG, ZERO}  (e.g. 2/-1=-2, 1/-2=0)
		// If this has POS component and other has NEG component
		if (hasPosThis && hasNegOther) {
			// Set both NEG and ZERO bits in result
			r |= 1 | 2;
		}

		// POS / POS -> {POS, ZERO}  (e.g. 2/1=2, 1/2=0)
		// If this has POS component and other has POS component
		if (hasPosThis && hasPosOther) {
			// Set both POS and ZERO bits in result
			r |= 4 | 2;
		}

		// Return the element corresponding to the new bitmask
		return fromElement(r);
	}

	/**
	 * Returns the abstract result of the modulo operation between this and another lattice element.
	 *
	 * @param other divisor (modulus)
	 * 
	 * @return abstract value of {@code this mod other}
	 */
	public ExtendedSignLattice modulus(ExtendedSignLattice other) {
		// If either operand is BOTTOM, OR
		// If the modulus divisor is purely zero
		if (
			(this.isBottom() || other.isBottom()) ||
			(!other.hasNegative() && !other.hasPositive())
		) {
			// Return bottom
			return BOTTOM;
		}

		// 0 mod x is always 0
		if (getElement() == 2) {
			return ZERO;
		}

		// Auxiliary var
		int r = 0;

		// If the divisor has a NEG component
		if (other.hasNegative()) {
			// Set both NEG and ZERO bits in result
			r |= 1 | 2;
		}

		// If the divisor has a POS component
		if (other.hasPositive()) {
			// Set both POS and ZERO bits in result
			r |= 4 | 2;
		}

		return fromElement(r);
	}

	/**
	 * Returns the abstract result of the remainder operation between this and another lattice element.
	 *
	 * @param other divisor
	 * 
	 * @return abstract value of {@code this % other}
	 */
	public ExtendedSignLattice remainder(ExtendedSignLattice other) {
		// If either operand is BOTTOM, OR
		// If the remainder divisor is purely zero
		if (
			(this.isBottom() || other.isBottom()) ||
			(!other.hasNegative() && !other.hasPositive())
		) {
			// Return bottom
			return BOTTOM;
		}

		// Auxiliary var 
		int r = 0;

		// If this has NEG component
		if (hasNegative()){
			// Set both NEG and ZERO bits in result
			r |= 1 | 2;
		}

		// If this has ZERO component
		if (hasZero()){
			// ZERO mod d is always ZERO
			r |= 2; // 0 % d = 0
		}

		// If this has POS component
		if (hasPositive()){
			// Set both POS and ZERO bits in result
			r |= 4 | 2;
		}

		// Return the element corresponding to the new bitmask
		return fromElement(r);
	}

}
