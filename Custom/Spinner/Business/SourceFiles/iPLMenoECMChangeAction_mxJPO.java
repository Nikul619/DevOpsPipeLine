import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import matrix.db.Context;
import matrix.db.Page;
import matrix.db.Person;
import matrix.util.StringList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedChanges;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IRealizedChange;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedActivity;

//Added comment
public class iPLMenoECMChangeAction_mxJPO extends emxDomainObject_mxJPO  {

	 public iPLMenoECMChangeAction_mxJPO (Context context, String[] args) throws Exception
	{
		super(context, args);
	}
	
	// CR-120 : Notice number validation - Check Change Action release check : vsatyana
	public int iPLMCADReleaseCheck(Context context, String[] args) throws Exception
	{
		int iReturn = 0;
		try
		{
			String sCAId = (String) args[0];			
			if(UIUtil.isNotNullAndNotEmpty(sCAId)){
				DomainObject domCA = new DomainObject(sCAId);
				StringList slSelectList = new StringList();
				slSelectList.add(SELECT_ORGANIZATION);
				slSelectList.add("project");
				slSelectList.add(SELECT_OWNER);
				slSelectList.add(SELECT_CURRENT);
				slSelectList.add("attribute["+ChangeConstants.ATTRIBUTE_SYNOPSIS+"]");
				slSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ORDER+"'].from.id");
				slSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ORDER+"'].from.owner");
				slSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ORDER+"'].from.description");
	                        slSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ORDER+"'].from.current");
				slSelectList.add("from["+ChangeConstants.RELATIONSHIP_TECHNICAL_ASSIGNEE+"].to");
						
				Map mpCAList = domCA.getInfo(context, slSelectList);				
				
				String sLegacyNoticeNumber = (String)mpCAList.get("attribute["+ChangeConstants.ATTRIBUTE_SYNOPSIS+"]");
				String sCA_current = (String)mpCAList.get(SELECT_CURRENT);
				if (UIUtil.isNullOrEmpty(sLegacyNoticeNumber)) {
					String sErrNotice = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.InvalidNoticeNumber");
					MqlUtil.mqlCommand(context, "notice $1", new String[] { sErrNotice });
					iReturn = 1;
				} else {
					StringBuffer sbNoticeMessage = new StringBuffer();
					String strExcludeTitle = null;
					String strTitleValidPattern = null;
					String strCADExclusionList = null;	
					String strInclusionTypeList = null;	
					String strTitle = null;
					String msgNoticeNumber = null;
					String msgDuplicateCO = null;
					String msgCONotice = null;
                                        String msgCOState = null;
					String msgCA_Assignee = null;
					String msgCA_Reviewer = null;
					String msgProposedMaturity = null;
					String msgReasonForChangeMaturity = null;
					String msgChangeLevelIndicator = null;
					String msgCADExclusion = null;
					String msgProposedChanges = null;
					String msgRealizedChanges = null;
					//String msgSupplierCACheck = null;
					String msgCADMultipleRev = null;
					//String msgProposedObjectReserve = null;
					String msgProposedObjectType = null;
					boolean bExcludeTitle = false;
					
					StringList slExcludeTitleList= null;
					StringList slTitleValidPatternList= null;
					StringList slCADExclusionList= null;
					StringList slInclusionTypeList= null;
					MapList mlProposedItems = null;
					
					Properties changeActionProperties = readPageObject(context,"iPLMPReleaseConfigurations");
					if (null != changeActionProperties) {
						
						strExcludeTitle = changeActionProperties.getProperty("Notice_Exclude");
						strTitleValidPattern = changeActionProperties.getProperty("Notice_Valid_Format");
						strCADExclusionList = changeActionProperties.getProperty("ExclusionList.StructureType");
						strInclusionTypeList = changeActionProperties.getProperty("InclusionList.Type");
										
						if(UIUtil.isNotNullAndNotEmpty(strExcludeTitle)){
							slExcludeTitleList = FrameworkUtil.split(strExcludeTitle,",");
						}
						if(UIUtil.isNotNullAndNotEmpty(strTitleValidPattern)){
							slTitleValidPatternList = FrameworkUtil.split(strTitleValidPattern,",");
						}
						if(UIUtil.isNotNullAndNotEmpty(strCADExclusionList)){
							slCADExclusionList = FrameworkUtil.split(strCADExclusionList,",");
						}
						if(UIUtil.isNotNullAndNotEmpty(strInclusionTypeList)){
							slInclusionTypeList = FrameworkUtil.split(strInclusionTypeList,",");
						}
									
						if(null!=slExcludeTitleList && slExcludeTitleList.size()>0){
							for(int i=0;i<slExcludeTitleList.size();i++){
								strTitle = (String)slExcludeTitleList.get(i);
								if(sLegacyNoticeNumber.matches(strTitle)||sLegacyNoticeNumber.startsWith(strTitle)){
									bExcludeTitle = true;
								}
							}
							if(!bExcludeTitle){							
								msgNoticeNumber = validateNoticeNumber(context,sLegacyNoticeNumber,slTitleValidPatternList);
								if(!"true".equalsIgnoreCase(msgNoticeNumber)){
									sbNoticeMessage.append(msgNoticeNumber);
								}
								
								msgDuplicateCO = validateDuplicateCO(context,sLegacyNoticeNumber);
								if(!"true".equalsIgnoreCase(msgDuplicateCO)){
									sbNoticeMessage.append(msgDuplicateCO);
								}
							}
						}						
					}
									
					msgCONotice = validateCONoticeNumber(context,mpCAList);
					if(!"true".equalsIgnoreCase(msgCONotice)){
						sbNoticeMessage.append(msgCONotice);
					}

                    if (ChangeConstants.STATE_CHANGE_ACTION_PREPARE.equalsIgnoreCase(sCA_current)) {
						msgCOState = validateCOState(context,mpCAList);
						if(!"true".equalsIgnoreCase(msgCOState)){
							sbNoticeMessage.append(msgCOState);
						}
					}
					
					msgCA_Assignee = validateCA_Assignee(context,mpCAList);
					if(!"true".equalsIgnoreCase(msgCA_Assignee)){
						sbNoticeMessage.append(msgCA_Assignee);
					}
					
					msgCA_Reviewer = checkCALeaderPnO(context,domCA,mpCAList);
					if(!"true".equalsIgnoreCase(msgCA_Reviewer)){
						sbNoticeMessage.append(msgCA_Reviewer);
					}
										
					IChangeAction iChangeAction=ChangeAction.getChangeAction(context, sCAId);
					
					List<IProposedChanges> proposedChanges = iChangeAction.getProposedChanges(context);
		
					if(proposedChanges.size()>0){
						mlProposedItems = getProposedChangesAttrValues(context, proposedChanges);
						msgProposedMaturity = checkProposedItemsMaturity(context,mlProposedItems);
						
						if(!"true".equalsIgnoreCase(msgProposedMaturity)){
							sbNoticeMessage.append(msgProposedMaturity);
						}
						
						msgProposedObjectType = checkProposedObjectType(context,mlProposedItems,slInclusionTypeList);						
						if(!"true".equalsIgnoreCase(msgProposedObjectType)){
							sbNoticeMessage.append(msgProposedObjectType);
						}
						
						/*msgProposedObjectReserve = checkProposedObjectReserve(context,mlProposedItems);						
							if(!"true".equalsIgnoreCase(msgProposedObjectReserve)){
								sbNoticeMessage.append(msgProposedObjectReserve);
							}
						*/
						
						msgReasonForChangeMaturity = checkActivityChangeMaturity(context,mlProposedItems);						
						if(!"true".equalsIgnoreCase(msgReasonForChangeMaturity)){
							sbNoticeMessage.append(msgReasonForChangeMaturity);
						}
						
						//if(!bExcludeTitle){	
						msgChangeLevelIndicator = checkChangeLevelIndicator(context,mlProposedItems);								
						if(!"true".equalsIgnoreCase(msgChangeLevelIndicator)){
							sbNoticeMessage.append(msgChangeLevelIndicator);
						}
						//}											
						
						msgCADExclusion = checkCADExclusionList(context,slCADExclusionList,mlProposedItems);						
						if(!"true".equalsIgnoreCase(msgCADExclusion)){
							sbNoticeMessage.append(msgCADExclusion);
						}
						
						/*strCAOrg = (String)mpCAList.get("organization");
						// check Supplier Project for strCAOrg -- return true
						if(true){
							msgSupplierCACheck = checkSupplierCAandProposedItems(strCAOrg,mlProposedItems);
							
							if(!"true".equalsIgnoreCase(msgSupplierCACheck)){
								sbNoticeMessage.append(msgSupplierCACheck);
							}
						}*/
						
						msgCADMultipleRev = checkCADMultipleRevOnCA(context,mlProposedItems);						
						if(!"true".equalsIgnoreCase(msgCADMultipleRev)){
							sbNoticeMessage.append(msgCADMultipleRev);
						}
					} else if (!ChangeConstants.STATE_CHANGE_ACTION_PREPARE.equalsIgnoreCase(sCA_current)) {
						msgProposedChanges = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.NoProposedItemOnCA");
						sbNoticeMessage.append(msgProposedChanges);
					}
					
					List<IRealizedChange> realizedChanges = iChangeAction.getRealizedChanges(context);
					
					if(realizedChanges.size()!=0){
						msgRealizedChanges = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.NoRealizedItemOnCA");
						sbNoticeMessage.append(msgRealizedChanges);
					}
					
					if(sbNoticeMessage.length()==0){
						iReturn = 0;
					}else{
						MqlUtil.mqlCommand(context, "notice $1", new String[] { sbNoticeMessage.toString() });
						iReturn = 1;
					}
				}				
			}			
		}
		catch (Exception localException)
		{
		   localException.printStackTrace();
		   throw localException;
		}
		return iReturn;
	}

	public  String validateNoticeNumber(Context context, String sLegacyNoticeNumber,StringList slTitleValidPatternList) throws Exception {
		String sNoticeNumberMsg = "true";
		int iListSize = slTitleValidPatternList.size();
		String strTitleValidPattern= null;
		boolean flag = false;
		if(null!=slTitleValidPatternList && iListSize>0){
			for(int i=0;i<iListSize;i++){
				strTitleValidPattern = (String)slTitleValidPatternList.get(i);
				if(sLegacyNoticeNumber.matches(strTitleValidPattern)){
					flag = true;					
				}
			}
			if(!flag){
				sNoticeNumberMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.InvalidNoticeNumber");
				sNoticeNumberMsg = sNoticeNumberMsg+"\n";
			}
		}
		return sNoticeNumberMsg;
	}

	public  String validateDuplicateCO(Context context, String sLegacyNoticeNumber) throws Exception {
		String sDuplicateCO = "true";
		String sMsgDuplicate = null;
		String strType=null;
		String strName=null;
		String strRev=null;
		String strCOWhereClause = "description ~~ \""+sLegacyNoticeNumber+"*\" && "+SELECT_CURRENT+ "!=" +ChangeConstants.STATE_CHANGE_ACTION_CANCEL;
		String strCAWhereClause = "attribute["+ChangeConstants.ATTRIBUTE_SYNOPSIS+"] == '"+sLegacyNoticeNumber+"' && "+SELECT_CURRENT+ "!=" +ChangeConstants.STATE_CHANGE_ACTION_CANCEL;
		StringBuffer sbChangeObj = new StringBuffer();
		StringList strBusSelects=new StringList();
		strBusSelects.add(SELECT_ID);
		strBusSelects.add(SELECT_TYPE);
		strBusSelects.add(SELECT_NAME);
		strBusSelects.add(SELECT_REVISION);
				
		MapList mlCOList = DomainObject.findObjects(context, 
									ChangeConstants.TYPE_CHANGE_ORDER, 
									 "*", 
									 strCOWhereClause,
									 strBusSelects);
									 
		MapList mlCAList = DomainObject.findObjects(context, 
									ChangeConstants.TYPE_CHANGE_ACTION, 
									 "*", 
									 strCAWhereClause, 
									 strBusSelects);
		int iCOSize =mlCOList.size(); 
		int iCASize =mlCAList.size(); 
		
		if (iCOSize>1) {
			for(int i=0;i<iCOSize;i++){
				Map mpCOList = (Map)mlCOList.get(i);
				strType = (String)mpCOList.get(SELECT_TYPE);
				strName = (String)mpCOList.get(SELECT_NAME);
				strRev = (String)mpCOList.get(SELECT_REVISION);
				sMsgDuplicate = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.DuplicateTitlesChangeOrders");
			
				sMsgDuplicate = sMsgDuplicate.replaceAll("<COName>", strType+" "+strName+" "+strRev);
				sMsgDuplicate = sMsgDuplicate.replaceAll("<NoticeNumber>",sLegacyNoticeNumber);
			
				sbChangeObj.append(sMsgDuplicate);
				sbChangeObj.append("\n");
			}			
		}
		if (iCASize>1) {
			for(int i=0;i<iCASize;i++){
				Map mpCAList = (Map)mlCAList.get(i);
				strType = (String)mpCAList.get(SELECT_TYPE);
				strName = (String)mpCAList.get(SELECT_NAME);
				strRev = (String)mpCAList.get(SELECT_REVISION);
				sMsgDuplicate = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.DuplicateTitlesChangeActions");				
				sMsgDuplicate = sMsgDuplicate.replaceAll("<CAName>", strType+" "+strName+" "+strRev);
				sMsgDuplicate = sMsgDuplicate.replaceAll("<NoticeNumber>",sLegacyNoticeNumber);
				sbChangeObj.append(sMsgDuplicate);
				sbChangeObj.append("\n");
			}		
		}
		if (sbChangeObj.length()!=0){
			sDuplicateCO = sbChangeObj.toString();
		}
		return sDuplicateCO;
	}
	//Incident 17239 - start
	public  String validateCA_Assignee(Context context, Map mpCAList) throws Exception {
		String sAssigneeError = "true";
		String coId = (String)mpCAList.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.id");
		String sCA_Assignee = (String)mpCAList.get("from["+ChangeConstants.RELATIONSHIP_TECHNICAL_ASSIGNEE+"].to");
		
		if(UIUtil.isNotNullAndNotEmpty(coId) && UIUtil.isNullOrEmpty(sCA_Assignee)){
			sAssigneeError = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Warning.AssigneesNotAssigned");
			sAssigneeError = "-"+sAssigneeError + "\n";
		}
		return sAssigneeError;
	}
	//Incident 17239 - End

	public  String validateCONoticeNumber(Context context, Map mpCAList) throws Exception {
		String sCOError = "true";
		StringBuffer msgBuffer = new StringBuffer(); 
		String coId = (String)mpCAList.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.id");
		String coNoticeNumber = (String)mpCAList.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.description");
		String coOwner = (String)mpCAList.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.owner");
		String sLegacyNoticeNumber = (String)mpCAList.get("attribute["+ChangeConstants.ATTRIBUTE_SYNOPSIS+"]");
		String sCAOwner = (String)mpCAList.get(SELECT_OWNER);
		String sCACurrent = (String)mpCAList.get(SELECT_CURRENT);
		if(UIUtil.isNotNullAndNotEmpty(coId)){			
			StringList slbusList = new StringList();
			slbusList.addElement(SELECT_ID);
			StringList slRelSels = new StringList();
			slRelSels.addElement(SELECT_ID);
			if ( !coNoticeNumber.equalsIgnoreCase(sLegacyNoticeNumber) && !coNoticeNumber.startsWith(sLegacyNoticeNumber)) {
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.NoticeNumberNotSame");
				msgBuffer.append(strMsg);
				msgBuffer.append("\n");
			}			
			if(ChangeConstants.STATE_CHANGE_ACTION_INWORK.equals(sCACurrent) && !sCAOwner.equalsIgnoreCase(coOwner)){
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.OwnerMismatch");
				msgBuffer.append(strMsg);
				msgBuffer.append("\n");
			}
		} else {
			String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.NoChangeOrderOnCA");
			msgBuffer.append(strMsg);
			msgBuffer.append("\n");
		}
		if (msgBuffer.length()!=0){
			sCOError = msgBuffer.toString();
		}		
		return sCOError;
	}
	
	public  String validateCOState(Context context, Map mpCAList) throws Exception {
		String sCOError = "true";
		String coId = (String)mpCAList.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.id");
		String coCurrent= (String)mpCAList.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.current");
	
		if(UIUtil.isNotNullAndNotEmpty(coId) && !ChangeConstants.STATE_CHANGE_ACTION_INWORK.equals(coCurrent)){			
			sCOError = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.ConnectedCONotInInWorkState");
			sCOError = "-"+sCOError + "\n";
		}
		return sCOError;
	}
	
	public String checkProposedObjectType (Context context, MapList mlProposedItems,StringList slInclusionTypeList) throws Exception {
		String msgProposedType = "true";
		String strType = null;
		String strName = null;
		String strRev = null;
		Map mProposed = new HashMap();
		StringBuffer msgBuffer = new StringBuffer();
		int iMapListSize = mlProposedItems.size();
		for (int i=0; i<iMapListSize;i++){
			mProposed = (Map)mlProposedItems.get(i);
			strType = (String)mProposed.get("type");
			strName = (String)mProposed.get("name");
			strRev = (String)mProposed.get("revision");
			if(!slInclusionTypeList.contains(strType)){
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.InclusionListType");
				strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev);
				msgBuffer.append(strMsg);
				msgBuffer.append("\n");
			}
		}
		msgProposedType = msgBuffer.toString();
		return msgProposedType;
	}
	
	public String checkCALeaderPnO(Context context, DomainObject domCA, Map mpCAList) throws Exception {
		String sPnOError = "true";
		StringList slUser = domCA.getInfoList(context,"from["+ChangeConstants.RELATIONSHIP_CHANGE_REVIEWER+"].to");
		String sCAOrg = (String)mpCAList.get(SELECT_ORGANIZATION);
		String sCAProject = (String)mpCAList.get("project");
		StringBuffer msgBuffer = new StringBuffer();
		int iUser = slUser.size();
		boolean bReviewer = false;
		for(int i=0; i<iUser;i++){
			String sChangeReviewer = (String)slUser.get(i);
			String sRole = "ctx::VPLMProjectLeader."+sCAOrg+"."+sCAProject;
			Person person = new Person(sChangeReviewer);
			if(person.isAssigned(context, sRole)){
				bReviewer = true;
				break;
			}			
		}
		if(!bReviewer){
			String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.checkCALeaderPnO");
			//strMsg = strMsg.replaceAll("<name>",sChangeReviewer);
			msgBuffer.append(strMsg);
			msgBuffer.append("\n");
		}
		sPnOError = msgBuffer.toString();
		return sPnOError;
	}
		
	public String checkProposedItemsMaturity (Context context, MapList mlProposedItems) throws Exception {
		String msgProposedMaturity = "true";		
		String msgProposedLOC = null;
		String sProposedState = null;
		String sProposedSOwner = null;
		String strType = null;
		String strName = null;
		String strRev = null;
		StringBuffer msgBuffer = new StringBuffer();
		Map mProposed = new HashMap();
		int iMapListSize = mlProposedItems.size();
		for (int i=0; i<iMapListSize;i++){
			mProposed = (Map)mlProposedItems.get(i);
			strType = (String)mProposed.get("type");
			strName = (String)mProposed.get("name");
			strRev = (String)mProposed.get("revision");
			sProposedState = (String)mProposed.get("current");
			sProposedSOwner = (String)mProposed.get("owner");
			
			msgProposedLOC = getProposedItemLOC(context,sProposedSOwner);
			
			if (!"FROZEN".equalsIgnoreCase(sProposedState)){
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.ProposedItemMaturity");
				strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev);
				msgBuffer.append(strMsg);
				msgBuffer.append("\n");
			}
			if("TC".equalsIgnoreCase(msgProposedLOC) || "R2014".equalsIgnoreCase(msgProposedLOC)){
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.ProposedItemLOC");
				strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev);
				msgBuffer.append(strMsg);
				msgBuffer.append("\n");
			}
		}
				
		if (msgBuffer.length()!=0){
			msgProposedMaturity = msgBuffer.toString();
		}
		return msgProposedMaturity;
	}
	
	public String checkActivityChangeMaturity (Context context, MapList mlProposedItems) throws Exception {
		
		String msgActivityChangeMaturity = "true";
		String strRequestedChange = ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE;
		String strActivityChangeMaturity = "";
		String strType = null;
		String strName = null;
		String strRev = null;
		StringBuffer sbActivityChangeMaturity = new StringBuffer();
		StringList slRelSelect = new StringList(1);
		slRelSelect.addElement(strRequestedChange);
		StringList slBusSelect = new StringList(1);
		slBusSelect.addElement(SELECT_ID);
		
		int iProposedItemsSize = mlProposedItems.size();
		for (int i=0;i<iProposedItemsSize;i++) {
			Map mProposed = (Map)mlProposedItems.get(i);
			
			List<IProposedActivity> activities = (List<IProposedActivity>) mProposed.get("activities");
			int iActivitiesSize = activities.size();
			strType = (String)mProposed.get("type");
			strName = (String)mProposed.get("name");
			strRev = (String)mProposed.get("revision");
			if(iActivitiesSize>0){
				for( int idx=0;idx<iActivitiesSize;idx++)
				{
					IProposedActivity activity=activities.get(idx);					
					strActivityChangeMaturity=activity.getWhatArguments().get(1).getArgumentAsString();									
					
					if (!"Release".equalsIgnoreCase(strActivityChangeMaturity)) {
						String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.ProposedItemMaturityChange");
						strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev);
						sbActivityChangeMaturity.append(strMsg);
						sbActivityChangeMaturity.append("\n");
						break;
					}
				}
			}else{				
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.ProposedItemMaturityChange");
				strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev);
				sbActivityChangeMaturity.append(strMsg);
				sbActivityChangeMaturity.append("\n");
			}			
		}
		if(sbActivityChangeMaturity.length()>0){
			msgActivityChangeMaturity = sbActivityChangeMaturity.toString();
		}
		return msgActivityChangeMaturity;
	}
	public String getProposedItemLOC(Context context,String sOwner) throws Exception {
		String sLOC = null;
		if ("xmigratr".equalsIgnoreCase(sOwner)){
			sLOC = "TC";
		}
		return sLOC;
	}
	
	public MapList getProposedChangesAttrValues(Context context, List<IProposedChanges> proposedChanges) throws Exception {
		MapList mlProposedItems = new MapList();
			
		String sProposedId = null;
		String sProposedPhysicalId = null;
		String sProposedType = null;
		String sProposedName = null;
		String sProposedRev = null;
		String sProposedState = null;
		String sProposedOwner = null;
		String sProposedOrg = null;
		String sProposedProj = null;
		String sProposedPartCLI = null;
		String sProposedDrawingCLI = null;
		String sCADStructureType = null;
		String sReserved = null;
		String sReservedBy = null;
		DomainObject proposedObj = new DomainObject();
		
		StringList localStringList1 = new StringList();
		localStringList1.add("id");
		localStringList1.add("physicalid");
		localStringList1.add("type");
		localStringList1.add("name");
		localStringList1.add("revision");
		localStringList1.add("current");
		localStringList1.add("owner");
		localStringList1.add("organization");
		localStringList1.add("project");
		localStringList1.add("reserved");
		localStringList1.add("reservedby");
		localStringList1.add("attribute[iPLMPart.iPLMChangeLevelIndicator]");
		localStringList1.add("attribute[iPLMDrawingRepresentation.iPLMChangeLevelIndicator]");
		localStringList1.add("attribute[iPLMPart.iPLMStructureType]");
		
		List<IProposedActivity> activities = null;
		
		for (IProposedChanges proposed : proposedChanges) {
			
			sProposedId = proposed.getWhere().getObjectId();
			proposedObj.setId(sProposedId);
			Map mProposedChanges = proposedObj.getInfo(context,localStringList1);
			Map mProposedItem = new HashMap();
			activities = proposed.getActivites();

			sProposedId = (String)mProposedChanges.get("id");
			sProposedPhysicalId = (String)mProposedChanges.get("physicalid");
			sProposedType = (String)mProposedChanges.get("type");
			sProposedName = (String)mProposedChanges.get("name");			
			sProposedRev = (String)mProposedChanges.get("revision");
			sProposedState = (String)mProposedChanges.get("current");
			sProposedOwner = (String)mProposedChanges.get("owner");
			sProposedOrg = (String)mProposedChanges.get("organization");
			sProposedProj = (String)mProposedChanges.get("project");
			sReserved = (String)mProposedChanges.get("reserved");
			sReservedBy = (String)mProposedChanges.get("reservedby");
			sProposedPartCLI = (String)mProposedChanges.get("attribute[iPLMPart.iPLMChangeLevelIndicator]");
			sProposedDrawingCLI = (String)mProposedChanges.get("attribute[iPLMDrawingRepresentation.iPLMChangeLevelIndicator]");
			sCADStructureType = (String)mProposedChanges.get("attribute[iPLMPart.iPLMStructureType]");
			
			mProposedItem.put("id",sProposedId);
			mProposedItem.put("physicalid",sProposedPhysicalId);
			mProposedItem.put("type",sProposedType);
			mProposedItem.put("name",sProposedName);
			mProposedItem.put("revision",sProposedRev);
			mProposedItem.put("current",sProposedState);
			mProposedItem.put("owner",sProposedOwner);
			mProposedItem.put("organization",sProposedOrg);
			mProposedItem.put("project",sProposedProj);
			mProposedItem.put("reserved",sReserved);
			mProposedItem.put("reservedby",sReservedBy);
			
			if("iPLMPart".equalsIgnoreCase(sProposedType)){
				mProposedItem.put("CLI",sProposedPartCLI);
				mProposedItem.put("iPLMStructureType",sCADStructureType);		
			}else if("iPLMDrawingRepresentation".equalsIgnoreCase(sProposedType)){
				mProposedItem.put("CLI",sProposedDrawingCLI);
			}
			mProposedItem.put("activities",activities);
			mlProposedItems.add(mProposedItem);
		}
		return mlProposedItems;		
	}
	
	public String checkCADExclusionList(Context context, StringList slCADExclusionList, MapList mlProposedItems) throws Exception {
		String msgCADExcliusion = "true";
		String strStructureType = null;
		String strType = null;
		String strName = null;
		String strRev = null;
		String strCADExclude = null;
		int iExclusionListSize = slCADExclusionList.size();
		int iProposedItemSize = mlProposedItems.size();
		StringBuffer sbExcludeCADList = new StringBuffer();
		Map mpPropItem = null;
		
		if(slCADExclusionList!=null && iExclusionListSize>0){
			for (int idx=0;idx<iExclusionListSize;idx++){
				strCADExclude = (String)slCADExclusionList.get(idx);				
				for(int i=0;i<iProposedItemSize;i++){
					mpPropItem = (Map)mlProposedItems.get(i);
					strStructureType = (String)mpPropItem.get("iPLMStructureType");
					strType = (String)mpPropItem.get("type");
					strName = (String)mpPropItem.get("name");
					strRev = (String)mpPropItem.get("revision");
					if("iPLMPart".equalsIgnoreCase(strType) &&  UIUtil.isNotNullAndNotEmpty(strStructureType) && (strCADExclude).equalsIgnoreCase(strStructureType)){
						String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.ExcludeCAD");
						strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev);
						sbExcludeCADList.append(strMsg);
						sbExcludeCADList.append("\n");
					}
				}
			}			
		}
		if (sbExcludeCADList.length()!=0){
			msgCADExcliusion = sbExcludeCADList.toString();
		}
		return msgCADExcliusion;
	}
	
	public String checkChangeLevelIndicator(Context context, MapList mlProposedItems) throws Exception {
		String msgChangeLevelIndicator = "true";
		String strCLI = "";
		String strType = "";
		String strName = "";
		String strRev = "";
		String strProj = "";
		String strOrg = "";
		Map mpPropItem = null;
		int iProposedItemSize = mlProposedItems.size();
		StringBuffer sbCLI = new StringBuffer();
		for(int i=0;i<iProposedItemSize;i++){
			mpPropItem = (Map)mlProposedItems.get(i);
			strCLI = (String)mpPropItem.get("CLI");
			strType = (String)mpPropItem.get("type");
			strName = (String)mpPropItem.get("name");
			strRev = (String)mpPropItem.get("revision");
			strProj = (String)mpPropItem.get("project");
			strOrg = (String)mpPropItem.get("organization");

			//Added for Incident : 17750 : rkakde :  Start
			if(("iPLMPart".equalsIgnoreCase(strType) || "iPLMDrawingRepresentation".equalsIgnoreCase(strType)) && !("JLR".equals(strOrg) && "E3DPD".equals(strProj)) && (!"Standard".equalsIgnoreCase(strProj)) &&  UIUtil.isNullOrEmpty(strCLI)){
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.EmptyCLIAttribute");
				strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev+" ");
				sbCLI.append(strMsg);
				sbCLI.append("\n");
			}
			//Added for Incident : 17750 : rkakde :  End
		}		
		if(sbCLI.length()!=0){
			msgChangeLevelIndicator = sbCLI.toString();	
		}
		return msgChangeLevelIndicator;
	}
	
	/*public String checkSupplierCAandProposedItems(String strCAOrg, MapList mlProposedItems) throws Exception{
		String msgSupplierCACheck = "true";
		String strType = null;
		String strName = null;
		String strRev = null;
		String strPIOrg = null;
		boolean flag = false;
		StringBuffer sbSupplierCheck = new StringBuffer();		
		sbSupplierCheck.append("-CA Project is not matching with the following Proposed Items:\n");
		int iProposedItemsSize =  mlProposedItems.size();
		strCAOrg = strCAOrg.substring(0,3);
		for(int i=0;i<iProposedItemsSize;i++){
			Map mpProposed = (Map)mlProposedItems.get(i);
			strType = (String)mpProposed.get("type");
			strName = (String)mpProposed.get("name");
			strRev = (String)mpProposed.get("revision");
			strPIOrg = (String)mpProposed.get("organization");
			strPIOrg = strPIOrg.substring(0,3);
			if(!strCAOrg.equalsIgnoreCase(strPIOrg)){
				flag = true;
				sbSupplierCheck.append(strType+" "+strName+" "+strRev+"\n");				
			}
		}
		if(flag)
		{
			msgSupplierCACheck = sbSupplierCheck.toString();
		}
		return msgSupplierCACheck;		
	}*/
	
	public String checkCADMultipleRevOnCA(Context context, MapList mlProposedItems) throws Exception {
		String msgCADMultipleRev = "true";	
		String strId = null;
		String strType = null;
		String strName = null;
		String strRev = null;
		String strPhysicalId = null;
		String strMajorIds = null;
		String strMqlCommand ="print bus $1 select $2 $3 dump $4 ";
		StringList slPropIds = new StringList();
		StringList slCADRevDetails = new StringList();		
		StringList slDuplicateCAD = new StringList();		
		StringBuffer sbCADMultipleRev = new StringBuffer();
		
		int iProposedItemsSize =  mlProposedItems.size();
		Map mpProp = new HashMap();
		for(int i=0;i<iProposedItemsSize;i++){
			Map mpProposed = (Map)mlProposedItems.get(i); 	
			strType = (String)mpProposed.get("type");
			strName = (String)mpProposed.get("name");
			strRev = (String)mpProposed.get("revision");
			strPhysicalId = (String)mpProposed.get("physicalid");
			slPropIds.add(strPhysicalId);
			mpProp.put((String)mpProposed.get("physicalid"), strType+" "+strName);
		}		
		
		for(int i=0; i<slPropIds.size();i++){
			strId = (String)slPropIds.get(i);
			//Incident 17420 : Able to freeze CA with multiple revisions of document  : Start
			strMajorIds = MqlUtil.mqlCommand(context, strMqlCommand,strId,"majorids","revisions[].physicalid","|");	
			if(UIUtil.isNotNullAndNotEmpty(strMajorIds)){
				slCADRevDetails = (StringList)FrameworkUtil.split(strMajorIds, "|");
				
				Set s= new HashSet();
			    s.addAll(slCADRevDetails);         
			    slCADRevDetails = new StringList();
			    slCADRevDetails.addAll(s);
				slCADRevDetails.remove(strId);			
				//Incident 17420 : Able to freeze CA with multiple revisions of document  : End
				if(slCADRevDetails.size()>0){
					for(int j=0 ; j<slCADRevDetails.size();j++){
						String sTypeName = (String)mpProp.get(slCADRevDetails.get(j));
						if(slPropIds.contains(slCADRevDetails.get(j))){
							if(!slDuplicateCAD.contains(sTypeName)){
								String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.CADMultipleRevOnCA");
								strMsg = strMsg.replaceAll("<objTNR>",sTypeName);
								sbCADMultipleRev.append(strMsg);
								sbCADMultipleRev.append("\n");
								slDuplicateCAD.add(sTypeName);
							}							
						}
					}
				}
			}
		}
		if (sbCADMultipleRev.length()!=0){
			msgCADMultipleRev=sbCADMultipleRev.toString();
		}
		return msgCADMultipleRev;
	}
	
	public String checkProposedObjectReserve(Context context, MapList mlProposedItems) throws Exception {
		String msgProposedObjectReserve = "true";
		String strType = null;
		String strName = null;
		String strRev = null;
		String strReserved = null;
		String strReservedBy = null;
		StringBuffer sbReserveObj = new StringBuffer();
		Map mpPropItem = new HashMap();
		int iProposedItemSize = mlProposedItems.size();
		
		for(int i=0;i<iProposedItemSize;i++){
			mpPropItem = (Map)mlProposedItems.get(i);
			strType = (String)mpPropItem.get("type");
			strName = (String)mpPropItem.get("name");
			strRev = (String)mpPropItem.get("revision");
			strReserved = (String)mpPropItem.get("reserved");
			strReservedBy = (String)mpPropItem.get("reservedby");
			if("true".equalsIgnoreCase(strReserved)){
				String strMsg = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.ReservedPIonCA");
				strMsg = strMsg.replaceAll("<objTNR>",strType+" "+strName+" "+strRev+", Reserved by : "+strReservedBy+", ");
				sbReserveObj.append(strMsg);
				sbReserveObj.append("\n");
			}
		}	
		if (sbReserveObj.length()!=0){
			msgProposedObjectReserve = sbReserveObj.toString();
		}
		return msgProposedObjectReserve;
	}
	
	public Properties readPageObject(Context context, String strPageObject) throws Exception {
		Properties propertyEntry = new Properties();
		try {
			Page pageAttributePopulation = new Page(strPageObject);
			pageAttributePopulation.open(context);
			String strProperties = pageAttributePopulation.getContents(context);
			pageAttributePopulation.close(context);
			InputStream input = new ByteArrayInputStream(strProperties.getBytes("UTF8"));
			propertyEntry.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return propertyEntry;
	}	
}
