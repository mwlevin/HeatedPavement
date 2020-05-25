/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author micha
 */
public class Node extends AirportComponent
{
    
    private List<Link> incoming, outgoing;
    
    public Node(String name)
    {
        super(name);
        incoming = new ArrayList<>();
        outgoing = new ArrayList<>();
    }
    
    public void addConstraints(IloCplex cplex) throws IloException
    {
        
        
        super.addConstraints(cplex);
        
        // runways are handled separately in the runway class
        
        if(isRunway())
        {
            return;
        }
        
        //System.out.println(getName());
        
        // conservation of flow
        IloLinearNumExpr lhs = cplex.linearNumExpr();
        
        for(Link l : getIncoming())
        {
            Taxiway ij = (Taxiway)l;
            
            // flow_ij is incoming
            // flow_ji is outgoing
            lhs.addTerm(1, ij.flow_ij);
            lhs.addTerm(-1, ij.flow_ji);
        }
        
        
        for(Link l : getOutgoing())
        {
            Taxiway ij = (Taxiway)l;
            
            // flow_ij is outgoing
            // flow_ji is incoming
            lhs.addTerm(-1, ij.flow_ij);
            lhs.addTerm(1, ij.flow_ji);
        }
        
        cplex.addEq(lhs, 0);
    }
    
    private boolean isRunway()
    {
        for(Link i : getIncoming())
        {
            if(i instanceof RunwayLink)
            {
                return true;
            }
        }
        
        for(Link i : getOutgoing())
        {
            if(i instanceof RunwayLink)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public List<Link> getIncoming()
    {
        return incoming;
    }
    
    public List<Link> getOutgoing()
    {
        return outgoing;
    }
    
    public Type getType()
    {
        return Type.none;
    }
    
    public void addLink(Link l)
    {
        if(l.getSource() == this)
        {
            outgoing.add(l);
        }
        else if(l.getDest() == this)
        {
            incoming.add(l);
        }
    }
}
