package com.teapink.screencam.activities;

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

import com.teapink.screencam.R;

import static com.teapink.screencam.utilities.FilePath.getPath;

public class UserSettings extends PreferenceActivity {

    private static SharedPreferences spSettings;
    private static SharedPreferences.Editor editSettings;
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
            path.setSummary(String.format(getString(R.string.path_storage_instruction_text),
                    Environment.getExternalStorageDirectory().getPath()));

            //confirm and clear all user data
            Preference prefClear = findPreference("prefClear");
            prefClear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setTitle(R.string.reset_dialog_title);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(R.string.reset_dialog_positive_button_text,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //clear all shared preferences files
                                    editSettings = spSettings.edit();
                                    editSettings.clear();
                                    editSettings.apply();

                                    Toast.makeText(getActivity(), R.string.reset_dialog_success_text,
                                            Toast.LENGTH_SHORT).show();
                                    getFragmentManager().beginTransaction().replace(android.R.id.content,
                                            new UserSettingsFragment()).commit();
                                }
                            });
                    builder1.setNegativeButton(R.string.reset_dialog_negative_button_text,
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