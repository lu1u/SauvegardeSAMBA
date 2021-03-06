package widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Calendar;

import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegarde;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegardeManager;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Report;
import lpi.sauvegardesamba.utils.Utils;

/**
 * Widget de SauvegardeSAMBA, permet de lancer et arreter les sauvegardes
 */
public class SauvegardeAppWidget extends AppWidgetProvider
{
public static final String WIDGET_ACTION = "lpi.SAMBAckup.widgetclic";

void updateAppWidget(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int appWidgetId)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget updateAppWidget id=" + appWidgetId);

	// Construct the RemoteViews object
	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_sauvegarde);

	// Intercepter les clics
	setClickListener(context, views);

	if (AsyncSauvegarde.enCours(context))
	{
		// Une sauvegarde est en cours
		Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: Sauvegarde en cours" + appWidgetId);
		views.setImageViewBitmap(R.id.imageView, Utils.getBitmap(context, R.mipmap.ic_stop));
		views.setViewVisibility(R.id.progressBarWidget, View.VISIBLE);
	}
	else
	{
		// Pas de sauvegarde en cours
		Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: Pas de sauvegarde en cours" + appWidgetId);
		views.setImageViewBitmap(R.id.imageView, Utils.getBitmap(context, R.mipmap.ic_play));
		views.setViewVisibility(R.id.progressBarWidget, View.GONE);
	}
	// Instruct the widget manager to update the widget
	appWidgetManager.updateAppWidget(appWidgetId, views);
}



/***
 * Mise en place du recepteur de clic
 *
 * @param context
 * @param views
 */
private void setClickListener(@NonNull Context context, @NonNull RemoteViews views)
{
	Intent intent = new Intent(context, this.getClass());
	intent.setAction(WIDGET_ACTION);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
}

@Override
public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: onUpdate");
	// There may be multiple widgets active, so update all of them
	for (int appWidgetId : appWidgetIds)
	{
		updateAppWidget(context, appWidgetManager, appWidgetId);
	}
}

@Override
public void onEnabled(@NonNull Context context)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: onEnabled");
	updateAll(context);
}


/**
 * @param context The Context in which the receiver is running.
 * @param intent  The Intent being received.
 */
@Override
public void onReceive(@NonNull Context context, @NonNull Intent intent)
{
	super.onReceive(context, intent);
	String action = intent.getAction();
	if (WIDGET_ACTION.equals(action))
		onWidgetClic(context, intent);
	else if (AsyncSauvegarde.ACTION_ASYNCSAVE.equals(action))
		onInfoFromAsyncSave(context, intent);
	else if ("android.intent.action.BOOT_COMPLETED".equals(action) //$NON-NLS-1$
			|| "android.intent.action.QUICKBOOT_POWERON".equals(action)) //$NON-NLS-1$
		onBoot(context, intent);
}

private void onBoot(@NonNull Context context, @NonNull Intent intent)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: onBoot");
	// Redemarrage, une sauvegarde ne peut donc pas etre en cours,
	// on l'enregistre pour le cas ou la machine aurait ete eteinte en plein milieu d'une
	// sauvegarde
	AsyncSauvegarde.enCours(context, false);
	updateAll(context);
}

/***
 * Reception d'information/notification envoyees par ASyncSauvegarde
 *
 * @param context
 * @param intent
 */
protected void onInfoFromAsyncSave(@NonNull Context context, @NonNull Intent intent)
{
	String commande = intent.getStringExtra(AsyncSauvegarde.COMMAND);
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: receive " + commande);
	if (AsyncSauvegarde.COMMAND_STARTED.equals(commande) || AsyncSauvegarde.COMMAND_FINISHED.equals(commande))
	{
		updateAll(context);
	}
}

protected void updateAll(@NonNull Context context)
{
	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	ComponentName thisAppWidget = new ComponentName(context.getPackageName(), this.getClass().getName());
	int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

	onUpdate(context, appWidgetManager, appWidgetIds);
}

/**
 * Called in response to the {@link AppWidgetManager#ACTION_APPWIDGET_RESTORED} broadcast
 * when instances of this AppWidget provider have been restored from backup.  If your
 * provider maintains any persistent data about its widget instances, override this method
 * to remap the old AppWidgetIds to the new values and update any other app state that may
 * be relevant.
 * <p/>
 * <p>This callback will be followed immediately by a call to {@link #onUpdate} so your
 * provider can immediately generate new RemoteViews suitable for its newly-restored set
 * of instances.
 * <p/>
 * {@more}
 *
 * @param context
 * @param oldWidgetIds
 * @param newWidgetIds
 */
@Override
public void onRestored(@NonNull Context context, int[] oldWidgetIds, int[] newWidgetIds)
{
	super.onRestored(context, oldWidgetIds, newWidgetIds);
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget onRestored");
	updateAll(context);
}

/***
 * Clic sur le widget:
 * - si aucune sauvegarde en cours: lancer la sauvegarde
 * - sinon: demander a la sauvegarde en cours de se terminer
 *
 * @param context
 * @param intent
 */
private void onWidgetClic(Context context, Intent intent)
{
	Report report = Report.getInstance(context);
	AsyncSauvegardeManager manager = AsyncSauvegardeManager.getInstance(context);
	if (AsyncSauvegarde.enCours(context))
	{
		// Annulation d'une sauvegarde en cours
		report.historique("Sauvegarde annulée par Widget");
		report.log(Report.NIVEAU.DEBUG, "Widget clic: annulation");
		Toast.makeText(context, "Annulation de la sauvegarde", Toast.LENGTH_SHORT).show();

		Preferences pref = Preferences.getInstance(context);
		int maintenant = (int) (Calendar.getInstance().getTimeInMillis() / 1000L);
		if (maintenant - pref.getDerniereTentativeAnnulation() > 5000)
			// la derniere tentative d'annulation est suffisament vieille, on tente une annulation en douceur
			manager.cancel();
		else
		{
			// Une tentative de a deja ete faite il n'y a pas longtemps, elle semble ne pas avoir marche
			// puisque l'utilisateur redemande une annulation, on force l'arret de la sauvegarde
			// Corrige aussi un bug (du lanceur android?) qui fait que le Widget est en mode 'sauvegarde en cours'
			// apres un reboot, alors qu'aucune sauvegarde n'est vraiment en route
			manager.forceCancel(context);
		}
		updateAll(context);
		pref.setDerniereTentativeAnnulation(maintenant);
	}
	else
	{
		// Lancement de la sauvegarde
		report.historique("Sauvegarde lancée par Widget");
		report.log(Report.NIVEAU.DEBUG, "Widget clic: démarrage");
		manager.startSauvegarde(AsyncSauvegarde.TOUS_LES_PROFILS, AsyncSauvegardeManager.TYPE_LAUNCHED.WIDGET);
		Toast.makeText(context, "Lancement de la sauvegarde", Toast.LENGTH_SHORT).show();
	}
	updateAll(context);
}

}

