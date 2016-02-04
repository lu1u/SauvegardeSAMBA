package lpi.sauvegardesamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.profils.ProfilsAdapter;
import lpi.sauvegardesamba.profils.ProfilsDatabase;
import lpi.sauvegardesamba.sauvegarde.Plannificateur;
import lpi.sauvegardesamba.sauvegarde.Sauvegarde;
import lpi.sauvegardesamba.utils.Preferences;

public class MainActivity extends AppCompatActivity
{
static public final int RESULT_EDIT_PROFIL = 0;
static public final int RESULT_LANCE_SAUVEGARDE = 1;
private static final int RESULT_PREFERENCES = 2;
private static final int RESULT_PLANNIFICATION = 3;
private static final int RESULT_REPORT = 4;
private static final int RESULT_ABOUT = 5;


private static AppCompatActivity _applicationActivity;
private ProfilsAdapter _adapterProfils;
private int _currentItemSelected = -1;
private BroadcastReceiver receiver = new BroadcastReceiver()
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();

		if (ProfilsAdapter.ACTION_LANCE_SAUVEGARDE.equals(action))
			lancerSauvegardeProfil(intent);
		else
			if ( EditProfileActivity.ACTION_EDIT_PROFIL_FINISHED.equals(action))
				onEditProfile(intent);
		else
				if ( Sauvegarde.ACTION_INFOS_FROM_SAUVEGARDE.equals(action))
					onInfoFromService(intent) ;
	}
};

private void onInfoFromService(Intent intent)
{
	_adapterProfils.changeCursor(ProfilsDatabase.getInstance(this).getCursor());
}


@Override
/***
 * Creation de l'activity
 */
protected void onCreate(Bundle savedInstanceState)
{
	//setTheme(R.style.AppTheme2);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);

	FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
	fab.setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
			startActivityForResult(intent, RESULT_EDIT_PROFIL);
		}
	});

	IntentFilter filter = new IntentFilter();
	filter.addAction(ProfilsAdapter.ACTION_LANCE_SAUVEGARDE);
	filter.addAction(EditProfileActivity.ACTION_EDIT_PROFIL_FINISHED);
	filter.addAction(Sauvegarde.ACTION_INFOS_FROM_SAUVEGARDE);
	registerReceiver(receiver, filter);

	InitProfils();
}


@Override
public boolean onCreateOptionsMenu(Menu menu)
{
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.menu_main, menu);
	return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item)
{
	// Handle action bar item clicks here. The action bar will
	// automatically handle clicks on the Home/Up button, so long
	// as you specify a parent activity in AndroidManifest.xml.
	int id = item.getItemId();

	switch (id)
	{
		case R.id.action_settings:
		{
			Intent intent = new Intent(MainActivity.this, ParametresActivity.class);
			startActivityForResult(intent, RESULT_PREFERENCES);
			return true;
		}

		case R.id.action_plannification:
		{
			Intent intent = new Intent(MainActivity.this, PlannificationActivity.class);
			startActivityForResult(intent, RESULT_PLANNIFICATION);
			return true;
		}
		case R.id.action_report:
		{
			Intent intent = new Intent(MainActivity.this, RapportActivity.class);
			startActivityForResult(intent, RESULT_REPORT);
			return true;
		}

		case R.id.action_about:
		{
			Intent intent = new Intent(MainActivity.this, AProposActivity.class);
			startActivityForResult(intent, RESULT_ABOUT);
			return true;
		}

		case R.id.action_lancer_tous:
			lancerSauvegardeTout();
			return true;

		case R.id.action_nouveau_profil:
			Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
			startActivityForResult(intent, RESULT_EDIT_PROFIL);
			break ;
	}

	return super.onOptionsItemSelected(item);
}

/***
 * Initialisation de la liste des profils a partir de la base de donnees
 */
private void InitProfils()
{
	ListView listView = (ListView) findViewById(R.id.listView);
	listView.setEmptyView(findViewById(R.id.textViewEmpty));

	_adapterProfils = new ProfilsAdapter(this, ProfilsDatabase.getInstance(this).getCursor());
	listView.setAdapter(_adapterProfils);
	registerForContextMenu(listView);

	listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			view.setSelected(true);
			_currentItemSelected = position;
			//ModifieProfil();
		}
	});

	listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
			view.setSelected(true);
			_currentItemSelected = position;
			return false;
		}
	});
}

/***
 * Signaler une erreur
 *
 * @param message
 * @param e
 */

static public void SignaleErreur(String message, Exception e)
{
	LayoutInflater inflater = _applicationActivity.getLayoutInflater();
	View layout = inflater.inflate(R.layout.layout_toast_erreur,
			(ViewGroup) _applicationActivity.findViewById(R.id.layoutRoot));

	TextView tv = (TextView) layout.findViewById(R.id.textViewTextErreur);
	String m = String.format(tv.getText().toString(), message);
	tv.setText(m);

	m = e.getLocalizedMessage();
	int nbMax = 0;
	for (StackTraceElement s : e.getStackTrace())
	{
		m += "\n" + (s.getClassName() + '/' + s.getMethodName() + ':' + s.getLineNumber());
		nbMax++;
		if (nbMax > 2)
			break;
	}

	((TextView) layout.findViewById(R.id.textViewStackTrace)).setText(m);
	Toast toast = new Toast(_applicationActivity.getApplicationContext());
	toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	toast.setDuration(Toast.LENGTH_LONG);
	toast.setView(layout);
	toast.show();

}

