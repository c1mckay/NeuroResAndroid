package edu.ucsd.neurores.network;

/**
 * Created by tbpetersen on 11/30/2017.
 */

public interface HTTPRequestCompleteListener {
    void onComplete(String s);

    void onError(int i);
}
