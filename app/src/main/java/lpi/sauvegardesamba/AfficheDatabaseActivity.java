package lpi.sauvegardesamba;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import lpi.sauvegardesamba.database.DatabaseHelper;
import lpi.sauvegardesamba.database.HistoriqueDatabase;
import lpi.sauvegardesamba.report.HistoriqueAdapter;

public class AfficheDatabaseActivity extends AppCompatActivity
{
private String[] _tables;

@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_affiche_database);

	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setDisplayShowTitleEnabled(false);

	// Setup spinner
	getTableNames();
	Spinner spinner = (Spinner) findViewById(R.id.spinner);
	spinner.setAdapter(new MyAdapter(toolbar.getContext(), _tables));

	spinner.setOnItemSelectedListener(new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			// When the given dropdown item is selected, show its contents in the
			// container view.
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, PlaceholderFragment.newInstance(_tables[position]))
					.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{
		}
	});
}

private void getTableNames()
{
	DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
	SQLiteDatabase database = dbHelper.getReadableDatabase();

	Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
	_tables = new String[c.getCount()];
	int i = 0;
	while (c.moveToNext())
	{
		_tables[i] = c.getString(c.getColumnIndex("name"));
		i++;
	}
}

@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item)
{
	// Handle action bar item clicks here. The action bar will
	// automatically handle clicks on the Home/Up button, so long
	// as you specify a parent activity in AndroidManifest.xml.
	int id = item.getItemId();

	//noinspection SimplifiableIfStatement
	if (id == R.id.action_settings)
	{
		return true;
	}

	return super.onOptionsItemSelected(item);
}


private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter
{
	private final ThemedSpinnerAdapter.Helper mDropDownHelper;

	public MyAdapter(@NonNull Context context, @NonNull String[] objects)
	{
		super(context, android.R.layout.simple_list_item_1, objects);
		mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
	}

	@Override
	@NonNull
	public View getDropDownView(int position, @Nullable View convertView, ViewGroup parent)
	{
		View view;

		if (convertView == null)
		{
			// Inflate the drop down using the helper's LayoutInflater
			LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
			view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		else
		{
			view = convertView;
		}

		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setText(getItem(position));

		return view;
	}

	@Override
	public Theme getDropDownViewTheme()
	{
		return mDropDownHelper.getDropDownViewTheme();
	}

	@Override
	public void setDropDownViewTheme(Theme theme)
	{
		mDropDownHelper.setDropDownViewTheme(theme);
	}
}


/**
 * A placeholder fragment containing a simple view.
 */
public static class PlaceholderFragment extends Fragment
{
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_TABLE_NAME = "table_name";
	CursorAdapter _adapter;


	public PlaceholderFragment()
	{
	}

	/**
	 * Returns a new instance of this fragment for the given section
	 * number.
	 */
	@NonNull
	public static PlaceholderFragment newInstance(String tableName)
	{
		PlaceholderFragment fragment = new PlaceholderFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TABLE_NAME, tableName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		   /*View rootView = inflater.inflate(R.layout.fragment_affiche_database, container, false);

		DatabaseHelper dbHelper = new DatabaseHelper(getContext());
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		//Cursor cursor =  database.query(getArguments().getString(ARG_TABLE_NAME), null, null, null, null, null, null);
		Cursor cursor =  database.query("PROFILS", null, null, null, null, null, null);
		//WithIdCursorWrapper c = new WithIdCursorWrapper(  cursor, WithIdCursorWrapper.ID_FIRST) ;
		String[] cols = cursor.getColumnNames();

		int[]   views = new int[cols.length];
		for ( int i = 0; i < views.length;i++)
				views[i] = android.R.id.text1;
        //GenericDatabaseAdapter adapter = new GenericDatabaseAdapter(getContext(), cursor);
		//lv.setAdapter(adapter);
		             */
		View v = inflater.inflate(R.layout.fragment_historique, container, false);

		ListView lv = (ListView) v.findViewById(R.id.listView);
		_adapter = new HistoriqueAdapter(getActivity(), HistoriqueDatabase.getInstance(getActivity()).getCursor());
		lv.setAdapter(_adapter);
		return v;
	}
}
}
