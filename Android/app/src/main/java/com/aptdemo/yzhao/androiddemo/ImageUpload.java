package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import android.view.View.OnClickListener;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class ImageUpload extends ActionBarActivity {
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PICTURE = 2;



    Context context = this;
    private String message;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        Bundle bundle = getIntent().getExtras();
        message = bundle.getString("message");

        final TextView lat_val = (TextView) findViewById(R.id.lat_value);
        final TextView lon_val = (TextView) findViewById(R.id.lon_value);

        LocationManager location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener location_listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                lat_val.setText("Latitude: "+String.valueOf(latitude));
                lon_val.setText("Longitude: "+String.valueOf(longitude));
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

        // Choose image from library
        Button chooseFromLibraryButton = (Button) findViewById(R.id.choose_from_library);
        chooseFromLibraryButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // To do this, go to AndroidManifest.xml to add permission
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // Start the Intent
                        startActivityForResult(galleryIntent, PICK_IMAGE);
                    }
                }
        );

        Button usecamerabutton = (Button) this.findViewById(R.id.use_camera);
        usecamerabutton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicIntent, TAKE_PICTURE);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_upload, menu);
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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = getIntent().getExtras();
        final String stream_name = bundle.getString("message");

        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();

            // User had pick an image.

            String[] filePathColumn = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            // Link to the image

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imageFilePath = cursor.getString(columnIndex);
            cursor.close();

            // Bitmap imaged created and show thumbnail

            ImageView imgView = (ImageView) findViewById(R.id.thumbnail);
            final Bitmap bitmapImage = BitmapFactory.decodeFile(imageFilePath);
            imgView.setImageBitmap(bitmapImage);

            // Enable the upload button once image has been uploaded

            final Button uploadButton = (Button) findViewById(R.id.upload_to_server);
            uploadButton.setClickable(true);

            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get photo caption

                            EditText text = (EditText) findViewById(R.id.upload_message);
                            if (text.length()>0) {
                                String photoCaption = text.getText().toString();
                                System.out.println(photoCaption);
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] b = baos.toByteArray();
                            byte[] encodedImage = Base64.encode(b, Base64.DEFAULT);
                            String encodedImageStr = encodedImage.toString();

//                            getUploadURL(b, photoCaption);
                            uploadHandler(b, message);
                        }
                    }
            );
        }

        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView mImageView = (ImageView) findViewById(R.id.thumbnail);
            mImageView.setImageBitmap(imageBitmap);

            final Button uploadButton = (Button) findViewById(R.id.upload_to_server);
            uploadButton.setClickable(true);
            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get photo caption

//                            EditText text = (EditText) findViewById(R.id.upload_message);
//                            String photoCaption = text.getText().toString();

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] b = baos.toByteArray();
                            byte[] encodedImage = Base64.encode(b, Base64.DEFAULT);
                            String encodedImageStr = encodedImage.toString();

//                            getUploadURL(b, photoCaption);
                            uploadHandler(b, message);
                        }
                    }
            );
        }
    }

    private void uploadHandler(final byte[] encodedImage, final String stream_name) {
            AsyncHttpClient httpClient = new AsyncHttpClient();
            String request_url= "http://sacred-highway-108321.appspot.com/android/";
            String upload_url = "http://sacred-highway-108321.appspot.com/android/upload";
            RequestParams params = new RequestParams();
            params.put("file",new ByteArrayInputStream(encodedImage));
            params.put("streamname", stream_name);
            AsyncHttpClient client = new AsyncHttpClient();
            System.out.println(upload_url+" \n" + encodedImage.toString());
            client.post(upload_url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    Log.w("async", "success!!!!");
                    Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();
                    // go back to single stream view
                    Intent intent= new Intent(context, ViewStreamSingle.class);
                    intent.putExtra("message", stream_name);
                    startActivity(intent);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    System.out.println(new String(errorResponse));
                    Log.e("Posting_to_blob", "There was a problem in retrieving the url : " + e.toString());
                }
            });
//            httpClient.get(request_url, new AsyncHttpResponseHandler() {
//                String upload_url;
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
//
//                    try {
//                        JSONObject jObject = new JSONObject(new String(response));
//
//                        upload_url = jObject.getString("upload_url");
//                        postToServer(encodedImage, photoCaption, upload_url);
//
//                    }
//                    catch(JSONException j){
//                        System.out.println("JSON Error");
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                    Log.e("Get_serving_url", "There was a problem in retrieving the url : " + e.toString());
//                }
//            });
        }

    private void getUploadURL(final byte[] encodedImage, final String photoCaption){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        String request_url="http://aptandroiddemo.appspot.com/getUploadURL";
        System.out.println(request_url);
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            String upload_url;

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                try {
                    JSONObject jObject = new JSONObject(new String(response));

                    upload_url = jObject.getString("upload_url");
                    postToServer(encodedImage, photoCaption, upload_url);

                } catch (JSONException j) {
                    System.out.println("JSON Error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("Get_serving_url", "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    private void postToServer(final byte[] encodedImage,String photoCaption, final String upload_url){
        System.out.println(upload_url);
        RequestParams params = new RequestParams();
        params.put("file",new ByteArrayInputStream(encodedImage));
        params.put("photoCaption", photoCaption);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(upload_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                System.out.println(upload_url);
                System.out.println(encodedImage.toString());
                Log.w("async", "success!!!!");
                Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("Posting_to_blob", "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    public void viewStream(View view){
        Intent intent= new Intent(this, ViewStream.class);
        startActivity(intent);
    }

    public void cameraWithPreview(View view){
        Intent intent= new Intent(this, TakePicture.class);
        startActivity(intent);
    }
}
