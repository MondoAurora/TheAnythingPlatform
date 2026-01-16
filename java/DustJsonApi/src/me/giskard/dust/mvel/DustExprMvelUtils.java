package me.giskard.dust.mvel;

import java.util.Map;

import org.mvel2.MVEL;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustExprMvelUtils implements DustExprMvelConsts {
	
	public static <RetType> RetType eval(String str, Object ctx, Map data) {				
		return (RetType)  MVEL.eval(str, ctx, data);
	}

//	public static Object compile(String expr) {
//		return MVEL.compileExpression(expr);
//	}
//	
//	public static <RetType> RetType evalCompiled(Object o, Object ctx) {				
//		return (RetType) ((ctx instanceof Map) ?  MVEL.executeExpression(o, null, (Map) ctx) : MVEL.executeExpression(o, ctx));
//	}
//
//	public static <RetType> RetType evalCompiled(Object o, Object ctx, Map data) {				
//		return (RetType) MVEL.executeExpression(o, ctx, data);
//	}

}
