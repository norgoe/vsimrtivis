
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;


/**
 * A simple sample application that uses JXMapKit based on example 4 and 6 of
 * Martin Steigers JXMapViewer2 examples
 *
 * @author Norbert Goebel
 */
public class VSimRTIVis {

    public static final String MOVE_VEHICLE = "MOVE_VEHICLE";
    public static final String RECV_MESSAGE = "RECV_MESSAGE";
    public static HashMap<String, VehicleInformation> vehicles = new HashMap<>();
    public static WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<>();
    public static Set<MyWaypoint> waypoints = new HashSet<>();
    public static final JXMapKit jXMapKit = new JXMapKit();
    public static final JLabel labelTimeNs = new JLabel("time [s]: ");

    public static void updateWaypoints(long curtimens) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Iterator it = vehicles.values().iterator();
                waypoints.clear();
                while (it.hasNext()) {
                    VehicleInformation v = (VehicleInformation) it.next();
                    //System.out.println(v.toString());
                    waypoints.add(new MyWaypoint(v.getId(), v.getColor(curtimens), v.getGeoPosition()));
                }
                waypointPainter.setWaypoints(waypoints);
                labelTimeNs.setText("time [ns]: " + curtimens / 1000000000.0);
                //labelTimeNs.repaint();
                jXMapKit.repaint();
            }
        });

        try {
            Thread.sleep(2);
        } catch (InterruptedException ex) {
            Logger.getLogger(VSimRTIVis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the program args (ignored)
     */
    public static void main(String[] args) throws IOException {

        //final JXMapKit jXMapKit = new JXMapKit();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        jXMapKit.setTileFactory(tileFactory);

        // Setup local file cache
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

        //location of Java
        final GeoPosition gp = new GeoPosition(51.167, 6.7515);

        // Set the focus
        jXMapKit.getMainMap().setCenterPosition(gp);
        jXMapKit.getMainMap().setZoom(2);

        final JToolTip tooltip = new JToolTip();
        tooltip.setTipText("Taubental");
        tooltip.setComponent(jXMapKit.getMainMap());
        jXMapKit.getMainMap().add(tooltip);

        jXMapKit.setAddressLocation(gp);

        jXMapKit.getMainMap().addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // ignore
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                JXMapViewer map = jXMapKit.getMainMap();

                // convert to world bitmap
                Point2D worldPos = map.getTileFactory().geoToPixel(gp, map.getZoom());

                // convert to screen
                Rectangle rect = map.getViewportBounds();
                int sx = (int) worldPos.getX() - rect.x;
                int sy = (int) worldPos.getY() - rect.y;
                Point screenPos = new Point(sx, sy);

                // check if near the mouse
                if (screenPos.distance(e.getPoint()) < 20) {
                    screenPos.x -= tooltip.getWidth() / 2;

                    tooltip.setLocation(screenPos);
                    tooltip.setVisible(true);
                } else {
                    tooltip.setVisible(false);
                }
            }
        });

        // Display the viewer in a JFrame
        JFrame frame = new JFrame("JXMapviewer2 showing vstimrti visualizer positions");
        frame.setLayout(new BorderLayout());
        frame.add(jXMapKit);
        frame.add(labelTimeNs, BorderLayout.SOUTH);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Create a waypoint painter that takes all the waypoints
        waypointPainter.setRenderer(new FancyWaypointRenderer());

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        jXMapKit.getMainMap().setOverlayPainter(painter);

        //String file = "/tmp/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/korrekt-mapping-log-20151117-145801-Taubental-large-etsi/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/korrekt-mapping-log-20151117-134001-Taubental-large/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20151119-073220-Taubental-large-etsi/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20151119-092412-Taubental-large/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20151119-112619-Taubental-large/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20151119-123727-Taubental-large/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20151119-130259-Taubental-large/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20160221-091950-Taubental-large/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20160221-124016-Taubental-large/visualizer.csv";
        //String file = "/home/goebel/vsimrti0.15/logs/log-20160221-134255-Taubental-large/visualizer.csv";
        String file = "/home/goebel/vsimrti0.15/logs/log-20160221-142005-Taubental-large/visualizer.csv";
        
        

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String[] splits;
            long lasttime = 0;
            long curtime = 0;
            while (true) {
                while ((line = br.readLine()) != null) {
                    splits = line.split(";");
                    curtime = Long.parseLong(splits[1]);
                    if (curtime > lasttime) {
                        lasttime = curtime;
                        updateWaypoints(curtime);
                    }
                    if (splits[0].equals(MOVE_VEHICLE)) {
                        if (vehicles.containsKey(splits[2])) {
                            vehicles.get(splits[2]).update(splits);
                        } else {
                            vehicles.put(splits[2], new VehicleInformation(splits));
                        }
                    } else if (splits[0].equals(RECV_MESSAGE) && splits[4].equals("EmergencyWarningMessage")) {
                        if (!vehicles.containsKey(splits[3])) {
                            System.err.println("No vehicle for message " + line + " in simulation!");
                            System.err.println(vehicles.keySet().toString());
                        } else {
                            vehicles.get(splits[3]).emergencyMessageReceived(Long.parseLong(splits[1]), Long.parseLong(splits[2]));
                        }
                        //RECV_MESSAGE;112105156375;379;veh_4;EmergencyWarningMessage
                    } else {
                        //System.out.println(line);
                    }

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VSimRTIVis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
