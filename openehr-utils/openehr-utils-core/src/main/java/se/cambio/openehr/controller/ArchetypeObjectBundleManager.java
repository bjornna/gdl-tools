package se.cambio.openehr.controller;

import com.marand.thinkehr.adl.flattener.ArchetypeFlattener;
import com.marand.thinkehr.adl.parser.AdlDeserializer;
import com.marand.thinkehr.adl.rm.ThinkEhrRmModel;
import org.apache.log4j.Logger;
import org.openehr.am.archetype.Archetype;
import org.openehr.jaxb.am.DifferentialArchetype;
import org.openehr.jaxb.am.FlatArchetype;
import org.openehr.jaxb.rm.ArchetypeId;
import se.acode.openehr.parser.ADLParser;
import se.cambio.cm.model.archetype.dto.ArchetypeDTO;
import se.cambio.cm.model.archetype.vo.ArchetypeObjectBundleCustomVO;
import se.cambio.cm.model.util.CMTypeFormat;
import se.cambio.openehr.controller.session.data.ArchetypeManager;
import se.cambio.openehr.util.IOUtils;
import se.cambio.openehr.util.exceptions.InstanceNotFoundException;
import se.cambio.openehr.util.exceptions.InternalErrorException;

public class ArchetypeObjectBundleManager {
    private final ArchetypeManager archetypeManager;
    private ArchetypeDTO archetypeDTO = null;
    protected boolean correctlyParsed = false;

    public ArchetypeObjectBundleManager(ArchetypeDTO archetypeDTO,ArchetypeManager archetypeManager) {
        this.archetypeDTO = archetypeDTO;
        this.archetypeManager = archetypeManager;
    }

    public void buildArchetypeObjectBundleCustomVO() throws InternalErrorException {
        Object obj = null;
        if (archetypeDTO.getAobcVO() != null){
            obj = IOUtils.getObject(archetypeDTO.getAobcVO());
        }
        if (!(obj instanceof ArchetypeObjectBundleCustomVO)){
            Logger.getLogger(ArchetypeObjectBundleManager.class).info("Parsing archetype '" + archetypeDTO.getId() + "'...");
            long startTime = System.currentTimeMillis();
            try{
                if (CMTypeFormat.ADL_FORMAT.getFormat().equals(archetypeDTO.getFormat())){
                    generateArchetype14Data();
                } else if (CMTypeFormat.ADLS_FORMAT.getFormat().equals(archetypeDTO.getFormat())) {
                    generateArchetype20Data();
                }
                    correctlyParsed = true;
            }catch(InternalErrorException e){
                throw e;
            }catch(Error e){
                new InternalErrorException(new Exception("Failed to parse archetype '"+archetypeDTO.getId()+"'", e));
            }catch(Exception e){
                new InternalErrorException(new Exception("Failed to parse archetype '"+archetypeDTO.getId()+"'", e));
            }
            long endTime = System.currentTimeMillis();
            Logger.getLogger(ArchetypeObjectBundleManager.class).info("Done (" + (endTime - startTime) + " ms)");
        }else{
            correctlyParsed = true;
        }
    }


    private void generateArchetype14Data()
            throws InternalErrorException{
        try{
            ADLParser adlParser = new ADLParser(archetypeDTO.getSource());
            Archetype ar = adlParser.parse();
            archetypeDTO.setAom(IOUtils.getBytes(ar));
            GenericObjectBundleManager genericObjectBundleManager = new GenericObjectBundleManager(ar, archetypeManager.getArchetypes().getArchetypeMap());
            ArchetypeObjectBundleCustomVO archetypeObjectBundleCustomVO = genericObjectBundleManager.generateObjectBundleCustomVO();
            archetypeDTO.setAobcVO(IOUtils.getBytes(archetypeObjectBundleCustomVO));
        } catch (Exception e) {
            throw new InternalErrorException(e);
        }
    }

    private void generateArchetype20Data()
            throws InternalErrorException{
        try{
            AdlDeserializer adlDeserializer = new AdlDeserializer(new ThinkEhrRmModel());
            DifferentialArchetype differentialArchetype = adlDeserializer.parse(archetypeDTO.getSource());
            FlatArchetype flatArchetype = parseAndFlattenArchetype(differentialArchetype);
            byte[] flatArchetypeBytes = IOUtils.getBytes(flatArchetype);
            archetypeDTO.setAom(flatArchetypeBytes);
            GenericObjectBundle20Manager genericObjectBundle20Manager = new GenericObjectBundle20Manager(flatArchetype, archetypeManager);
            ArchetypeObjectBundleCustomVO archetypeObjectBundleCustomVO = genericObjectBundle20Manager.generateObjectBundleCustomVO();
            archetypeDTO.setAobcVO(IOUtils.getBytes(archetypeObjectBundleCustomVO));
        } catch (Exception e) {
            throw new InternalErrorException(e);
        }
    }

    protected FlatArchetype parseAndFlattenArchetype(DifferentialArchetype differentialArchetype) throws InstanceNotFoundException, InternalErrorException {
        ArchetypeFlattener flattener = new ArchetypeFlattener(new ThinkEhrRmModel());
        FlatArchetype parent;
        ArchetypeId parentArchetypeId = differentialArchetype.getParentArchetypeId();
        if (parentArchetypeId != null) {
            parent = archetypeManager.getArchetypes().getArchetypeAOM2ById(parentArchetypeId.getValue());
        } else {
            parent = null;
        }
        return flattener.flatten(parent, differentialArchetype);
    }
}
