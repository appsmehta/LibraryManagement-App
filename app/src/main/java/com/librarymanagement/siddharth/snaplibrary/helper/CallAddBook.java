package com.librarymanagement.siddharth.snaplibrary.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.librarymanagement.siddharth.snaplibrary.AddFragment;
import com.librarymanagement.siddharth.snaplibrary.CatalogActivity;
import com.librarymanagement.siddharth.snaplibrary.ConfirmationFragment;
import com.librarymanagement.siddharth.snaplibrary.ListFragment;
import com.librarymanagement.siddharth.snaplibrary.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by siddharthdaftari on 11/2/17.
 */

public class CallAddBook {

    public void processAddBook(final HashMap<String, Object> params) throws JSONException{

        String urlString = Constants.AWS_URL + Constants.CALL_ADD_BOOK_URL;
        LogHelper.logMessage("URL", urlString);

        //Extracting ONLY REQUIRED params
        JSONObject requestJSON = null;
        final Context context;
        final String action;
        final View view;
        final AddFragment fragment;
        final Activity activity;
        requestJSON = (JSONObject) params.get(Constants.REQUEST_JSON);
        context = (Context) params.get(Constants.CONTEXT);
        action = (String) params.get(Constants.ACTION);
        view = (View) params.get(Constants.VIEW);
        fragment = (AddFragment) params.get(Constants.FRAGMENT);
        activity = (Activity) params.get(Constants.ACTIVITY);

        //Now making the request
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, urlString, requestJSON, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {

                            HashMap returnHashMap = new HashMap<String, String>();

                            Boolean isSuccess = jsonObject.getBoolean("success");// || jsonObject.getBoolean("sucess");
                            if(isSuccess){
                                String message = jsonObject.getString("message");
                                LogHelper.logMessage("Siddharth","\n message: " + message);

                                updateUI(fragment, context, action, activity, returnHashMap, params);
                            }else{
                                AddFragment.showProgress(false);
                                HashMap<String, Object> extraParams = new HashMap<String, Object>();
                                extraParams.put("activity", activity);
                                ExceptionMessageHandler.handleError(context, jsonObject.getString("message"), null, extraParams);
                            }

                        }catch (JSONException e){
                            AddFragment.showProgress(false);
                            HashMap<String, Object> extraParams = new HashMap<String, Object>();
                            extraParams.put("activity", activity);
                            ExceptionMessageHandler.handleError(context, e.getMessage(), e, extraParams);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = error.getMessage();

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            try {

                                JSONObject jsonError = new JSONObject(new String(networkResponse.data));
                                errorMessage = jsonError.getString("message");

                            } catch (JSONException e) {
                                AddFragment.showProgress(false);
                                ExceptionMessageHandler.handleError(context, Constants.GENERIC_ERROR_MSG, null, null);
                            }
                        }
                        AddFragment.showProgress(false);

                        error.printStackTrace();
                        HashMap<String, Object> hs = new HashMap<String, Object>();
                        hs.put("activity", activity);
                        ExceptionMessageHandler.handleError(context, errorMessage, error, hs);

                    }
                });
        RequestClass.getRequestQueue().add(jsObjRequest);
    }

    public void updateUI(Fragment fragment, Context context, String action, Activity activity, HashMap<String, String> returnHashMap, HashMap<String,Object> extraparams){

        switch (action) {
            case Constants.ACTION_ADD_BOOK:
                AddFragment.showProgress(false);
                Toast.makeText(activity, "New book added.", Toast.LENGTH_SHORT).show();
                Fragment fr = new ListFragment();
                fragment.getFragmentManager().beginTransaction().replace(R.id.place_holder,fr).addToBackStack(null).commit();
        }
    }
}
