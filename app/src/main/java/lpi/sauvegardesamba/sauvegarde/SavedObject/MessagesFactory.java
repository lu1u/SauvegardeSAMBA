package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
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
	Preferences pref = new Preferences(context);
	return pref.getRegrouperMessages();
}

@Override
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

		default:
			return "?!?" ;
	}
}

@Override
protected String getRepertoireObjets(Context context)
{
	Preferences pref = new Preferences(context);
	return pref.getPrefRepertoireMessages();
}

@Override
@Nullable
protected Cursor getList(Context context)
{
	return Message.getList(context);
}

@Override
protected SavedObject creerObjet(Cursor cursor, Context context)
{
	return new Message(cursor, context );
}
}
