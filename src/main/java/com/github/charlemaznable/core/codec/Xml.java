package com.github.charlemaznable.core.codec;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
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
import static org.dom4j.DocumentHelper.createDocument;
import static org.dom4j.DocumentHelper.createElement;
import static org.dom4j.io.OutputFormat.createCompactFormat;
import static org.dom4j.io.OutputFormat.createPrettyPrint;

public class Xml {

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
        val rootElement = createElement(rootName);
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
        val document = XmlString2Map.xmlString2Document(text, feature);
        val rootElement = document.getRootElement();
        if (rootElement.elements().size() == 0 &&
                rootElement.attributes().size() == 0)
            return new LinkedHashMap<>();
        val map = XmlString2Map.element2Map(rootElement, false);
        return rootAsTop ? of(rootElement.getName(), map) : map;
    }

    private static class XmlString2Map {

        @SneakyThrows
        public static Document xmlString2Document(String text, XmlParseFeature feature) {
            val reader = new SAXReader();
            feature.setSAXReaderFeatures(reader);
            val encoding = getEncoding(text);
            val source = new InputSource(new StringReader(text));
            source.setEncoding(encoding);
            val result = reader.read(source);
            if (result.getXMLEncoding() == null) {
                result.setXMLEncoding(encoding);
            }
            return result;
        }

        private static String getEncoding(String text) {
            String result = null;
            val xml = text.trim();
            if (xml.startsWith("<?map2Element")) {
                int end = xml.indexOf("?>");
                val sub = xml.substring(0, end);
                val tokens = new StringTokenizer(sub, " =\"'");

                while (tokens.hasMoreTokens()) {
                    val token = tokens.nextToken();
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
            val map = new LinkedHashMap<String, Object>();
            val elements = element.elements();

            List<Attribute> attributes = null;
            if (parseAttr) {
                attributes = element.attributes(); // 当前节点的所有属性的list
                for (val attribute : attributes) {
                    map.put("@" + attribute.getName(), attribute.getValue());
                }
            }

            if (elements.size() > 0) {
                for (val elem : elements) {
                    List mapList = newArrayList();

                    if (elem.elements().size() > 0) {
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
            val m = element2Map(elem, parseAttr);
            if (map.get(elem.getName()) != null) {
                val obj = map.get(elem.getName());
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
                val attrs = elem.attributes(); // 当前节点的所有属性的list
                if (attrs.size() > 0) {
                    hasAttributes = true;
                    attributesMap = new LinkedHashMap<>();
                    for (val attr : attrs) {
                        attributesMap.put("@" + attr.getName(), attr.getValue());
                    }
                }
            }

            if (map.get(elem.getName()) != null) {
                val obj = map.get(elem.getName());
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

    private static class Map2XmlString {

        @SneakyThrows
        private static String document2XmlString(Document document, boolean prettyFormat) {
            val writer = new StringWriter();
            val format = prettyFormat ? createPrettyPrint() : createCompactFormat();
            val xmlWriter = new XMLWriter(writer, format);
            xmlWriter.write(document);
            xmlWriter.close();
            return writer.toString();
        }

        @SuppressWarnings("unchecked")
        private static void map2Element(Map<String, Object> map, Element body) {
            for (val entry : map.entrySet()) {
                val key = entry.getKey();
                val value = entry.getValue();
                if (key.startsWith("@")) { // 属性
                    body.addAttribute(key.substring(1, key.length()), value.toString());
                } else if (key.equals(TEXT)) { // 有属性时的文本
                    body.addCDATA(value.toString());
                } else {
                    if (value instanceof List) {
                        parseListElement(body, key, (List) value);
                    } else if (value instanceof Map) {
                        val subElement = body.addElement(key);
                        map2Element((Map) value, subElement);
                    } else {
                        body.addElement(key).addCDATA(value.toString());
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static void parseListElement(Element body, String key, List list) {
            for (val obj : list) {
                // list里是map或String，不会存在list里直接是list的，
                if (obj instanceof Map) {
                    val subElement = body.addElement(key);
                    map2Element((Map) obj, subElement);
                } else {
                    body.addElement(key).addCDATA((String) obj);
                }
            }
        }

    }

    @Getter
    public static class XmlParseFeature {

        private static final String DISALLOW_DOCTYPE_DECL_NAME
                = "http://apache.org/map2Element/features/disallow-doctype-decl";
        private static final String LOAD_EXTERNAL_DTD_NAME
                = "http://apache.org/map2Element/features/nonvalidating/load-external-dtd";
        private static final String EXTERNAL_GENERAL_ENTITIES_NAME
                = "http://map2Element.org/sax/features/external-general-entities";
        private static final String EXTERNAL_PARAMETER_ENTITIES_NAME
                = "http://map2Element.org/sax/features/external-parameter-entities";
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
