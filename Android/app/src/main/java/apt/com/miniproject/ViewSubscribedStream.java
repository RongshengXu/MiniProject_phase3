package apt.com.miniproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewSubscribedStream extends ActionBarActivity {
    Context context = this;
    private String TAG = "Display Subscribed Streams";
    private Context mycontext;
    String user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_subscribed_stream);
        Bundle bundle = getIntent().getExtras();
        String temp = null;
        if (!bundle.isEmpty()) {
            temp = bundle.getString("user");
            if (temp.equals("")) {
                System.out.println("empty user!");
            } else {
                user_name = temp;
            }
        } else {
            System.out.println("No user found");
        }
        System.out.println("user is "+ user_name);
        mycontext = this;
        String u = user_name.split("@")[0];
        final String request_url = "http://sacred-highway-108321.appspot.com//android/mobileviewsubscribed=="+u;

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> streamcoverURLs = new ArrayList<String>();
                final ArrayList<String> streamNames = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
//                    JSONArray URLs = jObject.getJSONArray("imageurl");
//                    JSONArray names = jObject.getJSONArray("imagecap");
                    JSONArray URLs = jObject.getJSONArray("streamcoverurls");
                    JSONArray names = jObject.getJSONArray("streamnames");
//                    JSONArray URLs = jObject.getJSONArray("URLs");
//                    JSONArray names = jObject.getJSONArray("imageCaptionList");
                    System.out.println(new String(response));
                    for (int i = 0; i < URLs.length(); i++) {

                        streamcoverURLs.add(URLs.getString(i));
                        streamNames.add(names.getString(i));
                        System.out.println(URLs.getString(i));
                    }
                    System.out.println("Test.....");
                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context, streamcoverURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Intent intent = new Intent(mycontext, ViewStreamSingle.class);
                            intent.putExtra("message", streamNames.get(position));
                            intent.putExtra("user", user_name);
                            startActivity(intent);

                        }


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.viewsubscribedstream, menu);
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

    public void viewStream(View view){
        Intent intent= new Intent(this, ViewStream.class);
        intent.putExtra("user", user_name);
        startActivity(intent);
    }

}
