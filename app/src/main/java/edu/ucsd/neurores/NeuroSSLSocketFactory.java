package edu.ucsd.neurores;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by tbpetersen on 7/25/2017.
 */

public class NeuroSSLSocketFactory {
    Context context;
    public NeuroSSLSocketFactory(Context context){
        this.context = context;
        if(this.context == null){
            Log.v("contextp", "Context is null when creating!");
        }
    }

    protected org.apache.http.conn.ssl.SSLSocketFactory createAdditionalCertsSSLSocketFactory() {
        try {
            final KeyStore ks = KeyStore.getInstance("BKS");

            // the bks file we generated above
            if(context == null){
                Log.v("contextp", "Context is null when creating cert thing!");
            }
            final InputStream in = context.getResources().openRawResource( R.raw.neurores);
            try {
                // don't forget to put the password used above in strings.xml/mystore_password
                ks.load(in, context.getString( R.string.bks_password ).toCharArray());
            } finally {
                in.close();
            }

            return new AdditionalKeyStoresSSLSocketFactory(ks);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }
}
