package apt.com.miniproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rongshengxu on 10/24/15.
 */
        import android.app.Dialog;
        import android.content.Context;

        import android.content.Intent;
        import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
        import android.support.v7.app.ActionBarActivity;
        import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
        import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.Window;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.GridView;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.plus.Plus;
        import com.google.android.gms.plus.model.people.Person;
        import com.loopj.android.http.AsyncHttpClient;
        import com.loopj.android.http.AsyncHttpResponseHandler;
        import com.squareup.picasso.Picasso;

        import org.apache.http.Header;
        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.util.ArrayList;


        import org.json.*;
        import com.loopj.android.http.*;

public class ViewNearby extends ActionBarActivity {

    Context context = this;
    private String TAG  = "Display Single Stream";
    private Context mycontext;
    private TextView stname;
    private String message;
    private String user_name;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_nearby);
        Bundle bundle = getIntent().getExtras();
        user_name = bundle.getString("user");
        System.out.println("User is " + user_name);
        mycontext = this;

        LocationManager location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener location_listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, (float) 0.1, location_listener);


        String la = String.valueOf(latitude);
        String lo = String.valueOf(longitude);
        final String request_url = "http://sacred-highway-108321.appspot.com/android/viewnearby=="+la+"==="+lo;
//        final String request_url = "http://aptandroiddemo.appspot.com/viewAllPhotos";

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> imageURLs = new ArrayList<String>();
                final ArrayList<String> imageCaps = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray displayImages = jObject.getJSONArray("imageurl");
                    final JSONArray displayCaption = jObject.getJSONArray("streamnames");
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
                    gridview.setAdapter(new ImageAdapter(context,imageURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Intent intent = new Intent(mycontext, ViewStreamSingle.class);
                            intent.putExtra("message", imageCaps.get(position));
                            intent.putExtra("user", user_name);
                            startActivity(intent);
                        }
                    });
                }
                catch(JSONException j){
                    System.out.println("JSON Error");
                    System.out.println(new String(response));
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
        getMenuInflater().inflate(R.menu.view_stream_single, menu);
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

    public void showMore(View view) {
        Intent intent = new Intent(this, ShowMore.class);
//        intent.putExtra("message", message);
        intent.putExtra("user", user_name);
        startActivity(intent);
    }

}

