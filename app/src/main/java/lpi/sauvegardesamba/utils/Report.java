/**
 * 
 */
package lpi.sauvegardesamba.utils;

import android.content.Context;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author lucien
 *
 */
@SuppressWarnings("nls")
public class Report
{
	public static final String REPORT_FILE = "report.txt" ;
	public String _message;
	public String SauvegardeContacts = "";
	public String SauvegardeSMS = "";
	public String SauvegardeMMS = "";
	public String SauvegardePhotos = "";
	public String SauvegardeVideos = "";
	public String SauvegardeCallLog = "";
	public boolean _erreurDetectee = false ;
	
	private List<String> log = new ArrayList<String>();

	@Override
	public String toString()
	{
		if (_message == null)
			return SauvegardeContacts + "\n" + SauvegardeCallLog + "\n" + SauvegardeSMS + "\n" + SauvegardeMMS + "\n"
					+ SauvegardePhotos + "\n" + SauvegardeVideos;
		else
			return _message;

	}

	public void Log(String s)
	{
		log.add(getLocalizedDate() + ":" + s);
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

	public void Log(Exception e)
	{
		_message = "Erreur lors de l'envoi d\'un mail, vérifiez les paramètres"; //$NON-NLS-1$

		Log(e.getLocalizedMessage());
		for (StackTraceElement s : e.getStackTrace())
			Log(s.getClassName() + '/' + s.getMethodName() + ':' + s.getLineNumber());

	}

	public void Save(Context context)
	{
		File dir = new File(context.getCacheDir(), REPORT_FILE);
		try
		{
			FileOutputStream fileout = new FileOutputStream(dir.getPath());
			OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
			for (String s : log)
			{
			outputWriter.write(s + "\n");
			}
			outputWriter.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String Load(Context ctx)
	{
		StringBuffer sb = new StringBuffer() ;
		File dir = new File(ctx.getCacheDir(), REPORT_FILE);
		try
		{
			Reader r = new InputStreamReader(new FileInputStream(dir), "UTF-8");
			int c ;
			while ((c = r.read()) != -1)
				sb.append((char) c);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return sb.toString();
	}
}
