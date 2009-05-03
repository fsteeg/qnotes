package com.quui.notes.editor.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.quui.notes.editor.data.NotesFilter.SearchOption;

/**
 * Read XML notebook file using STAX.
 * @author Fabian Steeg (fsteeg)
 */
public final class NotesReader {
    /***/
    private XMLStreamReader reader;
    /***/
    private String location;
    /***/
    private boolean validating;

    /**
     * @param location The location of the XML file containing the notes to read
     * @return The reader instance
     */
    public static NotesReader getInstance(final String location) {
        return new NotesReader(location);
    }

    /**
     * @param location The location of the XML file containing the notes to read
     */
    private NotesReader(final String location) {
        this.location = location;
        this.validating = true;
        init();
    }

    private void init() {
        try {
            File f = new File(this.location);
            if (!f.exists()) {
                throw new IllegalStateException("File does not exist: "
                        + this.location);
            }
            this.reader = createReader(this.location, validating);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public final List<Note> read() {
        return read(null, null);
    }

    public final List<Note> read(String query, String item) {
        init();
        List<Note> result = new ArrayList<Note>();
        try {
            while (reader.hasNext()) {
                if (reader.isStartElement()) {
                    if (reader.getLocalName().equals("nbsp")) {

                    } else if (reader.getLocalName().equals("note")) {
                        String publicAttribute = reader.getAttributeValue(0);
                        String tagAttribute = reader.getAttributeValue(3);
                        String titleAttribute = reader.getAttributeValue(1);
                        String dateAttribute = reader.getAttributeValue(2);
                        String text = reader.getElementText();
                        Note note = Note.getInstance(text, titleAttribute,
                                dateAttribute, tagAttribute, publicAttribute);
                        if ((query == null && item == null)
                                || NotesFilter.matches(note, query,
                                        SearchOption.valueOf(item))) {
                            result.add(note);
                        }
                    }
                }
                reader.next();
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param fileLocation The location of the annotations file
     * @param validating
     * @return Returns the reader for the location
     * @throws FileNotFoundException while reading
     * @throws XMLStreamException while reading
     */
    private XMLStreamReader createReader(final String fileLocation,
            boolean validating) throws FileNotFoundException,
            XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                Boolean.TRUE);
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance("http://www.w3.org/2001/XMLSchema");
        Source source = new StreamSource(fileLocation);
        XMLStreamReader newReader;
        System.setProperty("javax.xml.stream.isCoalescing", "true");
        newReader = factory.createXMLStreamReader(new FileInputStream(
                fileLocation), "UTF-8");
        System.out.println("Validating: " + validating);
        if (validating) {
            try {
                URL resource = this.getClass().getResource("notes.xsd");
                if (resource == null) {
                    throw new IllegalStateException("Could not load XSD");
                }
                Schema schemaGrammar = schemaFactory.newSchema(resource);
                Validator schemaValidator = schemaGrammar.newValidator();
                schemaValidator.setErrorHandler(new ErrorHandler() {
                    public void warning(SAXParseException exception)
                            throws SAXException {}

                    public void fatalError(SAXParseException exception)
                            throws SAXException {}

                    public void error(SAXParseException exception)
                            throws SAXException {}
                });
                schemaValidator.validate(source);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newReader;
    }
}
