package com.zinalabs.orthodoxmezmur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ListMezmurActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView mezmurList;
    InputStream in;
    Toolbar toolbar;
    static JSONArray data;
    Activity context;
    Bundle extras;
    int catId= 0;
    String catTitle= null;
    TextView contemoraryTitle;
    RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_mezmur);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //      mezmurList = (ListView) findViewById(R.id.lists);
//        mezmurList.setOnItemClickListener(this);
        context = this;

        //contemoraryTitle = (TextView) findViewById(R.id.contemoraryTitle);
        //mezmurList = (ListView) findViewById(R.id.listmezmur);
        in = this.getResources().openRawResource(R.raw.index);
        extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("catId")) {
                catId = extras.getInt("catId");
                catTitle = extras.getString("catTitle");
            }
        } else {
            catId = MainActivity.CAT_ID;
            catTitle = MainActivity.CAT_TITLE;
        }
        getSupportActionBar().setTitle(catTitle);
        new GettingData().execute();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);



        try {
            Constant.bookMarkedIds=getBookmarkedIds();
            inflateBookmarkedList(R.id.bookmarkedList, Constant.bookMarkedIds);
        } catch (JSONException | IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        ListView bookMarkedList= (ListView) findViewById(R.id.bookmarkedList);
        bookMarkedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent MezmurActivity = new Intent(ListMezmurActivity.this, MezmurActivity.class);
                int mezmurId=0;
                try {
                    mezmurId = (int) Constant.bookMarkedIds.get(i);
                    //Log.v("Mezmur", mezmurId+" from "+Constant.bookMarkedIds.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MezmurActivity.putExtra("id", mezmurId);
                startActivity(MezmurActivity);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.hello_world, R.string.hello_world){

            public void onDrawerClosed(View view)
            {
                supportInvalidateOptionsMenu();
                //drawerOpened = false;
            }

            public void onDrawerOpened(View drawerView)
            {
                supportInvalidateOptionsMenu();
                //drawerOpened = true;
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //mDrawerToggle.setDrawerIndicatorEnabled(true);
        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();




    }
    private JSONArray getBookmarkedIds() throws JSONException {
        SharedPreferences prefs = getSharedPreferences("Mezmur", 0);
        String s= prefs.getString("bookMarkedMez", null);
        JSONArray toReturn= new JSONArray();
        if(s != null){
            try {
                JSONArray a= new JSONArray(s);
                int count=a.length();

                for(int i=0; i < count; i++){
                    if(Integer.parseInt(a.get(i).toString()) != 0){
                        toReturn.put(a.get(i));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return toReturn;
    }

    private void inflateBookmarkedList(int ListViewId, JSONArray bookMarkedData) throws JSONException, IOException, SAXException, ParserConfigurationException {
        //BOOKMARKED = bookMarkedData;
        Log.e("Mezmur", bookMarkedData.toString());
        int count= bookMarkedData.length();
        String[] toReturn = new String[count];
        for(int i= 0; i < count; i++){
            int mezmurID=bookMarkedData.getInt(i);
            //String[] MEZdata= getMezmurById(context, mezmurID);
            String[] MEZdata=getMezmurById(this, mezmurID);
            Log.e("Mezmur", mezmurID + MEZdata[1]);
            toReturn[i]= MEZdata[1];
            //TODO: Inflate the data in to listView
        }

        if(toReturn.length != 0){
            ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,toReturn);
            ListView bookMarkedList= (ListView) findViewById(ListViewId);
            bookMarkedList.setAdapter(adapter);
        }

    }

    public String[] getMezmurById(Activity context, int mezmurId) throws ParserConfigurationException, IOException, SAXException {
        String[] returna=new String[4];
        // id
        // title
        // azmach
        // teref
        InputStream in= context.getResources().openRawResource(R.raw.index);

        DocumentBuilderFactory documentBuilderFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement=xmlDocument.getDocumentElement();

        NodeList nodes=rootElement.getElementsByTagName("mezmur"); // bigData TAG

        Node myNode=nodes.item(mezmurId-1);
        NodeList myNodeChildren=myNode.getChildNodes();

        returna[0] = myNode.getAttributes().getNamedItem("id").getTextContent();
        returna[1] = myNode.getAttributes().getNamedItem("title").getTextContent();

        for (int i=0;i < myNodeChildren.getLength(); i++){
            Node currentChild=myNodeChildren.item(i);
            if(currentChild.getNodeName().equalsIgnoreCase("azmach")){
                returna[2] = currentChild.getTextContent().toString();
            }else if(currentChild.getNodeName().equalsIgnoreCase("teref")){
                returna[3] = currentChild.getTextContent().toString();
            }

        }

        return returna;
    }

    private void inflateData(){
        String[] toAdapt=null;
        if(data != null) {
            if (data.length() != 0) {
                toAdapt = new String[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject mezmur;

                    try {

                        mezmur = (JSONObject) data.get(i);
                        Log.v("TAG", mezmur.toString());
                        toAdapt[i] = mezmur.getString("title");

                    } catch (JSONException e) {
                        Log.e("TAG", e.getMessage());
                    }

                }

                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toAdapt);
                //mezmurList.setAdapter(adapter);

                ArrayList<String> myDataset= convertStringArrayToArraylist(toAdapt);


                mAdapter = new MyAdapter(myDataset, mRecyclerView);
                mRecyclerView.setAdapter(mAdapter);

                Log.d("Mezmur", data.toString());

            }
        }
    }

    public static ArrayList<String> convertStringArrayToArraylist(String[] strArr){
        ArrayList<String> stringList = new ArrayList<String>();
        for (String s : strArr) {
            stringList.add(s);
        }
        return stringList;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        try {
            Intent MezmurActivity = new Intent(ListMezmurActivity.this, MezmurActivity.class);
            JSONObject o= (JSONObject) data.get(position);
            int mezmurId= o.getInt("id");
            MezmurActivity.putExtra("id", mezmurId);
            startActivity(MezmurActivity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class GettingData extends AsyncTask<Void, Integer, Void> {



        @Override
        protected Void doInBackground(Void... voids) {

            try {

                data=getMezmursByCid(context,catId);

                //getCategoryById(context, 1);
            } catch (ParserConfigurationException e) {
                Log.e("TAG",e.getMessage());
            } catch (IOException e) {
                Log.e("TAG",e.getMessage());
            } catch (SAXException e) {
                Log.e("TAG",e.getMessage());
            } catch (JSONException e) {
                Log.e("TAG",e.getMessage());
            }
            return null;
        }


        protected void onPostExecute(Void result) {
            inflateData();
        }
    }

    public JSONArray getMezmursByCid(Activity context, int categoryId) throws ParserConfigurationException, IOException, SAXException, JSONException {
        JSONArray returna=new JSONArray();
        //Toast.makeText(context,"I made it",Toast.LENGTH_LONG).show();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement = xmlDocument.getDocumentElement();

        NodeList nodes = rootElement.getElementsByTagName("mezmur"); // bigData TAG

        //Node myNode=nodes.item(mezmurId - 1);
        //NodeList myNodeChildren=myNode.getChildNodes();
        int count = nodes.getLength();
//        Toast.makeText(context, "I ma " + count, Toast.LENGTH_LONG).show();
        int j=0;
        for (int i = 0; i < count; i++) {
            Node mezmur = nodes.item(i);

            if (mezmur.getAttributes().getNamedItem("category") != null) {

                String catInt = mezmur.getAttributes().getNamedItem("category").getTextContent();
                int catId = Integer.parseInt(catInt);

                if (catId == categoryId) {
                    Log.e("TAG",j+"---");
                    String mezInt = mezmur.getAttributes().getNamedItem("id").getTextContent();
                    String mezTitle = mezmur.getAttributes().getNamedItem("title").getTextContent();

                    int mezmurId = Integer.parseInt(mezInt);
                    JSONObject a= new JSONObject();
                    a.put("id",mezmurId);
                    a.put("title",mezTitle);

                    returna.put(j,a);
                    j++;
                }

            }

        }
        Log.d("TAG", returna.toString());
        return returna;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        MenuItem settings=menu.findItem(R.id.action_settings);
//        MenuItem search=menu.findItem(R.id.action_search);
//
//        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//
//
//                return false;
//            }
//        });
//
//        //final TextView mezmur= (TextView) findViewById(R.id.mezmur);
//        SearchView searchView= (SearchView) MenuItemCompat.getActionView(search);
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                //Toast.makeText(MainActivity.this,"Submitted query"+query,Toast.LENGTH_LONG).show();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                //mezmur.setText(newText);
//                return false;
//            }
//        });

        return true;
    }


}
