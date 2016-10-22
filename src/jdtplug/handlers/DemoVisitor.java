package jdtplug.handlers;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
//org.eclipse.jdt.ui....jar
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.corext.dom.Bindings;

@SuppressWarnings({ "restriction", "unused" })
public class DemoVisitor extends ASTVisitor {

	private IJavaProject javaProject;

	public DemoVisitor(IJavaProject javaProject){
		this.javaProject = javaProject;
	}

    @Override
    public boolean visit(FieldDeclaration node) {
        for (Object obj: node.fragments()) {
            VariableDeclarationFragment v = (VariableDeclarationFragment)obj;
            //System.out.println("Field:\t" + v.getName());
        }

        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        //System.out.println("Method:\t" + node.getName());
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        //System.out.println("Class:\t" + node.getName());
        return true;
    }
	@SuppressWarnings("rawtypes")
	@Override
    public boolean visit(ForStatement node) {
        System.out.println("forStatement:\t" + node.getBody());
        ASTNode parentNode = findParentMethod(node);
        if(parentNode.getNodeType() == ASTNode.METHOD_DECLARATION){
        	MethodDeclaration parentMethod = (MethodDeclaration) parentNode;
        	IMethodBinding methodBinding = parentMethod.resolveBinding();
        	IMethod iMethod = (IMethod)methodBinding.getMethodDeclaration().getJavaElement();
        	//IMethod iMethod = (IMethod)itypeBinding.getErasure().getJavaElement();
			if (iMethod != null) {
				System.out.println("MethodName:\t"+iMethod.getElementName());
				HashSet<IMethod> caller = new HashSet<IMethod>();
				HashSet<IMethod> methodinit = new HashSet<IMethod>();
				methodinit.add(iMethod);
				try {
					caller = getCallersOf(caller,methodinit);
					for (Iterator it = caller.iterator(); it.hasNext();) {
						System.out.println(it.next());
					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

        	}
        }
        return true;
    }
	/*
	 * function : 递归找出for statement node所在的方法
	 * Input
	 * node : for statement ASTNode
	 */
	private ASTNode findParentMethod(ASTNode node) {
		int parentNodeType = node.getParent().getNodeType();
		if (parentNodeType == ASTNode.METHOD_DECLARATION) {
			return node.getParent();
		}
		if (parentNodeType == ASTNode.INITIALIZER){
			return node.getParent();
		}
		if (parentNodeType == ASTNode.TYPE_DECLARATION){
			return node.getParent();
		}
		return findParentMethod(node.getParent());
	}
	/*
	 * find all caller of for statement method
	 * input
	 * callers : record the call method
	 * methodsSet : dynamically change, every recall the function
	 * 				getCallersOf, methodSet remove the first element
	 */
	public HashSet<IMethod> getCallersOf(HashSet<IMethod> callers,HashSet<IMethod> methodsSet) throws JavaModelException {
		if(methodsSet.isEmpty())
			return callers;
		JavaSearchScope searchCode = new JavaSearchScope();
		searchCode.add(javaProject);
		CallHierarchy callHierarchy = CallHierarchy.getDefault();
		callHierarchy.setSearchScope(searchCode);
		IMember[] members = { (IMember) methodsSet.toArray()[0] };
		methodsSet.remove(methodsSet.toArray()[0]);
		MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
		for (MethodWrapper method_wrapper : methodWrappers) {
			MethodWrapper[] methodwrapper_temp = method_wrapper.getCalls(new NullProgressMonitor());
			HashSet<IMethod> temp = getIMethods(methodwrapper_temp);
			callers.addAll(temp);
			methodsSet.addAll(temp);
		}
		return getCallersOf(callers,methodsSet);
	}

	HashSet<IMethod> getIMethods(MethodWrapper[] methodWrappers) {
		HashSet<IMethod> callmethod = new HashSet<IMethod>();
		for (MethodWrapper method_wrapper : methodWrappers) {
			IMethod im = getIMethodFromMethodWrapper(method_wrapper);
			if (im != null) {
				callmethod.add(im);
			}
		}
		return callmethod;
	}

	IMethod getIMethodFromMethodWrapper(MethodWrapper method_wrapper) {
		try {
			IMember im = method_wrapper.getMember();
			if (im.getElementType() == IJavaElement.METHOD) {
				return (IMethod) method_wrapper.getMember();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
//	public HashSet<IMethod> getCallersOf(IMethod method) throws JavaModelException {
//	JavaSearchScope searchCode = new JavaSearchScope();
//	searchCode.add(javaProject);
//	CallHierarchy callHierarchy = CallHierarchy.getDefault();
//	callHierarchy.setSearchScope(searchCode);
//	IMember[] members = { method };
//	MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
//	HashSet<IMethod> callers = new HashSet<IMethod>();
//	for (MethodWrapper method_wrapper : methodWrappers) {
//		MethodWrapper[] methodwrapper_temp = method_wrapper.getCalls(new NullProgressMonitor());
//		HashSet<IMethod> temp = getIMethods(methodwrapper_temp);
//		callers.addAll(temp);
//	}
//	for(Iterator iter = callers.iterator();iter.hasNext();)
//		return getCallersOf((IMethod)iter.next());
//	return callers;
//}
}