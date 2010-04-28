package hu.liptak.kickassalternate.checkers;

import hu.liptak.kickassalternate.CheckerException;
import hu.liptak.kickassalternate.Hook;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple helper class to enable easier checker implementation
 * 
 * @author Lipt�k G�bor
 */
public abstract class CheckerHelper implements Checker
{
	protected Map<String, String> parameters = new HashMap<String, String>();
	protected Hook hookInstance;

	public void addParameter(final String name, final String value) throws CheckerException
	{
		parameters.put(name, value);
	}

	public void setHook(final Hook hook)
	{
		this.hookInstance = hook;
	}

}
