package com.who.Edit.Base.Share.Share3;
import java.math.*;

public class NumberSpiltor
{
	public static double multiply(String f1, String f2)
	{
		BigDecimal d1 = new BigDecimal(f1);
		BigDecimal d2 = new BigDecimal(f2);
		BigDecimal d3 = d1.multiply(d2);
		return d3.doubleValue();
	}
	
}
