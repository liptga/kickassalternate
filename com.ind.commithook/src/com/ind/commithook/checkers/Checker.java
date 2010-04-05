/**
 * 
 */
package com.ind.commithook.checkers;

import java.io.File;
import java.util.Collection;

import com.ind.commithook.CheckerException;
import com.ind.commithook.Hook;

/**
 * Checker interface.
 * @author Lipt�k G�bor
 */
public interface Checker
{
	/**
	 * For future use checkers can accept name value pairs, that come from config.xml. For example 
	 * checkstyle config file can come from such a parameter.
	 * @param name
	 * @param value
	 * @throws CheckerException
	 */
	void addParameter(String name, String value) throws CheckerException;

	/**
	 * The hook instance should be accepted. This class can provide extra services if necessary. It is guaranteed that it runs before addParameter method calls.
	 * @param hook - base utility class instance
	 * @throws CheckerException
	 */
	void setHook(Hook hook) throws CheckerException;

	/**
	 * The main work happens here. 
	 * 
	 * @param listOfFilesToProcess - The Hook as base class renders the list of changed files that passes
	 * the include and exclude patterns, and passes it to the check.
	 * @param tempDir - temp directory root is passed to checker to enable checker to get relative path in repository 
	 * @return null if no problem detected. Any object, and the toString() of the returned object will be 
	 * sent to the committer to see what is the problem
	 * @throws CheckerException
	 */
	Object process(Collection<File> listOfFilesToProcess, File tempDir) throws CheckerException;
}