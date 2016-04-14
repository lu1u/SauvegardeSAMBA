/**
 *
 */
package lpi.sauvegardesamba.sauvegarde;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Calendar;

import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.utils.AlarmReceiver;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Report;

/**
 * @author lucien
 */
public class Plannificateur
{
public final static String COMMANDE_SAVE_ALARM = "lpi.Sauvegarde.Alarme"; //$NON-NLS-1$
private final static String TAG = "Sauvegarde"; //$NON-NLS-1$


/***
 * Retourne l'heure de la prochaine sauvegarde a partir de maintenant
 *
 * @param context
 * @return un Calendar ou null si sauvegarde desactivee
 */
@Nullable
static public Calendar getProchaineSauvegarde(Context context)
{
	Preferences pref = Preferences.getInstance(context);

	if (pref.getSauvegarderAuto())
	{
		int heure = pref.getSauvegardeAutoHeure();
		int minute = pref.getSauvegardeAutoMinute();

		Calendar calendar = Calendar.getInstance();
		setProchaineHeure(calendar, heure, minute);
		return calendar;
	}
	else
		return null;
}

/***
 * Calcule le CALENDAR reprensentant l'heure de la prochaine sauvegarde
 *
 * @param calendar
 * @param heure
 * @param minute
 */
static public void setProchaineHeure(Calendar calendar, int heure, int minute)
{
	calendar.set(Calendar.HOUR_OF_DAY, heure);
	calendar.set(Calendar.MINUTE, minute);
	calendar.set(Calendar.SECOND, 0);

	while (calendar.compareTo(Calendar.getInstance()) < 0)
		calendar.add(Calendar.DAY_OF_YEAR, 1);
}

/***
 * Plannifie la prochaine alarme Supprime la precedente si elle existe
 *
 * @param calendar
 */
static public void setAlarm(Context context, Calendar calendar)
{
	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	Intent intent = new Intent(context, AlarmReceiver.class);
	intent.setAction(COMMANDE_SAVE_ALARM);

	// Supprimer l'ancienne alarme
	PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, 0, intent, 0);
	alarmManager.cancel(pendingIntentCancel);

	if (calendar != null)
	{
		Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Set alarme " + calendar.get(Calendar.YEAR) + '/' + (calendar.get(Calendar.MONTH) + 1) + '/' + calendar.get(Calendar.DAY_OF_MONTH) //$NON-NLS-1$
				+ ' ' + calendar.get(Calendar.HOUR_OF_DAY) + ':' + calendar.get(Calendar.MINUTE) + ':' + calendar.get(Calendar.SECOND));
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}
}

/***
 * Changement de l'heure de la sauvegarde automatique
 *
 * @param
 */
static public void plannifieSauvegarde(Context context)
{
	Calendar calendar = getProchaineSauvegarde(context);
	setAlarm(context, calendar);

	if (calendar == null)
	{
		// Pas de sauvegarde automatique
		Toast.makeText(context, AsyncSauvegarde.formatResourceString(context, R.string.sauvegarde_auto_desactivee), Toast.LENGTH_SHORT).show();
	}
	else
	{
		Toast.makeText(context, getTextProchaineSauvegarde(context, calendar), Toast.LENGTH_SHORT).show();
	}
}

/***
 * Retrouve le texte de l'heure et date de la prochaine sauvegarde
 * @param calendar
 * @return
 */
@NonNull
static public String getTextProchaineSauvegarde(Context context, Calendar calendar)
{
	if (calendar == null)
		calendar = getProchaineSauvegarde(context);
	return AsyncSauvegarde.formatResourceString(context, R.string.sauvegarde_auto_programmee,
			AsyncSauvegarde.getLocalizedTimeAndDate(context, calendar));
}


}
