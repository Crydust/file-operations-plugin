package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.File;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import java.io.Serializable;

public class FolderCopyOperation extends FileOperation implements Serializable { 
	private final String sourceFolderPath;	
	private final String destinationFolderPath;	
	
	@DataBoundConstructor 
	 public FolderCopyOperation(String sourceFolderPath, String destinationFolderPath) { 
		this.sourceFolderPath = sourceFolderPath;	
		this.destinationFolderPath = destinationFolderPath;
	 }

	 public String getSourceFolderPath()
	 {
		 return sourceFolderPath;
	 }
	 
	 public String getDestinationFolderPath()
	 {
		 return destinationFolderPath;
	 }
	 	 
	 public boolean runOperation(AbstractBuild build, Launcher launcher, BuildListener listener) {
		 boolean result = false;
		 try
			{
			 	listener.getLogger().println("Folder Copy Operation:");				
				try {	
					FilePath ws = new FilePath(build.getWorkspace(),"."); 
					result = ws.act(new TargetFileCallable(listener, build.getEnvironment(listener).expand(sourceFolderPath),build.getEnvironment(listener).expand(destinationFolderPath),build.getEnvironment(listener)));				
				}
				catch (Exception e) {
					listener.fatalError(e.getMessage());					
					return false;
				}
			}
			catch(Exception e)
			{
				listener.fatalError(e.getMessage());
			}	
			return result;
		} 
 
	private static final class TargetFileCallable implements FileCallable<Boolean> {
		private static final long serialVersionUID = 1;
		private final BuildListener listener;
		private final EnvVars environment;
		private final String resolvedSourceFolderPath;
		private final String resolvedDestinationFolderPath;
		
		public TargetFileCallable(BuildListener Listener, String ResolvedSourceFolderPath, String ResolvedDestinationFolderPath, EnvVars environment) {
			this.listener = Listener;
			this.resolvedSourceFolderPath = ResolvedSourceFolderPath;	
			this.resolvedDestinationFolderPath = ResolvedDestinationFolderPath;	
			this.environment = environment;
		}
		@Override public Boolean invoke(File ws, VirtualChannel channel) {
			boolean result = false;
			try 
			{				
				FilePath fpWS = new FilePath(ws);
				FilePath fpSF = new FilePath(fpWS, resolvedSourceFolderPath);
				FilePath fpTL = new FilePath(fpWS, resolvedDestinationFolderPath);				
				listener.getLogger().println("Copying folder: " + fpSF.getRemote() + " to " + fpTL.getRemote());
				fpSF.copyRecursiveTo(fpTL);
				result = true;
			}
			catch(RuntimeException e)
			{
				listener.fatalError(e.getMessage());
				throw e;
			}
			catch(Exception e)
			{
				listener.fatalError(e.getMessage());
				result = false;
			}
			return result;	
		}
		
		@Override  public void checkRoles(RoleChecker checker) throws SecurityException {
                
		}		
	}
 @Extension public static class DescriptorImpl extends FileOperationDescriptor {
 public String getDisplayName() { return "Folder Copy"; }

 }
}