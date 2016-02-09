package lpi.sauvegardesamba.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Gestionnaire des preferences de l'application
 */
public class Preferences
{
public static final String PREFERENCES = "com.lpi.sauvegarde.preferences"; //$NON-NLS-1$
public static final String PREF_NON_INITIALISEE = "@@non_initialise@@";
private static final String PREF_SAUVEGARDE_EN_COURS = "SauvegardeEnCours"; //$NON-NLS-1$
private static final String PREF_SAUVEGARDE_AUTO_HEURE = "HeureAutomatique.Heure"; //$NON-NLS-1$
private static final String PREF_SAUVEGARDE_AUTO_MINUTE = "HeureAutomatique.Minute"; //$NON-NLS-1$
private static final String PREF_SAUVEGARDE_AUTO_ACTIVEE = "SauvegardeAutomatique"; //$NON-NLS-1$
private static final String PREF_RAPPORT_DERNIERE_SAUVEGARDE = "Rapport"; //$NON-NLS-1$
private static final String PREF_REPERTOIRE_SAUVEGARDE = "Repertoire.Sauvegarde"; //$NON-NLS-1$
private static final String PREF_REPERTOIRE_CONTACTS = "Repertoire.Contacts";
private static final String PREF_REPERTOIRE_APPELS = "Repertoire.Appels";
private static final String PREF_REPERTOIRE_MESSAGES = "Repertoire.Messages";
private static final String PREF_REPERTOIRE_PHOTOS = "Repertoire.Photos";
private static final String PREF_REPERTOIRE_VIDEOS = "Repertoire.Videos";
private static final String PREF_DETECTE_CONNEXION_WIFI = "Detection.WIFI";
private static final String PREF_REGROUPER_APPELS   = "Regrouper.Appels";
private static final String PREF_REGROUPER_MESSAGES = "Regrouper.Messages";
private static final String PREF_REGROUPER_PHOTOS   = "Regrouper.Photos";
private static final String PREF_REGROUPER_VIDEOS   = "Regrouper.Videos";
private static final String PREF_THEME = "Theme";
private SharedPreferences _preferences;
private SharedPreferences.Editor _editor;

public Preferences(Context c)
{
	_preferences = c.getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
	_editor = null;
}

@Override
public void finalize() throws Throwable
{
	if (_editor != null)
	{
		_editor.apply();
	}

	super.finalize();
}

public void save()
{
	if (_editor != null)
		_editor.apply();
}

public void putLong(String name, long v)
{
	if (_editor == null)
		_editor = _preferences.edit();

	_editor.putLong(name, v);
}


public void putString(String name, String s)
{
	if (_editor == null)
		_editor = _preferences.edit();

	_editor.putString(name, s);
}

public void putInt(String name, int i)
{
	if (_editor == null)
		_editor = _preferences.edit();

	_editor.putInt(name, i);
}

public void putBool(String name, boolean b)
{
	if (_editor == null)
		_editor = _preferences.edit();

	_editor.putBoolean(name, b);
}

public boolean getSauvegardeEnCours()
{
	return _preferences.getBoolean(PREF_SAUVEGARDE_EN_COURS, false);
}

public void setSauvegardeEnCours(boolean b)
{
	putBool(PREF_SAUVEGARDE_EN_COURS, b);
}

public String getRapport()
{
	return _preferences.getString(PREF_RAPPORT_DERNIERE_SAUVEGARDE, PREF_NON_INITIALISEE);
}

public void setRapport(String s)
{
	putString(PREF_RAPPORT_DERNIERE_SAUVEGARDE, s);
}

public int getSauvegardeAutoHeure()
{
	return _preferences.getInt(PREF_SAUVEGARDE_AUTO_HEURE, 0);
}

public void setPrefSauvegardeAutoHeure(int p)
{
	putInt(PREF_SAUVEGARDE_AUTO_HEURE, p);
}

public int getSauvegardeAutoMinute()
{
	return _preferences.getInt(PREF_SAUVEGARDE_AUTO_MINUTE, 0);
}

public void setPrefSauvegardeAutoMinute(int p)
{
	putInt(PREF_SAUVEGARDE_AUTO_MINUTE, p);
}

public boolean getSauvegarderAuto()
{
	return _preferences.getBoolean(PREF_SAUVEGARDE_AUTO_ACTIVEE, false);
}

public void setSauvegardeAuto(boolean b)
{
	putBool(PREF_SAUVEGARDE_AUTO_ACTIVEE, b);
}

public String getPrefRepertoireSauvegarde()
{
	return _preferences.getString(PREF_REPERTOIRE_SAUVEGARDE, "SauvegardeSAMBA");
}

public void setPrefRepertoireSauvegarde(String rep) { putString(PREF_REPERTOIRE_SAUVEGARDE, rep); }

public String getPrefRepertoireContacts() { return _preferences.getString(PREF_REPERTOIRE_CONTACTS, "Contacts"); }

public void setPrefRepertoireContacts(String rep)
{
	putString(PREF_REPERTOIRE_CONTACTS, rep);
}

public String getPrefRepertoireAppels() { return _preferences.getString(PREF_REPERTOIRE_APPELS, "Appels"); }

public void setPrefRepertoireAppels(String rep)
{
	putString(PREF_REPERTOIRE_APPELS, rep);
}

public String getPrefRepertoireMessages() { return _preferences.getString(PREF_REPERTOIRE_MESSAGES, "Messages"); }

public void setPrefRepertoireMessages(String rep)
{
	putString(PREF_REPERTOIRE_MESSAGES, rep);
}

public String getPrefRepertoirePhotos() { return _preferences.getString(PREF_REPERTOIRE_PHOTOS, "Photos"); }

public void setPrefRepertoirePhotos(String rep)
{
	putString(PREF_REPERTOIRE_PHOTOS, rep);
}

public String getPrefRepertoireVideos()
{
	return _preferences.getString(PREF_REPERTOIRE_VIDEOS, "Videos");
}

public void setPrefRepertoireVideos(String rep)
{
	putString(PREF_REPERTOIRE_VIDEOS, rep);
}

public boolean getRegrouperAppels() { return _preferences.getBoolean(PREF_REGROUPER_APPELS, true) ;}
public void setPrefRegrouperAppels(boolean regrouper ){ putBool(PREF_REGROUPER_APPELS, regrouper);}

public boolean getRegrouperMessages() { return _preferences.getBoolean(PREF_REGROUPER_MESSAGES, true) ;}
public void setPrefRegrouperMessages(boolean regrouper ){ putBool(PREF_REGROUPER_MESSAGES, regrouper);}

public boolean getRegrouperPhotos() { return _preferences.getBoolean(PREF_REGROUPER_PHOTOS, true) ;}
public void setPrefRegrouperPhotos(boolean regrouper ){ putBool(PREF_REGROUPER_PHOTOS, regrouper);}

public boolean getRegrouperVideos() { return _preferences.getBoolean(PREF_REGROUPER_VIDEOS, true) ;}
public void setPrefRegrouperVideos(boolean regrouper ){ putBool(PREF_REGROUPER_VIDEOS, regrouper);}


public boolean getDetectionWIFI()
{
	return _preferences.getBoolean(PREF_DETECTE_CONNEXION_WIFI, true);
}

public void setDetectionWIFI(boolean regrouper)
{
	putBool(PREF_DETECTE_CONNEXION_WIFI, regrouper);
}

public int getTheme()
{
	return _preferences.getInt(PREF_THEME, 0);
}

public void setTheme(int p)
{
	putInt(PREF_THEME, p);
}
}
