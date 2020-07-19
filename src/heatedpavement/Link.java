/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.Map;

/**
 *
 * @author micha
 */
public abstract class Link extends AirportComponent
{
    
    protected IloIntVar x;
    
    protected IloIntVar x_1, x_2, x_3;
    
    protected IloIntVar y1_ij, y1_ji; //number of times type 1 snowplow (non-autonomous) drives across link (i, j)
    protected IloIntVar y2_ij, y2_ji; //number of times type 2 snowplow (autonomous) drives across link (i, j)

    
    protected Node source, dest; 
    protected double value_x1, value_x2, value_x3;
    protected double value_y1, value_y2, value_yBoth;
    
    private double area;
    
    public Link(String name, Node source, Node dest, double area)
    {
        super(name);
        
        this.source = source;
        this.dest = dest;
        this.area = area;
        
        if(source != null)
        {
            source.addLink(this);
        }
        if(dest != null)
        {
            dest.addLink(this);
        }
    }
   
    
    public double getArea()
    {
        return area;
    }
    
    public void createVariables(IloCplex cplex) throws IloException
    {
        x = cplex.intVar(0, 1);
        x_1 = cplex.intVar(0, 1);
        x_2 = cplex.intVar(0, 1);
        x_3 = cplex.intVar(0, 1);
        y1_ij = cplex.intVar(0, Integer.MAX_VALUE);
        y1_ji = cplex.intVar(0, Integer.MAX_VALUE);
        y2_ij = cplex.intVar(0, Integer.MAX_VALUE);
        y2_ji = cplex.intVar(0, Integer.MAX_VALUE);
        

        
        if(!Airport.enable_x1)
        {
            cplex.addEq(x_1, 0);
        }
        
        if(!Airport.enable_x2)
        {
            cplex.addEq(x_2, 0);
        }
        
        if(!Airport.enable_x3)
        {
            cplex.addEq(x_3, 0);
        }
        if (Airport.enable_all) {
            cplex.addGe(cplex.sum(x_1, cplex.sum(x_2, x_3)), 1);
        }
        
        cplex.addLe(x, cplex.sum(x_1, cplex.sum(x_2, x_3)));
        cplex.addLe(x_1, cplex.sum(y1_ji, y1_ij));
        cplex.addLe(x_2, cplex.sum(y2_ji, y2_ij));
    }
    
    public void solve(IloCplex cplex) throws IloException {
        value_x1 = cplex.getValue(x_1);
        value_x2 = cplex.getValue(x_2);
        value_x3 = cplex.getValue(x_3);
        value_y1 = cplex.getValue(y1_ji)+cplex.getValue(y1_ij);
        value_y2 = cplex.getValue(y2_ji)+cplex.getValue(y2_ij);
        value_yBoth = value_y1 + value_y2;
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        // nothing to do here
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
