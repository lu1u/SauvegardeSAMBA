package lpi.sauvegardesamba.profils;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.database.ProfilsDatabase;

/**
 * Adapter pour afficher les profils
 */
public class ProfilsAdapter extends CursorAdapter
{
static public final String ACTION_LANCE_SAUVEGARDE = "lpi.sauvegardeSamba.LanceSauvegardeProfil";
static public final String PARAM_ID = "Id";
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

	/*if (cursor.getPosition() % 2 == 0)
		view.setBackgroundColor(ContextCompat.getColor(context, R.color.pair));
	else
		view.setBackgroundColor(ContextCompat.getColor(context, R.color.impair));
      */
	// Find fields to populate in inflated template
	Profil profil = new Profil(cursor);
	((TextView) view.findViewById(R.id.textViewNom)).setText(profil.Nom);
	((TextView) view.findViewById(R.id.textViewPartage)).setText(profil.Partage);
	((TextView) view.findViewById(R.id.textViewDerniereSauvegarde)).setText("Dernière sauvegarde: " + profil.getDerniereSauvegarde(context));


	setInteractionIntegration(view, R.id.imageViewWifi, profil, "Jamais", "Wifi seulement", "Toujours (attention aux frais de communication!)");
	setInteraction(view, R.id.imageContact, profil, Profil.inverseContacts, "Contacts activés pour ce profil", "Contacts désactivés pour ce profil");
	setInteraction(view, R.id.imageAppels, profil, Profil.inverseAppels, "Appels activés pour ce profil", "Appels désactivés pour ce profil");
	setInteraction(view, R.id.imageMessages, profil, Profil.inverseMessages, "Messages activés pour ce profil", "Messages désactivés pour ce profil");
	setInteraction(view, R.id.imagePhotos, profil, Profil.inversePhotos, "Photos activés pour ce profil", "Photos désactivés pour ce profil");
	setInteraction(view, R.id.imageVideos, profil, Profil.inverseVideos, "Vidéos activés pour ce profil", "Vidéos désactivés pour ce profil");
}

private void setInteractionIntegration(View view, int resId, Profil profil, final String jamais, final String wifi, final String toujours)
{
	final ImageView imageView = (ImageView) view.findViewById(resId);
	SetGraphicAttributeIntegration(imageView, profil.IntegrationSauvegardeAuto);
	imageView.setTag(profil.Id);
	imageView.setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int Id = ((Integer) v.getTag());
			ProfilsDatabase database = ProfilsDatabase.getInstance(_context);
			Profil p = database.getProfil(Id);
			String sToast = null;
			switch (p.IntegrationSauvegardeAuto)
			{
				case ProfilsDatabase.S_JAMAIS:
					p.IntegrationSauvegardeAuto = ProfilsDatabase.S_AUTO_WIFI;
					sToast = wifi;
					break;
				case ProfilsDatabase.S_AUTO_WIFI:
					p.IntegrationSauvegardeAuto = ProfilsDatabase.S_AUTO_TOUJOURS;
					sToast = toujours;
					break;
				case ProfilsDatabase.S_AUTO_TOUJOURS:
					p.IntegrationSauvegardeAuto = ProfilsDatabase.S_JAMAIS;
					sToast = jamais;
					break;
				default:
			}
			if (sToast != null)
				MainActivity.MessageNotification(v, sToast);
			database.ModifieProfil(p);
			SetGraphicAttributeIntegration(imageView, p.IntegrationSauvegardeAuto);
			ProfilsAdapter.this.changeCursor(database.getCursor());
		}
	});
}

private void SetGraphicAttributeIntegration(ImageView v, int fieldValue)
{
	int noIcone = R.drawable.ic_off;
	switch (fieldValue)
	{
		case ProfilsDatabase.S_JAMAIS:
			noIcone = R.drawable.ic_off;
			break;
		case ProfilsDatabase.S_AUTO_WIFI:
			noIcone = R.drawable.ic_wifi;
			break;
		case ProfilsDatabase.S_AUTO_TOUJOURS:
			noIcone = R.drawable.ic_toujours;
			break;
		default:
	}

	v.setImageDrawable(v.getResources().getDrawable(noIcone));
}

private void setInteraction(View view, int resId, Profil profil, final ProfilFieldInverser profilFieldInverser, final String siActif, final String siInactif)
{
	ImageView imageView = (ImageView) view.findViewById(resId);
	SetGraphicAttribute(imageView, profilFieldInverser.getFieldValue(profil));
	imageView.setTag(profil.Id);
	imageView.setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int Id = ((Integer) v.getTag());
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
	view.setAlpha(actif ? 0.95f : 0.15f);
}


@Nullable
public Profil get(int position)
{
	Cursor cursor = getCursor();

	if (cursor.moveToPosition(position))
		return new Profil(cursor);
	return null;
}

}