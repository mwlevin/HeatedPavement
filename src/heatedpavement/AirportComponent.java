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
public abstract class AirportComponent 
{
    public static enum Type {runway, taxiway, gate, none};
    public static char size; // A--F
    
    private String name;
    
    public AirportComponent(String name)
    {
        this.name = name;
    }
    
    public void createVariables(IloCplex cplex) throws IloException
    {
        // nothing to do here
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        // nothing to do here
    }
    
    public String toString()
    {
        return name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public abstract Type getType();
    
    public double getCapacity()
    {
        switch(getType())
        {
            case gate: return 2;
            case runway: return 50;
            case taxiway: return 100;
            default: return 0;
        }
    }
}