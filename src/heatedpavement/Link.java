/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author micha
 */
public abstract class Link extends AirportComponent
{
    
    protected IloIntVar x;
    
    protected IloIntVar x_1, x_2, x_3;
    
    
    
    private Node source, dest;  
    
    private double area;
    
    public Link(String name, Node source, Node dest, double area)
    {
        super(name);
        
        this.source = source;
        this.dest = dest;
        this.area = area;
    }
   
    
    public double getArea()
    {
        return area;
    }
    
    public void createCplex(IloCplex cplex) throws IloException
    {
        x = cplex.intVar(0, 1);
        x_1 = cplex.intVar(0, 1);
        x_2 = cplex.intVar(0, 1);
        x_3 = cplex.intVar(0, 1);
        
        cplex.addLe(x, cplex.sum(x_1, cplex.sum(x_2, x_3)));
        
        
        
    }
    
    
    
    public Node getSource()
    {
        return source;
    }
    
    public Node getDest()
    {
        return dest;
    }
}
