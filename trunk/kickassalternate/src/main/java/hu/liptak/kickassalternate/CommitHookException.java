/**
 * 
 */
package hu.liptak.kickassalternate;

/**
 * To be used by general framework services
 * 
 * @author liptak
 */
public class CommitHookException extends Exception
{

	/**
	 * 
	 */
	public CommitHookException()
	{
	}

	/**
	 * @param arg0
	 */
	public CommitHookException(final String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public CommitHookException(final Throwable arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public CommitHookException(final String arg0, final Throwable arg1)
	{
		super(arg0, arg1);
	}

}
