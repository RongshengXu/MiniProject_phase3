package com.aptdemo.yzhao.androiddemo;

/**
 * Created by rongshengxu on 10/23/15.
 */
import android.app.Dialog;
import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


import org.json.*;
import com.loopj.android.http.*;


public class ViewStream extends ActionBarActivity {
    Context context = this;
    private String TAG  = "Display Streams";
    private Context mycontext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stream);
        mycontext = this;

        final String request_url = "http://sacred-highway-108321.appspot.com/android/mobileview";
//        final String request_url = "http://aptandroiddemo.appspot.com/viewAllPhotos";
//        final Button searchButton = (Button) findViewById(R.id.search);
//        searchButton.setClickable(true);
//        searchButton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        System.out.println("Hello!!");
//
//                        EditText text = (EditText) findViewById(R.id.search_message);
//                        if (text.length() > 0) {
//                            String pattern = text.toString();
//                            System.out.println(pattern);
//                            Intent intent= new Intent(mycontext, SearchStream.class);
//                            intent.putExtra("message", pattern);
//                            startActivity(intent);
//                        }
//
//                    }
//                }
//        );

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> imageURLs = new ArrayList<String>();
                final ArrayList<String> imageCaps = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
//                    JSONArray displayImages = jObject.getJSONArray("imageurl");
//                    JSONArray displayCaption = jObject.getJSONArray("imagecap");
                    JSONArray displayImages = jObject.getJSONArray("streamcoverurls");
                    JSONArray displayCaption = jObject.getJSONArray("streamnames");
//                    JSONArray displayImages = jObject.getJSONArray("displayImages");
//                    JSONArray displayCaption = jObject.getJSONArray("imageCaptionList");
                    System.out.println(new String(response));
                    for (int i = 0; i < displayImages.length(); i++) {

                        imageURLs.add(displayImages.getString(i));
                        imageCaps.add(displayCaption.getString(i));
                        System.out.println(displayImages.getString(i));
                    }
                    System.out.println("Test.....");
                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context, imageURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Intent intent = new Intent(mycontext, ViewStreamSingle.class);
                            intent.putExtra("message", imageCaps.get(position));
                            startActivity(intent);

                        }

//                        @Override
//                        public void onClick(View v) {
//
//                        }
                    });
                } catch (JSONException j) {
                    System.out.println("JSON Error");
                    System.out.println(new String(response));
//                    System.out.println(new String(headers));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Button searchButton = (Button) findViewById(R.id.search);
        searchButton.setClickable(true);
        searchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        System.out.println("Hello!!");

                        EditText text = (EditText) findViewById(R.id.search_message);
                        if (text.length() > 0) {
                            String pattern = text.toString();
                            System.out.println(pattern);
                            Intent intent= new Intent(mycontext, SearchStream.class);
                            intent.putExtra("message", pattern);
                            startActivity(intent);
                        }

                    }
                }
        );
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_images, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
