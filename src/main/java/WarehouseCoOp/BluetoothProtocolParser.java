package WarehouseCoOp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 *
 * @author carls
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Denna klass sköter behandlingen av de inlästa meddelanden från AGV, då
 * inkommande meddelanden ibland är binära tal, av typen char eller olika stora
 * integers så måste dessa behandlas olika.
 *------------------------------------------------------------------------------------------------------------------------------------
 */
public class BluetoothProtocolParser {

    private InputStream bluetoothInput;
    ControlUI cui;

    public BluetoothProtocolParser(InputStream inputStream) {
        this.bluetoothInput = inputStream;

    }

    public void processInput(DataStore ds, ControlUI cui) throws IOException {
        long startTime = System.currentTimeMillis();
        int data;

        while ((data = bluetoothInput.read()) != -1) {
            char command = (char) data;
            switch (command) {
                case 'A':
                    handleConfirm(cui);
                    break;
                case 'B':
                    handlePickOK(ds, cui);
                    break;
                case 'C':
                    handlePickFail(ds, cui);
                    break;
                case 'F':
                    handleError(cui);
                    break;
                case 'X':
                    handlePositionX(readInt8(), ds);
                    break;
                case 'Y':
                    handlePositionY(readInt8(), ds);
                    break;
                case 'R':
                    handleDirection(readChar(), ds);
                    break;
                case 'D':
                    handleDistance(readInt8());
                    break;
                case 'P':
                    handleEnergyP(readInt8(), ds);
                    break;
                case 'Q':
                    handleEnergyQ(readInt8(), ds);
                    break;
                default:
                    System.out.println("Unknown command: " + command);
            }

        }
    }

    private char readChar() throws IOException {
        char byte1 = (char) bluetoothInput.read();

        return byte1;
    }

    private int readInt16() throws IOException {
        int byte1 = bluetoothInput.read();
        int byte2 = bluetoothInput.read();
        return (byte1 << 8) + byte2;
    }

    private int readInt8() throws IOException {
        return bluetoothInput.read();
    }

    private void handleConfirm(ControlUI cui) {
        System.out.println("Confirm command received");
        //cui.appendStatus("Confirm command received\n", 2);
    }

    private void handlePickOK(DataStore ds, ControlUI cui) {
        System.out.println("Pick OK");
        cui.appendStatus("Pick OK\n", 2);

        ds.shelvesPicked++;

        if (ds.PlockOrdning.size() > 0) {
            ds.PlockOrdning.removeFirst();
        }

        ds.pickFlag = true;

    }

    private void handlePickFail(DataStore ds, ControlUI cui) {
        ds.pickFlag = true;
        cui.appendStatus("Pick Fail\n", 2);
        System.out.println("Pick Fail");
        if (ds.PlockOrdning.size() > 0) {
            ds.PlockOrdning.removeFirst();
        }

        ds.shelvesPicked++;
    }

    private void handleError(ControlUI cui) {
        System.out.println("Error occurred");
        cui.appendStatus("Error occurred\n", 2);

    }

    private void handlePositionX(int position, DataStore ds) {

        System.out.println("Position X: " + position);
        position = position * 30 - 30;
        ds.truckX = position;

    }

    private void handlePositionY(int position, DataStore ds) {
        System.out.println("Position Y: " + position);
        position = position * 30;
        ds.truckY = position;
    }

    private void handleDirection(char direction, DataStore ds) {
        System.out.println("Direction: " + direction);
        ds.direction = direction;
    }

    private void handleDistance(int distance) {
        System.out.println("Distance to wall: " + distance);
    }

    private void handleEnergyP(int energy, DataStore ds) {
        System.out.println("Energy consumption: " + energy);
        ds.energyConsumption = energy;
    }

    private void handleEnergyQ(int energy, DataStore ds) {
        System.out.println("Energy consumption: " + energy);
        int temp = ds.energyConsumption;
        String sumString = Integer.toString(temp) + Integer.toString(energy);
        int result = Integer.parseInt(sumString);
        ds.energyConsumption = result;
    }
}
