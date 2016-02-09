package lpi.sauvegardesamba.sauvegarde;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Gestionnaire de la sauvegarde asynchrone
 */
public class AsyncSauvegardeManager
{

/**
 * Instance unique non préinitialisée
 */
private static AsyncSauvegardeManager INSTANCE = null;
public int _profilId;
public Context _context;
public TYPE_LAUNCHED _type;
private AsyncSauvegarde _asyncTask;
private boolean _isCanceled;

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
			return false;
	}

	_profilId = profilId;
	_isCanceled = false;
	_asyncTask = new AsyncSauvegarde();
	_asyncTask.execute(this);
	_type = type;
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

public enum TYPE_LAUNCHED
{
	MANUEL, AUTO, WIDGET
}
}
