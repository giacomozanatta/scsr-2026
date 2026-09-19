package it.unive.scsr.analysis;

import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class CPropDomain extends DataflowDomain<DefiniteSet<CProp>, CProp> {

    @Override
    public DefiniteSet<CProp> makeLattice() {
        return new DefiniteSet<>();
    }

    @Override
    public Set<CProp> gen(DefiniteSet<CProp> state, Identifier id, ValueExpression expression, ProgramPoint pp) {
        Set<CProp> result = new HashSet<>();
        if (expression instanceof Constant && ((Constant) expression).getValue() instanceof Integer) {
            result.add(new CProp(id, (Integer) ((Constant) expression).getValue()));
        }
        return result;
    }

    @Override
    public Set<CProp> gen(DefiniteSet<CProp> state, ValueExpression expression, ProgramPoint pp) {
        return Collections.emptySet();
    }

    @Override
    public Set<CProp> kill(DefiniteSet<CProp> state, Identifier id, ValueExpression expression, ProgramPoint pp) {
        Set<CProp> result = new HashSet<>();
        for (CProp e : state.getDataflowElements()) {
            if (e.getInvolvedIdentifiers().contains(id))
                result.add(e);
        }
        return result;
    }

    @Override
    public Set<CProp> kill(DefiniteSet<CProp> state, ValueExpression expression, ProgramPoint pp) {
        return Collections.emptySet();
    }
}