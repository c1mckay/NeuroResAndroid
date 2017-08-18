package edu.ucsd.neurores;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.github.barteksc.pdfviewer.PDFView;

public class PDFActivity extends AppCompatActivity {
    PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("PDF");
        }else{
            Log.v("taggy", "No action bar");
        }

        pdfView = (PDFView) findViewById(R.id.pdf_view);
        pdfView.fromAsset("ucsdreshandbook_2017_2018.pdf").load();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }
}
