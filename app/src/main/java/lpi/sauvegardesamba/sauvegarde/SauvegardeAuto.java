package lpi.sauvegardesamba.sauvegarde;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.R;

public class SauvegardeAuto extends AsyncTask<Void, Void, Void> implements ProgressDlg
{
    Context _context;
    NotificationManager _notificationManager;
    Notification _notification;
    NotificationCompat.Builder _builder;
    static final int NOTIFICATION_ID = 1;
    String _message;


    public SauvegardeAuto(Context c)
    {
        _context = c;
    }

    @SuppressLint("NewApi")
    public void Notification(String message)
    {
        try
        {
            if (_message != null)
                message = _message + " " + message;

            if (_builder == null)
            {

                _builder = new NotificationCompat.Builder(_context).setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(formatResourceString(R.string.app_name)).setContentText(message);
                Intent resultIntent = new Intent(_context, MainActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(_context, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                _builder.setContentIntent(resultPendingIntent);
            }

           _builder.setContentText(message);

            if (_notificationManager == null)
                _notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);

            _notificationManager.notify(NOTIFICATION_ID, _builder.build());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    @Override
    public void setProgress(String format, int step, int Max)
    {
        //	if(  step % 10 == 0)
        {
            Notification(String.format(format, Integer.valueOf(step), Integer.valueOf(Max)));
        }
    }

@Override
public void setProfil(String profil)
{
    Notification(profil);
}

@Override
public void setPartage(String partage)
{

}


@Override
    protected Void doInBackground(Void... params)
    {
        Sauvegarde sauve = new Sauvegarde(_context, this);
        sauve.execute(null, Sauvegarde.TYPE_LAUNCHED.AUTO, Sauvegarde.TOUS_LES_PROFILS);
        return null;
    }


    /***
     * Charge une chaine de caracteres depuis les ressources et ajoute eventuellement des arguments
     *
     * @param resId
     * @param args
     * @return
     */
    public String formatResourceString(int resId, Object... args)
    {
        String format = _context.getResources().getString(resId);
        return String.format(format, args);
    }

    @Override
    public boolean isCanceled()
    {
        return false;
    }


    /* (non-Javadoc)
     * @see lpi.sauvegardesamba.Sauvegarde.ProgressDlg#notification(java.lang.String)
     */
    @Override
    public void notification(String s)
    {
        Notification(s);
    }


}
