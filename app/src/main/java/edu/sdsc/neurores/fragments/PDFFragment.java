package edu.sdsc.neurores.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import edu.sdsc.neurores.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class PDFFragment extends Fragment{
    public static final String KEY_FILE_NAME = "filename";
    public static final String KEY_TOKEN = "token";

    public static final String HANDBOOK_FILE_NAME = "ucsdreshandbook_2017_2018.pdf";
    public static final String CLINIC_SESSIONS_FILE_NAME = "Open_Rooms.pdf";

    PDFView pdfView;


    public PDFFragment() {
        // required empty constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pdf, container, false);
        setHasOptionsMenu(true);

        String fileName = HANDBOOK_FILE_NAME;
        if(getArguments() !=  null){
            fileName = getArguments().getString(KEY_FILE_NAME);
        }

        pdfView = (PDFView) v.findViewById(R.id.pdf_view);
        pdfView.fromAsset(fileName).load();

        alignLeftToolbarTitle();
        return v;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_wipe_thread);
        item.setEnabled(false);
        item.setVisible(false);
    }


    private void alignLeftToolbarTitle() {
        TextView toolbarTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
        Toolbar.LayoutParams params =  (Toolbar.LayoutParams)toolbarTitle.getLayoutParams();
        params.gravity = Gravity.START;
        toolbarTitle.setLayoutParams(params);
    }
}
