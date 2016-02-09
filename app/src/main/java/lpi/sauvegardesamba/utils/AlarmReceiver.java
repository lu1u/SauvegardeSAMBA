package lpi.sauvegardesamba.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegarde;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegardeManager;
import lpi.sauvegardesamba.sauvegarde.Plannificateur;

/**
 *
 */

/**
 * @author lucien
 */
public class AlarmReceiver extends BroadcastReceiver
{
Context _context;

@Override
public void onReceive(Context context, Intent intent)
{
	_context = context;
	String action = intent.getAction();
	if (Plannificateur.COMMANDE_SAVE_ALARM.equals(action))
	{
		lanceSauvegardeAuto(context);

	}
	else if ("android.intent.action.BOOT_COMPLETED".equals(action) //$NON-NLS-1$
			|| "android.intent.action.QUICKBOOT_POWERON".equals(action)) //$NON-NLS-1$
	{

		Plannificateur plannificateur = new Plannificateur(context);
		plannificateur.plannifieSauvegarde();
	}
	else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
		connexionWifi(context, intent);
}

private void connexionWifi(Context context, Intent intent)
{
	Preferences pref = new Preferences(context);
	if (!pref.getDetectionWIFI())
		return;

	// On nous previent du changement de connectivite: si on est connecte maintenant, c'est qu'on
	// n'etait pas connecte avant
	if (Profil.IsWifiConnected(context))
		lanceSauvegardeAuto(context);
}

private void lanceSauvegardeAuto(Context context)
{
	AsyncSauvegardeManager manager = AsyncSauvegardeManager.getInstance(context);
	manager.startSauvegarde(AsyncSauvegarde.TOUS_LES_PROFILS, AsyncSauvegardeManager.TYPE_LAUNCHED.AUTO);
}
}
