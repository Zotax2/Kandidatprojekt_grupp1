/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WarehouseCoOp;

import java.util.ArrayList;

/**
 *
 * @author carls
 *------------------------------------------------------------------------------------------------------------------------------------
 * Detta är en tråd som sköter bluetooth-kommunikationen/styrningen till AGV,
 * sammanfattat så går den igenom alla körkommandon och skickar dessa beroende
 * på om AGV:n är redo att ta emot nästa meddelande. funktionen compass()
 * hjälper till att uppdatera AGV-riktningen i datastore beroende på vilket
 * sväng-kommando som skickas.
 *------------------------------------------------------------------------------------------------------------------------------------
 */
public class BluetoothCtl implements Runnable {

    private int sleepTime;
    private ControlUI cui;
    private DataStore ds;
    BluetoothTransmitter bt;

    public BluetoothCtl(DataStore ds, ControlUI cui, BluetoothTransmitter bt) {
        this.cui = cui;
        this.ds = ds;
        this.bt = bt;
        sleepTime = 20000;
    }

    public char compass(char actualDirection, String turn) {

        char dir = actualDirection;
        String svang = turn;

        switch (dir) {
            case 'N':
                if (svang.equals("c")) {
                    dir = 'E';
                    return dir;
                }
                if (svang.equals("d")) {
                    dir = 'W';
                    return dir;
                }

            case 'E':
                if (svang.equals("c")) {
                    dir = 'S';
                    return dir;
                }
                if (svang.equals("d")) {
                    dir = 'N';
                    return dir;
                }
            case 'S':
                if (svang.equals("c")) {
                    dir = 'W';
                    return dir;
                }
                if (svang.equals("d")) {
                    dir = 'E';
                    return dir;
                }
            case 'W':
                if (svang.equals("c")) {
                    dir = 'N';
                    return dir;
                }
                if (svang.equals("d")) {
                    dir = 'S';
                    return dir;
                }
            default:
                return dir;
        }

    }

