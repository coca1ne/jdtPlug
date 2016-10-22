package jdtplug.handlers;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * java源文件解析操作
 */
public class JdtAst {

    @SuppressWarnings("deprecation")
	private ASTParser astParser = ASTParser.newParser(AST.JLS3); // 非常慢
    /**
     * parser only one java file
     * 获得java源文件的结构CompilationUnit
     */
    public CompilationUnit getCompilationUnit(ICompilationUnit unit)
            throws CoreException {

        this.astParser.setResolveBindings(true);//
        this.astParser.setKind(ASTParser.K_COMPILATION_UNIT);//
        this.astParser.setBindingsRecovery(true);//
        @SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
        this.astParser.setCompilerOptions(options);
		this.astParser.setSource(unit);//创建并设定好ASTParser
        CompilationUnit result = (CompilationUnit) (this.astParser.createAST(null)); //源代码与AST的转换
        //createAST()方法的参数类型为IProgressMonitor，用于对AST的转换进行监控，不需要的话就填个null即可
        return result;

    }
}
