package main;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

public class UnicodeXMLStreamWriter implements XMLStreamWriter {
    private XMLStreamWriter xmlSW;
    private Writer writer;
    private CharSequenceTranslator characterEscapor = StringEscapeUtils.ESCAPE_XML;

    public static UnicodeXMLStreamWriter newInstance(Writer writer) throws XMLStreamException, FactoryConfigurationError {
        return newInstance(writer, XMLOutputFactory.newFactory());
    }

    public static UnicodeXMLStreamWriter newInstance(Writer writer, XMLOutputFactory factory) throws XMLStreamException {
        XMLStreamWriter xmlSW = factory.createXMLStreamWriter(writer);
        return new UnicodeXMLStreamWriter(writer, xmlSW);
    }

    public UnicodeXMLStreamWriter(Writer writer, XMLStreamWriter xmlSW) {
        this.writer = writer;
        this.xmlSW = xmlSW;
    }

    public void writeCharacters(String text) throws XMLStreamException {
        // finish writing start element
        xmlSW.writeCharacters("");
        xmlSW.flush();
        try {
            characterEscapor.translate(text, writer);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeCharacters(char[] text, int start, int len)
            throws XMLStreamException {
        // finish writing start element
        xmlSW.writeCharacters("");
        xmlSW.flush();
        try {
            characterEscapor.translate(CharBuffer.wrap(text, start, len), writer);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    //////////////// REMAINING METHODS ARE DELEGATES to xmlSW ////////////////

    public void writeStartElement(String localName) throws XMLStreamException {
        xmlSW.writeStartElement(localName);
    }

    public void writeStartElement(String namespaceURI, String localName)
            throws XMLStreamException {
        xmlSW.writeStartElement(namespaceURI, localName);
    }

    public void writeStartElement(String prefix, String localName,
                                  String namespaceURI) throws XMLStreamException {
        xmlSW.writeStartElement(prefix, localName, namespaceURI);
    }

    public void writeEmptyElement(String namespaceURI, String localName)
            throws XMLStreamException {
        xmlSW.writeEmptyElement(namespaceURI, localName);
    }

    public void writeEmptyElement(String prefix, String localName,
                                  String namespaceURI) throws XMLStreamException {
        xmlSW.writeEmptyElement(prefix, localName, namespaceURI);
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        xmlSW.writeEmptyElement(localName);
    }

    public void writeEndElement() throws XMLStreamException {
        xmlSW.writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        xmlSW.writeEndDocument();
    }

    public void close() throws XMLStreamException {
        xmlSW.close();
    }

    public void flush() throws XMLStreamException {
        xmlSW.flush();
    }

    public void writeAttribute(String localName, String value)
            throws XMLStreamException {
        xmlSW.writeAttribute(localName, value);
    }

    public void writeAttribute(String prefix, String namespaceURI,
                               String localName, String value) throws XMLStreamException {
        xmlSW.writeAttribute(prefix, namespaceURI, localName, value);
    }

    public void writeAttribute(String namespaceURI, String localName,
                               String value) throws XMLStreamException {
        xmlSW.writeAttribute(namespaceURI, localName, value);
    }

    public void writeNamespace(String prefix, String namespaceURI)
            throws XMLStreamException {
        xmlSW.writeNamespace(prefix, namespaceURI);
    }

    public void writeDefaultNamespace(String namespaceURI)
            throws XMLStreamException {
        xmlSW.writeDefaultNamespace(namespaceURI);
    }

    public void writeComment(String data) throws XMLStreamException {
        xmlSW.writeComment(data);
    }

    public void writeProcessingInstruction(String target)
            throws XMLStreamException {
        xmlSW.writeProcessingInstruction(target);
    }

    public void writeProcessingInstruction(String target, String data)
            throws XMLStreamException {
        xmlSW.writeProcessingInstruction(target, data);
    }

    public void writeCData(String data) throws XMLStreamException {
        xmlSW.writeCData(data);
    }

    public void writeDTD(String dtd) throws XMLStreamException {
        xmlSW.writeDTD(dtd);
    }

    public void writeEntityRef(String name) throws XMLStreamException {
        xmlSW.writeEntityRef(name);
    }

    public void writeStartDocument() throws XMLStreamException {
        xmlSW.writeStartDocument();
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        xmlSW.writeStartDocument(version);
    }

    public void writeStartDocument(String encoding, String version)
            throws XMLStreamException {
        xmlSW.writeStartDocument(encoding, version);
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return xmlSW.getPrefix(uri);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        xmlSW.setPrefix(prefix, uri);
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        xmlSW.setDefaultNamespace(uri);
    }

    public void setNamespaceContext(NamespaceContext context)
            throws XMLStreamException {
        xmlSW.setNamespaceContext(context);
    }

    public NamespaceContext getNamespaceContext() {
        return xmlSW.getNamespaceContext();
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return xmlSW.getProperty(name);
    }
}