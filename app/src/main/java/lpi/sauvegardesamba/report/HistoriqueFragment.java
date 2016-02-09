package lpi.sauvegardesamba.report;

import android.content.Context;
import android.net.Uri;
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
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private static final String ARG_PARAM1 = "param1";
private static final String ARG_PARAM2 = "param2";

HistoriqueAdapter _adapter;

private OnFragmentInteractionListener mListener;

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

// TODO: Rename method, update argument and hook method into UI event
public void onButtonPressed(Uri uri)
{
	if (mListener != null)
	{
		mListener.onFragmentInteraction(uri);
	}
}

@Override
public void onAttach(Context context)
{
	super.onAttach(context);
	if (context instanceof OnFragmentInteractionListener)
	{
		mListener = (OnFragmentInteractionListener) context;
	}

}

@Override
public void onDetach()
{
	super.onDetach();
	mListener = null;
}

@Override
public void Vide()
{
	HistoriqueDatabase db = HistoriqueDatabase.getInstance(getActivity());
	db.Vide();
	_adapter.changeCursor(db.getCursor());
	MainActivity.MessageNotification(getView(), "Historique effacé");

}

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p/>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 */
public interface OnFragmentInteractionListener
{
	// TODO: Update argument type and name
	void onFragmentInteraction(Uri uri);
}
}
