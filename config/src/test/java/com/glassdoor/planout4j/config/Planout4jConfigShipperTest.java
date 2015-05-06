package com.glassdoor.planout4j.config;

import org.junit.Test;

public class Planout4jConfigShipperTest {

    @Test
    public void testDefault() throws ValidationException {
        new Planout4jConfigShipperImpl().ship();
    }

}
