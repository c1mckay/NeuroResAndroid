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
    PDFView pdfView;
    private static String PDF_FILE_NAME = "ucsdreshandbook_2017_2018.pdf";


    public PDFFragment() {
        // required empty constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pdf, container, false);
        setHasOptionsMenu(true);

        pdfView = (PDFView) v.findViewById(R.id.pdf_view);
        pdfView.fromAsset(PDF_FILE_NAME).load();

        alignLeftToolbarTitle();
        return v;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.v("taggy","hiding trash");
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
