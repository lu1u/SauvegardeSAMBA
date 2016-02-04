package lpi.sauvegardesamba.sauvegarde;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.profils.ProfilsDatabase;
import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.utils.Report;

import java.util.Calendar;

public class Sauvegarde
{
public static final String TAG = "Sauvegarde"; //$NON-NLS-1$
public final static String COMMANDE_SAVE_ALARM = "lpi.Sauvegarde.Alarme"; //$NON-NLS-1$
public final static String ACTION_INFOS_FROM_SAUVEGARDE = "lpi.Sauvegarde.InfoFromService";

//static final long DELAI_MIN = 30 * 1000; // Delai en millisecondes entre deux sauvegardes
public static final String EXTRA_TYPE = "Type";
public static final String EXTRA_SAUVEGARDE_EN_COURS = "SauvegardeEnCours";

public static final String EXTRA_PARAM_TYPE = "Param";
public static int TOUS_LES_PROFILS = ProfilsDatabase.INVALID_ID ;

Context _context;
ProgressDlg _dlg;
ProfilsDatabase _database;

enum TYPE_LAUNCHED
{
	MANUEL, AUTO
}

NotificationManager _notificationManager;
Notification _notification;
RemoteViews _remoteViews;
NotificationCompat.Builder _builder;

public Sauvegarde(Context context, ProgressDlg dlg)
{
	_context = context;
	_dlg = dlg;
}


/***
 * Lance la sauvegarde
 *
 * @param a
 * @param type
 * @param profilId: Id du profil a sauvegarder ou -1 pour sauvegarder tous les profils
 */
public synchronized void execute(Activity a, TYPE_LAUNCHED type, int profilId)
{
	Report report = new Report();
	try
	{
		Preferences pref = new Preferences(_context);
		//    if (pref.getSauvegardeEnCours())
		//      return;

		statusSauvegardeEnCours(report, pref, true);
		report.Log("Depart sauvegarde " + (type == TYPE_LAUNCHED.MANUEL ? "manuelle" : "automatique")); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$

		_database = ProfilsDatabase.getInstance(_context);
		if ( profilId == TOUS_LES_PROFILS)
		{
			// Sauvegarder tous les profils
			Cursor cursor = _database.getProfilsActifs();

			if (cursor == null)
			{
				report.Log("Impossible d'obtenir la liste des curseurs");
			}
			else
			{
				int noProfil = 0 ;
				while (cursor.moveToNext() && !_dlg.isCanceled())
				{
					noProfil++;
					Profil profil = new Profil(cursor);
					_dlg.setProfil(String.format( "Profil %s (%d/%d)", profil.Nom, noProfil, cursor.getCount()));
					profil.Sauvegarde(report, _context, _dlg);
				}
				cursor.close();
			}
		}
		else
		{
			Profil profil = _database.getProfil(profilId);
			_dlg.setProfil("Profil: " + profil.Nom);
			profil.Sauvegarde(report, _context, _dlg);
		}

		if (_dlg.isCanceled())
		{
			report._message = _context.getString(R.string.sauvegarde_annulee_par_utilisateur); //$NON-NLS-1$
			report.Log(_context.getString(R.string.sauvegarde_annulee_par_utilisateur)); //$NON-NLS-1$
		}
		else
		{
			report._message = ""; //$NON-NLS-1$
			report.Log(_context.getString(R.string.sauvegarde_terminee_correctement)); //$NON-NLS-1$
		}

		sendReport(pref, report);
		report.Save(_context);
		statusSauvegardeEnCours(report, pref, false);
		pref.Save();
	} catch (Exception e)
	{
		report.Log(e);
		report.Save(_context);
		MainActivity.SignaleErreur("Une erreur est survenue pendant la sauvegarde, vous pouvez consulter le rapport de l'application", e);
	}
}

/***
 * Mise a jour de l'interface pour dire que la sauvegarde est finie
 * <p/>
 * aparam report
 */
private void statusSauvegardeEnCours(Report report, Preferences pref, boolean enCours)
{
	if (!enCours)
	{
		_dlg.notification(_context.getString(_dlg.isCanceled() ? R.string.sauvegarde_annulee : R.string.sauvegarde_terminee));
	}

	pref.setSauvegardeEnCours(enCours);
	pref.Save();
	Intent intent = new Intent(ACTION_INFOS_FROM_SAUVEGARDE);
	intent.putExtra(EXTRA_TYPE, EXTRA_SAUVEGARDE_EN_COURS);
	intent.putExtra(EXTRA_SAUVEGARDE_EN_COURS, enCours);
	_context.sendBroadcast(intent);
}

/***
 * Affichage de message d'erreur
 * <p/>
 * aparam localizedMessage
 */
public static void Erreur(Context c, String localizedMessage)
{
	Log.e(TAG, localizedMessage);
	Toast.makeText(c, "ERREUR: " + localizedMessage, Toast.LENGTH_LONG).show(); //$NON-NLS-1$
}

/***
 * Envoi un mail de rapport a l'adresse du developpeur
 * <p/>
 * aparam report
 */
private void sendReport(Preferences pref, Report report)
{
/*
        boolean envoyer;
        switch (pref.getEnvoiRapport())
        {
            case Preferences.RAPPORT_ERREUR:
                envoyer = report._erreurDetectee;
                break;

            case Preferences.RAPPORT_TOUJOURS:
                envoyer = true;
                break;
            default:
                envoyer = false;
        }

        pref.setRapport(report._message);
        if (!envoyer)
            return;

        MailInfo mInfo = new MailInfo();
        mInfo.read(pref);

        Mail m = new Mail(mInfo);
        String[] toArr = {"lucien.pilloni@gmail.com"}; //$NON-NLS-1$
        m.setTo(toArr);
        report.FillMail(m);
        try
        {
            m.send();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        */
}


/***
 * Charge une chaine de caracteres depuis les ressources et ajoute eventuellement des arguments
 * <p/>
 * aparam resId
 * aparam args
 *
 * @return
 */
public String formatResourceString(int resId, Object... args)
{
	String format = _context.getResources().getString(resId);
	return String.format(format, args);
}

/***
 * Charge une chaine de caracteres depuis les ressources et ajoute eventuellement des arguments
 * <p/>
 * aparam resId
 * aparam args
 *
 * @return
 */
static public String formatResourceString(Context context, int resId, Object... args)
{
	String format = context.getResources().getString(resId);
	return String.format(format, args);
}

public String getLocalizedTimeAndDate(Calendar c)
{
	if (c == null)
		c = Calendar.getInstance();

	return android.text.format.DateFormat.getDateFormat(_context).format(c.getTime()) + ' '
			+ android.text.format.DateFormat.getTimeFormat(_context).format(c.getTime());
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
}
