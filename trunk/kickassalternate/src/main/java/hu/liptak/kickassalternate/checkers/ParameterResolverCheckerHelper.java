package hu.liptak.kickassalternate.checkers;

import hu.liptak.kickassalternate.CheckerException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.svn.core.SVNException;

public abstract class ParameterResolverCheckerHelper extends CheckerHelper
{
	protected String getMethodName(final String parameterName)
	{
		return "set" + parameterName.substring(0, 1).toUpperCase()
				+ parameterName.substring(1);
	}

	/**
	 * Tries to resolve parameters to call setters of the checker class.
	 */
	@Override
	public void addParameter(final String name, final String value)
			throws CheckerException
	{
		final Method[] allMethods = this.getClass().getMethods();
		final String methodName = getMethodName(name);
		final Map<Class, Method> matchingMethods = new HashMap<Class, Method>();
		for (final Method method : allMethods)
		{
			final Class[] parameters = method.getParameterTypes();
			if (method.getName().equals(methodName) && parameters.length == 1)
			{
				matchingMethods.put(parameters[0], method);
			}
		}

		if (matchingMethods.size() == 0)
			throw new CheckerException("No matching setter found in checker '"
					+ this.getClass().getName() + "' for parameter '" + name
					+ "'. Check config file for mistypes!");

		final StringWriter errorsOfSetters = new StringWriter();
		final PrintWriter pw = new PrintWriter(errorsOfSetters);

		for (final Class parameterClass : matchingMethods.keySet())
		{
			final Method setter = matchingMethods.get(parameterClass);
			try
			{
				setter.invoke(this, new Object[]
				{ renderParameter(value, parameterClass) });
				return;
			}
			catch (final Exception e)
			{
				// if it throws exception, we try additional matching setters.
				// So nothing is to do here. We just print stacktrace to a
				// writer. If all trial fails, we print stacktrace to
				// standard error
				pw.println("Setting parameter '" + name + "' with method '"
						+ setter.toString()
						+ "' failed with the following exception:");
				e.printStackTrace(pw);
			}
			pw.flush();
			pw.close();
		}
		throw new CheckerException(
				"Failed to call setter in checker '"
						+ this.getClass().getName()
						+ "' for parameter '"
						+ name
						+ "'. Check value of parameter in config file!\r\nChecker errors:\r\n"
						+ errorsOfSetters.toString());
	}

	private Object renderParameter(final String value,
			final Class desiredParameter) throws SVNException, IOException
	{
		if (desiredParameter.equals(String.class))
			return value;
		if (desiredParameter.equals(Integer.class)
				|| desiredParameter.equals(Integer.TYPE))
			return new Integer(value);
		if (desiredParameter.equals(Short.class)
				|| desiredParameter.equals(Short.TYPE))
			return new Short(value);
		if (desiredParameter.equals(Long.class)
				|| desiredParameter.equals(Long.TYPE))
			return new Long(value);
		if (desiredParameter.equals(Float.class)
				|| desiredParameter.equals(Float.TYPE))
			return new Float(value);
		if (desiredParameter.equals(Double.class)
				|| desiredParameter.equals(Double.TYPE))
			return new Double(value);
		if (desiredParameter.equals(BigInteger.class))
			return new BigInteger(value);
		if (desiredParameter.equals(BigDecimal.class))
			return new BigDecimal(value);
		if (desiredParameter.equals(File.class))
		{
			return hookInstance.getFileFromFileSystemOrRepository(value);
		}
		throw new UnsupportedOperationException(desiredParameter.getClass()
				.getName()
				+ " is not implemented yet. Contribute, man! :)");
	}
}
