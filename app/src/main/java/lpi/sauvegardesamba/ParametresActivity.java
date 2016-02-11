package lpi.sauvegardesamba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import lpi.sauvegardesamba.sauvegarde.SavedObject.SavedObject;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Utils;

public class ParametresActivity extends AppCompatActivity
{
public static final String ACTION_SETTINGS_FINISHED = "lpi.action_settings_finished";
int _theme;

@Override
protected void onCreate(Bundle savedInstanceState)
{
	Utils.setTheme(this);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_parametres);
	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setDisplayShowTitleEnabled(true);

	Preferences pref = Preferences.getInstance(this);
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

	configureHints();

	final Spinner spinner = (Spinner) findViewById(R.id.spinnerTheme);
	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.themes, android.R.layout.simple_spinner_item);
	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	spinner.setAdapter(adapter);
	_theme = Preferences.getInstance(this).getTheme();
	spinner.setSelection(_theme);
	spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			Activity a = ParametresActivity.this;
			Preferences pref = Preferences.getInstance(a);
			if (position != pref.getTheme())
			{
				_theme = position;
				pref.setTheme(_theme);
				pref.save();
				TaskStackBuilder.create(a)
						.addNextIntent(new Intent(a, MainActivity.class))
						.addNextIntent(a.getIntent())
						.startActivities();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{

		}
	});
}

private void configureHints()
{
	Utils.addHint(this, R.id.editTextRepertoireSauvegarde, "Nom du dossier qui sera créé dans votre dossier partagé pour contenir la sauvegarde\nDoit être un nom de dossier correct et non vide");
	Utils.addHint(this, R.id.editTextContacts, "Nom du dossier qui sera créé dans le dossier de la sauvegarde pour contenir les contacts\nDoit être un nom de dossier correct et non vide");
	Utils.addHint(this, R.id.editTextAppels, "Nom du dossier qui sera créé dans le dossier de la sauvegarde pour contenir les appels\nDoit être un nom de dossier correct et non vide");
	Utils.addHint(this, R.id.editTextMessages, "Nom du dossier qui sera créé dans le dossier de la sauvegarde pour contenir les messages\nDoit être un nom de dossier correct et non vide");
	Utils.addHint(this, R.id.editTextPhotos, "Nom du dossier qui sera créé dans le dossier de la sauvegarde pour contenir les photos\nDoit être un nom de dossier correct et non vide");
	Utils.addHint(this, R.id.editTextVideos, "Nom du dossier qui sera créé dans le dossier de la sauvegarde pour contenir les vidéos\nDoit être un nom de dossier correct et non vide");
	Utils.addHint(this, R.id.checkBoxRegrouperAppels, "Cochez cette case pour que les appels soient regroupés dans un dossier par contact. Sinon, tous les appels seront dans le même dossier");
	Utils.addHint(this, R.id.checkBoxRegrouperMessages, "Cochez cette case pour que les messages soient regroupés dans un dossier par discussion. Sinon, tous les messages seront dans le même dossier");
	Utils.addHint(this, R.id.checkBoxRegrouperPhotos, "Cochez cette case pour que les photos soient regroupés dans un dossier par catégorie comme sur votre téléphone. Sinon, toutes les photos eront dans le même dossier");
	Utils.addHint(this, R.id.checkBoxRegrouperVideos, "Cochez cette case pour que les vidéos soient regroupées dans un dossier par catégorie comme sur votre téléphone. Sinon, toutes les vidéos eront dans le même dossier");
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
	boolean erreur = false;
	if (!isPathValid(R.id.editTextRepertoireSauvegarde))
		erreur = true;
	if (!isPathValid(R.id.editTextContacts))
		erreur = true;
	if (!isPathValid(R.id.editTextAppels))
		erreur = true;
	if (!isPathValid(R.id.editTextMessages))
		erreur = true;
	if (!isPathValid(R.id.editTextPhotos))
		erreur = true;
	if (!isPathValid(R.id.editTextVideos))
		erreur = true;

	if (erreur)
		return;
	Preferences pref = Preferences.getInstance(this);
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
	pref.setTheme(_theme);
	pref.save();
	Intent returnIntent = new Intent();
	returnIntent.setAction(ACTION_SETTINGS_FINISHED);
	setResult(Activity.RESULT_OK, returnIntent);

	sendBroadcast(returnIntent);
	finish();
}

private boolean isPathValid(int id)
{
	final EditText et = (EditText) findViewById(id);
	String path = et.getText().toString();
	if (validPath(path))
	{
		et.setError(null);
		return true;
	}

	et.setError("Veuillez donner un nom de dossier correct");
	return false;
}

private boolean validPath(String path)
{
	if (path == null)
		return false;

	if (path.isEmpty())
		return false;

	return SavedObject.fileNameOk(path);

}

public void onAnnuler()
{
	Intent returnIntent = new Intent();
	setResult(Activity.RESULT_CANCELED, returnIntent);
	finish();
}


}
