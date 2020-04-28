/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

/**
 *
 * @author micha
 */
public class RunwayNode extends Node
{
    public RunwayNode(String name)
    {
        super(name);
    }
    
    public void createCplex(IloCplex cplex) throws IloException
    {
        // nothing to do here
    }
}
