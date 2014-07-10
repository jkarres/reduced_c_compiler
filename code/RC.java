//---------------------------------------------------------------------
//	The input for the compiler can be entered by filenames on
//	the command line (all names preceeded with "-" are ignored)
//	or by entering standard input.
//---------------------------------------------------------------------
import	java.util.Vector;


class RC
{
	public static void
        main (String[] args)
	{
		//	First, read in the files given from the command line
		//	filtering out any command line arguments.  Go backward
		//	on the filenames so the file listed first will be
		//	first in the list of files.
		Vector			filenames = new Vector();

        for (int i = args.length-1; i >= 0; i--)
        {
            if (!args[i].startsWith("-")) {
                filenames.addElement(args[i]);
            } else if (args[i].equals("-supersecretdebug")) {
                DebugLogger.activate();
            }
        }


		//	Now, start up the lexer with the files found.  If there
		//	were no files, the lexer will default to reading in from
		//	System.in.
		Lexer		lexer = new Lexer (filenames);


		//	The error printer uses the lexer to get the name
		//	of the current file & line number.
		ErrorPrinter	errors = new ErrorPrinter (lexer, false);

        AssemblyWriter aw = new AssemblyWriter(false);

		//	Finally, the parser takes in everybody.
		MyParser     	parser = new MyParser (lexer, errors, aw);



		try
        {
            parser.parse ();
            if (parser.Ok ())
                System.out.println ("Compile: success.");
            else
                System.out.println ("Compile: failure.");
        }
		catch (Exception e)
        {
            e.printStackTrace();
            System.out.println ("Compile: failure.");
        }
        DebugLogger.closeLog();

        aw.close();

	}

}
