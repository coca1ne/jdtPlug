package jdtplug.handlers;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AntiPatternHandler extends AbstractHandler {

	private static final String TESTCLASS_INIT = "FieldDefinitionImpl.java";
	private static final boolean DEBUG_OPTION = true;//debug option
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("Plug-in start");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		System.out.println("projects :\t" + projects.length);
		// Loop over all projects
		try {
			for (IProject project : projects)
			{
				if (!project.getName().equals("Lab"))
				{
					if (project.isOpen() && project.isNatureEnabled("org.eclipse.jdt.core.javanature"))
					{
						processProject(project);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	/*
	 * process detail project
	 */
	@SuppressWarnings("unused")
	private void processProject(IProject project) throws CoreException{
		IJavaProject javaProject = JavaCore.create(project);
		IPackageFragment[] packages = javaProject.getPackageFragments();
		//IPackageFragmentRoot src_scope = ExtractScope(javaProject);
		for (IPackageFragment mypackage : packages){
			//if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE && mypackage.getElementName().equals("testUnit"))
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE)
			{
				for (ICompilationUnit unit : mypackage.getCompilationUnits())
				{
					//if((DEBUG_OPTION==true) && (unit.getElementName().equals(TESTCLASS_INIT))){
						JdtAst jdtAst = new JdtAst();
						CompilationUnit cunit = jdtAst.getCompilationUnit(unit);
				        CallerMethodVisitor visitor = new CallerMethodVisitor(javaProject);//设置搜索范围为整个工程，包括系统包jar
				        //DemoVisitor visitor = new DemoVisitor(src_scope);//设置搜索范围为src里面的代码
						//AnnotationVisitor visitor = new AnnotationVisitor();
				        cunit.accept(visitor);
					//}
				}
			}
		}
	}
	/*
	 * extract the search scope
	 * travel the code find src PackageFragmentRoot
	 * if not return null
	 */
	private IPackageFragmentRoot ExtractScope(IJavaProject javaProject) throws CoreException{
		System.out.println("project:\t"+javaProject.getElementName());
		IPackageFragmentRoot[] iPackageFragmentRoots = javaProject.getPackageFragmentRoots();
		for(IPackageFragmentRoot iPackageFragmentRoot: iPackageFragmentRoots){
			System.out.println("packageRoot:\t"+iPackageFragmentRoot);
			if(iPackageFragmentRoot.getElementName().equals("src"))
				return iPackageFragmentRoot;
		}
		return null;
	}
}
