/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WarehouseCoOp;

import java.util.ArrayList;

/**
 *
 * @author carls
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Denna klass uppdaterar textfält 3 med respektive information så länge bluetooth är anslutet.
 * ------------------------------------------------------------------------------------------------------------------------------------
 */
public class GuiUpdate implements Runnable {

    private int sleepTime;
    private ControlUI cui;
    private DataStore ds;

    public GuiUpdate(DataStore ds, ControlUI cui) {
        this.cui = cui;
        this.ds = ds;
        sleepTime = 15000;
    }

    @Override
    public void run() {

        while (!ds.btIsConnected) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        while (ds.btIsConnected) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                System.out.println(e.toString());
            }

            //Rensa textfältet
            cui.clearTextField(3);
            //Skriv ut alla variabler  
            cui.appendStatus("Energiförbrukning: " + ds.energyConsumption + "Kw" + "\n", 3);
            cui.appendStatus("TruckX: " + ds.truckX + "\n", 3);
            cui.appendStatus("TruckY: " + ds.truckY + "\n", 3);
            cui.appendStatus("Riktning: " + ds.direction + "\n", 3);
            //Uppdatera GUI
            cui.repaint();

        }
    }
}
