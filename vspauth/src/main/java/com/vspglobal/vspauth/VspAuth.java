package com.vspglobal.vspauth;

import android.app.Activity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class VspAuth {

    private static int CALL_FUNCTION = -1;

    private Activity activity;

    private OnCompleteListener completeListener;

    private String url;

    private JSONObject json;

    public VspAuth createUser(String url, JSONObject json) {
        this.url = url;
        this.json = json;
        CALL_FUNCTION = 0;
        return this;
    }

    public VspAuth loginUser(String url, JSONObject json) {
        this.url = url;
        this.json = json;
        CALL_FUNCTION = 1;
        return this;
    }

    public VspAuth isSignedIn() {
        CALL_FUNCTION = 2;
        return this;
    }

    public VspAuth addOnCompleteListener(Activity activity,OnCompleteListener completeListener) {
        this.activity = activity;
        this.completeListener = completeListener;
        if (CALL_FUNCTION == 0) {
            createUser();
        } else if (CALL_FUNCTION == 1) {
            loginUser();
        } else if (CALL_FUNCTION == 2) {
            isSignedIn(getHeader());
        }
        return this;
    }

    private void createUser() {

        final Task task = new Task();

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("logged in")) {
                        task.setStatus(true);
                        saveData(getHeader(response.toString()),response.getString(getHeader(response.toString())),"login_identifier.txt");
                        completeListener.onComplete(task);
                    }
                } catch (JSONException e) {
                    task.setStatus(false);
                    completeListener.onComplete(task);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                task.setStatus(false);
                completeListener.onComplete(task);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());
        queue.add(objectRequest);
    }

    private void loginUser() {

        final Task task = new Task();

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("logged in")) {
                        saveData(getHeader(response.toString()),response.getString(getHeader(response.toString())),"login_identifier.txt");
                        if (getHeader(response.toString()).equals("x-user-token")) {
                            task.setStatus(true);
                            task.setType(0);
                        } else if (getHeader(response.toString()).equals("x-qualityInspector-token")) {
                            task.setStatus(true);
                            task.setType(1);
                        } else if (getHeader(response.toString()).equals("x-cattleFarmOwner-token")) {
                            task.setStatus(true);
                            task.setType(2);
                        } else {
                            task.setStatus(true);
                            task.setType(-1);
                        }
                        completeListener.onComplete(task);
                    }
                } catch (JSONException e) {
                    task.setStatus(false);
                    task.setType(-1);
                    completeListener.onComplete(task);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                task.setStatus(false);
                task.setType(-1);
                completeListener.onComplete(task);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());
        queue.add(objectRequest);
    }

    private void isSignedIn(final String[] header) {

        final Task task = new Task();
        String link = null;

        if (header[0].equals("x-user-token")) {
            task.setType(0);
            link = "https://sih-drs-prototype-backend.herokuapp.com/api/app/userTest";
        } else if (header[0].equals("x-qualityInspector-token")) {
            task.setType(1);
            link = "https://sih-drs-prototype-backend.herokuapp.com/api/app/qualityInspectorTest";
        } else if (header[0].equals("x-cattleFarmOwner-token")) {
            task.setType(2);
            link = "https://sih-drs-prototype-backend.herokuapp.com/api/app/cattleFarmOwnerTest";
        } else {
            task.setStatus(false);
            task.setType(-1);
            completeListener.onComplete(task);
            return;
        }

        StringRequest objectRequest = new StringRequest(Request.Method.GET, link, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getString("status").equals("valid token")) {
                        if (header[0].equals("x-user-token")) {
                            task.setStatus(true);
                            task.setType(0);
                        } else if (header[0].equals("x-qualityInspector-token")) {
                            task.setStatus(true);
                            task.setType(1);
                        } else if (header[0].equals("x-cattleFarmOwner-token")) {
                            task.setStatus(true);
                            task.setType(2);
                        }
                        completeListener.onComplete(task);
                    } else {
                        task.setStatus(false);
                        task.setType(-1);
                        completeListener.onComplete(task);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    task.setStatus(false);
                    task.setType(-1);
                    completeListener.onComplete(task);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                task.setStatus(false);
                task.setType(-1);
                completeListener.onComplete(task);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put(header[0], header[1]);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());
        queue.add(objectRequest);
    }

    private String getHeader(String response) {
        String[] header = response.split("\"");
        return header[5];
    }

    public String[] getHeader() {
        FileInputStream fis = null;
        String[] token = new String[2];

        try {
            fis = activity.openFileInput("login_identifier.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            token[0] = br.readLine();
            token[1] = br.readLine();

        } catch (FileNotFoundException e) {
            Log.i("ERROR : ",e.getMessage());
            token[0] = "asd";
            token[1] = "asd";
            return token;
        } catch (IOException e) {
            Log.i("ERROR : ",e.getMessage());
            token[0] = "asd";
            token[1] = "asd";
            return token;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.i("ERROR : ",e.getMessage());
                    token[0] = "asd";
                    token[1] = "asd";
                    return token;
                }
            }
        }

        return token;
    }

    private void saveData(String headerName, String token, String filename) {
        FileOutputStream fos = null;
        try {
            activity.deleteFile(filename);
            fos = activity.openFileOutput(filename, MODE_PRIVATE);
            fos.write(headerName.getBytes());
            fos.write(("\n"+token).getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface OnCompleteListener {
        void onComplete(Task task);
    }

    public class Task {
        private boolean status;
        private int type;
        private void setType(int type) {
            this.type = type;
        }
        private void setStatus(boolean status) {
            this.status = status;
        }
        public boolean isSuccessful() {
            return status;
        }
        public int getType() {
            return type;
        }
    }

}
