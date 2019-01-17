package com.github.charlemaznable.codec;

import lombok.Getter;
import lombok.SneakyThrows;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
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

import static com.github.charlemaznable.lang.Listt.isNotEmpty;
import static com.github.charlemaznable.lang.Listt.newArrayList;
import static com.github.charlemaznable.lang.Mapp.of;
import static org.dom4j.DocumentHelper.createDocument;
import static org.dom4j.DocumentHelper.createElement;
import static org.dom4j.io.OutputFormat.createCompactFormat;
import static org.dom4j.io.OutputFormat.createPrettyPrint;

public class Xml {

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
        Element rootElement = createElement(rootName);
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
        Document document = XmlString2Map.xmlString2Document(text, feature);
        Element rootElement = document.getRootElement();
        if (rootElement.elements().size() == 0 &&
                rootElement.attributes().size() == 0) return new LinkedHashMap<>();
        Map<String, Object> map = XmlString2Map.element2Map(rootElement, false);
        return rootAsTop ? of(rootElement.getName(), map) : map;
    }

    private static class XmlString2Map {

        @SneakyThrows
        public static Document xmlString2Document(String text, XmlParseFeature feature) {
            SAXReader reader = new SAXReader();
            feature.setSAXReaderFeatures(reader);

            String encoding = getEncoding(text);
            InputSource source = new InputSource(new StringReader(text));
            source.setEncoding(encoding);
            Document result = reader.read(source);
            if (result.getXMLEncoding() == null) {
                result.setXMLEncoding(encoding);
            }

            return result;
        }

        private static String getEncoding(String text) {
            String result = null;
            String xml = text.trim();
            if (xml.startsWith("<?map2Element")) {
                int end = xml.indexOf("?>");
                String sub = xml.substring(0, end);
                StringTokenizer tokens = new StringTokenizer(sub, " =\"'");

                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
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
            Map<String, Object> map = new LinkedHashMap<>();
            List<Element> elements = element.elements();

            List<Attribute> attributes = null;
            if (parseAttr) {
                attributes = element.attributes(); // 当前节点的所有属性的list
                for (Attribute attribute : attributes) {
                    map.put("@" + attribute.getName(), attribute.getValue());
                }
            }

            if (elements.size() > 0) {
                for (Element elem : elements) {
                    List mapList = newArrayList();

                    if (elem.elements().size() > 0) {
                        Map m = element2Map(elem, parseAttr);
                        if (map.get(elem.getName()) != null) {
                            Object obj = map.get(elem.getName());
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

                    } else {
                        boolean hasAttributes = false;
                        Map<String, Object> attributesMap = null;
                        if (parseAttr) {
                            List<Attribute> attrs = elem.attributes(); // 当前节点的所有属性的list
                            if (attrs.size() > 0) {
                                hasAttributes = true;
                                attributesMap = new LinkedHashMap<>();
                                for (Attribute attr : attrs) {
                                    attributesMap.put("@" + attr.getName(), attr.getValue());
                                }
                            }
                        }

                        if (map.get(elem.getName()) != null) {
                            Object obj = map.get(elem.getName());
                            if (!(obj instanceof List)) {
                                mapList = newArrayList();
                                mapList.add(obj);
                                if (parseAttr && hasAttributes) {
                                    attributesMap.put("#text", elem.getText());
                                    mapList.add(attributesMap);
                                } else {
                                    mapList.add(elem.getText());
                                }
                            }
                            if (obj instanceof List) {
                                mapList = (List) obj;
                                if (parseAttr && hasAttributes) {
                                    attributesMap.put("#text", elem.getText());
                                    mapList.add(attributesMap);
                                } else {
                                    mapList.add(elem.getText());
                                }
                            }
                            map.put(elem.getName(), mapList);

                        } else {
                            if (parseAttr && hasAttributes) {
                                attributesMap.put("#text", elem.getText());
                                map.put(elem.getName(), attributesMap);
                            } else {
                                map.put(elem.getName(), elem.getText());
                            }
                        }
                    }
                }
            } else {
                // 根节点的
                if (isNotEmpty(attributes)) {
                    map.put("#text", element.getText());
                } else {
                    map.put(element.getName(), element.getText());
                }
            }
            return map;
        }
    }

    private static class Map2XmlString {

        @SneakyThrows
        private static String document2XmlString(Document document, boolean prettyFormat) {
            StringWriter writer = new StringWriter();
            OutputFormat format = prettyFormat ? createPrettyPrint() : createCompactFormat();
            XMLWriter xmlWriter = new XMLWriter(writer, format);
            xmlWriter.write(document);
            xmlWriter.close();
            return writer.toString();
        }

        @SuppressWarnings("unchecked")
        private static void map2Element(Map<String, Object> map, Element body) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.startsWith("@")) { // 属性
                    body.addAttribute(key.substring(1, key.length()), value.toString());
                } else if (key.equals("#text")) { // 有属性时的文本
                    body.addCDATA(value.toString());
                } else {
                    if (value instanceof List) {
                        List list = (List) value;
                        for (Object obj : list) {
                            // list里是map或String，不会存在list里直接是list的，
                            if (obj instanceof Map) {
                                Element subElement = body.addElement(key);
                                map2Element((Map) obj, subElement);
                            } else {
                                body.addElement(key).addCDATA((String) obj);
                            }
                        }
                    } else if (value instanceof Map) {
                        Element subElement = body.addElement(key);
                        map2Element((Map) value, subElement);
                    } else {
                        body.addElement(key).addCDATA(value.toString());
                    }
                }
            }
        }
    }

    @Getter
    public static class XmlParseFeature {

        private static final String disallowDoctypeDeclName
                = "http://apache.org/map2Element/features/disallow-doctype-decl";
        private static final String loadExternalDTDName
                = "http://apache.org/map2Element/features/nonvalidating/load-external-dtd";
        private static final String externalGeneralEntitiesName
                = "http://map2Element.org/sax/features/external-general-entities";
        private static final String externalParameterEntitiesName
                = "http://map2Element.org/sax/features/external-parameter-entities";
        private boolean disallowDoctypeDecl = true;
        private boolean loadExternalDTD = false;
        private boolean externalGeneralEntities = false;
        private boolean externalParameterEntities = false;

        public void setSAXReaderFeatures(SAXReader reader) {
            try {
                reader.setFeature(disallowDoctypeDeclName, disallowDoctypeDecl);
                reader.setFeature(loadExternalDTDName, loadExternalDTD);
                reader.setFeature(externalGeneralEntitiesName, externalGeneralEntities);
                reader.setFeature(externalParameterEntitiesName, externalParameterEntities);
            } catch (SAXException ignored) {
            }
        }
    }
}
