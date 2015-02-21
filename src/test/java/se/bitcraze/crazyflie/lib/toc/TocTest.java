package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

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
        assertEquals(1, mToc.getTocElementMap().size());
    }

    @Test
    public void testClear() {
        assertEquals(1, mToc.getTocElementMap().size());
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

}
