package lpi.sauvegardesamba.sauvegarde;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.profils.Profil;

public class SauvegardeAuto extends AsyncTask<Void, Void, Void> implements SauvegardeListener
{
static final int NOTIFICATION_ID = 1;
    Context _context;
    NotificationManager _notificationManager;
    Notification _notification;
    NotificationCompat.Builder _builder;
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



    public void setProgress(String format, int step, int Max)
    {
        //	if(  step % 10 == 0)
        {
            Notification(String.format(format, Integer.valueOf(step), Integer.valueOf(Max)));
        }
    }



@Override
    protected Void doInBackground(Void... params)
    {
        /*Sauvegarde sauve = new Sauvegarde(_context, this);
        sauve.execute(null, Sauvegarde.TYPE_LAUNCHED.AUTO, Sauvegarde.TOUS_LES_PROFILS);*/
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

    public boolean isCanceled()
    {
        return false;
    }


    /* (non-Javadoc)
     * @see lpi.sauvegardesamba.Sauvegarde.ProgressDlg#notification(java.lang.String)
     */
    public void notification(String s)
    {
        Notification(s);
    }


@Override
public void onDepartSauvegarde()
{

}

@Override
public void onFinSauvegarde()
{

}

@Override
public void onProfil(Profil profil)
{

}

@Override
public void onProgress(String format, int step, int Max)
{

}
}
