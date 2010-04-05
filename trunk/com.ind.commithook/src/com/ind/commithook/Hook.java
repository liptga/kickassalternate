package com.ind.commithook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.ISVNChangeEntryHandler;
import org.tmatesoft.svn.core.wc.admin.SVNChangeEntry;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

import com.ind.commithook.Check.Exclude;
import com.ind.commithook.Check.Include;
import com.ind.commithook.checkers.Checker;

public class Hook
{
	private static String TRANSACTION_ID_ARGUMENT = "-tr";
	private static String REPOSITORY_ROOT_ARGUMENT = "-repo";
	private static String TEMP_DIR_ARGUMENT = "-temp";
	private static String CONFIG_FILE_ARGUMENT = "-config";
	private static String[] HELP_ARGUMENTS = new String[] { "-help", "/?", "--help", "-h", "-?" };
	private static String HELP = "Commit Hook Framework\r\n\r\n" + //
			"Usage sample:\r\n" + //
			"java -jar commithook.jar com.ind.commithook.Hook " + TRANSACTION_ID_ARGUMENT + " 14-i " + REPOSITORY_ROOT_ARGUMENT + " d:\\repository " + CONFIG_FILE_ARGUMENT + " d:\\hookconfig.xml\r\n\r\n" + //
			"Arguments (all mandatory):\r\n" + //
			"\t" + TRANSACTION_ID_ARGUMENT + " : transaction identifier. Second argument of hook script.\r\n" + //
			"\t" + REPOSITORY_ROOT_ARGUMENT + " : root of repository in server file system. First argument of hook script.\r\n" + //
			"\t" + TEMP_DIR_ARGUMENT + " : into this directory will be the changes and needed files fetched.\r\n" + //
			"\t" + CONFIG_FILE_ARGUMENT + " : config file path. If not found in file system, fetched as relative path from SVN\r\n"; //
	private final String transactionId;
	private final File repositoryRootFile;
	private SVNLookClient svnClient;
	private final String tempDir;
	private final String configFile;
	private final Map<String, Pattern> patternCache = new HashMap<String, Pattern>();

	public Hook(final String transactionId, final String repositoryRoot, final String configFile, final String tempDir)
	{
		this.transactionId = transactionId;
		this.repositoryRootFile = new File(repositoryRoot);
		this.tempDir = tempDir;
		this.configFile = configFile;
	}

	public SVNLookClient getSvnClient()
	{
		return svnClient;
	}

	public String getTransactionId()
	{
		return transactionId;
	}

	public File getRepositoryRootFile()
	{
		return repositoryRootFile;
	}

	public byte[] getFileFromSVN(final String path) throws SVNException
	{
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		svnClient.doCat(repositoryRootFile, path, transactionId, bos);
		return bos.toByteArray();
	}

	public String relativePath2TempPath(final String relativePath)
	{
		final String filePathWithCorrectedSeparators = relativePath.replace('/', File.separatorChar);
		return getTempDirOfTransaction() + filePathWithCorrectedSeparators;
	}

	public File saveFileFromSVNToTemp(final String path) throws SVNException, IOException
	{
		final String targetFileName = relativePath2TempPath(path);
		final String targetDir = targetFileName.substring(0, targetFileName.lastIndexOf(File.separatorChar));
		if (targetDir.length() > 0)
			new File(targetDir).mkdirs();
		final File targetFile = new File(targetFileName);
		final FileOutputStream fos = new FileOutputStream(targetFile);
		fos.write(getFileFromSVN(path));
		fos.close();
		return targetFile;
	}

	private Pattern getPattern(final String pattern)
	{
		Pattern result = patternCache.get(pattern);
		if (result == null)
		{
			result = Pattern.compile(pattern);
			patternCache.put(pattern, result);
		}
		return result;
	}

	public String getTempDirOfTransaction()
	{
		return tempDir + File.separator + transactionId;
	}

	public static void main(final String[] args) throws CheckerException, CommitHookException
	{
		final Map<String, String> arguments = new HashMap<String, String>();
		String key = null;
		for (int i = 0; i < args.length; i++)
		{
			if (key == null)
			{
				key = args[i];
			}
			else
			{
				arguments.put(key, args[i]);
				key = null;
			}
		}
		boolean problem = false;
		final List<String> argumentList = Arrays.asList(args);
		for (int i = 0; i < HELP_ARGUMENTS.length; i++)
		{
			if (argumentList.contains(HELP_ARGUMENTS[i]))
			{
				System.out.println(HELP);
				System.exit(0);
			}
		}
		if (!arguments.containsKey(TRANSACTION_ID_ARGUMENT))
		{
			System.err.println("Argument " + TRANSACTION_ID_ARGUMENT + " must be set. This will be the second argument of hook script. Pass it to this program!");
			problem = true;
		}
		if (!arguments.containsKey(REPOSITORY_ROOT_ARGUMENT))
		{
			System.err.println("Argument " + REPOSITORY_ROOT_ARGUMENT + " must be set. This will be the first argument of hook script. Pass it to this program!");
			problem = true;
		}
		if (!arguments.containsKey(TEMP_DIR_ARGUMENT))
		{
			System.err.println("Argument " + TEMP_DIR_ARGUMENT + " must be set!");
			problem = true;
		}
		if (!arguments.containsKey(CONFIG_FILE_ARGUMENT))
		{
			System.err.println("Argument " + CONFIG_FILE_ARGUMENT + " must be set!");
			problem = true;
		}
		if (problem)
		{
			System.err.println("\r\n" + HELP);
			System.exit(1);
		}

		final Hook hook = new Hook(arguments.get(TRANSACTION_ID_ARGUMENT), arguments.get(REPOSITORY_ROOT_ARGUMENT), arguments.get(CONFIG_FILE_ARGUMENT), arguments.get(TEMP_DIR_ARGUMENT));

		final String result = hook.process();

		if (result != null && result.length() > 0)
		{
			System.err.print(result);
			System.exit(1);
		}
	}

