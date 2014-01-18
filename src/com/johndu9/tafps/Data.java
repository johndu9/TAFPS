package com.johndu9.tafps;

import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Class for reading and setting data to and from files 
 * @author John Du
 */
public class Data{

	/** Used for reading a file */
	private static Scanner fileReader;
	/** Used for cutting up strings into elements and data */
	private static StringTokenizer tokens;
	/** Path to the file we want to read data from */
	private String path;
	/** Holds the data in structure {{name, value}, ...} */
	private String[][] data;
	/** Index of the name of the data */
	public static final int NAME = 0;
	/** Index of the value of the data */
	public static final int VALUE = 1;
	/** File separator */
	public static final String SEP = System.getProperty("file.separator");
	/** Directory path */
	public static final String DIR_PATH = System.getProperty("user.dir") + SEP;
	/** Separates the name from the value */
	private static final String NAME_SEPARATOR = ":";
	/** Separates elements from each other within a value */
	private static final String ELEMENT_SEPARATOR = ",";
	
	/**
	 * Constructor, creates data from a file
	 * @param path Path to the file we want to read data from
	 */
	public Data(String path){
		setPath(path);
		readDataFromFile();
	}
	
	/**
	 * Counts the amount of times a given name shows up, allows for number at end of name
	 * @param name Name you want to count
	 * @return THe amount of times the name shows up
	 */
	public int countName(String name){
		int count = 0;
		for(int i = 0; i < data.length; i++){
			if(
				data[i][NAME].length() > name.length() &&
				data[i][NAME].length() <= name.length() + 2 &&
				data[i][NAME].substring(0, name.length()).equals(name)){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Gets the index of the first time a name shows up
	 * @param name Name we're looking for
	 * @return Index of first occurrence of name, otherwise -1
	 */
	public int getIndexOfName(String name){
		for(int i = 0; i < data.length; i++){
			if(data[i][NAME].equals(name)){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Gets the value at a given index
	 * @param index Index of value
	 * @return Value at given index
	 */
	public String getValue(int index){
		return data[index][VALUE];
	}
	
	/**
	 * Gets the value from a given name 
	 * @param name Name of value
	 * @return Value of given name, "" otherwise
	 */
	public String getValue(String name){
		int index = getIndexOfName(name);
		return (index != -1) ? (getValue(index)) : ("");
	}
	
	/**
	 * Sets the value of a given index to something new
	 * @param index Index of value
	 * @param value The new value we want
	 */
	public void setValue(int index, String value){
		data[index][VALUE] = value;
	}
	
	/**
	 * Sets the value of a given name to something new
	 * @param name Name of value
	 * @param value The new value we want
	 */
	public void setValue(String name, String value){
		int index = getIndexOfName(name);
		setValue(index, value);
	}
	
	/**
	 * Adds a value to the data
	 * @param name Name of value
	 * @param value Value of value
	 */
	public void addValue(String name, String value){
		String[][] newData = new String[data.length + 1][data[0].length];
		for(int i = 0; i < data.length; i++){
			newData[i] = data[i];
		}
		newData[data.length] = new String[]{name, value};
		data = newData;
	}
	
	/**
	 * Removes a value of given name
	 * @param name Name of value
	 */
	public void removeValue(String name){
		int index = getIndexOfName(name);
		if(index != -1){
			String[][] newData = new String[data.length - 1][data[0].length];
			for(int i = 0; i < newData.length; i++){
				if(i != index){
					if(i > index){
						newData[i] = data[i - 1];
					}else{
						newData[i] = data[i];
					}
				}
			}
			data = newData;
		}
	}
	
	/**
	 * @return Path to the file we want to read data from
	 */
	public String getPath(){
		return path;
	}
	
	/**
	 * @param path Path to the file we want to read data from
	 */
	public void setPath(String path){
		this.path = path;
	}
	
	/**
	 * Gets us a couple of elements from a given value by cutting it up
	 * @param index Index of value
	 * @return String array of the cut up value
	 */
	public String[] getElementsFromValue(int index){
		return getElementsFromValue(data[index][VALUE]);
	}
	
	/**
	 * Gets elements from a given index in a given data
	 * @param data Data we're searching in
	 * @param index Index of value
	 * @return String array of the cut up value
	 */
	public static String[] getElementsFromValue(Data data, int index){
		return getElementsFromValue(data.getData()[index][VALUE]);
	}
	
	/**
	 * Cuts up a value and gets us a String array of the elements
	 * @param value Value we're cutting up
	 * @return String array of the cut up value
	 */
	public static String[] getElementsFromValue(String value){
		tokens = new StringTokenizer(value, ELEMENT_SEPARATOR);
		String[] elements = new String[tokens.countTokens()];
		int counter = 0;
		
		while(tokens.hasMoreTokens()){
			elements[counter] = tokens.nextToken();
			counter++;
		}
		return elements;
	}
	
	/**
	 * Reads all of the data from the file and converts it to data-stuff that we can use in the program
	 */
	private void readDataFromFile(){
		if(!new File(getPath()).exists()){
			data = new String[][]{{"", ""}};
			return;
		}
		
		String[][] readData;
		int lineCount = 0;
		int tokenCount;
		String cache = "";
		
		resetFileReader();
		
		while(fileReader.hasNext()){
			cache = fileReader.nextLine();
			lineCount++;
		}
		
		tokens = new StringTokenizer(cache, NAME_SEPARATOR);
		tokenCount = tokens.countTokens();
		readData = new String[lineCount][tokenCount];
		
		resetFileReader();

		for(int i = 0; fileReader.hasNext(); i++){
			tokens = new StringTokenizer(fileReader.nextLine(), NAME_SEPARATOR);
			for(int j = 0; tokens.hasMoreTokens(); j++){
				readData[i][j] = tokens.nextToken();
			}
		}
		data = readData;
	}
	
	/**
	 * @return The data
	 */
	private String[][] getData(){
		return data;
	}
	
	/**
	 * Writes the data back into the file
	 * @throws IOException
	 */
	public void writeDataToFile() throws IOException{
		String total = "";
		for(int i = 0; i < data.length; i++){
			for(int j = 0; j < data[i].length; j++){
				total += data[i][j];
				if(j < data[i].length - 1){
					total += NAME_SEPARATOR;
				}
			}
			if(i < data.length - 1){
				total += "\n";
			}
		}
		
		Scanner reader = new Scanner(total);
		BufferedWriter writer = new BufferedWriter(new FileWriter(getPath()));
		while(reader.hasNextLine()){
			writer.write(reader.nextLine());
			if(reader.hasNextLine()){
				writer.newLine();
			}
		}
		reader.close();
		writer.close();
	}
	
	/**
	 * Resets the file reader
	 */
	private void resetFileReader(){
		try{
			fileReader = new Scanner(new File(getPath()));
		}catch(FileNotFoundException e){
			fileReader = null;
			e.printStackTrace();	
		}		
	}
	
}