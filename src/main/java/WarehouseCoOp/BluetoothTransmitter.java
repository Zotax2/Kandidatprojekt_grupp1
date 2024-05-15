package WarehouseCoOp;

import java.io.*;
import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author carls
 *------------------------------------------------------------------------------------------------------------------------------------
 * Denna klass tillsammans med dess metoder för att ansluta, frånkoppla, skicka och ta emot meddelanden
 * sköter den faktiska kommunikationen till AGV, i andra klasser och trådar används dessa metoder för att sedan bearbetas vidare.
 *------------------------------------------------------------------------------------------------------------------------------------
 */

//"Användar manual" : skapa ny bluetoothtransmitter, .openConnection(), gör grejer, closeConnection()
public class BluetoothTransmitter {

    String address;
    StreamConnection connection;
    InputStream bluetooth_in;
    BluetoothProtocolParser bpp;
    ControlUI cui;

    public BluetoothTransmitter() {    //default address lab computer
        address = "btspp://98D341F61D23:1";
        //address = "btspp://28D0EA37F900:1";

    }

    public BluetoothTransmitter(String setAddress, DataStore ds, ControlUI cui) { //any address
        address = setAddress;
        this.cui = cui;

    }
    private PrintStream bluetooth_ut;
    private BufferedReader bluetooth_input;
    boolean isConnected = false;

    //Öppnar anslutning till AGV
    public void openConnection() {
        //kan vara värt att öppna Streams input stream här så den kan ta emot när som
        try {

            connection = (StreamConnection) Connector.open(address);
            bluetooth_in = connection.openInputStream();
            bluetooth_ut = new PrintStream(connection.openOutputStream());

            bpp = new BluetoothProtocolParser(bluetooth_in);
            isConnected = true;

        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }

    //Stänger anslutning
    public void closeConnection() {
        try {
            bluetooth_ut.close();
            bluetooth_input.close();
            connection.close();
            isConnected = false;

        } catch (Exception e) {
            System.out.print(e.toString());
        }

    }

    //Skickar meddelandet
    public void transmit(String med) {
        try {

            bluetooth_ut.println(med);
            Thread.sleep(500);
            bluetooth_ut.flush();

        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }

    //Tar emot meddelanden från AGV och skickar till Protocol parser
    public void recieverV2(DataStore ds, ControlUI cui) {
        try {
            bpp.processInput(ds, cui);
        } catch (Exception e) {
            System.out.print(e.toString());
        }

    }

    //Kontrollerar anslutningen
    public void checkBluetoothConnection() {
        try {
            RemoteDevice rd = RemoteDevice.getRemoteDevice(connection);

            if (rd.getBluetoothAddress() == null) {
                cui.appendStatus("Bluetooth ej ansluten", 2);
            }

        } catch (Exception e) {
        }

    }

}
