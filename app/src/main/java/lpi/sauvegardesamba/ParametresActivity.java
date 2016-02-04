package lpi.sauvegardesamba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import lpi.sauvegardesamba.utils.Preferences;

public class ParametresActivity extends AppCompatActivity
{
public static final String ACTION_SETTINGS_FINISHED = "lpi.action_settings_finished";

@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_parametres);
	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setDisplayShowTitleEnabled(true);

	Preferences pref = new Preferences(this);
	((EditText) findViewById(R.id.editTextRepertoireSauvegarde)).setText(pref.getPrefRepertoireSauvegarde());
	((EditText) findViewById(R.id.editTextContacts)).setText(pref.getPrefRepertoireContacts());
	((EditText) findViewById(R.id.editTextAppels)).setText(pref.getPrefRepertoireAppels());
	((EditText) findViewById(R.id.editTextMessages)).setText(pref.getPrefRepertoireMessages());
	((EditText) findViewById(R.id.editTextPhotos)).setText(pref.getPrefRepertoirePhotos());
	((EditText) findViewById(R.id.editTextVideos)).setText(pref.getPrefRepertoireVideos());

	((CheckBox) findViewById(R.id.checkBoxRegrouperAppels)).setChecked(pref.getRegrouperAppels());
	((CheckBox) findViewById(R.id.checkBoxRegrouperMessages)).setChecked(pref.getRegrouperMessages());
	((CheckBox) findViewById(R.id.checkBoxRegrouperPhotos)).setChecked(pref.getRegrouperPhotos());
	((CheckBox) findViewById(R.id.checkBoxRegrouperVideos)).setChecked(pref.getRegrouperVideos());
}

public boolean onCreateOptionsMenu(Menu menu)
{
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.menu_dialog_box, menu);
	return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item)
{
	switch (item.getItemId())
	{
		case R.id.buttonOK:
			onOK();
			return true;

		case R.id.buttonCancel:
			onAnnuler();
			return true;

		default:
			// If we got here, the user's action was not recognized.
			// Invoke the superclass to handle it.
			return super.onOptionsItemSelected(item);

	}
}

/**
 * OK: fermer l'ecran et renvoyer les donnees
 */
public void onOK()
{
	Preferences pref = new Preferences(this);
	pref.setPrefRepertoireSauvegarde(((EditText) findViewById(R.id.editTextRepertoireSauvegarde)).getText().toString());
	pref.setPrefRepertoireContacts(((EditText) findViewById(R.id.editTextContacts)).getText().toString());
	pref.setPrefRepertoireAppels(((EditText) findViewById(R.id.editTextAppels)).getText().toString());
	pref.setPrefRepertoireMessages(((EditText) findViewById(R.id.editTextMessages)).getText().toString());
	pref.setPrefRepertoirePhotos(((EditText) findViewById(R.id.editTextPhotos)).getText().toString());
	pref.setPrefRepertoireVideos(((EditText) findViewById(R.id.editTextVideos)).getText().toString());

	pref.setPrefRegrouperAppels(((CheckBox) findViewById(R.id.checkBoxRegrouperAppels)).isChecked());
	pref.setPrefRegrouperMessages(((CheckBox) findViewById(R.id.checkBoxRegrouperMessages)).isChecked());
	pref.setPrefRegrouperPhotos(((CheckBox) findViewById(R.id.checkBoxRegrouperPhotos)).isChecked());
	pref.setPrefRegrouperVideos(((CheckBox) findViewById(R.id.checkBoxRegrouperVideos)).isChecked());

	pref.Save();
	Intent returnIntent = new Intent();
	returnIntent.setAction(ACTION_SETTINGS_FINISHED);
	setResult(Activity.RESULT_OK, returnIntent);

	sendBroadcast(returnIntent);
	finish();
}

public void onAnnuler()
{
	Intent returnIntent = new Intent();
	setResult(Activity.RESULT_CANCELED, returnIntent);
	finish();
}


}
