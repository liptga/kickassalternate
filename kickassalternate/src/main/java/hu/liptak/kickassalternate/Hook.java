package hu.liptak.kickassalternate;

import hu.liptak.kickassalternate.checkers.Checker;
import hu.liptak.kickassalternate.generated.Check;
import hu.liptak.kickassalternate.generated.CommitHookConfig;
import hu.liptak.kickassalternate.generated.Parameter;
import hu.liptak.kickassalternate.generated.RegexpPattern;
import hu.liptak.kickassalternate.generated.Check.Exclude;
import hu.liptak.kickassalternate.generated.Check.Include;

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

/**
 * Hook Framework to enable avoiding bad source code to be committed.<br/>
 * To use it, first go inside your repository directory in your server, and go
 * inside hooks directory.<br/>
 * Create pre-commit.bat, or pre-commit.sh according to your operating system.
 * Enter the following command:
 * 
 * 
 * @author Lipt�k G�bor
 */
public class Hook
{
	private static String TRANSACTION_ID_ARGUMENT = "-tr";
	private static String REPOSITORY_ROOT_ARGUMENT = "-repo";
	private static String TEMP_DIR_ARGUMENT = "-temp";
	private static String CONFIG_FILE_ARGUMENT = "-config";
	private static String[] HELP_ARGUMENTS = new String[]
	{ "-help", "/?", "--help", "-h", "-?" };
	private static String HELP = "Commit Hook Framework\r\n\r\n"
			+ //
			"Usage sample:\r\n"
			+ //
			"java -jar commithook.jar com.ind.commithook.Hook "
			+ TRANSACTION_ID_ARGUMENT
			+ " %2 "
			+ REPOSITORY_ROOT_ARGUMENT
			+ " %1 "
			+ CONFIG_FILE_ARGUMENT
			+ " d:\\hookconfig.xml\r\n\r\n"
			+ //
			"Arguments (all mandatory):\r\n"
			+ //
			"\t"
			+ TRANSACTION_ID_ARGUMENT
			+ " : transaction identifier. Second argument of hook script.\r\n"
			+ //
			"\t"
			+ REPOSITORY_ROOT_ARGUMENT
			+ " : root of repository in server file system. First argument of hook script.\r\n"
			+ //
			"\t"
			+ TEMP_DIR_ARGUMENT
			+ " : into this directory will be the changes and needed files fetched.\r\n"
			+ //
			"\t"
			+ CONFIG_FILE_ARGUMENT
			+ " : config file path. If not found in file system, fetched as relative path from SVN\r\n"; //
	private final String transactionId;
	private final File repositoryRootFile;
	private SVNLookClient svnClient;
	private final String tempDir;
	private final String configFile;
	private final Map<String, Pattern> patternCache = new HashMap<String, Pattern>();

	/**
	 * Creates an instance of Hook. The instance is not thread safe.
	 * 
	 * @param transactionId
	 *            transaction id. This is the second argument of the commit hook
	 *            script
	 * @param repositoryRoot
	 *            root of the repository in file system. This is the first
	 *            argument of hook script
	 * @param configFile
	 *            path of config file. First evaluated as file system entry, if
	 *            not found this way, then applied as relative path to the SVN
	 *            repository root
	 * @param tempDir
	 *            a temp directory in wich the hook can place temporary files
	 */
	public Hook(final String transactionId, final String repositoryRoot,
			final String configFile, final String tempDir)
	{
		this.transactionId = transactionId;
		this.repositoryRootFile = new File(repositoryRoot);
		this.tempDir = tempDir;
		this.configFile = configFile;
	}

	/**
	 * Gives back SVN client used by hook
	 * 
	 * @return
	 */
	public SVNLookClient getSvnClient()
	{
		return svnClient;
	}

	/**
	 * Transaction id
	 * 
	 * @return
	 */
	public String getTransactionId()
	{
		return transactionId;
	}

	/**
	 * File pointer to SVN repository directory.
	 * 
	 * @return
	 */
	public File getRepositoryRootFile()
	{
		return repositoryRootFile;
	}

	/**
	 * Fetches file from SVN by a path into a byte array
	 * 
	 * @param path
	 * @return
	 * @throws SVNException
	 */
	public byte[] getFileFromSVN(final String path) throws SVNException
	{
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		svnClient.doCat(repositoryRootFile, path, transactionId, bos);
		return bos.toByteArray();
	}

	/**
	 * Returns the desired place of temp file.
	 * 
	 * @param relativePath
	 *            SVN path
	 * @return file system path
	 */
	public String relativePath2TempPath(final String relativePath)
	{
		final String filePathWithCorrectedSeparators = relativePath.replace(
				'/', File.separatorChar);
		return getTempDirOfTransaction() + filePathWithCorrectedSeparators;
	}

