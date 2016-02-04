package lpi.sauvegardesamba.profils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import java.util.Calendar;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import lpi.sauvegardesamba.database.DatabaseHelper;
import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.sauvegarde.SavedObject.AppelsFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.ContactsFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.MessagesFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.PhotosFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.SavedObjectFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.VideosFactory;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Report;
import lpi.sauvegardesamba.sauvegarde.ProgressDlg;
import lpi.sauvegardesamba.sauvegarde.SavedObject.Appel;
import lpi.sauvegardesamba.sauvegarde.SavedObject.Contact;
import lpi.sauvegardesamba.sauvegarde.SavedObject.Message;
import lpi.sauvegardesamba.sauvegarde.SavedObject.Photo;
import lpi.sauvegardesamba.sauvegarde.SavedObject.SavedObject;
import lpi.sauvegardesamba.sauvegarde.SavedObject.Video;

/**
 * Created by lucien on 26/01/2016.
 */
public class Profil
{
public static final int INVALID_ID = -1;
public int Id = INVALID_ID;
public String Nom;
public String Utilisateur;
public String MotDePasse;
public String Partage;
public boolean SauvegardeManuelle = true;
public boolean Contacts = true;
public boolean Appels = true;
public boolean Messages = true;
public boolean Photos = true;
public boolean Videos = true;
public boolean Wifi = true;
public boolean Plannifie = true;
public int DerniereSauvegarde;

public Profil()
{
}

public Profil(int id, String nom, boolean wifi, boolean plannifie, String utilisateur, String motdepasse, String partage, boolean manuelle, boolean appels, boolean contacts, boolean messages, boolean photos, boolean videos, int derniereSauvegarde)
{
	Id = id;
	Nom = nom;
	Wifi = wifi;
	Plannifie = plannifie;
	Utilisateur = utilisateur;
	MotDePasse = motdepasse;
	Partage = partage;
	Contacts = contacts;
	Appels = appels;
	Messages = messages;
	Photos = photos;
	Videos = videos;
	SauvegardeManuelle = manuelle;
	DerniereSauvegarde = derniereSauvegarde;
}

public Profil(Cursor cursor)
{
	if (cursor != null)
	{
		Id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
		Wifi = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_WIFI)) != 0;
		Plannifie = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SAUVEGARDE_PLANNIFIEE)) != 0;
		Nom = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOM));
		Utilisateur = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_UTILISATEUR));
		MotDePasse = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MOTDEPASSE));
		Partage = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PARTAGE));
		SauvegardeManuelle = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SAUVEGARDE_MANUELLE)) != 0;
		Contacts = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS)) != 0;
		Appels = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_APPELS)) != 0;
		Messages = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGES)) != 0;
		Photos = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHOTOS)) != 0;
		Videos = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEOS)) != 0;
		DerniereSauvegarde = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE));
	}
}

public Profil(Bundle bundle)
{
	Id = bundle.getInt(DatabaseHelper.COLUMN_ID, Id);
	Nom = bundle.getString(DatabaseHelper.COLUMN_NOM, Nom);
	Utilisateur = bundle.getString(DatabaseHelper.COLUMN_UTILISATEUR, Utilisateur);
	Wifi = bundle.getBoolean(DatabaseHelper.COLUMN_WIFI, Wifi);
	Plannifie = bundle.getBoolean(DatabaseHelper.COLUMN_SAUVEGARDE_PLANNIFIEE, Plannifie);
	MotDePasse = bundle.getString(DatabaseHelper.COLUMN_MOTDEPASSE, MotDePasse);
	Partage = bundle.getString(DatabaseHelper.COLUMN_PARTAGE, Partage);
	SauvegardeManuelle = bundle.getBoolean(DatabaseHelper.COLUMN_SAUVEGARDE_MANUELLE, SauvegardeManuelle);
	Contacts = bundle.getBoolean(DatabaseHelper.COLUMN_CONTACTS, Contacts);
	Appels = bundle.getBoolean(DatabaseHelper.COLUMN_APPELS, Appels);
	Messages = bundle.getBoolean(DatabaseHelper.COLUMN_MESSAGES, Messages);
	Photos = bundle.getBoolean(DatabaseHelper.COLUMN_PHOTOS, Photos);
	Videos = bundle.getBoolean(DatabaseHelper.COLUMN_VIDEOS, Videos);
	DerniereSauvegarde = bundle.getInt(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE, DerniereSauvegarde);
}

public void Copie(Profil p)
{
	Id = p.Id;
	Nom = p.Nom;
	Wifi = p.Wifi;
	Plannifie = p.Plannifie;
	Utilisateur = p.Utilisateur;
	MotDePasse = p.MotDePasse;
	Partage = p.Partage;
	Contacts = p.Contacts;
	Appels = p.Appels;
	Messages = p.Messages;
	Photos = p.Photos;
	Videos = p.Videos;
	SauvegardeManuelle = p.SauvegardeManuelle;
	DerniereSauvegarde = p.DerniereSauvegarde;
}

/***
 * Fait une sauvegarde en fonction des parametres de ce profil
 *
 * @param report
 */
