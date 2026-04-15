error id: file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CProp.java
file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CProp.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[17,1]

error in qdox parser
file content:
```java
offset: 538
uri: file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CProp.java
text:
```scala
package it.unive.scsr.analysis;

import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.Collections;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    @Override
 @@   public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id,
                                 ValueExpression expression, ProgramPoint pp) throws SemanticException {
        // Check if the assigned value is a Constant
        if (expression instanceof Constant) {
            Constant c = (Constant) expression;

            // Requirement: Track only integer values
            if (c.getValue() instanceof Integer) {
                Integer val = (Integer) c.getValue(); // Get the actual int

                // Create the new pair [variable, constant] [cite: 355]
                return Collections.singleton(new CPropSetElem(id, val));
            }
        }

        // If it's not a constant integer, we generate nothing
        package it.unive.scsr.analysis;

        import it.unive.lisa.analysis.SemanticException;
        import it.unive.lisa.analysis.dataflow.DataflowDomain;
        import it.unive.lisa.analysis.dataflow.DefiniteSet;
        import it.unive.lisa.program.cfg.ProgramPoint;
        import it.unive.lisa.symbolic.value.BinaryExpression;
        import it.unive.lisa.symbolic.value.Constant;
        import it.unive.lisa.symbolic.value.Identifier;
        import it.unive.lisa.symbolic.value.UnaryExpression;
        import it.unive.lisa.symbolic.value.ValueExpression;
        import it.unive.lisa.symbolic.value.operator.AdditionOperator;
        import it.unive.lisa.symbolic.value.operator.DivisionOperator;
        import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
        import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
        import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

        import java.util.Collections;
        import java.util.HashSet;
        import java.util.Set;

        public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

            @Override
            public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id,
                                         ValueExpression expression, ProgramPoint pp) throws SemanticException {
                Integer constant = eval(expression, state);
                if (constant == null)
                    return Collections.emptySet();

                return Collections.singleton(new CPropSetElem(id, constant));
            }

            @Override
            public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
                return Collections.emptySet();
            }

            @Override
            public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
                Set<CPropSetElem> result = new HashSet<>();

                for (CPropSetElem element : state.getDataflowElements())
                    if (id.equals(element.getId()))
                        result.add(element);

                return result;
            }

            @Override
            public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
                return Collections.emptySet();
            }

            @Override
            public DefiniteSet<CPropSetElem> makeLattice() {
                return new DefiniteSet<>();
            }

            private Integer eval(ValueExpression expression, DefiniteSet<CPropSetElem> state) {
                if (expression == null)
                    return null;

                if (expression instanceof Constant constant)
                    return evalConstant(constant);

                if (expression instanceof Identifier identifier)
                    return lookup(identifier, state);

                if (expression instanceof UnaryExpression unary)
                    return evalUnary(unary, state);

                if (expression instanceof BinaryExpression binary)
                    return evalBinary(binary, state);

                return null;
            }

            private Integer evalConstant(Constant constant) {
                if (constant.getValue() instanceof Integer integer)
                    return integer;

                return null;
            }

            private Integer lookup(Identifier identifier, DefiniteSet<CPropSetElem> state) {
                Integer found = null;

                for (CPropSetElem element : state.getDataflowElements()) {
                    if (!identifier.equals(element.getId()))
                        continue;

                    Integer current = element.getConstant();
                    if (current == null)
                        return null;

                    if (found == null)
                        found = current;
                    else if (!found.equals(current))
                        return null;
                }

                return found;
            }

            private Integer evalUnary(UnaryExpression expression, DefiniteSet<CPropSetElem> state) {
                Integer argument = eval((ValueExpression) expression.getExpression(), state);
                if (argument == null)
                    return null;

                if (expression.getOperator() == NumericNegation.INSTANCE)
                    return -argument;

                return null;
            }

            private Integer evalBinary(BinaryExpression expression, DefiniteSet<CPropSetElem> state) {
                Integer left = eval((ValueExpression) expression.getLeft(), state);
                Integer right = eval((ValueExpression) expression.getRight(), state);
                if (left == null || right == null)
                    return null;

                if (expression.getOperator() instanceof AdditionOperator)
                    return left + right;

                if (expression.getOperator() instanceof SubtractionOperator)
                    return left - right;

                if (expression.getOperator() instanceof MultiplicationOperator)
                    return left * right;

                if (expression.getOperator() instanceof DivisionOperator) {
                    if (right == 0)
                        return null;

                    return left / right;
                }

                return null;
            }


```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	java.base/java.lang.Thread.run(Thread.java:833)
```
#### Short summary: 

QDox parse error in file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CProp.java