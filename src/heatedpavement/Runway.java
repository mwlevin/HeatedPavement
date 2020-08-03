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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author micha
 */
public class Runway extends AirportComponent
{
    //runways can have multiple incoming and outgoing links
    
    protected List<RunwayLink> links;

    private List<Node> exiting; // where aircraft exit the runway
    private List<Node> entering; // where aircraft enter the runway
    
    
    protected IloIntVar x;
    
    protected Map<Configuration, IloNumVar> departing_flow, arriving_flow;
    
    
    public Runway(String name, List<RunwayLink> links, List<Node> entering, List<Node> exiting)
    {
        super(name);
        this.links = links;
        this.entering = entering;
        this.exiting = exiting;
        
        for(Node n : entering)
        {
            n.isRunway = true;
        }
        
        for(Node n : exiting)
        {
            n.isRunway = true;
        }
    }
    
    
    public void createVariables(IloCplex cplex) throws IloException
    {
        departing_flow = new HashMap<>();
        arriving_flow = new HashMap<>();

        
        x = cplex.intVar(0, 1);
        for (Configuration c : Airport.configurations) {
            departing_flow.put(c, cplex.numVar(0, 100000));
            arriving_flow.put(c, cplex.numVar(0, 100000));

        }
        //departing_flow = cplex.numVar(0, 100000);
        //arriving_flow = cplex.numVar(0, 100000);
        
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
        
        for (Configuration c : Airport.configurations) {
            cplex.addLe(cplex.sum(departing_flow.get(c), arriving_flow.get(c)), cplex.prod(x, capacity));
        }
        //cplex.addLe(cplex.sum(arriving_flow, departing_flow), cplex.prod(x, capacity));
        
        
        // conservation of flow: entering traffic = departing traffic, more or less
        
        
        
        Node main_dep = entering.get(0);
        
        
        for(Node n : entering)
        {
            for(Configuration c : departing_flow.keySet())
            {
     
                if(!c.usesRunway(this))
                {
                    continue;
                }
                
                
                
                rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node

                if(n == main_dep)
                {
                    rhs.addTerm(-1, departing_flow.get(c));
                }
                
                for(Link l : n.getIncoming())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ij = (Taxiway)l;
                        rhs.addTerm(1, ij.dep_flow_ij.get(c));
                        rhs.addTerm(-1, ij.dep_flow_ji.get(c));
                        
                        //rhs.addTerm(1, ij.flow_ij); // ij is incoming flow
                        //rhs.addTerm(-1, ij.flow_ji); // ji is outgoing flow
                    }
                }

                for(Link l : n.getOutgoing())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ji = (Taxiway)l;
                        rhs.addTerm(-1, ji.dep_flow_ij.get(c));
                        rhs.addTerm(1, ji.dep_flow_ji.get(c));
                        
                        //rhs.addTerm(-1, ji.flow_ij); // ij is outgoing flow
                        //rhs.addTerm(1, ji.flow_ji); // ji is incoming flow
                    }
                }
            
            
            cplex.addEq(rhs, 0);
            
                 
            
                rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node


                for(Link l : n.getIncoming())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ij = (Taxiway)l;
                        rhs.addTerm(1, ij.arr_flow_ij.get(c));
                        rhs.addTerm(-1, ij.arr_flow_ji.get(c));
                        
                        //rhs.addTerm(1, ij.flow_ij); // ij is incoming flow
                        //rhs.addTerm(-1, ij.flow_ji); // ji is outgoing flow
                    }
                }

                for(Link l : n.getOutgoing())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ji = (Taxiway)l;
                        rhs.addTerm(-1, ji.arr_flow_ij.get(c));
                        rhs.addTerm(1, ji.arr_flow_ji.get(c));
                        
                        //rhs.addTerm(-1, ji.flow_ij); // ij is outgoing flow
                        //rhs.addTerm(1, ji.flow_ji); // ji is incoming flow
                    }
                }
            }
            
            cplex.addEq(rhs, 0);
            
        }
        
        
        
        
        
        
        // conservation of flow: exiting traffic = arriving traffic
        
        
        Node main_arr = exiting.get(exiting.size()-1);
        
        for(Node n : exiting)
        {
            
            for(Configuration c : arriving_flow.keySet())
            {

                if(!c.usesRunway(this))
                {
                    continue;
                }
                
                
                rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node

                if(n == main_arr)
                {
                    rhs.addTerm(-1, arriving_flow.get(c));
                }
                
                for(Link l : n.getIncoming())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ij = (Taxiway)l;
                        rhs.addTerm(1, ij.arr_flow_ij.get(c));
                        rhs.addTerm(-1, ij.arr_flow_ji.get(c));
                        
                        //rhs.addTerm(1, ij.flow_ij); // ij is incoming flow
                        //rhs.addTerm(-1, ij.flow_ji); // ji is outgoing flow
                    }
                }

                for(Link l : n.getOutgoing())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ji = (Taxiway)l;
                        rhs.addTerm(-1, ji.arr_flow_ij.get(c));
                        rhs.addTerm(1, ji.arr_flow_ji.get(c));
                        
                        //rhs.addTerm(-1, ji.flow_ij); // ij is outgoing flow
                        //rhs.addTerm(1, ji.flow_ji); // ji is incoming flow
                    }
                }
                
                cplex.addEq(rhs, 0);
                
            
                
                
                rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node

                
                for(Link l : n.getIncoming())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ij = (Taxiway)l;
                        rhs.addTerm(1, ij.dep_flow_ij.get(c));
                        rhs.addTerm(-1, ij.dep_flow_ji.get(c));
                        
                        //rhs.addTerm(1, ij.flow_ij); // ij is incoming flow
                        //rhs.addTerm(-1, ij.flow_ji); // ji is outgoing flow
                    }
                }

                for(Link l : n.getOutgoing())
                {
                    if(l instanceof Taxiway)
                    {
                        Taxiway ji = (Taxiway)l;
                        rhs.addTerm(-1, ji.dep_flow_ij.get(c));
                        rhs.addTerm(1, ji.dep_flow_ji.get(c));
                        
                        //rhs.addTerm(-1, ji.flow_ij); // ij is outgoing flow
                        //rhs.addTerm(1, ji.flow_ji); // ji is incoming flow
                    }
                }
                
                cplex.addEq(rhs, 0);
            }          
        }
        

        
        for(Configuration c : arriving_flow.keySet())
        {
            if(!c.usesRunway(this))
            {
                cplex.addEq(departing_flow.get(c), 0);
                cplex.addEq(arriving_flow.get(c), 0);
            }
        
        }
        
        
        // plow all entrances and exits
        
        /*
        for(Node n : exiting)
        {
            rhs = cplex.linearNumExpr();
            
            for(Link l : n.getIncoming())
            {
                rhs.addTerm(1, l.x);
            }
            
            for(Link l : n.getOutgoing())
            {
                rhs.addTerm(1, x);
            }
            
            cplex.addGe(rhs, x);
        }
        */
        
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
