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
 * Classe de base pour les objets sauvegardes
 * Created by lucien on 29/01/2016.
 */
public abstract class SavedObject
{
public static final String BEGIN_DATA = "\n<<<DATA>>>";
public static final String ENDDATA = "\n<<<ENDDATA>>>";
public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

static
{
	Arrays.sort(illegalChars);
}

public static String Combine(String partage, String path)
{
	if (partage.endsWith("/"))
		return partage + path;
	else
		return partage + "/" + path;
}

static public String getExtension(@NonNull String absolutePath)
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

public static boolean fileNameOk(String fileName)
{
	for (int i = 0; i < fileName.length(); i++)
	{
		int c = (int) fileName.charAt(i);
		if (Arrays.binarySearch(illegalChars, c) >= 0)
			return false;
	}

	return true;
}

/**
 * Retrouve le nom du ic_contact a partir de son numero
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
		c = a.getContentResolver().query(uri,
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

public static String sqliteDateToString(@NonNull Context context, long l)
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

		return String.format("%1$02dh %2$02dm %3$02ds", heures, minutes, secondes);
	} catch (Exception e)
	{
		return l + " (format de date non reconnue)"; //$NON-NLS-1$
	}
}

public static void copyLarge(InputStream input, OutputStream output) throws IOException
{
	byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	//long count = 0;
	int n;
	while (-1 != (n = input.read(buffer)))
	{
		output.write(buffer, 0, n);
		//count += n;
	}
	//return count;
}

public abstract SauvegardeReturnCode sauvegarde(SmbFile smbRoot, Context context, NtlmPasswordAuthentication authentification) throws Exception;

public boolean quelqueChoseASauvegarder()
{
	return true;
}


@NonNull
public abstract String Nom(@NonNull Context context);

/***
 * @return
 */
@NonNull
protected SmbFile getDestFile(@NonNull SmbFile smbRoot, @NonNull String categorie, @NonNull Report report, NtlmPasswordAuthentication authentication)
{
	String path = smbRoot.getCanonicalPath();
	path = Combine(path, categorie);

	try
	{
		SmbFile smbf = new SmbFile(path, authentication);
		if (!smbf.exists())
			smbf.mkdir();

		return smbf;
	} catch (Exception e)
	{
		report.log(Report.NIVEAU.ERROR, "création du répertoire de sauvegarde " + path);
		report.log(Report.NIVEAU.ERROR, e);
		return smbRoot;
	}
}

@NonNull
public abstract String getCategorie();
}