	/**
	 * Saves file into temp directory from SVN using SVN path. Returns pointer
	 * to it.
	 * 
	 * @param path
	 *            SVN path relative to SVN repository root
	 * @return {@link File} instance pointing to the saved file in the temp
	 *         directory
	 * @throws SVNException
	 * @throws IOException
	 */
	public File saveFileFromSVNToTemp(final String path) throws SVNException,
			IOException
	{
		final String targetFileName = relativePath2TempPath(path);
		final String targetDir = targetFileName.substring(0, targetFileName
				.lastIndexOf(File.separatorChar));
		if (targetDir.length() > 0)
			new File(targetDir).mkdirs();
		final File targetFile = new File(targetFileName);
		final FileOutputStream fos = new FileOutputStream(targetFile);
		fos.write(getFileFromSVN(path));
		fos.close();
		return targetFile;
	}

	/**
	 * Little enhancement to cache patterns to gain speed.
	 * 
	 * @param pattern
	 *            pattern string
	 * @return compiled {@link Pattern} instance
	 */
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

	/**
	 * Temp directory is returned. It is made of -temp argument +
	 * {@link File#separator} + -tr argument
	 * 
	 * @return temp directory path
	 */
	public String getTempDirOfTransaction()
	{
		return tempDir + File.separator + transactionId;
	}

	/**
	 * Main arguments are described in class documentation. Returns 1 as exit
	 * code if error is present. Error message is put into {@link System#err}
	 * 
	 * @param args
	 * @throws CheckerExceptions
	 * @throws CommitHookException
	 */
	public static void main(final String[] args) throws CheckerException,
			CommitHookException
	{
		// argument checking
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
			System.err
					.println("Argument "
							+ TRANSACTION_ID_ARGUMENT
							+ " must be set. This will be the second argument of hook script. Pass it to this program!");
			problem = true;
		}
		if (!arguments.containsKey(REPOSITORY_ROOT_ARGUMENT))
		{
			System.err
					.println("Argument "
							+ REPOSITORY_ROOT_ARGUMENT
							+ " must be set. This will be the first argument of hook script. Pass it to this program!");
			problem = true;
		}
		if (!arguments.containsKey(TEMP_DIR_ARGUMENT))
		{
			System.err.println("Argument " + TEMP_DIR_ARGUMENT
					+ " must be set!");
			problem = true;
		}
		if (!arguments.containsKey(CONFIG_FILE_ARGUMENT))
		{
			System.err.println("Argument " + CONFIG_FILE_ARGUMENT
					+ " must be set!");
			problem = true;
		}
		if (problem)
		{
			System.err.println("\r\n" + HELP);
			System.exit(1);
		}

		final Hook hook = new Hook(arguments.get(TRANSACTION_ID_ARGUMENT),
				arguments.get(REPOSITORY_ROOT_ARGUMENT), arguments
						.get(CONFIG_FILE_ARGUMENT), arguments
						.get(TEMP_DIR_ARGUMENT));

		final String result = hook.process();

