package com.ind.commithook.checkers;

import java.util.HashMap;
import java.util.Map;

import com.ind.commithook.Checker;
import com.ind.commithook.Hook;

/**
 * Simple helper class to enable easier checker implementation
 * 
 * @author Lipták Gábor
 */
public abstract class CheckerHelper implements Checker
{
	protected Map<String, String> parameters = new HashMap<String, String>();
	protected Hook hookInstance;

	@Override
	public void addParameter(String name, String value)
	{
		parameters.put(name, value);
	}

	@Override
	public void setHook(Hook hook)
	{
		this.hookInstance = hook;
	}

}
