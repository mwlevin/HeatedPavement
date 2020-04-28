/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

/**
 *
 * @author micha
 */
public class RunwayLink extends Link
{
    public RunwayLink(String name, Node source, Node dest, double area)
    {
        super(name, source, dest, area);
    }
    
    public Type getType()
    {
        return Type.runway;
    }
}
