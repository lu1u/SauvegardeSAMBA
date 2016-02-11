package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.utils.Preferences;

/**
 * Factory pour les Contacts
 */
public class ContactsFactory extends SavedObjectFactory
{
@Override
protected boolean objetsActifs(Profil profil)
{
	return profil.Contacts;
}

@Override
protected boolean regrouperObjets(Context context)
{
	return false;
}

@Override
@NonNull
protected String getMessage(SavedObjectFactory.MESSAGES message, Object... arguments)
{
	switch( message )
	{
		case LOG_SAUVEGARDE:
			return "Sauvegarde des contacts" ;
		case IMPOSSIBLE_CREER_REPERTOIRE :
			return String.format("Impossible de créer le répertoire des contacts %s", arguments );

		case PROGRESS:
			return "Contact %d/%d" ;

		case ERREUR_LORS_DE_LA_SAUVEGARDE:
			return String.format("Erreur lors de la sauvegarde ic_contact dans le répertoire %s", arguments);
		case INACTIF:
			return "Sauvegarde des contacts non active" ;

		default:
			return "?!?" ;
	}
}

@Override
@NonNull
protected String getRepertoireObjets(Context context)
{
	Preferences pref = Preferences.getInstance(context);
	return pref.getPrefRepertoireContacts();
}

@Override
@Nullable
protected Cursor getList(Context context)
{
	return Contact.getList(context);
}

@Override
@NonNull
protected SavedObject creerObjet(Cursor cursor, Context context)
{
	return new Contact(cursor, context );
}
}

