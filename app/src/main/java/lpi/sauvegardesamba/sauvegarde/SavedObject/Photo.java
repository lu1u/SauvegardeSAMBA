package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import lpi.sauvegardesamba.sauvegarde.SauvegardeReturnCode;
import lpi.sauvegardesamba.utils.Report;

/**
 * Created by lucien on 30/01/2016.
 */
public class Photo extends SavedObject
{
static private int _colonneDisplayName;
static private int _colonneDescription;
static private int _colonneDateModified;
static private int _colonneDateTaken;
static private int _colonneDateAdded;
static private int _colonneAbsolutePath;
static private int _colonneBucketDisplayName;

public String _absolutePath;
public String _displayName;
public String _description;
public long _dateTaken;
public long _dateAdded;
public long _dateModified;
public String _bucketName;

private static String[] COLONNES =
		{
				MediaStore.Images.ImageColumns.DATA,
				MediaStore.Images.ImageColumns.DISPLAY_NAME,
				MediaStore.Images.ImageColumns.DESCRIPTION,
				MediaStore.Images.ImageColumns.DATE_MODIFIED,
				MediaStore.Images.ImageColumns.DATE_TAKEN,
				MediaStore.Images.ImageColumns.DATE_ADDED,
				MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
		};

public Photo(Cursor cursor, Context context)
{
	_absolutePath = cursor.getString(_colonneAbsolutePath);
	_displayName = cursor.getString(_colonneDisplayName);
	_description = cursor.getString(_colonneDescription);
	_dateModified = cursor.getLong(_colonneDateModified);
	_dateTaken = cursor.getLong(_colonneDateTaken);
	_dateAdded = cursor.getLong(_colonneDateAdded);
	_bucketName = cursor.getString(_colonneBucketDisplayName);
}

public static Cursor getList(Context context)
{
	Uri u = Uri.parse("content://sms");
	Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			COLONNES,
			null,
			null,
			null);

	_colonneAbsolutePath = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
	_colonneDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
	_colonneDescription = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DESCRIPTION);
	_colonneDateModified = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED);
	_colonneDateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
	_colonneDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_ADDED);
	_colonneBucketDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
	return cursor;
}

/***
 * Calcule un nom de fichier pour enregistrer ce contact
 *
 * @return
 */
public String getFileName()
{
	return cleanFileName(_displayName);
}


/**
 * Sauvegarde la photo
 *
 * @param smbRoot = chemin vers le repertoire ou sont sauvegardees les photos "smb://{partage}/{sauvegardeSamba}/Photos
 * @param report
 * @param context
 * @throws IOException
 */
public SauvegardeReturnCode sauvegarde(SmbFile smbRoot, Report report, Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	String fileName = getFileName()  ;
	SmbFile smbDest = new SmbFile(Combine(smbRoot.getCanonicalPath(),fileName), authentification);
	if (smbDest.exists())
	{
		// Deja sauvegarde, plus rien a faire
		return SauvegardeReturnCode.EXISTE_DEJA ;
	}

	// Passer par un fichier temporaire pour le cas ou la copie serai interrompue
	String tempFileName = "temp" + fileName ;
	SmbFile smbTempDest = new SmbFile(Combine(smbRoot.getCanonicalPath(),tempFileName), authentification);
	if ( smbTempDest.exists())
	{
		try
		{
			smbTempDest.delete();
		} catch (SmbException e)
		{
			return SauvegardeReturnCode.IMPOSSIBLE_SUPPRIMER_TEMP ;
		}
	}

	OutputStream ops = null;
	InputStream ips = null;
	try
	{
		ops = smbTempDest.getOutputStream();
		ips = new FileInputStream(_absolutePath);

		copyLarge(ips, ops);

		ops.close();

	} catch (Exception e)
	{
		report.Log("Erreur lors de la sauvegarde de la photo " + _displayName);
		report.Log(e);
		return SauvegardeReturnCode.ERREUR_COPIE ;
	} finally
	{
		if (ips != null)
			ips.close();

		if (ops != null)
			ops.close();
	}

	smbTempDest.renameTo(smbDest);
	smbDest.setCreateTime(_dateTaken);
	smbDest.setLastModified(_dateModified);
	return SauvegardeReturnCode.OK;
}


@Override
public String Nom(Context context)
{
	return _displayName;
}

public String getCategorie()
{
	return _bucketName;
}
}
