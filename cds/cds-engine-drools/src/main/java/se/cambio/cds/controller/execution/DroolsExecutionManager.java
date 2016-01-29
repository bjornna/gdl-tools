/**
 * @author iago.corbal
 */
package se.cambio.cds.controller.execution;

import org.apache.log4j.Logger;

import org.kie.api.KieBase;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.runtime.StatelessKnowledgeSession;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import se.cambio.cds.model.instance.ElementInstance;
import se.cambio.cds.util.ExecutionLogger;
import se.cambio.cds.util.RuleExecutionWMLogger;
import se.cambio.cm.model.guide.dto.GuideDTO;
import se.cambio.openehr.util.ExceptionHandler;
import se.cambio.openehr.util.exceptions.InternalErrorException;
import se.cambio.openehr.util.misc.CDSConfigurationParametersManager;
import se.cambio.openehr.util.misc.DataValueGenerator;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.*;

public class DroolsExecutionManager {

    public Map<String, KieBase> _knowledgeBaseCache = null;
    private static DroolsExecutionManager _instance = null;
    private static final short MAX_KNOWLEDGE_BASE_CACHE = 10;
    private boolean _useCache = true;
    private ExecutionLogger _logger = null;
    private Long _timeOutInMillis = null;

    private DroolsExecutionManager(){
        _knowledgeBaseCache = Collections.synchronizedMap(new LinkedHashMap <String, KieBase>());
    }

    public static void executeGuides(
            Collection<GuideDTO> guideDTOs,
            Calendar date,
            Collection<Object> workingMemoryObjects,
            ExecutionLogger executionLogger)
            throws InternalErrorException{
        KieBase kb = null;
        if (getDelegate()._useCache){
            kb =  getKnowledgeBase(guideDTOs);
        }else{
            kb = generateKnowledgeBase(guideDTOs);
        }
        Collection<String> guideIds = new ArrayList<String>();
        for(GuideDTO guideDTO: guideDTOs){
            guideIds.add(guideDTO.getId());
        }
        executeGuides(guideIds, kb, date, workingMemoryObjects, executionLogger);
    }


    private static void executeGuides(
            Collection<String> guideIds,
            KieBase knowledgeBase,
            Calendar date,
            Collection<Object> workingMemoryObjects,
            ExecutionLogger executionLogger)
            throws InternalErrorException{
        try{
            final StatelessKieSession session = knowledgeBase.newStatelessKieSession();

            final RuleExecutionWMLogger ruleExecutionWMLogger = new RuleExecutionWMLogger();
            session.addEventListener(ruleExecutionWMLogger);
            if (date==null){
                date = Calendar.getInstance();
            }
            final DvDateTime currentDateTime = DataValueGenerator.toDvDateTime(date);
            session.setGlobal("$currentDateTime", currentDateTime);
            getDelegate()._logger = executionLogger;
            session.setGlobal("$executionLogger", executionLogger);
            session.setGlobal("$bindingMap", new HashMap<ElementInstance, Map<String, Boolean>>());
            session.setGlobal("$execute", true);
            int initSalience = 0;
            for(String guideId: guideIds) {
                session.setGlobal(getGuideSalienceId(guideId), initSalience);
                initSalience = initSalience+1000;
            }
            session.execute(workingMemoryObjects);
            executionLogger.setFiredRules(ruleExecutionWMLogger.getFiredRules());
        }catch(Exception e){
            e.printStackTrace();
            throw new InternalErrorException(e);
        }
    }

    public static void cancelCurrentExecution(){
        if (getDelegate()._logger!=null){
            //TODO Cancel current execution is done through the logger... should change this behaviour
            getDelegate()._logger.cancelExecution();
        }
    }


