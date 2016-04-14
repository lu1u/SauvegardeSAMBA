package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
 * Objet representant une video stockee sur le telephone
 * Created by lucien on 30/01/2016.
 */
public class Video extends SavedObject
{
static private int _colonneAbsolutePath;
static private int _colonneDisplayName;
static private int _colonneDateModified;
static private int _colonneDateTaken;
static private int _colonneBucketName;
public String _absolutePath;
public String _displayName;
public String _bucketName;
public long _dateTaken;
public long _dateModified;

public Video(Cursor cursor, Context context)
{
	_absolutePath = cursor.getString(_colonneAbsolutePath);
	_displayName = cursor.getString(_colonneDisplayName);
	_dateModified = cursor.getLong(_colonneDateModified);
	_dateTaken = cursor.getLong(_colonneDateTaken);
	_bucketName = cursor.getString(_colonneBucketName);
}

@Nullable
public static Cursor getList(Context context)
{
	Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
			null,
			null,  //$NON-NLS-1$
			null,
			null);

	if (cursor != null)
		try
		{
			_colonneAbsolutePath = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			_colonneDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
			_colonneDateModified = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
			_colonneDateTaken = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN);
			_colonneBucketName = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
		} catch (IllegalArgumentException e)
		{
			return null;
		}
	return cursor;
}

/***
 * Calcule un nom de fichier pour enregistrer ce ic_contact
 */
public String getFileName()
{
	return cleanFileName(_displayName);
}


/**
 * Sauvegarde le ic_contact dans le fichier samba donne en parametre
 */
public SauvegardeReturnCode sauvegarde(@NonNull SmbFile smbRoot, @NonNull Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	String fileName = getFileName();
	SmbFile smbDest = new SmbFile(Combine(smbRoot.getCanonicalPath(), fileName), authentification);
	if (smbDest.exists())
	{
		// Deja sauvegarde, plus rien a faire
		return SauvegardeReturnCode.EXISTE_DEJA;
	}

	// Passer par un fichier temporaire pour le cas ou la copie serai interrompue
	String tempFileName = "temp" + fileName;
	SmbFile smbTempDest = new SmbFile(Combine(smbRoot.getCanonicalPath(), tempFileName), authentification);
	if (smbTempDest.exists())
	{
		try
		{
			smbTempDest.delete();
		} catch (SmbException e)
		{
			return SauvegardeReturnCode.IMPOSSIBLE_SUPPRIMER_TEMP;
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
		Report report = Report.getInstance(context);
		report.log(Report.NIVEAU.ERROR, "Erreur lors de la sauvegarde de la vidéo " + _displayName);
		report.log(Report.NIVEAU.ERROR, e);
		return SauvegardeReturnCode.ERREUR_COPIE;
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
@NonNull
public String Nom(@NonNull Context context)
{
	return _displayName;
}

@NonNull
public String getCategorie()
{
	return _bucketName;
}
}
