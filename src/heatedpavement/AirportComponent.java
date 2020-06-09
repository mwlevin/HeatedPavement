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
    //public static char size; // A--F
    
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
            case gate: 
                char size = ((Gate)this).getSize();
                if(size == 'D' || size == 'E' || size == 'F')
                {
                    return 1.5; // 1 departure and 1 arrival per 90min
                }
                else
                {
                    return 2; // 1 departure and 1 arrival
                }
            case runway: 
                if(mix_index <= 20)
                {
                    return 59;
                }
                else if(mix_index <= 50)
                {
                    return 57;
                }
                else if(mix_index <= 80)
                {
                    return 56;
                }
                else if(mix_index <= 120)
                {
                    return 53;
                }
                else
                {
                    return 50;
                }
            case taxiway: return 90;
            default: return 0;
        }
    }
    
    public static double mix_index = 0;
}
