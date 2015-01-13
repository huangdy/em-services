package com.leidos.xchangecore.core.em.endpoint;

import org.easymock.EasyMock;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.jpa.AbstractJpaTests;

import com.leidos.xchangecore.core.em.endpoint.IAPServiceEndpoint;
import com.leidos.xchangecore.core.em.service.IAPService;

public class IAPServiceEndpointTest
    extends AbstractJpaTests {

    Logger log = LoggerFactory.getLogger(getClass());

    IAPServiceEndpoint iapEndpoint = new IAPServiceEndpoint();

    IAPService iapService;

    @Override
    public void onSetUp() {

        if (iapService == null) {
            iapService = EasyMock.createMock(IAPService.class);
            iapEndpoint.setIAPService(iapService);
        }
    }

    @Test
    public void testIAPServiceEndpointCreated() {

        assertNotNull(iapService);
        assertNotNull(iapEndpoint);
    }

}
