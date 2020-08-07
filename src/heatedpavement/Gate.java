/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

/**
 * flow in = flow out
 * @author micha
 */
public class Gate extends Taxiway
{
    private char size;
    
    public Gate(String name, Node dest, double area, char size)
    {
        // no source node, the gate is the end point
        super(name, null, dest, area);
        this.size = size;
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        super.addConstraints(cplex);
    
        double capacity = getCapacity()/2;
        
        
        
        for (Configuration c : Airport.configurations) {
            cplex.addLe(dep_flow_ij.get(c), capacity);
            cplex.addLe(arr_flow_ji.get(c), capacity);
            
            cplex.addEq(dep_flow_ji.get(c), 0);
            cplex.addEq(arr_flow_ij.get(c), 0);
        }
        
    }
    
    public char getSize()
    {
        return size;
    }
    
    public Type getType()
    {
        return Type.gate;
    }
    
}
