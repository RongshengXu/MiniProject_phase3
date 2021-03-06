package apt.com.miniproject;

/**
 * Created by rongshengxu on 10/25/15.
 */

import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchStream extends ActionBarActivity {
    Context context = this;
    private String TAG  = "Search Stream";
    private Context mycontext;
    private String message;
    private String user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        mycontext = this;
        Bundle bundle = getIntent().getExtras();
        message = bundle.getString("message");
        user_name = bundle.getString("user");
        System.out.println("Pattern is " + message);

        final String request_url = "http://sacred-highway-108321.appspot.com/android/searchresult=="+message;

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> streamcoverURLs = new ArrayList<String>();
                final ArrayList<String> streamNames = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray URLs = jObject.getJSONArray("streamcoverurls");
                    JSONArray Names = jObject.getJSONArray("streamnames");
                    System.out.println(new String(response));
                    TextView result_message;
                    result_message = (TextView) findViewById(R.id.result_mesage);
                    result_message.setText(Integer.toString(URLs.length())+ " results for " + message + ", \nclick on an image to view stream");
                    for (int i = 0; i < URLs.length(); i++) {

                        streamcoverURLs.add(URLs.getString(i));
                        streamNames.add(Names.getString(i));
                        System.out.println(URLs.getString(i));
                    }
                    System.out.println("Test.....");
                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context,streamcoverURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Intent intent= new Intent(mycontext, ViewStreamSingle.class);
                            intent.putExtra("message", streamNames.get(position));
                            intent.putExtra("user", user_name);
                            startActivity(intent);

                        }

                    });
                }
                catch(JSONException j){
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
        getMenuInflater().inflate(R.menu.search_stream, menu);
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

    public void searchHandler(View view) {
        Intent intent = new Intent(this, SearchStream.class);
        EditText text = (EditText) findViewById(R.id.search_message);
        if (text.length()>0) {
            String pattern = text.getText().toString();
            System.out.println(pattern);
            intent.putExtra("message", pattern);
            intent.putExtra("user", user_name);
            startActivity(intent);
        }
    }

    public void viewStream(View view){
        Intent intent= new Intent(this, ViewStream.class);
        intent.putExtra("user", user_name);
        startActivity(intent);
    }

}
