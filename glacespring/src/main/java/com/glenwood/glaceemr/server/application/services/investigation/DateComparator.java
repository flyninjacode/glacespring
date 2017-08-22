package com.glenwood.glaceemr.server.application.services.investigation;
import java.util.Comparator;
public class DateComparator implements Comparator<LogsheetDateBean>{
	@Override
    public int compare(LogsheetDateBean bean1, LogsheetDateBean bean2) {

        int year1 = bean1.getYear();
        int year2 = bean2.getYear();

        int month1 = bean1.getMonth();
        int month2 = bean2.getMonth();
        
        int day1 = bean1.getDay();
        int day2 = bean2.getDay();
        
        if(year1 > year2)
        	return +1;
        else if(year1 < year2)
        	return -1;
        else{
        	if(month1 > month2)
        		return +1;
        	else if(month1 < month2)
        		return -1;
        	else{
        		if(day1 > day2)
        			return +1;
        		else if(day1 < day2)
        			return -1;
        		else
        			 return 0;
        	}
        }
    }
}
