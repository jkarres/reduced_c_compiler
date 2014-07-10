enum Register {
    I0("i0"), I1("i1"), I2("i2"), I3("i3"), I4("i4"), I5("i5"),
    O0("o0"), O1("o1"), O2("o2"), O3("o3"), O4("o4"), O5("o5"),
    L0("l0"), L1("l1"), L2("l2"), L3("l3"), L4("l4"), L5("l5"),
    G0("g0"), G1("g1"), G2("g2"), G3("g3"), G4("g4"), G5("g5"),
    SP("sp"), FP("fp"), 
    F0("f0"), F1("f1"), F2("f2");

    static Register[] O;
    static Register[] I;

    static {
        O = new Register[6];
        O[0] = O0; O[1] = O1; O[2] = O2; O[3] = O3; O[4] = O4; O[5] = O5;
        I = new Register[6];
        I[0] = I0; I[1] = I1; I[2] = I2; I[3] = I3; I[4] = I4; I[5] = I5;
    }

    private String symbol;

    Register(String symbol) {
        this.symbol = "%" + symbol;
    }

    public String toString() {
        return symbol;
        
    }

}

