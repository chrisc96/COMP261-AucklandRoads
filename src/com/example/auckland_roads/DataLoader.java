package com.example.auckland_roads;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import static com.example.auckland_roads.RoadMap.map;

public class DataLoader {

    private boolean dataSmall = true; // If false, load big data files
    private BufferedReader data;

    private Map<Integer,Node> loadNodeMap = new HashMap<>();
    private Map<Integer,Road> loadRoadMap = new HashMap<>();
    private List<Segment> loadSegmentList = new ArrayList<>();
    private List<Polygon> polygonList = new ArrayList<>();

    public Map<Integer, Node> parseNodes(File nodes) {
        try {
            data = new BufferedReader(new FileReader(nodes));
            String line;

            while ((line = data.readLine()) != null) {
                String[] values = line.split("\t");
                int nodeID = Integer.parseInt(values[0]);
                Node node = new Node(nodeID, Double.parseDouble(values[1]), Double.parseDouble(values[2]), new Color(183, 183, 183, 0));
                loadNodeMap.put(nodeID, node);
            }
            data.close();
            return loadNodeMap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Segment> parseSegments(File segments) {
        try {
            data = new BufferedReader(new FileReader(segments));
            String line;

            while (((line = data.readLine()) != null)) {
                List<Location> coordsOfSegment = new ArrayList<>();
                String[] val = line.split("\t"); // split by tab
                List<String> values = Arrays.asList(val);
                int roadID = Integer.parseInt(values.get(0));
                Double length = Double.parseDouble(values.get(1));
                int nodeID1 = Integer.parseInt(values.get(2));
                int nodeID2 = Integer.parseInt(values.get(3));

                // Copies rest of array to new array
                List<String> rest = values.subList(4, values.size());
                int len = rest.size();

                for (int i = 0; i < len; i+=2) {
                    double lat = Double.parseDouble(rest.get(i));
                    double lon = Double.parseDouble(rest.get((i+1)));
                    Location loc = Location.newFromLatLon(lat, lon); // Converts lat/longtitudes to x/y values
                    coordsOfSegment.add(loc);
                }
                Segment seg = new Segment(roadID, length, nodeID1, nodeID2, coordsOfSegment);
                loadSegmentList.add(seg);


                // Add segment to relevant road
                if (map.roadMap.containsKey(roadID)) map.roadMap.get(roadID).addToSegments(seg);
                Node n1 = loadNodeMap.get(nodeID1);
                Node n2 = loadNodeMap.get(nodeID2);

                n1.addToSegmentOut(seg);
                n1.addToSegmentIn(seg);
                n2.addToSegmentOut(seg);
                n2.addToSegmentIn(seg);
            }
            data.close();
            return loadSegmentList;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

        public Map<Integer, Road> parseRoads(File roads) {
        try {
            data = new BufferedReader(new FileReader(roads));
            String line;

            while (((line = data.readLine()) != null)) {
                String[] values = line.split("\t");
                int roadID = Integer.parseInt(values[0]);
                int roadType = Integer.parseInt(values[1]);
                String roadName = values[2];
                String city = values[3];
                int notOneWay = Integer.parseInt(values[4]);
                int speed = Integer.parseInt(values[5]);
                int roadClass = Integer.parseInt(values[6]);
                int carNotAllowed = Integer.parseInt(values[7]);
                int bikeNotAllowed = Integer.parseInt(values[8]);
                int PedestrianNotAllowed = Integer.parseInt(values[9]);
                Color col = determineColorRoad(roadClass);
                Road road = new Road(   roadID, roadType, roadName, city, notOneWay,
                                        speed, roadClass, carNotAllowed, bikeNotAllowed, PedestrianNotAllowed, col);
                loadRoadMap.put(roadID, road);

                map.trie.add(roadName + " " + city, road);
            }
            data.close();
            return loadRoadMap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Polygon> parsePoly(File polygons) {
        try {
            data = new BufferedReader(new FileReader(polygons));
            String line;

            Polygon poly = null;
            String type = null;
            Color col = null;

            while ((line = data.readLine()) != null) {
                if (line.startsWith("Type")) {
                    String[] values = line.split("0x");
                    type = values[1];
                    col =  determineColor(type);
                }
                else if (line.startsWith("Data0") || line.startsWith("Data1")) {
                    // Create new polygon
                    poly = new Polygon();

                    // Stripping data down for easy iteration/data insertion
                    if (line.startsWith("Data0")) line = line.replaceFirst("Data0=", ""); // Removes 'Data0' from front of line
                    if (line.startsWith("Data1")) line = line.replaceFirst("Data1=", ""); // Removes 'Data0' from front of line
                    line = line.replaceAll("[()]",""); // Removes all instances of brackets
                    line = line.replaceAll("[,]", " "); // Removes all instances of commas

                    String[] values = line.split(" ");
                    for (int i = 0; i < values.length; i+=2) {
                        double lat = Double.parseDouble(values[i]);
                        double lon = Double.parseDouble(values[i + 1]);
                        Location loc = Location.newFromLatLon(lat, lon); // Converts lat/longtitudes to x/y values
                        poly.addCoords(loc);
                    }
                    poly.setColor(col);
                    poly.setType(type);
                    polygonList.add(poly);
                }
            }
            return polygonList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Color determineColorRoad(int roadClass) {
        Color col = null;

        switch (roadClass) {
            case 0:
                col = Color.white;
                break;
            case 1:
                col = Color.white;
                break;
            case 2:
                col = Color.white;
                break;
            case 3:
                col = new Color(255, 235, 175);
                break;
            case 4:
                col = new Color(254, 216, 157);
                break;
        }
        return col;
    }

    public Color determineColor(String polyType) {
        Color col = null;

        // Lakes/Oceans
        if  (   polyType.equals("3c") || polyType.equals("3d") || polyType.equals("3e") || polyType.equals("3f") ||
                polyType.equals("40") || polyType.equals("41") || polyType.equals("42") || polyType.equals("43") ||
                polyType.equals("44") || polyType.equals("45") || polyType.equals("28") || polyType.equals("47") ||
                polyType.equals("48")
                ) {
            col = new Color(163,204,255);
        }

        // National Parks/Reserves/Woodland
        else if(polyType.equals("50") || polyType.equals("16") || polyType.equals("1e") || polyType.equals("17")) {
            col = new Color(203,230,163);
        }

        switch (polyType) {
            case "7": // Airport
                col = new Color(234,234,234);
                break;
            case "e": // Airport Runway
                col = new Color(222,220,217);
                break;


            case "2": // City
                break;
            case "19": // Public Buildings (Sport?)
                break;
            case "5": // Car Park
                break;

            case "a": // University
                break;
            case "b": // Hospital
                break;
            case "8": // Shopping Centre
                break;
            case "18": // Golf Course
                break;

            case "13": // Man made area
                break;
            case "1a": // Cemetery
                break;
        }
        return col;
    }
}