/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

/**
 * All incoming and outgoing links are Taxiway or subclasses
 * @author micha
 */
public class TaxiwayNode extends Node
{
    public TaxiwayNode(String name)
    {
        super(name);
    }
    
    public void createCplex(IloCplex cplex) throws IloException
    {
        // conservation of flow
        IloLinearNumExpr lhs = cplex.linearNumExpr();
        
        for(Link l : getIncoming())
        {
            Taxiway ij = (Taxiway)l;
            
            // flow_ij is incoming
            // flow_ji is outgoing
            lhs.addTerm(1, ij.flow_ij);
            lhs.addTerm(-1, ij.flow_ji);
        }
        
        
        for(Link l : getOutgoing())
        {
            Taxiway ij = (Taxiway)l;
            
            // flow_ij is outgoing
            // flow_ji is incoming
            lhs.addTerm(-1, ij.flow_ij);
            lhs.addTerm(1, ij.flow_ji);
        }
        
        cplex.addEq(lhs, 0);
    }
}
