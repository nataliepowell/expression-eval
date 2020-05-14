package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
    public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	
    	// remove space and tab characters
    	expr=expr.replaceAll("\\s+","");
    	
    	StringTokenizer exprToken = new StringTokenizer(expr, delims, true);
    	ArrayList<String> exprArray = new ArrayList<String>();
    	while (exprToken.hasMoreTokens()) {
    		exprArray.add(exprToken.nextToken()); }
    	
    	for (int i=0; i<exprArray.size()-1; i++) {
    		String key = exprArray.get(i);
	   		if (Character.isAlphabetic(key.charAt(0)) && exprArray.get(i+1).equals("[") )
	   		 {
	   			Array arr = new Array(key);
	   			arrays.add(arr);
	   			}	 
	   		else if (Character.isAlphabetic(key.charAt(0)) && !exprArray.get(i+1).equals("[") ) {
	   			Variable var = new Variable(key);
	   			vars.add(var);
	   		}
    	}
    	
    	if (Character.isAlphabetic(exprArray.get(exprArray.size()-1).charAt(0)))
  		 {
  			Variable var = new Variable(exprArray.get(exprArray.size()-1));
  			vars.add(var);
  		}
    	
    	vars=removeVariableDuplicates(vars);
    	arrays=removeArrayDuplicates(arrays);	
    }
	
	/**
	 * This method removes the duplicate items in an ArrayList of objects type Array.
	 */
	private static ArrayList<Array> removeArrayDuplicates(ArrayList<Array> arrays)
	{
        for(int i = 0; i < arrays.size(); i++)
        {
            for(int j = i + 1; j < arrays.size(); j++)
            {
                if(arrays.get(i).equals(arrays.get(j))){
                    arrays.remove(j);
                    j--;
                }
            }
        }
        return arrays;
	}
	
	/**
	 * This method removes the duplicate items in an ArrayList of objects type Variable.
	 */
	private static ArrayList<Variable> removeVariableDuplicates(ArrayList<Variable> vars)
	{
        for(int i = 0; i < vars.size(); i++)
        {
            for(int j = i + 1; j < vars.size(); j++)
            {
                if(vars.get(i).equals(vars.get(j))){
                    vars.remove(j);
                    j--;
                }
            }
        }
        return vars;
	}

    public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }

    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    {
 
    	// remove space and tab characters
    	expr=expr.replaceAll("\\s+","");
    	
    	StringTokenizer exprToken = new StringTokenizer(expr, delims, true);
    	
    	// BASE CASE //
    	if (exprToken.countTokens()==1) {
    		float value = Float.parseFloat(exprToken.nextToken());
    		return value; }
    	
    	
    	// RECURSION //
    	else {
    		String newTerm="";
	    	ArrayList<String> exprArray = new ArrayList<String>();
	    	while (exprToken.hasMoreTokens()) {
	    		exprArray.add(exprToken.nextToken()); }
	    	
	    	if (exprArray.size()==2 && exprArray.get(0).equals("-") && Character.isDigit(exprArray.get(1).charAt(0)))
	    		return Float.parseFloat(arrayListToString(exprArray));

	    	for (int m=0; m<exprArray.size()-2; m++) {
	    		if (exprArray.get(m).equals("+") && exprArray.get(m+1).equals("-")) {
	    			exprArray.set(m, "-");
	    			exprArray.remove(m+1); }
	    		if (exprArray.get(m).equals("-") && exprArray.get(m+1).equals("-")) {
	    			exprArray.set(m, "+");
	    			exprArray.remove(m+1); }
	    		if ( ( exprArray.get(m).equals("/") || exprArray.get(m).equals("*") ||
	    				exprArray.get(m).equals("(") || exprArray.get(m).equals(")") || exprArray.get(m).equals("[")
	    				|| exprArray.get(m).equals("]") ) && exprArray.get(m+1).equals("-")) {
	    			exprArray.set(m+1, "-".concat(exprArray.get(m+2)));
	    			exprArray.remove(m+2);
	    		}
	    	}
	    	
	    	ArrayList<String> innermost = new ArrayList<String>();
	    	
	    	// IS ARRAY ELEMENT
	    	if (exprArray.lastIndexOf("[") > exprArray.lastIndexOf("(")) {
	    		List<String> subList = exprArray.subList(exprArray.lastIndexOf("[")-1, exprArray.size());
	    		int beginInnermost = exprArray.lastIndexOf("[")-1;
	    		
	    		
	    		for (int k=subList.indexOf("[")-1; k<=subList.indexOf("]"); k++)  
	    		{
	    		innermost.add(subList.get(k)); }
	    		int endInnermost = exprArray.lastIndexOf("[")+innermost.size()-1;
	    		newTerm = Double.toString(evaluateArrayAtIndex(innermost, vars, arrays));
	    		
	    		List<String> firstHalf = exprArray.subList(0, beginInnermost);
	    		List<String> secondHalf = exprArray.subList(endInnermost, exprArray.size());
	    		
	    		ArrayList<String> newExpr = new ArrayList<String>();
	    		newExpr.addAll(firstHalf);
	    		newExpr.add(newTerm);
	    		newExpr.addAll(secondHalf);

	    		return evaluate (arrayListToString(newExpr), vars, arrays); }
	    	
	    	// IS SIMPLE EXPRESSION
	    	else if (exprArray.lastIndexOf("(") > exprArray.lastIndexOf("[")) 
	    	{
	    		List<String> subList = exprArray.subList(exprArray.lastIndexOf("("), exprArray.size());
	    		int beginInnermost = exprArray.lastIndexOf("(");
	    		for (int j=subList.indexOf("("); j<=subList.indexOf(")"); j++) {
	    			innermost.add(subList.get(j)); }
	    		int endInnermost = exprArray.lastIndexOf("(")+innermost.size()-1;
	    		newTerm = simpleEvaluate(innermost, vars, arrays); 
	    		
	    		List<String> firstHalf = exprArray.subList(0, beginInnermost);
	    		List<String> secondHalf = exprArray.subList(endInnermost+1, exprArray.size());
	    		
	    		ArrayList<String> newExpr = new ArrayList<String>();
	    		newExpr.addAll(firstHalf);
	    		newExpr.add(newTerm);
	    		newExpr.addAll(secondHalf);

	    		return evaluate (arrayListToString(newExpr), vars, arrays); 
	    		
	    	}
	    	else {
	    		newTerm = simpleEvaluate(exprArray, vars, arrays); 
	    		return evaluate(newTerm, vars, arrays); }
	    	
	    	 }
    }
	
    
    private static String simpleEvaluate (List<String> innermost, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	
    	if (innermost.get(0).equals("(")) {
    		innermost.remove(innermost.size()-1);
    		innermost.remove(0); }
    	
    	float dividend, divisor, operand1, operand2;
    	
   	 	for (int i=0; i<innermost.size(); i++) 
   	 	{
   	 		String key = innermost.get(i);
	   		if (Character.isAlphabetic(key.charAt(0)))
	   		 {
	   			int varValue = sequentialSearchVariable(key, vars);
	   			innermost.set(i, Integer.toString(varValue));}
	    }
   	 	
   	 	if (innermost.get(0).equals("-")) {
			innermost.set(0, "-"+innermost.get(1));
			innermost.remove(1); }
   	 	
   	 	if (innermost.size()==1)
   	 			return innermost.get(0);
   	 	
   	 	for (int j=1; j<innermost.size(); j++) {
   	 		if (innermost.get(j).equals("/")) {
   	 			dividend = Float.parseFloat(innermost.get(j-1));
   	 			divisor = Float.parseFloat(innermost.get(j+1));
   	 			String newTerm = Float.toString(dividend/divisor); 
   	 			innermost.remove(j+1);
   	 			innermost.remove(j-1);
   	 			innermost.remove(j-1);
   	
   	 			if (innermost.isEmpty())
   	 				innermost.add(newTerm);	
   	 			else
   	 				innermost.add(j-1, newTerm); 
   	 			return simpleEvaluate(innermost, vars, arrays); }
   	 		else if (innermost.get(j).equals("*")) {
   	 			operand1 = Float.parseFloat(innermost.get(j-1));
   	 			operand2 = Float.parseFloat(innermost.get(j+1));
   	 			String newTerm = Float.toString(operand1*operand2);
   	 			innermost.remove(j+1);
	 			innermost.remove(j-1);
	 			innermost.remove(j-1);
	
	 			if (innermost.isEmpty())
	 				innermost.add(newTerm);
	 			else
	 				innermost.add(j-1, newTerm);
	 			return simpleEvaluate(innermost, vars, arrays);}
   	 	}
   	 		
   	 	for (int k=1; k<innermost.size(); k++) {
   	 		if (innermost.get(k).equals("+")) {
   	 		operand1 = Float.parseFloat(innermost.get(k-1));
	 			operand2 = Float.parseFloat(innermost.get(k+1));
	 			String newTerm = Float.toString(operand1+operand2);
	 			innermost.remove(k+1);
	 			innermost.remove(k-1);
	 			innermost.remove(k-1);

	 			if (innermost.isEmpty())
	 				innermost.add(newTerm);
	 			else
	 				innermost.add(k-1, newTerm);
	 			return simpleEvaluate(innermost, vars, arrays); }
   	 		else if (innermost.get(k).equals("-")) {
   	 			operand1 = Float.parseFloat(innermost.get(k-1));
   	 			operand2 = Float.parseFloat(innermost.get(k+1));
   	 			String newTerm = Float.toString(operand1-operand2);
	   	 		innermost.remove(k+1);
	 			innermost.remove(k-1);
	 			innermost.remove(k-1);
	 		
	 			if (innermost.isEmpty())
	 				innermost.add(newTerm);
	 			else
	 				innermost.add(k-1, newTerm);
	 			return simpleEvaluate(innermost, vars, arrays);}
   	 	}		
		return "";
    }
    
    private static double evaluateArrayAtIndex (List<String> innermost, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	List<String> withinBrackets = innermost.subList(2, innermost.size()-1);

    	String newWithinBrackets = simpleEvaluate(withinBrackets, vars, arrays);
    	int index = (int)Float.parseFloat(newWithinBrackets);
    	for (int i=2; i<innermost.size()-1;)
    		innermost.remove(i);
    	innermost.add(2, newWithinBrackets);
    	int [] arrayValues = sequentialSearchArray(innermost.get(0), arrays);
    	double newVal = (double)arrayValues[index];
    	return newVal;
    }

    private static int[] sequentialSearchArray (String key, ArrayList<Array> arrays) {
    	for (int j=0; j<arrays.size(); j++)
		 {
    		Array tmp = arrays.get(j);
			 if (tmp.name.equals(key))
				 return tmp.values; }
    		return null;
    }
    
    private static int sequentialSearchVariable (String key, ArrayList<Variable> vars) {
    	for (int j=0; j<vars.size(); j++)
		 {
    		Variable tmp = vars.get(j);
			 if (tmp.name.equals(key))
				 return tmp.value; }
    		return 0;
    }
    
    private static String arrayListToString (List<String> innermost) {
    	String s="";
    	for (int i=0; i<innermost.size(); i++)
    		s+=innermost.get(i);
    return s;
    }
    
}
