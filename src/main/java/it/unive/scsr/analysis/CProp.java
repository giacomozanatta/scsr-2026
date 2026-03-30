package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> domain, Identifier id, ValueExpression expr, ProgramPoint pp) throws SemanticException {
        // mi trovo in una situazione del tipo x = 5 + 2 | x = y + 2;
        Integer constValue = evaluate(expr, domain);

        // se sono riuscito a ricavare il valore dell'identificatore, restituisco una Collection con un solo elemento CPropSetElem
        // altrimenti restituisco una Collection vuota
        return constValue != null
                ? Collections.singleton(new CPropSetElem(id, constValue))
                : Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> domain, Identifier id, ValueExpression expr, ProgramPoint pp) {

        // vado alla ricerca di tutte le assegnazioni fatte alla variabile 'id' per "ucciderle",
        // dato che sta venendo assegnato alla variabile un nuovo valore
        Set<CPropSetElem> toRemoveElements = new HashSet<>();
        Collection<CPropSetElem> elements = domain.getDataflowElements();
        for (CPropSetElem element : elements) {
            if (element.getId().equals(id)) {
                toRemoveElements.add(element);
            }
        }
        return toRemoveElements;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> domain, ValueExpression expr, ProgramPoint pp) {
        // restituisco direttamente una Collection vuota, dato che mi trovo in un'espressione senza assegnazione
        // tipo return, if..., dato che non viene assegnato nessun nuovo valore a una variabile
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> domain, ValueExpression expr, ProgramPoint pp) {
        // restituisco direttamente una Collection vuota, dato che mi trovo in un'espressione senza assegnazione
        // tipo return, if... in cui non viene effettuata nessuna "uccisione"
        return Collections.emptySet();
    }

    // Valuta un'espressione simbolica cercando di ridurla a un intero costante.
    // Restituisce null se non e' possibile determinare il valore.
    // Funziona in modo ricorsivo: prima risolve le sottoespressioni, poi combina.
    private static Integer evaluate(ValueExpression expr, DefiniteSet<CPropSetElem> domain) {
        // caso 1: è una costante
        if (expr instanceof Constant constant) {
            Object value = constant.getValue();
            if (value instanceof Integer) {
                return (Integer) value;
            }
            return null;
        }

        // caso 2: è uguale ad un'altra variabile; cerco se ho già assegnato un valore a questa variabile
        //         e in caso lo assegno; se non trovo un'assegnazione, restituisco null
        if (expr instanceof Identifier id) {
            Collection<CPropSetElem> elements = domain.getDataflowElements();
            for (CPropSetElem element : elements) {
                if (element.getId().equals(id)) {
                    return element.getConstant();
                }
            }
            return null;
        }

        // caso 3: espressione unaria (es. negazione -x)
        if (expr instanceof UnaryExpression unaryExpression) {
            Integer innerValue = evaluate((ValueExpression) unaryExpression.getExpression(), domain);
            if (innerValue == null) {
                return null;
            }
            if (unaryExpression.getOperator() == NumericNegation.INSTANCE) {
                return -innerValue;
            }
        }

        // caso 4: espressione binaria (es. x + y, x * 3, ecc.)
        if (expr instanceof BinaryExpression binaryExpression) {
            Integer leftValue = evaluate((ValueExpression) binaryExpression.getLeft(), domain);
            Integer rightValue = evaluate((ValueExpression) binaryExpression.getRight(), domain);

            // non sono riuscito a ricavare il valore di uno dei due operandi, quindi restituisco null
            if (leftValue == null || rightValue == null) {
                return null;
            }

            BinaryOperator binaryOperator = binaryExpression.getOperator();

            if (binaryOperator instanceof AdditionOperator) {
                return leftValue + rightValue;
            }
            if (binaryOperator instanceof SubtractionOperator) {
                return leftValue - rightValue;
            }
            if (binaryOperator instanceof MultiplicationOperator) {
                return leftValue * rightValue;
            }

            if (binaryOperator instanceof DivisionOperator) {
                if (rightValue == 0) {
                    // divisore = 0, restituisco null
                    return null;
                } else {
                    return leftValue / rightValue;
                }
            }
        }

        // caso 5: non gestito, quindi restituisco null
        return null;
    }
}