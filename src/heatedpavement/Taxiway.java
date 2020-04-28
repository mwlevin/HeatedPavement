/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author micha
 */
public class Taxiway extends Link
{
    
    protected IloNumVar flow_ij, flow_ji;
    
    public Taxiway(String name, Node source, Node dest, double area)
    {
        super(name, source, dest, area);
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        super.addConstraints(cplex);
        
        flow_ij = cplex.numVar(0, 100000);
        flow_ji = cplex.numVar(0, 100000);
        
        
        
        double capacity = getCapacity();
        
        cplex.addLe(flow_ij, cplex.prod(x, capacity));
        cplex.addLe(flow_ji, cplex.prod(x, capacity));
    }
    
    public Type getType()
    {
        return Type.taxiway;
    }
}
