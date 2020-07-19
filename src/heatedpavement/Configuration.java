/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alison
 */
public class Configuration {
    protected List<Runway> activeRunways;
    protected String name;
    
    public Configuration(String n, List runways) {
        this.name = n;
        this.activeRunways = new ArrayList<>(runways);
    }
}
