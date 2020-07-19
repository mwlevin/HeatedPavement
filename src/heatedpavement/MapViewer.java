/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package heatedpavement;


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.Timer;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import static org.openstreetmap.gui.jmapviewer.JMapViewer.MIN_ZOOM;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author ml26893
 */
public class MapViewer extends JMapViewer
{
    public static void main(String[] args) throws Exception
    {
        Airport airport = new Airport("MSP_temp"); //change this to change the airport
        airport.solveCplex();
        
        
        JFrame frame = new JFrame();
        frame.add(new MapViewer(airport, 500, 500));
        frame.pack();
        frame.setVisible(true);
    }
    private int scale;
    
    Airport airport;

    public MapViewer(Airport airport, int viewWidth, int viewHeight)
    {
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        scale = 1;
        
        this.airport = airport;
        
        setFont(new Font("Arial", Font.BOLD, 14));
        
        Coordinate startDisplay = new Coordinate(44.8830, -93.2230);
        setDisplayPosition(startDisplay, 14);
    } 
    
    public void setScale(int scale)
    {
        this.scale = scale;
    }
    
    public void center(Coordinate c)
    {
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), c, getZoom());
    }
    
    public void setZoomControlsVisible(boolean visible) {
        super.setZoomContolsVisible(visible);
    }
    
    //Selecting a color based on y value
    protected Color yColor(double y) {
        if (y< 0.5) { //=0, gray
            Color min = new Color(61, 61, 61);
            return min;
        }
        else if (y <= 1) { //=1, purple
            Color one = new Color(153, 51, 155);
            return one;
        }
        else if (y <= 2) { //=2, green
            Color two = new Color(6, 215, 160);
            return two;
        }
        else if (y <= 3) { //=3, red
            Color three = new Color(239, 71, 111);
            return three;
        }
        else if (y > 3 && y < 8) { //4-7, yellow
            Color middle2 = new Color(255, 209, 102);
            return middle2;
        }
        else if (y > 7 && y < 12) { //8-11, orange
            Color middle3 = new Color(255, 126, 62);
            return middle3;
        }
        Color max = new Color(38, 84, 124); //>11, blue
        return max;
    }
    
    protected void paintComponent(Graphics window) {
        Graphics2D g = (Graphics2D)window;
        super.paintComponent(g);
        
        boolean colorcodeY = true; //boolean to see if we should color code based on y or x
        boolean lookAtY1 = true; //color code based on only y1 values
        boolean lookAtY2 = false; //color code based on only y2 values
        boolean lookAtBoth = true; //color code based on sum y1+y2.
                                    //If this is true, lookAtY1 and lookAtY2 should be false
        g.setStroke(new BasicStroke(5));
        
        for (Map.Entry<String, Link> entry : airport.lookupLink.entrySet()) {
            g.setColor(Color.black);
            Link l = entry.getValue();
            if (colorcodeY) {
                //System.out.println("y1: " + l.value_y1);
                //System.out.println("y2: " + l.value_y2);
                if (lookAtY1) {
                    g.setColor(yColor(l.value_y1));
                }
                if (lookAtY2) {
                    g.setColor(yColor(l.value_y2));
                }
                if (lookAtBoth) {
                    g.setColor(yColor(l.value_yBoth));
                }
            }
            else {
                //System.out.println("x1: " + l.value_x1);
                //System.out.println("x2: " + l.value_x2);
                //System.out.println("x3: " + l.value_x3);
                
                if (l.value_y1 > 0) {
                    g.setColor(Color.red);
                }
                if (l.value_y2 > 0) {
                    g.setColor(Color.green);
                }
                if (l.value_x3 > 0) {
                    g.setColor(Color.blue);
                }  
            }
            Point start = getMapPosition(l.source.coordinates, false);
            Point end = getMapPosition(l.dest.coordinates, false);
            g.drawLine(start.x, start.y, end.x, end.y);
        }
        
        //drawing gates
        Scanner in = null;
        try {
           in = new Scanner(new File("airports/MSP_temp/gate_coordinates.txt")); 
        }
        catch (Exception e) {
            System.out.println("exception caught");
            e.printStackTrace(System.err);
        }
        if (in != null) {
            in.nextLine();
            while (in.hasNext()) {
                
                String name = in.next();
                double latitude = in.nextDouble();
                double longitude = in.nextDouble();
                Coordinate coordinate = new Coordinate(latitude, longitude);
                Point gate = getMapPosition(coordinate, false);
                
                
                for (Gate gates : airport.gates) {
                    
                    if(gates.getName().equals(name))
                    {

                        g.setColor(Color.black);


                        if (colorcodeY) {
                            if (lookAtY1) {
                                g.setColor(yColor(gates.value_y1));
                            }
                            if (lookAtY2) {
                                g.setColor(yColor(gates.value_y2));
                            }
                            if (lookAtBoth) {
                                g.setColor(yColor(gates.value_yBoth));
                            }
                        }
                        else {
                            if (gates.value_y1 > 0) {
                                g.setColor(Color.red);
                            }
                            if (gates.value_y2 > 0) {
                                g.setColor(Color.green);
                            }
                            if (gates.value_x3 > 0) {
                                g.setColor(Color.blue);
                            }
                        }
                        break;
                    }
                }
            g.drawOval(gate.x, gate.y, 2, 2);
            }
        }
        
    }
    
    public void paintText(Graphics g, String name, Point position, int radius) {

        if (name != null && g != null && position != null) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x+radius+2, position.y+radius);
        }
    }

}
