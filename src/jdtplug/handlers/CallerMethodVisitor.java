package jdtplug.handlers;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
//org.eclipse.jdt.ui....jar
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.corext.dom.Bindings;

@SuppressWarnings({ "restriction", "unused" })
public class CallerMethodVisitor extends ASTVisitor {

	private ASTHandle astHandle;
	private boolean EntityFlag = false;

	public CallerMethodVisitor(IJavaProject javaProject){
		//this.javaProject = javaProject;
		astHandle = new ASTHandle(javaProject);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean visit(EnhancedForStatement node) {
		// find methods calees in for statement block

		// if @Entity exist, caller will do
		ASTNode parentNode = astHandle.findParentMethod(node);
		// find the for statement in which method
		if (parentNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
			MethodDeclaration parentMethod = (MethodDeclaration) parentNode;
			IMethodBinding methodBinding = parentMethod.resolveBinding();
			if (methodBinding == null)
				return true;
			IMethod iMethod = (IMethod) methodBinding.getMethodDeclaration().getJavaElement();
			// (IMethod)itypeBinding.getErasure().getJavaElement();
			if (iMethod != null) {
				HashSet<IMethod> callee = new HashSet<IMethod>();
				HashSet<IMethod> caller = new HashSet<IMethod>();
				HashSet<IMethod> methodinit = new HashSet<IMethod>();
				methodinit.add(iMethod);
				callee.add(iMethod);// 需要添加forStatement方法，因为这个方法可能在entity类里面
				try {
					callee.addAll(astHandle.getCalleesOf(methodinit));
					for (Iterator<IMethod> it = callee.iterator(); it.hasNext();) {
						IMethod method = (IMethod) it.next();
						EntityFlag = astHandle.DBMethod(method);
						//EntityFlag = false;
						if (EntityFlag) {
							System.out.println("forStatement:\t" + node.getBody());
							System.out.println("ForMethod:\t" + iMethod);
							methodinit.add(iMethod);// methodinit已经空，需要重新添加for
							caller = astHandle.getCallersOf(methodinit);
							break;
						}
					}
					if (EntityFlag) {
						for (Iterator it = caller.iterator(); it.hasNext();) {
							System.out.println("Caller method:\t" + it.next());
						}
						for (Iterator it = callee.iterator(); it.hasNext();) {
							System.out.println("Callee method\t" + it.next());
						}
					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
}