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
        rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node
        
        
        for(Node n : entering)
        {
            for(Link l : n.getIncoming())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ij = (Taxiway)l;
                    for (Map.Entry<Configuration, IloNumVar> entry : ij.flow_ij.entrySet()) {
                        rhs.addTerm(1, entry.getValue());
                    }
                    for (Map.Entry<Configuration, IloNumVar> entry : ij.flow_ji.entrySet()) {
                        rhs.addTerm(-1, entry.getValue());
                    }
                    //rhs.addTerm(1, ij.flow_ij); // ij is incoming flow
                    //rhs.addTerm(-1, ij.flow_ji); // ji is outgoing flow
                }
            }
            
            for(Link l : n.getOutgoing())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ji = (Taxiway)l;
                    for (Map.Entry<Configuration, IloNumVar> entry : ji.flow_ij.entrySet()) {
                        rhs.addTerm(-1, entry.getValue());
                    }
                    for (Map.Entry<Configuration, IloNumVar> entry : ji.flow_ji.entrySet()) {
                        rhs.addTerm(1, entry.getValue());
                    }
                    //rhs.addTerm(-1, ji.flow_ij); // ij is outgoing flow
                    //rhs.addTerm(1, ji.flow_ji); // ji is incoming flow
                }
            }
            
        }
        
        for (Map.Entry<Configuration, IloNumVar> entry : departing_flow.entrySet()) {
            boolean found = false;
            for (Runway r : entry.getKey().activeRunways) {
                if (r.name.equals(this.name)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                cplex.addEq(entry.getValue(), rhs);
            }
            else {
                cplex.addEq(entry.getValue(), 0);
            }
        }
        //cplex.addEq(departing_flow, rhs);
        
        
        
        // conservation of flow: exiting traffic = arriving traffic
        
        
        
        Map<Node, IloNumVar> exitingFlow = new HashMap<>();
        
        
        for(Node n : exiting)
        {
            IloNumVar exitAtN = cplex.numVar(0, 100000);
            exitingFlow.put(n, exitAtN);
            
            rhs = cplex.linearNumExpr(); // difference between entering and exiting at each node
            
            for(Link l : n.getIncoming())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ij = (Taxiway)l;
                    for (Map.Entry<Configuration, IloNumVar> entry : ij.flow_ij.entrySet()) {
                        rhs.addTerm(-1, entry.getValue());
                    }
                    for (Map.Entry<Configuration, IloNumVar> entry : ij.flow_ji.entrySet()) {
                        rhs.addTerm(1, entry.getValue());
                    }
                    //rhs.addTerm(-1, ij.flow_ij); // ij is incoming flow
                    //rhs.addTerm(1, ij.flow_ji); // ji is outgoing flow
                }
            }
            
            for(Link l : n.getOutgoing())
            {
                if(l instanceof Taxiway)
                {
                    Taxiway ji = (Taxiway)l;
                    for (Map.Entry<Configuration, IloNumVar> entry : ji.flow_ij.entrySet()) {
                        rhs.addTerm(1, entry.getValue());
                    }
                    for (Map.Entry<Configuration, IloNumVar> entry : ji.flow_ji.entrySet()) {
                        rhs.addTerm(-1, entry.getValue());
                    }
                    //rhs.addTerm(1, ji.flow_ij); // ij is outgoing flow
                    //rhs.addTerm(-1, ji.flow_ji); // ji is incoming flow
                }
            }
            
            cplex.addEq(exitAtN, rhs);
        }
        
        rhs = cplex.linearNumExpr();
        
        for(Node n : exitingFlow.keySet())
        {
            rhs.addTerm(1, exitingFlow.get(n));
        }
        
        for (Map.Entry<Configuration, IloNumVar> entry : arriving_flow.entrySet()) {
            boolean found = false;
            for (Runway r : entry.getKey().activeRunways) {
                if (r.name.equals(this.name)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                cplex.addEq(entry.getValue(), rhs);
            }
            else {
                cplex.addEq(entry.getValue(), 0);
            }
        }
        //cplex.addEq(arriving_flow, rhs);
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
