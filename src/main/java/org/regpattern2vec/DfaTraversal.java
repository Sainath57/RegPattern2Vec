/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.regpattern2vec;

import java.util.Set;

/**
 *
 * @author @ALIREZA_KAY
 */
public class DfaTraversal {
    
    private final State q0;
    private State curr;
    private String str;
    private final Set<String> input;
    
    public DfaTraversal(State q0, Set<String> input){
        this.q0 = q0;
        this.curr = this.q0;
        this.input = input;
    }
    
    public boolean setCharacter(String str){
        if (!input.contains(str)){
            return false;
        }
        this.str = str;
        return true;
    }
    
    public boolean traverse(){
        curr = curr.getNextStateBySymbol(str);
        return curr.getIsAcceptable();
    }
}
