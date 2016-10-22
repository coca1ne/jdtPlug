package jdtplug.handlers;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class EntityVisitor extends ASTVisitor {
	private boolean entityExist = false;
	private HashSet<String> dbConstant;
	
	public EntityVisitor(HashSet<String> dbConstant){
		this.dbConstant = dbConstant;
	}
	
	@Override
	/**
	 * 判断方法里面的变量是否包含Column，JoinColumn，MapKeyColumn
	 */
	public boolean visit(SimpleName node) {
		// TODO Auto-generated method stub
		IBinding iBinding = node.resolveBinding();
		if((iBinding != null) && (iBinding instanceof IVariableBinding)){
			IVariableBinding variableBinding = (IVariableBinding)iBinding;
			IAnnotationBinding[] annotationBindings = variableBinding.getAnnotations();
			for(IAnnotationBinding annotationBinding : annotationBindings){
				if(dbConstant.contains(annotationBinding.getName())){
					//System.out.println("IVariableBinding:\t"+annotationBinding.getName());
					entityExist = true;
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean getEntityExist(){
		return entityExist;
	}
	
}
