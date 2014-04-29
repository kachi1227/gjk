package com.gjk.utils;

import com.gjk.Application;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class NumberUtil {

	public static class NumberComparator implements Comparator<Number> {

		@Override
		public int compare(Number lhs, Number rhs) {
			double diff = lhs.doubleValue() - rhs.doubleValue();
			return diff == 0 ? 0 :  isNegative(diff) ? -1 : 1;
		}
		
	}

	public static boolean isNumber( String input )  {  
		try  {  
			Integer.parseInt( input );  
			return true;  
		}  
		catch( Exception e)  {  
			return false;  
		}  
	}

	public static boolean isInteger(String input) {
		try  {  
			int parsedInteger = Integer.parseInt( input );  
			return String.valueOf(parsedInteger).equals(input);  
		}  
		catch( Exception e)  {  
			return false;  
		}
	}

	public static int log2(int n){
		if(n <= 0) throw new IllegalArgumentException();
		return 31 - Integer.numberOfLeadingZeros(n);
	}

	public static int round(double value) {
		return (int)Math.floor(value + .5);
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2)); 
	}

	public static int[] convertIntegerArrayToIntArray(List<Integer> list) {
		int[] newArray = new int[list.size()];
		Iterator<Integer> iterator = list.iterator();
		for(int i=0, size=list.size(); i < size; i++)
			newArray[i] = iterator.next(); //use iterator for cost effeciency. Some lists dont have constant time access such as linked list
		return newArray;

	}

	public static boolean isBetween(int extremeOne, int extremeTwo, int candidate) {
		int lowEnd = extremeOne <= extremeTwo ? extremeOne : extremeTwo;
		int highEnd = extremeOne > extremeTwo ? extremeOne : extremeTwo;
		return lowEnd <= candidate && candidate <= highEnd;
	}
	
	public static boolean isNegative(double number) {
		return number < 0;
	}
	
}
