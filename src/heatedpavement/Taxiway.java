/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author micha
 */
public class Taxiway extends Link
{
    
    protected Map<Configuration, IloNumVar> flow_ij, flow_ji;
    //protected IloNumVar flow_ij, flow_ji;
    
    public Taxiway(String name, Node source, Node dest, double area)
    {
        super(name, source, dest, area);
        //flow_ij = new HashMap<>();
       // flow_ji = new HashMap<>();
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        super.addConstraints(cplex);
        
        double capacity = getCapacity();
        flow_ij = new HashMap<>();
        flow_ji = new HashMap<>();
        
        for (Configuration c : Airport.configurations) {
            flow_ij.put(c, cplex.numVar(0, 100000));
            cplex.addLe(flow_ij.get(c), cplex.prod(x, capacity));
            flow_ji.put(c, cplex.numVar(0, 100000));
            cplex.addLe(flow_ji.get(c), cplex.prod(x, capacity));
        }
        //flow_ij = cplex.numVar(0, 100000);
        //flow_ji = cplex.numVar(0, 100000);
        //cplex.addLe(flow_ij, cplex.prod(x, capacity));
        //cplex.addLe(flow_ji, cplex.prod(x, capacity));
    }
    
    public Type getType()
    {
        return Type.taxiway;
    }
}
