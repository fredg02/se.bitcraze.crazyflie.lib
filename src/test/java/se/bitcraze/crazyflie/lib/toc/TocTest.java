/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2015 Bitcraze AB
 *
 * Crazyflie Nano Quadcopter Client
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@Category(OfflineTests.class)
public class TocTest {

    private Toc mToc;

    @Before
    public void setUp() throws Exception {
        mToc = new Toc();
        assertTrue(mToc.getTocElementMap().isEmpty());
        TocElement tocElement = new TocElement();
        tocElement.setGroup("testGroup");
        tocElement.setName("testName");
        tocElement.setIdent(1);
        mToc.addElement(tocElement);
    }

    @Test
    public void testAddElement() {
        assertEquals(1, mToc.getTocSize());
        // add toc element
        TocElement tocElement2 = new TocElement();
        tocElement2.setGroup("testGroup2");
        tocElement2.setName("testName2");
        tocElement2.setIdent(2);
        mToc.addElement(tocElement2);
        assertEquals(2, mToc.getTocSize());
    }

    @Test
    public void testClear() {
        assertEquals(1, mToc.getTocSize());
        mToc.clear();
        assertTrue(mToc.getTocElementMap().isEmpty());
    }

    @Test
    public void testGetElementByCompleteName() {
        //positive test
        TocElement elementByCompleteName = mToc.getElementByCompleteName("testGroup.testName");
        assertTrue(elementByCompleteName != null);
        //negative test
        TocElement nonExistingElement = mToc.getElementByCompleteName("nonExistingGroup.nonExistingName");
        assertTrue(nonExistingElement == null);
    }

    @Test
    public void testGetElementId() {
        //positive test
        int elementId = mToc.getElementId("testGroup.testName");
        assertEquals(1, elementId);
        System.out.println("elementId: " + elementId);
        //negative test
        int elementId2 = mToc.getElementId("testGroup.nonExistingName");
        assertEquals(-1, elementId2);
        System.out.println("elementId2: " + elementId2);
    }

    @Test
    public void testGetElement() {
        //positive test
        TocElement element = mToc.getElement("testGroup", "testName");
        assertTrue(element != null);
        //negative test
        TocElement element2 = mToc.getElement("nonExistingGroup", "testName");
        assertTrue(element2 == null);
    }

    @Test
    public void testGetElementById() {
        //positive test
        TocElement elementById = mToc.getElementById(1);
        assertTrue(elementById != null);
        System.out.println("TocElementById: " + elementById);
        //negative test
        TocElement elementById2 = mToc.getElementById(4711);
        assertTrue(elementById2 == null);
        System.out.println("TocElementById2: " + elementById2);
    }

    @Test
    public void testGetElements() {
        //positive test
        List<TocElement> elements = mToc.getElements();
        assertEquals(1, elements.size());
        //negative test
        mToc.clear();
        List<TocElement> elements2 = mToc.getElements();
        assertEquals(0, elements2.size());
    }

    @Test
    public void testGetTocElementMap() {
        //positive test
        Map<String, TocElement> elements = mToc.getTocElementMap();
        assertEquals(1, elements.size());
        //negative test
        mToc.clear();
        Map<String, TocElement> elements2 = mToc.getTocElementMap();
        assertEquals(0, elements2.size());
    }

    @Test
    public void testGetTocSize() {
        //positive test
        assertEquals(1, mToc.getTocSize());
        //negative test
        mToc.clear();
        assertEquals(0, mToc.getTocSize());
    }
}
