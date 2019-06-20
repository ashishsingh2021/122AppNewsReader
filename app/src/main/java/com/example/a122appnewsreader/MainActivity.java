package com.example.a122appnewsreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static android.util.Log.i;

public class MainActivity extends AppCompatActivity {
    // GLOBAL VARIABLES
    SQLiteDatabase articlesDb;
    ArrayList<String>TITLES=new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    SharedPreferences sp;
    TextView tv2;

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);




                urlConnection = (HttpURLConnection)url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;

                    result += current;

                    data = reader.read();

                }
                return result;

            }
            catch(Exception e) {

                e.printStackTrace();

                return "Failed";

            }


        }

    }
    public String GetData(String link)
    {


        DownloadTask task = new DownloadTask();
        String result = null;

        try {

            result = task.execute(link).get();

        } catch (InterruptedException e) {

            e.printStackTrace();

        } catch (ExecutionException e) {

            e.printStackTrace();

        }
        return result;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv2=(TextView)findViewById(R.id.tv);



        sp=this.getSharedPreferences("com.example.a122appnewsreader", Context.MODE_PRIVATE);


        TITLES=GetArrayList("SavedTitles");


        articlesDb = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articlesDb.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, content VARCHAR)");




        if(TITLES.isEmpty())
        {
            TITLES.add("Click Update");

            String sql = "INSERT INTO articles (articleId, title, content) VALUES (?, ?, ?)";
            SQLiteStatement statement = articlesDb.compileStatement(sql);
            statement.bindString(1, "0");
            statement.bindString(2, "Click Update");
            statement.bindString(3, "<h2>Return Back And Click  Update</h2>");
            statement.execute();

        }





        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, TITLES);  //THIS CAN BE REPLACED WITH MAINACTIVITY
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String title_=TITLES.get(i);
                SendIntentWithTitle(title_);
            }
        });





    }

    public void SendIntentWithTitle(String  title_)
    {
        String content_=null;
        String query="SELECT * FROM articles where title =? ";
        String[] selectionArgs = {title_};
        Cursor c =articlesDb.rawQuery(query, selectionArgs);

        int contentId = c.getColumnIndex("content");


        c.moveToFirst();
        {
           content_=c.getString(contentId);
        }

        Intent intent = new Intent(getApplicationContext(), DisplayContent.class);
        intent.putExtra("Title",title_);
        intent.putExtra("Content",content_);
        startActivity(intent);

    }
    public void update_contents()
    {





        TITLES.clear();
        articlesDb.execSQL("DELETE FROM articles");



        // Step 1 Getting the TOPIC IDS as json array; Ok
        JSONArray Jarr=null;
        try
        {
            Jarr=new JSONArray(GetData("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty"));
            for(int j=0;j<Jarr.length();j++)
            {
                i("Content =>>",Jarr.getString(j));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        //Step1 Over










        // Step2 Getting The Article Topic,Url etc using the Json Array of step1
        int numberOfItems = 3;
        if (Jarr.length() < 3)
        {
            numberOfItems = Jarr.length();
        }
        String HTTPS_="https://hacker-news.firebaseio.com/v0/item/";
        String _HTTPS=".json?print=pretty";

        for(int i=0;i<numberOfItems;i++)
        {

            String ArticleInfo=null;
            JSONObject jsonObject=null;


            try
            {
                String ArticleId=Jarr.getString(i);                                                 // ArticleId
                String url= HTTPS_+ArticleId+_HTTPS;
                ArticleInfo=GetData(url);
                i("ARTICLE INFO ",ArticleInfo);
                jsonObject=new JSONObject(ArticleInfo);

                if(!jsonObject.isNull("url")&&!jsonObject.isNull("title"))
                {
                    String ArticleTitle = jsonObject.getString("title");                        // ArticleTitle
                    TITLES.add(ArticleTitle);
                    String ArticleURL = jsonObject.getString("url");                                                     //Adding titles to arraylist
                    String ArticleContent = GetData(ArticleURL);                                      //ArticleContent


                    //   Step4 Entering into The Database
                    String sql = "INSERT INTO articles (articleId, title, content) VALUES (?, ?, ?)";
                    SQLiteStatement statement = articlesDb.compileStatement(sql);
                    statement.bindString(1, ArticleId);
                    statement.bindString(2, ArticleTitle);
                    statement.bindString(3, ArticleContent);
                    statement.execute();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        SaveArrayListAs("SavedTitles",TITLES);





    }


    public void update_via_button(View view)
    {



       update_contents();




    }
    public int SaveArrayListAs(String name,ArrayList ArrayName)                         // Step3 Save Data
    {
        try
        {
            sp.edit().putString(name,ObjectSerializer.serialize(ArrayName)).apply();
            return 1;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return 0;
        }
    }
    public ArrayList<String> GetArrayList(String SavedName)
    {
        ArrayList<String> newArray=new ArrayList<String>();
        try
        {
            newArray=(ArrayList<String>)ObjectSerializer.deserialize(sp.getString(SavedName,ObjectSerializer.serialize(new ArrayList<String>())));

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return newArray;
    }

}

