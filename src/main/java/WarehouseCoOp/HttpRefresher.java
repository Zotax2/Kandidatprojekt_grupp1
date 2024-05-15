/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WarehouseCoOp;

/**
 *
 * @author carls
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Denna tråd uppdaterar hjälpsystemet en gång i sekunden så länge bluetooth är
 * anslutet.
 * ------------------------------------------------------------------------------------------------------------------------------------
 */
public class HttpRefresher implements Runnable {

    private DataStore ds;
    HTTPC http;

    public HttpRefresher(DataStore ds, HTTPC http) {

        this.ds = ds;
        this.http = http;
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

            http.refresh(ds.truckX, ds.truckY);
            System.out.println("HTTP uppdaterat");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e.toString());
            }

        }

        System.out.println("HttpRefresher har slutat!");
    }
}
