package lpi.sauvegardesamba.sauvegarde;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

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
public static final String ACTION_ASYNCSAVE = "lpi.SAMBAckup.AsyncSave";
public static final String COMMAND = "commande";
public static final String COMMAND_STARTED = "started";
public static final String COMMAND_FINISHED = "finished";
public static final String COMMAND_PROFIL = "profil";
public static final String COMMAND_PROGRESS = "progress";
public static final String PARAM_FORMAT = "profil.Nom";
public static final String PARAM_CURRENT = "current";
public static final String PARAM_MAX = "max";
public static int TOUS_LES_PROFILS = ProfilsDatabase.INVALID_ID;
static Boolean _enCours = false;
static private volatile Boolean _encours = false;

public static boolean enCours(Context context)
{
	synchronized (_encours)
	{
		_encours = Preferences.getInstance(context).getSauvegardeEnCours();
		return _encours;
	}
}


public static void enCours(Context context, boolean encours)
{
	synchronized (_encours)
	{
		Report.getInstance(context).log(Report.NIVEAU.DEBUG, "ASYNCS: encours=" + encours);
		/*if (encours == _encours)
		{
			// Bizarre, le status est deja le meme
			Report.getInstance(context).log(Report.NIVEAU.WARNING, "Appel de enCours avec la même valeur");
			return;
		}     */
		_encours = encours;
		Preferences pref = Preferences.getInstance(context);
		pref.setSauvegardeEnCours(encours);
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


protected Void doInBackground(@NonNull AsyncSauvegardeManager... params)
{
	AsyncSauvegardeManager manager = params[0];
	int profilId = AsyncSauvegardeManager._profilId;
	Context context = manager._context;

	if (enCours(context))
	{
		Report.getInstance(context).historique("Une sauvegarde est déjà en cours");
		Report.getInstance(context).log(Report.NIVEAU.WARNING, "ASYNCS:Depart d'une sauvegarde alors qu'une autre est en cours");
		return null;
	}

	enCours(context, true);
	signaleDebutSauvegarde(context);
	executeSauvegarde(context, profilId, manager);
	enCours(context, false);
	signaleFinSauvegarde(context);
	return null;
}

private synchronized void executeSauvegarde(@NonNull Context context, int profilId, @NonNull AsyncSauvegardeManager manager)
{
	Report report = Report.getInstance(context);

	try
	{
		ProfilsDatabase database = ProfilsDatabase.getInstance(context);
		if (profilId == TOUS_LES_PROFILS)
		{
			// Sauvegarder tous les profils
			Cursor cursor = database.getCursor();
			if (cursor == null)
			{
				report.log(Report.NIVEAU.ERROR, "ASYNCS:Impossible d'obtenir la liste des profils");
			}
			else
			{
				int noProfil = 0;
				while (cursor.moveToNext() && !manager.isCanceled())
				{
					noProfil++;
					Profil profil = new Profil(cursor);
					sauvegardeUnProfil(profil, context, manager, noProfil, cursor.getCount());
				}
				cursor.close();
			}
		}
		else
		{
			Profil profil = database.getProfil(profilId);
			sauvegardeUnProfil(profil, context, manager, 1, 1);
		}

		if (manager.isCanceled())
		{
			report.log(Report.NIVEAU.DEBUG, context.getString(R.string.sauvegarde_annulee_par_utilisateur)); //$NON-NLS-1$
		}
		else
		{
			report.log(Report.NIVEAU.DEBUG, context.getString(R.string.sauvegarde_terminee_correctement)); //$NON-NLS-1$
		}
	} catch (Exception e)
	{
		report.log(Report.NIVEAU.ERROR, e);
		MainActivity.SignaleErreur("Une erreur est survenue pendant la sauvegarde, vous pouvez consulter le rapport de l'application", e);
	}
}

private void sauvegardeUnProfil(Profil profil, Context context, AsyncSauvegardeManager manager, int current, int max)
{
	if (profil != null)
	{
		signaleProfil(context, profil, current, max);
		profil.Sauvegarde(context, manager);
	}
}

private void signaleProfil(@NonNull Context context, @NonNull Profil profil, int current, int max)
{
	Intent intent = new Intent(ACTION_ASYNCSAVE);
	intent.putExtra(COMMAND, COMMAND_PROFIL);
	Bundle b = new Bundle();
	profil.toBundle(b);
	intent.putExtras(b);
	intent.putExtra(PARAM_CURRENT, current);
	intent.putExtra(PARAM_MAX, max);
	context.sendBroadcast(intent);
}

private void signaleFinSauvegarde(Context context)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "ASYNCS:fin de sauvegarde");
	Intent intent = new Intent(ACTION_ASYNCSAVE);
	intent.putExtra(COMMAND, COMMAND_FINISHED);
	context.sendBroadcast(intent);
}

private void signaleDebutSauvegarde(Context context)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "ASYNCS:debut de sauvegarde");
	Intent intent = new Intent(ACTION_ASYNCSAVE);
	intent.putExtra(COMMAND, COMMAND_STARTED);
	context.sendBroadcast(intent);
}


}
