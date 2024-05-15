/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package WarehouseCoOp;

/*

------------------------------------------------------------------------------------------------------------------------------------
Denna klass sköter kommunikationen med hjälpsystemet, alltså förhandlingen av plockuppgifter och uppdateringen av våran position.
------------------------------------------------------------------------------------------------------------------------------------

 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.time.Clock.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class HTTPC {

    DataStore ds;

    String lastOut = "";
    String urlBase = "https://n7.se/pos.php?id=";

//https://n7.se/pos.php?id=1&x=0&y=0
    public HTTPC(DataStore ds) {
        this.ds = ds;
        int id = ds.AGVid;
        double x = ds.truckX;
        double y = ds.truckY;

        String url = urlBase;

        url = url + id + "&x=" + x + "&y=" + y;
        try {
            URL urlobjekt = new URL(url);
            HttpURLConnection anslutning = (HttpURLConnection) urlobjekt.openConnection();
            System.out.println("\nAnropar: " + url);
            int mottagen_status = anslutning.getResponseCode();
            System.out.println("Statuskod: " + mottagen_status);
            BufferedReader inkommande = new BufferedReader(new InputStreamReader(anslutning.getInputStream()));
            String inkommande_text;
            StringBuffer inkommande_samlat = new StringBuffer();
            while ((inkommande_text = inkommande.readLine()) != null) {
                inkommande_samlat.append(inkommande_text);
            }

            inkommande.close();
            lastOut = inkommande_samlat.toString();

        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }

    public String readOut() {
        return lastOut;
    }

    public int[] convertOutPos(String s) {
        String[] sep = s.split(",");
        int[] valOut = new int[sep.length];

        for (int i = 0; i < sep.length; i++) {
            valOut[i] = Integer.parseInt(sep[i]);
        }

        return valOut;
    }

    public void refresh(double x, double y) {
        int[] hej = {};
        String url = urlBase;
        int id = ds.AGVid;
        url = url + id + "&x=" + x + "&y=" + y;
        try {
            URL urlobjekt = new URL(url);
            HttpURLConnection anslutning = (HttpURLConnection) urlobjekt.openConnection();
            System.out.println("\nAnropar: " + url);
            int mottagen_status = anslutning.getResponseCode();
            System.out.println("Statuskod: " + mottagen_status);
            BufferedReader inkommande = new BufferedReader(new InputStreamReader(anslutning.getInputStream()));
            String inkommande_text;
            StringBuffer inkommande_samlat = new StringBuffer();
            while ((inkommande_text = inkommande.readLine()) != null) {
                inkommande_samlat.append(inkommande_text);
            }

            inkommande.close();
            lastOut = inkommande_samlat.toString();
            hej = convertOutPos(lastOut);
            ds.EnemyX = hej[1];
            ds.EnemyY = hej[2];

        } catch (Exception e) {
            System.out.print(e.toString());

        }

    }

    public int[] getAssignedTasks() {
        String tasklist = readTasks();
        int[] tasklistInt = convertOutPos(tasklist);
        int[] assigned = new int[tasklistInt.length];
        int id = ds.AGVid;

        int k = 0;
        for (int i = 0; i < tasklistInt.length; i++) {
            if (i % 2 == 0 && tasklistInt[i + 1] == id) {
                assigned[k] = tasklistInt[i];
                k++;
            }
        }
        int[] out = new int[k];
        for (int i = 0; i < k; i++) {
            out[i] = assigned[i];
        }
        return out;

    }

    public String dealtask(ArrayList<Integer> DesiredShelves) throws InterruptedException {
        int[] wishlist = new int[DesiredShelves.size()];
        for (int i = 0; i < DesiredShelves.size(); i++) {
            wishlist[i] = DesiredShelves.get(i);
        }

        String state;
        String tasklist;

        int index = 0;

        while (index != -1) {
            state = assigntask(wishlist);
            tasklist = readTasks();
            int[] intlist = convertOutPos(tasklist);

            Integer[] aInt = new Integer[intlist.length];
            Arrays.setAll(aInt, i -> intlist[i]);

            index = Arrays.asList(aInt).indexOf(0);
            if ("OK".equals(state)) {
                TimeUnit.SECONDS.sleep(5);
                System.out.print("OK");
                if (index == -1) {
                    break;
                }
                wishlist = null;
                wishlist = new int[]{intlist[index - 1]};
            } else if ("FAIL".equals(state)) {
                wishlist = null;
                wishlist = new int[]{intlist[index - 1]};
                TimeUnit.SECONDS.sleep(5);
                System.out.print("FAIL");
            }

        }
        return "Task deal failed";
    }

    public String assigntask(int[] taskList) {
        String task = "https://n7.se/assigntasks.php?id=";
        int id;
        id = ds.AGVid;
        task = task + id + "&t=";
        for (int i = 0; i < taskList.length; i++) {
            task = task + taskList[i];
            if (i < taskList.length - 1) {
                task = task + ",";
            }
        }

        try {

            URL oracle = new URL(task);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                return inputLine;
            }
            in.close();

        } catch (Exception e) {
            System.out.print(e.toString());
        }
        String wrong = "FEL";
        return wrong;
    }

    public String readTasks() {
        String url = "https://n7.se/listtasks.php";

        try {
            URL urlobjekt = new URL(url);
            HttpURLConnection anslutning = (HttpURLConnection) urlobjekt.openConnection();
            System.out.println("\nAnropar: " + url);
            int mottagen_status = anslutning.getResponseCode();
            System.out.println("Statuskod: " + mottagen_status);
            BufferedReader inkommande = new BufferedReader(new InputStreamReader(anslutning.getInputStream()));
            String inkommande_text;
            StringBuffer inkommande_samlat = new StringBuffer();
            while ((inkommande_text = inkommande.readLine()) != null) {
                inkommande_samlat.append(inkommande_text);
            }

            inkommande.close();
            return inkommande_samlat.toString();

        } catch (Exception e) {
            System.out.print(e.toString());
        }
        String wrong = "FEL";
        return wrong;
    }

}
