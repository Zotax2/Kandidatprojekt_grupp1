/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package WarehouseCoOp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author carls
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Denna klass är till för att rita upp "kartan" i GUI:n
 * Utöver själva lagergolvet så ritar den även ut hyllor, rutter, AGV och "extern" AGV och hinder.
 * ------------------------------------------------------------------------------------------------------------------------------------
 */
public class FloorPanel extends javax.swing.JPanel {

    DataStore ds;

    int startNode = 2; // Startnod för fordonet (19 - 1 eftersom index börjar från 0)
    int gridsizeX;
    int gridsizeY;

    /**
     * Creates new form FloorPanel
     */
    public FloorPanel(DataStore ds) {
        this.ds = ds;
        initComponents();
        this.gridsizeX = ds.gridSizeX;
        this.gridsizeY = ds.gridSizeY;

    }

    public int getNode(int x, int y) {
        int node = gridsizeX * y + x + 1;
        return node;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Color LIGHT_COLOR = new Color(150, 150, 150);
        final Color DARK_COLOR = new Color(0, 0, 0);
        final Color RED_COLOR = new Color(255, 0, 0);
        final Color GREEN_COLOR = new Color(0, 200, 0);
        final Color BLUE_COLOR = new Color(0, 0, 255);

        int panelHeight = getHeight();
        int panelWidth = getWidth();
        int distY = 30;
        int distX = 30;
        int offsetY = 30;
        int offsetX = 30;

        double xscale = 1.0 * panelWidth / (gridsizeX * distX + 2 * offsetX);
        double yscale = 1.0 * panelHeight / (gridsizeY * distY + 2 * offsetY);

        int nodeSize = 5;
        int obsSize = 20;

        if (ds.layoutRead == true) {
            // Draw nodes
            for (int x = 0; x < gridsizeX; x++) {
                for (int y = 0; y < gridsizeY; y++) {
                    g.drawOval((int) (offsetX * xscale + x * distX * xscale - nodeSize / 2),
                            panelHeight - (int) (offsetY * yscale + y * distY * yscale + nodeSize / 2), nodeSize, nodeSize);
                }
            }

            Graphics2D g2 = (Graphics2D) g;

            // Draw horizontal arcs
            for (int x = 0; x < gridsizeX - 1; x++) {
                for (int y = 0; y < gridsizeY; y++) {

                    g2.setStroke(new BasicStroke(1));
                    g.setColor(DARK_COLOR);
                    for (int i = 0; i < ds.pathLength; i++) {

                        if ((getNode(x, y) == ds.linkStarts[i]
                                && getNode(x + 1, y) == ds.linkEnds[i])
                                || (getNode(x, y) == ds.linkEnds[i]
                                && getNode(x + 1, y) == ds.linkStarts[i])) {
                            g2.setStroke(new BasicStroke(10));
                            g.setColor(RED_COLOR);
                        }

                    }
                    g.drawLine((int) (xscale * (offsetX + x * distX)),
                            panelHeight - (int) (yscale * (offsetY + y * distY)),
                            (int) (xscale * (offsetX + (x + 1) * distX)),
                            panelHeight - (int) (yscale * (offsetY + y * distY)));
                }
            }
            // Draw vertical arcs
            for (int x = 0; x < gridsizeX; x++) {
                for (int y = 0; y < gridsizeY - 1; y++) {

                    g2.setStroke(new BasicStroke(1));
                    g.setColor(DARK_COLOR);
                    for (int i = 0; i < ds.pathLength; i++) {

                        if ((getNode(x, y) == ds.linkStarts[i]
                                && getNode(x, y + 1) == ds.linkEnds[i])
                                || (getNode(x, y) == ds.linkEnds[i]
                                && getNode(x, y + 1) == ds.linkStarts[i])) {
                            g2.setStroke(new BasicStroke(10));
                            g.setColor(RED_COLOR);
                        }

                    }

                    g.drawLine((int) (xscale * (offsetX + x * distX)),
                            panelHeight - (int) (yscale * (offsetY + y * distY)),
                            (int) (xscale * (offsetX + x * distX)),
                            panelHeight - (int) (yscale * (offsetY + (y + 1) * distY)));
                }
            }
            g2.setStroke(new BasicStroke(1));
            // Draw obstacles
            g.setColor(RED_COLOR);

            for (int k = 0; k < ds.noObstacles; k++) {
                g.fillOval((int) (offsetX * xscale + ds.obsX[k] * xscale - obsSize / 2),
                        panelHeight - (int) (offsetY * yscale + ds.obsY[k] * yscale + obsSize / 2), obsSize, obsSize);
            }

            // Draw shelves
            for (int k = 0; k < ds.noShelves; k++) {
                g.setColor(GREEN_COLOR);
                int circleX = (int) (offsetX * xscale + ds.shelfX[k] * xscale);
                int circleY = panelHeight - (int) (offsetY * yscale + ds.shelfY[k] * yscale);

                // Rita ut stor cirkel för hyllan
                g.fillOval(circleX - obsSize / 2, circleY - obsSize / 2, obsSize, obsSize);

                // Rita ut mindre cirkel i rätt riktning
                if (ds.shelfDir[k].equals("N")) {
                    g.setColor(DARK_COLOR);
                    g.fillOval(circleX - obsSize / 4, circleY - obsSize / 2 - obsSize / 4, obsSize / 2, obsSize / 2);
                } else if (ds.shelfDir[k].equals("E")) {
                    g.setColor(DARK_COLOR);
                    g.fillOval(circleX + obsSize / 2 - obsSize / 4, circleY - obsSize / 4, obsSize / 2, obsSize / 2);
                } else if (ds.shelfDir[k].equals("S")) {
                    g.setColor(DARK_COLOR);
                    g.fillOval(circleX - obsSize / 4, circleY + obsSize / 2 - obsSize / 4, obsSize / 2, obsSize / 2);
                } else if (ds.shelfDir[k].equals("W")) {
                    g.setColor(DARK_COLOR);
                    g.fillOval(circleX - obsSize / 2 - obsSize / 4, circleY - obsSize / 4, obsSize / 2, obsSize / 2);
                }
            }

            // Draw truck
            g.setColor(BLUE_COLOR);
            int truckSize = 30;
            g.fillOval((int) (offsetX * xscale + ds.truckX * xscale - truckSize / 2),
                    panelHeight - (int) (offsetY * yscale + ds.truckY * yscale + truckSize / 2), truckSize, truckSize);

            // Draw direction arrow
            int arrowSize = 20;
            int arrowX = (int) (offsetX * xscale + ds.truckX * xscale);
            int arrowY = panelHeight - (int) (offsetY * yscale + ds.truckY * yscale);
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];

            if (ds.direction == 'N') {
                xPoints[0] = arrowX;
                yPoints[0] = arrowY - truckSize / 2;
                xPoints[1] = arrowX - arrowSize / 2;
                yPoints[1] = arrowY - truckSize / 2 - arrowSize;
                xPoints[2] = arrowX + arrowSize / 2;
                yPoints[2] = arrowY - truckSize / 2 - arrowSize;
            } else if (ds.direction == 'E') {
                xPoints[0] = arrowX + truckSize / 2;
                yPoints[0] = arrowY;
                xPoints[1] = arrowX + truckSize / 2 + arrowSize;
                yPoints[1] = arrowY - arrowSize / 2;
                xPoints[2] = arrowX + truckSize / 2 + arrowSize;
                yPoints[2] = arrowY + arrowSize / 2;
            } else if (ds.direction == 'S') {
                xPoints[0] = arrowX;
                yPoints[0] = arrowY + truckSize / 2;
                xPoints[1] = arrowX - arrowSize / 2;
                yPoints[1] = arrowY + truckSize / 2 + arrowSize;
                xPoints[2] = arrowX + arrowSize / 2;
                yPoints[2] = arrowY + truckSize / 2 + arrowSize;
            } else if (ds.direction == 'W') {
                xPoints[0] = arrowX - truckSize / 2;
                yPoints[0] = arrowY;
                xPoints[1] = arrowX - truckSize / 2 - arrowSize;
                yPoints[1] = arrowY - arrowSize / 2;
                xPoints[2] = arrowX - truckSize / 2 - arrowSize;
                yPoints[2] = arrowY + arrowSize / 2;
            }

            g.fillPolygon(xPoints, yPoints, 3);

            //Draw Enemy truck
            g.setColor(DARK_COLOR);
            //int EnemyTruckSize = 30;
            g.fillOval((int) (offsetX * xscale + ds.EnemyX * xscale - truckSize / 2),
                    panelHeight - (int) (offsetY * yscale + ds.EnemyY * yscale + truckSize / 2), truckSize, truckSize);

            for (int x = 0; x < gridsizeX; x++) {
                for (int y = 0; y < gridsizeY; y++) {

                    g.drawString("" + (y * gridsizeX + x + 1), (int) (offsetX * xscale + x * distX * xscale - nodeSize / 2) + 2,
                            panelHeight - (int) (offsetY * yscale + y * distY * yscale + nodeSize / 2) + 2);

                }
            }

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
