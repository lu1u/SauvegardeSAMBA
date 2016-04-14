package lpi.sauvegardesamba.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

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

/***
 * Reception des evenements
 *
 * @param context
 * @param intent
 */
@Override
public void onReceive(@NonNull Context context, @NonNull Intent intent)
{
	String action = intent.getAction();
	if (Plannificateur.COMMANDE_SAVE_ALARM.equals(action))
	{
		onSauvegardePlannifiee(context);
	}
	else if ("android.intent.action.BOOT_COMPLETED".equals(action) //$NON-NLS-1$
			|| "android.intent.action.QUICKBOOT_POWERON".equals(action)) //$NON-NLS-1$
		onBoot(context);
	else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
		onConnexionWIFI(context, intent);
}

/***
 * Redemarrage du systeme
 * @param context
 */
private void onBoot(Context context)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "ALRM: onBoot");
	// Redemarrage, une sauvegarde ne peut donc pas etre en cours,
	// on l'enregistre pour le cas ou la machine aurait ete eteinte en plein milieu d'une
	// sauvegarde
	Preferences.getInstance(context).setSauvegardeEnCours(false);
	Plannificateur.plannifieSauvegarde(context);
}

/***
 * Detection d'un changement de connectivite, s'agit-il d'une connexion WIFI?
 *
 * @param context
 * @param intent
 */
private void onConnexionWIFI(Context context, Intent intent)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "ALRM: Detection du changement de reseau");
	Preferences pref = Preferences.getInstance(context);

	// Doit-on essayer de sauvegarder des qu'une connexion WIFI est detectée?
	if (!pref.getDetectionWIFI())
		return;

	// On nous previent du changement de connectivite: si on est connecte maintenant, c'est qu'on
	// n'etait pas connecte avant
	if (Profil.IsWifiConnected(context))
	{
		Report.getInstance(context).historique("ALRM:Sauvegarde sur détection WIFI");
		AsyncSauvegardeManager manager = AsyncSauvegardeManager.getInstance(context);
		manager.startSauvegarde(AsyncSauvegarde.TOUS_LES_PROFILS, AsyncSauvegardeManager.TYPE_LAUNCHED.AUTO);
	}
}

/***
 * Alarme pour une
 *
 * @param context
 */
private void onSauvegardePlannifiee(Context context)
{
	Report.getInstance(context).historique("ALRM:Sauvegarde plannifiée");
	AsyncSauvegardeManager manager = AsyncSauvegardeManager.getInstance(context);
	manager.startSauvegarde(AsyncSauvegarde.TOUS_LES_PROFILS, AsyncSauvegardeManager.TYPE_LAUNCHED.AUTO);
}
}
