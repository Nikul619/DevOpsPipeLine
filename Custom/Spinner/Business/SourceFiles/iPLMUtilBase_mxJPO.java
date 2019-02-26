import java.util.HashMap;
import java.util.Map;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.effectivity.EffectivityFramework;

import matrix.db.Context;

public class iPLMUtilBase_mxJPO {
		
	public Map getEffectivityExpresssion (Context context, String[] args) throws Exception{
		String strLFRelId = args[0];
		Map exprMap = new HashMap();
		
		if(null!=strLFRelId && !strLFRelId.equals(""))
		{
			MapList expressionMap=null;
			EffectivityFramework ef = new EffectivityFramework();
			expressionMap =  ef.getRelExpression(context, strLFRelId);
			exprMap = (Map)expressionMap.get(0);						
			/*strActualExpression = (String)exprMap.get(EffectivityFramework.ACTUAL_VALUE);
			strDisplayExpression = (String)exprMap.get(EffectivityFramework.DISPLAY_VALUE);*/
			
		}
		//System.out.println(exprMap);
		return exprMap;
	}

}