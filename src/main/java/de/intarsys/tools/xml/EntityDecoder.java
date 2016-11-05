package de.intarsys.tools.xml;

import de.intarsys.tools.reader.DirectTagReader;
import de.intarsys.tools.reader.IDirectTagHandler;
import de.intarsys.tools.reader.ILocationProvider;

import java.io.IOException;
import java.io.Reader;

public class EntityDecoder extends DirectTagReader {

  private static IDirectTagHandler ENTITY_HANDLER = new IDirectTagHandler() {
    public String process(String tagContent, Object context)
        throws IOException {
      try {
        if (tagContent.startsWith("#")) {
          char c = (char) Integer.parseInt(tagContent.substring(1),
              10);
          return new String(new char[]{c});
        }
        if (tagContent.equals("amp")) {
          return "&";
        }
        if (tagContent.equals("lt")) {
          return "<";
        }
        if (tagContent.equals("gt")) {
          return ">";
        }
        if (tagContent.equals("apos")) {
          return "'";
        }
        if (tagContent.equals("auml")) {
          return "�";
        }
        if (tagContent.equals("Auml")) {
          return "�";
        }
        if (tagContent.equals("ouml")) {
          return "�";
        }
        if (tagContent.equals("Ouml")) {
          return "�";
        }
        if (tagContent.equals("uuml")) {
          return "�";
        }
        if (tagContent.equals("Uuml")) {
          return "�";
        }
      } catch (Exception e) {
      }
      return tagContent;
    }

    public void setLocationProvider(ILocationProvider locationProvider) {
    }

    public void startTag() {
    }
  };
  private static String ENTITY_END = ";"; //$NON-NLS-1$
  private static String ENTITY_START = "&"; //$NON-NLS-1$

  public EntityDecoder(Reader reader) {
    super(reader, ENTITY_HANDLER, null);
    setStartTag(ENTITY_START);
    setEndTag(ENTITY_END);
  }

  public EntityDecoder(Reader reader, boolean escape) {
    super(reader, ENTITY_HANDLER, null, escape);
    setStartTag(ENTITY_START);
    setEndTag(ENTITY_END);
  }

}
