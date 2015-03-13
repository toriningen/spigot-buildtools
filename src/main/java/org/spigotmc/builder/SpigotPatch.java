package org.spigotmc.builder;

import lombok.Getter;
import org.eclipse.jgit.lib.PersonIdent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class SpigotPatch
{

    private static final DateFormat format = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z" );

    private PersonIdent author;
    private String message;

    public SpigotPatch(File patch) throws Exception
    {
        BufferedReader reader = new BufferedReader( new FileReader( patch ) );
        try
        {
            String hash = null;
            String author = null;
            String date = null;
            StringBuilder message = new StringBuilder();

            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                if ( hash == null )
                {
                    hash = line;
                } else if ( author == null )
                {
                    author = line;
                } else if ( date == null )
                {
                    date = line;
                } else if ( line.startsWith( "diff " ) )
                {
                    break;
                } else
                {
                    if ( message.length() == 0 )
                    {
                        message.append( line.substring( "Subject: [PATCH] ".length() ).trim() );
                    } else
                    {
                        message.append( line.trim() );
                    }
                    message.append( '\n' );
                }
            }
            this.message = message.toString().trim();

            if ( author == null || date == null ) throw new RuntimeException();

            Date d = format.parse( date.substring( "Date: ".length() ) );
            int pos = date.indexOf( '+' );
            boolean neg = false;
            if ( pos == -1 )
            {
                pos = date.indexOf( '-' );
                neg = true;
            }
            int offsetHours = Integer.parseInt( date.substring( pos + 1, pos + 3 ) );
            int offsetMinutes = Integer.parseInt( date.substring( pos + 3 ) );
            int offset = offsetHours * 60 + offsetMinutes;

            this.author = new PersonIdent(
                    trimQuotes( author.substring( "From: ".length(), author.indexOf( '<' ) - 1 ) ),
                    author.substring( author.indexOf( '<' ) + 1, author.indexOf( '>' ) ),
                    d.getTime(),
                    neg ? -offset : offset
            );
        } finally
        {
            reader.close();
        }
    }

    private static String trimQuotes(String str) {
        if (str.startsWith( "\"" ) && str.endsWith( "\"" )) {
            return str.substring( 1, str.length() - 1 );
        }
        return str;
    }
}