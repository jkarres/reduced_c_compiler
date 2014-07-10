
//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

class ErrorPrinter
{
	public
        ErrorPrinter (Lexer lexer, boolean lineon)
	{
		m_lexer = lexer;

		m_lexer.setErrorPrinter (this);

		m_lineon = lineon;
	}


	public void
        print (String strMsg)
	{
		print (strMsg, 0);
	}


	public void
        print (String strMsg, int nOffset)
	{
		if(m_lineon)
            {
                System.out.println ("Error, \"" +
                                    m_lexer.getEPFilename () +
                                    "\", line " +
                                    (m_lexer.getLineNumber () + nOffset) + ": ");
                System.out.println ("  " + strMsg);
            }
		else
            {
                System.out.println ("Error, \"" +
                                    m_lexer.getEPFilename () +
                                    "\": ");
                System.out.println ("  " + strMsg);
            }
	}


	private	Lexer		m_lexer;
	private boolean		m_lineon;
}
