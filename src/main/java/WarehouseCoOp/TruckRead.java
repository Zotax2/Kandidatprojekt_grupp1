/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WarehouseCoOp;

/**
 *
 * @author carls
 * 
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Denna tråd öppnar en anslutning till AGVn och läser in streamad data från AGVn så länge den är ansluten.
 * ------------------------------------------------------------------------------------------------------------------------------------
 */
public class TruckRead implements Runnable {

    private int sleepTime;
    private ControlUI cui;
    private DataStore ds;
    BluetoothTransmitter bt;

    public TruckRead(DataStore ds, ControlUI cui, BluetoothTransmitter bt) {
        this.cui = cui;
        this.ds = ds;
        this.bt = bt;
        sleepTime = 20000;
    }

    @Override
    public void run() {

        bt.openConnection();

        ds.btIsConnected = true;

        while (ds.btIsConnected) {
            bt.recieverV2(ds, cui);
        }
        cui.appendStatus("Bluetooth frånkopplad!", 2);

    }

}
