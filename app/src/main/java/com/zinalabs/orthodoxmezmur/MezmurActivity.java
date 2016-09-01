package com.zinalabs.orthodoxmezmur;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MezmurActivity extends AppCompatActivity implements View.OnClickListener{

    Context context;
    TextView mezmurTV;
    InputStream in;
    Toolbar toolbar;
    int mezmurId;
    EditText cat_et;
    Button change_button;
    View all;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mezmur);

        //ScrollingMovementMethod.getInstance();
        context = this;
        all=findViewById(R.id.all);
        cat_et= (EditText) findViewById(R.id.cat_et);
        change_button= (Button) findViewById(R.id.change_button);
        mezmurTV = (TextView) findViewById(R.id.mezmur);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=  null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        change_button.setOnClickListener(this);
        in= this.getResources().openRawResource(R.raw.index);

        try {
            //processXml(this);
            Bundle extras=getIntent().getExtras();
            mezmurId=extras.getInt("id");
            String[] s=getMezmurById(this, mezmurId);
            setTitle(s[1]);
            String organizedMezmur=null;
            if(s[3] != null) {
                organizedMezmur = mezmurOrg(s[2], s[3]);
            }else{
                organizedMezmur = mezmurOrg(s[2], null);
            }
            toolbar.setTitle(s[0]);
            Log.d("Mezmur", mezmurId+"");
            mezmurTV.setText("\n" + s[1] +"\n" + organizedMezmur);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }


    public String mezmurOrg(String azmach,String teref){

        StringBuilder myMezmur=new StringBuilder();
        String divider = "\n *** \n";
        myMezmur.append(azmach);

        if(teref != null){
            myMezmur.append(divider);
            teref=teref.replace("\n *** \n", "\n***\n");
            teref=teref.replace("***","\n\n***\n");
            myMezmur.append(teref);
        }

        return myMezmur.toString();
    }

    public String getMezmurFromXml(Activity activity) throws IOException, XmlPullParserException {
        StringBuilder stringBuffer = new StringBuilder();
        Resources res = activity.getResources();
        XmlPullParser xpp = res.getXml(R.xml.index);
        xpp.next();
        int eventType = xpp.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT){
            if(eventType == XmlPullParser.START_TAG){
                //Toast.makeText(MainActivity.this,xpp.getName(),Toast.LENGTH_LONG).show();

                while (xpp.getEventType() == XmlPullParser.END_TAG && xpp.getName() == null) {
                    xpp.nextTag();
                    Toast.makeText(MezmurActivity.this,xpp.getName().toString(),Toast.LENGTH_LONG).show();
                }

                String name=xpp.getName();
                if(name != null){
                    if(name.equals("mezmur")){
                        stringBuffer.append(xpp.getAttributeValue(null,"title")+"\n");
                    }else if (name.equals("azmach")) {
                        if(xpp.getText() != null){
                            Toast.makeText(MezmurActivity.this,xpp.getText().toString(),Toast.LENGTH_LONG).show();
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

        DocumentBuilderFactory documentBuilderFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement=xmlDocument.getDocumentElement();

        NodeList nodes=rootElement.getElementsByTagName("mezmur"); // bigData TAG
        StringBuffer sb=new StringBuffer();
        for(int i=0; i < nodes.getLength(); i++){
            NodeList newNode=nodes.item(i).getChildNodes(); // Each Mezmur TAG


            //Toast.makeText(context, newNode.getLength()+"",Toast.LENGTH_LONG).show();
            Node currentChild;
            String currentChildName;
            for(int j=0; j < newNode.getLength(); j++){
                currentChild = newNode.item(j);
                currentChildName = newNode.item(j).getNodeName();
                if(currentChildName.equalsIgnoreCase("azmach"))
                    Log.d("TAG", currentChild.getTextContent());
                sb.append(newNode.item(i).getNodeName() + "\n");
            }

        }

        //Toast.makeText(context,sb.toString(),Toast.LENGTH_LONG).show();

    }

    public String[] getMezmurIdByCategory(int catId) throws ParserConfigurationException, IOException, SAXException {
        String[] returna=new String[4];


        DocumentBuilderFactory documentBuilderFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement=xmlDocument.getDocumentElement();

        NodeList nodes=rootElement.getElementsByTagName("mezmur"); // bigData TAG

        int mezmur=nodes.getLength();

//        Node myNode=nodes.item(mezmurId - 1);
//        NodeList myNodeChildren=myNode.getChildNodes();
//
//        returna[0] = myNode.getAttributes().getNamedItem("id").getTextContent();
//        returna[1] = myNode.getAttributes().getNamedItem("title").getTextContent();
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

        return returna;
    }


    public String[] getMezmurById(Activity context, int mezmurId) throws ParserConfigurationException, IOException, SAXException {
        String[] returna=new String[4];

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
            String toPut= currentChild.getTextContent().toString();
            if(currentChild.getNodeName().equalsIgnoreCase("azmach")){
                returna[2] = toPut;
            }else if(currentChild.getNodeName().equalsIgnoreCase("teref")){
                returna[3] = toPut;
            }

        }

        return returna;
    }


    public void changeMezmurCatById(Context context, int mezmurId, String newValue) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory documentBuilderFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(in);

        org.w3c.dom.Element rootElement=xmlDocument.getDocumentElement();

        NodeList nodes=rootElement.getElementsByTagName("mezmur"); // bigData TAG

        Node myNode=nodes.item(mezmurId-1);
        //NodeList myNodeChildren=myNode.getChildNodes();
        Toast.makeText(context, myNode.getAttributes().getNamedItem("category").getTextContent(),Toast.LENGTH_LONG).show();
        //----------------------------------------------------------------------------
        //myNode.getAttributes().getNamedItem("category").setTextContent(newValue);
        myNode.getAttributes().getNamedItem("category").setNodeValue(newValue);
        //-----------------------------------------------------------------------------

    }

    private boolean checkIfMezmurIsBookmarked(int mezmurId){
        SharedPreferences prefs = getSharedPreferences("Mezmur", 0);
        String s= prefs.getString("bookMarkedMez", null);
        boolean toReturn= false;
        if(s != null){
            try {
                JSONArray a= new JSONArray(s);
                int count=a.length();

                for(int i=0; i < count; i++){
                    if(Integer.parseInt(a.get(i).toString()) == mezmurId){
                        toReturn = true;
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return toReturn;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mezmur, menu);

        if(checkIfMezmurIsBookmarked(mezmurId)){

            MenuItem action_button = menu.findItem(R.id.action_bookmark);
            if(action_button.isEnabled()) {
                Drawable d = getResources().getDrawable(R.drawable.ic_bookmark_white_48dp);
                action_button.setIcon(d);
            }

        }
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
        }else if(id == R.id.action_category_changer){

//            cat_et.setVisibility(View.VISIBLE);
//            change_button.setVisibility(View.VISIBLE);
//            cat_et.setFocusableInTouchMode(true);
//            cat_et.setFocusable(true);
//            cat_et.requestFocus();

        }else if (id== R.id.action_bookmark){

            makeBookmarkAndToggleIC(mezmurId, item);

        }

        return super.onOptionsItemSelected(item);
    }
    private void makeBookmarkAndToggleIC(int mezmurId, MenuItem item){
        Drawable d = getResources().getDrawable(R.drawable.ic_bookmark_border_white_48dp);
        SharedPreferences prefs = getSharedPreferences("Mezmur", 0);
        SharedPreferences.Editor editor = prefs.edit();
        String prefMez=prefs.getString("bookMarkedMez",null);

        if(!checkIfMezmurIsBookmarked(mezmurId)){

            if(prefMez == null){// If the shared preferences is null

                JSONArray json= new JSONArray();
                json.put(mezmurId);
                editor.putString("bookMarkedMez", json.toString());
                editor.apply();

                Log.i("Mezmur", prefs.getString("bookMarkedMez", null));
                showSavedSnack();

            }else{

                if(prefMez != null){
                    JSONArray s= null;
                    try {
                        s = new JSONArray(prefMez);
                        s.put(mezmurId);
                        editor.putString("bookMarkedMez", s.toString());
                        editor.apply();
                        showSavedSnack();
                    } catch (JSONException e) {

                    }
                }else{
                    Log.e("Mezmur","PROBLEMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
                }
            }
        }else{
            if(prefMez != null) {
                JSONArray s = null;
                try {
                    s = new JSONArray(prefMez);
                    int count = s.length();
                    for(int i=0; i < count; i++){
                        if(Integer.parseInt(s.get(i).toString()) == mezmurId){
                            s.put(i, 0); break;
                        }
                    }
                    editor.putString("bookMarkedMez", s.toString());
                    editor.apply();
                    Log.i("Mezmur", "madeOne 0" + prefs.getString("bookMarkedMez", null));
                } catch (JSONException e) {
                    Log.i("Mezmur", e.getMessage());
                }
            }
        }


        if(item.getIcon().getConstantState().equals(d.getConstantState())) {
            item.setIcon(R.drawable.ic_bookmark_white_48dp);
        }else{
            item.setIcon(R.drawable.ic_bookmark_border_white_48dp);
        }
    }
    public  void showSavedSnack(){
        final Snackbar snack = Snackbar.make(all, "ሴቭ ተግይራ", Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.setAction("ሕራይ", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snack.dismiss();
            }
        });
        snack.show();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.change_button:
                if(!cat_et.getText().equals("null") || !cat_et.getText().equals("")){
                    boolean done=false;
                    try {
                        changeMezmurCatById(context, mezmurId, cat_et.getText().toString());
                        done=true;
                    } catch (ParserConfigurationException e) {
                        Log.e("Mezmur",e.getMessage());
                        done=false;
                    } catch (IOException | SAXException e) {
                        e.printStackTrace();
                        done=false;
                    }
                    if(done){
                        Toast.makeText(this, "Its Done bro!", Toast.LENGTH_LONG).show();
                    }
                }
                    Toast.makeText(MezmurActivity.this, cat_et.getText().toString(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
