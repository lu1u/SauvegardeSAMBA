package lpi.sauvegardesamba.report;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.R;
import lpi.sauvegardesamba.database.HistoriqueDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoriqueFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoriqueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoriqueFragment extends ReportFragment
{


HistoriqueAdapter _adapter;


public HistoriqueFragment()
{
	// Required empty public constructor
}

/**
 * Use this factory method to create a new instance of
 * this fragment using the provided parameters.
 *
 * @param param1 Parameter 1.
 * @param param2 Parameter 2.
 * @return A new instance of fragment HistoriqueFragment.
 */
// TODO: Rename and change types and number of parameters
public static HistoriqueFragment newInstance()
{
	return new HistoriqueFragment();
}


@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState)
{
	View v = inflater.inflate(R.layout.fragment_historique, container, false);

	ListView lv = (ListView) v.findViewById(R.id.listView);
	_adapter = new HistoriqueAdapter(getActivity(), HistoriqueDatabase.getInstance(getActivity()).getCursor());
	lv.setAdapter(_adapter);
	return v;
}

@Override
public void Vide()
{
	HistoriqueDatabase db = HistoriqueDatabase.getInstance(getActivity());
	db.Vide();
	_adapter.changeCursor(db.getCursor());
	MainActivity.MessageNotification(getView(), "Historique effacé");

}


}
