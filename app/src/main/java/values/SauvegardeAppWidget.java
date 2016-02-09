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

/**
 * Implementation of App Widget functionality.
 */
public class SauvegardeAppWidget extends AppWidgetProvider
{
public static final String WIDGET_ACTION = "lpi.SAMBAckup.widgetclic";
private Context _context;

static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                            int appWidgetId)
{
	// Construct the RemoteViews object
	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_sauvegarde);
	Intent intent = new Intent(context, SauvegardeAppWidget.class);
	intent.setAction(WIDGET_ACTION);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
	views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);

	if (AsyncSauvegarde.enCours(context))
	{
		views.setViewVisibility(R.id.progressBarWidget, View.VISIBLE);
		views.setViewVisibility(R.id.textViewProfil, View.GONE);
		views.setViewVisibility(R.id.textViewProgress, View.GONE);
	}
	else
	{
		views.setViewVisibility(R.id.progressBarWidget, View.GONE);
		views.setProgressBar(R.id.progressBarWidget, 1, 10, true);
	}
	// Instruct the widget manager to update the widget
	appWidgetManager.updateAppWidget(appWidgetId, views);
}

@Override
public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
{
	_context = context;
	// There may be multiple widgets active, so update all of them
	for (int appWidgetId : appWidgetIds)
	{
		updateAppWidget(context, appWidgetManager, appWidgetId);
	}
}

@Override
public void onEnabled(Context context)
{
	_context = context;
}


/**
 * @param context The Context in which the receiver is running.
 * @param intent  The Intent being received.
 */
@Override
public void onReceive(Context context, Intent intent)
{
	_context = context;
	super.onReceive(context, intent);
	String action = intent.getAction();
	if (WIDGET_ACTION.equals(action))
		onWidgetClic(context, intent);
	if (AsyncSauvegarde.ACTION_ASYNCSAVE.equals(action))
		onInfoFromService(context, intent);
}

private void onInfoFromService(Context context, Intent intent)
{
	_context = context;
	String commande = intent.getStringExtra(AsyncSauvegarde.COMMAND);
	updateAll();
	/*
	if ( AsyncSauvegarde.COMMAND_STARTED.equals(commande))
	{

	}
	else
	if ( AsyncSauvegarde.COMMAND_FINISHED.equals(commande))
	{

	}
	else
	if ( AsyncSauvegarde.COMMAND_PROFIL.equals(commande))
	{

	}
	else
	if ( AsyncSauvegarde.COMMAND_PROGRESS.equals(commande))
	{

	}
    */
}

private void updateAll()
{
	if (_context != null)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(_context);
		ComponentName thisAppWidget = new ComponentName(_context.getPackageName(), this.getClass().getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		onUpdate(_context, appWidgetManager, appWidgetIds);
	}
}

private void onWidgetClic(Context context, Intent intent)
{
	AsyncSauvegardeManager manager = AsyncSauvegardeManager.getInstance(context);
	if (AsyncSauvegarde.enCours(context))
	{
		manager.cancel();
		Toast.makeText(context, "Annulation de la sauvegarde", Toast.LENGTH_SHORT).show();
	}
	else
	{

		manager.startSauvegarde(AsyncSauvegarde.TOUS_LES_PROFILS, AsyncSauvegardeManager.TYPE_LAUNCHED.WIDGET);
		Toast.makeText(context, "Sauvegarde lancée", Toast.LENGTH_SHORT).show();
	}
}

}

