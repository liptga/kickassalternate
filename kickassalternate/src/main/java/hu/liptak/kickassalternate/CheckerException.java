package hu.liptak.kickassalternate;

/**
 * To be used by checkers. Checkers should throw this exception.
 * @author liptak
 *
 */
public class CheckerException extends Exception
{

	public CheckerException()
	{
		super();
	}

	public CheckerException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public CheckerException(final String message)
	{
		super(message);
	}

	public CheckerException(final Throwable cause)
	{
		super(cause);
	}

}