    @Override
    public void run() {
        boolean åkHem = false;
        boolean iVägen = false;
        HTTPC http = new HTTPC(ds);

        int k = 0;
        int flag;

        for (int i = 0; i < ds.route_cmds.size(); i++) {

            try {
                while (ds.stop % 2 == 1) {
                    Thread.sleep(100);
                }
            } catch (Exception e) {

            }
            //Om AGV är påväg "hem" från avlastningszon
            if ((ds.avlastningszon.contains(ds.getNode(ds.truckX, ds.truckY))) && (ds.noShelvesToVisit - ds.shelvesPicked) <= 0) {
                åkHem = true;

            }
            int p = i;
            iVägen = false;
            while (p < i + 5) {

                if (ds.linkEnds[p] == ds.getNode(ds.EnemyX, ds.EnemyY)) {
                    iVägen = true;
                }
                p++;
            }

            //Om annan AGV är i avlastningszonen och vi är påväg mot avlastningszonen
            while (ds.avlastningszon.contains(ds.getNode(ds.EnemyX, ds.EnemyY)) && ds.avlastningszon.contains(ds.linkEnds[i + 1]) && åkHem == false) {
                try {

                    Thread.sleep(1000);
                    http.refresh(ds.truckX, ds.truckY);

                } catch (Exception e) {

                }
            }
            //Om vi är AGV 1 och är nära AGV 2
            while (Math.abs(ds.truckX - ds.EnemyX) + Math.abs(ds.truckY - ds.EnemyY) <= 60 && ds.AGVid == 1 && åkHem == false) {
                try {

                    Thread.sleep(1000);
                    http.refresh(ds.truckX, ds.truckY);

                } catch (Exception e) {

                }
            }
            //Om vi är AGV 2 och är nära AGV 1 (60cm)

            if (Math.abs(ds.truckX - ds.EnemyX) + Math.abs(ds.truckY - ds.EnemyY) <= 60 && ds.AGVid == 2 && åkHem == false && iVägen == true) {

                ds.noObstacles = ds.noObstacles + 1;
                ds.obsX[ds.noObstacles - 1] = ds.EnemyX;
                ds.obsY[ds.noObstacles - 1] = ds.EnemyY;
                int node = ds.getNode(ds.truckX, ds.truckY);

                ArrayList<Integer> temp = ds.PlockOrdning;
                cui.runFinalPlan(temp, node);
                for (int ik = 0; ik < temp.size(); ik++) {
                    System.out.println("Här kommer plockordningen");

                    System.out.println(temp.get(ik));
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                System.out.println("Här kommer linkends");
                for (int a = 0; a < ds.linkEnds.length; a++) {
                    System.out.println(ds.linkEnds[a]);
                }
                http.refresh(ds.truckX, ds.truckY);

                i = 0;
                k = 0;
                cui.repaint();

                iVägen = false;
            }

            //Skriv ut kör-ordern i GUI
            cui.printOrder(ds.route_cmds.get(i).getCOMMAND_CHAR());
            //Skicka meddelandet till AGV
            bt.transmit(ds.route_cmds.get(i).getCOMMAND_CHAR() + " ");
            //Uppdatera hjälpsystemet
            http.refresh(ds.truckX, ds.truckY);
            //Uppdatera kompassen
            ds.actualDirection = compass(ds.actualDirection, ds.route_cmds.get(i).getCOMMAND_CHAR());

            System.out.println(ds.route_cmds.get(i).getCOMMAND_CHAR() + " ");
            try {

                if (ds.route_cmds.get(i).getCOMMAND_CHAR().equals("l")) {
                    flag = 1;
                    //Sålänge plockningen ej är godkänd
                    while (ds.pickFlag == false) {
                        if (flag % 10 == 0) {
                            http.refresh(ds.truckX, ds.truckY);
                        }

                        Thread.sleep(100);
                        writeToTextField(k);

                        flag++;
                    }
                    ds.pickFlag = false;

                    k++;

                } //Om vi svänger höger eller vänster
                else if (ds.route_cmds.get(i).getCOMMAND_CHAR().equals("c") || ds.route_cmds.get(i).getCOMMAND_CHAR().equals("d")) {

                    //Flagga för att uppdatera hjälpsystem
                    flag = 1;
                    //Om AGVn är på fel plats
                    while (ds.getNode(ds.truckX, ds.truckY) != ds.linkEnds[k] || ds.actualDirection != ds.direction) {
                        if (flag % 200 == 0) {
                            bt.transmit(ds.route_cmds.get(i).getCOMMAND_CHAR() + " ");
                            cui.jTextArea4.append(ds.route_cmds.get(i).getCOMMAND_CHAR() + " " + "(räddad)");
                        }
                        if (flag % 10 == 0) {
                            http.refresh(ds.truckX, ds.truckY);
                        }
                        Thread.sleep(100);
                        writeToTextField(k);
                        flag++;
                    }
                    k++;

                } else {
                    flag = 1;

                    while (!(ds.getNode(ds.truckX, ds.truckY) == ds.linkEnds[k])) {
                        if (flag % 100 == 0) {
                            bt.transmit(ds.route_cmds.get(i).getCOMMAND_CHAR() + " ");
                            cui.jTextArea4.append(ds.route_cmds.get(i).getCOMMAND_CHAR() + " " + "(räddad)");
                        }
                        if (flag % 10 == 0) {
                            http.refresh(ds.truckX, ds.truckY);
                        }
                        Thread.sleep(100);
                        writeToTextField(k);
                        flag++;

                    }
                    k++;

                }

            } catch (InterruptedException e) {

                e.printStackTrace();
                System.out.println("fel");
            }


        }

        System.out.println("Nu är det slut på route cmds!!!!");

    }

    public void writeToTextField(int k) {
        cui.clearTextField(1);
        cui.appendStatus("Rätt nod: " + ds.linkEnds[k] + "\n", 1);
        cui.appendStatus("AGV nod: " + ds.getNode(ds.truckX, ds.truckY) + "\n", 1);

        cui.appendStatus("Rätt riktning: " + ds.actualDirection + "\n", 1);
        cui.appendStatus("AGV riktning: " + ds.direction + "\n", 1);

        cui.appendStatus("Antal hyllor plockade: " + ds.shelvesPicked + "\n", 1);
        cui.appendStatus("Antal hyllor kvar: " + (ds.noShelvesToVisit - ds.shelvesPicked) + "\n", 1);

    }
}
