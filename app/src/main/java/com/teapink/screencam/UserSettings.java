package com.teapink.screencam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.widget.Toast;

import static com.teapink.screencam.FilePath.getPath;

public class UserSettings extends PreferenceActivity {

    static SharedPreferences spSettings;
    static SharedPreferences.Editor editSettings;
    private static final int RQS_OPEN_DOCUMENT_TREE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        getFragmentManager().beginTransaction().replace(android.R.id.content, new UserSettingsFragment()).commit();
    }

    public static class UserSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            //let user select storage path
            Preference path = findPreference("prefPath");
            path.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, RQS_OPEN_DOCUMENT_TREE);
                    return false;
                }
            });
            path.setSummary("Set storage path for the recording.\n" +
                    "Currently allows paths that are in \n" + Environment.getExternalStorageDirectory().getPath() + " only.");

            //confirm and clear all user data
            Preference prefClear = findPreference("prefClear");
            prefClear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setTitle("RESET");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //clear all shared preferences files
                                    editSettings = spSettings.edit();
                                    editSettings.clear();
                                    editSettings.apply();

                                    Toast.makeText(getActivity(), "Okay, done.", Toast.LENGTH_SHORT).show();
                                    getFragmentManager().beginTransaction().replace(android.R.id.content, new UserSettingsFragment()).commit();
                                }
                            });
                    builder1.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //don't do anything
                                    dialog.cancel();
                                }
                            });

                    AlertDialog clearAll = builder1.create();
                    clearAll.show();

                    return false;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE) {
                Uri uri = data.getData();
                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
                String path = getPath(getActivity(), docUri);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("path", path);
                editor.apply();
            }
        }
    }
}