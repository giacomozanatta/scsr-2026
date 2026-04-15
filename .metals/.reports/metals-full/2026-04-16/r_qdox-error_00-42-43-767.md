error id: file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CPropSetElem.java
file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CPropSetElem.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[30,27]

error in qdox parser
file content:
```java
offset: 1022
uri: file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CPropSetElem.java
text:
```scala
package it.unive.scsr.analysis;

import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.analysis.SemanticException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class CPropSetElem implements DataflowElement<CPropSetElem> {
    /**
     * IMPLEMENT THIS CLASS
     * the code below is outside of the scope of the course.
     * You can uncomment it to get your code to compile.
        if (id == null)
            return Collections.emptyList();

        return List.of(id);
     * and a field named "constant" exist in this class.
     */

    private final Identifier id;
        if (Objects.equals(@@id, source))
            return new CPropSetElem(target, constant);

        return this;

    public CPropSetElem(Identifier id, Integer constant) {
        this.id = id;
        this.constant = constant;
    }


    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return List.of();
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public CPropSetElem pushScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }



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

QDox parse error in file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CPropSetElem.java