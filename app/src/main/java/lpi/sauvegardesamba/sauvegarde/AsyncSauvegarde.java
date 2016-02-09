package lpi.sauvegardesamba.sauvegarde;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.database.ProfilsDatabase;
import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Report;

/**
 * Sauvegarde asynchrone
 * Created by lucien on 09/02/2016.
 */
public class AsyncSauvegarde extends AsyncTask<AsyncSauvegardeManager, Void, Void>           // Do in backgroud, update, postexecute
{
public static String ACTION_ASYNCSAVE = "lpi.SAMBAckup.AsyncSave";
public static String COMMAND = "commande";
public static String COMMAND_STARTED = "started";
public static String COMMAND_FINISHED = "finished";
public static String COMMAND_PROFIL = "profil";
public static String COMMAND_PROGRESS = "progress";
public static String PARAM_FORMAT = "profil.Nom";
public static String PARAM_CURRENT = "current";
public static String PARAM_MAX = "max";
public static int TOUS_LES_PROFILS = ProfilsDatabase.INVALID_ID;
static private volatile Boolean _encours = false;
ProfilsDatabase _database;
private Context _context;

public static boolean enCours(Context context)
{
	synchronized (_encours)
	{
		_encours = new Preferences(context).getSauvegardeEnCours();
		return _encours;
	}
}

public static void signaleProgress(Context context, String format, int current, int max)
{
	Intent intent = new Intent(ACTION_ASYNCSAVE);
	intent.putExtra(COMMAND, COMMAND_PROGRESS);
	intent.putExtra(PARAM_FORMAT, format);
	intent.putExtra(PARAM_CURRENT, current);
	intent.putExtra(PARAM_MAX, max);
	context.sendBroadcast(intent);
}

public static String getLocalizedTimeAndDate(Context context, Calendar c)
{
	if (c == null)
		c = Calendar.getInstance();

	return android.text.format.DateFormat.getDateFormat(context).format(c.getTime()) + ' '
			+ android.text.format.DateFormat.getTimeFormat(context).format(c.getTime());
}

public static String getLocalizedTimeAndDate(Context context, long d)
{
	Calendar c = Calendar.getInstance();
	c.setTimeInMillis(d);

	return getLocalizedTimeAndDate(context, c);
}

/***
 * Affichage de ic_message d'erreur
 * <p/>
 * aparam localizedMessage
 */
public static void Erreur(Context c, String localizedMessage)
{
	Log.e("SAMBA", localizedMessage);
	Toast.makeText(c, "ERREUR: " + localizedMessage, Toast.LENGTH_LONG).show(); //$NON-NLS-1$
}

/***
 * Charge une chaine de caracteres depuis les ressources et ajoute eventuellement des arguments
 * <p/>
 * aparam resId
 * aparam args
 */
static public String formatResourceString(Context context, int resId, Object... args)
{
	String format = context.getResources().getString(resId);
	return String.format(format, args);
}

private void enCours(boolean encours)
{
	synchronized (_encours)
	{
		if (encours == _encours)
		{
			// Bizarre, le status est deja le meme
			return;
		}
		Preferences pref = new Preferences(_context);
		pref.setSauvegardeEnCours(encours);
		pref.save();
		_encours = encours;
	}
}

protected Void doInBackground(AsyncSauvegardeManager... params)
{
	AsyncSauvegardeManager manager = params[0];
	int profilId = manager._profilId;
	_context = manager._context;

	if (enCours(_context))
		return null;

	signaleDebutSauvegarde();
	executeSauvegarde(_context, profilId, manager);
	signaleFinSauvegarde();
	return null;
}

private synchronized void executeSauvegarde(Context context, int profilId, AsyncSauvegardeManager manager)
{
	Preferences pref = new Preferences(_context);
	if (enCours(context))
	{
		Report.historique("Une sauvegarde est déjà en cours");
		return;
	}

	enCours(true);
	try
	{
		Report.Log(Report.NIVEAU.DEBUG, "Depart sauvegarde ");

		_database = ProfilsDatabase.getInstance(_context);
		if (profilId == TOUS_LES_PROFILS)
		{
			// Sauvegarder tous les profils
			Cursor cursor = _database.getCursor();
			if (cursor == null)
			{
				Report.Log(Report.NIVEAU.ERROR, "Impossible d'obtenir la liste des profils");
			}
			else
			{
				int noProfil = 0;
				while (cursor.moveToNext() && !manager.isCanceled())
				{
					noProfil++;
					Profil profil = new Profil(cursor);
					signaleProfil(profil, noProfil, cursor.getCount());
					profil.Sauvegarde(_context, manager);
				}
				cursor.close();
			}
		}
		else
		{
			Profil profil = _database.getProfil(profilId);
			signaleProfil(profil, 1, 1);
			profil.Sauvegarde(_context, manager);
		}

		if (manager.isCanceled())
		{
			Report.Log(Report.NIVEAU.DEBUG, _context.getString(R.string.sauvegarde_annulee_par_utilisateur)); //$NON-NLS-1$
		}
		else
		{
			Report.Log(Report.NIVEAU.DEBUG, _context.getString(R.string.sauvegarde_terminee_correctement)); //$NON-NLS-1$
		}
	} catch (Exception e)
	{
		Report.Log(Report.NIVEAU.ERROR, e);
		MainActivity.SignaleErreur("Une erreur est survenue pendant la sauvegarde, vous pouvez consulter le rapport de l'application", e);
	} finally
	{
		enCours(false);
	}
}

private void signaleProfil(Profil profil, int current, int max)
{
	Intent intent = new Intent(ACTION_ASYNCSAVE);
	intent.putExtra(COMMAND, COMMAND_PROFIL);
	Bundle b = new Bundle();
	profil.toBundle(b);
	/*intent.putExtra(PARAM_PROFIL_NOM, profil.Nom);
	intent.putExtra(PARAM_PROFIL_PARTAGE, profil.Partage);
	intent.putExtra(PARAM_PROFIL_ID, profil.Id); */
	intent.putExtras(b);
	intent.putExtra(PARAM_CURRENT, current);
	intent.putExtra(PARAM_MAX, max);
	_context.sendBroadcast(intent);
}

private void signaleFinSauvegarde()
{
	Intent intent = new Intent(ACTION_ASYNCSAVE);
	intent.putExtra(COMMAND, COMMAND_FINISHED);
	_context.sendBroadcast(intent);
}

private void signaleDebutSauvegarde()
{
	Intent intent = new Intent(ACTION_ASYNCSAVE);
	intent.putExtra(COMMAND, COMMAND_STARTED);
	_context.sendBroadcast(intent);
}

/***
 * Charge une chaine de caracteres depuis les ressources et ajoute eventuellement des arguments
 * <p/>
 * aparam resId
 * aparam args
 */
public String formatResourceString(int resId, Object... args)
{
	String format = _context.getResources().getString(resId);
	return String.format(format, args);
}

public String getLocalizedTimeAndDate(Calendar c)
{
	if (c == null)
		c = Calendar.getInstance();

	return android.text.format.DateFormat.getDateFormat(_context).format(c.getTime()) + ' '
			+ android.text.format.DateFormat.getTimeFormat(_context).format(c.getTime());
}


}
