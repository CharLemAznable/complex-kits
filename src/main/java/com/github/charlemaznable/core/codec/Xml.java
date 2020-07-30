package com.github.charlemaznable.core.codec;

import lombok.Getter;
import lombok.SneakyThrows;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.github.charlemaznable.core.lang.Listt.isNotEmpty;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.dom4j.DocumentHelper.createDocument;
import static org.dom4j.DocumentHelper.createElement;
import static org.dom4j.io.OutputFormat.createCompactFormat;
import static org.dom4j.io.OutputFormat.createPrettyPrint;

public final class Xml {

    private static final String TEXT = "#text";

    private Xml() {}

    public static String xml(Map<String, Object> map) {
        return xml(map, "xml", false);
    }

    public static String xml(Map<String, Object> map, String rootName) {
        return xml(map, rootName, false);
    }

    public static String xml(Map<String, Object> map, boolean prettyFormat) {
        return xml(map, "xml", prettyFormat);
    }

    public static String xml(Map<String, Object> map, String rootName, boolean prettyFormat) {
        var rootElement = createElement(rootName);
        Map2XmlString.map2Element(map, rootElement);
        return Map2XmlString.document2XmlString(createDocument(rootElement), prettyFormat);
    }

    public static Map<String, Object> unXml(String text) {
        return unXml(text, new XmlParseFeature(), false);
    }

    public static Map<String, Object> unXml(String text, XmlParseFeature feature) {
        return unXml(text, feature, false);
    }

    public static Map<String, Object> unXml(String text, boolean rootAsTop) {
        return unXml(text, new XmlParseFeature(), rootAsTop);
    }

    public static Map<String, Object> unXml(String text, XmlParseFeature feature, boolean rootAsTop) {
        var document = XmlString2Map.xmlString2Document(text, feature);
        var rootElement = document.getRootElement();
        if (rootElement.elements().isEmpty() &&
                rootElement.attributes().isEmpty())
            return new LinkedHashMap<>();
        var map = XmlString2Map.element2Map(rootElement, false);
        return rootAsTop ? of(rootElement.getName(), map) : map;
    }

    private static final class XmlString2Map {

        @SneakyThrows
        public static Document xmlString2Document(String text, XmlParseFeature feature) {
            var reader = new SAXReader();
            feature.setSAXReaderFeatures(reader);
            var encoding = getEncoding(text);
            var source = new InputSource(new StringReader(text));
            source.setEncoding(encoding);
            var result = reader.read(source);
            if (isNull(result.getXMLEncoding())) {
                result.setXMLEncoding(encoding);
            }
            return result;
        }

