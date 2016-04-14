package lpi.sauvegardesamba.profils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Calendar;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import lpi.sauvegardesamba.database.DatabaseHelper;
import lpi.sauvegardesamba.database.ProfilsDatabase;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegardeManager;
import lpi.sauvegardesamba.sauvegarde.SauvegardeReturnCode;
import lpi.sauvegardesamba.sauvegarde.SavedObject.AppelsFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.ContactsFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.MessagesFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.PhotosFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.SavedObject;
import lpi.sauvegardesamba.sauvegarde.SavedObject.SavedObjectFactory;
import lpi.sauvegardesamba.sauvegarde.SavedObject.VideosFactory;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Report;

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
public int IntegrationSauvegardeAuto = ProfilsDatabase.S_AUTO_WIFI;
public boolean Appels = true;
static final public ProfilFieldInverser inverseAppels = new ProfilFieldInverser()
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
public boolean Contacts = true;
static final public ProfilFieldInverser inverseContacts = new ProfilFieldInverser()
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
public boolean Messages = true;
static final public ProfilFieldInverser inverseMessages = new ProfilFieldInverser()
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
public boolean Photos = true;
static final public ProfilFieldInverser inversePhotos = new ProfilFieldInverser()
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
public boolean Videos = true;
static final public ProfilFieldInverser inverseVideos = new ProfilFieldInverser()
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
public int DerniereSauvegarde;

public Profil()
{

}

public Profil(int id, String nom, int integrationsauvegardeauto, String utilisateur, String motdepasse, String partage, boolean appels, boolean contacts, boolean messages, boolean photos, boolean videos, int derniereSauvegarde)
{
	Id = id;
	Nom = nom;
	Utilisateur = utilisateur;
	MotDePasse = motdepasse;
	Partage = partage;
	Contacts = contacts;
	Appels = appels;
	Messages = messages;
	Photos = photos;
	Videos = videos;
	IntegrationSauvegardeAuto = integrationsauvegardeauto;
	DerniereSauvegarde = derniereSauvegarde;
}

public Profil(Cursor cursor)
{
	if (cursor != null)
	{
		Id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
		Nom = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOM));
		Utilisateur = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_UTILISATEUR));
		MotDePasse = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MOTDEPASSE));
		Partage = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PARTAGE));
		IntegrationSauvegardeAuto = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_INTEGRATION_SAUVEGARDE_AUTO));
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
	MotDePasse = bundle.getString(DatabaseHelper.COLUMN_MOTDEPASSE, MotDePasse);
	Partage = bundle.getString(DatabaseHelper.COLUMN_PARTAGE, Partage);
	IntegrationSauvegardeAuto = bundle.getInt(DatabaseHelper.COLUMN_INTEGRATION_SAUVEGARDE_AUTO, IntegrationSauvegardeAuto);
	Contacts = bundle.getBoolean(DatabaseHelper.COLUMN_CONTACTS, Contacts);
	Appels = bundle.getBoolean(DatabaseHelper.COLUMN_APPELS, Appels);
	Messages = bundle.getBoolean(DatabaseHelper.COLUMN_MESSAGES, Messages);
	Photos = bundle.getBoolean(DatabaseHelper.COLUMN_PHOTOS, Photos);
	Videos = bundle.getBoolean(DatabaseHelper.COLUMN_VIDEOS, Videos);
	DerniereSauvegarde = bundle.getInt(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE, DerniereSauvegarde);
}

/***
 * Retourne true si la WIFI est connectee
 *
 * @return true si la WIFI est connectee
 */
public static boolean IsWifiConnected(Context context)
{
	ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	return mWifi != null && mWifi.isConnected();

}

public void Copie(Profil p)
{
	Id = p.Id;
	Nom = p.Nom;
	Utilisateur = p.Utilisateur;
	MotDePasse = p.MotDePasse;
	Partage = p.Partage;
	Contacts = p.Contacts;
	Appels = p.Appels;
	Messages = p.Messages;
	Photos = p.Photos;
	Videos = p.Videos;
	IntegrationSauvegardeAuto = p.IntegrationSauvegardeAuto;
	DerniereSauvegarde = p.DerniereSauvegarde;
}

