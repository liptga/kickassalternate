package hu.liptak.kickassalternate;

import java.io.File;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

public class HookTest
{
	private static final String REPO_NAME = "repo";
	private File repoDir = new File(REPO_NAME);
	private SVNCommitClient commitClient = null;

	@BeforeSuite
//AfterSuite
	public void clearRepository()
	{
		System.out.println("Clearing temp repository");
		if (repoDir.exists())
		{
			if (repoDir.isFile())
			{
				repoDir.delete();
			}
			else
			{
				Hook.deleteDirectory(repoDir);
			}
		}
	}

	@BeforeSuite(dependsOnMethods =
	{ "clearRepository" })
	public void createRepository() throws SVNException
	{
		System.out.println("Creating temp repository");
		SVNURL tgtURL = SVNRepositoryFactory.createLocalRepository(repoDir,
				true, false);
	}
	
	@Test
	public void importTestResources() throws SVNException
	{
		final SVNClientManager clientManager = SVNClientManager.newInstance();
		final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		commitClient = new SVNCommitClient(clientManager, options);
		
	}
}
