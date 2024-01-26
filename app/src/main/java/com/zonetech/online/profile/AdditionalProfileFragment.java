package com.zonetech.online.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdditionalProfileFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public  JSONObject profileJSON = new JSONObject();
    JSONArray statesDataArray = new JSONArray();
    ArrayList<String> statesArray = new ArrayList<>();
    Spinner qulificationSpinner;
    Spinner stateSpinner;
    EditText passingPicker;
    int qualificationPosition = 0;
    int statePostion = 0;
    public AdditionalProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_additional_profile, container, false);
        qulificationSpinner = rootView.findViewById(R.id.qualificationSpinner);
        stateSpinner = rootView.findViewById(R.id.stateSpinner);
        passingPicker = rootView.findViewById(R.id.passingPicker);

        Bundle bundle = getArguments();
        try {
            JSONObject profileJSONTemp = new JSONObject(bundle.getString("response"));
            profileJSON = profileJSONTemp.optJSONObject("Body").optJSONObject("profile");
            statesDataArray = profileJSONTemp.optJSONObject("Body").optJSONArray("states");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(int i = 0; i<statesDataArray.length(); i++){
            try {
                statesArray.add(statesDataArray.getJSONObject(i).optString("StateName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayList<String> qulificationList = new ArrayList<>();
        qulificationList.add("None");
        qulificationList.add("B.Tech");
        qulificationList.add("Diploma");
        qulificationList.add("Other");
        if(!isAdded()){
            return rootView;
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item, qulificationList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qulificationSpinner.setAdapter(dataAdapter);
        qulificationSpinner.setOnItemSelectedListener(this);
        if(!isAdded()){
            return rootView;
        }
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item, statesArray);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(dataAdapter2);
        stateSpinner.setOnItemSelectedListener(this);
        if (qulificationList.contains(profileJSON.optString("Qualification"))) {
            qulificationSpinner.setSelection(qulificationList.indexOf(profileJSON.optString("Qualification")));
        } else {
            qulificationSpinner.setSelection(0);
        }
        if (profileJSON.has("PassingYear") && profileJSON.optInt("PassingYear") != 0){
            passingPicker.setText(""+profileJSON.optInt("PassingYear"));
        }
        if (profileJSON.has("CStateID")) {
            int stateId = profileJSON.optInt("CStateID");
            for(int i = 0; i<statesDataArray.length(); i++){
                try {
                    if (statesDataArray.getJSONObject(i).optInt("StateID") == stateId){
                        stateSpinner.setSelection(i);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        EditText city = rootView.findViewById(R.id.city);
        city.setText(profileJSON.optString("CCity"));
        EditText address = rootView.findViewById(R.id.address);
        address.setText(profileJSON.optString("CAddress"));
        EditText pincode = rootView.findViewById(R.id.pincode);
        pincode.setText(profileJSON.optString("CPinCode"));
        rootView.findViewById(R.id.updetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    profileJSON.put("CPinCode", pincode.getText().toString().trim());
                    profileJSON.put("CAddress", address.getText().toString().trim());
                    profileJSON.put("CCity", city.getText().toString().trim());
                    profileJSON.put("PassingYear", passingPicker.getText().toString().trim());
                    profileJSON.put("CStateID",  statesDataArray.getJSONObject(stateSpinner.getSelectedItemPosition()).optInt("StateID"));
                    profileJSON.put("Qualification",  qulificationSpinner.getSelectedItem());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                updateProfile(rootView);
            }
        });
        return rootView;
    }


    private void updateProfile(View rootView) {
        rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        if(!isAdded()){
            return;
        }
        ServerApi.callServerApi(getActivity(), ServerApi.BASE_URL, "updateProfile", profileJSON, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                if(!isAdded()){
                    return;
                }
                Toast.makeText(getActivity(), "Profile Updated  Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent == qulificationSpinner){
            qualificationPosition = position;
        } else if(parent == stateSpinner){
            statePostion = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}