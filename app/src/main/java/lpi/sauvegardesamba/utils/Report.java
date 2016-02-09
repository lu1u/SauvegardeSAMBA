/**
 *
 */
package lpi.sauvegardesamba.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.Locale;

import lpi.sauvegardesamba.database.HistoriqueDatabase;
import lpi.sauvegardesamba.database.TracesDatabase;

/**
 * @author lucien
 */
@SuppressWarnings("nls")
public class Report
{
static HistoriqueDatabase _historiqueDatabase;
static TracesDatabase _tracesDatabase;
static private Context _context;

private Report(Context context)
{

}

static public void Init(Context context)
{
	_context = context;

	if (_historiqueDatabase == null)
		_historiqueDatabase = HistoriqueDatabase.getInstance(context);

	if (_tracesDatabase == null)
		_tracesDatabase = TracesDatabase.getInstance(context);
}

public static void Log(NIVEAU niv, String message)
{
	Calendar c = Calendar.getInstance();

	_tracesDatabase.Ajoute((int) (c.getTimeInMillis() / 1000L), toInt(niv), message);
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

public static String getLocalizedDate()
{
	return getLocalizedDate(System.currentTimeMillis());
}

static public void Log(NIVEAU niv, Exception e)
{
	Log(niv, e.getLocalizedMessage());
	for (int i = 0; i < e.getStackTrace().length && i < 5; i++)
		Log(niv, e.getStackTrace()[i].getClassName() + '/' + e.getStackTrace()[i].getMethodName() + ':' + e.getStackTrace()[i].getLineNumber());

}

static public void historique(String message)
{
	Calendar c = Calendar.getInstance();
	_historiqueDatabase.Ajoute((int) (c.getTimeInMillis() / 1000L), message);
}

public enum NIVEAU
{
	DEBUG,
	WARNING,
	ERROR
}


}
