/*
 * 	iPLMOk2UseListImportExcel_mxJPO.java
 ********************************************************************************************
 * Modification Details:
 *
 * Ver|  Date       | CDSID    | CR      | Comment
 * ---|-------------|----------|---------|--------------------------------------------------
 * 01 | 29-JUL-2018 | kmanojku,ranand2   | CR-72 | Added methods
 * 02 | 13-AUG-2018 | rannad2  | CR - 72 | Modified/Added methods
 * 03 | 24-AUG-2018 | ranand2  | 17368   | Modified method Ok2useCreate
 * 04 | 25-AUG-2018 | phardare | CR72 | Added 2 new methods - updateSequenceOrderForConfigOptions & updateSequenceOrderForConfigFeatures
 * 05 | 24-AUG-2018 | phardare | 17616 | PublishedRemoved : Added new method - disconnectConfigFeatureLinkageWithHP & modified 2 existing methods
 * 06 | 04-OCT-2018 | phardare | 17667 | Only Add/Remove Tasks To Be Shown In Hopper Dropdown : Modified existing method - getRangeForVehicleFilter
 * 07 | 08-OCT-2018 | phardare | 17635 | To Check sTaskName* LDIs : Added new method - getAllWIPModelsForSpecificTaskName & Modified existing method - getVehicleRelatedConfiguration
 * 08 | 18-OCT-2018 | phardare | 17707 | To set PnO on GFD to CF : Configuration Context relationship : Added new method : updatePnOConfigurationContextRel
 * 09 | 02-NOV-2018 | phardare | 17616_DELTA | PublishedRemoved : Modified method - disconnectConfigFeatureLinkageWithHP
 ********************************************************************************************
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import matrix.db.JPO;
import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.effectivity.EffectivityFramework;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.util.MxMessage;

import java.util.Properties;
import java.util.Map.Entry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import matrix.db.Page;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.PersonUtil;

//Added comment to automatically call Jenkin build

public class iPLMOk2UseListImportExcel_mxJPO implements DomainConstants
{
	final static protected String VAULT_PRODUCTION = "eService Production";
	final static String TYPE_CONFIGURATION_FEATURE = "Configuration Feature";
	//final static String POLICY_CONFIGURATION_FEATURE = "Configuration Feature";
	final static String POLICY_CONFIGURATION_FEATURE = "Perpetual Resource";
	final static String POLICY_CONFIGURATION_OPTION = PropertyUtil.getSchemaProperty("policy_ConfigurationOption");
	final static String POLICY_PROJECT_TASK = PropertyUtil.getSchemaProperty("policy_ProjectTask");
	static String CLASSIFIED_ITEM_REL_NAME = "Classified Item";
	static String CONFIGURATION_OPTIONS_REL_NAME = "Configuration Options";
	public static final String RELATIONSHIP_CANDIDTAE_CONFIGURATION_FEATURES = PropertyUtil.getSchemaProperty("relationship_CandidateConfigurationFeatures");
	public static final String RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES = PropertyUtil.getSchemaProperty("relationship_MandatoryConfigurationFeatures");
	public static final String TYPE_CONFIGURATION_FEATURES = PropertyUtil.getSchemaProperty("type_ConfigurationFeature");
	public static final String TYPE_CONFIGURATION_OPTION = PropertyUtil.getSchemaProperty("type_ConfigurationOption");
	static String CLASS_REL_NAME = "Subclass";
	static String REVISION = "-";
	static String strFamilyName = "";
	static String strFFName = "";
	static StringList strListFeatureFamilyGroup = new StringList();
	static StringList strListFeatureFamilyType = new StringList();
	final static protected StringList OBJ_SELECT;
	public static File fpGloballog = null;
	public static BufferedWriter bwGlobalLogger = null;
	static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SS");

	// kmanojku : Fix for Incident #15420 - Start
	public static final String OWNING_ORGANIZATION = "Non-Commodity";
	public static final String OWNING_PROJECT = "PD";
	// kmanojku : Fix for Incident #15420 - End

	public static String strConnectionMQL = "modify connection $1 organization $2 project $3";
	public static String strConnectionBusMQL = "modify bus $1 organization $2 project $3";
	String strMQLModName = "mod bus $1 name $2 revision $3";
	public static String strMQLQuery = "escape mod bus $1 $2 $3 current $4";
	// Constants for CCR 1197 Start
	final static String TYPE_GENERAL_LIBRARY = "General Library";
	final static String POLICY_IPLM_LIBRARY = "Libraries";
	final static String POLICY_CLASSIFICATION = "Classification";
	final static String TYPE_GENERAL_CLASS = "General Class";
	public static final String TYPE_TASK = PropertyUtil.getSchemaProperty("type_Task");
	final static String REL_SUBCLASS = "Subclass";
	// Constants for CCR 1197 End
	
	public static final String ATTRIBUTE_HOOPER_STATE = PropertyUtil.getSchemaProperty("attribute_iPLMHooperState");
	public static final String RELATIONSHIP_CONFIGURATION_OPTIONS = PropertyUtil.getSchemaProperty("relationship_ConfigurationOptions");
	public static final String RELATIONSHIP_CONFIGURATION_FEATURES = PropertyUtil.getSchemaProperty("relationship_ConfigurationFeatures");
	public static final String RELATIONSHIP_CONFIGURATION_CONTEXT = PropertyUtil.getSchemaProperty("relationship_ConfigurationContext");//phardare:17707
	public static final String TYPE_HARDWARE_PRODUCT = PropertyUtil.getSchemaProperty("type_HardwareProduct");
	public static final String RELATIONSHIP_PRODUCTS = PropertyUtil.getSchemaProperty("relationship_Products");
	public static final String TYPE_MODEL = PropertyUtil.getSchemaProperty("type_Model");
	
	public static final String VAULT_ESERVICE_PRODUCTION = PropertyUtil.getSchemaProperty("vault_eServiceProduction");

	// Type Specific Counters
	public static int nFeatureFamilyCreated = 0;
	public static int nFeatureCreated = 0;
	public static String GFD_ID = null;
	public static String Selected_Option_ID = null;
	final static String FAMILY_FEATURE_STATE = "Active";
	final static String FEATURE_STATE = "Exists";
	//public static String TASK_ID = null;;
	//public static DomainObject dmTASK = null;
	
	public static String sLogsFolderName = "2_RMFD_Logs";
	
	static
	{
		OBJ_SELECT = new StringList(4);
		OBJ_SELECT.add(DomainObject.SELECT_ID);
		OBJ_SELECT.add(DomainObject.SELECT_NAME);
		OBJ_SELECT.add(DomainObject.SELECT_TYPE);
		OBJ_SELECT.add(DomainObject.SELECT_REVISION);
	}
	
	
	/**
	 * This method is used to process the DELTA files 
	 * exec prog iPLMOk2UseListImportExcel -method processDeltaFiles "<<DELTA FOLDER Path>>"
	 * @param context
	 * @throws Exception
	 * @returns void
	 */
	public static void processDeltaFiles(Context _context, String[] _args) throws Exception
	{
		String sFolderPath= _args[0];
		
		String sNewDeltaFolderPath = "";
		String sFileName = "";
		String sDeltaFilePath = "";
		String sLogsFolderPath = "";
		
		String [] argsForProcess = null;
		
		File fDeltaDir = null;		
		File[] fDeltaFileList = null;		
		
		try{
					
			//strPathName - folder path which has DELTA folder which ends with _NEW
			if( null != sFolderPath && !"".equals(sFolderPath) ){
				
				//Create Log file
								
				Date dToday = new Date();
				DateFormat df = new SimpleDateFormat("ddMMYYYY");
				String sLogOrDeltaFileDate = df.format(dToday);				
				
				int nIndex = sFolderPath.lastIndexOf(java.io.File.separator);				
				
				//Log file creation
				if( nIndex >= 0){
					sLogsFolderPath = sFolderPath.substring(0, nIndex);	
					String sLogFileName = sLogsFolderPath + java.io.File.separator + sLogsFolderName + java.io.File.separator + "Log_DELTA_" + sLogOrDeltaFileDate + ".log";
					
					fpGloballog = new File( sLogFileName );
										
					if ( fpGloballog.exists() )
			        {
						bwGlobalLogger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fpGloballog), "UTF-8"));
			        } else {
			        	fpGloballog = new File( sLogFileName );
			        	try{
			        		boolean createdFile = fpGloballog.createNewFile();
				            if (createdFile)
				            {
				            	bwGlobalLogger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fpGloballog), "UTF-8"));
				                           
				            } else {
				            	 System.out.println("ERROR: Unable to create Log File.  Unable to continue.");
				            	 return;
				            }
			        	}catch(IOException ioe){
			        		ioe.printStackTrace();
			        	}
						
			        }
									
				} else {
					System.out.println("ERROR: Issue in finding folder");
					return;
				}				
				
				if( null != bwGlobalLogger ){
					
					//Get DELTA folder which ends with '_NEW'
					sNewDeltaFolderPath = getNewDeltaFolderPath(sFolderPath);
					
					if( UIUtil.isNotNullAndNotEmpty(sNewDeltaFolderPath) ){
						writeLog(bwGlobalLogger,"Delta Folder Path : " + sNewDeltaFolderPath);
						
						//Fetch files from DELTA folder which ends with '_NEW'
						fDeltaDir= new File(sNewDeltaFolderPath);
						fDeltaFileList = fDeltaDir.listFiles();
						
						if( fDeltaFileList != null && fDeltaFileList.length > 0 ){						
							
							writeLog(bwGlobalLogger,"\nFile Execution : Start\n");
							
							 for (File fDeltaFile : fDeltaFileList){							 
								 if( fDeltaFile.isFile() ){
									 try {									
									    sFileName = fDeltaFile.getName();									   								    
										sDeltaFilePath = sNewDeltaFolderPath + java.io.File.separator + sFileName;											
										argsForProcess = new String[1];
										argsForProcess[0] = sDeltaFilePath;
																				
										Ok2useCreate(_context, argsForProcess);	
										writeLog(bwGlobalLogger,"Processing done for File : " + sFileName);																		
										
									 } catch(Exception e){									 
										 writeLog(bwGlobalLogger,"Error in processDeltaFiles (for loop) : " + e.toString());
									 }									 
								 }
							 }
							 
							 writeLog(bwGlobalLogger,"\nFile Execution : End\n");
							
							//Remove '_NEW' from DELTA folder as the processing is done 
							int nNEWIndex = sNewDeltaFolderPath.lastIndexOf("_NEW");
								
							if( nNEWIndex >= 0){							
								try {
									String sNewFolderName = sNewDeltaFolderPath.substring(0, nNEWIndex);
									
									File fDirNew = new File(sNewFolderName);									
									boolean bRenamed = fDeltaDir.renameTo(fDirNew);
									if( bRenamed ){										
										writeLog(bwGlobalLogger,"Successfully removed '_NEW' for DELTA Folder : "+sNewFolderName);								
									} else {											
										writeLog(bwGlobalLogger,"Error while renaming folder - (removing '_NEW') for : "+sNewDeltaFolderPath);
									}
								}catch(Exception exp){
									writeLog(bwGlobalLogger,"Error in processDeltaFiles (Rename folder) : " + exp.toString());
								}
								
							} else {								
								writeLog(bwGlobalLogger,"DELTA folder ending with '_NEW' not found in the path : "+sNewDeltaFolderPath);
							}
							
							 //bwGlobalLogger.write("[" + simpleDateFormat.format(new Date())+ "] INFO - Number of Feature Family Created "+ nFeatureFamilyCreated + " \n");
							 //bwGlobalLogger.write("[" + simpleDateFormat.format(new Date())+ "] INFO - Number of Feature Created " + nFeatureCreated+ " \n");
							 writeLog(bwGlobalLogger,"\n\n[" + simpleDateFormat.format(new Date())+ "] INFO - Number of Feature Family Created "+ nFeatureFamilyCreated);
							 writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date())+ "] INFO - Number of Feature Created " + nFeatureCreated);
												 
						} else {							
							writeLog(bwGlobalLogger,"No file/s found in DELTA folder in the path : "+sNewDeltaFolderPath);
						}
					} else {						
						writeLog(bwGlobalLogger,"DELTA folder ending with '_NEW' not found in the path : "+sNewDeltaFolderPath);
					}
				}
				
			} else {
				System.out.println("Please provide DELTA folder location");
				return;
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
			if ( null != bwGlobalLogger ) {
				writeLog(bwGlobalLogger,"Error in processDeltaFiles : " + ex.toString());
			}				
			
		}finally {

			try {				
				if ( null != bwGlobalLogger )
					bwGlobalLogger.close();				
			} catch (IOException ioExp) {

				ioExp.printStackTrace();

			}
			//To execute the batch scripts at the end of delta file processing -
			updatePnOConfigurationContextRel(_context, argsForProcess);//phardare:17707:CronJob-1
			disconnectConfigFeatureLinkageWithHP(_context, argsForProcess);//phardare:17616:CronJob-2
			updateSequenceOrderForConfigFeatures(_context, argsForProcess);//phardare:17707:CronJob-3
			updateSequenceOrderForConfigOptions(_context, argsForProcess);//phardare:17707:CronJob-4
		}
	}
	
	/**
	 * 
	 * @param _context
	 * @param _args
	 * @throws Exception
	 */
	public static void Ok2useCreate(Context _context, String[] _args) throws Exception
	{
		String strPathName = _args[0];
		String strFilePath = null;
		String strFileName = null;
		String strTaskName = null;
		
		int nIndex = 0;
				
		// For Finding out the folder of the XML file to create the Script and
				
		// Logs
		if (strPathName.contains("/")) {
			nIndex = strPathName.lastIndexOf("/");
			strFilePath = strPathName.substring(0, nIndex + 1);
			strFileName = strPathName.substring(nIndex+1, strPathName.length());

			if(null != strFileName )
			{
				//Modified below line for Incident 17368 - Start
				//String[] strArray = strFileName.split("\\.");
				String[] strArray = strFileName.split("\\.xl");
				//Modified below line for Incident 17368 - End
				strTaskName = strArray[0];				
				strTaskName = strTaskName.replaceFirst("Delta_", "");
				strTaskName = strTaskName.replaceFirst("DELTA_", "");
			}
		}		
		//Commented for Delta folder execution - Start
		//java.util.Date date = new Date();
		//fpGloballog = new File(strFilePath + date.getTime() + "MFDGlobal.log");
		//bwGlobalLogger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fpGloballog), "UTF-8"));
		//Commented for Delta folder execution - End
		
		//Commented due to change in requirement - No need to create Library structure
		//createLibraryAndFamily(_context, _args);

		// Find GFD Id
		GFD_ID = findHPObjects(_context,"Hardware Product", "GFD","A");
		// Create Domain object for Task
		DomainObject dmTask = new DomainObject();
		String strTaskId = findHPObjects(_context,TYPE_TASK, strTaskName,"1");		
		
		if(null == strTaskId || strTaskId.isEmpty())
		{
			// Create TASK Id			
			dmTask.createObject(_context,TYPE_TASK,strTaskName, "1",POLICY_PROJECT_TASK, VAULT_PRODUCTION);
			try
			{
				MqlUtil.mqlCommand(_context, strConnectionBusMQL, true,	dmTask.getId(_context), OWNING_ORGANIZATION, OWNING_PROJECT);
			}
			catch(Exception ex)
			{
				writeLog(bwGlobalLogger,"Error in Ok2useCreate (MQL) : " + ex.toString());
			}
		}
		else
		{			
			dmTask = new DomainObject(strTaskId);
		}
		
		readMFDDataFromFile(_context, strPathName, dmTask);
		
		//Commented for Delta folder execution - Start
		//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date())+ "] INFO - Number of Feature Family Created "+ nFeatureFamilyCreated + " \n");
		//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date())+ "] INFO - Number of Feature Created " + nFeatureCreated+ " \n");
		//bwGlobalLogger.flush();
		//bwGlobalLogger.close();
		//Commented for Delta folder execution - End
	}
	
	public static String getNewDeltaFolderPath(String sDirectoryPath) {
		   
		String sNEWFolderPath = "";
	    String sTempFolderName = "";
	    try{
	    	File directory = new File(sDirectoryPath);
			
		    FileFilter directoryFileFilter = new FileFilter() {
		        public boolean accept(File file) {
		            return file.isDirectory();
		        }
		    };
				
		    File[] directoryListAsFile = directory.listFiles(directoryFileFilter);
		    
		    for (File directoryAsFile : directoryListAsFile) {		    	
		    	sTempFolderName = directoryAsFile.getName();
		    	if(sTempFolderName.endsWith("_NEW")){
		    		sNEWFolderPath = sDirectoryPath + java.io.File.separator + sTempFolderName;
		    		break;
		    	}
		    }
	    }catch (Exception e) {
			e.printStackTrace();
			writeLog(bwGlobalLogger,"Error in getNewDeltaFolderPath : " + e.toString());
		}		
	    return sNEWFolderPath;
	}
	
	/**
	 * 
	 * @param _context
	 * @param strPathName
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unchecked" })
	public static void readMFDDataFromFile(Context _context,String strPathName, DomainObject dmTask) throws Exception
	{
		try 
		{
			FileInputStream fis = null;		
			fis = new FileInputStream(strPathName);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			int nNumberOfSheets = workbook.getNumberOfSheets();
			int nRowNumber;
			MapList mfdDataList = new MapList();
			HashMap JLRLibraryMap;
			HashMap OALibraryMap;
			HashMap WERSLibraryMap;
			HashMap FeatureMap;
						
			for(int n=0;n< nNumberOfSheets;n++)
			{
				XSSFSheet sheet = workbook.getSheetAt(n);
				mfdDataList = new MapList();
				Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext())
				{
					Row row = rowIterator.next();
					nRowNumber = row.getRowNum();
					if(nRowNumber<1)
					{
						continue;
					}
					JLRLibraryMap = new HashMap();

					FeatureMap = new HashMap();
					// JLR Library Data Population Starts
					Cell cell = row.getCell(0,Row.CREATE_NULL_AS_BLANK);
					JLRLibraryMap.put("FeatureFamilyName", getValue(cell));

					cell = row.getCell(1,Row.CREATE_NULL_AS_BLANK);
					JLRLibraryMap.put("FeatureFamilyDescription", getValue(cell));

					//cell = row.getCell(2,Row.CREATE_NULL_AS_BLANK);
					JLRLibraryMap.put("Lifecycle", FAMILY_FEATURE_STATE);

					cell = row.getCell(2,Row.CREATE_NULL_AS_BLANK);
					JLRLibraryMap.put("FeatureName", getValue(cell));

					cell = row.getCell(3,Row.CREATE_NULL_AS_BLANK);
					JLRLibraryMap.put("FeatureDescription", getValue(cell));

					//cell = row.getCell(5,Row.CREATE_NULL_AS_BLANK);
					JLRLibraryMap.put("FeatureLifecycle", FEATURE_STATE);

					//cell = row.getCell(6,Row.CREATE_NULL_AS_BLANK);
					//JLRLibraryMap.put("Effectivity", getValue(cell));

					cell = row.getCell(6,Row.CREATE_NULL_AS_BLANK);
					JLRLibraryMap.put("OperationMode", getValue(cell));

					if(JLRLibraryMap.isEmpty())
					{
						FeatureMap.put("JLRLibrary", null);
					}
					else
					{
						FeatureMap.put("JLRLibrary", JLRLibraryMap);
					}	
					mfdDataList.add(FeatureMap);	            
				}
				processMFDData(_context,mfdDataList,dmTask);		        
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();			
			writeLog(bwGlobalLogger,"Error in readMFDDataFromFile : " + e.toString());
		}
	}

	/**
	 * 
	 * @param _context
	 * @param mfdDataList
	 * @throws Exception
	 */
	public static void processMFDData(Context _context,MapList mfdDataList, DomainObject dmTask) throws Exception
	{
		simpleDateFormat.format(new Date());
		Iterator mfdDataIterator = mfdDataList.iterator();
		HashMap FeatureMap = new HashMap();
		String strFeatureJLR = null;
		String strFeatureOA = null;
		String strFeatureWERS = null;
		DomainRelationship domRelObj = new DomainRelationship();
		String strDataInfo ="";
		String strModelName ="";
		strDataInfo ="Feature Name	Family Name	Milestone Effectivity\n";
		try {

			while (mfdDataIterator.hasNext()) {

				String[] arr = new String[3];
				String[] arrStats = new String[6];
				FeatureMap = (HashMap) mfdDataIterator.next();
				HashMap JLRLibraryMap = new HashMap();
				StringBuffer strEffectivity = new StringBuffer();
				String strSecondHPName = "";
				String strSecondHPRevision = "";
				String strHP2PhysicalId = "";
				String strConfigurationOptionsRelId = "";
				MapList mlNewInputExpr = new MapList();
				HashSet<String> hsDeltaModel = new HashSet<String>();

				JLRLibraryMap = (HashMap) FeatureMap.get("JLRLibrary");
				if (null != JLRLibraryMap && !JLRLibraryMap.isEmpty()) {
					String strOperationMode = (String) JLRLibraryMap.get("OperationMode");
					//if(null != strOperationMode && "Add".equalsIgnoreCase(strOperationMode))
					//{
					ContextUtil.startTransaction(_context, true);
					strFeatureJLR = processData(_context, JLRLibraryMap, "JLRC",dmTask);
					
					//Need to understand the code flow to check execution time 
					
					//String strEffectivityExpression = (String) JLRLibraryMap.get("Effectivity");

					String strFeatureName = (String) JLRLibraryMap.get("FeatureName");
					String strFeatureFamilyName = (String) JLRLibraryMap.get("FeatureFamilyName");
					//bwGlobalLogger.write("\n Feature Name                = "+ strFeatureName);
					writeLog(bwGlobalLogger,"Feature Name : " + strFeatureName);
					//bwGlobalLogger.write("\n New Effectivity Expression  = "+ strEffectivityExpression);

					//Read the default effectivity from page object and set.
					DomainObject dobjCO = DomainObject.newInstance(	_context, strFeatureJLR);
					strConfigurationOptionsRelId = dobjCO.getInfo(_context,	"to[Configuration Options].id");
					String strConfigurationFeatureObjId = dobjCO.getInfo(_context,"to[Configuration Options].from.id");
					//Read Hardware Product name from page object to apply default effectivity
					//Properties propertyEntry = readPageObject(_context,"iPLMOk2ulistHPInfo");
					//System.out.println("------propertyEntry---------"+propertyEntry);
					//String strHPName = propertyEntry.getProperty("Product_State");
					//String strHPId = findHPObjects(_context,"Hardware Product", strHPName,"*");
					//System.out.println("------strHPId---------"+strHPId);
					DomainObject dobjHP = DomainObject.newInstance(_context, GFD_ID);
					String strHPPhysicalId = dobjHP.getInfo(_context,"physicalid");

					String strModelPhysicalId = dobjHP.getInfo(_context,"to[Main Product].from.physicalid");
					String strModelId = dobjHP.getInfo(_context,"to[Main Product].from.id");
					if (strModelPhysicalId == null || "".equalsIgnoreCase(strModelPhysicalId) || "null".equalsIgnoreCase(strModelPhysicalId)) {
						strModelPhysicalId = dobjHP.getInfo(_context,"to[Products].from.physicalid");
						strModelId = dobjHP.getInfo(_context,"to[Products].from.id");
					}

					//Connect Configuration Feature with Hardware Product
					DomainObject dobjCF = DomainObject.newInstance(_context, strConfigurationFeatureObjId);
					StringList  strRelatedHardwareProductList=dobjCF.getInfoList(_context,"to[Configuration Features].from.id");
					if(!strRelatedHardwareProductList.contains(dobjHP.getObjectId()))
					{												
						DomainRelationship domRelGFD = new DomainRelationship();
						domRelGFD = DomainRelationship.connect(_context,dobjHP,"Configuration Features" ,dobjCF); 
						try
						{
							MqlUtil.mqlCommand(_context, strConnectionMQL, true,	domRelGFD.toString(), OWNING_ORGANIZATION, OWNING_PROJECT);
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
							writeLog(bwGlobalLogger,"Error in processMFDData (MQL) : " + ex.toString());
						}
					}

					//End
					//Prepare effectivity statement and apply
					
					//Added to get GFD Expr in () - start
					strEffectivity.append("(");
					strEffectivity.append("@EF_PR(PHY@EF:");
					strEffectivity.append(strModelPhysicalId);
					strEffectivity.append("[PHY@EF:");
					strEffectivity.append(strHPPhysicalId);
					strEffectivity.append("-^])");
					strEffectivity.append(")");
					strDataInfo = strDataInfo + strFeatureName +"	"+strFeatureFamilyName+"	"+"GFD"+"\n";
					
					//Added to get GFD Expr in () - end
					
					setEffectivity(_context, strConfigurationOptionsRelId, strEffectivity.toString(), strFeatureJLR);


					//End

					ContextUtil.commitTransaction(_context);
					//}
				}
			}

			//Remove Comment
			//sendNotification(_context, strModelName , strDataInfo);

		} catch (Exception e) {			
			ContextUtil.abortTransaction(_context);
			writeLog(bwGlobalLogger,"Error in readMFDDataFromFile and transaction aborted : " + e.toString());
		}
	}

	/**
	 * 
	 * @param _context
	 * @param dataMap
	 * @param strLibraryType
	 * @return
	 * @throws Exception
	 */
	private static String processData(Context _context, HashMap dataMap, String strLibraryType, DomainObject dmTask) throws Exception {
		String strFeatureId = null;
		String strConfigurationFeatureName = (String) dataMap.get("FeatureFamilyName");
		DomainRelationship domRelObj = new DomainRelationship();
		if (null == strConfigurationFeatureName	|| "".equalsIgnoreCase(strConfigurationFeatureName)) {
			return strFeatureId;
		}
		String strFeatureName = null;
		Double dFeatureName = null;
		/*
		 * if(dataMap.get("FeatureName") instanceof Double) { dFeatureName =
		 * (Double) dataMap.get("iPLMLessFeature"); strFeatureName = new
		 * Double(dFeatureName).toString(); } else {
		 */
		strFeatureName = (String) dataMap.get("FeatureName");
		// }
		String strConfigurationFeatureType = null;
		String strFeatureFamilyId = null;
		String strConfigurationFeaturePolicy = null;
		String strFeatureType = ConfigurationConstants.TYPE_CONFIGURATION_OPTION;
		String strFeaturePolicy = null;
		if (strLibraryType.equalsIgnoreCase("JLRC")) {
			strConfigurationFeatureType = TYPE_CONFIGURATION_FEATURE;
			strConfigurationFeaturePolicy = POLICY_CONFIGURATION_FEATURE;
			strFeaturePolicy = POLICY_CONFIGURATION_OPTION;
		}
		if (!"".equalsIgnoreCase(strConfigurationFeatureName)) {
			strFeatureFamilyId = createConfigurationFeatureAndSetAttributes(_context, strConfigurationFeatureType,strConfigurationFeatureName, strConfigurationFeaturePolicy,dataMap, strLibraryType, dmTask);
		}
		if (!"".equalsIgnoreCase(strFeatureName)) {
			strFeatureId = createFeatureAndSetAttributes(_context, strFeatureType, strFeatureName, strFeaturePolicy, dataMap, strLibraryType, strConfigurationFeatureName, dmTask);
		}
		if (null != strFeatureId && !"".equalsIgnoreCase(strFeatureId)	&& null != strFeatureFamilyId	&& !"".equalsIgnoreCase(strFeatureFamilyId)) {
			DomainObject dObjFeature = new DomainObject(strFeatureId);
			DomainObject dObjFamily = new DomainObject(strFeatureFamilyId);
			StringList strConfigurationFeatureObjectList = dObjFeature.getInfoList(_context, "to[" + CONFIGURATION_OPTIONS_REL_NAME + "].from.id");
			if (strConfigurationFeatureObjectList.size() > 0) {
				//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Feature Family " + strConfigurationFeatureName + " and Feature  " + strFeatureName + " Already Connected \n");
				writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Feature Family " + strConfigurationFeatureName + " and Feature  " + strFeatureName + " Already Connected");
			} else {
				try {
					if (null != dObjFamily.getObjectId() && !dObjFamily.getObjectId().equals("")) {
						domRelObj = DomainRelationship.connect(_context, dObjFamily, CONFIGURATION_OPTIONS_REL_NAME,  dObjFeature);
						// Remove Comment
						MqlUtil.mqlCommand(_context, strConnectionMQL, true, domRelObj.toString(), OWNING_ORGANIZATION, OWNING_PROJECT);

					}
				} catch (Exception e) {
					e.printStackTrace();					
					//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Connecting Feature Family "	+ strConfigurationFeatureName + " and Feature " + strFeatureName + " \n");
					writeLog(bwGlobalLogger,"Error in processData (MQL) : " + e.toString());
				}
			}
		}
		return strFeatureId;
	}

	/**
	 * 
	 * @param _context
	 * @param strFeatureType
	 * @param strFeatureName
	 * @param strFeaturePolicy
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	public static String createFeatureAndSetAttributes(Context _context,String strFeatureType,String strFeatureName,String strFeaturePolicy,HashMap dataMap,String strLibraryType,String strFamilyName, DomainObject dmTask)throws Exception
	{
		DomainObject dObjectFeature = new DomainObject();
		Map attributeMap = new HashMap();
		DomainRelationship domRelObj = new DomainRelationship();
		String strFeatureId = "";
		String strOperaionMode = (String) dataMap.get("OperationMode");
		if (null == strFeatureName) {
			return strFeatureId;
		}
		//Modify to MQL
		strFeatureId = findMatchingObjects(_context, strFeatureName,strFeatureType, strLibraryType, "");
		if ("".equalsIgnoreCase(strFeatureId)) {
			try {
				dObjectFeature.createObject(_context, strFeatureType, strFeatureName, "", strFeaturePolicy, VAULT_PRODUCTION);				
				//Remove Comment
				MqlUtil.mqlCommand(_context, strConnectionBusMQL, true, dObjectFeature.getId(_context), OWNING_ORGANIZATION, OWNING_PROJECT);
				nFeatureCreated++;
			} catch (Exception e) {
				e.printStackTrace();
				//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Creating Feature " + strFeatureName + " \n");
				writeLog(bwGlobalLogger,"Error in createFeatureAndSetAttributes (MQL) : " + e.toString());
				writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Creating Feature " + strFeatureName);
				return strFeatureId;
			}
		} else {
			dObjectFeature.setId(strFeatureId);
		}
		dObjectFeature.setDescription(_context,	(String) dataMap.get("FeatureDescription"));
		ContextUtil.pushContext(_context);
		try {
			MqlUtil.mqlCommand(_context, strMQLQuery, strFeatureType, strFeatureName, "", (String) dataMap.get("FeatureLifecycle").toString());
			// Remove Comment
			MqlUtil.mqlCommand(_context, strConnectionBusMQL, true,	dObjectFeature.getId(_context), OWNING_ORGANIZATION, OWNING_PROJECT);
		} catch (Exception e) {
			e.printStackTrace();
			//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Promoting Feature "	+ strFeatureName + " \n");
			writeLog(bwGlobalLogger,"Error in createFeatureAndSetAttributes (MQL) : " + e.toString());
			writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Promoting Feature "	+ strFeatureName);
		} finally {
			ContextUtil.popContext(_context);
		}

		attributeMap.put("Display Text", (String) dataMap.get("FeatureDescription"));
		attributeMap.put("Display Name", (String) dataMap.get("FeatureDescription"));
		attributeMap.put("Title", strLibraryType);
		dObjectFeature.setAttributeValues(_context, attributeMap);		

		connectTask(_context, dmTask,  dObjectFeature, strOperaionMode);

		return dObjectFeature.getObjectId();
	}

	/**
	 * 
	 * @param _context
	 * @param strConfigurationFeatureType
	 * @param strConfigurationFeatureName
	 * @param strConfigurationFeaturePolicy
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	public static String createConfigurationFeatureAndSetAttributes(Context _context, String strConfigurationFeatureType, String strConfigurationFeatureName, String strConfigurationFeaturePolicy, HashMap dataMap, String strLibraryType, DomainObject dmTask) throws Exception {
		DomainObject dObjectConfigurationFeature = new DomainObject();
		Map attributeMap = new HashMap();
		String strFeatureFamilyId = null;
		DomainRelationship domRelObj = new DomainRelationship();
		String strOperaionMode = (String) dataMap.get("OperationMode");
		String strConfigurationOptionName = (String) dataMap.get("FeatureName");
		strFeatureFamilyId = findMatchingObjects(_context,strConfigurationFeatureName, strConfigurationFeatureType, "A","*");
		String strRevision = null;
		if ("".equalsIgnoreCase(strFeatureFamilyId)) {
			try {
				dObjectConfigurationFeature.createObject(_context,strConfigurationFeatureType,strConfigurationFeatureName, REVISION,strConfigurationFeaturePolicy, VAULT_PRODUCTION);
				//connectTask(_context, dmTask,  dObjectConfigurationFeature, strOperaionMode);				
				// dObjectConfigurationFeature.setOwner(_context,
				// "Test Everything");
				// dObjectConfigurationFeature.setAttributeValue(_context,
				// DomainObject.ATTRIBUTE_ORIGINATOR, "Test Everything");
				//Remove Comment
				MqlUtil.mqlCommand(_context, strConnectionBusMQL, true,	dObjectConfigurationFeature.getId(_context), OWNING_ORGANIZATION, OWNING_PROJECT);
				nFeatureFamilyCreated++;
				//domRelObj = DomainRelationship.connect(_context, dmTask,DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT, dObjectConfigurationFeature);
			} catch (Exception e) {
				e.printStackTrace();
				//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Creating Feature Family " + strConfigurationFeatureName + " \n");
				writeLog(bwGlobalLogger,"Error in createConfigurationFeatureAndSetAttributes (MQL): " + e.toString());
				writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Creating Feature Family " + strConfigurationFeatureName);
				return strFeatureFamilyId;
			}
		} else {
			dObjectConfigurationFeature.setId(strFeatureFamilyId);
		}
		String strConfigurationFeatureClassName = null;
		dObjectConfigurationFeature.setDescription(_context, (String) dataMap.get("FeatureFamilyDescription"));
		strRevision = dObjectConfigurationFeature.getInfo(_context,	SELECT_REVISION);
		ContextUtil.pushContext(_context);
		try {
			MqlUtil.mqlCommand(_context, strMQLQuery, strConfigurationFeatureType, strConfigurationFeatureName,	strRevision, (String) dataMap.get("Lifecycle").toString());
			//Remove Comment
			//Not required
			MqlUtil.mqlCommand(_context, strConnectionBusMQL, true,	dObjectConfigurationFeature.getId(_context), OWNING_ORGANIZATION, OWNING_PROJECT);
		} catch (Exception e) {
			e.printStackTrace();
			//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Promoting Feature Family " + strConfigurationFeatureName + " \n");
			writeLog(bwGlobalLogger,"Error in createConfigurationFeatureAndSetAttributes (MQL) : " + e.toString());
			writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Promoting Feature Family " + strConfigurationFeatureName);
		} finally {
			ContextUtil.popContext(_context);
		}

		attributeMap.put("Title", strLibraryType);
		attributeMap.put("Display Text", (String) dataMap.get("FeatureFamilyDescription"));
		attributeMap.put("Display Name", (String) dataMap.get("FeatureFamilyDescription"));
		dObjectConfigurationFeature.setAttributeValues(_context, attributeMap);

		/*
		// Update CF if only one CO is connected and request received for Remove
		if(null != strOperaionMode && "Remove".equalsIgnoreCase(strOperaionMode))
		{
			StringList slOptions = dObjectConfigurationFeature.getInfoList(_context, "from[Configuration Options].to.name");
			System.out.println("@@@@@@@@@@@@@@@@@@slOptions : "+slOptions);
			if(null != slOptions && slOptions.size()==1)
			{
				connectTask(_context, dmTask,  dObjectConfigurationFeature, strOperaionMode);
			}
		}
		else if(null != strOperaionMode && "Add".equalsIgnoreCase(strOperaionMode))
		{
			StringList slTask = dObjectConfigurationFeature.getInfoList(_context, "to["+DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"].from.name");
			System.out.println("@@@@@@@@@@@@@@@@@@slOptions : "+slTask);
			if(null != slTask && !slTask.contains((String) dmTask.getName()))
			{
				connectTask(_context, dmTask,  dObjectConfigurationFeature, strOperaionMode);	
			}
		}
		 */

		//Code commented as NO Library structure is created.

		/*
		StringBuffer strObjectBuffer = new StringBuffer();
		strObjectBuffer.append("attribute["	+ LibraryCentralConstants.ATTRIBUTE_TITLE + "]");
		strObjectBuffer.append("==");
		strObjectBuffer.append(strLibraryType);
		strConfigurationFeatureClassName = strConfigurationFeatureName.substring(0, 1);
		MapList mlGENERALCLASSList = DomainObject.findObjects(_context, TYPE_GENERAL_CLASS,	strConfigurationFeatureClassName, "*", "*",	VAULT_PRODUCTION, strObjectBuffer.toString(), false, OBJ_SELECT);
		try {
			Map mEach = (Map) mlGENERALCLASSList.get(0);
			String strObjectIdClass = (String) mEach.get(DomainObject.SELECT_ID);
			DomainObject dObjGeneralClass = new DomainObject(strObjectIdClass);

			StringList strGeneralClassObjectList = dObjectConfigurationFeature.getInfoList(_context, "to[" + CLASSIFIED_ITEM_REL_NAME + "].from.id");
			if (null != dObjGeneralClass.getObjectId()	&& !dObjGeneralClass.getObjectId().equals("") && !strGeneralClassObjectList.contains(strObjectIdClass)) {
				domRelObj = DomainRelationship.connect(_context, dObjGeneralClass, CLASSIFIED_ITEM_REL_NAME, dObjectConfigurationFeature);
				MqlUtil.mqlCommand(_context, strConnectionMQL, true, domRelObj.toString(), OWNING_ORGANIZATION,	OWNING_PROJECT);
			}
		} catch (Exception e) {
			bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Connecting Feature Family Class " + strConfigurationFeatureClassName + " and Feature Family "+ strConfigurationFeatureName + "\n");
		} 
		 */
		return dObjectConfigurationFeature.getObjectId();
	}

	/**
	 * Method to get Type and Name of Object and get Revision from the DB
	 * 
	 * @param out
	 *            -- Writer
	 * @param context
	 * @param objName
	 *            -- Object Name to Search
	 * @param objType
	 *            -- Object Type to Search
	 * @throws
	 * @returns StringList -- Name and Revision string Created by IBM on
	 *          08-Feb-2013
	 */
	public static String findMatchingObjects(Context _context, String objName, String objType, String strLibraryType, String strConfigurationFeatureName) throws Exception {
		String strObjectId = null;
		StringBuffer strObjectBuffer = new StringBuffer();
		MapList mlGENERALCLASSList = DomainObject.findObjects(_context, objType.trim(), objName.trim(), strConfigurationFeatureName, "*", VAULT_PRODUCTION, strObjectBuffer.toString(), false, OBJ_SELECT);
		if (mlGENERALCLASSList.size() >= 1) {
			Iterator itr = mlGENERALCLASSList.iterator();
			while (itr.hasNext()) {
				Map mEach = (Map) itr.next();
				strObjectId = (String) mEach.get(DomainObject.SELECT_ID);
			}
		} else {
			strObjectId = "";
		}
		return strObjectId;
	}

	/**
	 * 
	 * @param _context
	 * @param newAttrList
	 * @param strObjectName
	 * @throws Exception
	 */
	public static void updatefeatureFamilyAtrribute(Context _context, StringList newAttrList, String strObjectName) throws Exception {
		String strConfigFileId = null;
		String strConfigFileDescription = null;
		StringList selectStmts = new StringList(2);
		selectStmts.add(DomainConstants.SELECT_ID);
		selectStmts.add(DomainConstants.SELECT_DESCRIPTION);
		StringList strConfigFileRangeList = new StringList();
		MapList configFileMaplist = DomainObject.findObjects(_context, "Configuration File", strObjectName, null, null, null, null,true, selectStmts);
		if (configFileMaplist != null && configFileMaplist.size() > 0) {
			Iterator configFileItr = configFileMaplist.iterator();
			while (configFileItr.hasNext()) {
				Map tempConfigFileMap = (Map) configFileItr.next();
				strConfigFileId = (String) tempConfigFileMap.get(DomainConstants.SELECT_ID);
				strConfigFileDescription = (String) tempConfigFileMap.get(DomainConstants.SELECT_DESCRIPTION);
			}
		}
		StringTokenizer strConfigFileTokenizer = new StringTokenizer(strConfigFileDescription, ",");
		String strConfigFileRangeToken = "";
		while (strConfigFileTokenizer.hasMoreElements()) {
			strConfigFileRangeToken = strConfigFileTokenizer.nextToken();
			strConfigFileRangeToken = strConfigFileRangeToken.trim();
			strConfigFileRangeList.addElement(strConfigFileRangeToken);

		}
		String strSelectedValue = "";
		for (int n = 0; n < newAttrList.size(); n++) {
			strSelectedValue = newAttrList.get(n).toString().trim();
			if (!strConfigFileRangeList.contains(strSelectedValue)) {
				strConfigFileRangeList.addElement(strSelectedValue);
			}
		}
		String strNewDesc = strConfigFileRangeList.toString();
		strNewDesc = strNewDesc.replace("[", "");
		strNewDesc = strNewDesc.replace("]", "");
		DomainObject dObj = DomainObject.newInstance(_context, strConfigFileId);
		dObj.setDescription(_context, strNewDesc);
	}

	/**
	 * Method to Get Value of a Cell
	 * 
	 * @param cell
	 *            - EXCEL CELL Object
	 * @return a String - Contents of CELL
	 * @throws Exception
	 *             if operation fails
	 * @grade 0
	 */
	private static String getValue(Cell cell) {
		String value = "";
		double nValue = 0;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN:
			value = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			nValue = (double) cell.getNumericCellValue();
			value = String.valueOf(nValue).replaceFirst("\\.0+$", "");
			break;
		case Cell.CELL_TYPE_STRING:
			value = String.valueOf(cell.getStringCellValue());
			value = value.trim();
			break;
		case Cell.CELL_TYPE_FORMULA:
			value = String.valueOf(cell.getRichStringCellValue());
			break;
		}
		return value;
	}

	/**
	 * Added for removing Pre and Post installation scripts : Added for CCR 1197
	 * 
	 * @param _context
	 * @param _args
	 * @throws Exception
	 */
	public static void createLibraryAndFamily(Context context, String args[])
			throws Exception {
		try {
			createLibrary(context, "JLR Library","JLR Library to store JLR Codes", "LR", "JLRC");
		} catch (Exception e) {
			bwGlobalLogger.write("[" + simpleDateFormat.format(new Date())	+ "] INFO - Exception in Creating Library \n" + e);
		}

	}

	/*
	 * Added for Creating libraries and returning library ids : Added for CCR
	 * 1197
	 * 
	 * @param context
	 * 
	 * @param strLibName : Name of library to be created
	 * 
	 * @param strDesc : Description of library to be created
	 * 
	 * @param strIPLMLibraryType : Attribute value iPLMLibraryType of library to
	 * be created
	 * 
	 * @returns strLibID : Object id of created library
	 * 
	 * @throws Exception
	 */
	public static String createLibrary(Context context, String strLibName,	String strDesc, String strIPLMLibraryType, String strTitle)
			throws Exception {
		DomainObject doLibrary = new DomainObject();
		String strLibID = findMatchingObjects(context, strLibName, TYPE_GENERAL_LIBRARY, "*", "*");
		if (DomainObject.EMPTY_STRING.equalsIgnoreCase(strLibID)) {
			try {
				doLibrary.createObject(context, TYPE_GENERAL_LIBRARY, strLibName, "-", POLICY_IPLM_LIBRARY, VAULT_PRODUCTION);
				doLibrary.promote(context);
				// HashMap<String, String> hmAttributes = new HashMap<String,
				// String>();
				// hmAttributes.put("iPLMLibraryType", strIPLMLibraryType);
				// doLibrary.setAttributeValues(context, hmAttributes);
				doLibrary.setDescription(context, strDesc);
				// Remove Comment
				MqlUtil.mqlCommand(context, strConnectionBusMQL, true, doLibrary.getId(context), OWNING_ORGANIZATION, OWNING_PROJECT);
				strLibID = doLibrary.getId(context);
			} catch (Exception e) {
				bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Creating Library " + strLibName + " \n" + e);
			}
		} else {
			bwGlobalLogger.write("\n Library exists : " + strLibName+"\n");
			doLibrary.setId(strLibID);
		}
		createAndConnectGeneralClass(context, strTitle, strLibID);
		return doLibrary.getObjectId(context);
	}

	/*
	 * Added for Creating iPLMFeatureFamilyClass objects and connecting them
	 * with library : Added for CCR 1197
	 * 
	 * @param context
	 * 
	 * @param strTitle : Revision of iPLMFeatureFamilyClass object and title
	 * attribute value
	 * 
	 * @param strLibID : Object id of library to be connected to
	 * iPLMFeatureFamilyClass
	 * 
	 * @throws Exception
	 */
	public static void createAndConnectGeneralClass(Context context,
			String strTitle, String strLibID) throws Exception {
		try {
			DomainObject doGeneralClass = null;
			doGeneralClass = new DomainObject();
			DomainObject doLibrary = new DomainObject(strLibID);
			String strGeneralClassName = null;
			String strFeatureExist = null;
			// Attributes for relationship
			for (char cGeneralClassName = 'A'; cGeneralClassName <= 'Z'; cGeneralClassName++) {
				strGeneralClassName = String.valueOf(cGeneralClassName);
				strFeatureExist = findMatchingObjects(context, strGeneralClassName, TYPE_GENERAL_CLASS, strTitle, "*");
				if (DomainObject.EMPTY_STRING.equalsIgnoreCase(strFeatureExist)) {
					try {
						DomainRelationship doRel = doGeneralClass.createAndConnect(context, TYPE_GENERAL_CLASS,	strGeneralClassName, strTitle, POLICY_CLASSIFICATION, VAULT_PRODUCTION, REL_SUBCLASS, doLibrary, true);
						doGeneralClass.setAttributeValue(context, "Title", strTitle);
						doGeneralClass.promote(context);
						// Remove Comments 
						MqlUtil.mqlCommand(context, strConnectionBusMQL, true, doGeneralClass.getId(context), OWNING_ORGANIZATION, OWNING_PROJECT);
						MqlUtil.mqlCommand(context, strConnectionMQL, true,	doRel.toString(), OWNING_ORGANIZATION, OWNING_PROJECT);
					} catch (Exception e) {
						bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Creating Class " + strGeneralClassName + " \n");
					}
				} else {
					doGeneralClass.setId(strFeatureExist);
				}
			}
			for (int cGeneralClassName = 0; cGeneralClassName <= 9; cGeneralClassName++) {
				strGeneralClassName = String.valueOf(cGeneralClassName);
				strFeatureExist = findMatchingObjects(context, strGeneralClassName, TYPE_GENERAL_CLASS, strTitle, "*");
				if (DomainObject.EMPTY_STRING.equalsIgnoreCase(strFeatureExist)) {
					try {
						DomainRelationship doRel = doGeneralClass.createAndConnect(context, TYPE_GENERAL_CLASS,	strGeneralClassName, strTitle, POLICY_CLASSIFICATION, VAULT_PRODUCTION, REL_SUBCLASS, doLibrary, true);
						doGeneralClass.setAttributeValue(context, "Title", strTitle);
						doGeneralClass.promote(context);
						// Remove Comments
						MqlUtil.mqlCommand(context, strConnectionBusMQL, true, doGeneralClass.getId(context), OWNING_ORGANIZATION, OWNING_PROJECT);
						MqlUtil.mqlCommand(context, strConnectionMQL, true,	doRel.toString(), OWNING_ORGANIZATION, OWNING_PROJECT);
					} catch (Exception e) {
						bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Exception in Creating Class " + strGeneralClassName + " \n");
					}
				} else {
					doGeneralClass.setId(strFeatureExist);
				}
			}

		} catch (Exception e) {
			bwGlobalLogger.write("[" + simpleDateFormat.format(new Date())	+ "] INFO - Exception in Creating Class \n");
		}
	}

	/**
	 * Method to get Type and Name of Object and get Revision from the DB
	 * 
	 * @param context
	 * @param objName
	 *            -- Object Name to Search
	 * @param objType
	 *            -- Object Type to Search
	 * @throws
	 * @returns String -- Object ID Created by IBM on 21-Aug-2013
	 */
	private static String findHPObjects(Context _context, String objType, String objName, String revision) throws Exception {

		String strObjectId = null;
		MapList mlObjectList = DomainObject.findObjects(_context, objType.trim(), objName.trim(), revision.trim(), "*",	VAULT_PRODUCTION, null, false, OBJ_SELECT);
		if (mlObjectList.size() >= 1) {
			Iterator<?> itr = mlObjectList.iterator();
			while (itr.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map mEach = (Map) itr.next();
				strObjectId = (String) mEach.get(DomainObject.SELECT_ID);
			}
		} else {
			strObjectId = "";
		}

		return strObjectId;
	}

	private static ArrayList<String> findHPObjects(Context _context, String objType, String objName, String revision, String sWhere) throws Exception {
		ArrayList<String> slIds = new ArrayList<String>();
		String strObjectId = null;
		MapList mlObjectList = DomainObject.findObjects(_context, objType.trim(), objName.trim(), revision.trim(), "*",	VAULT_PRODUCTION, sWhere, false, OBJ_SELECT);
		if (mlObjectList.size() >= 1) {
			Iterator<?> itr = mlObjectList.iterator();
			while (itr.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map mEach = (Map) itr.next();
				strObjectId = (String) mEach.get(DomainObject.SELECT_ID);
				if(null != strObjectId && !strObjectId.isEmpty())
				{
					slIds.add(strObjectId);
				}
			}
		} else {
			strObjectId = "";
		}

		return slIds;
	}

	public static void setEffectivity(Context _context, String strRelId, String strEffectivityExpression, String strFeatureJLR)
			throws Exception {

		// Generating components to call Effectivity Util Method
		Map<String, String> paramMap = new HashMap<String, String>();
		Map<String, Map<String, String>> programMap = new HashMap<String, Map<String, String>>();
		paramMap.put("relId", strRelId);
		paramMap.put("New Value", strEffectivityExpression);
		paramMap.put("objectId", strFeatureJLR);
		programMap.put("paramMap", paramMap);
		String[] args = JPO.packArgs(programMap);
		try {
			// ContextUtil.pushContext(_context);
			EffectivityFramework ef = new EffectivityFramework();
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.open(_context);
			String relType = domRel.getTypeName();
			String relAlias = FrameworkUtil.getAliasForAdmin(_context, "relationship", relType, true);

			MapList relEffTypes = ef.getRelEffectivity(_context, relAlias);
			EffectivityFramework.setRelEffectivityTypes(_context, strRelId, relEffTypes);
			MapList mlEffData = ef.getRelExpression(_context, strRelId);
			if(null == mlEffData || mlEffData.isEmpty())
			{
				ef.updateRelExpression(_context, args);
			}
			else
			{
				Map mpData = (Map) mlEffData.get(0);
				if(null == mpData || mpData.isEmpty())
				{
					ef.updateRelExpression(_context, args);
				}
				else
				{
					String strExp = (String) mpData.get("actualValue");
					if(null == strExp || "".equalsIgnoreCase(strExp.trim()))
					{
						ef.updateRelExpression(_context, args);
					}
				}
			}
		} catch (Exception e) {
			//bwGlobalLogger.write("\n exception-- " + e.getMessage());
			writeLog(bwGlobalLogger,"Error in setEffectivity : " + e.toString());
		} finally {
			// ContextUtil.popContext(_context);
		}
	}

	public static void setEffectivityUI(Context _context, String strRelId, String strEffectivityExpression, String strFeatureJLR)
			throws Exception {
				
		// Generating components to call Effectivity Util Method
		Map<String, String> paramMap = new HashMap<String, String>();
		Map<String, Map<String, String>> programMap = new HashMap<String, Map<String, String>>();
		paramMap.put("relId", strRelId);
		paramMap.put("New Value", strEffectivityExpression);
		paramMap.put("objectId", strFeatureJLR);
		programMap.put("paramMap", paramMap);
		String[] args = JPO.packArgs(programMap);
		try {
			// ContextUtil.pushContext(_context);			
			EffectivityFramework ef = new EffectivityFramework();
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.open(_context);
			String relType = domRel.getTypeName();
			String relAlias = FrameworkUtil.getAliasForAdmin(_context, "relationship", relType, true);
			ef.updateRelExpression(_context, args);	

		} catch (Exception e) {
			//bwGlobalLogger.write("\n exception-- " + e.getMessage());			
			e.printStackTrace();
			throw e;
		} finally {
			// ContextUtil.popContext(_context);
		}
	}

	/**
	 * @param context
	 * @param sRelId
	 * @return strEffectivityExp
	 * @throws Exception
	 */

	public static MapList getEffectiveMapList(Context context, String strEffectivityExp) throws Exception {

		MapList mlOldEff = new MapList();

		try {
			java.util.Scanner lineScanner = new java.util.Scanner(
					strEffectivityExp);
			lineScanner.useDelimiter("\\s*OR\\s*");

			java.util.Scanner tokenScanner = null;
			while (lineScanner.hasNext()) {
				tokenScanner = new java.util.Scanner(lineScanner.next());
				tokenScanner
				.findInLine("\\@EF\\_PR\\(PHY\\@EF\\:(\\w+)\\[PHY\\@EF\\:(\\w+)\\-PHY\\@EF\\:(\\w+)\\]\\)");
				java.util.regex.MatchResult result = null;
				try {
					result = tokenScanner.match();
					if (result.groupCount() == 3) {
						HashMap hmEachExp = new HashMap();
						hmEachExp.put("MODEL_MH_PID", result.group(1));
						hmEachExp.put("SMS_PID", result.group(2));
						hmEachExp.put("FMS_PID", result.group(3));
						mlOldEff.add(hmEachExp);
					}
				} catch (Exception e) {
					tokenScanner
					.findInLine("\\@EF\\_PR\\(PHY\\@EF\\:(\\w+)\\[PHY\\@EF\\:(\\w+)\\-\\^\\]\\)");
					result = tokenScanner.match();
					HashMap hmEachExp = new HashMap();
					hmEachExp.put("MODEL_MH_PID", result.group(1));
					hmEachExp.put("SMS_PID", result.group(2));
					hmEachExp.put("FMS_PID", "EMPTY");
					mlOldEff.add(hmEachExp);
					System.out.println("Exception e" + e);
				}

			}
			tokenScanner.close();
		} catch (Exception e) {
			bwGlobalLogger.write("\n lineScanner Exception " + e);
		}

		return mlOldEff;
	}

	public static String getEffExp(Context context, String sRelId)
			throws Exception {
		Map tempMap = new HashMap();
		MapList gBOMList = new MapList();
		EffectivityFramework ef = new EffectivityFramework();
		String strActualValue = "";
		tempMap.put(DomainConstants.SELECT_RELATIONSHIP_ID, sRelId);
		gBOMList.add(tempMap);
		MapList expressionMap = ef.getRelExpression(context, gBOMList, (double) -5.5, true);
		for (int idx = 0; idx < expressionMap.size(); idx++)

		{

			Map exprMap = (Map) expressionMap.get(idx);

			strActualValue = (String) exprMap.get(EffectivityFramework.ACTUAL_VALUE);

		}
		return strActualValue;
	}

	/**
	 * @param context
	 * @param sRelId
	 * @return strEffectivityExp
	 * @throws Exception
	 */

	public static MapList mergeMapList(Context context,	HashSet<String> deltaModelSet, MapList deltaExprML,	MapList oldEffMapList) throws Exception {

		MapList mlMerged = new MapList();
		oldEffMapList.addSortKey("MODEL_MH_PID", "ascending", "String");
		try {

			Iterator deltaExprMLItr = deltaExprML.iterator();
			while (deltaExprMLItr.hasNext()) {
				HashMap hmNewExpr = (HashMap) deltaExprMLItr.next();

				String sModelMHPid = (String) hmNewExpr.get("MODEL_MH_PID");
				String sSMHPid = (String) hmNewExpr.get("SMS_PID");
				String sFMHPid = (String) hmNewExpr.get("FMS_PID");

				Iterator oldEffMapListItr = oldEffMapList.iterator();
				while (oldEffMapListItr.hasNext()) {
					HashMap hmExpr = (HashMap) oldEffMapListItr.next();
					// if
					// (sModelMHPid.equals((String)hmExpr.get("MODEL_MH_PID"))
					// && sSMHPid.equals((String)hmExpr.get("SMS_PID")) &&
					// sFMHPid.equals((String)hmExpr.get("FMS_PID"))) {
					// if
					// (sModelMHPid.equals((String)hmExpr.get("MODEL_MH_PID"))
					// && sSMHPid.equals((String)hmExpr.get("SMS_PID"))) {
					// if
					// (sModelMHPid.equals((String)hmExpr.get("MODEL_MH_PID")))
					// {
					if (sModelMHPid.equals((String) hmExpr.get("MODEL_MH_PID"))) {
						oldEffMapListItr.remove();
					}
				}
			}
			mlMerged.addAll(deltaExprML);
			mlMerged.addAll(oldEffMapList);
			mlMerged.addSortKey("MODEL_MH_PID", "ascending", "String");

		} catch (Exception e) {
			bwGlobalLogger.write("\n Exception " + e);
		}

		return mlMerged;
	}

	/**
	 * @param context
	 * @param sRelId
	 * @return strEffectivityExp
	 * @throws Exception
	 */

	public static String getNewInputExpr(Context context, MapList mlNewInputExpr)
			throws Exception {

		String sNewInputExpr = "";
		try {

			Iterator mlNewInputExprItr = mlNewInputExpr.iterator();
			while (mlNewInputExprItr.hasNext()) {
				String sEachExpr = "";
				boolean isOutEffectivity = true;
				HashMap hmExpr = (HashMap) mlNewInputExprItr.next();
				String strFMS = (String) hmExpr.get("FMS_PID");
				if (strFMS.equalsIgnoreCase("EMPTY")) {
					strFMS = "^";
					isOutEffectivity = false;
				}
				if (isOutEffectivity)
					sEachExpr = "@EF_PR(PHY@EF:" + (String) hmExpr.get("MODEL_MH_PID") + "[PHY@EF:" + (String) hmExpr.get("SMS_PID") + "-PHY@EF:" + strFMS + "])";
				else
					sEachExpr = "@EF_PR(PHY@EF:" + (String) hmExpr.get("MODEL_MH_PID") + "[PHY@EF:"	+ (String) hmExpr.get("SMS_PID") + "-" + strFMS	+ "])";

				if (!sNewInputExpr.equals("")) {
					sNewInputExpr += " OR ";
					sNewInputExpr += sEachExpr;
				} else
					sNewInputExpr = sEachExpr;
			}
		} catch (Exception e) {
			bwGlobalLogger.write("\n Exception " + e);
		}

		return sNewInputExpr;
	}


	/**
	 * This method is used to create and attached document to Hardware Model
	 * @param context
	 * @param String -- strHardwareProductId  Hardware Model ID
	 * @param Map -- mpLogFile All Log File Details
	 * @throws 
	 * @returns String -- Void
	 * Created by anarkhed 4-dec-2015
	 */
	public static void sendNotification(Context context, String strModelName, String strDataInfo)throws Exception
	{

		//StringList slTO = new StringList();

		//slTO.addElement("rsinghal");


		String baseUrl = JPO.invoke(context, "emxMailUtil", null, "getBaseURL", null,String.class);
		String strEnvMsg ="\n 2017X Environment URL:- \n"+baseUrl;

		String strLanguage = context.getSession().getLanguage();

		String strSubjectKey = "Ok2useList has been updated for Model "+strModelName;
		String strMessage = "Following Feature Data have been updated/created against this Ok2uselist Delta Message\n";

		try
		{
			/*MailUtil.sendNotification(context,
								slTO, null, null,
								strSubjectKey,null, null,
								strMessage+strDataInfo, null, null,
								null, null,
								null) ;*/


			String strPublishEventMailGroup = readConfigurationFile(context, "Ok2uselistMailGroup");
			//Processing SMTP Name, Users mailId list
			if(UIUtil.isNotNullAndNotEmpty(strPublishEventMailGroup))
			{
				String[] strSMTPUserList = strPublishEventMailGroup.split(":");
				String strToListName = strSMTPUserList[0]; 
				String strFromListName = strSMTPUserList[1]; 
				Map<String, String> env = System.getenv();
				String strSMTPName = (String)env.get("MX_SMTP_HOST");
				String striPLMIsProductionEnvironment = readConfigurationFile(context, "isProductionEnvironment");
				if(UIUtil.isNotNullAndNotEmpty(strSMTPName) && striPLMIsProductionEnvironment.equalsIgnoreCase("TRUE"))
				{
					MxMessage messagebase = new MxMessage();
					messagebase.sendJavaMail(context, false);
					messagebase.setMessage(strDataInfo+strEnvMsg);
					messagebase.setSubject(strSubjectKey);
					messagebase.sendJavaMail(context, strSMTPName, strToListName, null, null, strFromListName, null);	
				}								
			}	
		}
		catch(Exception e)
		{
			//	logger.error(e);
			//	throw e;
		}
	}


	/**
	 * Generic Method to read Configuration File Object and return Description
	 * 
	 * @param context
	 * @param strConfigurationFileName - Name of configuration File
	 * @return String - Description of configuration file
	 */
	private static String readConfigurationFile(Context context, String strConfigurationFileName) {

		String strDescription = "";

		try {

			StringList slbusSelects = new StringList(DomainConstants.SELECT_DESCRIPTION);

			MapList mlConfigurationFiles = DomainObject.findObjects(context,
					"Configuration File",
					strConfigurationFileName,
					DomainConstants.QUERY_WILDCARD,
					DomainConstants.QUERY_WILDCARD,
					"eService Production",
					null,
					true,
					slbusSelects);

			if (mlConfigurationFiles.size() > 0)
			{
				Map mpObjectMap = (Map) mlConfigurationFiles.get(0);
				strDescription = (String) mpObjectMap.get(DomainConstants.SELECT_DESCRIPTION);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return strDescription;
	}


	/**
	 * Generic Method to read a Page Object.
	 * @param _context
	 * @param strPageObject - Name of Page Object to be read
	 * @return propertyEntry - All the property entries
	 * @throws Exception
	 */
	public static Properties readPageObject(Context _context,String strPageObject) throws Exception
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

	//Added for Feature Filter - Start
	public Map getRangeForVehicleFilter(Context context, String[] args) throws Exception
	{			
		Map rangeMap = new HashMap();
		//Map programMap = (Map)JPO.unpackArgs(args);

		StringList slDisplayList = new StringList();
		slDisplayList.addElement("");		

		StringList slOriginalList = new StringList();		
		slOriginalList.addElement("");		
		
		//Added by phardare on 4th October 2018 for CR72 (Incident-17667:Only Active Tasks In Hopper) - STARTS
		StringBuffer sbWhere = new StringBuffer();
		sbWhere.append("from["+DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"].attribute["+ATTRIBUTE_HOOPER_STATE+"].value==Add");
		sbWhere.append(" || ");
		sbWhere.append("from["+DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"].attribute["+ATTRIBUTE_HOOPER_STATE+"].value==Remove");
		//Added by phardare on 4th October 2018 for CR72 (Incident-17667:Only Active Tasks In Hopper) - ENDS

		StringList slSelect = new StringList();
		slSelect.addElement(DomainConstants.SELECT_NAME);
		slSelect.addElement(DomainConstants.SELECT_ID);

		// Get All tasks connected to CO and CF
		MapList mlTaskData = null;
		try
		{
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			mlTaskData = DomainObject.findObjects(context, TYPE_TASK,	"*", "*", "*",	VAULT_PRODUCTION, sbWhere.toString(), true, slSelect);
		}
		catch(Exception ex)
		{

		}
		finally
		{
			ContextUtil.popContext(context);
		}
		
		if(null != mlTaskData && !mlTaskData.isEmpty())
		{
			int iSize = mlTaskData.size();
			Map mpTask = null;
			String strId = null;
			String strName = null;
			for(int i=0; i<iSize; i++)
			{
				mpTask = (Map) mlTaskData.get(i);

				if(null != mpTask && !mpTask.isEmpty())
				{
					strId = (String) mpTask.get(DomainConstants.SELECT_ID);
					strName = (String) mpTask.get(DomainConstants.SELECT_NAME);
					slOriginalList.addElement(strId);
					slDisplayList.addElement(strName);
				}
			}
		}

		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;

	}

	public MapList getVehicleRelatedConfiguration(Context context,String[] args) throws Exception
	{		
		Map programMap = (Map)JPO.unpackArgs(args);		
		String sPersonFilter = (String) programMap.get("iPLMVehicleLineFilter");
		//String sPersonFilter =	" 12288.3952.3840.35951";
		MapList mlReturnList = new MapList();

		//Logic to fetch selected Person(sPersonFilter) Organizations			
		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		objectSelects.addElement(DomainConstants.SELECT_NAME);//    
		objectSelects.addElement(DomainConstants.SELECT_TYPE);// 

		//String strRelWhere = "attribute[iPLMHooperState]!=Published";
		String strRelWhere = "attribute["+ATTRIBUTE_HOOPER_STATE+"]!=Published && attribute["+ATTRIBUTE_HOOPER_STATE+"]!=PublishedRemoved";//phardare-17616

		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
		relSelects.addElement("attribute[iPLMHooperState]");

		if (UIUtil.isNotNullAndNotEmpty(sPersonFilter)){
			try
			{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				DomainObject dom = new DomainObject(sPersonFilter);
				
				//Added by phardare on 8th October 2018 for CR72 (Incident-17635:Check WIPs) - STARTS
				String sTaskName = dom.getName(context);
				MapList mlFinalWIPModels = getAllWIPModelsForSpecificTaskName(context, sTaskName);
				if(mlFinalWIPModels.isEmpty()){
					MqlUtil.mqlCommand(context, "notice \"There are no LDIs available for : "+sTaskName+"\n\nAt least 1 \'Hardware Product\' should be in \'Product Management\' (WIP) state to accept the features.\"");
				} else {
				//Added by phardare on 8th October 2018 for CR72 (Incident-17635:Check WIPs) - ENDS				
				mlReturnList = dom.getRelatedObjects(context,
						DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT, // rel pattern
						TYPE_CONFIGURATION_OPTION,// type pattern
						//TYPE_CONFIGURATION_OPTION+","+TYPE_CONFIGURATION_FEATURES,// type pattern
						objectSelects, // object selects
						relSelects,// rel selects
						true,// get To
						true,// get from
						(short) 1,// recurse to
						"", // object
						strRelWhere, // relationship where
						0);
				}//phardare-17635
			}
			catch(Exception ex)
			{

			}
			finally
			{
				ContextUtil.popContext(context);
			}
		}
		
		return mlReturnList;		
	}

	public void processAcceptedList(Context context, String[] args) throws Exception
	{		
		if(null != args && args.length>0)
		{
			String strData = args[0];			
			String[] saArray = strData.split(",");
			if(null != saArray && saArray.length>0)
			{
				int iLength = saArray.length;
				try
				{
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					for(int i=0;i<iLength;i++)
					{
						//Start
						//processConfigurationRow( context,  saArray[i]);						
						processConfigurationRequest(context, saArray[i]);						
					}
				}
				catch(Exception ex)
				{

				}
				finally
				{
					ContextUtil.popContext(context);
				}
			}
		}
	}

	public static void connectTask(Context _context, DomainObject dmTask, DomainObject dmObj,String strOperaionMode)
	{
		try
		{
			// Check Task is connected or not
			String strTaskName = dmTask.getName(_context);
			StringList slObjectSelects = new StringList(2);
			slObjectSelects.addElement(DomainConstants.SELECT_TYPE);
			slObjectSelects.addElement(DomainConstants.SELECT_NAME);

			StringList slRelSelects = new StringList(1);
			slRelSelects.addElement("id");
			//Added for Add/Remove issue fix - Start
			slRelSelects.addElement("attribute["+ATTRIBUTE_HOOPER_STATE+"].value");
			
			String sCurAttrVal = "";
			//Added for Add/Remove issue fix - End
			

			String sWhere = "name == \""+strTaskName+"\"";

			//DomainObject dom = new DomainObject(sPersonFilter);
			MapList mlReturnList = dmObj.getRelatedObjects(_context,
					DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT, // rel pattern
					TYPE_TASK,// type pattern
					slObjectSelects, // object selects
					slRelSelects,// rel selects
					true,// get To
					false,// get from
					(short) 1,// recurse to
					sWhere, // object
					"", // relationship where
					0);
			if(null == mlReturnList || mlReturnList.isEmpty())
			{
				DomainRelationship domRelObj = new DomainRelationship();
				domRelObj = DomainRelationship.connect(_context, dmTask,DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT, dmObj);
				try
				{
					MqlUtil.mqlCommand(_context, strConnectionMQL, true,	domRelObj.toString(), OWNING_ORGANIZATION, OWNING_PROJECT);
				}
				catch(Exception exp)
				{
					writeLog(bwGlobalLogger,"Error in connectTask (MQL) : " + exp.toString());
				}
				StringList slSelect = new StringList();;
				slSelect.addElement("id");
				Hashtable mpData = domRelObj.getRelationshipData(_context, slSelect);
				if(null != mpData && !mpData.isEmpty())
				{
					StringList strRelId = (StringList) mpData.get("id");
					if(null != strRelId && !strRelId.isEmpty())
					{
						String strIntf       = "mod connection $1 add interface \"$2\"";
						String strIntfResult   = MqlUtil.mqlCommand(_context, strIntf, (String) strRelId.get(0), "iPLMHooperInterface");
						domRelObj.setAttributeValue(_context, "iPLMHooperState", strOperaionMode);
					}
				}

			}
			else
			{				
				Map mpReldata = (Map) mlReturnList.get(0);
				if(null != mpReldata && !mpReldata.isEmpty())
				{
					String strRelId = (String) mpReldata.get("id");
					//Added for Add/Remove issue fix - Start
					sCurAttrVal = (String) mpReldata.get("attribute["+ATTRIBUTE_HOOPER_STATE+"].value");
					if(UIUtil.isNotNullAndNotEmpty(sCurAttrVal) && UIUtil.isNotNullAndNotEmpty(strRelId)){
						
						DomainRelationship domRelObj = new DomainRelationship(strRelId);
						
						if("Remove".equals(sCurAttrVal) && "Add".equals(strOperaionMode)){	
							
							domRelObj.setAttributeValue(_context, ATTRIBUTE_HOOPER_STATE, "Published");	
							//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " updated with Published value \n");
							writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " updated with Published value");
							
						}else if("Add".equals(sCurAttrVal) && "Remove".equals(strOperaionMode)){
							
							DomainRelationship.disconnect(_context, strRelId);
							//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " disconnected \n");
							writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " disconnected");
						
						} else {
							
							domRelObj.setAttributeValue(_context, ATTRIBUTE_HOOPER_STATE, strOperaionMode);
							//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " updated with strOperaionMode = "+strOperaionMode+" value \n");
							writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " updated with strOperaionMode = "+strOperaionMode+" value");
						}
						
					} else {
						if(null != strRelId && !strRelId.isEmpty())
						{
							DomainRelationship domRelObj = new DomainRelationship(strRelId);
							domRelObj.setAttributeValue(_context, "iPLMHooperState", strOperaionMode);
							//bwGlobalLogger.write("[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " updated with var strOperaionMode = "+strOperaionMode+" value \n");
							writeLog(bwGlobalLogger,"[" + simpleDateFormat.format(new Date()) + "] INFO - Relationship with " + strTaskName + " updated with var strOperaionMode = "+strOperaionMode+" value");
						}
					}
					//Added for Add/Remove issue fix - End					
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			writeLog(bwGlobalLogger,"Error in connectTask (MQL) : " + ex.toString());
		}
	}

	public static void processConfigurationRow(Context _context, String strRelId)
	{
		//String strRelId = "12288.3952.24833.42081";
		StringList slSelect = new StringList(6);
		//slSelect.addElement("from.id");
		slSelect.addElement("from.name");
		slSelect.addElement("to.id");
		slSelect.addElement("to.name");
		slSelect.addElement("to.type");
		slSelect.addElement("id");
		slSelect.addElement("attribute[iPLMHooperState]");

		//String[] saRel = {"12288.3952.24833.42081"};
		try
		{
			if(null != strRelId && !strRelId.isEmpty())
			{
				
				DomainRelationship domRelObj = new DomainRelationship(strRelId);
				Hashtable mpData = domRelObj.getRelationshipData(_context, slSelect);
				
				if(null != mpData && !mpData.isEmpty())
				{
					StringList strTaskName = (StringList) mpData.get("from.name");
					StringList strConfigId = (StringList) mpData.get("to.id");
					StringList strPoerationMode = (StringList) mpData.get("attribute[iPLMHooperState]");
					//StringList strTaskId = (StringList) mpData.get("from.id");
					StringList strConfigType = (StringList) mpData.get("to.type");
					if(null != strTaskName && !strTaskName.isEmpty() && null != strConfigId && !strConfigId.isEmpty())
					{
						DomainObject domCO = DomainObject.newInstance(_context, (String) strConfigId.get(0));
						String strCFId = domCO.getInfo(_context,"to[Configuration Options].from.id");
						
						DomainObject dmCF = new DomainObject(strCFId);							
						String strConfigurationOptionsRelId = domCO.getInfo(_context,	"to[Configuration Options].id");
						
						ArrayList<String> alIds = findHPObjects(_context,"Hardware Product", (String) strTaskName.get(0)+"*","*","current==\"Product Management\"");
												
						if(null != alIds && !alIds.isEmpty())
						{
							int iIDcnt = alIds.size();
							String strHPId =null;
							
							for(int j=0;j<iIDcnt;j++)
							{
								strHPId = alIds.get(j);
								
								if(null != strHPId && !strHPId.isEmpty())
								{
									DomainObject dobjHP = new  DomainObject(strHPId);

									if(null != strPoerationMode && strPoerationMode.contains("Add"))
									{								
										StringList  strRelatedHardwareProductList=dmCF.getInfoList(_context,"to[Configuration Features].from.id");
										if(null != strConfigType && !strConfigType.isEmpty() && "Configuration Option".equalsIgnoreCase((String)strConfigType.get(0)))
										{
											if(!strRelatedHardwareProductList.contains(dobjHP.getObjectId()))
											{
												// Connect HP with CF
												DomainRelationship domRel = new DomainRelationship();
												domRel = DomainRelationship.connect(_context,dobjHP,"Configuration Features" ,dmCF); 
												try
												{
													MqlUtil.mqlCommand(_context, strConnectionMQL, true,	domRel.toString(), OWNING_ORGANIZATION, "PI");
												}
												catch(Exception ex)
												{

												}
											}
										}
										// Update Attribute of TASK-CO relationship
										domRelObj.setAttributeValue(_context, "iPLMHooperState", "Published");

									}
									else if(null != strPoerationMode && strPoerationMode.contains("Remove"))
									{
										
										StringList slOptions = dmCF.getInfoList(_context, "from[Configuration Options].to.name");										

										// IF Respect CF has only one CO i.e CO for which delete request Accepted, then remove connection between HP and CF
										if(null != slOptions && slOptions.size()==1)
										{
											String strObjWhere = "id==\""+strHPId+"\" && current == \"Product Management\"";
											StringList objectSelects = new StringList(2);
											objectSelects.addElement(DomainConstants.SELECT_TYPE);
											objectSelects.addElement(DomainConstants.SELECT_NAME);
											StringList relSelects = new StringList();
											relSelects.addElement("id");
											MapList mlReturnList = dmCF.getRelatedObjects(_context,
													"Configuration Features", // rel pattern
													"Hardware Product",// type pattern
													objectSelects, // object selects
													relSelects,// rel selects
													true,// get To
													true,// get from
													(short) 1,// recurse to
													strObjWhere, // object
													null, // relationship where
													0);
											
											if(null != mlReturnList && !mlReturnList.isEmpty())
											{
												int iSize = mlReturnList.size();
												for(int i=0;i<iSize; i++)
												{
													Map mpRelData = (Map) mlReturnList.get(i);
													if(null != mpRelData && !mpRelData.isEmpty())
													{
														String sRelId = (String) mpRelData.get("id");
														
														if(null != sRelId && !sRelId.isEmpty())
														{
															// Disconnect relationship with HP
															try
															{
																ContextUtil.pushContext(_context, PropertyUtil.getSchemaProperty(_context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);			
																MqlUtil.mqlCommand(_context, "trigger off", true);
																String strIntf       = "delete connection $1 ";
																String strIntfResult   = MqlUtil.mqlCommand(_context, strIntf, sRelId);
															}
															catch(Exception ex)
															{

															}
															finally 
															{
																MqlUtil.mqlCommand(_context, "trigger on", true);
																ContextUtil.popContext(_context);
															}
														}
													}
												}
											}

										}
										// Delete relation between TASK and CO
										if(j==0)
										{
											String strIntf       = "delete connection $1 ";
											
											String strIntfResult   = MqlUtil.mqlCommand(_context, strIntf, strRelId);
										}
									}
								}
							}
							
							// Update Effectivity
							// Get GFD expression
							String strFinalExpn = "";
							String strGFDId = findHPObjects(_context,"Hardware Product", "GFD","A");
							String strGFDExpression = generateEffctExpression(_context,strGFDId);
							
							String sTask = (String) strTaskName.get(0);
							String[] saTask = sTask.split("-");
							String strModelName = saTask[0];
							String strModelId = findHPObjects(_context,"Model", strModelName,"");
							
							String strFilteredExp = "";
														
							if(null != strHPId && !"".equalsIgnoreCase(strHPId))
							{								
								strFilteredExp =  removeModelFromExprn(_context, strConfigurationOptionsRelId,  strHPId,(String) strTaskName.get(0));
								

								if(null == strFilteredExp || "".equalsIgnoreCase(strFilteredExp))
								{
									//
								}
								else
								{
									strGFDExpression = strFilteredExp;
								}

								if(null != strPoerationMode && strPoerationMode.contains("Add"))
								{
									
									String strDerExpn = generateExpressionWithCombinationBranch(_context,strModelId,(String)strPoerationMode.get(0),strCFId,(String) strTaskName.get(0));
									
									if(null == strDerExpn || "".equalsIgnoreCase(strDerExpn.trim()))
									{
										strFinalExpn = strGFDExpression;
									}
									else
									{
										strFinalExpn = strGFDExpression +" OR "+strDerExpn;
									}									
									setEffectivityUI(_context, strConfigurationOptionsRelId, strFinalExpn, (String) strConfigId.get(0));									

								}
								else if(null != strPoerationMode && strPoerationMode.contains("Remove"))
								{
									
									//String strDerExpn = generateExpressionWithCombinationDelete(_context,strModelId,strCFId);
									
									String strDerExpn = generateExpressionWithCombinationBranch(_context,strModelId,(String)strPoerationMode.get(0),strCFId,(String) strTaskName.get(0));
									
									
									if(null == strDerExpn || "".equalsIgnoreCase(strDerExpn.trim()))
									{
										strFinalExpn = strGFDExpression;
									}
									else
									{
										strFinalExpn = strGFDExpression +" OR "+strDerExpn;
									}
									
									setEffectivityUI(_context, strConfigurationOptionsRelId, strFinalExpn, (String) strConfigId.get(0));
									
								}
							}
						}
					}
				}				
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public StringList getStates(Context context, String[] args)
	{
		StringList slStates = new StringList();
		try
		{
			Map programMap = (Map)JPO.unpackArgs(args);	
			
			Map paramList = (Map) programMap.get("paramList");
			
			String strTaskId =  (String) paramList.get("iPLMVehicleLineFilter");
			
			MapList objectList =  (MapList)programMap.get("objectList");
			
			if(null != objectList && !objectList.isEmpty() && null != strTaskId && !strTaskId.isEmpty())
			{
				int iSize = objectList.size();
				Map mpData = null;
				String strConfId = null;
				for(int i=0; i<iSize; i++)
				{
					mpData = (Map) objectList.get(i);
					if(null != mpData && !mpData.isEmpty())
					{
						strConfId = (String) mpData.get("id");

						if(null != strConfId && !strConfId.isEmpty())
						{
							StringList slObjectSelects = new StringList(2);
							slObjectSelects.addElement(DomainConstants.SELECT_TYPE);
							slObjectSelects.addElement(DomainConstants.SELECT_NAME);

							StringList slRelSelects = new StringList(2);
							slRelSelects.addElement("id");
							slRelSelects.addElement("attribute[iPLMHooperState]");


							String sWhere = "id == \""+strTaskId+"\"";
							MapList mlReturnList = null;
							try
							{
								ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
								DomainObject dmObj = new DomainObject(strConfId);
								mlReturnList = dmObj.getRelatedObjects(context,
										DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT, // rel pattern
										TYPE_TASK,// type pattern
										slObjectSelects, // object selects
										slRelSelects,// rel selects
										true,// get To
										false,// get from
										(short) 1,// recurse to
										sWhere, // object
										"", // relationship where
										0);
							}
							catch(Exception ex)
							{

							}
							finally
							{
								ContextUtil.popContext(context);
							}

							if(null != mlReturnList && !mlReturnList.isEmpty() && mlReturnList.size() == 1)
							{
								Map mpRelData =  (Map) mlReturnList.get(0);
								if(null != mpRelData && !mpRelData.isEmpty())
								{
									String strState = (String) mpRelData.get("attribute[iPLMHooperState]");
									slStates.addElement(strState);
								}
							}
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return slStates;
	}

	public boolean hasLeaderAccess(Context context, String[] args)
			throws Exception
	{
		boolean access = false;
		String str = PersonUtil.getDefaultSecurityContext(context,context.getUser());

		if(null != str && "VPLMProjectLeader.Non-Commodity.PI".equalsIgnoreCase(str))
		{
			access = true;
		}
		
		return access;
	}


	private static ArrayList<String> findDerivatives(Context _context, String strModelId,String strTaskName) throws Exception {
		ArrayList<String> alData = new ArrayList<String>();

		DomainObject dmModel = new DomainObject(strModelId);
		String strObjWhere = "current == \"Release\" || current == \"Product Management\"";
		StringList slData = new StringList(5);
		slData.addElement(DomainObject.SELECT_TYPE);
		slData.addElement(DomainObject.SELECT_REVISION);
		slData.addElement(DomainObject.SELECT_ID);
		slData.addElement(DomainObject.SELECT_NAME);
		slData.addElement(DomainObject.SELECT_CURRENT);


		MapList mlObjectList = dmModel.getRelatedObjects(_context,
				"Products", // rel pattern
				"Hardware Product",// type pattern
				slData, // object selects
				null,// rel selects
				false,// get To
				true,// get from
				(short) 1,// recurse to
				strObjWhere, // object
				null, // relationship where
				0);		

		
		if (null != mlObjectList && !mlObjectList.isEmpty()) {
			int iSize = mlObjectList.size();
			Map mpData = null;
			String strId = null;
			String strRev = null;
			String strCurrent = null; 
			String strRel = null;
			String strName = null;

			for(int i=0;i<iSize;i++)
			{
				mpData = (Map) mlObjectList.get(i);
				if(null != mpData && !mpData.isEmpty())
				{
					strId = (String) mpData.get(DomainObject.SELECT_ID);
					strRev = (String) mpData.get(DomainObject.SELECT_REVISION);
					strCurrent = (String) mpData.get(DomainObject.SELECT_CURRENT);
					strName = (String) mpData.get(DomainObject.SELECT_NAME);
					strRel = (String) mpData.get("relationship");
					if(null != strName && strName.contains(strTaskName))
					{
						if(null != strId && !"".equalsIgnoreCase(strId) && null != strRev && !"".equalsIgnoreCase(strRev) && null != strRel && "Products".equalsIgnoreCase(strRel))
						{						
							alData.add(strRev+"|"+strId+"|"+strCurrent);						
						}
					}
				}
			}		

		}

		Collections.sort(alData);
		return alData;
	}
	//Added for Feature Filter - End

	public void testExpression(Context context, String[] args)
	{
		try
		{
			String strGFDId = findHPObjects(context,"Hardware Product", "GFD","A");
			String strGFDExpression = generateEffctExpression(context,strGFDId);			

/*
			String strModelId = args[0];
			System.out.println("@@@@@@@@@@@@@@@@@strModelId : "+strModelId);
			String strExp = generateExpressionWithCombinationBranch(context,strModelId,"Add","20032.59212.14080.36213");
			System.out.println("@@@@@@@@@@@@@@@@@strExp : "+strExp);
*/
			/*	
			String strModelId = args[0];
			System.out.println("@@@@@@@@@@@@@@@@@strModelId : "+strModelId);
			String strExp = generateExpressionWithCombinationBranch(context,strModelId,"Remove","20032.59212.14080.36213");
			System.out.println("@@@@@@@@@@@@@@@@@strExp : "+strExp);*/
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static String generateExpressionWithCombinationBranch(Context _context, String strModelId, String strOperationMode, String strCFId,String strTaskName) throws Exception
	{
		String strExpression = null;
		ArrayList<String> alData = findDerivatives(_context,strModelId,strTaskName);
		
		int iSize = alData.size();
		ArrayList<String> alRevA = new ArrayList<String>();
		ArrayList<String> alRevB = new ArrayList<String>();
		ArrayList<String> alRevC = new ArrayList<String>();
		String strTempData = null;
		for(int iRevItr=0;iRevItr<iSize;iRevItr++)
		{
			strTempData = alData.get(iRevItr);
			
			if(null != strTempData && strTempData.startsWith("A."))
			{
				alRevA.add(strTempData);
			}
			else if(null != strTempData && strTempData.startsWith("B."))
			{
				alRevB.add(strTempData);
			}
			else if(null != strTempData && strTempData.startsWith("C."))
			{
				alRevC.add(strTempData);
			}
		}
		
		String strExpressionA = null;
		String strExpressionB = null;
		String strExpressionC = null;

		if(null != strOperationMode && "Add".equalsIgnoreCase(strOperationMode))
		{
			strExpressionA = generateExpressionWithCombination(_context, alRevA,strCFId);
			strExpressionB = generateExpressionWithCombination(_context, alRevB,strCFId);
			strExpressionC = generateExpressionWithCombination(_context,  alRevC,strCFId);
		}
		else if(null != strOperationMode && "Remove".equalsIgnoreCase(strOperationMode))
		{
			strExpressionA = generateExpressionWithCombinationDelete(_context, alRevA,strCFId);
			strExpressionB = generateExpressionWithCombinationDelete(_context, alRevB,strCFId);
			strExpressionC = generateExpressionWithCombinationDelete(_context,  alRevC,strCFId);
		}

		boolean isStarted = false;
		if(null != strExpressionA && !"".equalsIgnoreCase(strExpressionA))
		{
			isStarted = true;
			strExpression = strExpressionA;
		}
		if(null != strExpressionB && !"".equalsIgnoreCase(strExpressionB))
		{			
			if(isStarted)
			{
				strExpression = strExpression + " OR " + strExpressionB;
			}
			else
			{
				isStarted = true;
				strExpression = strExpressionB;
			}
		}
		if(null != strExpressionC && !"".equalsIgnoreCase(strExpressionC))
		{
			if(isStarted)
			{
				strExpression = strExpression + " OR " + strExpressionC;
			}
			else
				strExpression = strExpressionC;
		}

		return strExpression;
	}
	public static String generateExpressionWithCombination(Context _context, ArrayList<String> alData, String strCFId) throws Exception
	{
		String strExpression = null;


		if(null != alData && !alData.isEmpty())
		{			
			if(null != alData && !alData.isEmpty())
			{
				int iSize = alData.size();			
				String strTemp = null;
				boolean isFRZExist = false;
				String[] saData = null;
				String[] saDataTemp = null;
				String strEffectExpression = null;

				String strVehicleId = null;
				DomainObject dmVeh = null;
				StringList slConnectdCF = null;
				boolean isVehConnected = false;




				for(int i=0;i<iSize;i++)
				{
					strTemp = alData.get(i);
					
					strVehicleId = null;
					dmVeh = null;
					slConnectdCF = null;
					isVehConnected = false;

					saDataTemp = strTemp.split("\\|");
					strVehicleId = saDataTemp[1];
					
					if(null != strVehicleId)
					{
						dmVeh = new DomainObject(strVehicleId);
						slConnectdCF = dmVeh.getInfoList(_context, "from[Configuration Features].to.id");
						
						if(null != slConnectdCF && !slConnectdCF.isEmpty())
						{
							isVehConnected = slConnectdCF.contains(strCFId);							
						}
					}
					
					if(null != strTemp && strTemp.endsWith("|Product Management"))
					{

						if(null == strExpression)
						{

							saData = strTemp.split("\\|");
							strEffectExpression = generateEffctExpression(_context,saData[1]);
							strExpression = strEffectExpression;
							
						}
					}
					else if(null != strTemp && strTemp.endsWith("|Release"))
					{
						if(null == strExpression && isVehConnected)
						{
							saData = strTemp.split("\\|");
							strEffectExpression = generateEffctExpression(_context,saData[1]);
							strExpression = strEffectExpression;
							
						}
						else if(null != strExpression && !"".equalsIgnoreCase(strExpression) && !isVehConnected)
						{
							saData = strTemp.split("\\|");
							strEffectExpression = generateEffctExpression(_context,saData[1]);
							strExpression = strExpression +" AND NOT " +strEffectExpression;
							
						}
					}
				}

			}
		}
		
		return strExpression;
	}

	/*
	  public static String generateExpressionWithCombination(Context _context, ArrayList<String> alData) throws Exception
	{
		String strExpression = null;


		if(null != alData && !alData.isEmpty())
		{			
			if(null != alData && !alData.isEmpty())
			{
				int iSize = alData.size();			
				String strTemp = null;
				boolean isFRZExist = false;
				String[] saData = null;
				String strEffectExpression = null;
				for(int i=0;i<iSize;i++)
				{
					strTemp = alData.get(i);
					System.out.println("@@@@@@@@@@@strTemp : "+strTemp);
					if(null != strTemp && strTemp.endsWith("|Product Management"))
					{

						if(null == strExpression)
						{

							saData = strTemp.split("\\|");
							strEffectExpression = generateEffctExpression(_context,saData[1]);
							strExpression = strEffectExpression;
						}
					}
					else if(null != strTemp && strTemp.endsWith("|Release"))
					{
						// Means In start it is finding FRZ Derivations
						if(null == strExpression)
						{
							// Skip these Derivations
						}
						else
						{
							saData = strTemp.split("\\|");
							strEffectExpression = generateEffctExpression(_context,saData[1]);
							strExpression = strExpression +" AND NOT " +strEffectExpression;
						}
					}
				}

			}
		}
		return strExpression;
	}
	 */
	public static String generateEffctExpression(Context _context, String strHPId) throws Exception
	{
		String strExp = null;
		try
		{
			DomainObject dobjHP = new  DomainObject(strHPId);

			//Prepare effectivity statement 
			String strHPPhysicalId = dobjHP.getInfo(_context,"physicalid");
			String strModelPhysicalId = dobjHP.getInfo(_context,"to[Main Product].from.physicalid");
			String strModelId = dobjHP.getInfo(_context,"to[Main Product].from.id");

			if (strModelPhysicalId == null || "".equalsIgnoreCase(strModelPhysicalId) || "null".equalsIgnoreCase(strModelPhysicalId)) {
				strModelPhysicalId = dobjHP.getInfo(_context,"to[Products].from.physicalid");
				strModelId = dobjHP.getInfo(_context,"to[Products].from.id");
			}		

			StringBuffer strEffectivity = new StringBuffer();
			//Changed below line to include IDX instead of PHY
			strEffectivity.append("@EF_PR(PHY@EF:");
			//strEffectivity.append("@EF_PR(IDX@EF:");
			strEffectivity.append(strModelPhysicalId);
			//Changed below line to include IDX instead of PHY
			strEffectivity.append("[PHY@EF:");
			//strEffectivity.append("[IDX@EF:");
			strEffectivity.append(strHPPhysicalId);
			strEffectivity.append("-^])");

			strExp = strEffectivity.toString();
		}
		catch(Exception ex)
		{

		}
		return strExp;
	}

	//public static String generateExpressionWithCombinationDelete(Context _context, String strModelId, String strCFId) throws Exception
	public static String generateExpressionWithCombinationDelete(Context _context, ArrayList<String> alData, String strCFId) throws Exception
	{
		String strExpression = null;
		//ArrayList<String> alData = findDerivatives(_context,strModelId);
		//System.out.println("@@@@@@@@@@@@@@@@@alData : "+alData);

		if(null != alData && !alData.isEmpty())
		{			
			if(null != alData && !alData.isEmpty())
			{
				int iSize = alData.size();			
				String strTemp = null;
				boolean isFRZExist = false;
				String[] saData = null;
				String[] saDataTemp = null;
				String strEffectExpressionFRZ = null;
				String strEffectExpressionWIP = null;
				String strVehicleId = null;
				DomainObject dmVeh = null;
				StringList slConnectdCF = null;
				boolean isVehConnected = false;
				for(int i=0;i<iSize;i++)
				{
					strTemp = alData.get(i);
					
					saDataTemp = strTemp.split("\\|");
					strVehicleId = null;
					dmVeh = null;
					slConnectdCF = null;
					isVehConnected = false;
					strVehicleId = saDataTemp[1];
					if(null != strVehicleId)
					{
						dmVeh = new DomainObject(strVehicleId);
						slConnectdCF = dmVeh.getInfoList(_context, "from[Configuration Features].to.id");
						if(null != slConnectdCF && !slConnectdCF.isEmpty())
						{
							isVehConnected = slConnectdCF.contains(strCFId);
							
						}
					}

					if(null != strTemp && strTemp.endsWith("|Product Management"))
					{

						if(null == strEffectExpressionWIP)
						{

							saData = strTemp.split("\\|");
							strEffectExpressionWIP = generateEffctExpression(_context,saData[1]);
							
						}
					}
					else if(null != strTemp && strTemp.endsWith("|Release") && isVehConnected)
					{						
						if(null == strEffectExpressionFRZ)
						{
							saData = strTemp.split("\\|");
							strEffectExpressionFRZ = generateEffctExpression(_context,saData[1]);							
						}

					}
				}

				if(null == strEffectExpressionFRZ)
				{
					// Default expression
				}
				else
				{
					if(null == strEffectExpressionWIP)
					{
						strExpression = strEffectExpressionFRZ;
						
					}
					else
					{
						strExpression = strEffectExpressionFRZ + " AND NOT " +strEffectExpressionWIP;
						
					}
				}

			}
		}
		
		return strExpression;
	}

	public static String removeModelFromExprn(Context context, String strRelId, String strHPId,String strTaskName) throws Exception
	{
		String strNewExpn = null;
		try
		{

			
			// Get ModelPhysicalId
			DomainObject dobjHP = new  DomainObject(strHPId);
			String strModelPhysicalId = dobjHP.getInfo(context,"to[Main Product].from.physicalid");

			if (strModelPhysicalId == null || "".equalsIgnoreCase(strModelPhysicalId) || "null".equalsIgnoreCase(strModelPhysicalId)) {
				strModelPhysicalId = dobjHP.getInfo(context,"to[Products].from.physicalid");
			}	
						
			
			// Remove input Model effectivity from Complete effectivity expression
			EffectivityFramework ef = new EffectivityFramework();
			MapList mlEffData = ef.getRelExpression(context, strRelId);
			
			if(null != mlEffData && !mlEffData.isEmpty())
			{
				Map mpData = (Map) mlEffData.get(0);
				String strActualExpr = (String) mpData.get("actualValue");
				
				if(null != strActualExpr && !"".equalsIgnoreCase(strActualExpr))
				{
					String[] saData = strActualExpr.split("OR");
					//ArrayList<String> alExpnData = new ArrayList<String>();
					//String strNewExpn = null;
					int iLen = saData.length;
					String strTemp = null;
					
					for(int i=0;i<iLen;i++)
					{

						strTemp = saData[i];
							
						//Check what info gets removed: is it only wrt to MY wrt Task name ?
						boolean isPresent = checkModelYear(context,strTaskName,strTemp);
						
						if(null != strTemp && !"".equalsIgnoreCase(strTemp) && !strTemp.contains(strModelPhysicalId) && !isPresent)
						{
							if(strNewExpn == null)
							{
								strNewExpn = strTemp;
							}
							else
							{
								strNewExpn = strNewExpn+ "OR"+strTemp;	
							}
						}
						else if(null != strTemp && !"".equalsIgnoreCase(strTemp) && strTemp.contains(strModelPhysicalId))
						{
							
						}
					}
					
				}
			}
		}
		catch(Exception ex)
		{

		}
		
		return strNewExpn;
	}
	
	public static boolean checkModelYear(Context context, String strTaskName, String strExpn) throws Exception
	{
		boolean isPresent = false;
		try
		{
			ArrayList<String> alData = findPhysicalId(context,"Hardware Product", strTaskName+"*", "*");			
			
			if(null != alData && !alData.isEmpty())
			{
				int iSize = alData.size();
				String strTemp = null;
				for(int i=0;i<iSize;i++)
				{
					strTemp = alData.get(i);
					if(null != strTemp && null != strExpn && strExpn.contains(strTemp))
					{
						isPresent = true;
						break;
					}
				}
						
			}
		}
		catch(Exception ex)
		{
			
		}
		
		return isPresent;
	}
	
	public static ArrayList<String> findPhysicalId(Context context, String strType, String strName, String strRev) throws Exception
	{
		ArrayList<String> alData =new ArrayList<String>();
		try
		{
			StringList objSel = new StringList(2);
			objSel.addElement("physicalid");
			objSel.addElement(DomainConstants.SELECT_NAME);
			
			MapList mlObjectList = DomainObject.findObjects(context, strType, strName, strRev, "*",	VAULT_PRODUCTION, "", false, objSel);
			
			if(null != mlObjectList && !mlObjectList.isEmpty())
			{
				int iSize = mlObjectList.size();
				Map mpData = null;
				String strPhyId = null;
				
				for(int i=0;i<iSize;i++)
				{
					mpData = (Map) mlObjectList.get(i);
					if(null != mpData && !mpData.isEmpty())
					{
						strPhyId = (String) mpData.get("physicalid");
						if(null != strPhyId && !"".equalsIgnoreCase(strPhyId))
						{
							alData.add(strPhyId);
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			
		}
		
		return alData;
	}
	
	//Added for CR-72 -  Start
	
	public static void processConfigurationRequest(Context _context, String strRelId) throws Exception {
		
		String sTaskName = "";
		String sCOId = "";
		String sCFId = "";
		String sOperationMode = "";
		String sConfOptRelID = "";
		String sModelName = "";
		String sObjwhr = "";
		String sEffActualExpr = "";
		String sEffExpr = "";
		String sModelInfo = "";
		String sModelPhysicalID = "";
		String sModelID = "";
		String sModelRelatedEffExpr = "";
		String sOtherModelEffExpr = "";
		String sEffForWIPModel = "";
		String sFinalEffForModel = "";
		String sGFDId = "";
		String sGFDExpression = "";
		String sEachEffWIPExpr = "";
		String sFinalEffForWIPModel = "";
		
		StringList slEffData = null;
		StringList slModelRelatedEffExpr = null;
		StringList slOtherModelEffExpr = null;
		StringList slEffForWIPModel = new StringList(1);
		StringList slFinalWIPExpr = new StringList(1);
		
		Map mpCOInfo = null;
		Map mpData = null;
		Map mpHPInfo = null;
		
		int iCnt = 0;
		int iEffWIPLen = 0;
		int iWIPCnt = 0 ;
		
		String[] aEffExpr = null; 
		List lEffData = null;
		
		boolean bModelExists = false;
		boolean bModelMYExistsInExpr = false;
		
		EffectivityFramework ef = null;
		
		StringList slSelect = new StringList(6);
		slSelect.addElement("from.name");
		slSelect.addElement("to.id");
		slSelect.addElement("to.name");
		slSelect.addElement("to.type");
		slSelect.addElement("id");
		slSelect.addElement("attribute["+ATTRIBUTE_HOOPER_STATE+"].value");
		
		StringList slHPSelect = new StringList(1);
		slHPSelect.addElement(DomainConstants.SELECT_ID);
		
		StringList slCOSelect = new StringList(2);
		slCOSelect.addElement("to["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].from.id");
		slCOSelect.addElement("to["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].id");
		
		try {
			if( UIUtil.isNotNullAndNotEmpty(strRelId) ){
				
				slModelRelatedEffExpr = new StringList(1);
				slOtherModelEffExpr = new StringList(1);
				
				DomainRelationship domRelObj = new DomainRelationship(strRelId);
				Hashtable mpRelData = domRelObj.getRelationshipData(_context, slSelect);
				
								
				if(null != mpRelData && !mpRelData.isEmpty()){
					
					sTaskName = (String)((StringList) mpRelData.get("from.name")).get(0);
					sModelName = (String)(FrameworkUtil.split(sTaskName, "-")).get(0); 
					sModelInfo = getModelInfo(_context, sModelName);
					sModelID = (String)(FrameworkUtil.split(sModelInfo, "|")).get(0);
					sModelPhysicalID = (String)(FrameworkUtil.split(sModelInfo, "|")).get(1);
					
					sCOId = (String)((StringList) mpRelData.get("to.id")).get(0);		
					
					
					sOperationMode = (String)((StringList) mpRelData.get("attribute["+ATTRIBUTE_HOOPER_STATE+"].value")).get(0);
					
					
					if( UIUtil.isNotNullAndNotEmpty(sModelInfo) && UIUtil.isNotNullAndNotEmpty(sOperationMode) && UIUtil.isNotNullAndNotEmpty(sCOId) && UIUtil.isNotNullAndNotEmpty(sTaskName) ){
											
						DomainObject domCO = DomainObject.newInstance(_context, sCOId);
						mpCOInfo = domCO.getInfo(_context, slCOSelect);
						
						sConfOptRelID = (String)mpCOInfo.get("to["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].id");
						sCFId = (String)mpCOInfo.get("to["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].from.id");
						
						ef = new EffectivityFramework();
						
						MapList mlEffData = ef.getRelExpression(_context, sConfOptRelID);

						DomainObject domCF = DomainObject.newInstance(_context, sCFId);
												
						if( "Add".equals(sOperationMode) ) {
														
							sObjwhr = "name match '"+sModelName+"*'";	
							//sObjwhr = "name match '"+sTaskName+"*'";
														
							MapList mlCORelatedHP = domCF.getRelatedObjects(_context, 
									RELATIONSHIP_CONFIGURATION_FEATURES, // rel pattern
									TYPE_HARDWARE_PRODUCT,// type pattern
									slHPSelect, // object selects
									null,// rel selects
									true,// get To
									false,// get from
									(short) 1,// recurse to
									sObjwhr, // object where
									null, // relationship where
									0);
							
							if( mlCORelatedHP != null && mlCORelatedHP.size() > 0){
								bModelExists = true;
							}	
							
														
							if( null != mlEffData && !mlEffData.isEmpty() ){
								
								mpData = (Map) mlEffData.get(0);
								
								sEffActualExpr = (String) mpData.get("actualValue");
								
								
								if( UIUtil.isNotNullAndNotEmpty(sEffActualExpr) ){
									if( bModelExists && sEffActualExpr.contains(":"+sModelPhysicalID+"[") ){										
										// For re-effect-in - Need to modify
																			
										aEffExpr = sEffActualExpr.split("\\) OR \\(");										
										lEffData = (List)Arrays.asList(aEffExpr);	
										
										for(iCnt=0;iCnt < lEffData.size() ;iCnt++){
											
											sEffExpr = (String)lEffData.get(iCnt);
											
											if( iCnt == 0 && sEffExpr.startsWith("(") ){												
												sEffExpr = sEffExpr.substring(1,sEffExpr.length());
											} else if( (iCnt == lEffData.size() - 1) && (sEffExpr.endsWith(")")) ){
												sEffExpr = sEffExpr.substring(0,sEffExpr.length()-1);												
											}
											
											if( sEffExpr.contains(":"+sModelPhysicalID+"[") ){								
													
												sEffForWIPModel = getEffExpressionForWIPModelAndConnectModelToCF(_context, sModelID, sTaskName, domCF);
												
												if( UIUtil.isNotNullAndNotEmpty(sEffForWIPModel) ){
													slModelRelatedEffExpr.add("("+sEffExpr+" OR "+sEffForWIPModel+")");
												} else {
													slModelRelatedEffExpr.add("("+sEffExpr+")");
												}												
											} else {												
												slOtherModelEffExpr.add("("+sEffExpr+")");												
											}												
										}	
																			
										
										if( slOtherModelEffExpr.size() > 0 ){
											sFinalEffForModel = (String)FrameworkUtil.join(slOtherModelEffExpr, " OR ");
										} 
										
										if( slModelRelatedEffExpr.size() > 0 ){
											sFinalEffForModel = sFinalEffForModel + " OR " + (String)FrameworkUtil.join(slModelRelatedEffExpr, " OR ");
										}										
										
									} else {
										//No Existing Eff Expr for this Model 
										//sFinalEffForModel - Get Eff Expr for WIP Models and Connect CF to WIP Model
										sEffForWIPModel = getEffExpressionForWIPModelAndConnectModelToCF(_context, sModelID, sTaskName, domCF);
																				
										sFinalEffForModel = sEffActualExpr;
										if( UIUtil.isNotNullAndNotEmpty(sEffForWIPModel) ){
											sFinalEffForModel = sFinalEffForModel + " OR " + "(" +sEffForWIPModel + ")";
										}
										
									}
								} else {
									//No Existing Eff Expr
									//Get expr for GFD
									sGFDId = findHPObjects(_context,"Hardware Product", "GFD","A");
									sGFDExpression = generateEffctExpression(_context,sGFDId);
									
									//sFinalEffForModel - Get Eff Expr for WIP Models and Connect CF to WIP Model
									sEffForWIPModel = getEffExpressionForWIPModelAndConnectModelToCF(_context, sModelID, sTaskName, domCF);
									
									//Final Expr sFinalEffForModel + GFD
									sFinalEffForModel = "("+ sGFDExpression + ")";
									if( UIUtil.isNotNullAndNotEmpty(sEffForWIPModel) ){
										sFinalEffForModel = sFinalEffForModel + " OR " + "(" +sEffForWIPModel + ")";
									}
									
								}
							} else {
								//No Existing Eff Expr
								//Get expr for GFD
								sGFDId = findHPObjects(_context,"Hardware Product", "GFD","A");
								sGFDExpression = generateEffctExpression(_context,sGFDId);
								
								//sFinalEffForModel - Get Eff Expr for WIP Models and Connect CF to WIP Model
								sEffForWIPModel = getEffExpressionForWIPModelAndConnectModelToCF(_context, sModelID, sTaskName, domCF);
								
								
								//Final Expr sFinalEffForModel + GFD
								sFinalEffForModel = "(" + sGFDExpression + ")";
								if( UIUtil.isNotNullAndNotEmpty(sEffForWIPModel) ){
									sFinalEffForModel = sFinalEffForModel + " OR " + "(" +sEffForWIPModel + ")";
								}
								
							}	
							
							//Update the relationship with eff expr								
							setEffectivityUI(_context, sConfOptRelID, sFinalEffForModel, sCOId);
														
							// Update Attribute of TASK-CO relationship							
							domRelObj.setAttributeValue(_context, ATTRIBUTE_HOOPER_STATE, "Published");
							
						} else if("Remove".equals(sOperationMode)){
							
							
							if( null != mlEffData && !mlEffData.isEmpty() ){
								
								mpData = (Map) mlEffData.get(0);
								
								sEffActualExpr = (String) mpData.get("actualValue");								
								
								if( UIUtil.isNotNullAndNotEmpty(sEffActualExpr) ){		
									
									aEffExpr = sEffActualExpr.split("\\) OR \\(");
									lEffData = (List)Arrays.asList(aEffExpr);
																		
									//For each Model iteration  - start
									for(iCnt=0;iCnt < lEffData.size() ;iCnt++){
										sEffExpr = (String)lEffData.get(iCnt);	
																				
										if( iCnt == 0 && sEffExpr.startsWith("(") ){											
											sEffExpr = sEffExpr.substring(1,sEffExpr.length());
										} else if( (iCnt == lEffData.size() - 1) && (sEffExpr.endsWith(")")) ){
											sEffExpr = sEffExpr.substring(0,sEffExpr.length()-1);											
										}
										
										if( sEffExpr.contains(":"+sModelPhysicalID+"[") ){																							
											
											//Existing Eff  Expr - sModelRelatedEffExpr
											//Get Eff Expr for WIP Models and disconnect CF to WIP Model in case of single CO
											
											sEffForWIPModel = getEffExpressionForWIPModelAndDisconnectModelToCF(_context, sModelID, sTaskName, domCF);										
																						
											if( UIUtil.isNotNullAndNotEmpty(sEffForWIPModel) ){
												// Check if Effect In and Effect Out point are same or different 																							
												slEffForWIPModel = FrameworkUtil.split(sEffForWIPModel, ",");
												
												iEffWIPLen = slEffForWIPModel.size();											
												
												for(iWIPCnt=0;iWIPCnt < iEffWIPLen ;iWIPCnt++){
													sEachEffWIPExpr = (String)slEffForWIPModel.get(iWIPCnt);
													if( sEffExpr.contains(sEachEffWIPExpr) ){
														if(sEffExpr.contains(sEachEffWIPExpr+ " OR ")){															
															
															sEffExpr = sEffExpr.replace(sEachEffWIPExpr+ " OR ", "");															

														} else if(sEffExpr.contains(" OR "+sEachEffWIPExpr)){
																														
															sEffExpr = sEffExpr.replace(" OR "+sEachEffWIPExpr, "");															

														} else {
																														
															sEffExpr = sEffExpr.replace(sEachEffWIPExpr, "");
															//Dont consider this expr as Effect In and Effect Out points are same
														} 
														
													} else {														
														slFinalWIPExpr.add(sEachEffWIPExpr);
													}																									
												}
												
												if( UIUtil.isNotNullAndNotEmpty(sEffExpr.trim()) ){
													
													if( slFinalWIPExpr.size() > 0 ){
														
														sFinalEffForWIPModel = (String)FrameworkUtil.join(slFinalWIPExpr, " OR ");
														
														if(sEffExpr.contains("AND NOT")){
															sEffExpr = sEffExpr+ " AND NOT (" +sFinalEffForWIPModel+ ")" ;
														}else{
															sEffExpr = "("+sEffExpr+")" + " AND NOT (" +sFinalEffForWIPModel+ ")" ;
														}
														
														slModelRelatedEffExpr.add("("+sEffExpr+")");														
														
													} else {														
														
														slModelRelatedEffExpr.add("("+sEffExpr+")");
														
													}
												} else {
																										
													//Dont add any expr wrt to this Model and any MY 
												}
												
											} else {
												
												slModelRelatedEffExpr.add("("+sEffExpr+")");
											}											
											
										} else {											
											slOtherModelEffExpr.add("("+sEffExpr+")");
											
										}												
									}	
									//For each Model iteration  - end
																		
									if( slOtherModelEffExpr.size() > 0 ){
										sFinalEffForModel = (String)FrameworkUtil.join(slOtherModelEffExpr, " OR ");
									}
									
									if( slModelRelatedEffExpr.size() > 0 ){
										bModelMYExistsInExpr = checkIfModelMYInfoExistsInExpr( _context, sModelID, sTaskName , slModelRelatedEffExpr);
										sFinalEffForModel = sFinalEffForModel + " OR " + (String)FrameworkUtil.join(slModelRelatedEffExpr, " OR ");
									}									
									
									setEffectivityUI(_context, sConfOptRelID, sFinalEffForModel, sCOId);
																											
									//Disconnect TASK-CO relationship if effectivity is absolutely blank for that Model & MY,
									
									if( !bModelMYExistsInExpr ){										
										DomainRelationship.disconnect(_context, strRelId);
									} else {										
										//domRelObj.setAttributeValue(_context, ATTRIBUTE_HOOPER_STATE, "Published");
										domRelObj.setAttributeValue(_context, ATTRIBUTE_HOOPER_STATE, "PublishedRemoved");//phardare-17616
									}
								}
							}							
							
						} else {
							//No Action
						}
						
					}
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	private static boolean checkIfModelMYInfoExistsInExpr( Context _context, String sModelID, String sTaskName , StringList slModelRelatedExpr)throws Exception {
		boolean bModelMYInfoExist = false;
		StringList slTaskRelatedModelIDs = new StringList(1);
		
		try {

			StringList slModelSel = new StringList(2);			
			slModelSel.addElement(DomainObject.SELECT_ID);
			slModelSel.addElement("physicalid");
			
			String sModelPhysicalId = "";
			
			Map mpTempData = null;
			
			String sModelRelatedExpr = (String)FrameworkUtil.join(slModelRelatedExpr, " OR "); 
			
			DomainObject doModel = new DomainObject(sModelID);
			
			String sModelWhere = "name match '"+sTaskName+"*'";
						
			
			MapList mlModel = doModel.getRelatedObjects(_context,
					RELATIONSHIP_PRODUCTS, // rel pattern
					TYPE_HARDWARE_PRODUCT,// type pattern
					slModelSel, // object selects
					null,// rel selects
					false,// get To
					true,// get from
					(short) 1,// recurse to
					sModelWhere, // object
					null, // relationship where
					0);	
			
			if ( null != mlModel && !mlModel.isEmpty() ) {
				int iModelListLen = mlModel.size(); 
				int iCnt = 0;
				for( iCnt = 0 ; iCnt < iModelListLen ; iCnt++ ) {
					mpTempData = (Map) mlModel.get(iCnt);
					sModelPhysicalId = (String)mpTempData.get("physicalid");
								
					if( sModelRelatedExpr.contains(":"+sModelPhysicalId+"-") ){							
						bModelMYInfoExist = true;
						break;
					}					
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return bModelMYInfoExist;
	}
	
	private static String getEffExpressionForWIPModelAndConnectModelToCF(Context _context, String sModelID, String sTaskName, DomainObject domCF)throws Exception {
		StringBuffer sbWIPEffExpr = new StringBuffer(1);
		
		try {

			StringList slModelSel = new StringList(2);			
			slModelSel.addElement(DomainObject.SELECT_ID);
			slModelSel.addElement(DomainObject.SELECT_REVISION);
			
			StringList  slRelatedModel = domCF.getInfoList(_context,"to[Configuration Features].from.id");
									
			Entry entry = null;
			
			String sModelRev = "";
			String sModelId = "";
			String sMajorRev = "";
			String sMinorRev = "";
			
			int iModelIdLen = 0;
									
			StringList slRevInfo = null;
			
			Map mpTempData = null;
			Map mpMinorRevAndModelId = null;
			Map mpDerivativeInfo = new HashMap ();
			
			DomainObject dobjHP = null;
			DomainRelationship doRel = null;
			
			List lModelId = null;
			
			DomainObject doModel = new DomainObject(sModelID);
			
			String sObjWhere = "name match '"+sTaskName+"*' && "+"current == \"Product Management\"";
			
			
			MapList mlModel = doModel.getRelatedObjects(_context,
					RELATIONSHIP_PRODUCTS, // rel pattern
					TYPE_HARDWARE_PRODUCT,// type pattern
					slModelSel, // object selects
					null,// rel selects
					false,// get To
					true,// get from
					(short) 1,// recurse to
					sObjWhere, // object
					null, // relationship where
					0);	
			
			if ( null != mlModel && !mlModel.isEmpty() ) {
				int iModelListLen = mlModel.size(); 
				int iCnt = 0;			
				
				for( iCnt = 0 ; iCnt < iModelListLen ; iCnt++ ) {
					mpTempData = (Map) mlModel.get(iCnt);
					
					sModelId = (String)mpTempData.get(DomainObject.SELECT_ID);
					sModelRev = (String)mpTempData.get(DomainObject.SELECT_REVISION);
					slRevInfo = (StringList)FrameworkUtil.split(sModelRev, "."); //A.2, A.3, B.1 ...
					if(slRevInfo.size() == 2){
						sMajorRev = (String)slRevInfo.get(0); // A,B,C...
						sMinorRev = (String)slRevInfo.get(1); // 1,2,3...
					} else {
						sMajorRev = sModelRev;
						sMinorRev = "0";
					}
										
					if( mpDerivativeInfo.containsKey(sMajorRev) ){
						
						mpMinorRevAndModelId = (TreeMap)mpDerivativeInfo.get(sMajorRev);
						mpMinorRevAndModelId.put(Integer.parseInt(sMinorRev), sModelId);
						mpDerivativeInfo.put(sMajorRev, mpMinorRevAndModelId);
						
					} else {
						
						mpMinorRevAndModelId = new TreeMap<Integer, String>();
						mpMinorRevAndModelId.put(Integer.parseInt(sMinorRev), sModelId);
						mpDerivativeInfo.put(sMajorRev, mpMinorRevAndModelId);
					}
					
				}
				
				iCnt = 0;
				if( null != mpDerivativeInfo && !mpDerivativeInfo.isEmpty() ){
					
					//Sorted map for each series A.1, A.2, A.3... B.1, B.2, B.3, ....
					Iterator itr = mpDerivativeInfo.entrySet().iterator();
					while(itr.hasNext()) {
						entry = (Entry) itr.next();
						mpMinorRevAndModelId = (TreeMap) entry.getValue();
						sModelId = (String)mpMinorRevAndModelId.values().toArray()[0];											
						if(sbWIPEffExpr.length() == 0){							
							sbWIPEffExpr.append(generateEffctExpression(_context, sModelId));
						} else {							
							sbWIPEffExpr.append(" OR ");
							sbWIPEffExpr.append(generateEffctExpression(_context, sModelId));
						}
						
						//Connect all WIP Models						
						lModelId = new ArrayList(mpMinorRevAndModelId.values());
						
						iModelIdLen = lModelId.size();
						for( iCnt = 0 ; iCnt < iModelIdLen ; iCnt++ ) {
							sModelId = (String)lModelId.get(iCnt);
							if(!slRelatedModel.contains(sModelId)){
								dobjHP = DomainObject.newInstance(_context, sModelId);						
								
								doRel = DomainRelationship.connect( _context, dobjHP, RELATIONSHIP_CONFIGURATION_FEATURES , domCF); 								
								try
								{
									
									MqlUtil.mqlCommand(_context, strConnectionMQL, true, doRel.toString(), OWNING_ORGANIZATION, "PI");
								}
								catch(Exception exe)
								{
									exe.printStackTrace();
									throw exe;
								}
							}								
						}
					}
					
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return sbWIPEffExpr.toString();
	}
	
	private static String getEffExpressionForWIPModelAndDisconnectModelToCF(Context _context, String sModelID, String sTaskName, DomainObject domCF)throws Exception {
		StringBuffer sbWIPEffExpr = new StringBuffer(1);
		
		try {

			StringList slModelSel = new StringList(2);			
			slModelSel.addElement(DomainObject.SELECT_ID);
			slModelSel.addElement(DomainObject.SELECT_REVISION);
			
			StringList slObjSel = new StringList(1);			
			slObjSel.addElement(DomainConstants.SELECT_NAME);
			
			StringList slRelSel = new StringList();
			slRelSel.addElement(DomainRelationship.SELECT_ID);
									
			Entry entry = null;
			
			String sModelRev = "";
			String sModelId = "";
			String sMajorRev = "";
			String sMinorRev = "";
			String sObjWhere = "";
			String sRelId = "";
			
			int iModelIdLen = 0;
									
			StringList slRevInfo = null;
			
			Map mpTempData = null;
			Map mpMinorRevAndModelId = null;
			Map mpDerivativeInfo = new HashMap ();
			
			sObjWhere = "name match '"+sTaskName+"*' && "+"current == \"Product Management\"";
			
			StringList slRelatedCO = domCF.getInfoList(_context,"from["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].to.id");
			
			//Disconnect the relationship b/w WIP models and CF in case of single CO
			if( null != slRelatedCO && slRelatedCO.size()==1 ) {
				
				MapList mlRelatedModel = domCF.getRelatedObjects(_context,
						RELATIONSHIP_CONFIGURATION_FEATURES, // rel pattern
						TYPE_HARDWARE_PRODUCT,// type pattern 
						slObjSel, // object selects
						slRelSel,// rel selects
						true,// get To
						false,// get from
						(short) 1,// recurse to
						sObjWhere, // object
						null, // relationship where
						0);
				
				if(null != mlRelatedModel && !mlRelatedModel.isEmpty()) {
					try {
						int iListSize = mlRelatedModel.size();
						
						MqlUtil.mqlCommand(_context, "trigger off", true);
											
						for(int iCount=0; iCount < iListSize; iCount++) {
							mpTempData = (Map) mlRelatedModel.get(iCount);						
							sRelId = (String) mpTempData.get(DomainRelationship.SELECT_ID);
							
							// Disconnect relationship with Model						
							DomainRelationship.disconnect(_context, sRelId);							
						}
					}
					catch(Exception excp)
					{
						excp.printStackTrace();
						throw excp;
					}
					finally 
					{
						MqlUtil.mqlCommand(_context, "trigger on", true);								
					}
					
				}
			}
			
			//Get Eff Expr wrt to all WIP models
			
			DomainObject doModel = new DomainObject(sModelID);
						
			MapList mlModel = doModel.getRelatedObjects(_context,
					RELATIONSHIP_PRODUCTS, // rel pattern
					TYPE_HARDWARE_PRODUCT,// type pattern
					slModelSel, // object selects
					null,// rel selects
					false,// get To
					true,// get from
					(short) 1,// recurse to
					sObjWhere, // object
					null, // relationship where
					0);	
			
			
			if ( null != mlModel && !mlModel.isEmpty() ) {
				int iModelListLen = mlModel.size(); 
				int iCnt = 0;		
				
				for( iCnt = 0 ; iCnt < iModelListLen ; iCnt++ ) {
					mpTempData = (Map) mlModel.get(iCnt);
					
					sModelId = (String)mpTempData.get(DomainObject.SELECT_ID);
					sModelRev = (String)mpTempData.get(DomainObject.SELECT_REVISION);
					slRevInfo = (StringList)FrameworkUtil.split(sModelRev, "."); //A.2, A.3, B.1 ...
					if(slRevInfo.size() == 2){
						sMajorRev = (String)slRevInfo.get(0); // A,B,C...
						sMinorRev = (String)slRevInfo.get(1); // 1,2,3...
					} else {
						sMajorRev = sModelRev;
						sMinorRev = "0";
					}
					
					
					if( mpDerivativeInfo.containsKey(sMajorRev) ){
						
						mpMinorRevAndModelId = (TreeMap)mpDerivativeInfo.get(sMajorRev);
						mpMinorRevAndModelId.put(Integer.parseInt(sMinorRev), sModelId);
						mpDerivativeInfo.put(sMajorRev, mpMinorRevAndModelId);
						
					} else {
						
						mpMinorRevAndModelId = new TreeMap<Integer, String>();
						mpMinorRevAndModelId.put(Integer.parseInt(sMinorRev), sModelId);
						mpDerivativeInfo.put(sMajorRev, mpMinorRevAndModelId);
					}
					
				}
				
				iCnt = 0;
				if( null != mpDerivativeInfo && !mpDerivativeInfo.isEmpty() ){
					//Sorted map for each series A.1, A.2, A.3... B.1, B.2, B.3, ....
					Iterator itr = mpDerivativeInfo.entrySet().iterator();
					while(itr.hasNext()) {
						entry = (Entry) itr.next();
						mpMinorRevAndModelId = (TreeMap) entry.getValue();
						sModelId = (String)mpMinorRevAndModelId.values().toArray()[0];											
						if(sbWIPEffExpr.length() == 0){
							
							sbWIPEffExpr.append(generateEffctExpression(_context, sModelId));
						} else {
							
							//sbWIPEffExpr.append(" OR ");
							sbWIPEffExpr.append(",");
							sbWIPEffExpr.append(generateEffctExpression(_context, sModelId));
						}
					}				
					
				}
			}			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return sbWIPEffExpr.toString();
	}
	
	private static String getModelInfo(Context _context, String sModelName) throws Exception {		
		
		StringList slObjSel = new StringList(2);
		slObjSel.add(DomainConstants.SELECT_ID);
		slObjSel.add("physicalid");
		
		StringBuffer sbReturnVal = new StringBuffer(1);
		try {
			MapList mlModel = DomainObject.findObjects(_context,
					TYPE_MODEL,
					sModelName,
					DomainConstants.QUERY_WILDCARD,
					DomainConstants.QUERY_WILDCARD,
					DomainConstants.QUERY_WILDCARD,
					DomainConstants.EMPTY_STRING,
					true,
					slObjSel);
			if(mlModel != null && mlModel.size() > 0){
				
				sbReturnVal.append((String)((Map)mlModel.get(0)).get(DomainConstants.SELECT_ID));
				sbReturnVal.append("|");
				sbReturnVal.append((String)((Map)mlModel.get(0)).get("physicalid"));
								
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return sbReturnVal.toString();
		
	}
	
	private static StringList getModelsRelatedToTaskName( Context _context, String sModelID, String sTaskName )throws Exception {
		StringList slTaskRelatedModelIDs = new StringList(1);
		
		try {

			StringList slModelSel = new StringList(2);			
			slModelSel.addElement(DomainObject.SELECT_ID);
			slModelSel.addElement("physicalid");
			
			Map mpTempData = null;
			
			DomainObject doModel = new DomainObject(sModelID);
			
			String sModelWhere = "name match '"+sTaskName+"*'";			
			
			
			MapList mlModel = doModel.getRelatedObjects(_context,
					RELATIONSHIP_PRODUCTS, // rel pattern
					TYPE_HARDWARE_PRODUCT,// type pattern
					slModelSel, // object selects
					null,// rel selects
					false,// get To
					true,// get from
					(short) 1,// recurse to
					sModelWhere, // object
					null, // relationship where
					0);	
			
			if ( null != mlModel && !mlModel.isEmpty() ) {
				int iModelListLen = mlModel.size(); 
				int iCnt = 0;			
				
				for( iCnt = 0 ; iCnt < iModelListLen ; iCnt++ ) {
					mpTempData = (Map) mlModel.get(iCnt);
					
					slTaskRelatedModelIDs.add((String)mpTempData.get("physicalid"));
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return slTaskRelatedModelIDs;
	}
	//Added for CR-72 -  End
	
	//Added by phardare on 25th July 2018 for CR72 - STARTS
		/**
		 * This method is used to update the sequence of all Configuration Options present in the database.
		 * @param context
		 * @throws Exception
		 * @returns void
		 * @Author : Prasad Hardare (phardare) - 18th July 2018.
		 */
		public static void updateSequenceOrderForConfigOptions(Context context, String[] args)throws Exception
		{
			System.out.println("\n\n\n\n*********** updateSequenceOrderForConfigOptions *******");
			String sTypeConfigFeature = PropertyUtil.getSchemaProperty("type_ConfigurationFeature");
			String sTypeConfigOption = PropertyUtil.getSchemaProperty("type_ConfigurationOption");
			String sRelConfigOptions = PropertyUtil.getSchemaProperty("relationship_ConfigurationOptions");
			String sAttrSequenceOrder = PropertyUtil.getSchemaProperty("attribute_SequenceOrder");
			String sAttrDisplayName = PropertyUtil.getSchemaProperty("attribute_DisplayName");
			
			String strLoginUser = context.getUser();
						
			try
			{
				String strWhereString = "";
				StringList strObjectSelectList = new StringList(2);
				strObjectSelectList.addElement(DomainConstants.SELECT_NAME);
				strObjectSelectList.addElement(DomainConstants.SELECT_ID);
				StringList relSelects = new StringList(2);
				relSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
				relSelects.addElement("attribute["+sAttrSequenceOrder+"].value");
				
				DomainObject dOConfigFeature = new DomainObject();
				DomainRelationship dORelConfigOption= new DomainRelationship();
				
				MapList mlConfigFeaturesList = DomainObject.findObjects(context, sTypeConfigFeature, DomainConstants.QUERY_WILDCARD, strWhereString, strObjectSelectList);
						
				if(!mlConfigFeaturesList.isEmpty()){
				
					int iCount, jCount, iSize, jSize;
					Map mCFMap,mCOMap;
					String strConfigFeatureID = "";
					String strConfigOptionRelID = "";
					String sSequenceOrderValue = "";
					String sSequenceOrderNEWValue = "";
					strObjectSelectList.addElement("attribute["+sAttrDisplayName+"].value");
					
					iSize = mlConfigFeaturesList.size();
					
					for(iCount=0;iCount<iSize;iCount++)
					{
						mCFMap = (Map)mlConfigFeaturesList.get(iCount);
						strConfigFeatureID = (String)mCFMap.get(DomainObject.SELECT_ID);
						
						dOConfigFeature = new DomainObject(strConfigFeatureID);
						MapList mlConfigOptionsList = dOConfigFeature.getRelatedObjects(context,
						sRelConfigOptions,  	// relationship pattern
						sTypeConfigOption,		// TYPE pattern
						strObjectSelectList,   	// object selects
						relSelects,              		// relationship selects
						false,          		// to direction
						true,           		// from direction
						(short)1,   		// recursion level
						null,     				// object where clause
						null);
								
						if(!mlConfigOptionsList.isEmpty()){
							mlConfigOptionsList.sort("attribute["+sAttrDisplayName+"].value","ascending","string");
						
							jSize = mlConfigOptionsList.size();
							
							for(jCount=0;jCount<jSize;jCount++)
							{
								mCOMap = (Map)mlConfigOptionsList.get(jCount);
								strConfigOptionRelID = (String)mCOMap.get(DomainObject.SELECT_RELATIONSHIP_ID);
								sSequenceOrderValue = (String)mCOMap.get("attribute["+sAttrSequenceOrder+"].value");
								
								if(sSequenceOrderValue!=null && sSequenceOrderValue!=""){
									dORelConfigOption = new DomainRelationship(strConfigOptionRelID);
									sSequenceOrderNEWValue = String.valueOf(jCount + 1);
									dORelConfigOption.setAttributeValue(context, sAttrSequenceOrder, sSequenceOrderNEWValue);
									
									//String test = dORelConfigOption.getAttributeValue(context, sAttrSequenceOrder);//Not needed
								}
							}
						}
					}
				}
			} catch(Exception ex) {
				throw new Exception(ex.toString());
			}
		}

		/**
		 * This method is used to update the sequence of all Configuration Features present in the database.
		 * @param context
		 * @throws Exception
		 * @returns void
		 * @Author : Prasad Hardare (phardare) - 18th July 2018.
		 */
		public static void updateSequenceOrderForConfigFeatures(Context context, String[] args)throws Exception
		{
			System.out.println("\n\n\n\n*********** updateSequenceOrderForConfigFeatures *******");
			String sTypeConfigFeature = PropertyUtil.getSchemaProperty("type_ConfigurationFeature");
			String sTypeHardwareProduct = PropertyUtil.getSchemaProperty("type_HardwareProduct");
			String sRelConfigFeatures = PropertyUtil.getSchemaProperty("relationship_ConfigurationFeatures");
			String sAttrSequenceOrder = PropertyUtil.getSchemaProperty("attribute_SequenceOrder");
			String sAttrDisplayName = PropertyUtil.getSchemaProperty("attribute_DisplayName");
						
			String strLoginUser = context.getUser();
			try
			{
				String strWhereString = "name!='GFD' && current=='Product Management'";
				StringList strObjectSelectList = new StringList(2);
				strObjectSelectList.addElement(DomainConstants.SELECT_NAME);
				strObjectSelectList.addElement(DomainConstants.SELECT_ID);
				StringList relSelects = new StringList(2);
				relSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
				relSelects.addElement("attribute["+sAttrSequenceOrder+"].value");
				
				DomainObject dOHardProd = new DomainObject();
				DomainRelationship dORelConfigFeature= new DomainRelationship();
				
				MapList mlHardProdList = DomainObject.findObjects(context, sTypeHardwareProduct, DomainConstants.QUERY_WILDCARD, strWhereString, strObjectSelectList);
						
				if(!mlHardProdList.isEmpty()){
				
					int iCount, jCount, iSize, jSize;
					Map mHPMap, mCFMap;
					String strHardProdID = "";
					String strConfigFeatureRelID = "";
					String sSequenceOrderValue = "";
					String sSequenceOrderNEWValue = "";
					strObjectSelectList.addElement("attribute["+sAttrDisplayName+"].value");
					
					iSize = mlHardProdList.size();
					
					for(iCount=0;iCount<iSize;iCount++)
					{
						mHPMap = (Map)mlHardProdList.get(iCount);
						strHardProdID = (String)mHPMap.get(DomainObject.SELECT_ID);
						
						dOHardProd = new DomainObject(strHardProdID);
						MapList mlConfigFeaturesList = dOHardProd.getRelatedObjects(context,
						sRelConfigFeatures,  	// relationship pattern
						sTypeConfigFeature,		// TYPE pattern
						strObjectSelectList,   	// object selects
						relSelects,              		// relationship selects
						false,          		// to direction
						true,           		// from direction
						(short)1,   		// recursion level
						null,     				// object where clause
						null);
								
						if(!mlConfigFeaturesList.isEmpty()){
							mlConfigFeaturesList.sort("attribute["+sAttrDisplayName+"].value","ascending","string");
						
							jSize = mlConfigFeaturesList.size();
							
							for(jCount=0;jCount<jSize;jCount++)
							{
								mCFMap = (Map)mlConfigFeaturesList.get(jCount);
								strConfigFeatureRelID = (String)mCFMap.get(DomainObject.SELECT_RELATIONSHIP_ID);
								sSequenceOrderValue = (String)mCFMap.get("attribute["+sAttrSequenceOrder+"].value");
								
								if(sSequenceOrderValue!=null && sSequenceOrderValue!=""){
									dORelConfigFeature = new DomainRelationship(strConfigFeatureRelID);
									sSequenceOrderNEWValue = String.valueOf(jCount + 1);
									dORelConfigFeature.setAttributeValue(context, sAttrSequenceOrder, sSequenceOrderNEWValue);
									
									//String test = dORelConfigFeature.getAttributeValue(context, sAttrSequenceOrder);//Not needed
								}
							}
						}
					}
				}
			} catch(Exception ex) {
				throw new Exception(ex.toString());
			}
		}
		//Added by phardare on 25th July 2018 for CR72 - ENDS

	//Added by phardare on 18th October 2018 for CR72 (Incident-17707: Set PnO on Configuration Context Rel) - STARTS
	/**
	 * This method is used to update the PnO On Configuration Context Relationship (Between GFD & CF).
	 * @param context
	 * @throws Exception
	 * @returns void
	 * @Author : Prasad Hardare (phardare) - 18th October 2018.
	 */
	public static void updatePnOConfigurationContextRel(Context context, String[] args) throws Exception
	{
		System.out.println("\n\n\n\n*********** updatePnOConfigurationContextRel *******");
		
		try
		{
			MapList mlConfigFeaturesList = null;
			int iSize, iCount;
			Map mCFMap = null;
			
			String strConfigContextRelID = "";
			String strRelProjectValue = "";
			String strRelOrganizationValue = "";
			DomainRelationship dORelConfigContext = new DomainRelationship();
			
			
			String GFD_ID = findHPObjects(context,"Model", "GFD","");
			DomainObject dObjGFD = new DomainObject(GFD_ID);
			
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			relSelects.addElement(DomainObject.SELECT_PROJECT);
			relSelects.addElement(DomainObject.SELECT_ORGANIZATION);
			
			String strRelWhere = "project=='' || organization==''";
			//String strRelWhere = "";
			
			mlConfigFeaturesList = dObjGFD.getRelatedObjects(context,
					RELATIONSHIP_CONFIGURATION_CONTEXT, // rel pattern
					TYPE_CONFIGURATION_FEATURES,// type pattern
					null, // object selects
					relSelects,// rel selects
					true,// get To
					false,// get from
					(short) 1,// recurse to
					null, // object where
					strRelWhere, // relationship where
					0);
			
			if(!mlConfigFeaturesList.isEmpty()){
				iSize = mlConfigFeaturesList.size();
				
				for(iCount=0;iCount<iSize;iCount++)
				{
					mCFMap = (Map)mlConfigFeaturesList.get(iCount);
					strConfigContextRelID = (String)mCFMap.get(DomainObject.SELECT_RELATIONSHIP_ID);
					strRelProjectValue = (String)mCFMap.get(DomainObject.SELECT_PROJECT);
					strRelOrganizationValue = (String)mCFMap.get(DomainObject.SELECT_ORGANIZATION);

					if(strConfigContextRelID!=null && (strRelProjectValue.equalsIgnoreCase("") || strRelOrganizationValue.equalsIgnoreCase(""))){
						MqlUtil.mqlCommand(context, strConnectionMQL, true, strConfigContextRelID, OWNING_ORGANIZATION, OWNING_PROJECT);
					}
				}
			}		
		} catch (Exception ex) {			
			throw ex;
		}
	}
	//Added by phardare on 18th October 2018 for CR72 (Incident-17707: Set PnO on Configuration Context Rel) - ENDS
		
		
	//Added by phardare on 28th September 2018 for CR72 (Incident-17616:PublishedRemoved) - STARTS
		/**
		 * This method is used to disconnect the link between CF & HP, if all its COs are PublishedRemoved.
		 * @param context
		 * @throws Exception
		 * @returns void
		 * @Author : Prasad Hardare (phardare) - 28th September 2018.
		 */
		public static void disconnectConfigFeatureLinkageWithHP(Context context, String[] args)throws Exception
		{
			System.out.println("\n\n\n\n*********** disconnectConfigFeatureLinkageWithHP *******");
			try
			{			
				String strConfigFeatureID = "";
				String strConfigFeatureRelID = "";
				String strTaskName = "";
				String strCOID = "";
				String strMQL = "";
				boolean bDisconnect;
				String sHopperStateValue = "";
				StringList strRelatedCOsList = new StringList(2);
				StringList strAllCR72TasksList = new StringList(2);
				DomainObject dOConfigFeature = new DomainObject();
				DomainObject dOConfigOption = new DomainObject();
				HashSet hs = new HashSet(3);
				MapList mlFinalWIPModels = new MapList(1);
				
				StringList strObjectSelectList = new StringList(2);
				strObjectSelectList.addElement(DomainConstants.SELECT_ID);
				strObjectSelectList.addElement(DomainConstants.SELECT_NAME);
				
				StringList strRelSelectList = new StringList(2);
				strRelSelectList.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
				String strObjWhere = "";
				
				//Added by phardare on 02nd November 2018 for 17616_DELTA :PublishedRemoved - STARTS
				MapList mlTaskData = DomainObject.findObjects(context, TYPE_TASK,	"*-*MY", "1", "*",	VAULT_PRODUCTION, null, true, strObjectSelectList);
				
				if(null != mlTaskData && !mlTaskData.isEmpty())
				{
					int kSize = mlTaskData.size();
					Map mpTask = null;
					String strName = null;
					for(int i=0; i<kSize; i++)
					{
						mpTask = (Map) mlTaskData.get(i);

						if(null != mpTask && !mpTask.isEmpty())
						{
							strName = (String) mpTask.get(DomainConstants.SELECT_NAME);
							strAllCR72TasksList.addElement(strName);
						}
					}
				}
				//Added by phardare on 02nd November 2018 for 17616_DELTA :PublishedRemoved - ENDS
				
				MapList mlConfigFeaturesList = DomainObject.findObjects(context,
                                        TYPE_CONFIGURATION_FEATURES,		// type pattern
                                        DomainConstants.QUERY_WILDCARD,		// name pattern
                                        DomainConstants.QUERY_WILDCARD,     // revision pattern
                                        DomainConstants.QUERY_WILDCARD,     // owner pattern
                                        DomainConstants.QUERY_WILDCARD,     // vault pattern
                                        null, 								// where expression
                                        false,								// Expand Type
                                        strObjectSelectList);     			// object selects
				
				if(!mlConfigFeaturesList.isEmpty()){
				
					int iCount, jCount;
					int iSize, jSize;
					Iterator itrTask, itrCO;
					Map mCFMap, mCOMap, mHPMap;
										
					iSize = mlConfigFeaturesList.size();

					for(iCount=0;iCount<iSize;iCount++)
					{
						mCFMap = (Map)mlConfigFeaturesList.get(iCount);
						strConfigFeatureID = (String)mCFMap.get(DomainObject.SELECT_ID);
						
						dOConfigFeature = new DomainObject(strConfigFeatureID);
						strRelatedCOsList = dOConfigFeature.getInfoList(context, "from["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].to.id");
					
						itrTask = strAllCR72TasksList.iterator();
						
						while(itrTask.hasNext()){
							strTaskName = (String) itrTask.next();
							bDisconnect = true;
							
							itrCO = strRelatedCOsList.iterator();							
							while(itrCO.hasNext()){
								strCOID = (String) itrCO.next();
								
								strMQL = "expand bus "+strCOID+" rel \'"+DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"\' select bus where name==\'"+strTaskName+"\' select rel id attribute["+ATTRIBUTE_HOOPER_STATE+"].value dump |";
								sHopperStateValue = MqlUtil.mqlCommand(context, strMQL);
								
								if (!sHopperStateValue.equalsIgnoreCase("") && !sHopperStateValue.contains("PublishedRemoved")) {
									bDisconnect = false;
								}
							}
							if (bDisconnect) {
								strObjWhere = "current == 'Product Management' && name match '"+strTaskName+"*'";
								
								mlFinalWIPModels = dOConfigFeature.getRelatedObjects(context,
												RELATIONSHIP_CONFIGURATION_FEATURES,	// rel pattern
												TYPE_HARDWARE_PRODUCT,					// type pattern
												strObjectSelectList,					// object selects
												strRelSelectList,						// rel selects
												true,									// get To
												true,									// get from
												(short) 1,								// recurse to
												strObjWhere,							// object Where
												null,									// relationship where
												0);
						
								if (!mlFinalWIPModels.isEmpty()) {
									jSize = mlFinalWIPModels.size();
									
									for(jCount=0;jCount<jSize;jCount++)
									{
										mHPMap = (Map)mlFinalWIPModels.get(jCount);
										strConfigFeatureRelID = (String)mHPMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
										System.out.println("\n\n *********DISCONNECTTTTTTTTTTTT strConfigFeatureRelID ----------- "+strConfigFeatureRelID);
										
										try
										{
											//Need to triggers to be OFF to delete the relationship -
											MqlUtil.mqlCommand(context, "trigger off", true);
											DomainRelationship.disconnect(context, strConfigFeatureRelID);//Between HP & CF
										} catch(Exception ex) {
											throw new Exception(ex.toString());
										} finally {										
											MqlUtil.mqlCommand(context, "trigger on", true);
										}
									}
								}
							}
						}
					}
				}
			} catch(Exception ex) {
				throw new Exception(ex.toString());
			}
		}
	//Added by phardare on 28th September 2018 for CR72 (Incident-17616:PublishedRemoved) - ENDS
	
	//Added by phardare on 8th October 2018 for CR72 (Incident-17635:Check WIPs) - STARTS		
		/**
		 * This method is used to get all the WIP state Hardware Products (strTaskName*) For A Specific Task Name
		 * @param context
		 * @throws Exception
		 * @returns void
		 * @Author : Prasad Hardare (phardare) - 8th October 2018.
		 */
		public static MapList getAllWIPModelsForSpecificTaskName (Context context, String strTaskName)throws Exception
		{
			MapList mlFinalWIPModels = new MapList(1);
			
			StringList strObjectSelectList = new StringList(2);
			strObjectSelectList.addElement(DomainConstants.SELECT_ID);
			strObjectSelectList.addElement(DomainConstants.SELECT_NAME);
			
			try
			{
				mlFinalWIPModels = DomainObject.findObjects(context,
                                        TYPE_HARDWARE_PRODUCT,				// type pattern
                                        strTaskName+"*",         			// name pattern
                                        DomainConstants.QUERY_WILDCARD,     // revision pattern
                                        DomainConstants.QUERY_WILDCARD,     // owner pattern
                                        DomainConstants.QUERY_WILDCARD,     // vault pattern
                                        "current=='Product Management'",	// where expression
                                        false,								// Expand Type
                                        strObjectSelectList);     			// object selects
		
			} catch(Exception ex) {
				throw new Exception(ex.toString());
			}
			return mlFinalWIPModels;
		}
	//Added by phardare on 8th October 2018 for CR72 (Incident-17635:Check WIPs) - ENDS
		
	/**
	 * This method is used to log the message in log file
	 * @param context
	 * @throws Exception
	 * @returns void
	 */	
		
	public static void writeLog(BufferedWriter bw,String strContent) {		
		try {
			if( null != bw ){
				bw.write(strContent);
				bw.write("\n");
				bw.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	//Added to fetch the existing Configuration objects and there related 'Effectivity Expression' - Start
	/**
	 * This method is used to get the Effectivity Expression of all Configuration Options present in the database.
	 * exec prog iPLMOk2UseListImportExcel -method getAllConfigOptionsAndRelatedEffectivityExpressionInfo "<<Folder Path for File>>"
	 * @param context
	 * @throws Exception
	 * @returns void
	 */
	public static void getAllConfigOptionsAndRelatedEffectivityExpressionInfo ( Context _context, String[] args ) throws Exception {
		
		String sOutputFilePath = "";
		String sType = "";
		String sName = "";
		String sRev = "";
		String sConfigOptObjectId = "";
		String sConfigOptionRelId = "";	
		String sTemp = "";
		String sEffActualExpr = "";
		
		XSSFRow xssfrow = null;
		Cell cell = null;
		
		Map mpConfigOptInfo = null;
		Map mpEffData = null;
		
		int iCnt;
		int rowNum=0;
		
		EffectivityFramework ef = null;
		
		MapList mlEffData = null;
		
		try 
		{			
			StringList slObjSelect = new StringList(5);			
			slObjSelect.addElement(DomainConstants.SELECT_ID);
			slObjSelect.addElement(DomainConstants.SELECT_TYPE);
			slObjSelect.addElement(DomainConstants.SELECT_NAME);
			slObjSelect.addElement(DomainConstants.SELECT_REVISION);
			slObjSelect.addElement("to["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].id");
			
			
			if( null != args && args.length > 0 ){
				
				sOutputFilePath = args[0];
				
				//Output File name CR-72_Exisintg_Data_Sheet.xlsx
				sOutputFilePath = sOutputFilePath + java.io.File.separator + "CR72 - Data Correction For Existing Effectivity Expressions.xlsx";
				
				FileOutputStream fos = new FileOutputStream(sOutputFilePath);
				
				String sOutputsheetName = "Existing Data";//name of sheet
				
				
				XSSFWorkbook xssfwbook = new XSSFWorkbook();
				XSSFSheet xssOutputsheet = xssfwbook.createSheet(sOutputsheetName) ;
				
				xssfrow = xssOutputsheet.createRow(0);	
				
				//Create Header for Log Sheet
				String sOutputFileHeader = "Type\tName\tRevision\tObject ID\t"
						+ "rel[Configuration Options].id\tExisting Effectivity Expression\tNew Effectivity Expression\tComment";
								
				StringList slHeaderList = FrameworkUtil.split(sOutputFileHeader,"\t");
				
				
				
				int iColCnt = slHeaderList.size();
				
				for( iCnt=0; iCnt < iColCnt ; iCnt++ ){
					sTemp = (String) slHeaderList.get(iCnt);
					
					if(UIUtil.isNotNullAndNotEmpty(sTemp)){
						cell = xssfrow.createCell(iCnt);
						cell.setCellValue(sTemp);
					}
				}
				
				//Find all Configuration Options and related Data
				
				MapList mlConfigoptions = DomainObject.findObjects(
						_context,
						TYPE_CONFIGURATION_OPTION,					//String typePattern
						DomainConstants.QUERY_WILDCARD,				//String namePattern
						DomainConstants.QUERY_WILDCARD, 	//String revPattern
						DomainConstants.QUERY_WILDCARD, 	//String ownerPattern
						VAULT_ESERVICE_PRODUCTION, 		//String vaultPattern
						DomainConstants.EMPTY_STRING, 	//String whereExpression
						false, 							//boolean expandType
						slObjSelect							//StringList objectSelect
						);
				
				if( null != mlConfigoptions && mlConfigoptions.size() > 0 ){
					int iListSize = mlConfigoptions.size();
					
					for( iCnt=0; iCnt < iListSize ; iCnt++ ){
						
						sEffActualExpr = "";
						
						mpConfigOptInfo = (Map) mlConfigoptions.get(iCnt);
						sType = (String)mpConfigOptInfo.get(DomainConstants.SELECT_TYPE);
						sName = (String)mpConfigOptInfo.get(DomainConstants.SELECT_NAME);
						sRev = (String)mpConfigOptInfo.get(DomainConstants.SELECT_REVISION);
						sConfigOptObjectId = (String)mpConfigOptInfo.get(DomainConstants.SELECT_ID);
						sConfigOptionRelId = (String)mpConfigOptInfo.get("to["+RELATIONSHIP_CONFIGURATION_OPTIONS+"].id");	
												
						if(UIUtil.isNotNullAndNotEmpty(sConfigOptionRelId)){
							//Get Existing Effectivity Expression 
							ef = new EffectivityFramework();
							mlEffData = ef.getRelExpression(_context, sConfigOptionRelId);
							if( null != mlEffData && !mlEffData.isEmpty() ){
								mpEffData = (Map) mlEffData.get(0);							
								sEffActualExpr = (String) mpEffData.get("actualValue");
								if( null == sEffActualExpr || "null".equals(sEffActualExpr) ){
									sEffActualExpr = "";
								}
							}
						}	
						
						rowNum = rowNum+1;
						xssfrow = xssOutputsheet.createRow(rowNum);
						
						//Configuration Option - Type
						cell = xssfrow.createCell(0);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(sType);
						
						//Configuration Option - Name
						cell = xssfrow.createCell(1);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(sName);
						
						//Configuration Option - Revision
						cell = xssfrow.createCell(2);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(sRev);				
						
						//Configuration Option Id
						cell = xssfrow.createCell(3);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(sConfigOptObjectId);
						
						//Configuration Option Relationship Id
						cell = xssfrow.createCell(4);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(sConfigOptionRelId);
						
						//Existing Effectivity Expression
						cell = xssfrow.createCell(5);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(sEffActualExpr);						
					}
				}
				
				xssfwbook.write(fos);
				
				fos.flush();
				fos.close();
				
			} else {
				System.out.println(" Please provide the Folder path as an argument ");
				return;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}			
	}
	
	/**
	 * This method is used to update the Effectivity Expression of Configuration Options present in the database with a .xlsx as input.
	 * exec prog iPLMOk2UseListImportExcel -method dataCorrectionForExistingEffectivityExpressions "<<Input File Path>>" "<<Folder Path For Log File>>"
	 * @param context
	 * @throws Exception
	 * @returns void
	 */
	
	public static void dataCorrectionForExistingEffectivityExpressions ( Context _context, String[] args ) throws Exception {
		
		String sFilePath = "";		
		String sOutputFilePath = "";
		String sOutputsheetName = "";
		String sTemp = "";
		String sErrorMsg = "";
		
		String sType = "";
		String sName = "";
		
		String sConfigOptObjectId = "";
		String sConfigOptionRelId = "";
		String sNewEffExpr = "";
		
		XSSFRow xssfrow = null;
		XSSFRow sourceRow = null;
		XSSFCell cell = null;
		XSSFCell destCell = null;
		
		int iCnt;
		int iRowNum = 0;
		
		
		try{			
			if( null != args && args.length > 1 ){
				
				sFilePath = args[0];
				sOutputFilePath = args[1];
				
				//Output File name - CR-72_Data_Correction_Log_Sheet.xlsx
				sOutputFilePath = sOutputFilePath + java.io.File.separator + "CR-72_Data_Correction_Log_Sheet.xlsx";
				
				sOutputsheetName = "Data Correction Log Sheet";//name of sheet
				
			} else {
				System.out.println("Please provide the Input file and LogSheet folder path");
				return;
			}
				
			File inputFile = new File(sFilePath);		
			
			if(inputFile.exists()){
				
				FileInputStream fileInStr = new FileInputStream(inputFile);
				FileOutputStream fos = new FileOutputStream(sOutputFilePath);
				
				try{
					ContextUtil.pushContext(_context, PropertyUtil.getSchemaProperty(_context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					
					//Output file
					XSSFWorkbook xssfLogwbook = new XSSFWorkbook();
					XSSFSheet xssOutputsheet = xssfLogwbook.createSheet(sOutputsheetName);
					
					xssfrow = xssOutputsheet.createRow(0);	
					//Create Header for Log Sheet
					String sOutputFileHeader = "Type\tName\tObject ID\trel[Configuration Options].id\tNew Effectivity Expression\tStatus";
									
					StringList slHeaderList = FrameworkUtil.split(sOutputFileHeader,"\t");
					
					int iColCnt = slHeaderList.size();					
					
					for( iCnt=0; iCnt < iColCnt ; iCnt++ ){
						sTemp = (String) slHeaderList.get(iCnt);
						
						if(UIUtil.isNotNullAndNotEmpty(sTemp)){
							cell = xssfrow.createCell(iCnt);
							cell.setCellValue(sTemp);
						}
					}
					
					//Input file					
					XSSFWorkbook xssfInputwbook = new XSSFWorkbook(fileInStr);				
					XSSFSheet xssfInputSheet = xssfInputwbook.getSheetAt(0);
										
					for(int iRowCnt=0; iRowCnt <= xssfInputSheet.getLastRowNum(); iRowCnt++){
						
						sErrorMsg = "";
						
						iRowNum = iRowNum+1;
						
						sourceRow = xssfInputSheet.getRow(iRowCnt);
						
						cell = sourceRow.getCell(0, Row.CREATE_NULL_AS_BLANK);						
						sType = getValue(cell);
						
						cell = sourceRow.getCell(1, Row.CREATE_NULL_AS_BLANK);
						sName = getValue(cell);
						
						
						cell = sourceRow.getCell(2, Row.CREATE_NULL_AS_BLANK);
						sConfigOptObjectId = (getValue(cell)).trim();
						
						
						cell = sourceRow.getCell(3, Row.CREATE_NULL_AS_BLANK);
						sConfigOptionRelId =  (getValue(cell)).trim();
						
						
						cell = sourceRow.getCell(4, Row.CREATE_NULL_AS_BLANK);
						sNewEffExpr =  (getValue(cell)).trim();
						
						//Copy the cell values to Log File
						xssfrow = xssOutputsheet.createRow(iRowNum);
						
						destCell = xssfrow.createCell(0);
						destCell.setCellType(Cell.CELL_TYPE_STRING);
						destCell.setCellValue(sType);
						
						destCell = xssfrow.createCell(1);
						destCell.setCellType(Cell.CELL_TYPE_STRING);
						destCell.setCellValue(sName);
						
						destCell = xssfrow.createCell(2);
						destCell.setCellType(Cell.CELL_TYPE_STRING);
						destCell.setCellValue(sConfigOptObjectId);
						
						destCell = xssfrow.createCell(3);
						destCell.setCellType(Cell.CELL_TYPE_STRING);
						destCell.setCellValue(sConfigOptionRelId);
						
						destCell = xssfrow.createCell(4);
						destCell.setCellType(Cell.CELL_TYPE_STRING);
						destCell.setCellValue(sNewEffExpr);
												
						try{							
							//Update New Effectivity Expression
							setEffectivityUI(_context, sConfigOptionRelId, sNewEffExpr, sConfigOptObjectId);
							
						}catch(Exception exc){
							sErrorMsg = exc.getMessage();
							
							if( null == sErrorMsg || "null".equals(sErrorMsg) ){								
								sErrorMsg = "Error while updating Effectivity Expression";
							}							
						}
						
						destCell = xssfrow.createCell(5);
						destCell.setCellType(Cell.CELL_TYPE_STRING);
												
						if( UIUtil.isNotNullAndNotEmpty(sErrorMsg) ){
							destCell.setCellValue(sErrorMsg);
						} else {
							destCell.setCellValue("Effectivity Expression updated Successfully");
						}
					}
					
					xssfLogwbook.write(fos);					
					fos.flush();				
					
				}catch(Exception exce){
					exce.printStackTrace();
				}finally {
					ContextUtil.popContext(_context);
					fileInStr.close();
					fos.close();
				}
			}			
			
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	//Added to fetch the existing Configuration objects and there related 'Effectivity Expression' - End
}
