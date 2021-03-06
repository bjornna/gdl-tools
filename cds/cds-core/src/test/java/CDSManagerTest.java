import org.junit.Before;
import org.junit.Test;
import se.cambio.cds.controller.cds.CDSManager;
import se.cambio.cds.gdl.model.expression.OperatorKind;
import se.cambio.cds.model.facade.execution.vo.GeneratedArchetypeReference;
import se.cambio.cds.model.facade.execution.vo.GeneratedElementInstance;
import se.cambio.cds.model.facade.execution.vo.PredicateGeneratedElementInstance;
import se.cambio.cds.model.facade.execution.vo.PredicateGeneratedElementInstanceBuilder;
import se.cambio.cds.model.instance.ArchetypeReference;
import se.cambio.cds.util.Domains;
import se.cambio.cds.util.GeneratedElementInstanceCollection;
import se.cambio.openehr.util.OpenEHRConstUI;
import se.cambio.openehr.util.UserConfigurationManager;
import se.cambio.openehr.util.exceptions.InternalErrorException;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;


public class CDSManagerTest {

    @Before
    public void configCM() throws URISyntaxException, InternalErrorException {
        UserConfigurationManager.setParameter(UserConfigurationManager.TERMINOLOGIES_FOLDER_KW, CDSManagerTest.class.getClassLoader().getResource("terminologies").toURI().getPath());
        UserConfigurationManager.setParameter(UserConfigurationManager.ARCHETYPES_FOLDER_KW, CDSManagerTest.class.getClassLoader().getResource("archetypes").toURI().getPath());
        UserConfigurationManager.setParameter(UserConfigurationManager.TEMPLATES_FOLDER_KW, CDSManagerTest.class.getClassLoader().getResource("templates").toURI().getPath());
    }


    @Test
    public void shouldContainGeneratedElementInstances(){
        GeneratedElementInstanceCollection geic = new GeneratedElementInstanceCollection();

        GeneratedArchetypeReference gar = new GeneratedArchetypeReference(Domains.EHR_ID, "openEHR-EHR-EVALUATION.contact.v1", null);
        new GeneratedElementInstance("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0006]", null, gar, null, OpenEHRConstUI.NULL_FLAVOUR_CODE_NO_INFO);
        geic.add(gar);

        gar = new GeneratedArchetypeReference(Domains.EHR_ID, "openEHR-EHR-EVALUATION.contact.v1", null);
        new GeneratedElementInstance("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0004]", null, gar, null, OpenEHRConstUI.NULL_FLAVOUR_CODE_NO_INFO);
        geic.add(gar);

        Collection<ArchetypeReference> ars = CDSManager.getEHRArchetypeReferencesWithEventTimeElements(geic);
        assertEquals(1, ars.size()); //Compact ars
        ArchetypeReference ar = ars.iterator().next();

        assertNotNull(ar.getElementInstancesMap().get("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0003]")); //Add event time paths
        assertNotNull(ar.getElementInstancesMap().get("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0004]"));
        assertNotNull(ar.getElementInstancesMap().get("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0006]"));
    }

    @Test
    public void shouldContainPredicateGeneratedElementInstances(){
        GeneratedElementInstanceCollection geic = new GeneratedElementInstanceCollection();

        GeneratedArchetypeReference gar = new GeneratedArchetypeReference(Domains.EHR_ID, "openEHR-EHR-EVALUATION.contact.v1", null);
        new PredicateGeneratedElementInstanceBuilder()
                .setId("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0004]")
                .setArchetypeReference(gar)
                .setNullFlavour(OpenEHRConstUI.NULL_FLAVOUR_CODE_NO_INFO)
                .setOperatorKind(OperatorKind.INEQUAL)
                .createPredicateGeneratedElementInstance();
        geic.add(gar);

        gar = new GeneratedArchetypeReference(Domains.EHR_ID, "openEHR-EHR-EVALUATION.contact.v1", null);
        new PredicateGeneratedElementInstanceBuilder()
                .setId("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0003]")
                .setArchetypeReference(gar)
                .setNullFlavour(OpenEHRConstUI.NULL_FLAVOUR_CODE_NO_INFO)
                .setOperatorKind(OperatorKind.INEQUAL)
                .createPredicateGeneratedElementInstance();
        geic.add(gar);

        gar = new GeneratedArchetypeReference(Domains.EHR_ID, "openEHR-EHR-EVALUATION.contact.v1", null);
        new PredicateGeneratedElementInstanceBuilder()
                .setId("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0004]")
                .setArchetypeReference(gar)
                .setNullFlavour(OpenEHRConstUI.NULL_FLAVOUR_CODE_NO_INFO)
                .setOperatorKind(OperatorKind.INEQUAL)
                .createPredicateGeneratedElementInstance();
        geic.add(gar);

        gar = new GeneratedArchetypeReference(Domains.EHR_ID, "openEHR-EHR-EVALUATION.contact.v1", null);
        new PredicateGeneratedElementInstanceBuilder()
                .setId("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0003]")
                .setArchetypeReference(gar)
                .setNullFlavour(OpenEHRConstUI.NULL_FLAVOUR_CODE_NO_INFO)
                .setOperatorKind(OperatorKind.EQUALITY)
                .createPredicateGeneratedElementInstance();
        geic.add(gar);

        Collection<ArchetypeReference> ars = CDSManager.getEHRArchetypeReferencesWithEventTimeElements(geic);
        assertEquals(1, ars.size()); //Compact ars
        Iterator<ArchetypeReference> i = ars.iterator();
        ArchetypeReference ar = i.next();
        assertTrue(ar instanceof GeneratedArchetypeReference);
        assertTrue(ar.getElementInstancesMap().get("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0003]") instanceof PredicateGeneratedElementInstance); //Add event time paths
        assertTrue(ar.getElementInstancesMap().get("openEHR-EHR-EVALUATION.contact.v1/data[at0001]/items[at0004]") instanceof PredicateGeneratedElementInstance);
    }
}
