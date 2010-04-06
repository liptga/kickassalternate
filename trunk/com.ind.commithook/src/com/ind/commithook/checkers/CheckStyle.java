package com.ind.commithook.checkers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ind.commithook.CheckerException;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public class CheckStyle extends ParameterResolverCheckerHelper
{
	private File configFile;

	public File getConfigFile()
	{
		return configFile;
	}

	public void setConfigFile(final File configFile)
	{
		this.configFile = configFile;
	}

	@Override
	public Object process(final Collection<File> listOfFilesToProcess, final File tempDir) throws CheckerException
	{
		try
		{
			final Configuration checkstyleConfig = ConfigurationLoader.loadConfiguration(configFile.getCanonicalPath(), new PropertiesExpander(System.getProperties()));
			final com.puppycrawl.tools.checkstyle.Checker checker = new com.puppycrawl.tools.checkstyle.Checker();
			final ClassLoader moduleClassLoader = Checker.class.getClassLoader();
			checker.setModuleClassLoader(moduleClassLoader);
			checker.configure(checkstyleConfig);
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			checker.addListener(new DefaultLogger(bos, true));
			final List<File> argument = new ArrayList<File>();
			argument.addAll(listOfFilesToProcess);
			final int numberOfErrors = checker.process(argument);
			final String result = bos.toString();
			if (numberOfErrors > 0)
				return result;
			return null;
		}
		catch (final Exception e)
		{
			throw new CheckerException(e);
		}
	}
}
