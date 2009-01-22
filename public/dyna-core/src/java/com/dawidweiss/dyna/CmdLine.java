package com.dawidweiss.dyna;

import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Command-line utilities.
 */
public final class CmdLine
{
    /**
     * Parse command line arguments, applying them to <code>target</code>. In case of
     * an error, the usage string is printed to standard output. 
     * 
     * @return <code>false</code> if an error parsing arguments occurred.
     */
    public static boolean parseArgs(Object target, String [] args)
    {
        final CmdLineParser parser = new CmdLineParser(target);
        parser.setUsageWidth(80);
        try
        {
            parser.parseArgument(args);
            return true;
        }
        catch (CmdLineException e)
        {
            PrintStream ps = System.out;
            ps.print("Usage: ");
            parser.printSingleLineUsage(ps);
            ps.println();
            parser.printUsage(ps);

            ps.println("\n" + e.getMessage());
            return false;
        }
    }
}
