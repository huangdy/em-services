package com.leidos.xchangecore.core.em.endpoint;

import org.junit.Test;
import org.springframework.test.jpa.AbstractJpaTests;
import org.uicds.agreementService.AgreementType;
import org.uicds.agreementService.AgreementType.Principals;
import org.uicds.broadcastService.BroadcastMessageRequestDocument;

import x0.oasisNamesTcEmergencyEDXLDE1.ValueSchemeType;
import x0.oasisNamesTcEmergencyEDXLDE1.EDXLDistributionDocument.EDXLDistribution;

import com.leidos.xchangecore.core.em.endpoint.BroadcastServiceEndpoint;
import com.leidos.xchangecore.core.em.exceptions.SendMessageErrorException;
import com.leidos.xchangecore.core.em.service.BroadcastService;
import com.leidos.xchangecore.core.infrastructure.exceptions.AgreementWithCoreExists;
import com.leidos.xchangecore.core.infrastructure.exceptions.EmptyCoreNameListException;
import com.leidos.xchangecore.core.infrastructure.exceptions.MissingConditionInShareRuleException;
import com.leidos.xchangecore.core.infrastructure.exceptions.MissingShareRulesElementException;
import com.leidos.xchangecore.core.infrastructure.service.AgreementService;

public class BroadcastServiceEndpointTest
    extends AbstractJpaTests {

    BroadcastService broadcastService;

    // @Autowired
    BroadcastServiceEndpoint broadcastServiceEndpoint = new BroadcastServiceEndpoint();

    AgreementService agreementService;

    private String localJID = "RM@core1.saic.com";
    private String remoteJID = "RM@core2.saic.com";
    private String addressScheme = "uicds:user";

    @Override
    public void onSetUp() {

        broadcastService = org.easymock.EasyMock.createMock(BroadcastService.class);
        agreementService = org.easymock.EasyMock.createMock(AgreementService.class);
        broadcastServiceEndpoint.setBroadcastService(broadcastService);
    }

    @Test
    public void testBroadcastMessage() throws Exception {

        assertNotNull(broadcastServiceEndpoint);

        createAgreement();

        BroadcastMessageRequestDocument message = BroadcastMessageRequestDocument.Factory.newInstance();
        message = setJIDDataBroadcastRequest(message);
        assertNotNull(message);

        try {
            // Note the message object must be the same object that the broadcastServiceEndpoint
            // will use to call the broadcastService.
            broadcastService.broadcastMessage(message);
            org.easymock.EasyMock.expectLastCall();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (EmptyCoreNameListException e1) {
            e1.printStackTrace();
        } catch (SendMessageErrorException e1) {
            e1.printStackTrace();
        }
        org.easymock.EasyMock.replay(broadcastService);

        broadcastServiceEndpoint.BroadcastMessage(message);
        assertTrue(true);

        org.easymock.EasyMock.verify(broadcastService);
    }

    private BroadcastMessageRequestDocument setJIDDataBroadcastRequest(BroadcastMessageRequestDocument request) {

        // set test data
        request.addNewBroadcastMessageRequest().addNewEDXLDistribution();
        EDXLDistribution edxl = request.addNewBroadcastMessageRequest().addNewEDXLDistribution();

        edxl.setSenderID(localJID);
        ValueSchemeType address = edxl.addNewExplicitAddress();
        String[] JIDArray = new String[1];
        JIDArray[0] = localJID;
        address.setExplicitAddressScheme(addressScheme);
        address.setExplicitAddressValueArray(JIDArray);
        JIDArray[0] = remoteJID;
        address = edxl.addNewExplicitAddress();
        address.setExplicitAddressScheme(addressScheme);
        edxl.getExplicitAddressArray(1).setExplicitAddressValueArray(JIDArray);
        return request;
    }

    private void createAgreement() {

        AgreementType agreement = AgreementType.Factory.newInstance();
        Principals principals = agreement.addNewPrincipals();
        principals.addNewLocalCore().setStringValue(localJID);
        principals.addNewRemoteCore().setStringValue(remoteJID);
        agreement.addNewShareRules().setEnabled(true);

        try {
            AgreementType response = agreementService.createAgreement(agreement);
            org.easymock.EasyMock.expectLastCall();
        } catch (MissingConditionInShareRuleException e) {
            fail(e.getMessage());
        } catch (AgreementWithCoreExists e) {
            fail(e.getMessage());
        } catch (MissingShareRulesElementException e) {
            fail(e.getMessage());
        }
    }

}
