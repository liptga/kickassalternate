package hu.liptak.kickassalternate.checkers;

import hu.liptak.kickassalternate.CheckerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLWellFormedChecker extends ParameterResolverCheckerHelper
{
	public Object process(Collection<File> listOfFilesToProcess, File tempDir)
			throws CheckerException
	{
		StringBuilder result = new StringBuilder();
		XMLReader reader;
		try
		{
			reader = XMLReaderFactory.createXMLReader();
		}
		catch (SAXException e)
		{
			throw new CheckerException("Error while instantiating XMLReader", e);
		}
		for (File file : listOfFilesToProcess)
		{
			try
			{
				reader.parse(new InputSource(new FileInputStream(file)));
			}
			catch (Exception e)
			{
				result.append("Error while parsing ");
				// making path to repository relative path
				try
				{
					result.append(file.getCanonicalPath().substring(
							tempDir.getCanonicalPath().length()).replace('\\',
							'/'));
				}
				catch (IOException e1)
				{
					// do not exactly know what to do here.
					e1.printStackTrace();
				}
				result.append(". Error message: ");
				result.append("\r\n");
				Throwable cause = null;
				do
				{
					if (cause == null)
						cause = e;
					else
						cause = cause.getCause();
					result.append(e.getLocalizedMessage());
					result.append("\r\n");
				}
				while (e.getCause() != null);
			}
		}
		if (result.length() > 0)
			return result.toString();
		return null;
	}

}
