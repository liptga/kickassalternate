package com.ind.commithook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.ISVNChangeEntryHandler;
import org.tmatesoft.svn.core.wc.admin.SVNChangeEntry;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public class Hook
{
	private static final int COMMENT_MIN_LENGTH = 4;
	private static final String TEMP_DIR = "d:\\repo\\temp";
	private static Checker checker = null;
	
	private static String TRANSACTION_ID_ARGUMENT = "-tr";
	private static String REPOSITORY_ROOT_ARGUMENT = "-repo";
	private static String TEMP_DIR_ARGUMENT = "-tempdir";
	private static String CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT = "-fileconfig";
	private static String CONFIG_FILE_IN_REPOSITORY_ARGUMENT = "-repoconfig";
	private static String[] HELP_ARGUMENTS = new String[] { "-help", "/?" , "--help", "-h", "-?" };
	private static String HELP = 
		"Commit Hook Framework\r\n\r\n" + //
		"Usage sample:\r\n" + //
		"java -jar commithook.jar com.ind.commithook.Hook " + TRANSACTION_ID_ARGUMENT + " 14-i " + REPOSITORY_ROOT_ARGUMENT + " d:\\repository " + CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT + " d:\\hookconfig.xml\r\n\r\n" + //
		"Arguments:\r\n" + //
		"\t" + TRANSACTION_ID_ARGUMENT + " : (MANDATORY) transaction identifier. Second argument of hook script.\r\n" + //
		"\t" + REPOSITORY_ROOT_ARGUMENT + " : (MANDATORY) root of repository in server file system. First argument of hook script.\r\n" + //
		"\t" + TEMP_DIR_ARGUMENT + " : must be given if config file do not contain \"TempDirectory\" element. Into this directory will be the changes and needed files fetched.\r\n" + //
		"\tExactly on of the followings must be given:\r\n" + //
		"\t" + CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT + " : if config file comes from the file system you define it here\r\n" + //
		"\t" + CONFIG_FILE_IN_REPOSITORY_ARGUMENT + " : if config file comes from the SVN repository itself you define it here\r\n";
	private String transactionId;
	private String repositoryRoot;
	private File repositoryRootFile;
	private SVNLookClient svnClient;
	private CommitHookConfig config;

	public Hook( String transactionId, String repositoryRoot, String configFile, boolean configIsInFileSystem )
	{
		this.transactionId = transactionId;
		this.repositoryRoot = repositoryRoot;
		this.repositoryRootFile = new File( repositoryRoot );
		String configFilePath = configFile;
		final SVNClientManager clientManager = SVNClientManager.newInstance();
		final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		svnClient = new SVNLookClient(clientManager, options);
		if ( !configIsInFileSystem )
		{
			
		}
	}

	public static void main(final String[] args) throws InterruptedException,
			SVNException, CheckstyleException, IOException, JAXBException
	{
	    JAXBContext jc = JAXBContext.newInstance( CommitHookConfig.class.getPackage().getName() );
	    Unmarshaller u = jc.createUnmarshaller();
	    CommitHookConfig config = (CommitHookConfig)u.unmarshal( new FileInputStream( "C:\\Documents and Settings\\Lipták Gábor\\commithook\\com.ind.commithook\\samples\\config.xml" ) );
		
		Map<String, String> arguments = new HashMap<String, String>();
		String key = null;
		for (int i = 0; i < args.length; i++)
		{
			if ( key == null )
			{
				key = args[ 0 ];
			}
			else
			{
				arguments.put(key, args[i] );
				key = null;
			}
		}
		boolean problem = false;
		List<String> argumentList = Arrays.asList( args );
		for (int i = 0; i < HELP_ARGUMENTS.length; i++)
		{
			if ( argumentList.contains(HELP_ARGUMENTS[i] ) )
			{
				System.out.println( HELP );
				System.exit( 0 );
			}
		}
		if ( !arguments.containsKey( TRANSACTION_ID_ARGUMENT ) )
		{
			System.err.println( "Argument " + TRANSACTION_ID_ARGUMENT + " must be set. This will be the second argument of hook script. Pass it to this program!" );
			problem = true;
		}
		if ( !arguments.containsKey( REPOSITORY_ROOT_ARGUMENT ) )
		{
			System.err.println( "Argument " + REPOSITORY_ROOT_ARGUMENT + " must be set. This will be the first argument of hook script. Pass it to this program!" );
			problem = true;
		}
		//XOR
		if ( ! ( arguments.containsKey( CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT ) ^ arguments.containsKey( CONFIG_FILE_IN_REPOSITORY_ARGUMENT ) ) )
		{
			System.err.println( "Exactly one of " + CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT + " or " + CONFIG_FILE_IN_REPOSITORY_ARGUMENT + " must be given!" );
			problem = true;
		}
		if ( problem )
		{
			System.err.println( "\r\n" + HELP );
			System.exit(1);
		}
		
		Hook hook = new Hook( arguments.get(TRANSACTION_ID_ARGUMENT), arguments.get(REPOSITORY_ROOT_ARGUMENT), arguments.containsKey(CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT) ? arguments.get(CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT) : arguments.get(CONFIG_FILE_IN_REPOSITORY_ARGUMENT), arguments.containsKey(CONFIG_FILE_IN_FILE_SYSTEM_ARGUMENT));

		String result = hook.process();
		
		if ( result != null && result.length()>0)
		{
			System.err.print(result);
			System.exit(1);
		}
		
//		final String commitComment = look.doGetLog(repoRoot, transactionId);
//		if (commitComment.length() < COMMENT_MIN_LENGTH)
//		{
//			System.err.println("Commit comment length cannot be shorter than "
//					+ COMMENT_MIN_LENGTH + "! Your comment was like: \""
//					+ commitComment + "\".");
//		}
//
//		final Configuration checkstyleConfig = ConfigurationLoader
//				.loadConfiguration(
//						"d:\\repo\\svnchecker\\svnchecker-0.3\\checkstyle.xml",
//						new PropertiesExpander(System.getProperties()));
//
//		checker = new Checker();
//		final ClassLoader moduleClassLoader = Checker.class.getClassLoader();
//		checker.setModuleClassLoader(moduleClassLoader);
//		checker.configure(checkstyleConfig);
//		checker.addListener(new DefaultLogger(System.err, false));
//		final ChangeHandler changeHandler = new ChangeHandler();
//		look.doGetChanged(repoRoot, transactionId, changeHandler, true);
//		checker.process(saveFiles(changeHandler.getFiles(), look, repoRoot,
//				transactionId));
//		deleteDirectory(new File(TEMP_DIR + File.separator + transactionId));
//		System.exit(1);

	}

	private String process()
	{
		return null;
	}

	static private boolean deleteDirectory(final File path)
	{
		if (path.exists())
		{
			final File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					deleteDirectory(files[i]);
				} else
				{
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private static List<File> saveFiles(final List<String> files,
			final SVNLookClient look, final File repoRoot,
			final String transaction) throws SVNException, IOException
	{
		final List<File> savedFiles = new ArrayList<File>();
		for (int i = 0; i < files.size(); i++)
		{
			final String filePath = files.get(i);
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			look.doCat(repoRoot, filePath, transaction, bos);
			final String directoryOfTransaction = TEMP_DIR + File.separator
					+ transaction;
			final String filePathWithCorrectedSeparators = filePath.replace(
					'/', File.separatorChar);
			final String targetFileName = directoryOfTransaction
					+ filePathWithCorrectedSeparators;
			final String targetDir = targetFileName.substring(0, targetFileName
					.lastIndexOf(File.separatorChar));
			new File(targetDir).mkdirs();
			final FileOutputStream fos = new FileOutputStream(targetFileName);
			fos.write(bos.toByteArray());
			fos.close();
			savedFiles.add(new File(targetFileName));
		}
		return savedFiles;
	}

	/**
	 * Inner class for change traversing ala SVNKit.
	 * @author Lipták Gábor
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
			final String fileName = entry.getPath();
			if (fileName.endsWith(".java"))
			{
				files.add(fileName);
			}
		}
	}
}
