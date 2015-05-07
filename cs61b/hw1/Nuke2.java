/* Nuke2.java */

import java.io.*;

class Nuke2
{
    public static void main (String[] args) throws Exception
    {
        InputStreamReader inStreamReader = new InputStreamReader(System.in);
        BufferedReader keyboard = new BufferedReader(inStreamReader);
        String inString;

        inString = keyboard.readLine();
        if (inString.length() > 2)
        {
            String outString;
            outString = inString.substring(0,1) + inString.substring(2);
            System.out.println(outString);
        }
        else
        {
            System.out.println(inString.charAt(0));
        }
    }
}
