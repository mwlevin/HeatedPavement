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
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.add(new MapViewer(500, 500));
        frame.pack();
        frame.setVisible(true);
    }
    private int scale;

    public MapViewer(int viewWidth, int viewHeight)
    {
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        scale = 1;
        
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
    
    protected void paintComponent(Graphics window) {
        Graphics2D g = (Graphics2D)window;
        super.paintComponent(g);
        
        g.setStroke(new BasicStroke(5));
        Airport airport = null;
        try {
            airport = new Airport("MSP_nw");
            airport.solveCplex();
        }
        catch (Exception e) {
            System.out.println("exception caught");
        }
        for (Map.Entry<String, Link> entry : airport.lookupLink.entrySet()) {
            g.setColor(Color.black);
            Link l = entry.getValue();
            System.out.println("x1: " + l.value_x1);
            System.out.println("x2: " + l.value_x2);
            System.out.println("x3: " + l.value_x3);
            if (l.value_x1 == 1) {
                g.setColor(Color.red);
            }
            if (l.value_x2 == 1) {
                g.setColor(Color.white);
            }
            if (l.value_x3 == 1) {
                g.setColor(Color.blue);
            }
            Point start = getMapPosition(l.source.coordinates, false);
            Point end = getMapPosition(l.dest.coordinates, false);
            g.drawLine(start.x, start.y, end.x, end.y);
        }
        
        Scanner in = null;
        try {
           in = new Scanner(new File("airports/MSP_nw/gate_coordinates.txt")); 
        }
        catch (Exception e) {
            System.out.println("exception caught");
        }
        in.nextLine();
        while (in.hasNext()) {
            g.setColor(Color.black);
            in.next();
            double latitude = in.nextDouble();
            double longitude = in.nextDouble();
            Coordinate coordinate = new Coordinate(latitude, longitude);
            Point gate = getMapPosition(coordinate, false);
            for (Gate gates : airport.gates) {
                if (gates.value_x1 == 1) {
                    g.setColor(Color.red);
                }
                if (gates.value_x2 == 1) {
                    g.setColor(Color.white);
                }
                if (gates.value_x3 == 1) {
                    g.setColor(Color.blue);
                }
            }
            g.drawOval(gate.x, gate.y, 2, 2);
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
