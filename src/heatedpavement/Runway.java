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
import java.util.List;

/**
 *
 * @author micha
 */
public class Runway extends AirportComponent
{
    //runways can have multiple incoming and outgoing links
    
    private List<RunwayLink> links;

    private List<RunwayNode> exiting; // where aircraft exit the runway
    private List<RunwayNode> entering; // where aircraft enter the runway
    
    
    protected IloIntVar x;
    
    protected IloNumVar departing_flow, arriving_flow;
    
    public Runway(String name, List<RunwayLink> links, List<RunwayNode> entering, List<RunwayNode> exiting)
    {
        super(name);
        this.links = links;
        this.entering = entering;
        this.exiting = exiting;
    }
    
    
    public void createVariables(IloCplex cplex) throws IloException
    {
        x = cplex.intVar(0, 1);
        departing_flow = cplex.numVar(0, 100000);
        arriving_flow = cplex.numVar(0, 100000);
        
        for(Link l : links)
        {
            l.createVariables(cplex);
        }
        
        for(Node n : entering)
        {
            n.createVariables(cplex);
        }
        
        for(Node n : exiting)
        {
            n.createVariables(cplex);
        }
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        for(Link l : links)
        {
            l.addConstraints(cplex);
        }
        
        for(Node n : entering)
        {
            n.addConstraints(cplex);
        }
        
        for(Node n : exiting)
        {
            n.addConstraints(cplex);
        }
        
        
        
        
        
        
        IloLinearNumExpr rhs = cplex.linearNumExpr();
        
        for(Link l : links)
        {
            rhs.addTerm(1.0/links.size(), l.x);
        }
        
        cplex.addLe(x, rhs); // runway is not active unless all links are cleared
        
        
        
        
        double capacity = getCapacity();
        
        cplex.addLe(cplex.sum(arriving_flow, departing_flow), cplex.prod(x, capacity));
        
        
        
        // conservation of flow: entering traffic = departing traffic, more or less
        rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node
        
        
        for(Node n : entering)
        {
            for(Link l : n.getIncoming())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ij = (Taxiway)l;
                    rhs.addTerm(1, ij.flow_ij); // ij is incoming flow
                    rhs.addTerm(-1, ij.flow_ji); // ji is outgoing flow
                }
            }
            
            for(Link l : n.getOutgoing())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ji = (Taxiway)l;
                    rhs.addTerm(-1, ji.flow_ij); // ij is outgoing flow
                    rhs.addTerm(1, ji.flow_ji); // ji is incoming flow
                }
            }
        }
        
        cplex.addEq(departing_flow, rhs);
        
        
        
        // conservation of flow: exiting traffic = arriving traffic
        rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node
        
        
        for(Node n : exiting)
        {
            for(Link l : n.getIncoming())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ij = (Taxiway)l;
                    rhs.addTerm(-1, ij.flow_ij); // ij is incoming flow
                    rhs.addTerm(1, ij.flow_ji); // ji is outgoing flow
                }
            }
            
            for(Link l : n.getOutgoing())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ji = (Taxiway)l;
                    rhs.addTerm(1, ji.flow_ij); // ij is outgoing flow
                    rhs.addTerm(-1, ji.flow_ji); // ji is incoming flow
                }
            }
        }
        
        cplex.addEq(arriving_flow, rhs);
    }
    
    public Type getType()
    {
        return Type.runway;
    }
    
    public List<RunwayLink> getLinks()
    {
        return links;
    }
}
