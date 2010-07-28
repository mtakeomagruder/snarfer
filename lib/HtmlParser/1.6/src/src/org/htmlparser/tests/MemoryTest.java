// HTMLParser Library $Name: v1_6_20060319 $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derrick Oswald
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/tests/MemoryTest.java,v $
// $Author: derrickoswald $
// $Date: 2004/07/31 01:22:45 $
// $Revision: 1.3 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.tests;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;

/**
 * Test big memory requirements.
 */
public class MemoryTest extends ParserTestCase
{
    
    static
    {
        System.setProperty ("org.htmlparser.tests.MemoryTest", "MemoryTest");
    }

    public MemoryTest (String name)
    {
        super (name);
    }

    /**
     * Test for bug #922439 OutOfMemory on huge HTML files (4,7MB)
     */
    public void testBigFile () throws Exception
    {
        Parser parser;
        NodeIterator iterator;
        Node node;
        int size;
        
        parser = new Parser ("http://htmlparser.sourceforge.net/test/A002.html");
        size = 0;
        try
        {
            iterator = parser.elements ();
            while (iterator.hasMoreNodes ())
            {
                node = iterator.nextNode ();
                size += node.toHtml ().length ();
            }
        }
        catch (OutOfMemoryError ome)
        {
            fail ("out of memory");
        }
        assertEquals ("wrong size fetched", 4697386, size);
    }
    
}
