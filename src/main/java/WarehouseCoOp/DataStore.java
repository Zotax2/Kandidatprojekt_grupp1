/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WarehouseCoOp;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;

/**
 *
 * @author carls
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Syftet med denna klass är att spara alla variabler som används i andra
 * klasser. Det finns även en metod som heter readLayout() som läser in
 * lagerlayouten från ett textdokument. I slutet finns även en metod för att
 * omvandla centimeter till nodnummer vilket kan vara bra att kunna göra i många
 * andra klasser.
 * ------------------------------------------------------------------------------------------------------------------------------------
 */
public class DataStore {

    boolean layoutRead;
    int AGVid;
    String fileName = "Layouts//layout2.txt";
    int noObstacles;
    int noShelves;
    int[] obsX;
    int[] obsY;
    int[] shelfX;
    int[] shelfY;
    String[] shelfDir;
    int truckX;
    int truckY;

    ArrayList<Integer> avlastningszon;

    int energyConsumption;
    char direction;
    char actualDirection;
    boolean updateFlag;
    ArrayList<Robot_Command> route_cmds;
    int gridSizeX;
    int gridSizeY;

    ArrayList<Integer> DesiredShelves;
    ArrayList<Integer> PlockOrdning;

    int pathLength;
    int[] linkStarts;
    int[] linkEnds;

    int startNod;
    int slutNod;

    int EnemyX;
    int EnemyY;

    boolean pickFlag;
    boolean btIsConnected;

    int stop;

    int shelvesPicked;

    int noShelvesToVisit;

    DataStore() {
        AGVid = 1;
        layoutRead = false;
        obsX = new int[100];
        obsY = new int[100];
        shelfX = new int[100];
        shelfY = new int[100];
        shelfDir = new String[100];
        truckX = 0;
        truckY = 180;

        avlastningszon = new ArrayList<>();
        Collections.addAll(avlastningszon, 67, 68, 89, 90, 111, 112);

        direction = ' ';
        actualDirection = 'E';
        energyConsumption = 0;
        updateFlag = false;
        ArrayList<Robot_Command> route_cmds = new ArrayList<>();
        gridSizeX = 22;
        gridSizeY = 10;

        DesiredShelves = new ArrayList<Integer>();
        PlockOrdning = new ArrayList<>();

        pathLength = 0;
        linkStarts = new int[1000];
        linkEnds = new int[1000];

        startNod = 133;
        slutNod = 89;

        EnemyX = 0;
        EnemyY = 0;

        pickFlag = false;
        btIsConnected = false;

        stop = 0;

        shelvesPicked = 0;
        noShelvesToVisit = 0;

    }

    public void readLayout() {
        String line;

        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file, "UTF-8");
            String[] sline;

            // Läs in hinder
            line = (scanner.nextLine());
            noObstacles = Integer.parseInt(line.split(",")[0].trim());
            noObstacles += 1;
            obsX[noObstacles - 1] = -1;
            obsY[noObstacles - 1] = -1;
            System.out.println("Obstacles: " + noObstacles);

            for (int i = 0; i < noObstacles - 1; i++) {
                line = (scanner.nextLine());
                obsX[i] = Integer.parseInt(line.split(",")[0].trim());
                obsY[i] = Integer.parseInt(line.split(",")[1].trim());
                System.out.println("Obstacle " + i + ": x= " + obsX[i] + " y= " + obsY[i]);
            }
            // Läs in hyllor
            line = (scanner.nextLine());
            noShelves = Integer.parseInt(line.split(",")[0].trim());
            System.out.println("Shelves: " + noShelves);

            for (int i = 0; i < noShelves; i++) {
                line = (scanner.nextLine());
                shelfX[i] = Integer.parseInt(line.split(",")[0].trim());
                shelfY[i] = Integer.parseInt(line.split(",")[1].trim());
                shelfDir[i] = line.split(",")[2].trim();
                System.out.println("Shelf " + i + ": x= " + shelfX[i] + " y= " + shelfY[i] + " Dir= " + shelfDir[i]);
            }

            //Visa att all data är läst
            layoutRead = true;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public int getNode(int x, int y) {
        int node = ((x / 30) + (y / 30) * gridSizeX) + 1;
        return node;
    }

}
