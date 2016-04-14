package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.utils.Preferences;

/**
 * Created by lucien on 04/02/2016.
 */
public class MessagesFactory extends SavedObjectFactory
{
@Override
protected boolean objetsActifs(Profil profil)
{
	return profil.Messages;
}

@Override
protected boolean regrouperObjets(Context context)
{
	Preferences pref = Preferences.getInstance(context);
	return pref.getRegrouperMessages();
}

@Override
@NonNull
protected String getMessage(MESSAGES message, Object... arguments)
{
	switch( message )
	{
		case LOG_SAUVEGARDE:
			return "Sauvegarde des messages" ;
		case IMPOSSIBLE_CREER_REPERTOIRE :
			return String.format("Impossible de créer le répertoire des messages %s", arguments );

		case PROGRESS:
			return "Message %d/%d" ;

		case ERREUR_LORS_DE_LA_SAUVEGARDE:
			return String.format( "Erreur lors de la sauvegarde du message dans le répertoire %s", arguments) ;
		case INACTIF:
			return "Sauvegarde des messages non active" ;
		case SAUVEGARDES_SUR:
			return String.format("%d nouveaux messages sauvegardés sur %d existants", arguments);

		default:
			return "?!?" ;
	}
}

@Override
@NonNull
protected String getRepertoireObjets(@NonNull Context context)
{
	Preferences pref = Preferences.getInstance(context);
	return pref.getPrefRepertoireMessages();
}

@Override
@Nullable
protected Cursor getList(@NonNull Context context)
{
	return Message.getList(context);
}

@Override
@NonNull
protected SavedObject creerObjet(@NonNull Cursor cursor, @NonNull Context context)
{
	return new Message(cursor, context );
}
}