        private static String getEncoding(String text) {
            String result = null;
            var xml = text.trim();
            if (xml.startsWith("<?xml")) {
                int end = xml.indexOf("?>");
                var sub = xml.substring(0, end);
                var tokens = new StringTokenizer(sub, " =\"'");

                while (tokens.hasMoreTokens()) {
                    var token = tokens.nextToken();
                    if ("encoding".equals(token)) {
                        if (tokens.hasMoreTokens()) {
                            result = tokens.nextToken();
                        }
                        break;
                    }
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private static Map<String, Object> element2Map(Element element, boolean parseAttr) {
            var map = new LinkedHashMap<String, Object>();
            var elements = element.elements();

            List<Attribute> attributes = null;
            if (parseAttr) {
                attributes = element.attributes(); // 当前节点的所有属性的list
                for (var attribute : attributes) {
                    map.put("@" + attribute.getName(), attribute.getValue());
                }
            }

            if (!elements.isEmpty()) {
                for (var elem : elements) {
                    List mapList = newArrayList();

                    if (!elem.elements().isEmpty()) {
                        parseElementWithChildren(map, elem, mapList, parseAttr);

                    } else {
                        parseElementWithoutChildren(map, elem, parseAttr);
                    }
                }
            } else {
                // 根节点的
                parseRootElement(map, element, attributes);
            }
            return map;
        }

        @SuppressWarnings("unchecked")
        private static void parseElementWithChildren(LinkedHashMap<String, Object> map, Element elem, List mapList, boolean parseAttr) {
            var m = element2Map(elem, parseAttr);
            if (nonNull(map.get(elem.getName()))) {
                var obj = map.get(elem.getName());
                if (!(obj instanceof List)) {
                    mapList = newArrayList();
                    mapList.add(obj);
                    mapList.add(m);
                }
                if (obj instanceof List) {
                    mapList = (List) obj;
                    mapList.add(m);
                }
                map.put(elem.getName(), mapList);
            } else map.put(elem.getName(), m);
        }

        private static void parseElementWithoutChildren(LinkedHashMap<String, Object> map, Element elem, boolean parseAttr) {
            List mapList;
            var hasAttributes = false;
            Map<String, Object> attributesMap = null;
            if (parseAttr) {
                var attrs = elem.attributes(); // 当前节点的所有属性的list
                if (!attrs.isEmpty()) {
                    hasAttributes = true;
                    attributesMap = new LinkedHashMap<>();
                    for (var attr : attrs) {
                        attributesMap.put("@" + attr.getName(), attr.getValue());
                    }
                }
            }

            if (nonNull(map.get(elem.getName()))) {
                var obj = map.get(elem.getName());
                mapList = obj instanceof List ?
                        (List) obj : newArrayList(obj);
                addListItem(mapList, elem,
                        parseAttr, hasAttributes, attributesMap);
                map.put(elem.getName(), mapList);

            } else {
                if (parseAttr && hasAttributes) {
                    attributesMap.put(TEXT, elem.getText());
                    map.put(elem.getName(), attributesMap);
                } else {
                    map.put(elem.getName(), elem.getText());
                }
            }
        }

        private static void parseRootElement(LinkedHashMap<String, Object> map, Element element, List<Attribute> attributes) {
            if (isNotEmpty(attributes)) {
                map.put(TEXT, element.getText());
            } else {
                map.put(element.getName(), element.getText());
            }
        }

        @SuppressWarnings("unchecked")
        private static void addListItem(List mapList, Element elem, boolean parseAttr, boolean hasAttributes, Map<String, Object> attributesMap) {
            if (parseAttr && hasAttributes) {
                attributesMap.put(TEXT, elem.getText());
                mapList.add(attributesMap);
            } else {
                mapList.add(elem.getText());
            }
        }
    }

    private static final class Map2XmlString {

        @SneakyThrows
        private static String document2XmlString(Document document, boolean prettyFormat) {
            var writer = new StringWriter();
            var format = prettyFormat ? createPrettyPrint() : createCompactFormat();
            var xmlWriter = new XMLWriter(writer, format);
            xmlWriter.write(document);
            xmlWriter.close();
            return writer.toString();
        }

        @SuppressWarnings("unchecked")
        private static void map2Element(Map<String, Object> map, Element body) {
            for (var entry : map.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (key.startsWith("@")) { // 属性
                    body.addAttribute(key.substring(1, key.length()), value.toString());
                } else if (key.equals(TEXT)) { // 有属性时的文本
                    body.addCDATA(value.toString());
                } else {
                    if (value instanceof List) {
                        parseListElement(body, key, (List) value);
                    } else if (value instanceof Map) {
                        var subElement = body.addElement(key);
                        map2Element((Map) value, subElement);
                    } else {
                        if (isNull(value)) continue;
                        body.addElement(key).addCDATA(value.toString());
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static void parseListElement(Element body, String key, List list) {
            for (var obj : list) {
                // list里是map或String，不会存在list里直接是list的，
                if (obj instanceof Map) {
                    var subElement = body.addElement(key);
                    map2Element((Map) obj, subElement);
                } else {
                    body.addElement(key).addCDATA((String) obj);
                }
            }
        }

    }

    @Getter
    public static final class XmlParseFeature {

        private static final String DISALLOW_DOCTYPE_DECL_NAME
                = "http://apache.org/xml/features/disallow-doctype-decl";
        private static final String LOAD_EXTERNAL_DTD_NAME
                = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
        private static final String EXTERNAL_GENERAL_ENTITIES_NAME
                = "http://xml.org/sax/features/external-general-entities";
        private static final String EXTERNAL_PARAMETER_ENTITIES_NAME
                = "http://xml.org/sax/features/external-parameter-entities";
        private boolean disallowDoctypeDecl = true;
        private boolean loadExternalDTD = false;
        private boolean externalGeneralEntities = false;
        private boolean externalParameterEntities = false;

        public void setSAXReaderFeatures(SAXReader reader) {
            try {
                reader.setFeature(DISALLOW_DOCTYPE_DECL_NAME, disallowDoctypeDecl);
                reader.setFeature(LOAD_EXTERNAL_DTD_NAME, loadExternalDTD);
                reader.setFeature(EXTERNAL_GENERAL_ENTITIES_NAME, externalGeneralEntities);
                reader.setFeature(EXTERNAL_PARAMETER_ENTITIES_NAME, externalParameterEntities);
            } catch (SAXException ignored) {
                // ignored
            }
        }
    }
}
