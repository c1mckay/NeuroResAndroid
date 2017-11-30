package edu.ucsd.neurores.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by tbpetersen on 3/16/2017.
 * This adapter is used to alter the data that is shown in SearchActivity. data holds all the
 * strings that are available for the user to click on at the start. Filtered data is
 * initialized to have the same strings as data. Whenever the user types in a string to search for,
 * filterData() is called and all strings that do not contain the searchedString are removed
 * from filteredData. (Everything in filtered view is shown to the user in a list view)
 */
public class ContactSearchAdapter extends ArrayAdapter<String> {

    ArrayList<String> data;
    ArrayList<String> filteredData;



    public ContactSearchAdapter(Context context, int resource,ArrayList<String> data){
        super(context, resource);
        Collections.sort(data, String.CASE_INSENSITIVE_ORDER);
        this.data = data;
        filteredData = new ArrayList<String>(data);
    }

    @Override
    public int getCount(){
        return filteredData.size();
    }

    @Override
    public String getItem(int position){
        return filteredData.get(position);
    }

    /**
     * Clears filtered data and fills it will all the strings from data that contain the
     * searchTerm.
     * @param searchTerm the text the user is searching for
     */
    public void filterData(String searchTerm){
        filteredData.clear();

        ArrayList<ArrayList<String>> allMatches = new ArrayList<ArrayList<String>>();

        ArrayList<String> bestMatches = new ArrayList<String>(); //First word starts with searchTerm
        ArrayList<String> goodMatches = new ArrayList<String>(); // Some word starts with searchTerm
        ArrayList<String> okMatches = new ArrayList<String>(); // Words right after parenthesis
        ArrayList<String> matches = new ArrayList<String>(); // Words contains search word

        allMatches.add(bestMatches);
        allMatches.add(goodMatches);
        allMatches.add(okMatches);
        allMatches.add(matches);

        searchTerm = searchTerm.toLowerCase();
        for(String s : data){
            if(s.toLowerCase().contains(searchTerm)){
                int startIndex = s.toLowerCase().indexOf(searchTerm);
                if( startIndex == 0){
                    bestMatches.add(s);
                }else if(!Character.isLetterOrDigit(s.charAt(startIndex - 1))){
                    if(s.charAt(startIndex - 1) == '('){
                        okMatches.add(s);
                    }else{
                        goodMatches.add(s);
                    }
                }else{
                    matches.add(s);
                }
            }
        }


        for(ArrayList<String> list : allMatches){
            Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
            for(String s : list){
                filteredData.add(s);
            }
        }
        notifyDataSetChanged();
    }


}
