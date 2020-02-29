import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

class AuxArray{

	public static String[] remove(String[] arr, int index){
		String[] new_arr = new String[arr.length-1];
		for (int i=0,j=0;i<arr.length;i++)
		{
			if (i==index)	continue;
      
			new_arr[j]=arr[i];
			j++;
		}
		return new_arr;
	}
}
