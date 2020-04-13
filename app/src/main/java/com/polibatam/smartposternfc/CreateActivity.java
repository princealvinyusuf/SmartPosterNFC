package com.polibatam.smartposternfc;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.polibatam.smartposternfc.model.NFCManager;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class CreateActivity extends AppCompatActivity {

    Tag currentTag;
    private NFCManager nfcMger;
    private View v;
    private NdefMessage message = null;
    private ProgressDialog dialog;
    private CheckBox lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        nfcMger = new NFCManager(this);
        lock = findViewById(R.id.lock);

        v = findViewById(R.id.mainLyt);

        final Spinner sp = (Spinner) findViewById(R.id.tagType);
        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this, R.array.tagContentType, android.R.layout.simple_spinner_dropdown_item);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(aa);

        final EditText et = (EditText) findViewById(R.id.content);

        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = sp.getSelectedItemPosition();
                String content = et.getText().toString();
                boolean isEmptyFields = false;

                if (TextUtils.isEmpty(content)) {
                    isEmptyFields = true;
                    et.setError("The Content Cannot Be Empty");
                }

                if (!isEmptyFields) {
                    switch (pos) {
                        case 0:
                            message = nfcMger.createUriMessage(content, "http://");
                            break;
                        case 1:
                            message = nfcMger.createUriMessage(content, "tel: ");
                            break;
                        case 2:
                            message = nfcMger.createUriMessage(content, "mailto: ");
                            break;
                        case 3:
                            message = nfcMger.createTextMessage(content);
                            break;

                    }

                    if (message != null) {

                        dialog = new ProgressDialog(CreateActivity.this);
                        dialog.setMessage("Plese TAP NFC Tag");
                        dialog.show();
                    }
                }

            }
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle("Create Poster Tag");

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            nfcMger.verifyNFC();

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[]{};
            String[][] techList = new String[][]{{android.nfc.tech.Ndef.class.getName()}, {android.nfc.tech.NdefFormatable.class.getName()}};
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        } catch (NFCManager.NFCNotSupported nfcnsup) {
            Snackbar.make(v, "NFC not supported", Snackbar.LENGTH_LONG).show();
        } catch (NFCManager.NFCNotEnabled nfcnEn) {
            Snackbar.make(v, "NFC Not enabled", Snackbar.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        nfcMger.disableDispatch();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        // It is the time to write the tag
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        Ndef ndef = Ndef.get(currentTag);
        if (message != null && writableTag(currentTag)) {
            nfcMger.writeTag(currentTag, message);
            dialog.dismiss();
            Snackbar.make(v, "Smart Poster Successfully Created", Snackbar.LENGTH_LONG).show();

            if (lock.isChecked()) {
                makeTagReadOnly(currentTag);
            }

        } else if (message == null) {
            Snackbar.make(v, "Smart Poster Failed to Create", Snackbar.LENGTH_LONG).show();

        } else {
            dialog.dismiss();
            Snackbar.make(v, "Smart Poster Failed to Create", Snackbar.LENGTH_LONG).show();
        }
    }

    public void makeTagReadOnly(Tag currentTag) {
        if (currentTag == null) {
            return;
        }

        try {
            Ndef ndef = Ndef.get(currentTag);

            if (ndef != null) {
                ndef.connect();

                if (ndef.canMakeReadOnly()) {
                    ndef.makeReadOnly();
                }
                ndef.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writableTag(Tag tag) {

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {

                    ndef.close();
                    return false;
                }
                ndef.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