		if (result != null && result.length() > 0)
		{
			System.err.print(result);
			System.exit(1);
		}
	}

	/**
	 * Processing method. See inline comments
	 * 
	 * @return error message if present. Otherwise null
	 * @throws CheckerException
	 * @throws CommitHookException
	 */
	private String process() throws CheckerException, CommitHookException
	{
		final StringBuilder result = new StringBuilder();
		try
		{
			// SVN client instantiation
			final SVNClientManager clientManager = SVNClientManager
					.newInstance();
			final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
			svnClient = new SVNLookClient(clientManager, options);

			// loading config file with JAXB
			final JAXBContext jc = JAXBContext
					.newInstance(CommitHookConfig.class.getPackage().getName());
			final Unmarshaller u = jc.createUnmarshaller();

			// config can be both in file system and in SVN
			final CommitHookConfig config = (CommitHookConfig) u
					.unmarshal(getFileStreamFromFileSystemOrRepository(configFile));
			final List<String> changedFilesPath = getChangedFiles();
			saveFilesFromSVNToTemp(getChangedFiles());

			for (final Check check : config.getCheck())
			{
				final Class checkerClass = Class.forName(check.getClassName());
				final Checker checker = (Checker) checkerClass.newInstance();
				checker.setHook(this);
				if (check.getParameters() != null)
					for (final Parameter parameter : check.getParameters()
							.getParameter())
					{
						checker.addParameter(parameter.getName(), parameter
								.getValue());
					}
				final Object checkResult = checker.process(renderFileList(
						changedFilesPath, check.getExclude(), check
								.getInclude()), new File(tempDir));
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

	/**
	 * Returns true, if the String is matching with any of the patterns in the
	 * list.
	 * 
	 * @param regexpList
	 * @param stringToMatch
	 * @return
	 */
	private boolean matchesOneRegexpListItem(
			final List<RegexpPattern> regexpList, final String stringToMatch)
	{
		for (final RegexpPattern patternConfigItem : regexpList)
		{
			if (getPattern(patternConfigItem.getValue()).matcher(stringToMatch)
					.matches())
				return true;
		}
		return false;
	}

	/**
	 * For checkers patterns can be defined. These patterns are evaluated
	 * against the changed files here.
	 * 
	 * @param changedFiles
	 * @param excludes
	 * @param includes
	 * @return
	 */
	private Set<File> renderFileList(final List<String> changedFiles,
			final Exclude excludes, final Include includes)
	{
		final Set<File> result = new HashSet<File>();
		for (final String changedFilePath : changedFiles)
		{
			// comments are to retain formatting i like
			if (//
			(//
					includes == null || // 
					matchesOneRegexpListItem(includes.getRegexpPattern(),
							changedFilePath)// 
					)//
					&& //
					(//
					excludes == null || //
					!matchesOneRegexpListItem(excludes.getRegexpPattern(),
							changedFilePath)// 
					)//
			)//
				result.add(new File(relativePath2TempPath(changedFilePath)));
		}
		return result;
	}

	/**
	 * Basically works in the same way like
	 * {@link #getFileFromFileSystemOrRepository(String)}, but returns an opened
	 * {@link FileOutputStream} to the file.
	 * 
	 * @param path
	 * @return stream of desired file
	 * @throws SVNException
	 * @throws IOException
	 */
	public InputStream getFileStreamFromFileSystemOrRepository(final String path)
			throws SVNException, IOException
	{
		return new FileInputStream(getFileFromFileSystemOrRepository(path));
	}

	/**
	 * If path is found in the file system, and it is a file, a {@link File}
	 * instance pointing to it is returned. If not found in file system, then it
	 * is treated as a SVN path, the file is saved to temp directory of
	 * transaction ({@link #getTempDirOfTransaction()}), and a {@link File}
	 * instance pointing to it is returned.
	 * 
	 * @param path
	 * @return desired file pointer
	 * @throws SVNException
	 * @throws IOException
	 */
	public File getFileFromFileSystemOrRepository(final String path)
			throws SVNException, IOException
	{
		final File file = new File(path);
		if (file.exists() && file.isFile())
			return new File(path);
		else
		{
			return saveFileFromSVNToTemp(path);
		}
	}

	/**
	 * Deletes a directory recursively. Take care of stack, since it is
	 * recursive.
	 * 
	 * @param path
	 *            directory path to delete
	 * @return true if deletion was successful
	 */
	public static boolean deleteDirectory(final File path)
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

	/**
	 * Retrieves the list of changed files from SVN transaction. The changes are
	 * filtered, and only files are returned. Directory and property changes are
	 * omitted currently.
	 * 
	 * @return list of changed files (SVN paths)
	 * @throws SVNException
	 */
	public List<String> getChangedFiles() throws SVNException
	{
		final ChangeHandler changeHandler = new ChangeHandler();
		svnClient.doGetChanged(repositoryRootFile, transactionId,
				changeHandler, true);
		return changeHandler.getFiles();
	}

	/**
	 * Saves a list of files from SVN repository to the temp directory of the
	 * transaction.
	 * 
	 * @param changedFiles
	 *            list of SVN paths to be saved. These SVN paths are relative to
	 *            the SVN repository root.
	 * @return an list with {@link File} instances referencing tosaved files
	 * @throws SVNException
	 * @throws IOException
	 */
	private List<File> saveFilesFromSVNToTemp(final List<String> changedFiles)
			throws SVNException, IOException
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
	 * 
	 * @author Liptak Gabor
	 */
	private static class ChangeHandler implements ISVNChangeEntryHandler
	{
		/**
		 * Into this list are changed files saved.
		 */
		private final List<String> files = new ArrayList<String>();

		/**
		 * Returns list of files (SVN paths) changed by SVN transaction.
		 * 
		 * @return
		 */
		public List<String> getFiles()
		{
			return files;
		}

		public void handleEntry(final SVNChangeEntry entry) throws SVNException
		{
			if (entry.getKind().equals(SVNNodeKind.FILE))
				files.add(entry.getPath());
		}
	}
}
