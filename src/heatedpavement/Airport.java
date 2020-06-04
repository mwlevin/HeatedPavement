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

        double INFTY = Double.MAX_VALUE;


        //----------------------------------
        //**METHOD 1: snowplows and deicing fluid**
        //for x_1
        //life cycle: 15 years
        //----------------------------------
        
        IloLinearNumExpr AR1 = cplex.linearNumExpr();
        //calculate runway area
        for (Runway r : runways) {
            for(RunwayLink l : r.getLinks()) {
                AR1.addTerm(l.getArea(), l.x_1);
            }
        }
        //calculate taxiway area
        IloLinearNumExpr AT1 = cplex.linearNumExpr();
        for(Taxiway t : taxiways) {
            AT1.addTerm(t.getArea(), t.x_1);
        }
        //calculate gate area
        IloLinearNumExpr AG1 = cplex.linearNumExpr();
        for(Gate g : gates) {
            AG1.addTerm(g.getArea(), g.x_1);
        }

        //Runway pavement:
        //ER_Req1 = ceil((AR1/ATR)*ER + (AR1/A)*EB); --> required equipment for runway operations
        IloIntVar ER_Req1 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(ER_Req1, cplex.sum(cplex.prod(AR1, 1.0/ATR*ER), cplex.prod(AR1, 1.0/A*EB)));
        //PR_Req1 = ceil((ER_Req1/E)*P); --> personnel required for runway operations
        IloIntVar PR_Req1 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(PR_Req1, cplex.prod(ER_Req1, P/E));
        //DeiceR_Req1 = (AR1/A)*Deice; --> gal/year deice required for runway
        IloNumVar DeiceR_Req1 = cplex.numVar(0, INFTY);
        cplex.addEq(DeiceR_Req1, cplex.prod(AR1, Deice/A));
        //CRP1 = (CPM*PR_Req1*(PM/P))+(CPR*PR_Req1*(PR/P)); --> annual cost of personnel for runway operations
        IloNumVar CRP1 = cplex.numVar(0, INFTY);
        cplex.addEq(CRP1, cplex.sum(cplex.prod(CPM, cplex.prod(PR_Req1, PM/P)), cplex.prod(CPR, cplex.prod(PR_Req1, PR/P))));
        //CRE1 = ((AR1/A)*EB*(CEB) + (AR1/ATR)*ER*(CER)) * MF1; --> life time cost of equipment and maintenance
        IloNumVar CRE1 = cplex.numVar(0, INFTY);
        cplex.addEq(CRE1, cplex.prod(cplex.sum(cplex.prod(AR1, 1.0/A*EB*CEB), cplex.prod(AR1, 1.0/ATR*ER*CER)), MF));
        //CRD1 = (DeiceR_Req1)* 25; --> annual cost of deicing material
        IloNumVar CRD1 = cplex.numVar(0, INFTY);
        cplex.addEq(CRD1, cplex.prod(DeiceR_Req1, 25));
        //CR_Total1 = 15*(CRP1+CRD1) + CRE1; --> total cost for life cycle for runway pavement
        
        IloNumVar CR_Total1 = cplex.numVar(0, INFTY);
        
        cplex.addEq(CR_Total1, cplex.sum(cplex.prod(cplex.sum(CRP1, CRD1), 15), CRE1));

        //Gate pavement:
        //EG_Req1 = ceil((AG1/ATG)*EG + (AG1/A)*EB); --> required equipment for gate operations
        IloIntVar EG_Req1 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(EG_Req1, cplex.sum(cplex.prod(AG1, EG/ATG), cplex.prod(AG1, EB/A)));
        //PG_Req1 = ceil((EG_Req1/E)*P); --> personnel required for gate operations
        IloIntVar PG_Req1 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(PG_Req1, cplex.prod(EG_Req1, P/E));
        //DeiceG_Req1 = (AG1/A)*Deice; --> gal/year deice required for gates
        IloNumVar DeiceG_Req1 = cplex.numVar(0, INFTY);
        cplex.addEq(DeiceG_Req1, cplex.prod(AG1, Deice/A));
        //CGP1 = (CPM*PG_Req1*(PM/P))+(CPR*PG_Req1*(PR/P)); --> annual cost of personnel for gate operations
        IloNumVar CGP1 = cplex.numVar(0, INFTY);
        cplex.addEq(CGP1, cplex.sum(cplex.prod(CPM, cplex.prod(PG_Req1, PM/P)), cplex.prod(CPR, cplex.prod(PG_Req1, PR/P))));
        //CGE1 = ((AG1/A)*EB*(CEB) + (AG1/ATG)*EG*(CEG)) * MF1; --> life time cost of equipment and maintenance
        IloNumVar CGE1 = cplex.numVar(0, INFTY);
        cplex.addEq(CGE1, cplex.prod(cplex.sum(cplex.prod(AG1, 1.0/A*EB*CEB), cplex.prod(AG1, 1.0/ATG*EG*CEG)), MF));
        //CGD1 = (DeiceG_Req1)* 25; --> annual cost of deicing
        IloNumVar CGD1 = cplex.numVar(0, INFTY);
        cplex.addEq(CGD1, cplex.prod(DeiceG_Req1, 25));
        //CG_Total1 = 15*(CGP1+CGD1) + CGE1; --> total cost for life cycle for gate pavement
        IloNumVar CG_Total1 = cplex.numVar(0, INFTY);
        cplex.addEq(CG_Total1, cplex.sum(cplex.prod(15, cplex.sum(CGP1, CGD1)), CGE1));


        //----------------------------------
        //**METHOD 2: autonomous snowplows and deicing fluid**
        //----------------------------------
        // objective function -> cost models

        // this is for x_2, autonomous vehicles

        // Life Cycle: 15 years
        
        IloLinearNumExpr AR2 = cplex.linearNumExpr();
        // calculate runway area
        for(Runway r : runways)
        {
            for(RunwayLink l : r.getLinks())
            {
                AR2.addTerm(l.getArea(), l.x_2);
            }
        }

        IloLinearNumExpr AT2 = cplex.linearNumExpr();

        for(Taxiway t : taxiways)
        {
            AT2.addTerm(t.getArea(), t.x_2);
        }

        IloLinearNumExpr AG2 = cplex.linearNumExpr();

        for(Gate g : gates)
        {
            AG2.addTerm(g.getArea(), g.x_2);
        }

        //Runway area:
        // ER_Req = ceil((AR2/ATR)*ER + (AR2/A)*EB);        // required
        // *** ? should the second term be AG/ATG * EG?

        // make this an IloIntVar, and make the constraint GE to convert to integer variable
        IloIntVar ER_Req2 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(ER_Req2, cplex.sum(cplex.prod(AR2, 1.0/ATR*ER), cplex.prod(AR2, 1.0/A*EB)));


        // PR_Req2 = ceil((ER_Req2/E)*PR);                // personnel required for runway operations
        IloIntVar PR_Req2 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(PR_Req2, cplex.prod(ER_Req2, PR/E));
        // DeiceR_Req2 = (AR/A)*Deice;                  // gal/year deice required for runway
        IloNumVar DeiceR_Req2 = cplex.numVar(0, INFTY);
        cplex.addEq(DeiceR_Req2, cplex.prod(AR2, Deice/A));

        //CRP2 = (CPM*PR_Req2*(PM/P))+(CPR*PR_Req2*(PR/P));      // annual cost of personnel for runway operations
        IloNumVar CRP2 = cplex.numVar(0, INFTY);
        cplex.addEq(CRP2, cplex.prod(PR_Req2, CPM*PM/P + CPR*PR/P));

        //CRE2 = ((AR2/A)*EB*(CEB) + (AR2/ATR)*ER*(CER)) * MF2 * ACF ;        //life time cost of equipment and maintance
        IloNumVar CRE2 = cplex.numVar(0, INFTY);
        cplex.addEq(CRE2, cplex.prod(cplex.prod(cplex.sum(cplex.prod(AR2, EB*CEB/A), cplex.prod(AR2, ER*CER/ATR)), MF), ACF));

        //CRD2 = (DeiceR_Req2)* 25;                             // annual cost of deicing material
        IloNumVar CRD2 = cplex.numVar(0, INFTY);
        cplex.addEq(CRD2, cplex.prod(DeiceR_Req2, 25));

        //CR_Total2 = 15*(CRP2+CRD2) + CRE2;                      // total cost for life cycle for runway pavement
        IloNumVar CR_Total2 = cplex.numVar(0, INFTY);
        cplex.addEq(CR_Total2, cplex.sum(cplex.prod(LC, cplex.sum(CRP2, CRD2)), CRE2));

        //// Gate Area

        //EG_Req2 = ceil((AG2/ATG)*EG + (AG2/A)*EB);        // required equipment for gate operations, assume same efficiency as current equipment
        IloIntVar EG_Req2 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(EG_Req2, cplex.sum(cplex.prod(AG2, EG/ATG), cplex.prod(AG2, EB/A)));

        //PG_Req2 = ceil((EG_Req2/E)*PR);                // personnel required for gate operations
        IloIntVar PG_Req2 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(PG_Req2, cplex.prod(EG_Req2, PR/E));

        //DeiceG_Req2 = (AG2/A)*Deice;                  // gal/year deice required for gates
        IloNumVar DeiceG_Req2 = cplex.numVar(0, INFTY);
        cplex.addEq(DeiceG_Req2, cplex.prod(AG2, Deice/A));

        //CGP2 = CPR*PG_Req2;                            // annual cost of personnel for gate operations
        IloNumVar CGP2 = cplex.numVar(0, INFTY);
        cplex.addEq(CGP2, cplex.prod(CPR, PG_Req2));

        //CGE2 = ((AG2/A)*EB*(CEB) + (AG2/ATG)*EG*(CEG)) * MF2 * ACF;        //life time cost of equipment and maintance
        IloNumVar CGE2 = cplex.numVar(0, INFTY);
        cplex.addEq(CGE2, cplex.prod(cplex.prod(cplex.sum(cplex.prod(AG2, EB*CEB/A), cplex.prod(AG2, EG*CEG/ATG)), MF), ACF));

        //CGD2 = (DeiceG_Req2)* 25;                             // annual cost of deicing material
        IloNumVar CGD2 = cplex.numVar(0, INFTY);
        cplex.addEq(CGD2, cplex.prod(DeiceG_Req2, 25));

        //CG_Total = 15*(CGP+CGD) + CGE;                      // total cost for life cycle for gate pavement
        IloNumVar CG_Total2 = cplex.numVar(0, INFTY);
        cplex.addEq(CG_Total2, cplex.sum(cplex.prod(LC, cplex.sum(CGP2, CGD2)), CGE2));

        
        


        //----------------------------------
        //**METHOD 3: heated pavements**
        //for x_3
        //life cycle: 20 years
        //----------------------------------
        IloLinearNumExpr AR3 = cplex.linearNumExpr();
        //calculate runway area
        for (Runway r : runways) {
            for(RunwayLink l : r.getLinks()) {
                AR3.addTerm(l.getArea(), l.x_3);
            }
        }
        //calculate taxiway area
        IloLinearNumExpr AT3 = cplex.linearNumExpr();
        for(Taxiway t : taxiways) {
            AT3.addTerm(t.getArea(), t.x_3);
        }
        //calculate gate area
        IloLinearNumExpr AG3 = cplex.linearNumExpr();
        for(Gate g : gates) {
            AG3.addTerm(g.getArea(), g.x_3);
        }
        //Model inputs
        double LC3 = 20;    //20 year life cycle
        double MFP = 1.05;  //maintenance cost factor for hydronic system
        double CHP = 15;    //cost of hydronic pavement ($/SF)
        /*
        ESI = readmatrix('energysnowinfo.xlsx','Range', 'B3:D8');      % table of energy and storm information
        TER = ESI(:,3) * (AH) * 0.277778 ;     % Total Energy Requirenmet of Electrical Heating (kwh)
        ECS = TER * CEE;            % Energy cost per storm ($)
        EC = ECS.* ESI(:,1);        % total energy cost matrix
        TEC = sum(EC);              % total energy cost ($/year)
        */
        double TEC = 1312398.71+ 521979.36+1102495.15+529704.75+254456.34+373947.67;

        //Runway pavement:
        //ER_Req3 = ceil(0.15*((AR3/ATR)*ER + (AR3/A)*EB)); --> required equipment for runway operations, keep 15% of current equipment in case
        IloIntVar ER_Req3 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(ER_Req3, cplex.prod(cplex.sum(cplex.prod(AR3, 1.0/ATR*ER), cplex.prod(AR3, 1.0/A*EB)), .15));
        //PR_Req3 = ceil((ER_Req3/E)*P); --> personnel required for runway operations
        IloIntVar PR_Req3 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(PR_Req3, cplex.prod(ER_Req3, P/E));
        //CRP3 = (CPM*PR_Req3*(PM/P))+(CPR*PR_Req3*(PR/P)); --> annual cost of personnel for runway operations
        IloNumVar CRP3 = cplex.numVar(0, INFTY);
        cplex.addEq(CRP3, cplex.sum(cplex.prod(cplex.prod(CPM, PR_Req3), PM/P), cplex.prod(cplex.prod(CPR, PR_Req3), PR/P)));
        //CRE3 = 0.15*(((AR3/A)*EB*(CEB) + (AR3/ATR)*ER*(CER)) * MFE3); --> life time cost of equipment and maintenance
        IloNumVar CRE3 = cplex.numVar(0, INFTY);
        cplex.addEq(CRE3, cplex.prod(.15, cplex.sum(cplex.prod(AR3, EB*CEB/A), cplex.prod(AR3, ER*CER/ATR*MF))));
        //CRHP3 =  MFP*(CHP * AR3); --> cost of hydronic pavement construction
        IloNumVar CRHP3 = cplex.numVar(0, INFTY);
        cplex.addEq(CRHP3, cplex.prod(MFP, cplex.prod(CHP, AR3)));
        //CREE3 = TEC *(AR3/AH); --> energy cost for runway area
        IloNumVar CREE3 = cplex.numVar(0, INFTY);
        cplex.addEq(CREE3, TEC*ATR/A);
        //CR_Total3 = LC3*(CRP3 + CREE3) + CRE3 + CRHP3; --> total cost for life cycle for runway pavement
        IloNumVar CR_Total3 = cplex.numVar(0, INFTY);
        cplex.addEq(CR_Total3, cplex.sum(cplex.prod(LC3, cplex.sum(CRP3, CREE3)), cplex.sum(CRE3, CRHP3)));

        //Gate pavement:
        //EG_Req3 = ceil(0.05*((AG3/ATG)*EG + (AG3/A)*EB)); --> required equipment for gate operations, keep 5% of current equipment in case
        IloIntVar EG_Req3 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(EG_Req3, cplex.prod(cplex.sum(cplex.prod(AG3, 1.0/ATG*EG), cplex.prod(AG3, 1.0/A*EB)), .05));
        //PG_Req3 = ceil((EG_Req3/E)*PR); --> personnel required for gate operations
        IloIntVar PG_Req3 = cplex.intVar(0, (int) INFTY);
        cplex.addGe(PG_Req3, cplex.prod(EG_Req3, P/E));
        /*if PG_Req3<= 2 && AG3 > 0
                PG_Req3 = 2;
          else
                PG_Req3 = ceil((ER_Req3/E)*PR);*/
        //CGP3 = CPR*PG_Req3; --> annual cost of personnel for gate operations
        IloNumVar CGP3 = cplex.numVar(0, INFTY);
        cplex.addEq(CGP3, cplex.prod(CPR, PG_Req3));
        //CGE3 = 0.05*(((AG3/A)*EB*(CEB) + (AG3/ATG)*EG*(CEG)) * MFE3); --> life time cost of equipment and maintenance
        IloNumVar CGE3 = cplex.numVar(0, INFTY);
        cplex.addEq(CGE3, cplex.prod(.05, cplex.sum(cplex.prod(AG3, EB*CEB/A), cplex.prod(AG3, 1.0/ATG*EG*CEG*MF))));
        //CGHP3 =  MFP*(CHP * AG3); --> cost of hydronic pavement construction
        IloNumVar CGHP3 = cplex.numVar(0, INFTY);
        cplex.addEq(CGHP3, cplex.prod(MFP, cplex.prod(CHP, AG3)));
        //CGEE3 = TEC *(AG3/AH); --> energy cost for gate area
        IloNumVar CGEE3 = cplex.numVar(0, INFTY);
        cplex.addEq(CGEE3, cplex.prod(1.0/A, cplex.prod(TEC, AG3))); //FIX THIS
        //CG_Total3 = 15*(CGP3 +CGEE3) + CGHP3 + CGE3; --> total cost for life cycle for gate pavement
        IloNumVar CG_Total3 = cplex.numVar(0, INFTY);
        cplex.addEq(CG_Total3, cplex.sum(cplex.prod(15, cplex.sum(CGP3, CGEE3)), cplex.sum(CGHP3, CGE3)));
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        IloLinearNumExpr obj = cplex.linearNumExpr();
        obj.addTerm(1, CR_Total1);
        obj.addTerm(1, CG_Total1);
        obj.addTerm(1, CR_Total2);
        obj.addTerm(1, CG_Total2);
        obj.addTerm(1, CR_Total3);
        obj.addTerm(1, CG_Total3);
                
        for(Taxiway t : taxiways)
        {
            obj.addTerm(0.001, t.flow_ij);
            obj.addTerm(0.001, t.flow_ji);
        }

        cplex.addMinimize(obj);
        
        
        
        

        cplex.solve();

        System.out.println("Method 1 runway cost: " + cplex.getValue(CR_Total1));
        System.out.println("Method 1 gate cost: " + cplex.getValue(CG_Total1));
        System.out.println("Method 2 runway cost: " + cplex.getValue(CR_Total2));
        System.out.println("Method 2 gate cost: " + cplex.getValue(CG_Total2));
        System.out.println("Method 3 runway cost: " + cplex.getValue(CR_Total3));
        System.out.println("Method 3 gate cost: " + cplex.getValue(CG_Total3));
        System.out.println();
        System.out.println("Runway area for current method: " + cplex.getValue(AR2));
        System.out.println("Gate area for current method: " + cplex.getValue(AG2));
        System.out.println();
        System.out.println("ATR: " + ATR);
        System.out.println("A: " + A);
        System.out.println("ER_Req: " + cplex.getValue(ER_Req3));
        System.out.println("PR_Req: " + cplex.getValue(PR_Req3));
        //System.out.println("DeiceR_req: " + cplex.getValue(DeiceR_Req3));
        System.out.println("CRP: " + cplex.getValue(CRP3));
        System.out.println("CRE: " + cplex.getValue(CRE3));
        System.out.println("CRD: " + cplex.getValue(CRHP3));
        System.out.println("CREE: " + cplex.getValue(CREE3));
        System.out.println("EG_Req: " +cplex.getValue(EG_Req3));
        System.out.println("PG_Req: " + cplex.getValue(PG_Req3));
        System.out.println("CGP: " + cplex.getValue(CGP3));
        System.out.println("CGE: " + cplex.getValue(CGE3));
        System.out.println("CGHP: " + cplex.getValue(CGHP3));
        System.out.println("CGEE: " + cplex.getValue(CGEE3));
        System.out.println("TEC: " + TEC);

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
