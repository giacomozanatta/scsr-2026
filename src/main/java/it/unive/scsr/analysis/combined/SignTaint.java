package it.unive.scsr.analysis.combined;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.Sign;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import java.util.Objects;

/**
 * A combined abstract domain representing the Cartesian product of Sign and Taint analyses.
 * It tracks both the mathematical sign of a variable and its taint status simultaneously.
 */
public class SignTaint implements BaseNonRelationalValueDomain<SignTaint>, Lattice<SignTaint> {

    // The sign lattice component
    private final SignLattice sign;
    
    // The taint lattice component
    private final TaintThreeLevelsLattice taint;

    // Constructs a new SignTaint combined domain with specific sign and taint states
    public SignTaint(SignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign = sign;
        this.taint = taint;
    }

    // Default constructor initializing to the TOP state (most imprecise) for both domains
    public SignTaint() {
        this(SignLattice.TOP, TaintThreeLevelsLattice.Top);
    }

    // Returns the TOP element of the combined lattice
    @Override
    public SignTaint top() {
        return new SignTaint(SignLattice.TOP, TaintThreeLevelsLattice.Top);
    }

    // Returns the BOTTOM element (unreachable state) of the combined lattice
    @Override
    public SignTaint bottom() {
        return new SignTaint(SignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);
    }

    // Checks if the current state is TOP
    @Override
    public boolean isTop() {
        return sign == SignLattice.TOP && taint == TaintThreeLevelsLattice.Top;
    }

    // Checks if the current state is BOTTOM
    @Override
    public boolean isBottom() {
        return sign == SignLattice.BOTTOM && taint == TaintThreeLevelsLattice.Bottom;
    }

    // Returns the default unknown state for an identifier
    @Override
    public SignTaint unknownValue(Identifier id) {
        return top();
    }

    // Computes the Least Upper Bound (LUB) by applying LUB to both sub-domains independently
    @Override
    public SignTaint lub(SignTaint other) throws SemanticException {
        return new SignTaint(sign.lub(other.sign), taint.lub(other.taint));
    }

    // Checks the partial order relation by checking it in both sub-domains independently
    @Override
    public boolean lessOrEqual(SignTaint other) throws SemanticException {
        return sign.lessOrEqual(other.sign) && taint.lessOrEqual(other.taint);
    }

    // Generates the visual representation for the HTML graphs (e.g., "(+, T)")
    @Override
    public StructuredRepresentation representation() {
        if (isTop()) return Lattice.topRepresentation();
        if (isBottom()) return Lattice.bottomRepresentation();
        return new StringRepresentation("(" + sign.representation() + ", " + taint.representation() + ")");
    }

    // Getters for the individual domain states
    public SignLattice getSign() { return sign; }
    public TaintThreeLevelsLattice getTaint() { return taint; }

    // Evaluates an expression by delegating the evaluation to both sub-domains
    // Removed @Override to avoid signature mismatch issues in the base class
    public SignTaint eval(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        // Create temporary environments initialized to TOP for evaluating the expression
        ValueEnvironment<SignLattice> signEnv = new ValueEnvironment<>(SignLattice.TOP);
        ValueEnvironment<TaintThreeLevelsLattice> taintEnv = new ValueEnvironment<>(TaintThreeLevelsLattice.Top);

        // Evaluate the expression in the Sign domain and Taint domain respectively
        SignLattice s = new Sign().eval(signEnv, expression, pp, oracle);
        TaintThreeLevelsLattice t = new TaintThreeLevels().eval(taintEnv, expression, pp, oracle);

        // Return the newly computed combined state
        return new SignTaint(s, t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignTaint other = (SignTaint) o;
        return sign == other.sign && taint == other.taint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, taint);
    }
}