package edu.ucsd.neurores;

/**
 * Created by tbpetersen on 8/3/2017.
 */

public class InvalidLoginTokenException extends Exception {
    InvalidLoginTokenException(){

    }

    InvalidLoginTokenException(String s ){
        super(s);
    }
}
