import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

class Vertex {
    int id;
    int x, y;

    Vertex(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}

class Edge {
    Vertex start, end;
    int weight;

    Edge(Vertex start, Vertex end, int weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }
}

public class GraphBoard extends JFrame {
    private ArrayList<Vertex> vertices;
    private ArrayList<Edge> edges;
    private Vertex startVertex;
    private int edgeWeight;
    private boolean eraseMode;

    public GraphBoard() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        edgeWeight = 1;
        startVertex = null;
        eraseMode = false;

        setTitle("Graph Board");
        setSize(1500, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (Vertex v : vertices) {
                    g.setColor(Color.CYAN);
                    g.fillOval(v.x - 15, v.y - 15, 30, 30); // Larger vertices
                    g.setColor(Color.BLACK);
                    FontMetrics fm = g.getFontMetrics();
                    int textWidth = fm.stringWidth(Integer.toString(v.id));
                    int textHeight = fm.getHeight();
                    g.drawString(Integer.toString(v.id), v.x - textWidth / 2, v.y + textHeight / 4); // Display vertex ID in the middle
                }
                for (Edge e : edges) {
                    g.setColor(Color.BLACK);
                    g.drawLine(e.start.x, e.start.y, e.end.x, e.end.y);
                    g.drawString(Integer.toString(e.weight), (e.start.x + e.end.x) / 2, (e.start.y + e.end.y) / 2);
                }
            }
        };

        JButton sourceButton = new JButton("Set Source");
        sourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sourceID = JOptionPane.showInputDialog("Enter the ID of the source vertex:");
                if (sourceID != null && !sourceID.isEmpty()) {
                    int source = Integer.parseInt(sourceID);
                    for (Vertex v : vertices) {
                        if (v.id == source) {
                            startVertex = v;
                            break;
                        }
                    }
                }
            }
        });

        JButton findButton = new JButton("Find");
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startVertex != null && !edges.isEmpty()) {
                    displayShortestDistances();
                } else {
                    JOptionPane.showMessageDialog(null, "Please set the source vertex and create edges first!");
                }
            }
        });

        panel.add(sourceButton);
        panel.add(findButton);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !eraseMode) {
                    int nextID = vertices.isEmpty() ? 0 : vertices.get(vertices.size() - 1).id + 1;
                    vertices.add(new Vertex(nextID, e.getX(), e.getY()));
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (!eraseMode) {
                        if (startVertex == null) {
                            for (Vertex v : vertices) {
                                if (Math.sqrt(Math.pow(e.getX() - v.x, 2) + Math.pow(e.getY() - v.y, 2)) <= 15) {
                                    startVertex = v;
                                    break;
                                }
                            }
                        } else {
                            Vertex endVertex = null;
                            for (Vertex v : vertices) {
                                if (Math.sqrt(Math.pow(e.getX() - v.x, 2) + Math.pow(e.getY() - v.y, 2)) <= 15) {
                                    endVertex = v;
                                    break;
                                }
                            }
                            if (endVertex != null) {
                                String weightInput = JOptionPane.showInputDialog("Enter weight for the edge:");
                                if (weightInput != null && !weightInput.isEmpty()) {
                                    edgeWeight = Integer.parseInt(weightInput);
                                    edges.add(new Edge(startVertex, endVertex, edgeWeight));
                                    startVertex = null;
                                    repaint();
                                } else {
                                    JOptionPane.showMessageDialog(null, "Invalid weight!");
                                }
                            }
                        }
                    } else { // Erase mode
                        for (Vertex v : vertices) {
                            if (Math.sqrt(Math.pow(e.getX() - v.x, 2) + Math.pow(e.getY() - v.y, 2)) <= 15) {
                                vertices.remove(v);
                                // Remove edges related to the deleted vertex
                                edges.removeIf(edge -> edge.start == v || edge.end == v);
                                repaint();
                                break;
                            }
                        }
                    }
                }
            }
        });

        // Add key listener to switch erase mode on/off
        panel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_E) {
                    eraseMode = !eraseMode;
                    if (eraseMode) {
                        JOptionPane.showMessageDialog(null, "Erase mode enabled. Right-click to erase vertices.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Erase mode disabled.");
                    }
                }
            }
        });

        panel.setFocusable(true); // Required for key listener to work
        add(panel);
        setVisible(true);
    }

    private void displayShortestDistances() {
        if (startVertex != null && !edges.isEmpty()) {
            Map<Vertex, Integer> distances = new HashMap<>();
            Set<Vertex> visited = new HashSet<>();
            PriorityQueue<Vertex> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

            // Map to store shortest paths
            Map<Vertex, List<Vertex>> paths = new HashMap<>();

            // Initialize distances
            for (Vertex v : vertices) {
                distances.put(v, Integer.MAX_VALUE);
                paths.put(v, new ArrayList<>()); // Initialize empty path for each vertex
            }
            distances.put(startVertex, 0);
            paths.get(startVertex).add(startVertex); // Start vertex is part of its own path
            pq.add(startVertex);

            while (!pq.isEmpty()) {
                Vertex current = pq.poll();
                visited.add(current);
                for (Edge edge : edges) {
                    if (edge.start == current) {
                        Vertex next = edge.end;
                        int alt = distances.get(current) + edge.weight;
                        if (alt < distances.get(next) && !visited.contains(next)) {
                            distances.put(next, alt);
                            paths.get(next).clear(); // Clear previous path (if any)
                            paths.get(next).addAll(paths.get(current)); // Copy path from parent
                            paths.get(next).add(next); // Add current vertex to the path
                            pq.add(next);
                        }
                    }
                }
            }

            // Debugging step: Print confirmation after calculating shortest distances and paths
            System.out.println("Shortest distances and paths calculated.");

            // Display the minimum distances and paths
            JFrame resultFrame = new JFrame("Minimum Distances & Shortest Paths from Source");
            JTextArea resultTextArea = new JTextArea();
            resultTextArea.append("Minimum distances and shortest paths from source (ID " + startVertex.id + "):\n");
            for (Vertex v : vertices) {
                if (v != startVertex) {
                    resultTextArea.append("Vertex " + v.id + ": ");
                    if (distances.get(v) == Integer.MAX_VALUE) {
                        resultTextArea.append("No path\n"); // Indicate unreachable vertex
                    } else {
                        resultTextArea.append(distances.get(v) + " - Path: ");
                        // Construct path string by traversing paths list backwards
                        List<Vertex> path = paths.get(v);
                        for (int i = path.size() - 1; i >= 0; i--) {
                            resultTextArea.append(path.get(i).id + (i > 0 ? " -> " : ""));
                        }
                        resultTextArea.append("\n");
                    }
                }
            }

            resultFrame.add(new JScrollPane(resultTextArea));
            resultFrame.setSize(500, 400); // Adjust size as needed
            resultFrame.setLocationRelativeTo(null);
            // Ensure visibility is set to true
            resultFrame.setVisible(true);

            // Debugging step: Add exception handling for potential issues while displaying the window
            try {
                // ... (rest of the code for displaying the result window) ...
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error displaying results: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please set the source vertex and create edges first!");
        }
    }


    public static void main(String[] args) {
        new GraphBoard();
    }
}

