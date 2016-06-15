package com.zinalabs.orthodoxmezmur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ListMezmurActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView mezmurList;
    InputStream in;
    Toolbar toolbar;
    JSONArray data;
    Activity context;
    Bundle extras;
    int catId= 0;
    String catTitle= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_mezmur);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=  null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        mezmurList = (ListView) findViewById(R.id.lists);
        mezmurList.setOnItemClickListener(this);
        context = this;

        //mezmurList = (ListView) findViewById(R.id.listmezmur);
        in = this.getResources().openRawResource(R.raw.index);
        extras = getIntent().getExtras();
        catId = extras.getInt("catId");
        catTitle=extras.getString("catTitle");
        getSupportActionBar().setTitle(catTitle);

//        try {
//            //processXml(this);
//            Bundle extras = getIntent().getExtras();
//            int catId = extras.getInt("catId");
//            String catTitle=extras.getString("catTitle");
//            data=getMezmursByCid(this,catId);
//
//            Toast.makeText(this, data.toString(), Toast.LENGTH_LONG).show();
//
//        } catch (ParserConfigurationException e) {
//            Log.e("TAG",e.getMessage());
//        } catch (IOException e) {
//            Log.e("TAG",e.getMessage());
//        } catch (SAXException e) {
//            Log.e("TAG",e.getMessage());
//        } catch (JSONException e) {
//            Log.e("TAG",e.getMessage());
//        }

        new GettingData().execute();

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

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toAdapt);
                mezmurList.setAdapter(adapter);
            }
        }
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem settings=menu.findItem(R.id.action_settings);
        MenuItem search=menu.findItem(R.id.action_search);

        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                return false;
            }
        });

        //final TextView mezmur= (TextView) findViewById(R.id.mezmur);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(MainActivity.this,"Submitted query"+query,Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //mezmur.setText(newText);
                return false;
            }
        });

        return true;
    }


}
