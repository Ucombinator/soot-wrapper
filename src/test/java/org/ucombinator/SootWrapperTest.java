package org.ucombinator;

import org.ucombinator.SootWrapper;

class SootWrapperTest {
    public static void main (String[] args) {
        System.out.println(SootWrapper.fromSource(args[0], args[1]).getShimple().getFirst().getMethods().get(0).getActiveBody().getClass().getName());
        System.out.println(SootWrapper.fromSource(args[0], args[1]).getJimple().getFirst().getMethods().get(0).getActiveBody().getClass().getName());
        System.out.println(SootWrapper.fromSource(args[0], args[1]).getCallGraph());
    }
}
