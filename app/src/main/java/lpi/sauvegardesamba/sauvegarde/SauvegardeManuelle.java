package lpi.sauvegardesamba.sauvegarde;

import android.app.Activity;
import android.os.AsyncTask;

public class SauvegardeManuelle extends AsyncTask<Void, Integer, Void> implements ProgressDlg
{
private static final int NOTIFICATION = 0;
private static final int PROFIL = 1;
private static final int OBJETSUR = 2;
private static final int PARTAGE= 3;

private Activity _mainActivity;
private String _message;
private String _partage;
private String _profil ;
private String _unSur;
private int _current;
private int _max;
ProgressDlg _dlg;
int _profilId ;

public SauvegardeManuelle(Activity activity, ProgressDlg dlg, int profilId)
{
	_profilId = profilId ;
	_mainActivity = activity;
	_dlg = dlg;
}

@Override
protected void onPostExecute(Void result)
{
		/*if (dialog.isShowing())
		{
			dialog.dismiss();
		}*/
}

@Override
protected Void doInBackground(Void... params)
{
	Sauvegarde sauve = new Sauvegarde(_mainActivity, this);
	sauve.execute(_mainActivity, Sauvegarde.TYPE_LAUNCHED.MANUEL, _profilId);
	return null;
}

@Override
public void setProgress(String format, int step, int Max)
{
	try
	{
		_unSur = format;
		_current = step;
		_max = Max;
		publishProgress(OBJETSUR);
	} catch (Exception e)
	{
		e.printStackTrace();
	}
}

@Override
public void setProfil(String profil)
{
	try
	{
		_profil = profil;
		publishProgress(PROFIL);
	} catch (Exception e)
	{
		e.printStackTrace();
	}
}

@Override
public void setPartage(String partage)
{
	try
	{
		_partage = partage;
		publishProgress(PARTAGE);
	} catch (Exception e)
	{
		e.printStackTrace();
	}
}

@Override
public void notification(String s)
{
	try
	{
		_message = s;
		publishProgress(NOTIFICATION);
	} catch (Exception e)
	{
		e.printStackTrace();
	}
}

/* (non-Javadoc)
 * @see android.os.AsyncTask#onProgressUpdate(java.lang.Object[])
 */
@Override
protected void onProgressUpdate(Integer... values)
{
	int type = values[0].intValue();

	switch( type )
	{
		case NOTIFICATION:
			_dlg.notification(_message);
			break ;


		case PROFIL:
			_dlg.setProfil(_profil);
			break ;

		case OBJETSUR:
			_dlg.setProgress(_unSur, _current, _max);
			break ;

		case PARTAGE:
			_dlg.setPartage(_partage);
			break ;
	}

	super.onProgressUpdate(values);
}

@Override
public boolean isCanceled()
{
	return _dlg.isCanceled();
}
}