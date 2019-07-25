/*
 * Copyright 2016-2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.python.controllers;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.gaffer.python.session.GafferSession;
import uk.gov.gchq.gaffer.python.util.exceptions.NoPortsAvailableException;
import uk.gov.gchq.gaffer.python.util.exceptions.PortInUseException;
import uk.gov.gchq.gaffer.python.util.exceptions.PortNotInRangeException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SessionManagerTest {

    private static SessionManager sessionManager;
    private static InetAddress address = null;
    private GafferSession gs = null;
    private GafferSession gs2 = null;

    @BeforeClass
    public static void setUp() { // Sets up address and session manager before the suite starts
        sessionManager = SessionManager.getInstance();
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() { // Removes any instances of GafferSession left after each test
        if (gs != null) {
            sessionManager.removeSession(gs);
        }
        if (gs2 != null) {
            sessionManager.removeSession(gs2);
        }
    }


    @Test
    public void sessionFactory_CreatesADefaultSession() throws PortInUseException {
        gs = sessionManager.sessionFactory();

        assertEquals(1, sessionManager.getAllSessions().size());
    }

    @Test(expected = PortInUseException.class)
    public void sessionFactory_CannotRunMultipleDefaultSessions() throws PortInUseException {
        gs = sessionManager.sessionFactory();
        gs2 = sessionManager.sessionFactory(); // should error on this
    }

    @Test
    public void sessionFactory1_CreatesASession() throws NoPortsAvailableException {
        gs = sessionManager.sessionFactory(address);

        assertEquals(1, sessionManager.getAllSessions().size());
    }

    @Test
    public void sessionFactory1_OnDestructionRemovesSession() throws NoPortsAvailableException {
        gs = sessionManager.sessionFactory(address);
        assertTrue(sessionManager.removeSession(gs));
    }

    @Test
    public void sessionFactory2_CreatesASession() throws PortInUseException, PortNotInRangeException {
        gs = sessionManager.sessionFactory(address, 25339);

        assertEquals(1, sessionManager.getAllSessions().size());
    }

    @Test(expected = PortInUseException.class)
    public void sessionFactory2_ThrowsAPortInUseError() throws PortInUseException, PortNotInRangeException {
        gs = sessionManager.sessionFactory(address, 25337);
        gs2 = sessionManager.sessionFactory(address, 25337);
    }

    @Test
    public void sessionFactory2_OnDestructionRemovesSession() throws PortInUseException, PortNotInRangeException {
        gs = sessionManager.sessionFactory(address, 25335);

        assertTrue(sessionManager.removeSession(gs));
    }


    @Test(expected = PortNotInRangeException.class)
    public void sessionFactory2_DoesntAllowPortHigherThanMaxRange() throws PortInUseException, PortNotInRangeException {
        gs = sessionManager.sessionFactory(address, 65536);
    }

    @Test(expected = PortNotInRangeException.class)
    public void sessionFactory2_DoesntAllowPortLowerThanLowestRange() throws PortInUseException, PortNotInRangeException {
        gs = sessionManager.sessionFactory(address, 25332);
    }
}
