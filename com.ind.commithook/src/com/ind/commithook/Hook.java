package com.ind.commithook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	public static void main(final String[] args) throws InterruptedException, SVNException, CheckstyleException, IOException
	{
		final SVNClientManager clientManager = SVNClientManager.newInstance();
		final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		final SVNLookClient look = new SVNLookClient(clientManager, options);
		final File repoRoot = new File(args[0]);
		final String transactionId = args[1];

		final String commitComment = look.doGetLog(repoRoot, transactionId);
		if (commitComment.length() < COMMENT_MIN_LENGTH)
		{
			System.err.println("Commit comment length cannot be shorter than " + COMMENT_MIN_LENGTH + "! Your comment was like: \"" + commitComment + "\".");
		}

		final Configuration checkstyleConfig = ConfigurationLoader.loadConfiguration("d:\\repo\\svnchecker\\svnchecker-0.3\\checkstyle.xml", new PropertiesExpander(System.getProperties()));

		checker = new Checker();
		final ClassLoader moduleClassLoader = Checker.class.getClassLoader();
		checker.setModuleClassLoader(moduleClassLoader);
		checker.configure(checkstyleConfig);
		checker.addListener(new DefaultLogger(System.err, false));
		final ChangeHandler changeHandler = new ChangeHandler();
		look.doGetChanged(repoRoot, transactionId, changeHandler, true);
		checker.process(saveFiles(changeHandler.getFiles(), look, repoRoot, transactionId));
		deleteDirectory(new File(TEMP_DIR + File.separator + transactionId));
		System.exit(1);

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
				}
				else
				{
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private static List<File> saveFiles(final List<String> files, final SVNLookClient look, final File repoRoot, final String transaction) throws SVNException, IOException
	{
		final List<File> savedFiles = new ArrayList<File>();
		for (int i = 0; i < files.size(); i++)
		{
			final String filePath = files.get(i);
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			look.doCat(repoRoot, filePath, transaction, bos);
			final String directoryOfTransaction = TEMP_DIR + File.separator + transaction;
			final String filePathWithCorrectedSeparators = filePath.replace('/', File.separatorChar);
			final String targetFileName = directoryOfTransaction + filePathWithCorrectedSeparators;
			final String targetDir = targetFileName.substring(0, targetFileName.lastIndexOf(File.separatorChar));
			new File(targetDir).mkdirs();
			final FileOutputStream fos = new FileOutputStream(targetFileName);
			fos.write(bos.toByteArray());
			fos.close();
			savedFiles.add(new File(targetFileName));
		}
		return savedFiles;
	}

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