/***
 * Fait une sauvegarde en fonction des parametres de ce profil

 */
public void Sauvegarde(Context context, AsyncSauvegardeManager dlg)
{
	Report report = Report.getInstance(context);
	report.log(Report.NIVEAU.DEBUG, "Sauvegarde du profil: " + Nom);

	if (IntegrationSauvegardeAuto == ProfilsDatabase.S_AUTO_WIFI)
	{
		if (!IsWifiConnected(context))
		{
			report.historique("Profil '" + Nom + "': non connecté au WIFI, annulé");
			//dlg.notification(context.getString(R.string.non_connecte_wifi));
			return;
		}
	}

	if ((dlg._type == AsyncSauvegardeManager.TYPE_LAUNCHED.AUTO) && (IntegrationSauvegardeAuto == ProfilsDatabase.S_JAMAIS))
	{
		report.historique("Profil '" + Nom + "' non actif lors des sauvegardes automatiques");
		return;
	}

	report.historique("Démarrage du profil '" + Nom + "', chemin de la sauvegarde:" + Partage);

	report.log(Report.NIVEAU.DEBUG, "Chemin de la sauvegarde:" + Partage);
	String path = SavedObject.Combine("smb://" + Partage, Preferences.getInstance(context).getPrefRepertoireSauvegarde());
	NtlmPasswordAuthentication authentification = new NtlmPasswordAuthentication(null, Utilisateur, MotDePasse);

	if (partageNonAccessible(path, authentification, report))
		return;

	boolean erreur = false;
	if (!dlg.isCanceled())
	{
		if (Nok(SauvegardeObjets(new AppelsFactory(), context, path, authentification, dlg)))
			erreur = true;
	}
	if (!dlg.isCanceled())
	{
		if (Nok(SauvegardeObjets(new ContactsFactory(), context, path, authentification, dlg)))
			erreur = true;
	}
	if (!dlg.isCanceled())
	{
		if (Nok(SauvegardeObjets(new MessagesFactory(), context, path, authentification, dlg)))
			erreur = true;
	}
	if (!dlg.isCanceled())
	{
		if (Nok(SauvegardeObjets(new PhotosFactory(), context, path, authentification, dlg)))
			erreur = true;
	}
	if (!dlg.isCanceled())
	{
		if (Nok(SauvegardeObjets(new VideosFactory(), context, path, authentification, dlg)))
			erreur = true;
	}

	DerniereSauvegarde = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	ProfilsDatabase.getInstance(context).ChangeDate(Id, DerniereSauvegarde);
	if (dlg.isCanceled())
	{
		report.log(Report.NIVEAU.WARNING, "Sauvegarde annulée par l'utilisateur");
		report.historique("Profil " + Nom + " annulé par l'utilisateur");
	}
	else
		report.historique("Profil '" + Nom + (erreur ? "': Erreur détectée" : "': Sauvegarde terminée correctement"));
}

private boolean partageNonAccessible(String path, NtlmPasswordAuthentication authentification, Report report)
{
	try
	{
		SmbFile sFile = new SmbFile(path, authentification);
		if (!sFile.exists())
		{
			try
			{
				sFile.mkdir();
			} catch (SmbException e)
			{
				report.log(Report.NIVEAU.ERROR, "Impossible de creer le repertoire pour la sauvegarde:" + path);
				report.historique("Impossible de creer le repertoire pour la sauvegarde:" + path);
				return true;
			}
		}

		if (!sFile.canWrite())
		{
			report.log(Report.NIVEAU.ERROR, "Impossible d'ecrire dans le repertoire de sauvegarde:" + path);
			report.historique("Impossible d'ecrire dans le repertoire de sauvegarde:" + path);
			return true;
		}
	} catch (Exception e)
	{
		report.historique("Profil " + Nom + " erreur lors de la création du répertoire, non accessible?");
		report.log(Report.NIVEAU.ERROR, "Erreur lors de la creation du repertoire (repertoire non accessible?)" + path);
		report.log(Report.NIVEAU.ERROR, e);
		return true;
	}

	return false;
}

