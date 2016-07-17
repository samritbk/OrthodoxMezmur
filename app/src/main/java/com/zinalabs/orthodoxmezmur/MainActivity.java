package com.zinalabs.orthodoxmezmur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    TextView mezmurTV;
    ListView lists;
    String[] items;
    InputStream in;
    Toolbar toolbar;
    DrawerLayout mDrawer;
    Activity context;
    public static int CAT_ID=0;
    public static String CAT_TITLE=null;
    JSONArray BOOKMARKED;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        context = this;
        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);


        DateTime dtISO = new DateTime(year, month, date,12,0,0,0);

        // find out what the same instant is using the Ethiopic Chronology
        DateTime dtEthiopic = dtISO.withChronology(EthiopicChronology.getInstance());
        Toast.makeText(this, dtEthiopic.toString()+"\n"+date+month+year, Toast.LENGTH_LONG).show();

        lists = (ListView) findViewById(R.id.lists);

        items=getResources().getStringArray(R.array.mezmur_category);

        if(items.length != 0){
            ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
            lists.setAdapter(adapter);
        }
        lists.setOnItemClickListener(this);


        try {
            Log.v("Mezmur", getBookmarkedIds().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            inflateBookmarkedList(R.id.bookmarkedList, getBookmarkedIds());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.hello_world, R.string.hello_world)
        {

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
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private  void inflateBookmarkedList(int ListViewId, JSONArray bookMarkedData) throws JSONException, IOException, SAXException, ParserConfigurationException {
        BOOKMARKED = bookMarkedData;
        int count= bookMarkedData.length();
        String[] toReturn = new String[count];
        for(int i= 0; i < count; i++){
            int mezmurID=bookMarkedData.getInt(i);
            String[] MEZdata= getMezmurById(context, mezmurID);
            toReturn[i]= MEZdata[1];
            Log.d("Mezmur", toReturn[i].toString());
//TODO: Inflate the data in to listView
        }
        Log.d("Mezmur", toReturn.toString());
        if(toReturn.length != 0){
            ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,toReturn);
            ListView bookMarkedList= (ListView) findViewById(R.id.bookmarkedList);
            bookMarkedList.setAdapter(adapter);
        }

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
                    if(a.get(i) != 0){
                        toReturn.put(a.get(i));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return toReturn;
    }


    public void getCategoryById(Activity context, int categoryId) throws ParserConfigurationException, IOException, SAXException {
        String[] returna=new String[4];
        //Toast.makeText(context,"I made it",Toast.LENGTH_LONG).show();

        DocumentBuilderFactory documentBuilderFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement=xmlDocument.getDocumentElement();

        NodeList nodes=rootElement.getElementsByTagName("mezmur"); // bigData TAG

        //Node myNode=nodes.item(mezmurId - 1);
        //NodeList myNodeChildren=myNode.getChildNodes();
        int count=nodes.getLength();
        //Toast.makeText(context,"I ma "+count,Toast.LENGTH_LONG).show();
        for(int i=0; i < count; i++){
            Node mezmur=nodes.item(i);

            if(mezmur.getAttributes().getNamedItem("category") != null){

                String catInt= mezmur.getAttributes().getNamedItem("category").getTextContent();
                int catId= Integer.parseInt(catInt);
                if(catId == categoryId) {
                    String mezInt= mezmur.getAttributes().getNamedItem("id").getTextContent();
                    int mezmurId= Integer.parseInt(mezInt);
                    Log.d("TAG",mezmurId+"");
                }

            }


        }
        //returna[0] = myNode.getAttributes().getNamedItem("id").getTextContent();
        //returna[1] = myNode.getAttributes().getNamedItem("title").getTextContent();
//
//        for (int i=0;i < myNodeChildren.getLength(); i++){
//            Node currentChild=myNodeChildren.item(i);
//            if(currentChild.getNodeName().equalsIgnoreCase("azmach")){
//                returna[2] = currentChild.getTextContent().toString();
//            }else if(currentChild.getNodeName().equalsIgnoreCase("teref")){
//                returna[3] = currentChild.getTextContent().toString();
//            }
//
//        }
//
//        return returna;
    }


    public String getMezmurFromXml(Activity activity) throws IOException, XmlPullParserException {
        StringBuffer stringBuffer = new StringBuffer();
        Resources res = activity.getResources();
        XmlPullParser xpp = res.getXml(R.xml.index);
        xpp.next();
        int eventType = xpp.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT){
            if(eventType == XmlPullParser.START_TAG){
                //Toast.makeText(MainActivity.this,xpp.getName(),Toast.LENGTH_LONG).show();

                    while (xpp.getEventType() == XmlPullParser.END_TAG && xpp.getName() == null) {
                        xpp.nextTag();
                        Toast.makeText(MainActivity.this,xpp.getName().toString(),Toast.LENGTH_LONG).show();
                    }

                        //xpp.next();

                        String name=xpp.getName();
                        if(name != null){
                            if(name.equals("mezmur")){
                                stringBuffer.append(xpp.getAttributeValue(null,"title")+"\n");
                            }else if (name.equals("azmach")) {
                                if(xpp.getText() != null){
                                    Toast.makeText(MainActivity.this,xpp.getText().toString(),Toast.LENGTH_LONG).show();
                                    stringBuffer.append(xpp.getText() + "\n");
                                }
                            }
                        }

            }
            eventType = xpp.next();

        }
        return  stringBuffer.toString();
    }


    public void processXml(Activity context) throws ParserConfigurationException, IOException, SAXException {

        InputStream in= context.getResources().openRawResource(R.raw.index);

        DocumentBuilderFactory documentBuilderFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement=xmlDocument.getDocumentElement();

        NodeList nodes=rootElement.getElementsByTagName("mezmur"); // bigData TAG
        StringBuffer sb=new StringBuffer();
        for(int i=0; i < nodes.getLength(); i++){
            NodeList newNode=nodes.item(i).getChildNodes(); // Each Mezmur TAG


            Toast.makeText(context, newNode.getLength()+"",Toast.LENGTH_LONG).show();
            Node currentChild=null;
            String currentChildName= null;
            for(int j=0; j < newNode.getLength(); j++){
                currentChild = newNode.item(j);
                currentChildName = newNode.item(j).getNodeName();
                if(currentChildName.equalsIgnoreCase("azmach"))
                    Log.d("TAG", currentChild.getTextContent());
                sb.append(newNode.item(i).getNodeName() + "\n");
            }

        }

        Toast.makeText(context,sb.toString(),Toast.LENGTH_LONG).show();

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

        Node myNode=nodes.item(mezmurId);
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

    public String[] getAllMezmurNames(Activity context) throws ParserConfigurationException, IOException, SAXException {


        InputStream in= context.getResources().openRawResource(R.raw.index);

        DocumentBuilderFactory documentBuilderFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement=xmlDocument.getDocumentElement();

        NodeList nodes=rootElement.getElementsByTagName("mezmur"); // bigData TAG

        int count=nodes.getLength();
        String[] returna=new String[count];
        for(int i=0;i < count;i++){
            Node myNode=nodes.item(i);
            if(myNode.getAttributes().getNamedItem("title") != null)
                returna[i] = myNode.getAttributes().getNamedItem("title").getTextContent();
        }

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
                Toast.makeText(context, newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent mezmurActivity = new Intent(MainActivity.this,ListMezmurActivity.class);
        mezmurActivity.putExtra("catId",position+1);
        mezmurActivity.putExtra("catTitle",items[position]);
        CAT_ID= position+1;
        CAT_TITLE= items[position];
        startActivity(mezmurActivity);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
