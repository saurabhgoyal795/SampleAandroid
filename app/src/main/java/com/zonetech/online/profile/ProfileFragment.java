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

import com.google.android.material.tabs.TabLayout;
import com.zonetech.online.R;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    EditText studentCode;
    EditText studentName;
    EditText studentEmail;
    EditText studentMobile;
    EditText studentCourse;
    EditText studentSpelization;
    Spinner genderSpinner;
    int genderPostion = 0;
    EditText picker;
    private int selectionCount = 0;

    JSONObject profileJSON = new JSONObject();
    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_profile, container, false);
        studentCode = rootView.findViewById(R.id.studentCode);
        studentEmail = rootView.findViewById(R.id.email);
        studentName = rootView.findViewById(R.id.name);
        studentMobile = rootView.findViewById(R.id.phone);
        studentCourse = rootView.findViewById(R.id.course);
        studentSpelization = rootView.findViewById(R.id.specialization);
        genderSpinner = rootView.findViewById(R.id.genderSpinner);
        picker=(EditText)rootView.findViewById(R.id.datePicker);


        ArrayList<String> genderList = new ArrayList<>();
        genderList.add("Select Gender");
        genderList.add("Male");
        genderList.add("Female");
        genderList.add("Other");
        if(!isAdded()){
            return rootView;
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item, genderList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(dataAdapter);
        Bundle bundle = getArguments();
        try {
            profileJSON = new JSONObject(bundle.getString("response"));
            setView(profileJSON.optJSONObject("Body").optJSONObject("profile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                picker.setText(sdf.format(myCalendar.getTime()));
            }
        };

        picker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!isAdded()){
                    return;
                }
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        rootView.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject tempProfileJson = profileJSON;
                    JSONObject profileJSONNew = profileJSON.optJSONObject("Body");
                    if (profileJSONNew == null){
                        profileJSONNew = new JSONObject();
                    }
                    JSONObject profileData = profileJSONNew.optJSONObject("profile");
                    profileData.put("Gender", genderSpinner.getSelectedItemPosition());
                    profileData.put("DOB", picker.getText().toString().trim());
                    profileJSONNew.put("profile", profileData);
                    tempProfileJson.put("Body", profileJSONNew);
                    if(!isAdded()){
                        return;
                    }
                    ((ProfileActivity)getActivity()).setProfileJSON(tempProfileJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(!isAdded()){
                    return;
                }
                TabLayout tabs = (TabLayout)((ProfileActivity)getActivity()).findViewById(R.id.tabs);
                tabs.getTabAt(1).select();
            }
        });

        return rootView;
    }

    private void setView(JSONObject data){
        try{
            studentCode.setText(data.optString("StudentCode"));
            studentEmail.setText(data.optString("EmailID"));
            studentName.setText(data.optString("StudentName"));
            studentMobile.setText(data.optString("MobileNo"));
            studentCourse.setText(data.optString("CourseName"));
            studentSpelization.setText(data.optString("SpecName"));
            genderSpinner.setSelection(data.optInt("Gender"));
            picker.setText(data.optString("DOB"));
        }catch (Exception e){

        }
    }

}