private boolean Nok(SauvegardeReturnCode sauvegardeReturnCode)
{
	return sauvegardeReturnCode != SauvegardeReturnCode.OK &&
			sauvegardeReturnCode != SauvegardeReturnCode.EXISTE_DEJA &&
			sauvegardeReturnCode != SauvegardeReturnCode.INACTIF;
}


private SauvegardeReturnCode SauvegardeObjets(SavedObjectFactory factory, Context context, String path, NtlmPasswordAuthentication authentification, AsyncSauvegardeManager dlg)
{
	SauvegardeReturnCode res = factory.sauvegarde(context, this, path, authentification, dlg);

	switch (res)
	{
		case OK:
		case EXISTE_DEJA:
		case INACTIF:
			break;

		case IMPOSSIBLE_SUPPRIMER_TEMP:
		case ERREUR_COPIE:
		case IMPOSSIBLE_CREER_REPERTOIRE:
		case ERREUR_CREATION_FICHIER:
			Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Erreur detectée");
			break;
	}
	return res;
}

public void toContentValues(@NonNull ContentValues content, boolean putId)
{
	if (putId)
		content.put(DatabaseHelper.COLUMN_ID, Id);
	content.put(DatabaseHelper.COLUMN_NOM, Nom);
	content.put(DatabaseHelper.COLUMN_UTILISATEUR, Utilisateur);
	content.put(DatabaseHelper.COLUMN_MOTDEPASSE, MotDePasse);
	content.put(DatabaseHelper.COLUMN_PARTAGE, Partage);
	content.put(DatabaseHelper.COLUMN_INTEGRATION_SAUVEGARDE_AUTO, IntegrationSauvegardeAuto);
	content.put(DatabaseHelper.COLUMN_CONTACTS, Contacts ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_APPELS, Appels ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_MESSAGES, Messages ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_PHOTOS, Photos ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_VIDEOS, Videos ? 1 : 0);
	content.put(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE, DerniereSauvegarde);
}

public void toBundle(@NonNull Bundle bundle)
{
	bundle.putInt(DatabaseHelper.COLUMN_ID, Id);
	bundle.putString(DatabaseHelper.COLUMN_NOM, Nom);
	bundle.putString(DatabaseHelper.COLUMN_UTILISATEUR, Utilisateur);
	bundle.putString(DatabaseHelper.COLUMN_MOTDEPASSE, MotDePasse);
	bundle.putString(DatabaseHelper.COLUMN_PARTAGE, Partage);
	bundle.putInt(DatabaseHelper.COLUMN_INTEGRATION_SAUVEGARDE_AUTO, IntegrationSauvegardeAuto);
	bundle.putBoolean(DatabaseHelper.COLUMN_CONTACTS, Contacts);
	bundle.putBoolean(DatabaseHelper.COLUMN_APPELS, Appels);
	bundle.putBoolean(DatabaseHelper.COLUMN_MESSAGES, Messages);
	bundle.putBoolean(DatabaseHelper.COLUMN_PHOTOS, Photos);
	bundle.putBoolean(DatabaseHelper.COLUMN_VIDEOS, Videos);
	bundle.putInt(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE, DerniereSauvegarde);
}

public String getDerniereSauvegarde(@NonNull Context context)
{
	if (DerniereSauvegarde == 0)
		return "Jamais";

	// Attention, on a divise le nombre de millisecondes par 1000 (secondes) pour que ca rentre dans la base
	Calendar c = Calendar.getInstance();
	c.setTimeInMillis((long) DerniereSauvegarde * 1000L);
	return android.text.format.DateFormat.getDateFormat(context).format(c.getTime()) + ' '
			+ android.text.format.DateFormat.getTimeFormat(context).format(c.getTime());
}


}
