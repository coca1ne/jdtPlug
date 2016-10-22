package jdtplug.handlers;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

@SuppressWarnings("restriction")
public class ASTHandle {
	//private IPackageFragmentRoot packagesRoot;
	private IJavaProject javaProject;
	/**
	 * 构造函数
	 * 将搜索的范围作为参数传进来
	 */
	public ASTHandle(IJavaProject javaProject){
		this.javaProject = javaProject;
	}
	/**
	 * function : for statement node
	 * 
	 * @param for statement ASTNode
	 * @return method contain this for statement
	 */
	public ASTNode findParentMethod(ASTNode node) {
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
	 /**
	  * Fetches the search scope with the appropriate include mask. 
	  *  
	  * @param includeMask the include mask 
	  * @return the search scope with the appropriate include mask 
	  */ 
	private IJavaSearchScope getSearchScope(IJavaProject javaProject){
		//设置搜索范围是整个工程，而且只搜索源码,不搜索依赖的包和系统包
		int constraints = IJavaSearchScope.SOURCES;
		//constraints |= IJavaSearchScope.APPLICATION_LIBRARIES;
		//constraints |= IJavaSearchScope.SYSTEM_LIBRARIES;
		IJavaElement[] javaElements = new IJavaElement[] {javaProject};
		IJavaSearchScope searchCode = SearchEngine.createJavaSearchScope(javaElements,constraints);
		return searchCode;
	}
	/**
	 * find all callees of for statement method
	 * 
	 * @param
	 * methodsSet : dynamically change, every recall the function
	 * 				getCallersOf, methodSet remove the first element
	 */
	public HashSet<IMethod> getCalleesOf(HashSet<IMethod> methodsSet)
			throws JavaModelException {
		//保存for语句里面递归调用的方法
		HashSet<IMethod> callees = new HashSet<IMethod>();
		//保存已经访问过的方法
		HashSet<IMethod> methodsVisited = new HashSet<IMethod>();
		//设置搜索范围
		CallHierarchy callHierarchy = CallHierarchy.getDefault();
		callHierarchy.setSearchScope(getSearchScope(javaProject));
		//循环知道没有方法调用，判断methodSet是否空，每次删除第一个方法
		while (!methodsSet.isEmpty()) {
			//取出第一个搜索的方法
			IMember[] members = { (IMember) methodsSet.toArray()[0] };
			IMethod firstiMethod = (IMethod)methodsSet.toArray()[0];
			//如果这个方法已经搜索过，删除并重新回到while循环，防止两个方法来回调用，死循环readCountGenericEntity
			if(methodsVisited.contains(firstiMethod)){
				methodsSet.remove(firstiMethod);
				continue;
			}
			//获取这个方法调用了哪些方法
			MethodWrapper[] methodWrappers = callHierarchy.getCalleeRoots(members);
			for (MethodWrapper method_wrapper : methodWrappers) {
				MethodWrapper[] methodwrapper_temp = method_wrapper.getCalls(new NullProgressMonitor());
				HashSet<IMethod> temp = getIMethods(methodwrapper_temp);
				callees.addAll(temp);
				methodsSet.addAll(temp);
			}
			methodsVisited.add(firstiMethod);//保存已经访问的方法
			//使用(IMethod)methodsSet.toArray()[0]会造成无限循环，因为set的addall方法不是末尾添加
			methodsSet.remove(firstiMethod);//删除已经访问的方法，需要放倒addAll(temp)之后，否则递归会出现无限循环
		}
		//递归会造成栈溢出
		//return getCalleesOf(callees, methodsSet);
		return callees;
	}

	/**
	 * find all callers of for statement method
	 * 
	 * @param
	 * methodsSet : dynamically change, every recall the function
	 * 				getCallersOf, methodSet remove the first element
	 */
	public HashSet<IMethod> getCallersOf(HashSet<IMethod> methodsSet) throws JavaModelException {
		HashSet<IMethod> callers = new HashSet<IMethod>();
		HashSet<IMethod> methodsVisited = new HashSet<IMethod>();
		CallHierarchy callHierarchy = CallHierarchy.getDefault();
		callHierarchy.setSearchScope(getSearchScope(javaProject));
		
		while(!methodsSet.isEmpty()){
			IMember[] members = { (IMember) methodsSet.toArray()[0] };
			IMethod firstiMethod = (IMethod)methodsSet.toArray()[0];
			if(methodsVisited.contains(firstiMethod)){
				methodsSet.remove(firstiMethod);
				continue;
			}
			//找到所有调用目标方法的方法
			MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
			for (MethodWrapper method_wrapper : methodWrappers) {
				MethodWrapper[] methodwrapper_temp = method_wrapper.getCalls(new NullProgressMonitor());
				HashSet<IMethod> temp = getIMethods(methodwrapper_temp);
				callers.addAll(temp);
				methodsSet.addAll(temp);
				methodsVisited.add(firstiMethod);
			}
			methodsSet.remove(firstiMethod);
		}
		//return getCallersOf(callers,methodsSet);
		return callers;
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
	
	/**
	 * 判断方法所在的类有没有entity字段
	 * @param IMethod
	 * @return if exist @Entity return true, otherwise false
	 */
	public boolean entityMethod(IMethod iMethod){
		EntityVisitor entity = new EntityVisitor();
		ICompilationUnit iCompilationUnit = iMethod.getCompilationUnit();
		if(iCompilationUnit != null){
			JdtAst jdtAst = new JdtAst();
			CompilationUnit cunit;
			try {
				cunit = jdtAst.getCompilationUnit(iCompilationUnit);
				cunit.accept(entity);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return entity.entityExist();
		}
		return false;
	}
}
