package com.sagar.real_time_notification;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private EditText editText_Current_Location;
    private EditText editText_Destination_Location;
    private AppCompatButton buttonEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText_Current_Location = findViewById(R.id.edt_current_location);
        editText_Destination_Location = findViewById(R.id.edt_destination_location);
        buttonEvent = findViewById(R.id.buttonEvent);

        editText_Current_Location.addTextChangedListener(editTextWatcher);
        editText_Destination_Location.addTextChangedListener(editTextWatcher);

        buttonEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "dsdss", Toast.LENGTH_SHORT).show();
            }
        });


        // end
    }


    private TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String cur_location = editText_Current_Location.getText().toString().trim();
            String dest_location = editText_Destination_Location.getText().toString().trim();

            buttonEvent.setEnabled(!cur_location.isEmpty() && !dest_location.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };





    // END
}
