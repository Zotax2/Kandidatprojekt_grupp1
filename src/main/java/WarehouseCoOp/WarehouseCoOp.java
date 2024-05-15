package WarehouseCoOp;

/**
 *
 * @author carls
 * 
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Detta är main-klassen som startar nya instanser av de olika klasserna.
 * ------------------------------------------------------------------------------------------------------------------------------------
 *
 */
public class WarehouseCoOp {

    DataStore ds;
    ControlUI cui;

    BluetoothTransmitter bt;
    HTTPC http;

    WarehouseCoOp() {

        bt = new BluetoothTransmitter();

        ds = new DataStore();

        http = new HTTPC(ds);

        ds.readLayout();

        OptPlan op = new OptPlan(ds);

        cui = new ControlUI(ds, op, bt, http);
        cui.setVisible(true);

    }

    public static void main(String[] args) {

        WarehouseCoOp x = new WarehouseCoOp();
        GuiUpdate z = new GuiUpdate(x.ds, x.cui);

        Thread t2 = new Thread(z);

        t2.start();

    }
}
