package se.cambio.cds.gdl.converters.drools;





import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.drools.core.util.DroolsStreamUtils;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public class CompilationManager {
private static Logger log = Logger.getLogger(CompilationManager.class);
    public static byte[] compile(String guideStr) throws CompilationErrorException {
        log.info("compile:" + guideStr);
        Resource guide = ResourceFactory.newByteArrayResource(guideStr.getBytes());
        return compile(guide);
    }

    public static byte[] compile(Resource guide) throws CompilationErrorException {
        try {
            Collection<Resource> guides = new ArrayList<Resource>();
            guides.add(guide);
            KieBase kb = getKnowledgeBase(guides);
            if(kb.getKiePackages().iterator().hasNext()) {
                KiePackage kpakage = kb.getKiePackages().iterator().next();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream objOut = new ObjectOutputStream(baos);
                objOut.writeObject(kpakage);
                objOut.flush();
                return baos.toByteArray();
            }else{
                throw new CompilationErrorException("There where no KIE packages to build from");
            }
        } catch (IOException e) {
            throw new CompilationErrorException(e);
        }catch (NoSuchElementException e){
            throw new CompilationErrorException(e);
        }
    }

    public static KieBase getKnowledgeBase(Collection<Resource> guides)
            throws CompilationErrorException{
        return getKnowledgeBase(guides, null);
    }

    public static KieBase getKnowledgeBase(Collection<Resource> guides, KnowledgeBuilderConfiguration kbc)
            throws CompilationErrorException{
       final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbc);
        for (Resource guia : guides) {
            kbuilder.add(guia, ResourceType.DRL);



        }
Collection<KnowledgePackage> packages = kbuilder.getKnowledgePackages();
        kbase.addKnowledgePackages(packages);

        KnowledgeBuilderErrors packErrors = kbuilder.getErrors();
        if (packErrors.size()>0){
            throw new CompilationErrorException(packErrors.iterator().next().getMessage());
        }

        return kbase;
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