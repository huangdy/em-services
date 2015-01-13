package com.leidos.xchangecore.core.em.service;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.test.jpa.AbstractJpaTests;
import org.uicds.broadcastService.BroadcastMessageRequestDocument;

import x0.oasisNamesTcEmergencyEDXLDE1.EDXLDistributionDocument;
import x0.oasisNamesTcEmergencyEDXLDE1.ValueSchemeType;
import x0.oasisNamesTcEmergencyEDXLDE1.EDXLDistributionDocument.EDXLDistribution;

import com.leidos.xchangecore.core.em.service.impl.BroadcastServiceImpl;
import com.leidos.xchangecore.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareRuleInAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnknownException;
import com.leidos.xchangecore.core.infrastructure.exceptions.UICDSException;
import com.leidos.xchangecore.core.infrastructure.service.CommunicationsService;

public class BroadcastServiceTest
    extends AbstractJpaTests {

    private static final String USER2_JID = "user@core2.saic.com";

    private static final String USER1_JID = "user@core1.saic.com";

    BroadcastServiceImpl broadcastService = new BroadcastServiceImpl();

    CommunicationsService commService;

    @Override
    public void onSetUp() {

        commService = org.easymock.EasyMock.createMock(CommunicationsService.class);
        broadcastService.setCommunicationsService(commService);
    }

    @Test
    public void testBroadcastCreation() {

        assertNotNull(broadcastService);
    }

    @Test
    public void testSendCore2CoreMessage() throws IllegalArgumentException,
        LocalCoreNotOnlineException {

        BroadcastMessageRequestDocument message = BroadcastMessageRequestDocument.Factory.newInstance();
        EDXLDistribution edxl = message.addNewBroadcastMessageRequest().addNewEDXLDistribution();
        ValueSchemeType explictAddress = edxl.addNewExplicitAddress();
        explictAddress.setExplicitAddressScheme(CommunicationsService.UICDSExplicitAddressScheme);
        explictAddress.addExplicitAddressValue("user@core1");

        ValueSchemeType explictAddress2 = edxl.addNewExplicitAddress();
        explictAddress2.setExplicitAddressScheme(CommunicationsService.UICDSExplicitAddressScheme);
        explictAddress2.addExplicitAddressValue("user@core2");

        // System.out.println(message);

        // The commService sendMessage should get called twice, once for each core.
        try {
            // Note the message object must be the same object that the broadcastService
            // will use to call the communicationsService.
            EDXLDistributionDocument edxlDoc = EDXLDistributionDocument.Factory.newInstance();
            edxlDoc.setEDXLDistribution(edxl);

            commService.sendMessage(edxlDoc.xmlText(),
                CommunicationsService.CORE2CORE_MESSAGE_TYPE.BROADCAST_MESSAGE,
                "core1");
            // org.easymock.EasyMock.expectLastCall();
            commService.sendMessage(edxlDoc.xmlText(),
                CommunicationsService.CORE2CORE_MESSAGE_TYPE.BROADCAST_MESSAGE,
                "core2");
            // org.easymock.EasyMock.expectLastCall();
        } catch (RemoteCoreUnavailableException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (RemoteCoreUnknownException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoShareAgreementException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoShareRuleInAgreementException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        org.easymock.EasyMock.replay(commService);

        try {
            broadcastService.broadcastMessage(message);
            assertTrue(true);
        } catch (UICDSException e) {
            assertFalse(false);
        }

        org.easymock.EasyMock.verify(commService);
    }

    @Test
    public void testSendCore2CoreMessageWithOneOffline() throws IllegalArgumentException,
        LocalCoreNotOnlineException {

        BroadcastMessageRequestDocument message = BroadcastMessageRequestDocument.Factory.newInstance();
        EDXLDistribution edxl = message.addNewBroadcastMessageRequest().addNewEDXLDistribution();
        ValueSchemeType explictAddress = edxl.addNewExplicitAddress();
        explictAddress.setExplicitAddressScheme(CommunicationsService.UICDSExplicitAddressScheme);
        explictAddress.addExplicitAddressValue("user@core1");

        ValueSchemeType explictAddress2 = edxl.addNewExplicitAddress();
        explictAddress2.setExplicitAddressScheme(CommunicationsService.UICDSExplicitAddressScheme);
        explictAddress2.addExplicitAddressValue("user@core2");

        // System.out.println(message);

        // The commService sendMessage should get called twice, once for each core.
        try {
            // Note the message object must be the same object that the broadcastService
            // will use to call the communicationsService.
            EDXLDistributionDocument edxlDoc = EDXLDistributionDocument.Factory.newInstance();
            edxlDoc.setEDXLDistribution(edxl);

            // Only one call to send is expected because the local core is not online
            // therefore no messages could be sent
            //            commService.sendMessage(edxlDoc.toString(),
            //                CommunicationsService.CORE2CORE_MESSAGE_TYPE.BROADCAST_MESSAGE, "core1");
            // org.easymock.EasyMock.expectLastCall();
            commService.sendMessage(edxlDoc.xmlText(),
                CommunicationsService.CORE2CORE_MESSAGE_TYPE.BROADCAST_MESSAGE,
                "core2");
            EasyMock.expectLastCall().andThrow(new LocalCoreNotOnlineException());
            // org.easymock.EasyMock.expectLastCall();
        } catch (RemoteCoreUnavailableException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (RemoteCoreUnknownException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoShareAgreementException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoShareRuleInAgreementException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        org.easymock.EasyMock.replay(commService);

        try {
            broadcastService.broadcastMessage(message);
            assertTrue(true);
        } catch (UICDSException e) {
            assertFalse(false);
        }

        org.easymock.EasyMock.verify(commService);
    }

    @Test
    public void testSendXMPPMessage() throws IllegalArgumentException, LocalCoreNotOnlineException {

        BroadcastMessageRequestDocument message = BroadcastMessageRequestDocument.Factory.newInstance();
        EDXLDistribution edxl = message.addNewBroadcastMessageRequest().addNewEDXLDistribution();
        ValueSchemeType explictAddress = edxl.addNewExplicitAddress();
        explictAddress.setExplicitAddressScheme(CommunicationsService.XMPPAddressScheme);
        explictAddress.addExplicitAddressValue(USER1_JID);

        ValueSchemeType explictAddress2 = edxl.addNewExplicitAddress();
        explictAddress2.setExplicitAddressScheme(CommunicationsService.XMPPAddressScheme);
        explictAddress2.addExplicitAddressValue(USER2_JID);

        // System.out.println(message);

        // The commService sendMessage should get called twice, once for each core.
        // Note the message object must be the same object that the broadcastService
        // will use to call the communicationsService.
        EDXLDistributionDocument edxlDoc = EDXLDistributionDocument.Factory.newInstance();
        edxlDoc.setEDXLDistribution(edxl);

        commService.sendXMPPMessage(EasyMock.isA(String.class),
            (String) EasyMock.isNull(),
            EasyMock.eq(edxlDoc.xmlText()),
            EasyMock.eq(USER1_JID));
        // org.easymock.EasyMock.expectLastCall();
        commService.sendXMPPMessage(EasyMock.isA(String.class),
            (String) EasyMock.isNull(),
            EasyMock.eq(edxlDoc.xmlText()),
            EasyMock.eq(USER2_JID));
        // org.easymock.EasyMock.expectLastCall();

        org.easymock.EasyMock.replay(commService);

        try {
            broadcastService.broadcastMessage(message);
            assertTrue(true);
        } catch (UICDSException e) {
            assertFalse(false);
        }

        org.easymock.EasyMock.verify(commService);
    }

}
