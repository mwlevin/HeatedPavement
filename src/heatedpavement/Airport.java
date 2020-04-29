/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplexModeler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class Airport 
{
    // enable x_1, x_2, x_3
    public static final boolean enable_x1 = false;
    public static final boolean enable_x2 = false;
    public static final boolean enable_x3 = true;
    
    
    // define the runways in 1 direction only please
    
    private List<Runway> runways;
    private List<Node> nodes; 
    private List<Taxiway> taxiways;
    private List<Gate> gates;
    
    private List<AirportComponent> components;
    
    private Map<Character, Double> departures, arrivals;
    
    
    private Map<String, Node> lookupNode;
    private Map<String, Link> lookupLink;
    private List<String> runwayNodes, runwayLinks;
    
    public Airport(String airportName) throws IOException
    {
        runways = new ArrayList<>();
        nodes = new ArrayList<>();
        taxiways = new ArrayList<>();
        gates = new ArrayList<>();
        components = new ArrayList<>();
        departures = new HashMap<>();
        arrivals = new HashMap<>();
        
        
        
        lookupNode = new HashMap<>();
        lookupLink = new HashMap<>();
        
        String directory = "airports/"+airportName+"/";
        
        runwayNodes = new ArrayList<>();
        runwayLinks = new ArrayList<>();
        
        Scanner filein = new Scanner(new File(directory+"runways.txt"));
        filein.nextLine();
        
        while(filein.hasNext())
        {
            filein.next();
            String line = filein.nextLine().trim();
            
            String temp = line.substring(1, line.indexOf('}'));
            line = line.substring(line.indexOf('}')+1).trim();
            Scanner chopper = new Scanner(temp);
            
            while(chopper.hasNext())
            {
                runwayLinks.add(chopper.next());
            }
            
            temp = line.substring(1, line.indexOf('}'));
            line = line.substring(line.indexOf('}')+1).trim();
            
            chopper = new Scanner(temp);
            
            while(chopper.hasNext())
            {
                runwayNodes.add(chopper.next());
            }
            
            temp = line.substring(1, line.indexOf('}'));
            
            chopper = new Scanner(temp);
            
            while(chopper.hasNext())
            {
                runwayNodes.add(chopper.next());
            }
            
            
        }
        filein.close();
        
        filein = new Scanner(new File(directory+"gates.txt"));
        filein.nextLine();
        
        while(filein.hasNext())
        {
            gates.add(new Gate(filein.next(), findNode(filein.next()), filein.nextDouble(), filein.next().charAt(0)));
        }
        filein.close();
        
        filein = new Scanner(new File(directory+"links.txt"));
        filein.nextLine();
        
        while(filein.hasNext())
        {
            String name = filein.next();
            String source = filein.next();
            String dest = filein.next();
            double area = filein.nextDouble();
            
            Link link;
            
            if(runwayLinks.contains(name))
            {
                link = new RunwayLink(name, findNode(source), findNode(dest), area);
            }
            else
            {
                link = new Taxiway(name, findNode(source), findNode(dest), area);
                taxiways.add((Taxiway)link);
            }
            
            lookupLink.put(name, link);
        }
        filein.close();
        
        filein = new Scanner(new File(directory+"runways.txt"));
        filein.nextLine();
        
        while(filein.hasNext())
        {
            String name = filein.next();
            String line = filein.nextLine().trim();
            
            String temp = line.substring(1, line.indexOf('}'));
            line = line.substring(line.indexOf('}')+1).trim();
            Scanner chopper = new Scanner(temp);
            
            List<RunwayLink> links = new ArrayList<>();
            List<Node> entering = new ArrayList<>();
            List<Node> exiting = new ArrayList<>();
            
            while(chopper.hasNext())
            {
                links.add((RunwayLink)lookupLink.get(chopper.next()));
            }
            
            temp = line.substring(1, line.indexOf('}'));
            line = line.substring(line.indexOf('}')+1).trim();
            
            chopper = new Scanner(temp);
            
            while(chopper.hasNext())
            {
                entering.add(lookupNode.get(chopper.next()));
            }
            
            temp = line.substring(1, line.indexOf('}'));
            
            chopper = new Scanner(temp);
            
            while(chopper.hasNext())
            {
                exiting.add(lookupNode.get(chopper.next()));
            }
            
            runways.add(new Runway(name, links, entering, exiting));
            
        }
        filein.close();
        
        
        filein = new Scanner(new File(directory+"/arrivals.txt"));
        filein.nextLine();
        
        while(filein.hasNext())
        {
            arrivals.put(filein.next().charAt(0), filein.nextDouble());
        }
        filein.close();
        
        filein = new Scanner(new File(directory+"/departures.txt"));
        filein.nextLine();
        
        while(filein.hasNext())
        {
            departures.put(filein.next().charAt(0), filein.nextDouble());
        }
        filein.close();
        
        
        components = new ArrayList<>();
        
        for(Taxiway t : taxiways)
        {
            if(!(t instanceof Gate))
            {
                components.add(t);
            }
        }
        
        for(Gate g : gates)
        {
            components.add(g);
        }
        
        for(Node n : nodes)
        {
            components.add(n);
        }
        
        for(Runway r : runways)
        {
            components.add(r);
        }
        
        
        
        
        runwayNodes = null;
        runwayLinks = null;
    }
    
    private Node findNode(String name)
    {
        if(!lookupNode.containsKey(name))
        {
            Node node = new Node(name);
            if(!runwayNodes.contains(name))
            {
                nodes.add(node);
            }
            
            lookupNode.put(name, node);
            return node;
        }
        else
        {
            return lookupNode.get(name);
        }
    }
    
    public Airport(List<Runway> runways, List<Node> nodes, List<Taxiway> taxiways, List<Gate> gates, 
            Map<Character, Double> departures, Map<Character, Double> arrivals)
    {
        this.runways = runways;
        this.nodes = nodes;
        this.taxiways = taxiways;
        this.gates = gates;
        
        components = new ArrayList<>();
        
        for(Taxiway t : taxiways)
        {
            if(!(t instanceof Gate))
            {
                components.add(t);
            }
        }
        
        for(Gate g : gates)
        {
            components.add(g);
        }
        
        for(Node n : nodes)
        {
            components.add(n);
        }
        
        for(Runway r : runways)
        {
            components.add(r);
        }
        
        this.departures = departures;
        this.arrivals = arrivals;
    }
    
    public void solveCplex() throws IloException
    {
        IloCplex cplex = new IloCplex();
        
        for(AirportComponent c : components)
        {
            c.createVariables(cplex);
        }
        
        for(AirportComponent c : components)
        {
            c.addConstraints(cplex);
        }
        
        
        // airport total demand -> runway departures, arrivals; gate flows
        
        double total_departing = 0;
        
        for(char s : departures.keySet())
        {
            total_departing += departures.get(s);
        }
        
        double total_arriving = 0;
        
        for(char s : arrivals.keySet())
        {
            total_arriving += arrivals.get(s);
        }
        
        
        IloLinearNumExpr lhs = cplex.linearNumExpr();
        
        for(Runway r : runways)
        {
            lhs.addTerm(1, r.departing_flow);
        }
        
        cplex.addEq(lhs, total_departing);
        
        
        // F.flow >= F.demand
        // F.flow + E.flow >= F.demand + E.demand
        // F.flow + E.flow + D.flow >= F.demand + E.demand + D.demand
        // etc.
        
        
        // convention: for gates, flow_ij is departing, flow_ji is arriving
        for(char s = 'F'; s >= 'A'; s--)
        {
            // this is for departures
            lhs = cplex.linearNumExpr();
            double total_demand = 0;
            
            for(char i = s; i <= 'F'; i++)
            {
                if(departures.containsKey(i))
                {
                    total_demand += departures.get(i);
                }
                
                for(Gate g : gates)
                {
                    if(g.getSize() == i)
                    {
                        lhs.addTerm(1, g.flow_ij);
                    }
                }
            }
            
            cplex.addGe(lhs, total_demand);
            
            // this is for arrivals
            lhs = cplex.linearNumExpr();
            total_demand = 0;
            
            for(char i = s; i <= 'F'; i++)
            {
                if(arrivals.containsKey(i))
                {
                    total_demand += arrivals.get(i);
                }
                
                for(Gate g : gates)
                {
                    if(g.getSize() == i)
                    {
                        lhs.addTerm(1, g.flow_ji);
                    }
                }
            }
            
            cplex.addGe(lhs, total_demand);
        }
        
        
        lhs = cplex.linearNumExpr();
        
        for(Runway r : runways)
        {
            lhs.addTerm(1, r.arriving_flow);
        }
        
        cplex.addEq(lhs, total_arriving);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        // objective function -> cost models
        
        // this is for x_3, autonomous vehicles
        
        // Life Cycle: 15 years

        // Model Inputs

        double LC = 15;         // 15 year life cycle

        double ATR = 23.5E6;   // Total runway area in SF
        double ATG = 4.5E6;    // Total Gate area in SF
        double A = ATR + ATG; // Total Airfield area

        double ER = 87;       // current pieces of runway equipment
        double EG = 27;       // current pieces of gate equipment
        double EB = 27;       // current pieces of runway & gate equipment
        double E = ER+EG+EB;      // current total pieces of equipment
        double CER = 702300;        // avg cost of runway equipment
        double CEG = 105600;        // avg cost of gate equipment
        double CEB = 370400;       // avg cost of runway & gate equipment
        double MF = 1.2;           // maintance cost factor
        double ACF = 2;            // autonomous cost factor
        double Deice = 400000;     // Potassium acetate in gal/year
        double CDeice = 25;    // cost of deicing material per gal

        double PM = 76;        // current maintance personnel
        double PR = 20;        // current repair shop personnel
        double P = PM+PR;      // current total personnel
        double CPM = 52000;    // maintance personnel salary per year per person
        double CPR = 104000;    // repair shop personnel salary per year per person

        double INFTY = Integer.MAX_VALUE;
        
        
        
        
        
        
        IloLinearNumExpr AR = cplex.linearNumExpr();
        // calculate runway area
        for(Runway r : runways)
        {
            for(RunwayLink l : r.getLinks())
            {
                lhs.addTerm(l.getArea(), l.x_3);
            }
        }
        
        IloLinearNumExpr AT = cplex.linearNumExpr();
        
        for(Taxiway t : taxiways)
        {
            lhs.addTerm(t.getArea(), t.x_3);
        }
        
        IloLinearNumExpr AG = cplex.linearNumExpr();
        
        for(Gate g : gates)
        {
            AG.addTerm(g.getArea(), g.x_3);
        }
        
        // ER_Req = ceil((AR/ATR)*ER + (AR/A)*EB);        // required 
        // *** ? should the second term be AG/ATG * EG?
        
        // make this an IloIntVar, and make the constraint GE to convert to integer variable
        IloNumVar ER_Req = cplex.numVar(0, INFTY);
        cplex.addEq(ER_Req, cplex.sum(cplex.prod(AR, 1.0/ATR*ER), cplex.sum(AR, 1.0/A*EB)));
        
        
        // PR_Req = ceil((ER_Req/E)*PR);                // personnel required for runway operations
        IloNumVar PR_Req = cplex.numVar(0, INFTY);
        cplex.addEq(PR_Req, cplex.prod(ER_Req, PR/E));
        // DeiceR_Req = (AR/A)*Deice;                  // gal/year deice required for runway 
        IloNumVar DeiceR_Req = cplex.numVar(0, INFTY);
        cplex.addEq(DeiceR_Req, cplex.prod(AR, Deice/A));

        //CRP = (CPM*PR_Req*(PM/P))+(CPR*PR_Req*(PR/P));      // annual cost of personnel for runway operations
        IloNumVar CRP = cplex.numVar(0, INFTY);
        cplex.addEq(CRP, cplex.prod(PR_Req, CPM*PM/P + CPR*PR/P));
        
        //CRE = ((AR/A)*EB*(CEB) + (AR/ATR)*ER*(CER)) * MF * ACF ;        //life time cost of equipment and maintance
        IloNumVar CRE = cplex.numVar(0, INFTY);
        cplex.addEq(CRE, cplex.sum(cplex.prod(AR, EB*CEB/A), cplex.prod(AR, ER*CER/ATR*MF*ACF)));
        
        //CRD = (DeiceR_Req)* 25;                             // annual cost of deicing material 
        IloNumVar CRD = cplex.numVar(0, INFTY);
        cplex.addEq(CRD, cplex.prod(DeiceR_Req, 25));
        


        //CR_Total = 15*(CRP+CRD) + CRE;                      // total cost for life cycle for runway pavement
        IloNumVar CR_Total = cplex.numVar(0, INFTY);
        cplex.addEq(CR_Total, cplex.sum(cplex.prod(LC, cplex.sum(CRP, CRD)), CRE));
        
//// Gate Area

        //EG_Req = ceil((AG/ATG)*EG + (AG/A)*EB);        // required equipment for gate operations, assume same efficieny as current equipment
        IloNumVar EG_Req = cplex.numVar(0, INFTY);
        cplex.addEq(EG_Req, cplex.sum(cplex.prod(AG, EG/ATG), cplex.prod(AG, EB/A)));
        
        //PG_Req = ceil((EG_Req/E)*PR);                // personnel required for gate operations
        IloNumVar PG_Req = cplex.numVar(0, INFTY);
        cplex.addEq(PG_Req, cplex.prod(EG_Req, PR*E));
        
        //DeiceG_Req = (AG/A)*Deice;                  // gal/year deice required for gates
        IloNumVar DeiceG_Req = cplex.numVar(0, INFTY);
        cplex.addEq(DeiceG_Req, cplex.prod(AG, Deice/A));

        //CGP = CPR*PG_Req;                            // annual cost of personnel for gate operations
        IloNumVar CGP = cplex.numVar(0, INFTY);
        cplex.addEq(CGP, cplex.prod(CPR, PG_Req));
        
        //CGE = ((AG/A)*EB*(CEB) + (AG/ATG)*EG*(CEG)) * MF * ACF;        //life time cost of equipment and maintance
        IloNumVar CGE = cplex.numVar(0, INFTY);
        cplex.addEq(CGE, cplex.sum(cplex.prod(AG, EB*CEB/A), cplex.prod(AG, EG*CEG/ATG*MF*ACF)));
        
        //CGD = (DeiceG_Req)* 25;                             // annual cost of deicing material
        IloNumVar CGD = cplex.numVar(0, INFTY);
        cplex.addEq(CGD, cplex.prod(DeiceG_Req, 25));

        //CG_Total = 15*(CGP+CGD) + CGE;                      // total cost for life cycle for gate pavement
        IloNumVar CG_Total = cplex.numVar(0, INFTY);
        cplex.addEq(CG_Total, cplex.sum(cplex.prod(LC, cplex.sum(CGP, CGD)), CGE));
        
        
        IloLinearNumExpr total_flow = cplex.linearNumExpr();
        
        for(Taxiway t : taxiways)
        {
            total_flow.addTerm(0.001, t.flow_ij);
            total_flow.addTerm(0.001, t.flow_ji);
        }
        
        cplex.addMinimize(cplex.sum(total_flow, cplex.sum(CR_Total, CG_Total)));

        
        
        
        
        
        
        
        
        
        
        cplex.solve();
        
        
        System.out.println("Gates\tx\tflow_in\tflow_out");
        for(Gate g : gates)
        {
            System.out.println(g+"\t"+String.format("%.0f", cplex.getValue(g.x))
                    +"\t"+String.format("%.1f", cplex.getValue(g.flow_ji))+"\t"+String.format("%.1f", cplex.getValue(g.flow_ij)));
        }
        
        
        System.out.println("\nTaxiways\tx\tflow_in\tflow_out");
        for(Taxiway t : taxiways)
        {
            System.out.println(t+"\t"+String.format("%.0f", cplex.getValue(t.x))
                    +"\t"+String.format("%.1f", cplex.getValue(t.flow_ij))+"\t"+String.format("%.1f", cplex.getValue(t.flow_ji)));
        }
        
        System.out.println("\nRunways\tx\tdeparting\tarriving");
        for(Runway r : runways)
        {
            System.out.println(r+"\t"+String.format("%.0f", cplex.getValue(r.x))
                    +"\t"+String.format("%.1f", cplex.getValue(r.departing_flow))
                    +"\t"+String.format("%.1f", cplex.getValue(r.arriving_flow)));
        }
    }
}
