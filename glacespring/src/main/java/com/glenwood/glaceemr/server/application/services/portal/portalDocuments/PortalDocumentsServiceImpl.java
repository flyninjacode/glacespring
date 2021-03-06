package com.glenwood.glaceemr.server.application.services.portal.portalDocuments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaQuery;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glenwood.glaceemr.server.application.models.FileDetails;
import com.glenwood.glaceemr.server.application.models.FileName;
import com.glenwood.glaceemr.server.application.models.PatientPortalSharedDocs;
import com.glenwood.glaceemr.server.application.models.PatientSharedDocumentLog;
import com.glenwood.glaceemr.server.application.repositories.FileDetailsRepository;
import com.glenwood.glaceemr.server.application.repositories.FileNameRepository;
import com.glenwood.glaceemr.server.application.repositories.PatientPortalSharedDocsRepository;
import com.glenwood.glaceemr.server.application.repositories.PatientSharedDocumentLogRepository;
import com.glenwood.glaceemr.server.application.services.audittrail.AuditTrailEnumConstants;
import com.glenwood.glaceemr.server.application.services.audittrail.AuditTrailSaveService;
import com.glenwood.glaceemr.server.application.specifications.PortalDocumentsSpecification;

@Service
public class PortalDocumentsServiceImpl implements PortalDocumentsService {

	@Autowired
	PatientPortalSharedDocsRepository portalSharedDocsRepository;

	@Autowired
	PatientSharedDocumentLogRepository patientSharedDocumentLogRepository;

	@Autowired
	FileDetailsRepository fileDetailsRepository;

	@Autowired
	FileNameRepository fileNameRepository;

	@Autowired
	EntityManager em;

	@Autowired
	EntityManagerFactory emf;

	@Autowired
	ObjectMapper objectMapper;

	ExportHTMLAsPdf exportHTMLAsPdf=new ExportHTMLAsPdf();
	
	@Autowired
	AuditTrailSaveService auditTrailSaveService;
	
	@Autowired
	HttpServletRequest request;

	@Override
	public List<PatientPortalSharedDocs> getPatientSharedDocs(int patientId, int pageOffset, int pageIndex) {

		List<PatientPortalSharedDocs> sharedDocsList=portalSharedDocsRepository.findAll(PortalDocumentsSpecification.SharedDocsList(patientId), PortalDocumentsSpecification.createPortalSharedDocsPageRequestByDescDate(pageIndex, pageOffset)).getContent();

		auditTrailSaveService.LogEvent(AuditTrailEnumConstants.LogType.GLACE_LOG,AuditTrailEnumConstants.LogModuleType.PATIENTPORTAL,
				AuditTrailEnumConstants.LogActionType.READ,1,AuditTrailEnumConstants.Log_Outcome.SUCCESS,"Requested patient's shared documents",-1,
				request.getRemoteAddr(),patientId,"",
				AuditTrailEnumConstants.LogUserType.PATIENT_LOGIN,"Patient with id "+patientId+"requested patient's shared documents.","");
		
		return sharedDocsList;

	}


	@Override
	public List<PatientSharedDocumentLog> getPatientVisitSummary(int patientId, int pageOffset, int pageIndex) throws Exception {

		List<PatientSharedDocumentLog> visitSummary=patientSharedDocumentLogRepository.findAll(PortalDocumentsSpecification.getPatientVisitSummary(patientId), PortalDocumentsSpecification.createPortalVisitSummaryPageRequestByDescDate(pageIndex, pageOffset)).getContent();
		
		auditTrailSaveService.LogEvent(AuditTrailEnumConstants.LogType.GLACE_LOG,AuditTrailEnumConstants.LogModuleType.PATIENTPORTAL,
				AuditTrailEnumConstants.LogActionType.READ,1,AuditTrailEnumConstants.Log_Outcome.SUCCESS,"Requested patient's visit summary",-1,
				request.getRemoteAddr(),patientId,"",
				AuditTrailEnumConstants.LogUserType.PATIENT_LOGIN,"Patient with id "+patientId+"requested patient's visit summary.","");
		
		return visitSummary;

	}


