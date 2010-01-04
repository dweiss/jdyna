package org.jdyna.view.resources;

import java.io.IOException;

import org.junit.Test;


public class ResourceUtilitiesTest
{
    @Test
    public void testResourceAccessible() throws IOException
    {
        // Will throw an exception or NPE if the resource is not found.
        ResourceUtilities.open("jme/bomb.jme").close();
        ResourceUtilities.open("icons/life.png").close();
    }
}
