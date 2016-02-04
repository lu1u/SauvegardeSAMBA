package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import lpi.sauvegardesamba.sauvegarde.SauvegardeReturnCode;
import lpi.sauvegardesamba.utils.Report;

/**
 * Created by lucien on 29/01/2016.
 */
public abstract class SavedObject
{
public abstract SauvegardeReturnCode sauvegarde(SmbFile smbRoot, Report report, Context context, NtlmPasswordAuthentication authentification) throws Exception;
public static String Combine(String partage, String path)
{
	if (partage.endsWith("/"))
		return partage + path;
	else
		return partage + "/" + path;
}

public boolean quelqueChoseASauvegarder()
{
	return true;
}

public abstract String Nom(Context context);

final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

static
{
	Arrays.sort(illegalChars);
}


static public String getExtension(String absolutePath)
{
	return MimeTypeMap.getFileExtensionFromUrl(absolutePath);
}

public static String cleanFileName(String badFileName)
{
	StringBuilder cleanName = new StringBuilder();
	for (int i = 0; i < badFileName.length(); i++)
	{
		int c = (int) badFileName.charAt(i);
		if (Arrays.binarySearch(illegalChars, c) < 0)
		{
			cleanName.append((char) c);
		}
	}
	return cleanName.toString();
}

/**
 * Retrouve le nom du contact a partir de son numero
 *
 * @param a
 * @param numero
 * @return
 * @throws Exception
 */
public static String getContact(Context a, String numero)
{
	String res = numero;
	Cursor c = null;
	try
	{
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numero));
		c = a.getContentResolver().query(
				Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
						Uri.encode(numero)),
				new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
		if (c != null)
		{
			c.moveToFirst();
			res = c.getString(c.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
		}
	} catch (Exception e)
	{
		if (numero.startsWith("+33"))
		{
			numero = "0" + numero.substring(3);
			return getContact(a, numero);
		}
		else
			res = numero;
	} finally
	{
		if (c != null)
			c.close();
	}
	return res;

}


/***
 * @return
 */
protected SmbFile getDestFile(SmbFile smbRoot, String categorie, Report report, NtlmPasswordAuthentication authentication)
{
	String path = smbRoot.getCanonicalPath();

	if (categorie != null)
		path = Combine(path, categorie);

	try
	{
		SmbFile smbf = new SmbFile(path, authentication);
		if (!smbf.exists())
			smbf.mkdir();

		return smbf;
	} catch (Exception e)
	{
		report.Log("création du répertoire de sauvegarde " + path);
		report.Log(e);
		return smbRoot;
	}
}

public static String sqliteDateToString(Context context, long l)
{
	try
	{
		return android.text.format.DateFormat.getDateFormat(context).format(new Date(l));
	} catch (Exception e)
	{
		return l + " (format de date non reconnue)"; //$NON-NLS-1$
	}
}

public static String sqliteDateHourToString(Context context, long l)
{
	try
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		Date date = new Date(l);
		return dateFormat.format(date);
	} catch (Exception e)
	{
		return l + " (format de date non reconnue)"; //$NON-NLS-1$
	}
}

/***
 * Converti en texte une valeur representant une duree en secondes
 *
 * @param context
 * @param l
 * @return
 */
public static String sqliteDurationToString(Context context, long l)
{
	try
	{
		int secondes = (int) l % 60;
		l /= 60;
		int minutes = (int) l % 60;
		l /= 60;
		int heures = (int) l;

		return String.format("%1$02dh %2$02dm %3$02ds", Integer.valueOf(heures),
				Integer.valueOf(minutes), Integer.valueOf(secondes));
	} catch (Exception e)
	{
		return l + " (format de date non reconnue)"; //$NON-NLS-1$
	}
}

public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

public static long copyLarge(InputStream input, OutputStream output) throws IOException
{
	byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	long count = 0;
	int n = 0;
	while (-1 != (n = input.read(buffer)))
	{
		output.write(buffer, 0, n);
		count += n;
	}
	return count;
}

@NonNull
public abstract String getCategorie();
}
