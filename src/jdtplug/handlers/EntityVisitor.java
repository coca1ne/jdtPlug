package jdtplug.handlers;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;

public class EntityVisitor extends ASTVisitor {

	private static final String ENTITY_ANNOTAION = "Entity";
	private boolean entityExist = false;
	@Override
	public boolean visit(MarkerAnnotation node) {
		// TODO Auto-generated method stub
		//System.out.println("getTypeName()\t"+node.getTypeName());
		if(node.getTypeName().toString().equals(ENTITY_ANNOTAION)){
			entityExist = true;
			return false;
		}
		return true;
	}
	
	public boolean entityExist(){
		return entityExist;
	}

}