    private static KieBase getKnowledgeBase(Collection<GuideDTO> guideDTOs)
            throws InternalErrorException{
        if (guideDTOs==null || guideDTOs.isEmpty()){
            return null;
        }
        String guideIdsId = getGuideIdsId(guideDTOs);
        KieBase kb = getDelegate()._knowledgeBaseCache.get(guideIdsId);
        if (kb==null){
            kb = DroolsExecutionManager.generateKnowledgeBase(guideDTOs);
            getDelegate()._knowledgeBaseCache.put(guideIdsId, kb);
            if (getDelegate()._knowledgeBaseCache.size()>MAX_KNOWLEDGE_BASE_CACHE){
                //Remove oldest KB in cache
                String oldestGuideIdsId = getDelegate()._knowledgeBaseCache.keySet().iterator().next();
                getDelegate()._knowledgeBaseCache.remove(oldestGuideIdsId);
                Logger.getLogger(DroolsExecutionManager.class).warn("KnowledgeBase cache full. Removing oldest KB: " + guideIdsId);
            }
        }
        return kb;
    }



    public static void setUseCache(boolean useCache){
        Logger.getLogger(DroolsExecutionManager.class).warn("USE-CACHE on cds engine changed to '" + useCache + "'");
        getDelegate()._useCache = useCache;
    }

    public static void clearCache(){
        getDelegate()._knowledgeBaseCache.clear();
    }

    private static String getGuideIdsId(Collection<GuideDTO> guideDTOs) {
        List<String> guideIdsIdList = new ArrayList<String>();
        for (GuideDTO guideDTO : guideDTOs) {
            guideIdsIdList.add(guideDTO.getId());
        }
        Collections.sort(guideIdsIdList);
        StringBuffer guideIdsIdSB = new StringBuffer();
        for (String guideId : guideIdsIdList) {
            guideIdsIdSB.append(guideId);
        }
        return guideIdsIdSB.toString();
    }

    private static KieBase generateKnowledgeBase(Collection<GuideDTO> guideDTOs) {
        ArrayList<KnowledgePackage> knowledgePackages = new ArrayList<KnowledgePackage>();
        for (GuideDTO guideDTO : guideDTOs) {
            if (guideDTO.getCompiledGuide()==null){
                Logger.getLogger(DroolsExecutionManager.class).warn("Guide '" + guideDTO.getId() + "' is not compiled.");
            };
            KnowledgePackage knowledgePackage =
                    DroolsExecutionManager.getKnowledgePackage(guideDTO.getCompiledGuide());
            if (knowledgePackage!=null){
                knowledgePackages.add(knowledgePackage);
            }
        }
        final KieBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        if (!knowledgePackages.isEmpty()){
            knowledgeBase.getKiePackages().addAll(knowledgePackages);
            //knowledgeBase.addKnowledgePackages(knowledgePackages);
        }
        return knowledgeBase;
    }

    private static KnowledgePackage getKnowledgePackage(byte[] guiaCompilada){
        if (guiaCompilada==null){
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(guiaCompilada);
        ObjectInputStream objInput = null;
        KnowledgePackage knowledgePackage = null;
        try {
            objInput = new ObjectInputStream(bais);
            knowledgePackage = (KnowledgePackage)objInput.readObject();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return null;
        }
        return knowledgePackage;
    }

    private static String getGuideString(Collection<GuideDTO> guides){
        StringBuffer guidesStr = new StringBuffer();
        for (GuideDTO guideDTO : guides) {
            guidesStr.append(guideDTO.getSource());
        }
        return guidesStr.toString();
    }

    public static DroolsExecutionManager getDelegate(){
        if (_instance==null){
            _instance = new DroolsExecutionManager();
        }
        return _instance;
    }

    public static Long getExecutionTimeOut(){
        if (getDelegate()._timeOutInMillis==null){
            try {
                String timeOutStr = CDSConfigurationParametersManager.getParameter(CDSConfigurationParametersManager.CDS_EXECUTION_TIMEOUT);
                getDelegate()._timeOutInMillis = Long.parseLong(timeOutStr);
            } catch (Exception e) {
                Logger.getLogger(DroolsExecutionManager.class).info("No CDS execution timeout or errors found loading it. Timeout will be disabled.");
            }
            if (getDelegate()._timeOutInMillis==null){
                getDelegate()._timeOutInMillis = Long.MAX_VALUE;
            }
        }
        return getDelegate()._timeOutInMillis;
    }

    public static String getGuideSalienceId(String guideId){
        return "$"+guideId.replaceAll("[^a-zA-Z0-9]+","")+"_salience";
    }

}
/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 2.0/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  2.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *
 *  The Initial Developers of the Original Code are Iago Corbal and Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2012-2013
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */