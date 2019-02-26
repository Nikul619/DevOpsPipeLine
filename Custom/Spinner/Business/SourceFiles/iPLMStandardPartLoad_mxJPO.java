import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.BusinessInterface;
import matrix.db.Context;
import matrix.util.StringList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.Date;

import com.matrixone.apps.domain.util.StringUtil;
import matrix.db.Page;
import com.matrixone.apps.domain.util.PropertyUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class iPLMStandardPartLoad_mxJPO  extends emxDomainObject_mxJPO  {

	 public iPLMStandardPartLoad_mxJPO (Context context, String[] args) throws Exception
	{
		super(context, args);
	}

	
	 /**
	 * This method is used for loading standard part .
		
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 
	 *				args[0] = 'Input File' 
     *				args[1] = 'Log Directory'
	 *            
	 * @return nothing
	 * @throws Exception
	 *             if the operation fails
	 * @author rkakde
	 */	
	 public void loadPart(Context context, String[] args) throws Exception {
		
		String strInputFileName = "";
		String strFormat = "yyyyMMdd_HHmmss";
		String strTimeStamp = new SimpleDateFormat(strFormat).format(Calendar.getInstance().getTime());
		BufferedWriter bwLogger = null;
	
		
		
		if (null != args && args.length == 2 && UIUtil.isNotNullAndNotEmpty(args[0]) && UIUtil.isNotNullAndNotEmpty(args[1])) 
		{
			strInputFileName = args[0];
			File fpSuccessLog = new File(args[1] + "Standard_Parts_Import"+ strTimeStamp + ".log");
			bwLogger = new BufferedWriter(new FileWriter(fpSuccessLog.getAbsoluteFile()));

		} else 
		{
			System.out.println("****Pass the Input file path and Log file path****");
			return;
		}
		
		
		readFile(context,strInputFileName,bwLogger);
		
	 }
	
	public static void readFile(Context context,String sFilename,BufferedWriter bwLogger)throws Exception{
		
		 FileInputStream fis = null;
		 XSSFSheet sheet = null;
		 String SheetName = null;
		 XSSFWorkbook workbook = null;
		 try {
			 
			 	File file = new File(sFilename);

			 	if(file.exists()){
			 		fis = new FileInputStream(file);

			 		workbook = new XSSFWorkbook(fis); 
			 		sheet = workbook.getSheetAt(0);
					
			  		    Iterator<Row> rowIterator = sheet.iterator();
			  		    int RowCount = sheet.getLastRowNum();
			 
		                int cnt = 1; 
		                String fieldsArrayList = "";	
	            		    String columnValues = "";
	            		 for(int i=1; i<=RowCount; i++)
	            		 {
	            		    try
	            		    {
	            		    	Row nextRow = sheet.getRow(i);
	            		    	columnValues = "";

	            		    	Iterator<Cell> cellIterator = nextRow.cellIterator();
	            		    	 while (cellIterator.hasNext()) {
	            		             Cell nextCell = cellIterator.next();
	            		            if (Cell.CELL_TYPE_STRING == nextCell.getCellType()) 
	            		            {
	            		    
	            		            columnValues +=  nextCell.getStringCellValue()+  ";";

	            		         }            		          
	            	          }

	            		    	 process(context,columnValues,bwLogger);
	            		    }
	            		    catch (Exception e) {
								loggerMSG(bwLogger,"Exception ::: "+e.getLocalizedMessage());
	            	    		
	            		    }
	            		}
			 	}  
		 }catch (Exception e) {
				loggerMSG(bwLogger,"Exception ::: "+e.getLocalizedMessage());
			 	
			}
   }
   
   public static void process(Context context,String columnValues,BufferedWriter bwLogger)throws  Exception
	{
		
		String strRowDetails =null;
		String strGeneralClassName =null;
		String strStandardPartBaseNo = null;
		String strAttributeName=null;
		String strAttributeValue =null;
		Map mAttributeMap = new HashMap();
		String strGeneralClassID = null;
		String strGeneralClassRev = null;
		String strStdPartID =null;
		MapList mlStandardPartsDetails =null;
		MapList mlGeneralClassDetails =null;
		try
		{
			if(UIUtil.isNotNullAndNotEmpty(columnValues)){
			
				StringList strSelectedCADIdStringList	= FrameworkUtil.split(columnValues,";");
		
				for(int i=0;i<strSelectedCADIdStringList.size();i++)
				{
					strRowDetails				= (String)strSelectedCADIdStringList.get(i);
					
					if(UIUtil.isNotNullAndNotEmpty(strRowDetails))
					if(i==0){
						strGeneralClassName = strRowDetails.trim();
						
					}
					else if(i==1){
						strGeneralClassRev = strRowDetails.trim();
						
					}
					else if(i==2){
						strStandardPartBaseNo =  strRowDetails.trim();
						strStandardPartBaseNo = strStandardPartBaseNo+"*";
						
					}
					else{
						
						StringList slAttributeDetails = FrameworkUtil.split(strRowDetails, "|");
					
						try{
						strAttributeName= (String) slAttributeDetails.get(0);						
						strAttributeValue = (String) slAttributeDetails.get(1);
						
						if(UIUtil.isNotNullAndNotEmpty(strAttributeValue))
							mAttributeMap.put(strAttributeName,strAttributeValue);
						
						}catch (Exception e) {
							System.out.println("strRowDetails ========> " + strRowDetails + " : " + e.getLocalizedMessage());
						}
					}
					
						
					}
				try {
					
					if(UIUtil.isNotNullAndNotEmpty(strGeneralClassName) && UIUtil.isNotNullAndNotEmpty(strGeneralClassRev) && UIUtil.isNotNullAndNotEmpty(strStandardPartBaseNo) )
					{
						mlGeneralClassDetails = checkGeneralClassExist(context,strGeneralClassName,strGeneralClassRev,"General Class");
						mlStandardPartsDetails = checkStdPartExist(context,strStandardPartBaseNo,"iPLMPart");
						
						if(null != mlGeneralClassDetails && !mlGeneralClassDetails.isEmpty())
						{
							connectStdParttoGenClass(context,mlGeneralClassDetails,mlStandardPartsDetails,mAttributeMap,strStandardPartBaseNo, bwLogger);
						}else {
						
							loggerMSG(bwLogger,"ERROR : 100 : General Class does not exist : 2nd, 3rd and 4th column values will be set empty " + strGeneralClassName + "  "+ strGeneralClassRev);						
						}
						
					
					}
						
					
				
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	public static MapList checkGeneralClassExist(Context context,String strName,String strRev, String strType)throws Exception
	{
		String strPartId = null;
		StringList objSelect = new StringList(1);
		objSelect.addElement(DomainConstants.SELECT_ID);
		objSelect.addElement(DomainConstants.SELECT_NAME);
		MapList mlGeneralClassData =  null;
		try
		{
			mlGeneralClassData =  DomainObject.findObjects(context, strType , strName, strRev, "*", "eService Production", null, true, objSelect);
	
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return mlGeneralClassData;

	}
	
	public static MapList checkStdPartExist(Context context,String strName, String strType)throws Exception
	{
		MapList mlObjData = null;
		StringList objSelect = new StringList(1);
		objSelect.addElement(DomainConstants.SELECT_ID);
		objSelect.addElement(DomainConstants.SELECT_NAME);
		objSelect.addElement(DomainConstants.SELECT_REVISION);
		String strPageName = "iPLMStandardPart";
		String strProject = "";
		String strOrganization = "";
		String strCheckProject = "";
		String strCheckOrg = "";
		StringBuffer sbWhere = new StringBuffer();
		// Added for Inc 17498 : rkakde :start
		sbWhere.append("revision==last");
		
		try
		{
			Properties pageProperties = readPageObject(context, strPageName);
			strProject=pageProperties.getProperty("Project");
			strCheckProject = checkProjectAndOrg(strProject,"Project");
			strOrganization=pageProperties.getProperty("Organization");
			strCheckOrg = checkProjectAndOrg(strOrganization,"Organization");
			if(UIUtil.isNotNullAndNotEmpty(strCheckProject) )
				sbWhere.append(" && "+strCheckProject);
			if(UIUtil.isNotNullAndNotEmpty(strCheckOrg))
				sbWhere.append(" && "+strCheckOrg);
			
			mlObjData =  DomainObject.findObjects(context, strType , strName, "*", "*", "vplm", sbWhere.toString(), true, objSelect);
			// Added for Inc 17498 : rkakde :End
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return mlObjData;

	}
	
	
	public static void connectStdParttoGenClass(Context context,MapList mlGeneralClassDetails, MapList mlStdPartsData,Map mAttributeMap,String strStandardPartBaseNo,BufferedWriter bwLogger)throws Exception
	{
		DomainObject domGenClassObj = null;
		DomainObject domStdPartObj = null;
		String strInterfaceName = null;
		String strStandardPartId = null;
		String strStandardPartName=null;
		String strStandardPartRev =null;
		String strGeneralClassID=null;
		String strGeneralClassName =null;
		String strAttributesNotPresent = null;

		try
		{
			
			if(null != mlGeneralClassDetails && !mlGeneralClassDetails.isEmpty())
			{
					Map mpObjectId = (Map)mlGeneralClassDetails.get(0);
					strGeneralClassID = (String)mpObjectId.get(DomainObject.SELECT_ID);
					strGeneralClassName = (String)mpObjectId.get(DomainObject.SELECT_NAME);
					
					domGenClassObj=new DomainObject(strGeneralClassID);
					

					strInterfaceName = domGenClassObj.getAttributeValue(context, ATTRIBUTE_MXSYSINTERFACE);
					strAttributesNotPresent = checkAttributeValues(context, mAttributeMap,strInterfaceName,bwLogger);
					if(UIUtil.isNotNullAndNotEmpty(strAttributesNotPresent)){
							loggerMSG(bwLogger,"ERROR : 300 : ["+strAttributesNotPresent+ "] does not exist in the General Class - "+strGeneralClassName);
										
					}

					if(null != mlStdPartsData && !mlStdPartsData.isEmpty())
					{
						int iCount = mlStdPartsData.size();
						for(int i=0; i<iCount;i++)
						{
							Map mpStandardPartsDetails = (Map)mlStdPartsData.get(i);
							strStandardPartId = (String)mpStandardPartsDetails.get(DomainObject.SELECT_ID);
							strStandardPartName = (String)mpStandardPartsDetails.get(DomainObject.SELECT_NAME);
							strStandardPartRev = (String)mpStandardPartsDetails.get(DomainObject.SELECT_REVISION);
							
							domStdPartObj=new DomainObject(strStandardPartId);
							if(UIUtil.isNotNullAndNotEmpty(strStandardPartId)){
								if(UIUtil.isNotNullAndNotEmpty(strAttributesNotPresent)){
									loggerMSG(bwLogger,"ERROR : 300 : ["+strAttributesNotPresent+ "] does not exist in the Standard Part -"+strStandardPartName);
										
								}
							}
							try{
								
								ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), "", "");
								if(!context.isTransactionActive())
								{
									context.start(true);
								}
								else
								{

									context.commit();
									context.start(true);
								}
								StringList slGenClass = domStdPartObj.getInfoList(context, "to[Classified Item].from.id");
								//System.out.println("slGenClass =========> " + slGenClass.toString());
								if(slGenClass != null && slGenClass.contains(strGeneralClassID)) 
									loggerMSG(bwLogger,"SUCCESS : 0 : Already connected "+strGeneralClassName + " to " + strStandardPartName + " - " + strStandardPartRev + "  with Base Number "+strStandardPartBaseNo);
								else
									DomainRelationship.connect(context, domGenClassObj, "Classified Item", domStdPartObj);
								//context.commit();
							
								
								try {
									/*if(!context.isTransactionActive())
									{
										context.start(true);
									}
									else
									{

										context.commit();
										context.start(true);
									}*/
									//System.out.println("mAttributeMap =========> " + mAttributeMap.toString());
									domStdPartObj.setAttributeValues(context, mAttributeMap);
									
									context.commit();	
									
									loggerMSG(bwLogger,"SUCCESS : 0 : Successfully connected "+strGeneralClassName + " to " + strStandardPartName + " - " + strStandardPartRev + "  with Base Number "+strStandardPartBaseNo);
									
									loggerMSG(bwLogger,"SUCCESS : 0 : Successfully updated attribute values to the Standard Part -"+strStandardPartName);
									
								}catch (Exception e) {
								
									
								context.abort();
								loggerMSG(bwLogger,"ERROR : 500 : Not able to coonect Standard Part or update attribute values on the Standard Part - "+strStandardPartName +" : "+e.toString());
								
								
								}
								
								
							}catch (Exception e) {
								context.abort();
								loggerMSG(bwLogger,"ERROR : 999 : while connecting "+strGeneralClassName + " to " + strStandardPartName + " - " + strStandardPartRev + "  with Base Number "+strStandardPartBaseNo + ":" +e.toString());
								
												
							} finally {
								ContextUtil.popContext(context);
							}						
				
						}
					
					}
					else {
						
						loggerMSG(bwLogger,"ERROR : 200 : Standard Part does not exist - 3rd and 4th column values will be set empty - Base Number "+strStandardPartBaseNo );
						
					}
			}
		
		}catch (Exception e) {
			loggerMSG(bwLogger,"ERROR : 999 : Any other errors with description : " +e.toString());

					
		}
	}
	
	public static String checkAttributeValues(Context context,Map mAttributeMap,String strInterfaceName,BufferedWriter bwLogger)throws Exception{
		String strInterface = null;
		String strMqlCmd = null;
		String strkeyValue=null;
		StringList slattributesList = new StringList();
		String strAttributesNotPresent = "";
		try{
			strInterface = "print interface '" + strInterfaceName + "' select attribute dump |";
			strMqlCmd = MqlUtil.mqlCommand(context, strInterface);
			slattributesList = FrameworkUtil.split(strMqlCmd, "|");
			Iterator itrAttributes=mAttributeMap.keySet().iterator();

			while(itrAttributes.hasNext())
			{

				strkeyValue=(String)itrAttributes.next();
				if(!slattributesList.contains(strkeyValue))
				{
					strAttributesNotPresent +=  strkeyValue + ",";
				}
				
			}
		}catch (Exception e) {
			loggerMSG(bwLogger,"ERROR : 999 : Error in method checkAttributeValues : " +e.getLocalizedMessage());
			throw e;
					
		}
		return strAttributesNotPresent;
	}
   
   public static void loggerMSG(BufferedWriter bw,String strMessage)
	{
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
			bw.write("["+simpleDateFormat.format(new Date()) + "] : " + strMessage+ "\r\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Added for Inc 17498 : rkakde :start
	public static Properties readPageObject(Context context, String strPageObject) throws Exception {
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
	
	public static String checkProjectAndOrg(String strProjectOrOrganization,String strAttribute){
		
		StringList slProjOrOrg=new StringList();
		StringList slReturnProjOrOrg=new StringList();
		String strReturnExpr= "";
		
		if (UIUtil.isNotNullAndNotEmpty(strProjectOrOrganization)) {
			slProjOrOrg = StringUtil.split(strProjectOrOrganization, ",");
			if(slProjOrOrg != null && slProjOrOrg.size() > 0){
				int iSize = slProjOrOrg.size();
				for(int i = 0 ; i <iSize; i++)
				{
					slReturnProjOrOrg.add(strAttribute+" == '"+slProjOrOrg.get(i)+"' ");
					
				}
				if(slReturnProjOrOrg!= null && slReturnProjOrOrg.size() > 0){
					strReturnExpr = (FrameworkUtil.join(slReturnProjOrOrg, "||"));
					
					strReturnExpr = "("+strReturnExpr+")";
				}
			}
		}
		return strReturnExpr;
		
	}
	// Added for Inc 17498 : rkakde :End
	
}