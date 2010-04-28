package hu.liptak.kickassalternate.checkers;

import hu.liptak.kickassalternate.CheckerException;

import java.io.File;
import java.util.Collection;

import org.tmatesoft.svn.core.SVNException;

public class CommentChecker extends ParameterResolverCheckerHelper
{
	private int minLength = -1;
	private String pattern;

	public int getMinLength()
	{
		return minLength;
	}

	public void setMinLength(final int minLength)
	{
		this.minLength = minLength;
	}

	public String getPattern()
	{
		return pattern;
	}

	public void setPattern(final String pattern)
	{
		this.pattern = pattern;
	}

	public Object process(final Collection<File> listOfFilesToProcess, final File tempDir) throws CheckerException
	{
		// TODO Auto-generated method stub
		final StringBuilder sb = new StringBuilder();
		try
		{
			if (minLength < 0 && pattern == null)
				throw new CheckerException("At least one of 'minLength' and 'pattern' parameters must be set!");
			final String commitComment = hookInstance.getSvnClient().doGetLog(hookInstance.getRepositoryRootFile(), hookInstance.getTransactionId());
			if (minLength > 0 && commitComment.length() < minLength)
			{
				sb.append("Commit comment length cannot be shorter than ").append(minLength).append("! Your comment was: \"").append(commitComment).append("\".\r\n");
			}
			if (pattern != null && !commitComment.matches(pattern))
			{
				sb.append("Commit comment do not match regular expression '").append(pattern).append("'! Your comment was: \"").append(commitComment).append("\".\r\n");
			}
		}
		catch (final SVNException e)
		{
			throw new CheckerException(e);
		}
		if (sb.length() > 0)
			return sb.toString();
		return null;
	}
}
