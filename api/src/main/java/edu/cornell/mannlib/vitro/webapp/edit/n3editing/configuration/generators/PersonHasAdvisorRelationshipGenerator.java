/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.FirstAndLastNameValidator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeIntervalValidationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ChildVClassesWithParent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaVClassOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

public class PersonHasAdvisorRelationshipGenerator extends VivoBaseGenerator implements
        EditConfigurationGenerator {

    final static String advisingRelClass = vivoCore + "AdvisingRelationship";
    final static String subjAreaClass = "http://www.w3.org/2004/02/skos/core#Concept";
    final static String degreeClass = vivoCore+"AcademicDegree";
    final static String adviseeClass = foaf + "Person";
    final static String adviseeRoleClass = "http://vivoweb.org/ontology/core#AdviseeRole";
    final static String advisorRoleClass = "http://vivoweb.org/ontology/core#AdvisorRole";
    final static String advisingRelToInterval = vivoCore + "dateTimeInterval";
    final static String intervalType = vivoCore + "DateTimeInterval";
    final static String intervalToStart = vivoCore + "start";
    final static String intervalToEnd = vivoCore + "end";
    final static String dateTimeValueType = vivoCore + "DateTimeValue";
    final static String dateTimeValue = vivoCore + "dateTime";
    final static String dateTimePrecision = vivoCore + "dateTimePrecision";

    public PersonHasAdvisorRelationshipGenerator() {}

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) throws Exception {

        EditConfigurationVTwo conf = new EditConfigurationVTwo();

        initBasics(conf, vreq);
        initPropertyParameters(vreq, session, conf);
        initObjectPropForm(conf, vreq);

        conf.setTemplate("personHasAdvisorRelationship.ftl");

        conf.setVarNameForSubject("person");
        conf.setVarNameForPredicate("predicate");
        conf.setVarNameForObject("advisorRole");

        conf.setN3Required( Arrays.asList( n3ForNewAdvisingRelationship,
                                           advisingRelLabelAssertion,
                                           advisingRelTypeAssertion ) );
        conf.setN3Optional( Arrays.asList( n3ForNewAdviseeAssertion,
                                           n3ForExistingAdviseeAssertion,
                                           degreeAssertion,
                                           firstNameAssertion,
                                           lastNameAssertion,
                                           n3ForExistingSubjAreaAssertion, //relationship to existing subject area
                                           n3ForNewSubjAreaAssertion, //this will include all the new information that needs to be captured
                                           n3ForStart,
                                           n3ForEnd ) );

        conf.addNewResource("advisingRelationship", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("newAdvisee", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("vcardAdvisee", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("vcardName", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("advisorRole", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("adviseeRole", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("newSubjArea", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("intervalNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("startNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("endNode", DEFAULT_NS_FOR_NEW_RESOURCE);

        //uris in scope: none
        //literals in scope: none

        conf.setUrisOnform(Arrays.asList("advisingRelType", "existingSubjArea", "degree", "existingAdvisee"));
        conf.setLiteralsOnForm(Arrays.asList("advisingRelLabel", "subjAreaLabel", "adviseeLabel", "firstName", "lastName", "subjAreaLabelDisplay", "adviseeLabelDisplay" ));

        conf.addSparqlForExistingLiteral("advisingRelLabel", advisingRelLabelQuery);
        conf.addSparqlForExistingLiteral("adviseeLabel", adviseeLabelQuery);
        conf.addSparqlForExistingLiteral("subjAreaLabel", subjAreaLabelQuery);
        conf.addSparqlForExistingLiteral("startField-value", existingStartDateQuery);
        conf.addSparqlForExistingLiteral("endField-value", existingEndDateQuery);

        conf.addSparqlForExistingUris("advisingRelType", advisingRelTypeQuery);
        conf.addSparqlForExistingUris("advisingRelationship", existingAdvisingRelQuery);
        conf.addSparqlForExistingUris("adviseeRole", existingAdviseeRoleQuery);
        conf.addSparqlForExistingUris("existingSubjArea", subjAreaQuery);
        conf.addSparqlForExistingUris("existingAdvisee", adviseeQuery);
        conf.addSparqlForExistingUris("degree", degreeQuery);
        conf.addSparqlForExistingUris("intervalNode",existingIntervalNodeQuery);
        conf.addSparqlForExistingUris("startNode", existingStartNodeQuery);
        conf.addSparqlForExistingUris("endNode", existingEndNodeQuery);
        conf.addSparqlForExistingUris("startField-precision", existingStartPrecisionQuery);
        conf.addSparqlForExistingUris("endField-precision", existingEndPrecisionQuery);

        conf.addField( new FieldVTwo().
                setName("advisingRelType").
                setValidators( list("nonempty") ).
                setOptions( new ChildVClassesWithParent(advisingRelClass))
                );

        conf.addField( new FieldVTwo().
                setName("advisingRelLabel").
                setRangeDatatypeUri(RDF.dtLangString.getURI() ).
                setValidators( list("datatype:" + RDF.dtLangString.getURI()) )
                );

        conf.addField( new FieldVTwo().
                setName("firstName").
                setRangeDatatypeUri(RDF.dtLangString.getURI() ).
                setValidators( list("datatype:" + RDF.dtLangString.getURI()) )
                );

        conf.addField( new FieldVTwo().
                setName("lastName").
                setRangeDatatypeUri(RDF.dtLangString.getURI() ).
                setValidators( list("datatype:" + RDF.dtLangString.getURI()) )
                );

        conf.addField( new FieldVTwo(). // options set by auto complete JS
                setName("existingSubjArea")
                );

        conf.addField( new FieldVTwo().
                setName("subjAreaLabel").
                setRangeDatatypeUri(RDF.dtLangString.getURI() ).
                setValidators( list("datatype:" + RDF.dtLangString.getURI()) )
                );

        conf.addField( new FieldVTwo().
                setName("degree").
                setOptions(
                    new IndividualsViaVClassOptions(degreeClass))
                );

        conf.addField( new FieldVTwo(). // options set by auto complete JS
                setName("existingAdvisee")
        );

        conf.addField( new FieldVTwo().
                setName("adviseeLabel").
                setRangeDatatypeUri(RDF.dtLangString.getURI() ).
                setValidators( list("datatype:" + RDF.dtLangString.getURI()) )
                );

        conf.addField( new FieldVTwo().
                setName("subjAreaLabelDisplay").
                setRangeDatatypeUri(XSD.xstring.toString() ).
                setValidators( list("datatype:" + XSD.xstring.toString()) )
                );

        conf.addField( new FieldVTwo().
                setName("adviseeLabelDisplay").
                setRangeDatatypeUri(XSD.xstring.toString() ).
                setValidators( list("datatype:" + XSD.xstring.toString()) )
                );

        conf.addField( new FieldVTwo().setName("startField").
                setEditElement(
                        new DateTimeWithPrecisionVTwo(null,
                                VitroVocabulary.Precision.YEAR.uri(),
                                VitroVocabulary.Precision.NONE.uri())
                                )
                );

        conf.addField( new FieldVTwo().setName("endField").
                setEditElement(
                        new DateTimeWithPrecisionVTwo(null,
                                VitroVocabulary.Precision.YEAR.uri(),
                                VitroVocabulary.Precision.NONE.uri())
                                )
                );

        conf.addValidator(new DateTimeIntervalValidationVTwo("startField","endField"));
        conf.addValidator(new AntiXssValidation());
        conf.addValidator(new FirstAndLastNameValidator("existingAdvisee", I18n.bundle(vreq)));
        addFormSpecificData(conf, vreq);

        prepare(vreq, conf);
        return conf;
    }

    /* N3 assertions  */

    final static String n3ForNewAdvisingRelationship =
        "@prefix vivo: <" + vivoCore + "> . \n\n" +
        "?person <http://vivoweb.org/ontology/core#relatedBy>  ?advisingRelationship . \n" +
        "?advisingRelationship a  <" + advisingRelClass + "> . \n" +
        "?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?person . \n" +
        "?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "?advisorRole a  <" + advisorRoleClass + "> . \n" +
        "?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        "?person <http://purl.obolibrary.org/obo/RO_0000053>  ?advisorRole . \n" +
        "?advisorRole <http://purl.obolibrary.org/obo/RO_0000052>  ?person . ";

    final static String advisingRelLabelAssertion  =
        "?advisingRelationship <"+ label + "> ?advisingRelLabel .";

    final static String advisingRelTypeAssertion  =
        "?advisingRelationship a ?advisingRelType .";

    final static String n3ForNewAdviseeAssertion  =
        "?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?newAdvisee . \n" +
        "?newAdvisee <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        "?newAdvisee <" + label + "> ?adviseeLabel . \n" +
        "?newAdvisee a <" + adviseeClass + ">  . \n" +
        "?newAdvisee <http://purl.obolibrary.org/obo/RO_0000053>  ?adviseeRole . \n" +
        "?adviseeRole <http://purl.obolibrary.org/obo/RO_0000052>  ?newAdvisee . \n" +
        "?adviseeRole a  <" + adviseeRoleClass + "> . \n" +
        "?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?adviseeRole . \n" +
        "?adviseeRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . ";

    final static String n3ForExistingAdviseeAssertion  =
        "?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?existingAdvisee . \n" +
        "?existingAdvisee <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        "?existingAdvisee <http://purl.obolibrary.org/obo/RO_0000053>  ?adviseeRole . \n" +
        "?adviseeRole <http://purl.obolibrary.org/obo/RO_0000052>  ?existingAdvisee . \n" +
        "?adviseeRole a  <" + adviseeRoleClass + "> . \n" +
        "?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?adviseeRole . \n" +
        "?adviseeRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . ";

    final static String firstNameAssertion  =
        "@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .  \n" +
        "?newAdvisee <http://purl.obolibrary.org/obo/ARG_2000028>  ?vcardAdvisee . \n" +
        "?vcardAdvisee <http://purl.obolibrary.org/obo/ARG_2000029>  ?newAdvisee . \n" +
        "?vcardAdvisee a <http://www.w3.org/2006/vcard/ns#Individual> . \n" +
        "?vcardAdvisee vcard:hasName  ?vcardName . \n" +
        "?vcardName a <http://www.w3.org/2006/vcard/ns#Name> . \n" +
        "?vcardName vcard:givenName ?firstName .";

    final static String lastNameAssertion  =
        "@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .  \n" +
        "?newAdvisee <http://purl.obolibrary.org/obo/ARG_2000028>  ?vcardAdvisee . \n" +
        "?vcardAdvisee <http://purl.obolibrary.org/obo/ARG_2000029>  ?newAdvisee . \n" +
        "?vcardAdvisee a <http://www.w3.org/2006/vcard/ns#Individual> . \n" +
        "?vcardAdvisee vcard:hasName  ?vcardName . \n" +
        "?vcardName a <http://www.w3.org/2006/vcard/ns#Name> . \n" +
        "?vcardName vcard:familyName ?lastName .";

    final static String degreeAssertion  =
        "?advisingRelationship <http://vivoweb.org/ontology/core#degreeCandidacy> ?degree . \n" +
        " ";

    //This is for an existing subject area
    //Where we only need the existing subject area label
    final static String n3ForExistingSubjAreaAssertion  =
        "?advisingRelationship <http://vivoweb.org/ontology/core#hasSubjectArea> ?existingSubjArea . \n" +
        "?existingSubjArea <http://vivoweb.org/ontology/core#subjectAreaOf> ?advisingRelationship";
    //For new subject area, we include all new information
    //new subject area should always be a new resource
    //and the following should only get evaluated
    //when there is something in the label

    final static String n3ForNewSubjAreaAssertion  =
    	"?advisingRelationship <http://vivoweb.org/ontology/core#hasSubjectArea> ?newSubjArea . \n" +
	    "?newSubjArea <http://vivoweb.org/ontology/core#subjectAreaOf> ?advisingRelationship . \n" +
        "?newSubjArea <"+ label + "> ?subjAreaLabel . \n" +
        "?newSubjArea a <" + subjAreaClass + "> . ";

    final static String n3ForStart =
        "?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToStart + "> ?startNode . \n" +
        "?startNode a <" + dateTimeValueType + "> . \n" +
        "?startNode  <" + dateTimeValue + "> ?startField-value . \n" +
        "?startNode  <" + dateTimePrecision + "> ?startField-precision . \n";

    final static String n3ForEnd =
        "?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToEnd + "> ?endNode . \n" +
        "?endNode a <" + dateTimeValueType + "> . \n" +
        "?endNode  <" + dateTimeValue + "> ?endField-value . \n" +
        "?endNode  <" + dateTimePrecision + "> ?endField-precision . \n";

    /* Queries for editing an existing entry */

    final static String existingAdvisingRelQuery  =
        "SELECT ?advisingRelationship WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "}";

    final static String advisingRelTypeQuery =
    	"PREFIX vitro: <" + VitroVocabulary.vitroURI + "> \n" +
        "SELECT ?advisingRelType WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship vitro:mostSpecificType ?advisingRelType . \n" +
        "}";

    final static String advisingRelLabelQuery =
        "SELECT ?existingAdvisingRelLabel WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship <"  + label + "> ?existingAdvisingRelLabel . \n" +
        "}";

    final static String adviseeQuery  =
        "SELECT ?existingAdvisee WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?existingAdvisee . \n" +
        " ?existingAdvisee <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?existingAdvisee a <" + adviseeClass + ">  . \n" +
        " ?existingAdvisee <http://purl.obolibrary.org/obo/RO_0000053> ?existingAdviseeRole . \n" +
		" ?existingAdviseeRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?existingAdviseeRole a <" + adviseeRoleClass + ">  . \n" +
        "}";

    final static String adviseeLabelQuery =
        "SELECT ?existingAdviseeLabel WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?existingAdvisee . \n" +
        " ?existingAdvisee <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?existingAdvisee a <" + adviseeClass + ">  . \n" +
        " ?existingAdvisee <"  + label + "> ?existingAdviseeLabel . \n" +
        " ?existingAdvisee <http://purl.obolibrary.org/obo/RO_0000053> ?existingAdviseeRole . \n" +
		" ?existingAdviseeRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?existingAdviseeRole a <" + adviseeRoleClass + ">  . \n" +
        "}";

    final static String existingAdviseeRoleQuery  =
        "SELECT ?existingAdviseeRole WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?existingAdviseeRole . \n" +
        " ?existingAdviseeRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?existingAdviseeRole a  <" + adviseeRoleClass + "> . \n" +
        "}";

    final static String subjAreaQuery =
        "SELECT ?existingSubjArea WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#hasSubjectArea> ?existingSubjArea . \n" +
        " ?existingSubjArea a <http://www.w3.org/2004/02/skos/core#Concept>  . \n" +
        " ?existingSubjArea <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> ?type \n" +
        "}";

    final static String subjAreaLabelQuery  =
        "SELECT ?existingSubjAreaLabel WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#hasSubjectArea> ?existingSubjArea . \n" +
        " ?existingSubjArea a <http://www.w3.org/2004/02/skos/core#Concept>  . \n" +
        " ?existingSubjArea <" + label + "> ?existingSubjAreaLabel . \n" +
        " ?existingSubjArea <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> ?type \n" +
        "}";

    final static String degreeQuery  =
        "SELECT ?existingDegree WHERE {\n"+
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#degreeCandidacy> ?existingDegree . \n" +
        " ?existingDegree a  <" + degreeClass + "> . \n" +
        "}";

    final static String existingStartDateQuery =
        "SELECT ?existingDateStart WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "  ?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "  ?intervalNode a <" + intervalType + "> . \n" +
        "  ?intervalNode <" + intervalToStart + "> ?startNode . \n" +
        "  ?startNode a <" + dateTimeValueType +"> . \n" +
        "  ?startNode <" + dateTimeValue + "> ?existingDateStart . }";

    final static String existingEndDateQuery =
        "SELECT ?existingEndDate WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "  ?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "  ?intervalNode a <" + intervalType + "> . \n " +
        "  ?intervalNode <" + intervalToEnd + "> ?endNode . \n" +
        "  ?endNode a <" + dateTimeValueType + "> . \n" +
        "  ?endNode <" + dateTimeValue + "> ?existingEndDate . }";

    final static String existingIntervalNodeQuery =
        "SELECT ?existingIntervalNode WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "  ?advisingRelationship <" + advisingRelToInterval + "> ?existingIntervalNode . \n" +
        "  ?existingIntervalNode a <" + intervalType + "> . }";

    final static String existingStartNodeQuery =
        "SELECT ?existingStartNode WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "  ?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "  ?intervalNode a <" + intervalType + "> . \n" +
        "  ?intervalNode <" + intervalToStart + "> ?existingStartNode . \n" +
        "  ?existingStartNode a <" + dateTimeValueType + "> .}   ";

    final static String existingEndNodeQuery =
        "SELECT ?existingEndNode WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "  ?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "  ?intervalNode a <" + intervalType + "> . \n" +
        "  ?intervalNode <" + intervalToEnd + "> ?existingEndNode . \n" +
        "  ?existingEndNode a <" + dateTimeValueType + "> } ";

    final static String existingStartPrecisionQuery =
        "SELECT ?existingStartPrecision WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "  ?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "  ?intervalNode a <" + intervalType + "> . \n" +
        "  ?intervalNode <" + intervalToStart + "> ?startNode . \n" +
        "  ?startNode a  <" + dateTimeValueType + "> . \n" +
        "  ?startNode <" + dateTimePrecision + "> ?existingStartPrecision . }";

    final static String existingEndPrecisionQuery =
        "SELECT ?existingEndPrecision WHERE { \n" +
        " ?advisorRole <http://vivoweb.org/ontology/core#relatedBy> ?advisingRelationship . \n" +
        " ?advisingRelationship <http://vivoweb.org/ontology/core#relates> ?advisorRole . \n" +
        "  ?advisingRelationship <" + advisingRelToInterval + "> ?intervalNode . \n" +
        "  ?intervalNode a <" + intervalType + "> . \n" +
        "  ?intervalNode <" + intervalToEnd + "> ?endNode . \n" +
        "  ?endNode a <" + dateTimeValueType + "> . \n" +
        "  ?endNode <" + dateTimePrecision + "> ?existingEndPrecision . }";

	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		formSpecificData.put("sparqlForAcFilter", getSparqlForAcFilter(vreq));
		editConfiguration.setFormSpecificData(formSpecificData);
	}

	public String getSparqlForAcFilter(VitroRequest vreq) {
		String subject = EditConfigurationUtils.getSubjectUri(vreq);
		String predicate = EditConfigurationUtils.getPredicateUri(vreq);
		//Get all objects for existing predicate, filters out results from addition and edit
		String query =  "SELECT ?objectVar WHERE { " +
			"<" + subject + "> <" + predicate + "> ?objectVar .} ";
		return query;
	}

}
