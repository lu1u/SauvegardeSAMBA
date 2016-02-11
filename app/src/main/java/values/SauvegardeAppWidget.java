package values;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegarde;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegardeManager;
import lpi.sauvegardesamba.utils.Report;
import lpi.sauvegardesamba.utils.Utils;

/**
 * Implementation of App Widget functionality.
 */
public class SauvegardeAppWidget extends AppWidgetProvider
{
public static final String WIDGET_ACTION = "lpi.SAMBAckup.widgetclic";

static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                            int appWidgetId)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget updateAppWidget id=" + appWidgetId);

	// Construct the RemoteViews object
	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_sauvegarde);

	// Intercepter les clics
	setClickListener(context, views);

	if (AsyncSauvegarde.enCours(context))
	{
		// Une sauvegarde est en cours
		views.setImageViewBitmap(R.id.imageView, Utils.getBitmap(context, R.mipmap.ic_stop));
		views.setViewVisibility(R.id.progressBarWidget, View.VISIBLE);
		//views.setViewVisibility(R.id.textViewProfil, View.GONE);
		//views.setViewVisibility(R.id.textViewProgress, View.GONE);
	}
	else
	{
		// Pas de sauvegarde en cours
		views.setImageViewBitmap(R.id.imageView, Utils.getBitmap(context, R.mipmap.ic_play));
		views.setViewVisibility(R.id.progressBarWidget, View.GONE);
		views.setProgressBar(R.id.progressBarWidget, 1, 10, true);
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
private static void setClickListener(Context context, RemoteViews views)
{
	Intent intent = new Intent(context, SauvegardeAppWidget.class);
	intent.setAction(WIDGET_ACTION);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
}

@Override
public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: onUpdate");
	// There may be multiple widgets active, so update all of them
	for (int appWidgetId : appWidgetIds)
	{
		updateAppWidget(context, appWidgetManager, appWidgetId);
	}
}

@Override
public void onEnabled(Context context)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: onEnabled");
	updateAll(context);
}


/**
 * @param context The Context in which the receiver is running.
 * @param intent  The Intent being received.
 */
@Override
public void onReceive(Context context, Intent intent)
{
	super.onReceive(context, intent);
	String action = intent.getAction();
	//Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget: onReceive " + action);
	if (WIDGET_ACTION.equals(action))
		onWidgetClic(context, intent);
	else if (AsyncSauvegarde.ACTION_ASYNCSAVE.equals(action))
		onInfoFromAsyncSave(context, intent);
	else if ("android.intent.action.BOOT_COMPLETED".equals(action) //$NON-NLS-1$
			|| "android.intent.action.QUICKBOOT_POWERON".equals(action)) //$NON-NLS-1$
		onBoot(context, intent);
}

private void onBoot(Context context, Intent intent)
{
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "onBoot");
	updateAll(context);
}

/***
 * Reception d'information/notification envoyees par ASyncSauvegarde
 *
 * @param context
 * @param intent
 */
private void onInfoFromAsyncSave(Context context, Intent intent)
{
	String commande = intent.getStringExtra(AsyncSauvegarde.COMMAND);
	if (AsyncSauvegarde.COMMAND_STARTED.equals(commande))
	{
		updateAll(context);

	}
	else if (AsyncSauvegarde.COMMAND_FINISHED.equals(commande))
	{
		updateAll(context);

	}
/*	else
	if ( AsyncSauvegarde.COMMAND_PROFIL.equals(commande))
	{

	}
	else
	if ( AsyncSauvegarde.COMMAND_PROGRESS.equals(commande))
	{

	}
    */
}

private void updateAll(Context context)
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
public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds)
{
	super.onRestored(context, oldWidgetIds, newWidgetIds);
	Report.getInstance(context).log(Report.NIVEAU.DEBUG, "Widget onRestored");
	updateAll(context);
}

private void onWidgetClic(Context context, Intent intent)
{
	Report report = Report.getInstance(context);
	AsyncSauvegardeManager manager = AsyncSauvegardeManager.getInstance(context);
	if (AsyncSauvegarde.enCours(context))
	{
		report.historique("Annulation de la sauvegarde par le widget");
		report.log(Report.NIVEAU.DEBUG, "Widget clic: annulation");
		manager.cancel();
		Toast.makeText(context, "Annulation de la sauvegarde", Toast.LENGTH_SHORT).show();
	}
	else
	{
		report.historique("Démarrage de la sauvegarde de tous les profils par le widget");
		report.log(Report.NIVEAU.DEBUG, "Widget clic: démarrage");
		manager.startSauvegarde(AsyncSauvegarde.TOUS_LES_PROFILS, AsyncSauvegardeManager.TYPE_LAUNCHED.WIDGET);
		Toast.makeText(context, "Lancement de la sauvegarde", Toast.LENGTH_SHORT).show();
	}
}

}

