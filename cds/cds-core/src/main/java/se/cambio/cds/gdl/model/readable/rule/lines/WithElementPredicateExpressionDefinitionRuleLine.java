package se.cambio.cds.gdl.model.readable.rule.lines;

import org.apache.log4j.Logger;
import se.cambio.cds.gdl.model.expression.BinaryExpression;
import se.cambio.cds.gdl.model.expression.ExpressionItem;
import se.cambio.cds.gdl.model.expression.OperatorKind;
import se.cambio.cds.gdl.model.expression.Variable;
import se.cambio.cds.gdl.model.readable.rule.lines.elements.*;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.ArchetypeReferenceRuleLine;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.DefinitionsRuleLine;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.PredicateRuleLine;
import se.cambio.cds.model.instance.ArchetypeReference;
import se.cambio.cm.model.archetype.vo.ArchetypeElementVO;
import se.cambio.openehr.util.OpenEHRLanguageManager;
import se.cambio.openehr.util.UserConfigurationManager;


public class WithElementPredicateExpressionDefinitionRuleLine extends ExpressionRuleLine implements ArchetypeReferenceRuleLine, DefinitionsRuleLine, PredicateRuleLine{

    private PredicateArchetypeElementAttributeRuleLineElement archetypeElementAttributeRuleLineDefinitionElement = null;
    private PredicateAttributeComparisonOperatorRuleLineElement comparisonOperatorRuleLineElement = null;
    private ExpressionRuleLineElement expressionRuleLineElement = null;

    private ArchetypeElementRuleLineDefinitionElement _archetypeElementRuleLineDefinitionElement = null;

    public WithElementPredicateExpressionDefinitionRuleLine(
            ArchetypeInstantiationRuleLine archetypeInstantiationRuleLine) {
        super(OpenEHRLanguageManager.getMessage("ElementPredicateExpression"),
                OpenEHRLanguageManager.getMessage("ElementPredicateExpressionDesc"));
        archetypeElementAttributeRuleLineDefinitionElement = new PredicateArchetypeElementAttributeRuleLineElement(this);
        comparisonOperatorRuleLineElement = new PredicateAttributeComparisonOperatorRuleLineElement(this);
        expressionRuleLineElement = new ExpressionRuleLineElement(this);

        if (archetypeInstantiationRuleLine!=null){
            archetypeInstantiationRuleLine.addChildRuleLine(this);
        }

        getRuleLineElements().add(new StaticTextRuleLineElement(OpenEHRLanguageManager.getMessage("WithElementRLE")));
        getRuleLineElements().add(archetypeElementAttributeRuleLineDefinitionElement);
        getRuleLineElements().add(comparisonOperatorRuleLineElement);
        getRuleLineElements().add(expressionRuleLineElement);
    }

    public PredicateArchetypeElementAttributeRuleLineElement getArchetypeElementAttributeRuleLineDefinitionElement() {
        return archetypeElementAttributeRuleLineDefinitionElement;
    }

    public PredicateAttributeComparisonOperatorRuleLineElement getComparisonOperatorRuleLineElement() {
        return comparisonOperatorRuleLineElement;
    }

    public ExpressionRuleLineElement getExpressionRuleLineElement(){
        return expressionRuleLineElement;
    }

    @Override
    public ArchetypeReference getArchetypeReference() {
        return getArchetypeInstantiationRuleLine().
                getArchetypeReferenceRuleLineDefinitionElement().getValue();
    }

    public ArchetypeInstantiationRuleLine getArchetypeInstantiationRuleLine() {
        return (ArchetypeInstantiationRuleLine)getParentRuleLine();
    }

    @Override
    public ExpressionItem toExpressionItem() {
        ArchetypeElementVO archetypeElementVO = getArchetypeElementAttributeRuleLineDefinitionElement().getValue();
        String attribute = getArchetypeElementAttributeRuleLineDefinitionElement().getAttribute();
        String path =  archetypeElementVO.getPath()+"/value/"+attribute;
        ExpressionRuleLineElement expressionRuleLineElement =
                getExpressionRuleLineElement();
        OperatorKind operatorKind =
                getComparisonOperatorRuleLineElement().getValue();
        String name = getArchetypeManager().getArchetypeElements().getText(archetypeElementVO, UserConfigurationManager.getLanguage());
        return new BinaryExpression(
                new Variable(null, name, path),
                expressionRuleLineElement.getValue(),
                operatorKind);
    }

    @Override
    public String getPredicateDescription() {
        StringBuffer sb = new StringBuffer();
        PredicateArchetypeElementAttributeRuleLineElement paearle = getArchetypeElementAttributeRuleLineDefinitionElement();
        PredicateAttributeComparisonOperatorRuleLineElement pacorl = getComparisonOperatorRuleLineElement();
        ExpressionRuleLineElement ere = getExpressionRuleLineElement();
        if (paearle!=null){
            ArchetypeElementVO archetypeElementVO = paearle.getValue();
            String attribute = paearle.getAttribute();
            if (archetypeElementVO!=null && pacorl.getValue()!=null){
                String name = getArchetypeManager().getArchetypeElements().getText(archetypeElementVO, UserConfigurationManager.getLanguage());
                sb.append(name+"."+attribute+" "+pacorl.getValue().getSymbol()+" "+ere.toString());
            }else{
                Logger.getLogger(ArchetypeReference.class).warn("Unknown predicate for AR '"+paearle.toString()+"'");
                sb.append("*UNKNOWN PREDICATE*");
            }
        }
        return sb.toString();
    }
}/*
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