	/*Method to execute the custom queries*/
	public List<Map<String, Object>> CustomQueryResult(List<String> listinorder,CriteriaQuery<Object> cq) 
	{

		try{

			Iterator iter= em.createQuery(cq).getResultList().iterator();
			List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();

			while ( iter.hasNext() ) {
				Object[] row = (Object[]) iter.next();
				Map<String,Object> newMap=  new HashMap<String,Object>();
				for(int i=0;i<row.length;i++)
				{
					newMap.put(listinorder.get(i),(Object)row[i]);
				}
				resultList.add(newMap);

			}
			return resultList;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}


	@Override
	public FileDetails getFileDetails(int patientId, int fileId) {

		FileDetails fileDetails=fileDetailsRepository.findOne(PortalDocumentsSpecification.getFileDetailsByFileId(patientId, fileId));

		auditTrailSaveService.LogEvent(AuditTrailEnumConstants.LogType.GLACE_LOG,AuditTrailEnumConstants.LogModuleType.PATIENTPORTAL,
				AuditTrailEnumConstants.LogActionType.READ,1,AuditTrailEnumConstants.Log_Outcome.SUCCESS,"Requested patient's file details",-1,
				request.getRemoteAddr(),patientId,"",
				AuditTrailEnumConstants.LogUserType.PATIENT_LOGIN,"Patient with id "+patientId+"requested patient's file details.","");
		
		return fileDetails;
	}


	@Override
	public List<FileName> getFileNameDetails(int patientId, int fileId) {

		List<FileName> filesList=fileNameRepository.findAll(PortalDocumentsSpecification.getFileNamesList(patientId, fileId));

		auditTrailSaveService.LogEvent(AuditTrailEnumConstants.LogType.GLACE_LOG,AuditTrailEnumConstants.LogModuleType.PATIENTPORTAL,
				AuditTrailEnumConstants.LogActionType.READ,1,AuditTrailEnumConstants.Log_Outcome.SUCCESS,"Requested patient's file details list",-1,
				request.getRemoteAddr(),patientId,"",
				AuditTrailEnumConstants.LogUserType.PATIENT_LOGIN,"Patient with id "+patientId+"requested patient's file details list.","");
		
		return filesList;
	}


	@Override
	public PortalFileDetailBean getPatientCDAFileDetails(int patientId, int encounterId,String fileName) throws Exception {

		PortalFileDetailBean fileDetailBean=new PortalFileDetailBean();

		File f = new File(SharedFolderUtil.getSharedFolderPath()+"GlacePortal/CDA/"+patientId+"/PatientCDA_"+patientId+"_"+encounterId+".pdf");
		if(f.exists() && !f.isDirectory()) { 

			fileDetailBean.setFileName("PatientCDA_"+patientId+"_"+encounterId+".pdf");
			fileDetailBean.setFileNameWithLocation(SharedFolderUtil.getSharedFolderPath()+"GlacePortal/CDA/"+patientId+"/PatientCDA_"+patientId+"_"+encounterId+".pdf");
			fileDetailBean.setFilePath(SharedFolderUtil.getSharedFolderPath()+"GlacePortal/CDA/"+patientId);
			fileDetailBean.setPatientId(patientId);
		}else{
			fileDetailBean=exportHTMLAsPdf.getCDAPdfDetails(CDAFileManager.processCDAFile(1, fileName), patientId, encounterId,"exportAsPdf");
			fileDetailBean.setPatientId(patientId);
		}

		auditTrailSaveService.LogEvent(AuditTrailEnumConstants.LogType.GLACE_LOG,AuditTrailEnumConstants.LogModuleType.PATIENTPORTAL,
				AuditTrailEnumConstants.LogActionType.READ,1,AuditTrailEnumConstants.Log_Outcome.SUCCESS,"Requested patient's CDA",-1,
				request.getRemoteAddr(),patientId,"",
				AuditTrailEnumConstants.LogUserType.PATIENT_LOGIN,"Patient with id "+patientId+"requested patient's CDA.","");
		
		return fileDetailBean;
	}




}
