package lpi.sauvegardesamba.sauvegarde;

import android.content.Context;
import android.os.AsyncTask;

import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Report;

/**
 * Gestionnaire de la sauvegarde asynchrone
 */
public class AsyncSauvegardeManager
{

public static int _profilId;
/**
 * Instance unique non préinitialisée
 */
private static AsyncSauvegardeManager INSTANCE = null;
private static AsyncSauvegarde _asyncTask;
private static boolean _isCanceled;
public final Context _context;
public TYPE_LAUNCHED _type;

private AsyncSauvegardeManager(Context context)
{
	_context = context;
}

/**
 * Point d'accès pour l'instance unique du singleton
 */
public static synchronized AsyncSauvegardeManager getInstance(Context context)
{
	if (INSTANCE == null)
	{
		INSTANCE = new AsyncSauvegardeManager(context);
	}
	return INSTANCE;
}

public boolean startSauvegarde(int profilId, TYPE_LAUNCHED type)
{
	if (_asyncTask != null)
	{
		if (_asyncTask.getStatus() != AsyncTask.Status.FINISHED)
			// Déjà en cours
			return false;
	}

	_profilId = profilId;
	_isCanceled = false;
	_type = type;
	_asyncTask = new AsyncSauvegarde();
	_asyncTask.execute(this);
	return true;
}

public void cancel()
{
	_isCanceled = true;
}

public boolean isCanceled()
{
	return _isCanceled;
}

public void forceCancel(Context context)
{
	Preferences.getInstance(context).setSauvegardeEnCours(false);
	Report report = Report.getInstance(context);
	report.log(Report.NIVEAU.WARNING, "Annulation forcee de la sauvegarde");

	if (_asyncTask == null)
	{
		report.log(Report.NIVEAU.WARNING, "thread asynctask est a null");
		return;
	}

	if (_asyncTask.getStatus() == AsyncTask.Status.FINISHED)
	{
		report.log(Report.NIVEAU.WARNING, "thread asynctask deja termine");
		return;
	}

	report.log(Report.NIVEAU.WARNING, "tentative d'arret du thread asynctack");
	_asyncTask.cancel(true);
}

public enum TYPE_LAUNCHED
{
	MANUEL, AUTO, WIDGET
}
}
