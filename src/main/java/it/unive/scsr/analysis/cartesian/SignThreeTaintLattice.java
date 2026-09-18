package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.floatInterval.FloatIntervalLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.Objects;

public class SignThreeTaintLattice implements BaseLattice<SignThreeTaintLattice>{

    private final ExtendedSignLattice sign;
    private final TaintThreeLevelsLattice taint;

    public static SignThreeTaintLattice TOP= new SignThreeTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.Top);
    public static SignThreeTaintLattice BOTTOM= new SignThreeTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);
    public SignThreeTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint){
        this.sign=sign;
        this.taint=taint;
    }

    public ExtendedSignLattice getSign(){return sign;}
    public TaintThreeLevelsLattice getTaint(){return taint;}

    @Override
    public SignThreeTaintLattice top(){return TOP;}
    @Override
    public SignThreeTaintLattice bottom(){return BOTTOM;}

    //call to lub (which then calls specific lubAux for sign and taint), same thing for other methods
    @Override
    public SignThreeTaintLattice lubAux(SignThreeTaintLattice other) throws SemanticException {
        return new SignThreeTaintLattice(this.sign.lub(other.sign), this.taint.lub(other.taint));
    }

    @Override
    public SignThreeTaintLattice glbAux(SignThreeTaintLattice other) throws SemanticException{
        return new SignThreeTaintLattice(this.sign.glb(other.sign), this.taint.glb(other.taint));
    }

    @Override
    public boolean lessOrEqualAux(SignThreeTaintLattice other) throws SemanticException{
        return this.sign.lessOrEqual(other.sign) && this.taint.lessOrEqual(other.taint);
    }

    @Override
    public StructuredRepresentation representation(){
        if(isBottom())  return Lattice.bottomRepresentation();
        if(isTop()) return Lattice.topRepresentation();
        return new StringRepresentation("["+sign.representation()+","+taint.representation()+"]");
    }

    @Override
    public int hashCode(){return Objects.hash(sign,taint);}

    @Override
    public boolean equals(Object obj){
        if(this==obj) return true;
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass())   return false;
        SignThreeTaintLattice other= (SignThreeTaintLattice) obj;
        return this.sign.equals(other.sign) && this.taint.equals(other.taint);
    }

}
