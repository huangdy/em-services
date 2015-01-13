package com.leidos.xchangecore.core.em.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.leidos.xchangecore.core.em.dao.LEITSCIncidentDAO;
import com.leidos.xchangecore.core.em.model.LEITSCIncident;
import com.leidos.xchangecore.core.infrastructure.dao.AgreementDAO;
import com.leidos.xchangecore.core.infrastructure.model.Agreement;
import com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType;
import com.leidos.xchangecore.core.infrastructure.model.ShareRule;

/**
 * 
 * @author roger
 * 
 */
public class MyAgreementDAOTest
    extends AbstractJpaTests {

    private Logger log = LoggerFactory.getLogger(MyAgreementDAOTest.class);

    @Autowired
    AgreementDAO agreementDAO;

    final String LOCAL_CORE_ID = "localCoreID";
    final String REMOTE_CORE_ID = "remoteCoreID";
    final String RULEID_1 = "1";
    final String RULEID_2 = "2";
    final String RULE_PREFIX = "rule";
    final String IG_CODESPACE = "http://uicds.org/interestgroup#incident";
    final String IG_LABEL = "Incident Type";
    final String IG_VALUE = "traffic";
    final String IG_VALUE_2 = "env";
    final String WP_CODESPACE = "http://uicds.org/workproducts";
    final String WP_LABEL = "Work Product Type";
    final String WP_VALUE = "incident";
    final String WP2_VALUE = "ICS";

    @Override
    protected void onSetUpInTransaction() throws Exception {

    }

    /*
     * @Override protected String[] getConfigLocations() { return new String[] {
     * "classpath:contexts/applicationContext-infra.xml",
     * "classpath:contexts/test-dataSrcContext.xml" }; }
     */

    @Test
    public void testSimpleAgreement() {

        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);

        // System.out.println(AgreementUtil.copyProperties(agreement));

        log.debug("Retrieving simple Agreement");
        Agreement result2 = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertNotNull(result2);
        assertEquals(agreement, result2);

    }

    @Test
    public void testGetSetOneShareRule() {

        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);

        assertEquals(0, agreement.getShareRules().size());

        addShareRule(agreement, true, "", true);

        agreement = agreementDAO.makePersistent(agreement);

        Agreement result = agreementDAO.findByCoreID(REMOTE_CORE_ID);

        assertNotNull(result);
        assertEquals(agreement, result);
        assertNotNull(result.getShareRules());
        assertEquals(1, result.getShareRules().size());
        assertTrue(result.getShareRules().iterator().hasNext());

        ShareRule rule = result.getShareRules().iterator().next();
        assertTrue(rule.isEnabled());
        assertNotNull(rule.getInterestGroup());
        assertEquals(RULE_PREFIX, rule.getRuleID());
        assertEquals(2, rule.getWorkProducts().size());

        CodeSpaceValueType interestGroup = result.getShareRules().iterator().next().getInterestGroup();
        assertNotNull(interestGroup);
        assertEquals(IG_CODESPACE, interestGroup.getCodeSpace());
        assertEquals(IG_LABEL, interestGroup.getLabel());
        assertEquals(IG_VALUE, interestGroup.getValue());
    }

    @Test
    public void testAddAndRemoveOneShareRule() {

        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);

        assertEquals(0, agreement.getShareRules().size());

        addShareRule(agreement, true, "", true);

        agreement = agreementDAO.makePersistent(agreement);

        Agreement result = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertTrue(result.getShareRules().iterator().hasNext());

        Set<ShareRule> shareRules = result.getShareRules();
        shareRules.clear();
        agreement.setShareRules(shareRules);
        agreement = agreementDAO.makePersistent(agreement);
        result = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertEquals(0, result.getShareRules().size());
    }

    @Test
    public void testDisableOneShareRule() {

        // Create agreement add enabled share rule and save
        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);
        addShareRule(agreement, true, "", true);
        agreement = agreementDAO.makePersistent(agreement);
        Agreement result = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertTrue(result.getShareRules().iterator().next().isEnabled());

        // Disable share rule and make sure it is disabled.
        disableFirstShareRule(result, false);

        agreement = agreementDAO.makePersistent(agreement);
        result = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertNotNull(result);
        assertEquals(agreement, result);
        assertNotNull(result.getShareRules());
        assertEquals(1, result.getShareRules().size());
        assertTrue(result.getShareRules().iterator().hasNext());
        assertFalse(result.getShareRules().iterator().next().isEnabled());

    }

    @Test
    public void testAddAWorkProduct() {

        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);

        assertEquals(0, agreement.getShareRules().size());

        addShareRule(agreement, true, "", true);

        agreement = agreementDAO.makePersistent(agreement);

        Agreement result = agreementDAO.findByCoreID(REMOTE_CORE_ID);

        // check start with 2 work products
        Set<ShareRule> shareRules = agreement.getShareRules();
        ShareRule rule = shareRules.iterator().next();
        assertEquals(2, rule.getWorkProducts().size());

        // Get the current work product set
        Set<CodeSpaceValueType> workProducts = new HashSet<CodeSpaceValueType>(rule.getWorkProducts());
        CodeSpaceValueType newWorkProduct = new CodeSpaceValueType();
        newWorkProduct.setCodeSpace(WP_CODESPACE);
        newWorkProduct.setLabel(WP_LABEL);
        newWorkProduct.setValue(WP_VALUE + "ANOTHER");
        assertEquals(2, workProducts.size());
        workProducts.add(newWorkProduct);
        assertEquals(3, workProducts.size());
        rule.setWorkProducts(workProducts);
        // agreement.setShareRules(shareRules);
        assertEquals(3, workProducts.size());
        agreement = agreementDAO.makePersistent(agreement);

        result = agreementDAO.findByCoreID(REMOTE_CORE_ID);

        rule = result.getShareRules().iterator().next();
        assertTrue(rule.isEnabled());
        assertNotNull(rule.getInterestGroup());
        assertEquals(RULE_PREFIX, rule.getRuleID());
        assertEquals(3, rule.getWorkProducts().size());
    }

    @Test
    public void testRemoveAWorkProduct() {

        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);

        assertEquals(0, agreement.getShareRules().size());

        addShareRule(agreement, true, "", true);

        agreement = agreementDAO.makePersistent(agreement);

        Agreement result = agreementDAO.findByCoreID(REMOTE_CORE_ID);

        // remove a work product
        ShareRule rule = agreement.getShareRules().iterator().next();
        assertEquals(2, rule.getWorkProducts().size());
        Set<CodeSpaceValueType> workProducts = rule.getWorkProducts();
        workProducts.remove(workProducts.iterator().next());
        agreement = agreementDAO.makePersistent(agreement);

        result = agreementDAO.findByCoreID(REMOTE_CORE_ID);

        rule = result.getShareRules().iterator().next();
        assertTrue(rule.isEnabled());
        assertNotNull(rule.getInterestGroup());
        assertEquals(RULE_PREFIX, rule.getRuleID());
        assertEquals(1, rule.getWorkProducts().size());
    }

    @Test
    public void testGetSetMultipleShareRules() {

        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);

        assertEquals(0, agreement.getShareRules().size());

        addShareRule(agreement, true, "", true);
        addShareRule(agreement, false, "1", true);
        addShareRule(agreement, true, "2", false);
        // System.out.println(AgreementUtil.copyProperties(agreement));

        agreement = agreementDAO.makePersistent(agreement);

        Agreement result = agreementDAO.findByCoreID(REMOTE_CORE_ID);

        assertNotNull(result);
        assertEquals(agreement, result);
        assertNotNull(result.getShareRules());
        assertEquals(3, result.getShareRules().size());
        int enabledRuleCount = 0;
        for (ShareRule rule : result.getShareRules()) {
            if (rule.isEnabled())
                enabledRuleCount++;
            if (rule.getRuleID().equals(RULE_PREFIX + "2")) {
                assertNotNull(rule.getWorkProducts());
                assertEquals(0, rule.getWorkProducts().size());
            }
        }
        assertEquals(2, enabledRuleCount);
    }

    @Test
    public void testRemoveMultipleShareRules() {

        Agreement agreement = createAndPersistSimpleAgreement(REMOTE_CORE_ID);

        assertEquals(0, agreement.getShareRules().size());

        addShareRule(agreement, true, "", false);
        addShareRule(agreement, false, "1", true);
        agreement = agreementDAO.makePersistent(agreement);
        Agreement result = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertNotNull(result);
        assertEquals(2, result.getShareRules().size());

        addShareRule(agreement, true, "2", true);
        agreement = agreementDAO.makePersistent(agreement);
        result = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertNotNull(result);
        assertEquals(3, result.getShareRules().size());

        agreement.getShareRules().remove(agreement.getShareRules().iterator().next());
        agreement = agreementDAO.makePersistent(agreement);
        result = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertNotNull(result);
        assertEquals(2, result.getShareRules().size());

        HashSet<ShareRule> emptyShareRules = new HashSet<ShareRule>();
        agreement.setShareRules(emptyShareRules);
        agreement = agreementDAO.makePersistent(agreement);

        result = agreementDAO.findByCoreID(REMOTE_CORE_ID);

        assertNotNull(result);
        assertEquals(0, result.getShareRules().size());
    }

    @Test
    public void testGetSetMultipleAgreements() {

        Agreement agreement1 = createAndPersistSimpleAgreement(REMOTE_CORE_ID);
        addShareRule(agreement1, true, "", false);
        addShareRule(agreement1, true, "1", true);
        agreement1 = agreementDAO.makePersistent(agreement1);

        Agreement agreement2 = createAndPersistSimpleAgreement(REMOTE_CORE_ID + "1");
        addShareRule(agreement2, true, "", true);
        addShareRule(agreement2, false, "1", true);
        addShareRule(agreement2, false, "2", false);
        agreement2 = agreementDAO.makePersistent(agreement2);

        Agreement agreement3 = createAndPersistSimpleAgreement(REMOTE_CORE_ID + "2");
        agreement3 = agreementDAO.makePersistent(agreement3);

        Agreement result2 = agreementDAO.findByCoreID(REMOTE_CORE_ID + "1");

        assertNotNull(result2);
        assertEquals(agreement2, result2);
        assertNotNull(result2.getShareRules());
        assertEquals(3, result2.getShareRules().size());
        int enabledRuleCount = 0;
        for (ShareRule rule : result2.getShareRules()) {
            if (rule.isEnabled())
                enabledRuleCount++;
        }
        assertEquals(1, enabledRuleCount);

    }

    @Test
    public void testModifiyMultipleAgreements() {

        Agreement agreement1 = createAndPersistSimpleAgreement(REMOTE_CORE_ID);
        addShareRule(agreement1, true, "", true);
        addShareRule(agreement1, true, "1", true);
        agreement1 = agreementDAO.makePersistent(agreement1);

        Agreement agreement2 = createAndPersistSimpleAgreement(REMOTE_CORE_ID + "1");
        addShareRule(agreement2, true, "", true);
        addShareRule(agreement2, true, "1", true);
        agreement2 = agreementDAO.makePersistent(agreement2);

        Agreement agreement3 = createAndPersistSimpleAgreement(REMOTE_CORE_ID + "2");
        agreement3 = agreementDAO.makePersistent(agreement3);
        addShareRule(agreement3, true, "", true);
        addShareRule(agreement3, true, "1", true);

        Agreement result2 = agreementDAO.findByCoreID(REMOTE_CORE_ID + "1");
        Set<ShareRule> shareRules = result2.getShareRules();
        shareRules.clear();
        result2.setShareRules(shareRules);
        result2 = agreementDAO.makePersistent(result2);

        Agreement result3 = agreementDAO.findByCoreID(REMOTE_CORE_ID + "1");

        // agreement 2 should now have no rules
        assertNotNull(result3);
        assertEquals(result2, result3);
        assertNotNull(result2.getShareRules());
        assertEquals(0, result2.getShareRules().size());

        // agreement 1 should still have 2 rules
        Agreement result4 = agreementDAO.findByCoreID(REMOTE_CORE_ID);
        assertNotNull(result4);
        assertEquals(agreement1, result4);
        assertNotNull(result4.getShareRules());
        assertEquals(2, result4.getShareRules().size());

        // agreement 1 should still have 2 rules
        Agreement result5 = agreementDAO.findByCoreID(REMOTE_CORE_ID + "2");
        assertNotNull(result5);
        assertEquals(agreement3, result5);
        assertNotNull(result5.getShareRules());
        assertEquals(2, result5.getShareRules().size());

    }

    private void addShareRule(Agreement agreement,
                              boolean enabled,
                              String valueSuffix,
                              boolean addWorkProducts) {

        ShareRule rule = new ShareRule();

        rule.setEnabled(enabled);
        rule.setRuleID(RULE_PREFIX + valueSuffix);
        CodeSpaceValueType interestGroup = new CodeSpaceValueType();
        interestGroup.setCodeSpace(IG_CODESPACE);
        interestGroup.setLabel(IG_LABEL);
        interestGroup.setValue(IG_VALUE + valueSuffix);
        rule.setInterestGroup(interestGroup);

        if (addWorkProducts) {
            HashSet<CodeSpaceValueType> workProducts = new HashSet<CodeSpaceValueType>();
            CodeSpaceValueType wp1 = new CodeSpaceValueType();
            wp1.setCodeSpace(WP_CODESPACE);
            wp1.setLabel(WP_LABEL);
            wp1.setValue(WP_VALUE);
            workProducts.add(wp1);

            CodeSpaceValueType wp2 = new CodeSpaceValueType();
            wp2.setCodeSpace(WP_CODESPACE);
            wp2.setLabel(WP_LABEL);
            wp2.setValue(WP_VALUE + "1");
            workProducts.add(wp2);

            rule.setWorkProducts(workProducts);
        }

        Set<ShareRule> shareRules = null;
        if (agreement.getShareRules().size() == 0) {
            shareRules = new HashSet<ShareRule>();
        } else {
            shareRules = new HashSet<ShareRule>(agreement.getShareRules());
        }
        shareRules.add(rule);
        agreement.setShareRules(shareRules);
    }

    private void disableFirstShareRule(Agreement agreement, boolean enabled) {

        ShareRule rule = agreement.getShareRules().iterator().next();
        rule.setEnabled(enabled);
    }

    // private Agreement createAndPersistOneRuleAgreement() {
    // log.debug("Creating sample Agreement");
    // Agreement agreement = buildSimpleAgreement();
    // log.debug(agreement.toString());
    // assertNotNull(agreement);
    //        
    //        
    // ShareRule[] shareRules = new ShareRule[1];
    //          
    // ShareRule s1 = new ShareRule();
    // s1.setRuleID("1");
    // s1.setEnabled(true);
    // CodeSpaceValueType ig1 = new CodeSpaceValueType();
    // ig1.setCodeSpace(IG_CODESPACE);
    // ig1.setLabel(IG_LABEL);
    // ig1.setValue(IG_VALUE);
    // s1.setInterestGroup(ig1);
    // shareRules[0] = s1;
    // agreement.setShareRules(shareRules);
    //        
    // // System.out.println(AgreementUtil.copyProperties(agreement));
    //        
    // log.debug("Persisting sample Agreement");
    // Agreement result = agreementDAO.makePersistent(agreement);
    // assertNotNull(result);
    // assertEquals(agreement, result);
    //        
    // return agreement;
    // }
    //  
    // private Agreement createAndPersistAgreement(String coreSuffix) {
    // log.debug("Creating sample Agreement");
    // Agreement agreement = buildSampleAgreement(coreSuffix);
    // log.debug(agreement.toString());
    // assertNotNull(agreement);
    //
    // log.debug("Persisting sample Agreement");
    // Agreement result = agreementDAO.makePersistent(agreement);
    // assertNotNull(result);
    // assertEquals(agreement, result);
    //        
    // Agreement a = agreementDAO.findByCoreID(agreement.getRemoteCore().getValue());
    // assertNotNull(a);
    //        
    // return agreement;
    // }

    // @Test
    // public void testUpdateSimpleAgreement() {
    //
    // Agreement agreement = createAndPersistSimpleAgreement();
    //        
    // // update agreement
    // ShareRule[] rules = agreement.getShareRules();
    // int length = 1;
    // if (rules != null) {
    // length = agreement.getShareRules().length + 1;
    // }
    // ShareRule[] newRules = new ShareRule[length];
    //        
    // if (rules != null) {
    // for (int i = 0; i < length; i++) {
    // newRules[i] = rules[i];
    // }
    // }
    // newRules[length-1] = new ShareRule();
    // newRules[length-1].setEnabled(true);
    // newRules[length-1].setRuleID(RULEID_1);
    // CodeSpaceValueType interestGroup = new CodeSpaceValueType();
    // interestGroup.setCodeSpace(IG_CODESPACE);
    // interestGroup.setLabel(IG_LABEL);
    // interestGroup.setValue(IG_VALUE);
    // newRules[length-1].setInterestGroup(interestGroup);
    //        
    // agreement.setShareRules(newRules);
    //        
    // Agreement result3 = agreementDAO.makePersistent(agreement);
    // assertNotNull(result3);
    // assertEquals(agreement, result3);
    // assertNotNull(result3.getShareRules());
    // assertEquals(1,result3.getShareRules().length);
    //        
    // // System.out.println(AgreementUtil.copyProperties(result3));
    //        
    // Agreement a = agreementDAO.findByCoreID(agreement.getRemoteCore().getValue());
    // assertNotNull(a);
    //        
    // List<Agreement> list = agreementDAO.findAll();
    // assertEquals(1,list.size());
    // }
    //    
    // @Test
    // public void testFindByEntityId() {
    //
    //
    // Agreement agreement = createAndPersistAgreement(null);
    // // System.out.println(AgreementUtil.copyProperties(agreement));
    //
    // log.debug("Retrieving sample Agreement");
    // Agreement result2 = agreementDAO.findByCoreID(REMOTE_CORE_ID);
    // assertNotNull(result2);
    // assertEquals(agreement,result2);
    // // System.out.println(AgreementUtil.copyProperties(result2));
    //        
    // assertEquals(2,result2.getShareRules().length);
    // ShareRule[] rules = result2.getShareRules();
    // boolean found = false;
    //        
    // // check a share rule
    // for (ShareRule rule : rules) {
    // if (rule.getRuleID().equals(RULEID_1)) {
    // found = true;
    // assertNotNull(rule.getInterestGroup());
    // CodeSpaceValueType ig1 = rule.getInterestGroup();
    // assertNotNull(ig1.getCodeSpace());
    // assertEquals(IG_CODESPACE,ig1.getCodeSpace());
    // assertNotNull(ig1.getLabel());
    // assertEquals(IG_LABEL,ig1.getLabel());
    // assertEquals(IG_VALUE,ig1.getValue());
    //                  
    // CodeSpaceValueType[] wps = rule.getWorkProductTypes();
    // assertEquals(2,wps.length);
    // assertEquals(WP_CODESPACE,wps[0].getCodeSpace());
    // assertEquals(WP_LABEL,wps[1].getLabel());
    // assertEquals(WP_VALUE, wps[1].getValue());
    // }
    // }
    // assertTrue(found);
    // }
    //    
    // @Test
    // public void testFindEntityWithOneRule() {
    // Agreement agreement = createAndPersistOneRuleAgreement();
    // assertNotNull(agreement);
    // }
    //    
    // @Test
    // public void testThreeAgreements() {
    // Agreement agreement1 = createAndPersistAgreement("1");
    // Agreement agreement2 = createAndPersistAgreement("2");
    // Agreement agreement3 = createAndPersistAgreement("3");
    //          
    // List<Agreement> list = agreementDAO.findAll();
    // assertNotNull(list);
    // assertEquals(3,list.size());
    //          
    // Agreement a = agreementDAO.findByCoreID(REMOTE_CORE_ID + "2");
    // assertNotNull(a);
    // // System.out.println(a.getRemoteCore().getValue());
    // assertEquals(a.getRemoteCore().getValue(),REMOTE_CORE_ID + "2");
    // }
    //
    // @Test
    // public void testUpdateAgreement() {
    //
    //
    // Agreement agreement = createAndPersistAgreement(null);
    //
    // log.debug("Retrieving sample Agreement");
    // Agreement result2 = agreementDAO.findByCoreID(REMOTE_CORE_ID);
    // assertNotNull(result2);
    // assertEquals(agreement,result2);
    //        
    // // update agreement
    // ShareRule[] rules = result2.getShareRules();
    // ShareRule[] newRules = new ShareRule[rules.length + 1];
    // for (int i = 0; i < rules.length; i++) {
    // newRules[i] = rules[i];
    // }
    // newRules[rules.length] = new ShareRule();
    // newRules[rules.length].setEnabled(true);
    // newRules[rules.length].setRuleID(RULEID_1);
    // CodeSpaceValueType interestGroup = new CodeSpaceValueType();
    // interestGroup.setCodeSpace(IG_CODESPACE);
    // interestGroup.setLabel(IG_LABEL);
    // interestGroup.setValue(IG_VALUE);
    // newRules[rules.length].setInterestGroup(interestGroup);
    //       
    // result2.setShareRules(newRules);
    //        
    // Agreement result3 = agreementDAO.makePersistent(result2);
    // assertNotNull(result3);
    // assertEquals(result2, result3);
    // assertEquals(result2.getShareRules().length,result3.getShareRules().length);
    //        
    // Agreement a = agreementDAO.findByCoreID(agreement.getRemoteCore().getValue());
    // assertNotNull(a);
    // }

    private Agreement createAndPersistSimpleAgreement(String remoteCoreID) {

        log.debug("Creating sample Agreement");
        Agreement agreement = buildSimpleAgreement(remoteCoreID);
        log.debug(agreement.toString());
        assertNotNull(agreement);

        log.debug("Persisting sample Agreement");
        Agreement result = agreementDAO.makePersistent(agreement);
        assertNotNull(result);
        assertEquals(agreement, result);

        Agreement a = agreementDAO.findByCoreID(agreement.getRemoteCore().getValue());
        assertNotNull(a);

        return agreement;
    }

    // private Agreement createAndPersistOneRuleAgreement() {
    // log.debug("Creating sample Agreement");
    // Agreement agreement = buildSimpleAgreement();
    // log.debug(agreement.toString());
    // assertNotNull(agreement);
    //        
    //        
    // ShareRule[] shareRules = new ShareRule[1];
    //          
    // ShareRule s1 = new ShareRule();
    // s1.setRuleID("1");
    // s1.setEnabled(true);
    // CodeSpaceValueType ig1 = new CodeSpaceValueType();
    // ig1.setCodeSpace(IG_CODESPACE);
    // ig1.setLabel(IG_LABEL);
    // ig1.setValue(IG_VALUE);
    // s1.setInterestGroup(ig1);
    // shareRules[0] = s1;
    // agreement.setShareRules(shareRules);
    //        
    // // System.out.println(AgreementUtil.copyProperties(agreement));
    //        
    // log.debug("Persisting sample Agreement");
    // Agreement result = agreementDAO.makePersistent(agreement);
    // assertNotNull(result);
    // assertEquals(agreement, result);
    //        
    // return agreement;
    // }
    //  
    // private Agreement createAndPersistAgreement(String coreSuffix) {
    // log.debug("Creating sample Agreement");
    // Agreement agreement = buildSampleAgreement(coreSuffix);
    // log.debug(agreement.toString());
    // assertNotNull(agreement);
    //
    // log.debug("Persisting sample Agreement");
    // Agreement result = agreementDAO.makePersistent(agreement);
    // assertNotNull(result);
    // assertEquals(agreement, result);
    //        
    // Agreement a = agreementDAO.findByCoreID(agreement.getRemoteCore().getValue());
    // assertNotNull(a);
    //        
    // return agreement;
    // }

    private Agreement buildSimpleAgreement(String remoteCoreID) {

        Agreement agreement = new Agreement();

        CodeSpaceValueType localCore = new CodeSpaceValueType();
        localCore.setValue(LOCAL_CORE_ID);

        CodeSpaceValueType remoteCore = new CodeSpaceValueType();
        remoteCore.setValue(remoteCoreID);

        agreement.setLocalCore(localCore);
        agreement.setRemoteCore(remoteCore);

        agreement.setEnabled(true);

        return agreement;
    }

    @Autowired
    LEITSCIncidentDAO leitscIncidentDAO;

    @Test
    public void testLEITSCIncidentDAOCreation() {

        assertNotNull(leitscIncidentDAO);
    }

    @Test
    public void testFindAll() {

        LEITSCIncident leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident1");
        leitscIncident.setLeitscIncidentID("LEITSCIncident1");
        leitscIncidentDAO.makePersistent(leitscIncident);

        leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident2");
        leitscIncident.setLeitscIncidentID("LEITSCIncident2");
        leitscIncidentDAO.makePersistent(leitscIncident);

        List<LEITSCIncident> leitscIncidents = leitscIncidentDAO.findAll();

        assertEquals(2, leitscIncidents.size());

        leitscIncidentDAO.makeTransient(leitscIncidents.get(0));
        leitscIncidents = leitscIncidentDAO.findAll();
        assertEquals(1, leitscIncidents.size());

    }

    @Test
    public void testFindByLEITSCIncident() {

        LEITSCIncident leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident1");
        leitscIncident.setLeitscIncidentID("LEITSCIncident1");
        leitscIncidentDAO.makePersistent(leitscIncident);

        leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident2");
        leitscIncident.setLeitscIncidentID("LEITSCIncident2");
        leitscIncidentDAO.makePersistent(leitscIncident);

        LEITSCIncident result1 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident1");
        assertNotNull(result1);
        assertEquals("Incident1", result1.getIncidentID());

        LEITSCIncident result2 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident2");
        assertNotNull(result2);
        assertEquals("Incident2", result2.getIncidentID());

        leitscIncidentDAO.makeTransient(result1);
        result1 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident1");
        assertNull(result1);

        leitscIncidentDAO.makeTransient(result2);
        result2 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident2");
        assertNull(result2);
    }

    @Test
    public void testFindByIncident() {

        LEITSCIncident leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident1");
        leitscIncident.setLeitscIncidentID("LEITSCIncident1");
        leitscIncidentDAO.makePersistent(leitscIncident);

        leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident2");
        leitscIncident.setLeitscIncidentID("LEITSCIncident2");
        leitscIncidentDAO.makePersistent(leitscIncident);

        LEITSCIncident result1 = leitscIncidentDAO.findByIncident("Incident1");
        assertNotNull(result1);
        assertEquals("LEITSCIncident1", result1.getLeitscIncidentID());

        LEITSCIncident result2 = leitscIncidentDAO.findByIncident("Incident2");
        assertNotNull(result2);
        assertEquals("LEITSCIncident2", result2.getLeitscIncidentID());

        leitscIncidentDAO.makeTransient(result1);
        result1 = leitscIncidentDAO.findByIncident("Incident1");
        assertNull(result1);

        leitscIncidentDAO.makeTransient(result2);
        result2 = leitscIncidentDAO.findByIncident("Incident2");
        assertNull(result2);

    }

    @Override
    protected String[] getConfigLocations() {

        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/applicationContext-em.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml"
        };
    }
}
