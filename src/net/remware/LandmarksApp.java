package net.remware;

import com.esri.core.geometry.*;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.map.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

/**
 * This application shows a ({@link JMap})  highlighting landmarks spots.
 *  <p>
 *
 */
public class LandmarksApp {

    private JMap map = new JMap();
    private static DecimalFormat decimalFormat = new DecimalFormat("##.###");
    private JTextArea jTextArea;

    private class MouseMoveOverlay extends MapOverlay {
        private static final long serialVersionUID = 1L;

        @Override
        public void onMouseMoved(MouseEvent arg0) {
            try {
                if (!map.isReady()) {
                    return;
                }

                java.awt.Point screenPoint = arg0.getPoint();
                com.esri.core.geometry.Point mapPoint = map.toMapPoint(screenPoint.x, screenPoint.y);

                String screenCoords = "Screen Coordinates: X = " + screenPoint.x
                        + ", Y = " + screenPoint.y;
                String mapCoords = "Map Coordinates: X = " + decimalFormat.format(mapPoint.getX())
                        + ", Y = " + decimalFormat.format(mapPoint.getY());
                String decimalDegrees = "Decimal Degrees: "
                        + CoordinateConversion.pointToDecimalDegrees(mapPoint, map.getSpatialReference(), 2);
                String degreesDecimalMinutes = "Degrees Decimal Minutes: "
                        + CoordinateConversion.pointToDegreesDecimalMinutes(mapPoint, map.getSpatialReference(), 2);
                String degreesMinutesSeconds = "Degrees Minutes Seconds: "
                        + CoordinateConversion.pointToDegreesMinutesSeconds(mapPoint, map.getSpatialReference(), 2);

                jTextArea.setText(
                        screenCoords + "\n" +
                                mapCoords + "\n" +
                                decimalDegrees + "\n" +
                                degreesDecimalMinutes + "\n" +
                                degreesMinutesSeconds );

            } finally {
                super.onMouseMoved(arg0);
            }
        }
    }

    private JMap createMap() {

        final JMap jMap = new JMap();

        jMap.addMapOverlay(new MouseMoveOverlay());
        jMap.setExtent(new Envelope(2626058, 8740679, 2629717, 8739602 ));

        // add tiled map service layer
        ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
                "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");

        jMap.getLayers().add(tiledLayer);

        final GraphicsLayer graphicsLayer = new GraphicsLayer();
        graphicsLayer.setName("Tampere Mustavuori");
        // add graphics layer to map
        jMap.getLayers().add(graphicsLayer);

        jMap.addMapEventListener(new MapEventListener() {

            @Override
            public void mapReady(MapEvent event) {
                // get the spatial reference from the map that fired this event
                SpatialReference mapSR = event.getMap().getSpatialReference();
                // print the spatial reference's ID
                System.out.println("The map spatial reference is wkid=" + mapSR.getID());

                // create a Point in the map's spatial reference from lon, lat coordinates on known reference
                SpatialReference knownSR = SpatialReference.create(4326);
                com.esri.core.geometry.Point point = (Point) GeometryEngine.project( new Point( 23.6061, 61.4947),knownSR,mapSR);

                // create a PictureMarkerSymbol from a URL
                PictureMarkerSymbol symbol = new PictureMarkerSymbol(
                        "http://static.arcgis.com/images/Symbols/Basic/RedShinyPin.png");

                // create a graphic using the point and symbol
                Graphic pointGraphic = new Graphic(point, symbol);

                // add the graphic to your graphics layer
                graphicsLayer.addGraphic(pointGraphic);
            }

            @Override
            public void mapExtentChanged(MapEvent event) {
                // TODO Auto-generated method stub
            }

            @Override
            public void mapDispose(MapEvent event) {
                // TODO Auto-generated method stub
            }
        });

        return jMap;
    }

    public  JComponent createUI() {

        JLayeredPane contentPane = new JLayeredPane();
        contentPane.setBounds(100, 100, 1000, 700);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setVisible(true);

        map = createMap();

        contentPane.add(map, BorderLayout.CENTER);

        if (jTextArea == null) {
            jTextArea = new JTextArea(5,1);
            jTextArea.setFont(new Font(jTextArea.getFont().getName(), Font.PLAIN, 12));
            jTextArea.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
            jTextArea.setEditable(false);
            jTextArea.setText("Map coordinates will display here, move the mouse cursor around the map.");
        }
        contentPane.add(jTextArea, BorderLayout.SOUTH);
        return contentPane;
    }

    private JFrame createWindow() {

        JFrame window = new JFrame("Tampere Map Application");
        window.setSize(800, 600);
        window.setLocationRelativeTo(null); // center on screen
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().setLayout(new BorderLayout(0, 0));
        window.setVisible(true);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                map.dispose();
            }
        });
        return window;
    }

    public static void main(String[] args) {

        LandmarksApp myApp = new LandmarksApp();

        JFrame appWindow = myApp.createWindow();

        appWindow.setContentPane(myApp.createUI());

    }
}
