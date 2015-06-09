package com.fyp.filetransfer2;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class FileChooser extends ListActivity {

    private File currentDir;
    private static FileArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);

        currentDir = new File("/sdcard/");
        fill(currentDir);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //---------------------------------------------------
    //get all the files and folder for the current directory
    private void fill(File f)
    {
        File[] dirs = f.listFiles();
        this.setTitle("Current Dir: " +  f.getName());

        List<Option> dir = new ArrayList<Option>(); //Folders
        List<Option> fls = new ArrayList<Option>(); //Files


        try
        {
            for(File ff: dirs)
            {
                if(ff.isDirectory())
                {
                    //Folders
                    dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                }
                else
                {
                    //Files
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                }
            }
        }
        catch(Exception e)
        {}

        Collections.sort(dir);
        Collections.sort(fls);

        dir.addAll(fls);

        if(!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0,new Option("..", "Parent Directory", f.getParent()));

        adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
        this.setListAdapter(adapter);
    }


}
