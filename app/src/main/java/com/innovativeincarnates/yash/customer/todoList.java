package com.innovativeincarnates.yash.customer;

/**
 * Created by yash on 21/3/17.
 */

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.innovativeincarnates.yash.customer.db.TaskContract;
import com.innovativeincarnates.yash.customer.db.TaskDbHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class todoList extends AppCompatActivity {

    String Username;

    public static final String PREFS_NAME = "DataFile";
    public static final String PREFS_USERNAME = "Username";


    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    String task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todolist_layout);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Username = settings.getString(PREFS_USERNAME, "default");

        //Intent intent= getIntent();
        //Bundle bundle = intent.getExtras();
        //Username = bundle.getString("msg");
        mHelper = new TaskDbHelper(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        updateUI();


    }


    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                //  final Spinner sp = new Spinner(this);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(todoList.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
                mBuilder.setTitle("Select a new task");
                final Spinner mSpinner = (Spinner) mView.findViewById(R.id.spinner);
                final Spinner mSpinner1 = (Spinner) mView.findViewById(R.id.spinner1);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(todoList.this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.items_to_do));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(adapter);

                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(todoList.this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.items_to_do1));
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner1.setAdapter(adapter1);

                mSpinner.setOnItemSelectedListener(new OnSpinnerItemClicked(mSpinner1));

                mBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mSpinner.getSelectedItem().toString().equalsIgnoreCase("Select a task here.....")) {
                                   /* Toast.makeText(MainActivity.this , mSpinner.getSelectedItem().toString(),
                                            Toast.LENGTH_SHORT)
                                            .show();*/
                            task = String.valueOf(mSpinner1.getSelectedItem());
                            SQLiteDatabase db = mHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                    null,
                                    values,
                                    SQLiteDatabase.CONFLICT_REPLACE);
                            db.close();
                            updateUI();
                            addList(task);

                        }
                    }
                });
                mBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setView(mView);
                AlertDialog mBuilder1 = mBuilder.create();
                mBuilder1.show();
                        /*.setMessage("What do you want to do next?")
                        .setView(taskEditText)*/
                      /*  mBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface mBuilder , int which) {
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                mBuilder.show();*/


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }

    public class OnSpinnerItemClicked implements AdapterView.OnItemSelectedListener {
        public Spinner mSpinner1;

        public OnSpinnerItemClicked(Spinner spinner) {
            this.mSpinner1 = spinner;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            if (pos == 0) {
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(todoList.this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.items_to_do1));
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner1.setAdapter(adapter1);
            } else if (pos == 1) {
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(todoList.this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.items_to_do2));
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner1.setAdapter(adapter1);
            } else if (pos == 2) {
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(todoList.this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.items_to_do3));
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner1.setAdapter(adapter1);
            } else if (pos == 3) {
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(todoList.this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.items_to_do4));
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner1.setAdapter(adapter1);
            }
        }

        @Override
        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }


    }
    public static final String UPLOAD_URL = "http://192.168.208.182:8081/buyer_update_inventory";

    private void addList(final String Inventory){

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

                        Toast.makeText(todoList.this, "Saved your profile.\nLogin to web app to select items at your shop" , Toast.LENGTH_LONG).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Log.e(TAG,"ErrorListener"+volleyError.getMessage());
                        //Showing toast
                        Toast.makeText(todoList.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
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

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                String KEY_Username = "Username";
                String KEY_Inventory = "Inventory";

                params.put(KEY_Username, Username);
                params.put(KEY_Inventory, Inventory);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }


    public void test(View v){
        String list="";
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }
        for(int i=0; i < (taskList.size()); i++) {
             list=list+","+String.valueOf(taskList.get(i));
        }
        addList(list);
    }
    public void startScan(View v) {
        startService(new Intent(getBaseContext(), MyService.class));
    }

    // Method to stop the service
    public void stopScan(View v) {
        stopService(new Intent(getBaseContext(), MyService.class));
    }
}