	private String process() throws CheckerException, CommitHookException
	{
		final StringBuilder result = new StringBuilder();
		try
		{
			final SVNClientManager clientManager = SVNClientManager.newInstance();
			final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
			svnClient = new SVNLookClient(clientManager, options);

			final JAXBContext jc = JAXBContext.newInstance(CommitHookConfig.class.getPackage().getName());
			final Unmarshaller u = jc.createUnmarshaller();
			final CommitHookConfig config = (CommitHookConfig) u.unmarshal(getFileStreamFromFileSystemOrRepository(configFile));
			final List<String> changedFilesPath = getChangedFiles();
			saveChangedFilesToTemp(getChangedFiles());
			for (final Check check : config.getCheck())
			{
				final Class checkerClass = Class.forName(check.getClassName());
				final Checker checker = (Checker) checkerClass.newInstance();
				checker.setHook(this);
				for (final Parameter parameter : check.getParameters().getParameter())
				{
					checker.addParameter(parameter.getName(), parameter.getValue());
				}
				final Object checkResult = checker.process(renderFileList(changedFilesPath, check.getExclude(), check.getInclude()), new File(tempDir));
				if (checkResult != null)
				{
					result.append("Checker \"");
					result.append(check.getLegend());
					result.append("\" returned errors. See its output:\r\n");
					result.append(checkResult);
					result.append("\r\n");
				}
			}
			return result.toString();
		}
		catch (final CheckerException ce)
		{
			throw ce;
		}
		catch (final Exception e)
		{
			throw new CommitHookException(e);
		}
		finally
		{
			deleteDirectory(new File(getTempDirOfTransaction()));
		}
	}

	private boolean matchesOneRegexpListItem(final List<RegexpPattern> regexpList, final String stringToMatch)
	{
		for (final RegexpPattern patternConfigItem : regexpList)
		{
			if (getPattern(patternConfigItem.getValue()).matcher(stringToMatch).matches())
				return true;
		}
		return false;
	}

	private Set<File> renderFileList(final List<String> changedFiles, final Exclude excludes, final Include includes)
	{
		final Set<File> result = new HashSet<File>();
		for (final String changedFilePath : changedFiles)
		{
			//comments are to retain formatting i like
			if (//
			(//
					includes == null || // 
					matchesOneRegexpListItem(includes.getRegexpPattern(), changedFilePath)// 
					)//
					&& //
					(//
					excludes == null || //
					!matchesOneRegexpListItem(excludes.getRegexpPattern(), changedFilePath)// 
					)//
			)//
				result.add(new File(relativePath2TempPath(changedFilePath)));
		}
		return result;
	}

	public InputStream getFileStreamFromFileSystemOrRepository(final String path) throws SVNException, IOException
	{
		return new FileInputStream(getFileFromFileSystemOrRepository(path));
	}

	public File getFileFromFileSystemOrRepository(final String path) throws SVNException, IOException
	{
		final File file = new File(path);
		if (file.exists() && file.isFile())
			return new File(path);
		else
		{
			return saveFileFromSVNToTemp(path);
		}
	}

	public boolean deleteDirectory(final File path)
	{
		if (path.exists())
		{
			final File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					deleteDirectory(files[i]);
				}
				else
				{
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private List<String> getChangedFiles() throws SVNException
	{
		final ChangeHandler changeHandler = new ChangeHandler();
		svnClient.doGetChanged(repositoryRootFile, transactionId, changeHandler, true);
		return changeHandler.getFiles();
	}

	private List<File> saveChangedFilesToTemp(final List<String> changedFiles) throws SVNException, IOException
	{
		final List<File> savedFiles = new ArrayList<File>();
		for (int i = 0; i < changedFiles.size(); i++)
		{
			final String filePath = changedFiles.get(i);
			savedFiles.add(saveFileFromSVNToTemp(filePath));
		}
		return savedFiles;
	}

	/**
	 * Inner class for change traversing ala SVNKit.
	 * @author Lipt�k G�bor
	 */
	private static class ChangeHandler implements ISVNChangeEntryHandler
	{
		private final List<String> files = new ArrayList<String>();

		public List<String> getFiles()
		{
			return files;
		}

		@Override
		public void handleEntry(final SVNChangeEntry entry) throws SVNException
		{
			if (entry.getKind().equals(SVNNodeKind.FILE))
				files.add(entry.getPath());
		}
	}
}
