package lpi.sauvegardesamba.profils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.R;

/**
 * Created by lucien on 27/01/2016.
 */
public class ProfilsAdapter extends CursorAdapter
{
static public final String ACTION_LANCE_SAUVEGARDE = "lpi.sauvegardeSamba.LanceSauvegardeProfil";
static public final String PARAM_ID = "Id";
public static final String PARAM_ITEM_VIEW = "View";
private static final int KEY_ID = 0;
private Context _context;



public ProfilsAdapter(Context context, Cursor cursor)
{
	super(context, cursor, 0);
	_context = context;
}

// The newView method is used to inflate a new view and return it,
// you don't bind any data to the view at this point.
@Override
public View newView(Context context, Cursor cursor, ViewGroup parent)
{
	return LayoutInflater.from(context).inflate(R.layout.element_list_profils, parent, false);
}

// The bindView method is used to bind all data to a given view
// such as setting the text on a TextView.
@Override
public void bindView(View view, final Context context, Cursor cursor)
{
	// Find fields to populate in inflated template
	Profil profil = new Profil(cursor);
	((TextView) view.findViewById(R.id.textViewNom)).setText(profil.Nom);
	((TextView) view.findViewById(R.id.textViewPartage)).setText(profil.Partage);
	((TextView) view.findViewById(R.id.textViewDerniereSauvegarde)).setText("Dernière sauvegarde: " + profil.getDerniereSauvegarde(context));

	//setInteraction(view, R.id.imageViewManuelle, profil, Profil.inverseManuelle, "Sauvegarde manuelle de ce profil activée", "Sauvegarde manuelle de ce profil désactivée");
	setInteraction(view, R.id.imageViewPlannifiee, profil, Profil.inversePlannifiee, "Sauvegarde plannifiée de ce profil activée",  "Sauvegarde plannifiée de ce profil désactivée");
	setInteraction(view, R.id.imageViewWifi, profil, Profil.inverseWifi, "Sauvegarde par Wifi uniquement", "Sauvegarde par données mobiles activée (ATTENTION aux frais de communication!)");

	setInteraction(view, R.id.imageContact, profil, Profil.inverseContacts, "Sauvegarde des contacts activée pour ce profil", "Sauvegarde des contacts désactivée pour ce profil");
	setInteraction(view, R.id.imageAppels, profil, Profil.inverseAppels, "Sauvegarde des appels activée pour ce profil", "Sauvegarde des appels désactivée pour ce profil");
	setInteraction(view, R.id.imageMessages, profil, Profil.inverseMessages, "Sauvegarde des messages activée pour ce profil", "Sauvegarde des messages désactivée pour ce profil");
	setInteraction(view, R.id.imagePhotos, profil, Profil.inversePhotos, "Sauvegarde des photos activée pour ce profil", "Sauvegarde des photos désactivée pour ce profil");
	setInteraction(view, R.id.imageVideos, profil, Profil.inverseVideos, "Sauvegarde des contacts vidéos pour ce profil", "Sauvegarde des vidéos désactivée pour ce profil");

	TextView tv = (TextView) view.findViewById(R.id.textViewGo);
	tv.setTag(Integer.valueOf(profil.Id));
	final View itemView = view;
	tv.setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int Id = ((Integer) v.getTag()).intValue();
			Intent intent = new Intent(ACTION_LANCE_SAUVEGARDE);
			intent.putExtra(PARAM_ID, Id);
			context.sendBroadcast(intent);
		}
	});
}

private void setInteraction(View view, int resId, Profil profil, final ProfilFieldInverser profilFieldInverser, final String siActif, final String siInactif)
{
	ImageView imageView = (ImageView) view.findViewById(resId);
	SetGraphicAttribute(imageView, profilFieldInverser.getFieldValue(profil));
	imageView.setTag(Integer.valueOf(profil.Id));
	imageView.setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int Id = ((Integer) v.getTag()).intValue();
			ProfilsDatabase database = ProfilsDatabase.getInstance(_context);
			Profil p = database.getProfil(Id);
			profilFieldInverser.InverseField(p);
			database.ModifieProfil(p);
			SetGraphicAttribute(v, profilFieldInverser.getFieldValue(p));
			MainActivity.MessageNotification(v, profilFieldInverser.getFieldValue(p) ? siActif : siInactif);

			ProfilsAdapter.this.changeCursor(database.getCursor());
		}
	});
}

private void SetGraphicAttribute(View view, boolean actif)
{
	view.setAlpha(actif ? 0.9f : 0.15f);
}


@Nullable
public Profil get(int position)
{
	Cursor cursor = getCursor();

	Profil profilDuCurseur = null;
	if (cursor.moveToPosition(position))
		return new Profil(cursor);
	return null;
}

}