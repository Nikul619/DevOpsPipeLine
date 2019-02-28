/*
 * 	iPLMVPLMUtil_mxJPO.java
 ********************************************************************************************
 * Modification Details:
 *
 * Ver|  Date       | CDSID    | CR      | Comment
 * ---|-------------|----------|---------|--------------------------------------------------
 * 01 |             |          |         | 
 ********************************************************************************************
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.DomainConstants;

//Added Comment
public class iPLMVPLMUtil_mxJPO implements DomainConstants {
	public static final String TYPE_PROCESS_HEADER = PropertyUtil.getSchemaProperty("type_iPLMProcessHeader");

	/**		  
	  * This method is used to reserve a business object
	  * @param context Context : User's Context.
	  * @param args String array
	  * @return void
	  * @throws Exception if operation fails.
	  * @since V6R2017x
	  * @author ranand2
	  */ 
	public void autoLockStrictLocking(Context context, String[] args) throws Exception {
				
		String sSymbolicType = "";		
		StringList slInclusionTypes = null;
		
		try
		{						
			if ((args != null) && (args.length >= 3))
			{				
		        String sObjectType = args[3];
		        String sInclusionTypes = args[4];
		        
		        if( UIUtil.isNotNullAndNotEmpty(sInclusionTypes) ){
		        	slInclusionTypes = FrameworkUtil.split(sInclusionTypes, ",");
		        } else{
		        	slInclusionTypes = new StringList(1);
		        }
		        
		        if( UIUtil.isNotNullAndNotEmpty(sObjectType) ){
		        	 sSymbolicType = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_TYPE, sObjectType, false);
		        }
		        
		        if ( UIUtil.isNotNullAndNotEmpty(sSymbolicType) && slInclusionTypes.size() > 0 && slInclusionTypes.contains(sSymbolicType) ){		        	
		        	//Reserve the reference object - call the OOTB code
		        	JPO.invoke(context,"TeamSecurityServices",new String[]{},"AutoLockStrictLocking",args,void.class);		        	 
		        }		        
			} else {
				throw new MatrixException("ERROR : Illegal Argument");
			}
		}catch( Exception e){
			e.printStackTrace();
			throw e;			
		}
	}	
	
	/**		  
	  * This method is used to reserve a connection
	  * @param context Context : User's Context.
	  * @param args String array
	  * @return void
	  * @throws Exception if operation fails.
	  * @since V6R2017x
	  * @author ranand2
	  */ 	
	public void autoLockStrictLockingRel(Context context, String[] args) throws Exception {
		
		String sFromSymbolicType = "";			
		StringList slInclusionTypes = null;
		
		try
		{										
			if ((args != null) && (args.length >= 3))
			{	
				String sRelId = args[0];
		        String sFromObjectId = args[1];
		        String sToObjectId = args[2];
		        String sFromObjectType = args[3];        		       
		        String sFromObjInclusionTypes = args[4];
		        	 
		        if( UIUtil.isNotNullAndNotEmpty(sFromObjInclusionTypes) ){
		        	slInclusionTypes = FrameworkUtil.split(sFromObjInclusionTypes, ",");
		        } else{
		        	slInclusionTypes = new StringList(1);
		        }
		        
		        if( UIUtil.isNotNullAndNotEmpty(sRelId) && UIUtil.isNotNullAndNotEmpty(sFromObjectId) && UIUtil.isNotNullAndNotEmpty(sToObjectId) ){
		        	
		        	 if( UIUtil.isNotNullAndNotEmpty(sFromObjectType) ){
		        		 sFromSymbolicType = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_TYPE, sFromObjectType, false);
			         }
		        			        	
	        		if( UIUtil.isNotNullAndNotEmpty(sFromSymbolicType) && slInclusionTypes.size() > 0 && slInclusionTypes.contains(sFromSymbolicType) ){	        			
	        			//Reserve the instance object - call the OOTB code
			        	JPO.invoke(context,"TeamSecurityServices",new String[]{},"AutoLockStrictLockingRel",args,void.class);			        	
			        }			        
		        }		       	         
			} else {
				throw new MatrixException("ERROR : Illegal Argument");
			}
				
		}catch( Exception e){
			e.printStackTrace();
			throw e;			
		}
	}
	/**
	 * This method is used to display Scoring document Icon and create Hyperlink for same
	 * @param context
	 * @param args
	 * @return Vector
	 * @throws Exception
	 * @author dpuri
	 */
	public Vector showScoringDocumentLink(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		String sPHId = "";
		String sType = "";
		String sLink = "";
		StringBuffer sbLink = null;
		Map mpProdStruct = null; 
		Vector vReturn   = null;
		
		try {
			vReturn = new Vector(objectList.size());
			Iterator prodStructItr = objectList.iterator();
			while (prodStructItr.hasNext()) {
				mpProdStruct = (Map)prodStructItr.next();
				sType = (String)mpProdStruct.get(DomainConstants.SELECT_TYPE);
				sbLink = new StringBuffer(200);
				if(TYPE_PROCESS_HEADER.equals(sType)){
					sPHId = (String)mpProdStruct.get(DomainConstants.SELECT_ID);
					sLink = "";
					sLink = "<a href='javascript: var url = \"../common/iPLMDocumentViewIntermediate.jsp?fromProcessStructure=true&amp;objectId="+sPHId+"\"; var objFrame=findFrame(top,\"HCScoringDocInformationCmd\"); objFrame.document.location.href=url; alert(\"Please make sure Scoring Document tab is selected.\")'><img border='0' src='../common/images/I_HSC.bmp' alt=\"Scoring Document\" title=\"Scoring Document\"/></a>";
						sbLink.append(sLink);
				}
				vReturn.addElement(sbLink.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}
		return vReturn;
	}
	/**
	 * This method is used as Range Function for Scoring attributes
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 * @author dpuri
	 */
	public HashMap getScoringAttrRanges(Context context,String[] args) throws Exception{
		HashMap hmRangeMap = new HashMap();
		Properties propNotification =null;
		String strAttributeChoices=null;
		StringTokenizer tokenizer= null;
		String strFieldChoice;
		StringList slFieldChoices = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap fieldMap = (HashMap) programMap.get("fieldMap");
		HashMap settingsMap = (HashMap) fieldMap.get("settings");
		String strAttributeName = (String) settingsMap.get("Attribute");
		propNotification = readPageObject(context, "iPLMHCScoringDocumentAttributes");
		if(propNotification!=null && !propNotification.equals("null") && !(propNotification.size()==0))
			strAttributeChoices  = propNotification.getProperty(strAttributeName);
		if (null!=strAttributeChoices && !(strAttributeChoices.length()==0)) {
			tokenizer = new StringTokenizer(strAttributeChoices, "|");

			while (tokenizer.hasMoreElements()){
				strFieldChoice = tokenizer.nextToken().trim();
				slFieldChoices.add(strFieldChoice);
			}
		}
		hmRangeMap.put("field_choices", slFieldChoices);
		hmRangeMap.put("field_display_choices", slFieldChoices);
		return hmRangeMap;
	}
	/**
	 * This method is used to read page file
	 * @param context
	 * @param args
	 * @return Properties
	 * @throws Exception
	 * @author dpuri
	 */
	public Properties readPageObject(Context context,String strPageObject) throws Exception {
		Properties propertyEntry = new Properties();
		try {
			Page page = new Page(strPageObject);
			page.open(context);
			String strProperties = page.getContents(context);
			page.close(context);
			InputStream input = new ByteArrayInputStream(strProperties.getBytes("UTF8"));
			propertyEntry.load(input);
		} catch(Exception e){
			e.printStackTrace();
		}
		return propertyEntry;
	}
}
