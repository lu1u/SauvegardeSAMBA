/**
 *
 */
package lpi.sauvegardesamba.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;

import lpi.sauvegardesamba.database.DatabaseHelper;
import lpi.sauvegardesamba.database.HistoriqueDatabase;
import lpi.sauvegardesamba.database.TracesDatabase;

/**
 * @author lucien
 */
@SuppressWarnings("nls")
public class Report
{
private static final int MAX_BACKTRACE = 10;
private static Report INSTANCE = null;

final HistoriqueDatabase _historiqueDatabase;
final TracesDatabase _tracesDatabase;


private Report(Context context)
{
	_historiqueDatabase = HistoriqueDatabase.getInstance(context);
	_tracesDatabase = TracesDatabase.getInstance(context);

}

/**
 * Point d'accès pour l'instance unique du singleton
 *
 * @param context: le context habituel d'ANdroid, peut être null si l'objet a deja ete utilise
 */
public static synchronized Report getInstance(Context context)
{
	if (INSTANCE == null)
	{
		INSTANCE = new Report(context);
	}
	return INSTANCE;
}

public static int toInt(NIVEAU n)
{
	switch (n)
	{
		case DEBUG:
			return 0;
		case WARNING:
			return 1;
		case ERROR:
			return 2;
		default:
			return 0;
	}
}

public static NIVEAU toNIVEAU(int n)
{
	switch (n)
	{
		case 0:
			return NIVEAU.DEBUG;
		case 1:
			return NIVEAU.WARNING;
		case 2:
			return NIVEAU.ERROR;
		default:
			return NIVEAU.DEBUG;
	}
}

@SuppressWarnings("boxing")
public static String getLocalizedDate(long date)
{
	Calendar c = Calendar.getInstance();
	c.setTimeInMillis(date);

	return String.format(Locale.getDefault(), "%02d/%02d/%02d %02d:%02d:%02d",
			c.get(Calendar.DAY_OF_MONTH),
			(c.get(Calendar.MONTH) + 1), c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
			c.get(Calendar.SECOND)); // + ":" + c.get(Calendar.MILLISECOND) ;
}

@NonNull
public static String getLocalizedDate()
{
	return getLocalizedDate(System.currentTimeMillis());
}

public void log(NIVEAU niv, String message)
{
	_tracesDatabase.Ajoute(DatabaseHelper.CalendarToSQLiteDate(null), toInt(niv), message);
}

public void log(NIVEAU niv, Exception e)
{
	log(niv, e.getLocalizedMessage());
	for (int i = 0; i < e.getStackTrace().length && i < MAX_BACKTRACE; i++)
		log(niv, e.getStackTrace()[i].getClassName() + '/' + e.getStackTrace()[i].getMethodName() + ':' + e.getStackTrace()[i].getLineNumber());

}

public void historique(String message)
{
	_historiqueDatabase.Ajoute(DatabaseHelper.CalendarToSQLiteDate(null), message);
}

public enum NIVEAU
{
	DEBUG,
	WARNING,
	ERROR
}


}
