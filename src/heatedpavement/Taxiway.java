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
    
    protected Map<Configuration, IloNumVar> arr_flow_ij, arr_flow_ji, dep_flow_ij, dep_flow_ji;
    //protected IloNumVar flow_ij, flow_ji;
    
    public Taxiway(String name, Node source, Node dest, double area)
    {
        super(name, source, dest, area);
        //flow_ij = new HashMap<>();
       // flow_ji = new HashMap<>();
    }
    
    public void createVariables(IloCplex cplex) throws IloException
    {
        super.createVariables(cplex);
        
        arr_flow_ij = new HashMap<>();
        arr_flow_ji = new HashMap<>();
        dep_flow_ij = new HashMap<>();
        dep_flow_ji = new HashMap<>();
        
        for (Configuration c : Airport.configurations) {
            arr_flow_ij.put(c, cplex.numVar(0, 100000));
            arr_flow_ji.put(c, cplex.numVar(0, 100000));
            dep_flow_ij.put(c, cplex.numVar(0, 100000));
            dep_flow_ji.put(c, cplex.numVar(0, 100000));
        }
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        super.addConstraints(cplex);
        
        double capacity = getCapacity();
        
        
        
        for (Configuration c : Airport.configurations) {
            
            cplex.addLe(cplex.sum(cplex.sum(arr_flow_ij.get(c), dep_flow_ij.get(c)),
                    cplex.sum(arr_flow_ji.get(c), dep_flow_ji.get(c))), cplex.prod(x, capacity));
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
