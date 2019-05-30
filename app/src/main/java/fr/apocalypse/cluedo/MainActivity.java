package fr.apocalypse.cluedo;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    ArrayList<CheckBox> cbs = new ArrayList<>();
    int version = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            FileInputStream firstRun = openFileInput("version_0.json");
            firstRun.close();
        } catch (FileNotFoundException e) {
            initializeVersion();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button bp_reset = (Button)findViewById(R.id.bp_reset);
        bp_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
        ImageButton bp_edit = (ImageButton)findViewById(R.id.bp_edit);
        bp_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("clicked!");
                Toast.makeText(MainActivity.this, R.string.not_edit_yet, Toast.LENGTH_SHORT).show();
            }
        });
        Spinner spinner = (Spinner)findViewById(R.id.menu);
        generateVersionsList();
        loadGame();
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveGame();
    }

    //GUI Interactions
    protected void createList(String title, String[] items){
        int id = getResources().getIdentifier(title, "id", getPackageName());
        LinearLayout root = (LinearLayout)findViewById(id);
        TextView label = new TextView(this);
        id = getResources().getIdentifier(title, "string", getPackageName());
        label.setText(getString(id));
        label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        label.setTypeface(null, Typeface.BOLD);
        root.addView(label);

        for(int i=0; i < items.length; i++)
        {
            LinearLayout l = new LinearLayout(this);
            TextView t = new TextView(this);
            CheckBox c = new CheckBox(this);
            t.setText(items[i]);
            t.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,0.5f));
            c.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,4.0f));
            l.addView(t);
            l.addView(c);
            root.addView(l);
            cbs.add(c);
        }
    }
    protected void generateVersionsList(){
        Spinner spinner = (Spinner)findViewById(R.id.menu);
        ArrayList<String> versions = new ArrayList<>();

        File dir = getFilesDir();
        String[] l = dir.list();
        for(int i = 0; i < l.length; i++)
        {
            if(l[i].startsWith("version_")){
                try{
                    FileInputStream inputStream = openFileInput(l[i]);
                    int size = inputStream.available();
                    byte[] b = new byte[size];
                    inputStream.read(b);
                    inputStream.close();
                    JSONObject json = new JSONObject(new String(b, "UTF-8"));
                    String n = json.getString("name");
                    versions.add(n);

                    log("add version \"" + n + "\" to spinner");
                }
                catch (Exception e){
                    log("Error adding version to spinner");
                    e.printStackTrace();
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, versions);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }
    protected void reset(){
        Iterator<CheckBox> it = cbs.iterator();
        while(it.hasNext())
        {
            it.next().setChecked(false);
        }
        log("reset");
    }

    //GAME TOOLS
    protected void saveGame(){
        log("saving game...");

        JSONObject json = new JSONObject();
        try
        {
            json.put("version", getVersion());
            json.put("size", cbs.size());
            Iterator<CheckBox> it = cbs.iterator();
            int i = 0;
            while(it.hasNext())
            {
                json.put("checked_" + i,  it.next().isChecked());
                i++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            log("save failed on JSON!");
        }

        String filename = "save.json";
        String fileContents = json.toString();
        log("loaded:" + fileContents);
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            log("save failed on file!");
        }
        log("saved!");
    }
    protected void loadGame(){
        log("loading game...");
        String filename = "save.json";
        String fileContents = "";
        FileInputStream inputStream;
        JSONObject json;
        try{
            inputStream = openFileInput(filename);
            int size = inputStream.available();
            byte[] b = new byte[size];
            inputStream.read(b);
            inputStream.close();
            fileContents = new String(b, "UTF-8");

            log("loaded:" + fileContents);
            json = new JSONObject(fileContents);
            version = json.getInt("version");
            setVersion(version);
            size = json.getInt("size");
            Iterator<CheckBox> it = cbs.iterator();
            int i = 0;
            while(it.hasNext())
            {
                it.next().setChecked(json.getBoolean("checked_" + i));
                i++;
            }

        }
        catch(Exception e)
        {
            log("load game failed!");
            e.printStackTrace();
        }
    }

    //VERSION TOOLS
    protected int getVersion() {
        return version;
    }
    protected void setVersion(int version) {
        log("setting version "+version+"...");
        this.version = version;
        try{
            String filename = "version_" + this.version + ".json";
            String fileContents = "";
            FileInputStream inputStream = openFileInput(filename);
            int size = inputStream.available();
            byte[] b = new byte[size];
            inputStream.read(b);
            inputStream.close();
            fileContents = new String(b, "UTF-8");
            JSONObject json = new JSONObject(fileContents);

            LinearLayout layout;
            String[] l;

            cbs.clear();

            //Suspects
            layout = (LinearLayout)findViewById(R.id.suspects);
            layout.removeAllViews();
            size = json.getInt("suspects_size");
            l = new String[size];
            for(int i = 0; i < size; i++)
            {
                l[i] = json.getString("suspect_"+i);
            }
            createList("suspects", l);
            
            //Weapons
            layout = (LinearLayout)findViewById(R.id.weapons);
            layout.removeAllViews();
            size = json.getInt("weapons_size");
            l = new String[size];
            for(int i = 0; i < size; i++)
            {
                l[i] = json.getString("weapon_"+i);
            }
            createList("weapons", l);
            
            //Locations
            layout = (LinearLayout)findViewById(R.id.locations);
            layout.removeAllViews();
            size = json.getInt("locations_size");
            l = new String[size];
            for(int i = 0; i < size; i++)
            {
                l[i] = json.getString("location_"+i);
            }
            createList("locations", l);

            Spinner spinner = (Spinner)findViewById(R.id.menu);
            spinner.setSelection(this.version);
        }
        catch(Exception e){
            e.printStackTrace();
            log("set version failed");
        }
        log("version setted");
    }
    protected void initializeVersion() {
        log("generating version 0...");
        JSONObject json = new JSONObject();

        try {   //Version 0
            String[] suspects = {"Moutarde","Rose","Leblanc","Olive","Pervenche","Violet"};
            String[] weapons = {"Poignard", "Revolver", "Chandelier", "Corde", "Clé anglaise", "Matraque"};
            String[] locations = {"Cuisine", "Grand Salon", "Petit Salon", "Salle à Manger", "Bureau", "Bibliothèque", "Véranda", "Hall", "Salle de Billard"};
            json.put("version", 0);
            json.put("name", "Cluedo 1987(Edition Parker)");
            json.put("suspects_size", suspects.length);
            for(int i=0; i < suspects.length;i++)
            {
                json.put("suspect_"+i, suspects[i]);
            }
            json.put("weapons_size", weapons.length);
            for(int i=0; i < weapons.length;i++)
            {
                json.put("weapon_"+i, weapons[i]);
            }
            json.put("locations_size", locations.length);
            for(int i=0; i < locations.length;i++)
            {
                json.put("location_"+i, locations[i]);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();

            log("generating version 0 failed");
        }
        saveVersion(json);

        try {   //Version 1
            String[] suspects = {"Moutarde","Rose","Leblanc","Olive","Pervenche","Violet", "Chose", "Pêche", "Legris", "Prunelle"};
            String[] weapons = {"Poignard", "Revolver", "Chandelier", "Corde", "Clé anglaise", "Matraque", "Fiole de poison", "Fer à cheval"};
            String[] locations = {"Cuisine", "Grand Salon", "Petit Salon", "Salle à Manger", "Bureau", "Bibliothèque", "Véranda", "Hall", "Fontaine", "Kiosque", "Grange", "Jardin"};
            json.put("version", 1);
            json.put("name", "Super Cluedo(1990/2000 Wikipedia)");
            json.put("suspects_size", suspects.length);
            for(int i=0; i < suspects.length;i++)
            {
                json.put("suspect_"+i, suspects[i]);
            }
            json.put("weapons_size", weapons.length);
            for(int i=0; i < weapons.length;i++)
            {
                json.put("weapon_"+i, weapons[i]);
            }
            json.put("locations_size", locations.length);
            for(int i=0; i < locations.length;i++)
            {
                json.put("location_"+i, locations[i]);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();

            log("generating version 0 failed");
        }
        saveVersion(json);

        setVersion(0);
    }
    protected void saveVersion(JSONObject json) {
        try {
            String filename = "version_" + json.getInt("version") + ".json";
            String fileContents = json.toString();
            FileOutputStream outputStream = openFileOutput(filename, MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            log("saveVersion failed");
        }
    }
    //TOOLS
    public void log(String message)
    {
        Log.d("[CLUEDO]", message);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        log(item + "selected");
        if(position!=version)
        {
            setVersion(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
