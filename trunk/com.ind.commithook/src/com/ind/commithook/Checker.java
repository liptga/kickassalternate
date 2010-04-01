/**
 * 
 */
package com.ind.commithook;

import java.io.File;
import java.util.List;

/**
 * Checker interface.
 * @author Lipták Gábor
 */
public interface Checker
{
	/**
	 * For future use checkers can accept name value pairs, that come from config.xml. For example 
	 * checkstyle config file can come from such a parameter.
	 * @param name
	 * @param value
	 */
	void addParameter( String name, String value );
	
	/**
	 * The hook instance should be accepted. This class can provide extra services if necessary
	 * @param hook - base utility class instance
	 */
	void setHook( Hook hook );
	
	/**
	 * The main work happens here. 
	 * 
	 * @param listOfFilesToProcess - The Hook as base class renders the list of changed files that passes
	 * the include and exclude patterns, and passes it to the check.
	 * @return null if no problem detected. Any object, and the toString() of the returned object will be 
	 * sent to the committer to see what is the problem
	 */
	Object process( List<File> listOfFilesToProcess );
}