/**
 * Dispatch incoming result to the correct fragment.
 *
 * @param requestCode
 * @param resultCode
 * @param data
 */
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data)
{
	super.onActivityResult(requestCode, resultCode, data);
	if (resultCode == RESULT_CANCELED)
		return;

	switch (requestCode)
	{
		case RESULT_EDIT_PROFIL:
			onEditProfile(data);
			break;
	}
}

/***
 * Reception du résultat de l'activite d'edition d'un profil
 *
 * @param data
 */
private void onEditProfile(Intent data)
{
	String Operation = data.getExtras().getString(EditProfileActivity.EXTRA_OPERATION);
	Profil intentProfil = new Profil(data.getExtras());

	ProfilsDatabase database = ProfilsDatabase.getInstance(this);
	if (EditProfileActivity.EXTRA_OPERATION_AJOUTE.equals(Operation))
	{
		// Ajouter le profil
		database.Ajoute(intentProfil);
		_adapterProfils.changeCursor(database.getCursor());
		_currentItemSelected = -1;
	}
	else if (EditProfileActivity.EXTRA_OPERATION_MODIFIE.equals(Operation))
	{
		// Modifier le profil
		database.ModifieProfil(intentProfil);
		_adapterProfils.changeCursor(database.getCursor());
		//_currentItemSelected = -1;
	}

}

@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
{
	super.onCreateContextMenu(menu, v, menuInfo);
	if (v.getId() == R.id.listView)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_liste, menu);
	}
}

@Override
public boolean onContextItemSelected(MenuItem item)
{
	switch (item.getItemId())
	{
		case R.id.action_modifier:
			ModifieProfil();
			return true;
		case R.id.action_supprimer:
			SupprimeProfil();
			return true;
		default:
			return super.onContextItemSelected(item);
	}
}

private void SupprimeProfil()
{
	if (_currentItemSelected == -1)
		return;

	final Profil profilASupprimer = _adapterProfils.get(_currentItemSelected);

	if (profilASupprimer != null)
	{
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setTitle("Supprimer");
		dialog.setMessage("Supprimer le profil " + profilASupprimer.Nom + " ?");
		dialog.setCancelable(false);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int buttonId)
					{
						if (profilASupprimer != null)
						{
							ProfilsDatabase database = ProfilsDatabase.getInstance(MainActivity.this);
							// Supprimer
							database.SupprimeProfil(profilASupprimer);
							_adapterProfils.changeCursor(database.getCursor());
							_currentItemSelected = -1;
						}
					}
				});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int buttonId)
					{
						// Ne rien faire
					}
				});
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.show();
	}
}

private void ModifieProfil()
{
	if (_currentItemSelected == -1)
		return;

	Profil profil = _adapterProfils.get(_currentItemSelected);

	Intent intent = new Intent(this, EditProfileActivity.class);
	Bundle b = new Bundle();
	profil.toBundle(b);
	b.putString(EditProfileActivity.EXTRA_OPERATION, EditProfileActivity.EXTRA_OPERATION_MODIFIE);
	intent.putExtras(b);
	startActivityForResult(intent, RESULT_EDIT_PROFIL);
}


/**
 * Dispatch onResume() to fragments.  Note that for better inter-operation
 * with older versions of the platform, at the point of this call the
 * fragments attached to the activity are <em>not</em> resumed.  This means
 * that in some cases the previous state may still be saved, not allowing
 * fragment transactions that modify the state.  To correctly interact
 * with fragments in their proper state, you should instead override
 * {@link #onResumeFragments()}.
 */
@Override
protected void onResume()
{
	super.onResume();
	_adapterProfils.changeCursor(ProfilsDatabase.getInstance(this).getCursor());

	Preferences pref = new Preferences(this);
	if ( pref.getSauvegarderAuto())
	{
		Plannificateur p = new Plannificateur(this);
		((TextView)findViewById(R.id.textViewStatusSauvegardePlannifiee)).setText(p.getTextProchaineSauvegarde(null));
	}
	else
		((TextView)findViewById(R.id.textViewStatusSauvegardePlannifiee)).setText("Pas de sauvegarde automatique plannifiée");
}

/***
 * Lance la sauvegarde manuelle d'un profil
 *
 * @param intent
 */
private void lancerSauvegardeProfil(Intent intent)
{
	int Id = intent.getIntExtra(ProfilsAdapter.PARAM_ID, -1);
	if (Id == -1)
		return;

	// Ouvrir l'activity "Sauvegarde en cours
	Intent intentForResult = new Intent(MainActivity.this, SauvegardeEnCoursActivity.class);
	intentForResult.putExtra(SauvegardeEnCoursActivity.PARAM_ID, Id);
	startActivityForResult(intentForResult, RESULT_LANCE_SAUVEGARDE);
}

/***
 * Lance la sauvegarde manuelle de tous les profils
 */
private void lancerSauvegardeTout()
{
	// Ouvrir l'activity "Sauvegarde en cours
	Intent intentForResult = new Intent(MainActivity.this, SauvegardeEnCoursActivity.class);
	intentForResult.putExtra(SauvegardeEnCoursActivity.PARAM_ID, Sauvegarde.TOUS_LES_PROFILS);
	startActivityForResult(intentForResult, RESULT_LANCE_SAUVEGARDE);
}

public static void MessageNotification(View v, String message)
{
	Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
}

}


