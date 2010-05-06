package hu.liptak.kickassalternate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class HookTest
{
	private static final String REPO_NAME = "repo";
	private static final String WORKING_COPY = "working_copy";
	private File repoDir = new File(REPO_NAME);
	private File workingCopyDir = new File(WORKING_COPY);
	private SVNCommitClient commitClient = null;
	
	private static final String TEST_JAVA_FILE = "HookTest.java";

	@BeforeSuite
//AfterSuite
	public void cleanUp()
	{
		System.out.println("Clearing temp repository");
		delete(repoDir);
		delete( workingCopyDir );
		repoDir.mkdirs();
		workingCopyDir.mkdirs();
	}
	
	public void delete( File file )
	{
		if (file.exists())
		{
			if (file.isFile())
			{
				file.delete();
			}
			else
			{
				Hook.deleteDirectory(file);
			}
		}
	}

	@BeforeSuite(dependsOnMethods =
	{ "cleanUp" })
	public void createRepository() throws SVNException
	{
		FSRepositoryFactory.setup(); // for local access (file protocol). 
		System.out.println("Creating temp repository");
		SVNURL tgtURL = SVNRepositoryFactory.createLocalRepository(repoDir,
				true, false);
		final SVNClientManager clientManager = SVNClientManager.newInstance();
		final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		commitClient = new SVNCommitClient(clientManager, options);
	}
	
	@Test
	public void importTestResources() throws SVNException, IOException
	{
		InputStream demoJavaFileStream = getClass().getClassLoader().getResourceAsStream(TEST_JAVA_FILE);
		String content = IOUtils.toString( demoJavaFileStream );
		IOUtils.closeQuietly( demoJavaFileStream );
		FileOutputStream fos = new FileOutputStream( workingCopyDir.getCanonicalPath() + File.separator + TEST_JAVA_FILE );
		IOUtils.write(content, fos);
		IOUtils.closeQuietly(fos);
		commitClient.doImport(workingCopyDir, SVNURL.fromFile(repoDir), "Initial import", null, true, true, SVNDepth.INFINITY );
	}
}
