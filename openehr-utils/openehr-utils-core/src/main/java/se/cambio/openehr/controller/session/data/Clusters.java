package se.cambio.openehr.controller.session.data;

import org.openehr.am.archetype.ontology.ArchetypeTerm;
import se.cambio.openehr.model.archetype.vo.ClusterVO;
import se.cambio.openehr.util.OpenEHRConst;
import se.cambio.openehr.util.PathUtils;

import java.util.*;

public class Clusters {
    private static Clusters _instance = null;
    private  Map<String, ClusterVO> _clustersById = null;
    private Map<String, Map<String, ClusterVO>> _templateClustersByTemplateIdAndId = null;


    private Clusters(){
        init();
    }

    public void init(){
        _clustersById = new HashMap<String, ClusterVO>();
        _templateClustersByTemplateIdAndId = new HashMap<String, Map<String,ClusterVO>>();
    }

    public static void loadClusters(Collection<ClusterVO> clusterVOs){
        for (ClusterVO clusterVO : clusterVOs) {
            registerCluster(clusterVO);
        }
    }

    public static void registerCluster(ClusterVO clusterVO){
        if (clusterVO.getIdTemplate()==null){
            getDelegate()._clustersById.put(clusterVO.getId(), clusterVO);
        }else{
            getClusterVOMap(clusterVO.getIdTemplate()).put(clusterVO.getId(), clusterVO);
        }
    }

    private static Map<String, ClusterVO> getClusterVOMap(String idTemplate){
        Map<String, ClusterVO> map = getDelegate()._templateClustersByTemplateIdAndId.get(idTemplate);
        if (map==null){
            map = new LinkedHashMap<String, ClusterVO>();
            getDelegate()._templateClustersByTemplateIdAndId.put(idTemplate, map);
        }
        return map;
    }

    public static ClusterVO getClusterVO(String idTemplate, String idCluster){
        if (idTemplate!=null){
            return getClusterVOMap(idTemplate).get(idCluster);
        }else{
            return getDelegate()._clustersById.get(idCluster);
        }
    }

    public static ClusterVO getClusterVOWithCardinalityGT1(String idTemplate, String idCluster){
        ClusterVO clusterVO = getClusterVO(idTemplate, idCluster);
        while (clusterVO!=null){
            if (clusterVO.getUpperCardinality()==null || clusterVO.getUpperCardinality()>1){
                return clusterVO;
            }else{
                clusterVO = getClusterVO(idTemplate, clusterVO.getIdParent());
            }
        }
        return null;
    }

    public static Collection<ClusterVO> getSections(String idTemplate){
        Collection<ClusterVO> sections = new ArrayList<ClusterVO>();
        for (ClusterVO clusterVO : getClusterVOMap(idTemplate).values()) {
            if(clusterVO.getRMType().equals(OpenEHRConst.SECTION)){
                sections.add(clusterVO);
            }
        }
        return sections;
    }

    public static Clusters getDelegate(){
        if (_instance == null){
            _instance = new Clusters();
        }
        return _instance;
    }

    public static String getText(ClusterVO clusterVO, String lang){
        return getText(clusterVO.getIdTemplate(), clusterVO.getId(), lang);
    }

    public static String getText(String idTemplate, String idElement, String lang){
        ClusterVO clusterVO = getClusterVO(idTemplate, idElement);
        if (clusterVO!=null){
            ArchetypeTerm archetypeTem = getArchetypeTerm(idTemplate, idElement, lang);
            if (archetypeTem!=null){
                return archetypeTem.getText();
            }else{
                return clusterVO.getName();
            }
        }else{
            return "*UNKNOWN*";
        }
    }

    public static String getDescription(ClusterVO clusterVO, String lang){
        return getDescription(clusterVO.getIdTemplate(), clusterVO.getId(), lang);
    }

    public static String getDescription(String idTemplate, String idElement, String lang){
        ClusterVO clusterVO = getClusterVO(idTemplate, idElement);
        if (clusterVO!=null){
            ArchetypeTerm archetypeTem = getArchetypeTerm(idTemplate, idElement, lang);
            if (archetypeTem!=null){
                return archetypeTem.getDescription();
            }else{
                return clusterVO.getDescription();
            }
        }else{
            return "*UNKNOWN*";
        }
    }

    private static ArchetypeTerm getArchetypeTerm(String idTemplate, String idElement, String lang){
        String atCode = idElement.substring(idElement.lastIndexOf("[")+1, idElement.length()-1);
        ArchetypeTerm archetypeTem = null;
        if (idTemplate==null){
            String archetypeId = idElement.substring(0, idElement.indexOf("/"));
            archetypeTem = Archetypes.getArchetypeTerm(archetypeId, lang, atCode);
        }else{
            String archetypeId = PathUtils.getLastArchetypeIdInPath(idElement, Archetypes.getAOMMap().keySet());
            if (archetypeId==null){
                archetypeTem = Templates.getArchetypeTerm(idTemplate, lang, atCode);
            }else{
                archetypeTem = Archetypes.getArchetypeTerm(archetypeId, lang, atCode);
            }
        }
        return archetypeTem;
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