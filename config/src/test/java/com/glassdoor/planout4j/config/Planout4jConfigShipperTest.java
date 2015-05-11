package com.glassdoor.planout4j.config;

import org.junit.Test;

public class Planout4jConfigShipperTest {

    @Test
    public void testDefault() throws ValidationException {
        Planout4jTestConfigHelper.setSystemProperties(true);
        new Planout4jConfigShipperImpl().ship(false);
    }

}
