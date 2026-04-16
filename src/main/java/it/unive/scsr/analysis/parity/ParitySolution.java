package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ParitySolution extends DataflowDomain<DefiniteSet<Parity>, Parity> {

    private Parity.State eval(ValueExpression expression, DefiniteSet<Parity> state) {
        if (expression instanceof Constant) {
            Object val = ((Constant) expression).getValue();
            if (val instanceof Integer) 
                return (Math.abs((Integer) val) % 2 == 0) ? Parity.State.EVEN : Parity.State.ODD;
        }
        if (expression instanceof Identifier) {
            for (Parity p : state.getDataflowElements())
                if (p.getId().equals(expression)) return p.getState();
        }
        return null;
    }

    @Override
    public Set<Parity> gen(DefiniteSet<Parity> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Parity.State s = eval(expression, state);
        if (s != null) return Collections.singleton(new Parity(id, s));
        return Collections.emptySet();
    }

    @Override
    public Set<Parity> gen(DefiniteSet<Parity> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<Parity> kill(DefiniteSet<Parity> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<Parity> result = new HashSet<>();
        for (Parity p : state.getDataflowElements())
            if (p.getId().equals(id)) result.add(p);
        return result;
    }

    @Override
    public Set<Parity> kill(DefiniteSet<Parity> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<Parity> makeLattice() {
        return new DefiniteSet<>();
    }
}