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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
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

/**
 *
 * @author ml26893
 */
public class MapViewer extends JMapViewer
{
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};

    private Set<Node> nodes;
    private Set<Link> links;
    
    
    

    private int time;
    
    private int scale;

    public MapViewer(int viewWidth, int viewHeight)
    {
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        nodes = new HashSet<Node>();
        links = new HashSet<Link>();
        
        scale = 1;
        
        setFont(new Font("Arial", Font.BOLD, 14));
        
    }

    
    
    public void setScale(int scale)
    {
        this.scale = scale;
    }
    

    

    
    public void center(Location n)
    {
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), n.getCoordinate(), getZoom());
    }
    
    

    
    public void setZoomControlsVisible(boolean visible) {
        super.setZoomContolsVisible(visible);
    }
    
    protected void paintComponent(Graphics window) 
    {
        Graphics2D g = (Graphics2D)window;
        
        super.paintComponent(g);

       // draw links here, use Graphics drawing tools
       // use getMapPosition(Location, false) to get the (x,y) coordinate
    }
    
    
    
    public void paintText(Graphics g, String name, Point position, int radius) {

        if (name != null && g != null && position != null) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x+radius+2, position.y+radius);
        }
    }

}
