/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author micha
 */
public abstract class Node extends AirportComponent
{
    
    private List<Link> incoming, outgoing;
    
    public Node(String name)
    {
        super(name);
        incoming = new ArrayList<>();
        outgoing = new ArrayList<>();
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
