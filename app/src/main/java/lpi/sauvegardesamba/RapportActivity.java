package lpi.sauvegardesamba;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import lpi.sauvegardesamba.utils.Report;

public class RapportActivity extends AppCompatActivity
{

/**
 * Dispatch onResume() to fragments.  Note that for better inter-operation
 * with older versions of the platform, at the point of this call the
 * fragments attached to the activity are <em>not</em> resumed.  This means
 * that in some cases the previous state may still be saved, not allowing
 * fragment transactions that modify the state.  To correctly interact
 * with fragments in their proper state, you should instead override
 * {@link #onResumeFragments()}.
 */
@Override
protected void onResume()
{
	super.onResume();
	Report r = new Report();
	String s = r.Load(this);

	if (s != null)
		if (! s.isEmpty())
			((TextView)findViewById(R.id.textViewReport)).setText(s);
}

@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_rapport);
	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);

	setSupportActionBar(toolbar);
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setDisplayShowTitleEnabled(true);
}

}
