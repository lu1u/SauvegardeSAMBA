package lpi.sauvegardesamba;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import lpi.sauvegardesamba.partages.Partages;
import lpi.sauvegardesamba.profils.Profil;

public class EditProfileActivity extends AppCompatActivity
{
public static final int RESULT_EDIT_PROFILE = 1;
public static final String ACTION_EDIT_PROFIL_FINISHED = "lpi.EDITEPROFIL";
public static final String EXTRA_OPERATION = "lpi.OPERATION";
public static final String EXTRA_OPERATION_AJOUTE = "AJOUTE";
public static final String EXTRA_OPERATION_MODIFIE = "MODIFIE";

String Operation;
Profil _profil;

EditText eNom;
EditText eUtilisateur;
EditText eMotDePasse;
//CheckBox cbActif;
CheckBox cbWifi;
CheckBox cbPlannifie;
CheckBox cbContacts;
CheckBox cbAppels;
CheckBox cbMessages;
CheckBox cbPhotos;
CheckBox cbVideos;
Button btPartage;
TextView tvId;
private BroadcastReceiver receiver = new BroadcastReceiver()
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (Partages.ACTION_RESULT_RECHERCHE_PARTAGE.equals(intent.getAction()))
			onReceiveListePartages(intent);
	}
};
@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_edit_profile);
	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setDisplayShowTitleEnabled(true);

	eNom = (EditText) findViewById(R.id.editTextNomProfil);
	eUtilisateur = (EditText) findViewById(R.id.editTextNomUtilisateur);
	eMotDePasse = (EditText) findViewById(R.id.editTextMotDePasse);
	btPartage = (Button) findViewById(R.id.buttonPartage);

	//cbActif = (CheckBox) findViewById(R.id.checkBoxActif);
	cbContacts = (CheckBox) findViewById(R.id.checkBoxContacts);
	cbAppels = (CheckBox) findViewById(R.id.checkBoxAppels);
	cbMessages = (CheckBox) findViewById(R.id.checkBoxMessages);
	cbPhotos = (CheckBox) findViewById(R.id.checkBoxPhotos);
	cbVideos = (CheckBox) findViewById(R.id.checkBoxVideos);
	cbWifi = (CheckBox) findViewById(R.id.checkBoxWifi);
	cbPlannifie = (CheckBox)findViewById(R.id.checkBoxPlannifiee);
	tvId = (TextView) findViewById(R.id.textViewId);

	if (savedInstanceState == null)
		savedInstanceState = this.getIntent().getExtras();

	if (savedInstanceState != null)
	{
		_profil = new Profil(savedInstanceState);
		Operation = savedInstanceState.getString(EXTRA_OPERATION);
	}
	else
	{
		_profil = new Profil();
		Operation = EXTRA_OPERATION_AJOUTE;
	}

	MajUI();
}

private void MajUI()
{
	if (_profil.Nom != null)
		eNom.setText(_profil.Nom);

	if (_profil.Utilisateur != null)
		eUtilisateur.setText(_profil.Utilisateur);

	if (_profil.MotDePasse != null)
		eMotDePasse.setText(_profil.MotDePasse);

	if (_profil.Partage == null)
		btPartage.setText("Choisissez un répertoire partagé");
	else
		btPartage.setText(_profil.Partage);

	if (_profil.Id != -1)
		tvId.setText("Id " + _profil.Id);
	else
		tvId.setVisibility(View.GONE);

	cbPlannifie.setChecked(_profil.Plannifie);
	cbWifi.setChecked(_profil.Wifi);
	//cbActif.setChecked(_profil.SauvegardeManuelle);
	cbAppels.setChecked(_profil.Appels);
	cbContacts.setChecked(_profil.Contacts);
	cbMessages.setChecked(_profil.Messages);
	cbPhotos.setChecked(_profil.Photos);
	cbVideos.setChecked(_profil.Videos);
}



@Override
protected void onResume()
{
	IntentFilter filter = new IntentFilter();
	filter.addAction(Partages.ACTION_RESULT_RECHERCHE_PARTAGE);
	registerReceiver(receiver, filter);
	super.onResume();
}

@Override
protected void onPause()
{
	unregisterReceiver(receiver);
	super.onPause();
}
@Override
public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.menu_dialog_box, menu);
	return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
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
	Intent returnIntent = new Intent();
	returnIntent.setAction(ACTION_EDIT_PROFIL_FINISHED);
	returnIntent.putExtra("result", RESULT_EDIT_PROFILE);

	_profil.Nom = eNom.getText().toString();
	_profil.Utilisateur = eUtilisateur.getText().toString();
	_profil.MotDePasse = eMotDePasse.getText().toString();

	if ("".equals(_profil.Nom))
	{
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
		MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), "Veuillez donner un nom à votre profil") ;
		return ;
	}

	if (_profil.Partage == null || "".equals(_profil.Partage))
	{
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
		MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), "Veuillez choisir un partage réseau");
		return ;
	}

	_profil.Wifi = cbWifi.isChecked();
	_profil.Plannifie = cbPlannifie.isChecked();
	//_profil.SauvegardeManuelle = cbActif.isChecked();
	_profil.Appels = cbAppels.isChecked();
	_profil.Contacts = cbContacts.isChecked();
	_profil.Messages = cbMessages.isChecked();
	_profil.Photos = cbPhotos.isChecked();
	_profil.Videos = cbVideos.isChecked();

	Bundle bundle = new Bundle();
	_profil.toBundle(bundle);

	bundle.putString(EXTRA_OPERATION, Operation);
	returnIntent.putExtras(bundle);
	setResult(Activity.RESULT_OK, returnIntent);
	finish();
}

public void onAnnuler()
{
	Intent returnIntent = new Intent();
	setResult(Activity.RESULT_CANCELED, returnIntent);
	finish();
}


public void onClickChoisirPartage(View v)
{
	_profil.Utilisateur = eUtilisateur.getText().toString();
	_profil.MotDePasse = eMotDePasse.getText().toString();

	Partages.LancheRecherchePartage(this, _profil.Utilisateur, _profil.MotDePasse);
}


/***
 * Reception de la liste des partages trouves sur le reseau et lancement d'une fenetre pour en choisir un
 * @param intent
 */
private void onReceiveListePartages(Intent intent)
{
	if ( intent == null)
		return ;

	int retour = intent.getIntExtra(Partages.RESULT_RECHERCHE, Partages.RESULT_ERREUR);
	if (retour != Partages.RESULT_OK)
	{
		MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), "Erreur pendant la recherche des partages:\n" + intent.getStringExtra(Partages.RESULT_MESSAGE));
		return;
	}

	ArrayList<String> liste = intent.getStringArrayListExtra(Partages.LISTE_RESULT);
	if (liste == null || liste.size()==0)
	{
		MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), "Aucun partage trouvé sur cet ordinateur");
		return;
	}

	AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
	builderSingle.setTitle("Partages");

	final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, liste);
	builderSingle.setAdapter(
			arrayAdapter,
			new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					_profil.Partage = arrayAdapter.getItem(which);
					MajUI();
				}
			});
	builderSingle.show();
}

}
