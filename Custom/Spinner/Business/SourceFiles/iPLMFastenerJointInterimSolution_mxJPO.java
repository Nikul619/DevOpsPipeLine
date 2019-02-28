/*
 ** ${CLASSNAME}
 **
 **Author: Santhosh Kondapaka
 *Fastener Joint Allocation
 **************************************************************
 * Modification Details:
 *
 * Ver| Date      | CDSID    | Issue  | Comment
 * ---|-----------|----------|-----|---------------------
 * 
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.text.SimpleDateFormat;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.util.StringList;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

//Test
public class ${CLASSNAME}
//public class iPLMFastenerJointInterimSolution_mxJPO
{
	//	${CLASS:iPLMUtilBase} iPLMUtilBaseJPO = new ${CLASS:iPLMUtilBase}();
	
	public ${CLASSNAME} () throws Exception	
	//public iPLMFastenerJointInterimSolution_mxJPO () throws Exception	
	{
		
	}
	
	MapList fastenerGlobalMapList = new MapList();
	static int LOCATION_FP_DN_Zone1; //Position of the pilot panel in the report
	static int LOCATION_FASTENER_INSTANCE_TITLE; // Position of the Fastener Instance
	static int LOCATION_FASTENER_PRODUCT; //Position of Fastener Product
	static int LOCATION_ZONE_COUNT; // Position of number of Panels fastened
	static int LOCATION_FEATURE_TYPE; // Position of type of Fastener
	private int nTotalJointCreated = 0;
	private int nTotalFastnereUsageUpdate = 0;
	private int iFastenerJointNumber = 0;
	private static String strDisplayNameDelimiter = null;
	private static String VAULT = "eService Production";
	private static int[] JOINT_LOCAL_INDEXES = new int[] {0,1};
	private static String FASTENER_INSTANCE_TYPE = "iPLMFlexibleFastenerAssemblyInstance";
	private static String FASTENER_JOINT_TYPE = "iPLMFastenerJoint";
	private static String FASTENER_JOINT_REVISION = "A.1";
	private static String FASTENER_JOINT_POLICY = "VPLM_SMB_Definition";
	//private static String EXTERNAL_ID_ATTRIBUTE = "PLM_ExternalID";
	private static String EXTERNAL_ID_ATTRIBUTE = "PLMEntity.PLM_ExternalID";
	private static String SELECT_EXTERNAL_ID_ATTRIBUTE = "attribute[PLM_ExternalID]";
	private static String JOINT_INDEX_ATT = "iPLMFastenerJoint.iPLMJointIndex";
	private static String JOINT_NUMBER_ATT = "iPLMFastenerJoint.iPLMJointNumber";
	private static String JOINT_LAST_RUNNING_NUMBER_ATT = "iPLMFastenerJoint.iPLMJointLastRunningNumber";
	private static String JOINT_PANEL_NAMES_ATT = "iPLMFastenerJoint.iPLMPanelNames";
	private static String JOINT_PANEL_PHYSICALIDS_ATT = "iPLMFastenerJoint.iPLMPanelPhysIds";
	private static String JOINT_ZONE_COUNT = "iPLMFlexibleFastenerAssemblyInstance.iPLMZoneCount";
	private static String SPOTFASTENER_UNIQUE_FIXING_ID_ATT = "iPLMFlexibleFastenerAssemblyInstance.iPLMFastenerID";
	private static String SPOTFASTENER_JOINT_ID_ATT = "iPLMFlexibleFastenerAssemblyInstance.iPLMJoint_ID";
	private static String KEY_INSTANCE_TITLE = "Instance_Title";
	private static String KEY_PRODUCT = "Product";
	private static String KEY_PARENT = "Parent";
	private static String KEY_PANEL = "Panel";
	private static String KEY_ZONECOUNT = "ZoneCount";
	private static String KEY_PLM_EXTERNAL_OR_NAME = null;
	private String strUserProject = null;
	private String strUserRole = null;
	private String strCurrentJointNumber = null;
	private String strFinalAlertMessage = "";			
	private String strPathName = null;
	private String strLogPath = null;
	private static BufferedWriter bwLogFileWriter = null;
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'@'HH-mm-ss");
	private StringList globalInstanceList = new StringList(); //Needs to be removed from code once testing completed
	private static int LOCATION_X_COORDINATE; // X-Coordinate Position of Fastener
	private static int LOCATION_Y_COORDINATE; // Y-Coordinate Position of Fastener
	private static int LOCATION_Z_COORDINATE; // Z-Coordinate Position of Fastener
	private static String KEY_COORDINATES = "Coordinates";
	private static String JOINT_FASTENER_COORDINATES_ATT = "iPLMFastenerJoint.iPLMFastenerCoordinateValues";
	private static StringBuffer sbCoordinates = null;
	private static double TOLERANCE;
	private static boolean IS_TOLERANCE;
	private static String strFastenerTemplates = "";
	
		
	public void createJointObjects(Context context) throws Exception
	{
		MapList mlResult = new MapList();
		
		String strMQLCommand = null;
		String strMQLResult = null;		
		String strInstanceTitle = null;
		String strProduct = null;
		String strParent = null;
		String strInstanceId = null;
		String strPanelName = null;
		String strProductName = null;
		String strParentName = null;
		String strContextInstanceTitle = null;
		String strZoneCount = null;
		String strCoordinates = null;
		
		StringList tokenList = null;
		StringList tempList = null;
		StringList allInstanceList = null;
		StringList productList = null;
		StringList parentList = null;
		
		int nCnt = 0;
		int nSize = 0;
		int nOutCnt = 0;
		int nICnt = 0;
		int nCounter = 0;
		int nInstanceListSize = 0;
		int nProductListSize = 0;
		int nParentListSize = 0;
		int nAllInstanceListSize = 0;	
		
		boolean isSelfWeld = false;		
		
		try 
		{
		
			Iterator itrFastenerConnection = fastenerGlobalMapList.iterator();
			
			if(UIUtil.isNullOrEmpty(strFastenerTemplates)) {
				
				strMQLCommand = "query connection type $1 select $2 $3 dump $4";
				
				strMQLResult = MqlUtil.mqlCommand(context,strMQLCommand,FASTENER_INSTANCE_TYPE, SELECT_EXTERNAL_ID_ATTRIBUTE, "id", "|");
			} else {
				
				strMQLCommand = "query connection type $1 where \"$2\" select $3 $4 dump $5";
			
				strMQLResult = MqlUtil.mqlCommand(context,strMQLCommand,FASTENER_INSTANCE_TYPE, "!(to."+ SELECT_EXTERNAL_ID_ATTRIBUTE +" matchlist '"+ strFastenerTemplates +"' ',')" , SELECT_EXTERNAL_ID_ATTRIBUTE, "id", "|");
			}
			
			allInstanceList = FrameworkUtil.splitString(strMQLResult, "\n");
			
			debug("allInstanceList ------------  "+allInstanceList);

			nAllInstanceListSize = allInstanceList.size();		
		
			if (nAllInstanceListSize > 0) {

				while (itrFastenerConnection.hasNext()) {
					Map fastenerInfo = new HashMap();
					fastenerInfo = (Map) itrFastenerConnection.next();
					strInstanceTitle = (String)fastenerInfo.get(KEY_INSTANCE_TITLE);
					strProduct = (String)fastenerInfo.get(KEY_PRODUCT);
					strParent = (String)fastenerInfo.get(KEY_PARENT);
					strPanelName = (String)fastenerInfo.get(KEY_PANEL);
					strZoneCount = (String)fastenerInfo.get(KEY_ZONECOUNT);
					strCoordinates = (String)fastenerInfo.get(KEY_COORDINATES);
					debug("strProduct --- 177 ---  "+strProduct);
					for( nCounter = 0 ; nCounter < nAllInstanceListSize ; nCounter++ )
					{				
						tokenList = FrameworkUtil.splitString((String)allInstanceList.get( nCounter ), "|");
						
						strContextInstanceTitle = (String)tokenList.get(1);
						debug("strContextInstanceTitle  :"+strContextInstanceTitle);
						debug("strInstanceTitle  :"+strInstanceTitle);
						if(strContextInstanceTitle!=null && !strContextInstanceTitle.equals("") && strContextInstanceTitle.equals(strInstanceTitle))
						{		
							debug("Instance Name Found");
							
							strInstanceId = (String)tokenList.get(2);
							
							debug("Instance=="+strInstanceId);
							
							globalInstanceList.add(strInstanceId);							
							
							strMQLCommand = "print connection $1 select $2 dump $3";
											
							strMQLResult = MqlUtil.mqlCommand(context,strMQLCommand,strInstanceId,"from.attribute["+KEY_PLM_EXTERNAL_OR_NAME+"]","|");
							
							productList = FrameworkUtil.splitString(strMQLResult, "\n");
							
							nProductListSize = productList.size();
							
							debug("productList=="+productList);
							
							for( nCnt = 0 ; nCnt < nProductListSize ; nCnt++ )
							{
								strProductName = (String)productList.get( nCnt );
								
								debug("strProductName=="+strProductName);
								
								debug("strProduct=="+strProduct);
															
								if(strProduct.equals(strProductName))
								{
									debug("Product Name Matched"+strProductName);
																									
									getFastenerJointAndUsage( context,strInstanceId,strPanelName,strZoneCount,strCoordinates); 
								}	
							}
						}					
					}
				}
			}
		} catch (Exception e) {
				debug("Error in createJointObjects! " + e);
				e.printStackTrace();
		}	
	}
	
	
	boolean checkSelfWeld( String strPanelName )
	{
		String[] tokens;
		int index = 0;
		int nLength = 0;
		boolean bSelfWeldCase = false;
		
		if (strPanelName.contains(";")) {
			
			tokens = strPanelName.split(";");
			
			nLength = tokens.length;
			
			String strContextPanel = tokens[index];
			
			debug("strContextPanel=="+strContextPanel);	
			
			String strToken = null;
			
			for ( index = nLength-1; index >= 0; index--) {
				
				debug("token=="+tokens[index]);	
				strToken = tokens[index];
				if( strToken.equals(strContextPanel) )
				{
					bSelfWeldCase = true;
				}
				else
				{			
					return false;
				}
			}
				return bSelfWeldCase;
		}
		else 
			return true;
	}
	
	
	/**
	 * Method setJointNumber, creates new fastner joint number. Basically incremetns the last 
	 * joint number by 1.
	 * 
	 * @throws 	Exception
	 * @return 	void
	 */
	
	private void setJointNumber ( )
	{
		//  Parse all of the current iPLMFastenerJoint objects to get the last iPMJointNumber attribute.
		iFastenerJointNumber = iFastenerJointNumber + 1;
		
		strCurrentJointNumber = String.format("%05d",iFastenerJointNumber);
		
		return;
	}
	
	
	/**
	 * Method getJointName, returns the iPLM project based unique name for the iPLMFastenreJoint object.
	 * This is used for the PLMExternal_ID.
	 * 
	 * @throws 	Exception.
	 * @return	String.
	 */
	
	private String getJointName ( String strJointNum )
	{
		return strUserProject + "-" + strJointNum;
	}
	
	
	private void createNewFastenerJointObjects ( Context context, String strPanelNames, String strInstanceId, String strZoneCount, String strCoordinates)
	{
		//  Local method variables.
		DomainObject doFastenerJoint;
		String strFastenerJointBusObjectId = "";
		String strFastenerJointName = "";
		String strFirstJointNumber = "";
		Map<String,String> mpFastenerJointAttributes = new HashMap<String,String>();
		
		try {
	
			//  Create two new iPLMFastenerJoint objects for each of the two localIndex values {0,1}.
			//  Represents joints for both nominated pilot part direction and reverse side entry direction.
			
			boolean isSelfWeld = checkSelfWeld( strPanelNames );
			boolean is2TOr4T = false;
			
			debug( "In craete joints =="+strPanelNames);
			debug( "Self Weld ?=="+isSelfWeld);
			
			if( isSelfWeld )
			{
				String[] tokens;
				int nLength = 0;
				int index = 0;	
				
				if (strPanelNames.contains(";")) {
					tokens = strPanelNames.split(";");
					nLength = tokens.length;	
					if( nLength==2 || nLength==4 )
					{
						is2TOr4T = true;
						strPanelNames = reverseMultivaluatedList(strPanelNames);
					}
				}
			}
			debug( "Is this a 2T or 4T ?=="+is2TOr4T);
			debug( "Panel Name ?=="+strPanelNames);
			
			for (int iLocalIndex : JOINT_LOCAL_INDEXES) {
				
				String strLocalPanelPhysIds = "";
				String strLocalPanelNames = "";
				
				StringList slJointData = new StringList();
				
				int iJointRunningNumber = 0;
				
				//  Create the new iPLMFastenerJoint object and get its BusId
				doFastenerJoint = new DomainObject();

				//  Set iFastenerJointNumber to be a 5 character field string, padded leading 0.
				setJointNumber();
				
				if (iLocalIndex == 0) {
					strFirstJointNumber = strCurrentJointNumber;
				}				
				strFastenerJointName = getJointName(strCurrentJointNumber);
				
				debug( "strFastenerJointName ?=="+strFastenerJointName);
				
				doFastenerJoint.createObject(context,FASTENER_JOINT_TYPE,strFastenerJointName,FASTENER_JOINT_REVISION,FASTENER_JOINT_POLICY,context.getVault().toString());
			
				nTotalJointCreated++;
				strFastenerJointBusObjectId = doFastenerJoint.getInfo(context,DomainConstants.SELECT_ID);				                                                         
				debug( "strFastenerJointBusObjectId  -----358---- "+strFastenerJointBusObjectId);
				if (iLocalIndex == 0)
				{			
					StringTokenizer stPanelIds = new StringTokenizer(strPanelNames,";");
					iJointRunningNumber = stPanelIds.countTokens();
					strLocalPanelPhysIds = strPanelNames;
					strLocalPanelNames = strPanelNames;
				} else {
					
					iJointRunningNumber = 0;
					strLocalPanelPhysIds = reverseMultivaluatedList(strPanelNames);
					strLocalPanelNames = reverseMultivaluatedList(strPanelNames);
					debug( "Reversed Panel Names ?=="+strLocalPanelNames);
				}
								                                                                   
				String strJointRunningNumber = String.format("%03d",0);
				debug( "strJointRunningNumber  ---374------ "+strJointRunningNumber);
				debug( "strCurrentJointNumber  ---375------ "+strCurrentJointNumber);
				debug( "joint Index  ---376------ "+Integer.toString(iLocalIndex));
				debug( "strLocalPanelPhysIds  ---377------ "+strLocalPanelPhysIds);
				debug( "strLocalPanelNames  ---378------ "+strLocalPanelNames);
				/*                                                                    
				  Add attributes only required at iPLMFastenerJoint object creation.
				  Add the iPLMPanelPhysIds and iPLMPanelPhysIds attributes to the iPLMFastenerJoint object.
				  Add the iJointRunningNumber attributes to the iPLMFastenerJoint object.
				  Add the last joint running number for the newly created iPLMFastenerJoint.
				  This will get updated with subsequent usage of the iPLMFastenerJoint.
				  Only created for the first of the two iPLMFastenerJoint object being created.
				*/   				

				mpFastenerJointAttributes.put(EXTERNAL_ID_ATTRIBUTE, strFastenerJointName);
				mpFastenerJointAttributes.put(JOINT_NUMBER_ATT, strCurrentJointNumber);
				mpFastenerJointAttributes.put(JOINT_INDEX_ATT,Integer.toString(iLocalIndex));
				mpFastenerJointAttributes.put(JOINT_PANEL_PHYSICALIDS_ATT,strLocalPanelPhysIds);
				mpFastenerJointAttributes.put(JOINT_PANEL_NAMES_ATT,strLocalPanelNames);
				mpFastenerJointAttributes.put(JOINT_LAST_RUNNING_NUMBER_ATT,strJointRunningNumber);
				doFastenerJoint.setAttributeValues(context, mpFastenerJointAttributes);
				
				if (iLocalIndex == 0 && is2TOr4T == false)
				{
					updateFastenerInstance ( context, strInstanceId, strZoneCount,strCoordinates);
				}
				else if( iLocalIndex == 1 && is2TOr4T == true)
				{
					updateFastenerInstance ( context, strInstanceId, strZoneCount,strCoordinates);
				}
			}
		} catch (Exception e) {
			debug("Error in createNewFastenerJointObjects! " + e);
			e.printStackTrace();
		} 
		return;
	}
	
	
	/**
	 * Method reverseMultivaluatedList, reverses the order of a multivaluated list by
	 * decomposing into the components delimited by the ";" character.
	 * 
	 * @param 	strInputMVList as String.
	 * @return 	reversed multivaluate list as String.
	 */
	
	private String reverseMultivaluatedList ( String strInputMVList )
	{
		//  Local method variables.
		String strReversedMVList = "";
		String strDelimiterString = "";
		String[] tokens;
		int nLength = 0;
		int index = 0;	
		boolean bSelfWeldCase = false;
		
		if (strInputMVList.contains(";")) {
			tokens = strInputMVList.split(";");
			nLength = tokens.length;
			
			//Check whether we have Self Welding Case
			String strContextPanel = tokens[index];			
			String strToken = null;
			bSelfWeldCase = checkSelfWeld(strInputMVList);
			
			if( bSelfWeldCase == true )
			{
				//Self Welding Case : Self to Self Weld
				if ( nLength == 2 )
				{
					strReversedMVList = strContextPanel;
				}
				//Self Welding Case : Self to Self to Self Weld
				else if ( nLength == 3 )
				{
					strReversedMVList = strContextPanel + ";" + strContextPanel + ";" + strContextPanel + ";" + strContextPanel;
				}
				//Self Welding Case : Self to Self to Self to Self Weld
				else if ( nLength == 4 )
				{
					strReversedMVList = strContextPanel + ";" + strContextPanel + ";" + strContextPanel;
				}
			}
			else
			{
				int iLastIndex = nLength - 1;
				for ( index = iLastIndex; index >= 0; index--) {
					strToken = tokens[index];
					strReversedMVList = strReversedMVList + strDelimiterString + strToken;
					strDelimiterString = ";";
				}
			}
		}
		//Self Welding Case : Single Part Weld
		else
		{
			strReversedMVList = strInputMVList + ";" + strInputMVList;
		}
		return strReversedMVList;
	}
	
	
	private boolean jointExists ( String strPanelPhysIdsToFind, TreeMap jointObjectMap)
	{	
		//  Local method variables.
		Set jointKeys = jointObjectMap.keySet();
		Iterator itrJointData = jointKeys.iterator();
		
		if (jointKeys.isEmpty()) {
			return false;
		}
		
		while (!jointKeys.isEmpty() && itrJointData.hasNext()) {
			
			String strExistingJointNumber = (String) itrJointData.next();
			
			StringList slExistingJointData = (StringList) jointObjectMap.get(strExistingJointNumber);
			
			String strExistingPanelPhysicalIds = (String) slExistingJointData.get(6);
			
			if ((strExistingPanelPhysicalIds.equals(strPanelPhysIdsToFind))) {
				strCurrentJointNumber = strExistingJointNumber;
				return true;
			}
		}		
		return false;
	}
	
	private String getJointLastRunningNumber ( Context context, String strJointNum ) 
	{
		// Get the current last running number for joint.
		String strMQLCommand = null;
		String strCurrentJointRunningNum = null;
		
		try {
			
			strMQLCommand = "print bus $1 $2 $3 select $4 dump";
			strCurrentJointRunningNum = MqlUtil.mqlCommand(context,strMQLCommand, FASTENER_JOINT_TYPE, getJointName(strJointNum), FASTENER_JOINT_REVISION, "attribute[" + JOINT_LAST_RUNNING_NUMBER_ATT + "]");
			debug("strCurrentJointRunningNum ------512------  " + strCurrentJointRunningNum);
			if (strCurrentJointRunningNum.length() == 3) {
				return strCurrentJointRunningNum;
			}
			
		} catch (Exception e) {
			debug("Error in getJointLastRunningNumber! " + e);
		}
			
		return strCurrentJointRunningNum;
	}
		
	private void updateRunningNumber ( Context context, String strSpotInstPhysId, String strUniqueJointNumber, String strZoneCount, String strCoordinates )
	{
		//  Local method variables.
		String strMQLCommand = null;
		String strSetSpotInstRunningNumber = null;
		String strSetJointRunningNumber = null;
		String strSetJointCoordinates = null;
		String strSetZoneCount = null;
		
		try {
	
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);

			strMQLCommand = "print connection $1 select $2 dump $3";	
			
			String strJointId = MqlUtil.mqlCommand(context,strMQLCommand,strSpotInstPhysId,"attribute[" + SPOTFASTENER_JOINT_ID_ATT + "]","|");
			
			debug("strJointId  " + strJointId);
			
			if( strJointId.equals("") || strJointId==null || strJointId.equals("null") || !strJointId.equals(strCurrentJointNumber)) {
				
				strMQLCommand = "mod connection $1 $2 $3 "; 
				
				String strSpotInstJointNumber = MqlUtil.mqlCommand(context,strMQLCommand,strSpotInstPhysId,SPOTFASTENER_JOINT_ID_ATT,strCurrentJointNumber);
			}		
						
			strMQLCommand = "print connection $1 select $2 dump $3";
			
			String strCurrentSpotInstRunningNumber = MqlUtil.mqlCommand(context,strMQLCommand,strSpotInstPhysId,"attribute[" + SPOTFASTENER_UNIQUE_FIXING_ID_ATT + "]","|");
			
			debug("strCurrentSpotInstRunningNumber " + strCurrentSpotInstRunningNumber);
			
			 //If the Running is not updated on Instance, then only modify the Rivet Id
		    if( strCurrentSpotInstRunningNumber.equals("") || strCurrentSpotInstRunningNumber==null || strCurrentSpotInstRunningNumber.equals("null")  || !strJointId.equals(strCurrentJointNumber)) {
			
				nTotalFastnereUsageUpdate++;				
				
				String strLastJointRunningNum = getJointRunningNumOfMatchedCoordinates(context,strCurrentJointNumber,strCoordinates);
				debug("strLastJointRunningNum -----562------ " + strLastJointRunningNum);
				if(!"".equals(strLastJointRunningNum) && strLastJointRunningNum != "") {
					
					strMQLCommand = "mod connection $1 $2 $3 ";
					
					strSetSpotInstRunningNumber = MqlUtil.mqlCommand(context,strMQLCommand,strSpotInstPhysId,SPOTFASTENER_UNIQUE_FIXING_ID_ATT,strLastJointRunningNum);
					
				}
				else {
				//  Update SpotInstance object iPLMUniqueFixingID attribute to updated joint last running number.
					strMQLCommand = "mod connection $1 $2 $3 ";
					
					strSetSpotInstRunningNumber = MqlUtil.mqlCommand(context,strMQLCommand,strSpotInstPhysId,SPOTFASTENER_UNIQUE_FIXING_ID_ATT,strUniqueJointNumber);
										
					strMQLCommand = "mod bus $1 $2 $3 $4 $5";
					
					strSetJointRunningNumber = MqlUtil.mqlCommand(context,strMQLCommand,FASTENER_JOINT_TYPE,getJointName(strCurrentJointNumber),FASTENER_JOINT_REVISION,JOINT_LAST_RUNNING_NUMBER_ATT,strUniqueJointNumber);
					
					
					String coordinateVal = strUniqueJointNumber+":"+strCoordinates;
					
					if(sbCoordinates!=null ) {
						
						sbCoordinates.append(coordinateVal);
						
					} else {
						
						sbCoordinates = new StringBuffer(coordinateVal);
					}
					debug("sbCoordinates -----582------ " + sbCoordinates.toString());
					strMQLCommand = "mod bus $1 $2 $3 $4 $5";
					
					strSetJointCoordinates = MqlUtil.mqlCommand(context,strMQLCommand,FASTENER_JOINT_TYPE,getJointName(strCurrentJointNumber),FASTENER_JOINT_REVISION,JOINT_FASTENER_COORDINATES_ATT,sbCoordinates.toString());
				
				}
				
				//Update Zone Count
				strMQLCommand = "mod connection $1 $2 $3 ";
				
				strSetZoneCount = MqlUtil.mqlCommand(context,strMQLCommand,strSpotInstPhysId,JOINT_ZONE_COUNT,strZoneCount);
			
			}
						
			ContextUtil.popContext(context);
						
		} catch (Exception e) {
			debug("Error in updateRunningNumber! " + e);
		}
		
		return;
	}
	
	private void updateFastenerInstance ( Context context, String strInstanceId, String strZoneCount,String strCoordinates )
	{
		try
		{
		
			String strJointRunningNumber = "";
			
			String strcurrentJointRunningNumber = getJointLastRunningNumber(context,strCurrentJointNumber);
			
			int iCurrentJointRunningNumber = Integer.parseInt((String) strcurrentJointRunningNumber)+1;
			
			strJointRunningNumber = String.format("%03d",iCurrentJointRunningNumber);
			
			updateRunningNumber(context,strInstanceId,strJointRunningNumber, strZoneCount,strCoordinates);
		}
		catch( Exception e )
		{
			debug("Exception : "+e);
		}
	}
	
	public void getFastenerJointAndUsage( Context _context, String strInstanceId, String strPanelName, String strZoneCount, String strCoordinates)
	{
		try
		{
	
			boolean bNewJointReqd = false;				
					
			TreeMap jointObjectMap = getReportJointObjects ( _context );
			bNewJointReqd = !jointExists( strPanelName, jointObjectMap);
			
			debug("strPanelName=="+strPanelName);
			debug("bNewJointReqd=="+bNewJointReqd);
			
			if (bNewJointReqd) 
			{				
					createNewFastenerJointObjects( _context, strPanelName, strInstanceId, strZoneCount, strCoordinates);
			}
			else
			{
				updateFastenerInstance(_context, strInstanceId, strZoneCount, strCoordinates );
			}
		}
		catch( Exception e )
		{
			debug("Exception : "+e);
		}
	}
	
	/**
	 * readFastenerReport
	 *
	 *
	 *
	 *
	 */
	
	public String readFastenerReport(Context _context,String strPathName) throws Exception
	{
		String strSuccessMessage = "";
		String originalline = null;
		
		try
		{

			File inputFile = new File (strPathName);
			
			if (inputFile.exists())
			{
				if(isLatestFile(inputFile))
				{	
					
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(strPathName), "UTF-8"));
					String line = br.readLine();//Skip First LIne
					line = br.readLine();			
					StringList tokenList = null;
					String strZoneCount = null;
					StringList productHierarchyList = null;
					StringList instanceNameList = null;
					int nZoneCount = 0;
					int nCnt = 0;
					int nLocation = 0;
					int nSize = 0;
					String strPanelNames = "";
					String strPrependString = "";
					String strName = null;
					String strFastenerInstanceTitle = null;
					String strFastenerUnderProduct = null;
					StringList tempList = null;
					String strFastenerType = null;
					boolean isGenericPanel = false;
					boolean bStartReading = false;
					
					String Xcoordinate = "";
					String Ycoordinate = "";
					String Zcoordinate = "";
					String coordinateVal;
					
					while((line = br.readLine()) != null)
					{
						if (line.equalsIgnoreCase("//Fastener_DETAILS_START")){
							bStartReading = true;
							continue;
						}						
						if (line.equalsIgnoreCase("//Fastener_DETAILS_END"))
							break;
						
						if (bStartReading) {
							try {
								//Fastener Map with FastenerInstanceName, Panel Names, Zone Count?
								strPanelNames = "";
								strPrependString = "";
								Map fastenerMap = new HashMap();
								originalline = line;
								isGenericPanel = false;
								int n = 0;
								tokenList = FrameworkUtil.splitString(line, "\t");

								strFastenerType = (String)tokenList.get(LOCATION_FEATURE_TYPE);
								
								if( strFastenerType.equals( "Fst_SpotAssy" ) )
								{
									strZoneCount = (String)tokenList.get(LOCATION_ZONE_COUNT);
									nZoneCount = Integer.parseInt(strZoneCount);
									nLocation = LOCATION_FP_DN_Zone1;
									
									for(nCnt = 0; nCnt < nZoneCount ; nCnt++)
									{
										
										strName = (String)tokenList.get(nLocation);
										if( strDisplayNameDelimiter==null || strDisplayNameDelimiter.equals("")  || strDisplayNameDelimiter.equals(" "))
										{
											tempList = FrameworkUtil.splitString(strName, " "); //change it to delimiter
										}
										else
										{
											tempList = FrameworkUtil.splitString( strName, strDisplayNameDelimiter );
										}
										strName = (String)tempList.get(0);
										if (strName.startsWith("JLR-"))
										{
											isGenericPanel = true;
											debug("Excluding ==>"+strName);
											break;
										}
										else
										{
											strPanelNames = strPanelNames + strPrependString + strName;
											strPrependString = ";";
											nLocation = nLocation + 20;
										}
									}
									if( isGenericPanel == false)
									{
										fastenerMap.put(KEY_ZONECOUNT,strZoneCount);
										
										fastenerMap.put(KEY_PANEL,strPanelNames);
										
										strFastenerInstanceTitle = (String)tokenList.get(LOCATION_FASTENER_INSTANCE_TITLE);
										
										instanceNameList = FrameworkUtil.splitString(strFastenerInstanceTitle, " 0/");
										
										fastenerMap.put( KEY_INSTANCE_TITLE,(String)instanceNameList.get(0));
										
										strFastenerUnderProduct = (String)tokenList.get(LOCATION_FASTENER_PRODUCT);
										
										productHierarchyList = FrameworkUtil.splitString(strFastenerUnderProduct, "!");
										
										nSize = productHierarchyList.size();
										
										if( nSize > 0 )
										{					
											if( strDisplayNameDelimiter==null || strDisplayNameDelimiter.equals("")  || strDisplayNameDelimiter.equals(" "))
												tempList = FrameworkUtil.splitString((String)productHierarchyList.get(nSize-1), " "); // change it to delimiter
											else
												tempList = FrameworkUtil.splitString((String)productHierarchyList.get(nSize-1), strDisplayNameDelimiter);
											
											strName = (String)tempList.get(0);
											fastenerMap.put(KEY_PRODUCT,strName);					
										}
										
										if( nSize > 1 )
										{					
											if( strDisplayNameDelimiter==null || strDisplayNameDelimiter.equals("")  || strDisplayNameDelimiter.equals(" "))
												tempList = FrameworkUtil.splitString((String)productHierarchyList.get(nSize-3), " "); //change it to delimiter
											else
												tempList = FrameworkUtil.splitString((String)productHierarchyList.get(nSize-3), strDisplayNameDelimiter); 
											
											strName = (String)tempList.get(0);
											fastenerMap.put(KEY_PARENT,strName);					
										}
										
										Xcoordinate = (String)tokenList.get(LOCATION_X_COORDINATE);
										Ycoordinate = (String)tokenList.get(LOCATION_Y_COORDINATE);
										Zcoordinate = (String)tokenList.get(LOCATION_Z_COORDINATE);
										
										if(!Xcoordinate.isEmpty() && !Ycoordinate.isEmpty() && !Zcoordinate.isEmpty()) {
											coordinateVal = Xcoordinate+"~"+Ycoordinate+"~"+Zcoordinate;
											fastenerMap.put(KEY_COORDINATES,coordinateVal);
										}
										//debug("fastenerMap  ----811----- "+fastenerMap);
										fastenerGlobalMapList.add(fastenerMap);
									}
								}
							} catch (Exception e) {
								
								MqlUtil.mqlCommand(_context,"notice $1", "Exception Occured "+e);
								debug("Exception Occured "+e);
							}
						}
					}
									
					strSuccessMessage = "Success";
					
				} else {
					strSuccessMessage = "Fastener Report for " + strUserProject + " is not latest. \\nPlease contact BIW administration to make this report available on server.";
				}
			} else {
				strSuccessMessage = "Fastener Report for " + strUserProject + " does not exist on Server. \\nPlease contact BIW administration to make this report available on server.";
			}
		}catch (Exception e)
		{
			debug("Exception Occured while reading Fastener Report "+e);
		}
		
		return strSuccessMessage;
		
	}
	
	/**
	 * Method getReportJointObjects, returns the iPLM project based unique name for the iPLMFastenreJoint object.
	 * This is used for the PLMExternal_ID.
	 * 
	 * @param   Context context.
	 * @param   args as String arrary of command line arguments.
	 * @throws 	Exception.
	 * @return	MapList.
	 */
	
	public TreeMap getReportJointObjects ( Context context) throws Exception
	{
		//  Local method variables.
		String strMQLCommand = null;
		String strFastenerJointSearchName = null;
		TreeMap tmiPLMFastenerJointsData = new TreeMap();
		debug("strUserProject=="+strUserProject);
		strFastenerJointSearchName = " " + strUserProject + "-* ";
		
		try {
			// debug("Inside method getReportJointObjects -------");
						
			/*strMQLCommand = "temp query bus $1 $2 $3 select $4 $5 $6 $7 $8 dump $9 recordsep $10"; 
			String strExistingFastenerJointsData = MqlUtil.mqlCommand(context,strMQLCommand, FASTENER_JOINT_TYPE, strFastenerJointSearchName, FASTENER_JOINT_REVISION, "attribute[" + JOINT_NUMBER_ATT + "]", "attribute[" + JOINT_INDEX_ATT +"]", "attribute[" + JOINT_LAST_RUNNING_NUMBER_ATT +"]", "attribute[" + JOINT_PANEL_PHYSICALIDS_ATT + "]", "attribute[" + JOINT_PANEL_NAMES_ATT +"]", "|", "^");*/
			
			strMQLCommand = "temp query bus " + FASTENER_JOINT_TYPE + strFastenerJointSearchName + FASTENER_JOINT_REVISION + " select " +
					        "attribute[" + JOINT_NUMBER_ATT + "] " +
					        "attribute[" + JOINT_INDEX_ATT +"] " +
					        "attribute[" + JOINT_LAST_RUNNING_NUMBER_ATT +"] " +
					        "attribute[" + JOINT_PANEL_PHYSICALIDS_ATT + "] " +
					        "attribute[" + JOINT_PANEL_NAMES_ATT +"] " +
					        "dump | recordsep ^;";  
			
			
String strExistingFastenerJointsData = MqlUtil.mqlCommand(context,strMQLCommand);

			debug("strExistingFastenerJointsData ----864----- " + strExistingFastenerJointsData);
			
			StringTokenizer stJointsData = new StringTokenizer(strExistingFastenerJointsData,"^");
			while (stJointsData.hasMoreTokens()) {
				
				String strJointsData = stJointsData.nextToken();
				
				StringList slJointData = FrameworkUtil.split(strJointsData,"|");
				
				if (slJointData.size() == 8) {
					
					String strJointNumber = (String) slJointData.get(3);
					tmiPLMFastenerJointsData.put(strJointNumber,slJointData);
				}
			}
			
			if (!tmiPLMFastenerJointsData.isEmpty()) {
				//  Get the last iPLMFastenerJoint joint number as an integer.
				iFastenerJointNumber = Integer.parseInt((String) tmiPLMFastenerJointsData.lastEntry().getKey());
			}
			
		} catch (Exception e) {
			debug("Error in getExistingFastenerJointObjects! " + e);
		}
	
		return tmiPLMFastenerJointsData;
	}
	
	/**
	 * isLatestFile
	 * @arg inputFile
	 *		java.io.File object to check
	 * @return bLatest
	 *		Boolean. True if the file was modified within last 12 Hours else False.
	 *
	 */
	 
	 public boolean isLatestFile(File inputFile) {
		
		Boolean bLatest = false;
		
		// Get File Modified Date in millis
		Long lLastModified = inputFile.lastModified();
		
		// Get Current Date in millis
		Date CurrentDate = new Date();
		long lCurrentDate = CurrentDate.getTime();
		
		if ((lCurrentDate - lLastModified) > 43200000) {
				
			debug("Files was created more than 12 Hours ago");
			
		} else {
			debug("Files was created in last 12 hours");
			bLatest = true;
		}
		
		return bLatest;
	 }
	 
	  /**
	 * Fastener Joint Automation
	 *
	 *
	 *
	 */
	 
	 public void readFastenerFiles (Context _context, String []args) throws Exception 
	 {

		 readPageObject(_context);
		 File inputDir = new File (strPathName);
		 String strFileActualName = "";
		 // Check if directory exists
		 System.out.println("2 : inputDir.exists()   " + inputDir.exists());
		 if (inputDir.exists()) 
		 {
			 File[] files = inputDir.listFiles();
			//If this pathname does not denote a directory, then listFiles() returns null. 

			for (File file : files) {
			 	if (file.isFile()) {
					strFileActualName = file.getName();
					if (getExtension(strFileActualName).equalsIgnoreCase("txt")) {
						if (strFileActualName.contains("-"))
							strUserProject = strFileActualName.split("-")[0];
						else
							strUserProject = strFileActualName.split("\\.")[0]; 
						try {
							bwLogFileWriter = new BufferedWriter(new FileWriter(strLogPath + "Logs_" + strUserProject + "_" + simpleDateFormat.format(new Date()) + ".log" ));
						} catch (Exception e) {
							debug("Error occurred while initializing logs");
						}
						
						createFastenerJointsAutomation(_context, strFileActualName);
					
					} else {
						debug(strFileActualName + " is not a txt file. Skipping....");
					}
				}
			}
		 }
	 }
	 
	 /**
	 * Fastener Joint Automation
	 *
	 *
	 *
	 *
	 */
	 
	 
	public MapList createFastenerJointsAutomation(Context _context, String strFileName) throws Exception
	{
		MapList mlResult = new MapList();
		
		String strMessage = readFastenerReport(_context,strPathName + strFileName);
		
		if (strMessage != null && strMessage.equalsIgnoreCase("Success"))
		{
			createJointObjects(_context);
		
			//  Output JSP Alert Box messages.
			String strResult = "";
			
			String strCreateJointsResult = "";
			
			if( !fastenerGlobalMapList.isEmpty() )
			{
				strResult = "*** Create/Update iPLMFastener joints completed sucessfully. ***        ";
				strCreateJointsResult = strResult + "\\n\\n";
				strResult = "  Successfully created " + nTotalJointCreated + " fastener joints for";
				strCreateJointsResult = strCreateJointsResult + strResult + "\\n";
				strResult = "  file " + strFileName + ".";
				strCreateJointsResult = strCreateJointsResult + strResult + "\\n\\n";
				strResult = "  Updated joint attributes in " + nTotalFastnereUsageUpdate + " fastener instances.";
				strCreateJointsResult = strCreateJointsResult + strResult + "\\n";
				strFinalAlertMessage = strFinalAlertMessage + strCreateJointsResult;
			} 
			else {
				strFinalAlertMessage = "  No fasteners found in file " + strFileName + ".";
			}
		} 
		else {
			strFinalAlertMessage = strMessage;
		}
		
		mlResult.add(strFinalAlertMessage);
			
		return mlResult;
	}
	
	/**
	*
	*
	*
	*
	*
	*/
	protected static String getExtension (String strName) {
		
		if(strName.contains("."))
			return strName.substring(strName.lastIndexOf(".") + 1);
		else
			return strName;
	}	

	/**
	 * Method to Generate Logs
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param message holds string to print
	 * @throws Exception if operation fails
	 * @grade 0
	 */

	protected static void debug(String message) 
	{
		String strDate = simpleDateFormat.format(new Date());
		
		if (bwLogFileWriter != null)
		{
			try 
			{
				bwLogFileWriter.write("[" + strDate + "] INFO - " + message + "\r\n");
				bwLogFileWriter.flush();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		} 
		else {
			System.out.println("[" + strDate + "] INFO - " + message);
		}
		
	}

	
	public void readPageObject( Context _context )
	{
		try
		{
			
			String pageargs = "iPLMFastenerJointInformation";
			
			Properties propNotification = readPageObjectContent(_context, pageargs);
			strPathName = (String)propNotification.getProperty("PROGRAM_FILE_LOC");
			debug("strPathName " + strPathName);
			
			strLogPath = (String)propNotification.getProperty("LOG_FILE_LOC");
			debug("strLogPath " + strLogPath);
			
			LOCATION_FP_DN_Zone1 = Integer.parseInt((String)propNotification.getProperty("LOCATION_FP_DN_Zone1"));
			debug("LOCATION_FP_DN_Zone1 " + LOCATION_FP_DN_Zone1);
			
			LOCATION_FASTENER_INSTANCE_TITLE = Integer.parseInt((String)propNotification.getProperty("LOCATION_FASTENER_INSTANCE_TITLE"));
			debug("LOCATION_FASTENER_INSTANCE_TITLE " + LOCATION_FASTENER_INSTANCE_TITLE);
			
			LOCATION_FASTENER_PRODUCT = Integer.parseInt((String)propNotification.getProperty("LOCATION_FASTENER_PRODUCT"));
			debug("LOCATION_FASTENER_PRODUCT " + LOCATION_FASTENER_PRODUCT);
			
			strDisplayNameDelimiter = (String)propNotification.getProperty("DisplayNameDelimiter");
			debug("strDisplayNameDelimiter " + strDisplayNameDelimiter);
			
			KEY_PLM_EXTERNAL_OR_NAME = (String)propNotification.getProperty("KEY_PLM_EXTERNAL_OR_NAME");
			debug("KEY_PLM_EXTERNAL_OR_NAME " + KEY_PLM_EXTERNAL_OR_NAME);
			
			LOCATION_ZONE_COUNT = Integer.parseInt((String)propNotification.getProperty("LOCATION_ZONE_COUNT"));
			debug("LOCATION_ZONE_COUNT " + LOCATION_ZONE_COUNT);
			
			LOCATION_FEATURE_TYPE = Integer.parseInt((String)propNotification.getProperty("LOCATION_FEATURE_TYPE"));
			debug("LOCATION_FEATURE_TYPE " + LOCATION_FEATURE_TYPE);
			
			LOCATION_X_COORDINATE = Integer.parseInt((String)propNotification.getProperty("LOCATION_X_COORIDATE"));
			debug("LOCATION_X_COORDINATE " + LOCATION_X_COORDINATE);
			
			LOCATION_Y_COORDINATE = Integer.parseInt((String)propNotification.getProperty("LOCATION_Y_COORIDATE"));
			debug("LOCATION_Y_COORDINATE " + LOCATION_Y_COORDINATE);
			
			LOCATION_Z_COORDINATE = Integer.parseInt((String)propNotification.getProperty("LOCATION_Z_COORIDATE"));
			debug("LOCATION_Z_COORDINATE " + LOCATION_Z_COORDINATE);
			
			TOLERANCE = Double.parseDouble((String)propNotification.getProperty("TOLERANCE_VALUE"));
			debug("TOLERANCE VALUE " + TOLERANCE);
			
			IS_TOLERANCE = Boolean.parseBoolean((String)propNotification.getProperty("IS_TOLERANCE"));
			debug("IS_TOLERANCE " + IS_TOLERANCE);
			
			 strFastenerTemplates = (String)propNotification.getProperty("FASTENER_TEMPLATE_LIST");
			 debug("strFastenerTemplates " + strFastenerTemplates);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	

private	String getJointRunningNumOfMatchedCoordinates (Context context, String strJointNum, String strCoordinates ) 
{
	// Get the current last running number for joint.
	String strMQLCommand = null;
	String strLastJointRunningNum = "";
	String FastenerDlts = null;
	StringList FastenerDltsList = new StringList();
	sbCoordinates = new StringBuffer();
	
	try {
		
		strMQLCommand = "print bus $1 $2 $3 select $4 dump $5";
		
		FastenerDlts = MqlUtil.mqlCommand(context,strMQLCommand, FASTENER_JOINT_TYPE, getJointName(strJointNum),FASTENER_JOINT_REVISION, "attribute[" + JOINT_FASTENER_COORDINATES_ATT + "]", "|" );
		
		if(UIUtil.isNotNullAndNotEmpty(FastenerDlts)) {
			
			FastenerDltsList = FrameworkUtil.split(FastenerDlts, ",");
			
			for(Object sCoordinateAttr : FastenerDltsList) 
			{				
				if(sCoordinateAttr.toString()=="") { continue; }
				
				sbCoordinates.append(sCoordinateAttr.toString());
				
				sbCoordinates.append(",");
				
				StringList slCoordinatesAttr = FrameworkUtil.split(sCoordinateAttr.toString(), ":");
				
				String sCoordinates = slCoordinatesAttr.get(1).toString();
				
				String sLastRunningNum = slCoordinatesAttr.get(0).toString();
				
				if(IS_TOLERANCE == false) 
				{
					strLastJointRunningNum = getLstRuningNoIfToleranceFalse(sCoordinates,sLastRunningNum,strCoordinates);
					
					if(!"".equals(strLastJointRunningNum)) { break; }
					
				}  
				else {
					double tolerance = calculateTolerance(sCoordinates,strCoordinates);
					
					if (tolerance <= TOLERANCE) {
						strLastJointRunningNum = sLastRunningNum;
						break;
					}
				}
			}
		}
	} catch (Exception e) {
		debug("Error in getCoordinates method " + e.getLocalizedMessage());
		e.printStackTrace();
	}
	return strLastJointRunningNum;
}
		
private static double calculateTolerance(String sCoordinates, String strCoordinates) {
	
	double tolerance = 0;
	
	try {

		StringList slSplitCoordinates = FrameworkUtil.split(sCoordinates, "~");
		
		StringList slCoordinatesLogFile = FrameworkUtil.split(strCoordinates, "~");
		
		double LOCX1 = Double.parseDouble(slSplitCoordinates.get(0).toString());
		
		double LOCY1 = Double.parseDouble(slSplitCoordinates.get(1).toString());
		
		double LOCZ1 = Double.parseDouble(slSplitCoordinates.get(2).toString());
		
		double LOCX2 = Double.parseDouble(slCoordinatesLogFile.get(0).toString());
		
		double LOCY2 = Double.parseDouble(slCoordinatesLogFile.get(1).toString());
		
		double LOCZ2 = Double.parseDouble(slCoordinatesLogFile.get(2).toString());
		
		tolerance = Math.sqrt((LOCX1-LOCX2)*(LOCX1-LOCX2) + (LOCY1-LOCY2)*(LOCY1-LOCY2) + (LOCZ1-LOCZ2)*(LOCZ1-LOCZ2));
		
	}catch (Exception e) {
		
		debug("Error in calculateTolerance method " + e.getLocalizedMessage());
		e.printStackTrace();
	}
	return tolerance;
}

private static String getLstRuningNoIfToleranceFalse(String sCoordinates,String sLastRunningNum,String strCoordinates){
	
	String sLstRuningNo = "";
	
	try {
		if(UIUtil.isNotNullAndNotEmpty(sCoordinates) && UIUtil.isNotNullAndNotEmpty(strCoordinates) && sCoordinates.equalsIgnoreCase(strCoordinates)) 
			sLstRuningNo = sLastRunningNum;
	}catch(Exception e) {
		debug("Error in checkCoordinatesMatchExactly method " + e.getLocalizedMessage());
		e.printStackTrace();
	}
	return sLstRuningNo;
}
	
/**
 * Generic Method to read a Page Object.
 * @param _context
 * @param strPageObject - Name of Page Object to be read
 * @return propertyEntry - All the property entries
 * @throws Exception
 */
private static Properties readPageObjectContent(Context _context,String strPageObject) throws Exception
{
	Properties propertyEntry = new Properties();
	try
	{
		Page pageAttributePopulation = new Page(strPageObject);
		pageAttributePopulation.open(_context);
		String strProperties = pageAttributePopulation.getContents(_context);
		pageAttributePopulation.close(_context);
		InputStream input = new ByteArrayInputStream(strProperties.getBytes("UTF8"));
		propertyEntry.load(input);
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	return propertyEntry;
}


}