public void Sauvegarde(Report report, Context context, ProgressDlg dlg)
{
	if (Wifi)
	{
		if (!IsWifiConnected(context))
		{
			dlg.notification(context.getString(R.string.non_connecte_wifi));
			return;
		}
	}
	report.Log("Sauvegarde du profil " + Nom);
	report.Log("Chemin de la sauvegarde:" + Partage);
	Preferences pref = new Preferences(context) ;
	String path = SavedObject.Combine("smb://" + Partage, pref.getPrefRepertoireSauvegarde());
	NtlmPasswordAuthentication authentification = new NtlmPasswordAuthentication(null, Utilisateur, MotDePasse);

	try
	{
		SmbFile sFile = new SmbFile(path, authentification);
		if (!sFile.exists())
			sFile.mkdir();
	} catch (Exception e)
	{
		report.Log("Erreur lors de la creation du repertoire (repertoire non accessible?)" + path);
		report.Log(e);
		return;
	}

	if (!dlg.isCanceled())
		SauvegardeObjets(new AppelsFactory(), context, path, report, authentification, dlg);
	if (!dlg.isCanceled())
		SauvegardeObjets(new ContactsFactory(), context, path, report, authentification, dlg);
	if (!dlg.isCanceled())
		SauvegardeObjets(new MessagesFactory(), context, path, report, authentification, dlg);
	if (!dlg.isCanceled())
		SauvegardeObjets(new PhotosFactory(), context, path, report, authentification, dlg);
	if (!dlg.isCanceled())
		SauvegardeObjets(new VideosFactory(), context, path, report, authentification, dlg);

	DerniereSauvegarde = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	ProfilsDatabase.getInstance(context).ChangeDate(Id, DerniereSauvegarde);
}

private void SauvegardeObjets(SavedObjectFactory factory, Context context, String path, Report report, NtlmPasswordAuthentication authentification, ProgressDlg dlg)
{
	factory.sauvegarde(context, this, path, report, authentification, dlg);
}


public void toContentValues(ContentValues content, boolean putId)
{
	if (putId)
		content.put(DatabaseHelper.COLUMN_ID, Id);
	content.put(DatabaseHelper.COLUMN_NOM, Nom);
	content.put(DatabaseHelper.COLUMN_UTILISATEUR, Utilisateur);
	content.put(DatabaseHelper.COLUMN_WIFI, Wifi ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_SAUVEGARDE_PLANNIFIEE, Plannifie ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_MOTDEPASSE, MotDePasse);
	content.put(DatabaseHelper.COLUMN_PARTAGE, Partage);
	content.put(DatabaseHelper.COLUMN_SAUVEGARDE_MANUELLE, SauvegardeManuelle ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_CONTACTS, Contacts ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_APPELS, Appels ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_MESSAGES, Messages ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_PHOTOS, Photos ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_VIDEOS, Videos ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE, DerniereSauvegarde);
}

public void toBundle(Bundle bundle)
{
	bundle.putInt(DatabaseHelper.COLUMN_ID, Id);
	bundle.putString(DatabaseHelper.COLUMN_NOM, Nom);
	bundle.putString(DatabaseHelper.COLUMN_UTILISATEUR, Utilisateur);
	bundle.putBoolean(DatabaseHelper.COLUMN_WIFI, Wifi);
	bundle.putBoolean(DatabaseHelper.COLUMN_SAUVEGARDE_PLANNIFIEE, Plannifie);
	bundle.putString(DatabaseHelper.COLUMN_MOTDEPASSE, MotDePasse);
	bundle.putString(DatabaseHelper.COLUMN_PARTAGE, Partage);
	bundle.putBoolean(DatabaseHelper.COLUMN_SAUVEGARDE_MANUELLE, SauvegardeManuelle);
	bundle.putBoolean(DatabaseHelper.COLUMN_CONTACTS, Contacts);
	bundle.putBoolean(DatabaseHelper.COLUMN_APPELS, Appels);
	bundle.putBoolean(DatabaseHelper.COLUMN_MESSAGES, Messages);
	bundle.putBoolean(DatabaseHelper.COLUMN_PHOTOS, Photos);
	bundle.putBoolean(DatabaseHelper.COLUMN_VIDEOS, Videos);
	bundle.putLong(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE, DerniereSauvegarde);
}

/***
 * Retourne true si la WIFI est connectee
 *
 * @return true si la WIFI est connectee
 */
private boolean IsWifiConnected(Context context)
{
	ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	return mWifi != null && mWifi.isConnected();

}

public String getDerniereSauvegarde(Context context)
{
	if (DerniereSauvegarde == 0)
		return "Jamais";

	// Attention, on a divise le nombre de millisecondes par 1000 (secondes) pour que ca rentre dans la base
	Calendar c = Calendar.getInstance();
	c.setTimeInMillis((long) DerniereSauvegarde * 1000L);
	return android.text.format.DateFormat.getDateFormat(context).format(c.getTime()) + ' '
			+ android.text.format.DateFormat.getTimeFormat(context).format(c.getTime());
}

static public ProfilFieldInverser inverseContacts = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.Contacts = !profil.Contacts;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.Contacts;
	}
};

static public ProfilFieldInverser inverseAppels = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.Appels = !profil.Appels;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.Appels;
	}
};

static public ProfilFieldInverser inverseMessages = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.Messages = !profil.Messages;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.Messages;
	}
};

static public ProfilFieldInverser inversePhotos = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.Photos = !profil.Photos;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.Photos;
	}
};

static public ProfilFieldInverser inverseVideos = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.Videos = !profil.Videos;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.Videos;
	}
};

static public ProfilFieldInverser inverseManuelle = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.SauvegardeManuelle = !profil.SauvegardeManuelle;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.SauvegardeManuelle;
	}
};

static public ProfilFieldInverser inversePlannifiee = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.Plannifie = !profil.Plannifie;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.Plannifie;
	}
};

static public ProfilFieldInverser inverseWifi = new ProfilFieldInverser()
{
	@Override
	public void InverseField(Profil profil)
	{
		profil.Wifi = !profil.Wifi;
	}

	@Override
	public boolean getFieldValue(Profil profil)
	{
		return profil.Wifi;
	}
};


}
