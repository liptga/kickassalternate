package com.ind.commithook.checkers;

import java.util.HashMap;
import java.util.Map;

import com.ind.commithook.CheckerException;
import com.ind.commithook.Hook;

/**
 * Simple helper class to enable easier checker implementation
 * 
 * @author Lipt�k G�bor
 */
public abstract class CheckerHelper implements Checker
{
	protected Map<String, String> parameters = new HashMap<String, String>();
	protected Hook hookInstance;

	@Override
	public void addParameter(final String name, final String value) throws CheckerException
	{
		parameters.put(name, value);
	}

	@Override
	public void setHook(final Hook hook)
	{
		this.hookInstance = hook;
	}

}
