package com.innovativeincarnates.yash.customer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Map;

import static android.R.id.message;
import static android.content.ContentValues.TAG;

/**
 * Created by yash on 11/3/17.
 */

public class profileSet extends Activity {
    Button btnTakePhoto;
    ImageView imgTakenPhoto;
    int CAM_REQUEST = 100;
    Button save;
    public String USERNAME = "";
    EditText editTextfullName,editTextphoneNo;
    Bitmap thumbnail;


    public static final String PREFS_NAME = "DataFile";
    public static final String PREFS_USERNAME = "Username";


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);


        final Dialog dialog = new Dialog(profileSet.this);
        dialog.setContentView(R.layout.login);
        dialog.setTitle("Login");

        save =(Button)findViewById(R.id.profile_Save);
        btnTakePhoto = (Button) findViewById(R.id.cam);
        imgTakenPhoto = (ImageView) findViewById(R.id.imageView);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraintent, CAM_REQUEST);
            }
        });

        editTextfullName=(EditText)findViewById(R.id.profile_full_name);
        editTextphoneNo=(EditText)findViewById(R.id.phone_no);


        // Set On ClickListener
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v){
                String fullname=editTextfullName.getText().toString();
                String phoneno=editTextphoneNo.getText().toString();
                uploadData(fullname,phoneno,thumbnail);

                USERNAME=fullname;

                //todoList uname = new todoList();
                //uname.getuser(fullname);
            }
        });

    }
    /*
    public void getuser(String user)
    {
        USERNAME = user;

    }
    */
    public static final String UPLOAD_URL = "http://192.168.208.182:8081/buyer_set_Profile";

    private void uploadData(final String a,final String b,final Bitmap bitmap){

        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Log.d(TAG,"Listener");


                        // Toast.makeText(MainActivity.this, s , Toast.LENGTH_LONG).show();

                        Toast.makeText(profileSet.this, "Saved your profile.\nLogin to web app to select items at your shop" , Toast.LENGTH_LONG).show();
                        Intent intre=new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intre);
                        //Intent intent = new Intent(profileSet.this, MainActivity.class);
                        //intent.putExtra("message", USERNAME);
                        //startActivity(intent);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Log.e(TAG,"ErrorListener"+volleyError.getMessage());
                        //Showing toast
                        Toast.makeText(profileSet.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
            /*
            String Username = "Username";
            String Phone = "Phone";
            String shopname = "ArIES";
            String Address = "ECE_Dept";
            */
                String Image = getStringImage(bitmap);
                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                String KEY_Username = "Username";
                String KEY_Phone = "Phone";
                String KEY_Image="Image";

                params.put(KEY_Username, a);
                params.put(KEY_Phone, b);
                params.put(KEY_Image,Image);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }

    public String getStringImage(Bitmap bmp){
        //converts bitmap to Base64 format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"Check");
        if (requestCode == CAM_REQUEST) {
            thumbnail = (Bitmap) data.getExtras().get("data");
            imgTakenPhoto.setImageBitmap(thumbnail);

        }
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_USERNAME, USERNAME);
        editor.apply();
    }
}
