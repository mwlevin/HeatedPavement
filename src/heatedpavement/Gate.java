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
    
    public char getSize()
    {
        return size;
    }
    
    public Type getType()
    {
        return Type.gate;
    }
    
}
