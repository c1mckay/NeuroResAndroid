package edu.ucsd.neurores;

/**
 * Created by tbpetersen on 8/3/2017.
 */

public class UnauthorizedException extends Exception {
    UnauthorizedException(){

    }

    UnauthorizedException(String s ){
        super(s);
    }
}
