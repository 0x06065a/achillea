package ru.stereohorse.cinimex.achillea.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Csv {
    private static final String ERR_FMT_NO_DECLARATION = "No declaration for type [%s] referenced at [%s:%d]";

    private static final String DEFAULT_MIN_OCCURS = "1";
    private static final String DEFAULT_MAX_OCCURS = "1";
    private static final String UNBOUNDED_XML = "unbounded";
    private static final String UNBOUNDED_CSV = "n";
    private static final String CONSTRAINT_FMT = "%s..%s";
    private static final String ATTR_CONSTRAINT = "0..1";

    private static final String NUMBER_FORMAT = "%s.%d";
    private static final String CHOICE_FORMAT = "%s(choice%d)";
    private static final String PATH_SEP = ".";

    public static final String NEW_LINE = String.format("%n");
    public static final String SEP = ";";

    private static final Map<String, String> TYPES = new HashMap<>();

    private final List<CsvEntity> entities = new ArrayList<>();

    static {
        TYPES.put(XmlNode.Tag.ATTRIBUTE, "атрибут");
        TYPES.put(XmlNode.Tag.ELEMENT, "элемент");
        TYPES.put(XmlNode.Tag.SECTION, "секция");
    }

    public Csv(XmlNode node) {
        parse(node, new ParsingData(), true);
    }

    private void parse(XmlNode currentNode, ParsingData currentData, boolean isRoot) {
        String currentTag = currentNode.getTag();

        CsvEntity currentEntity = new CsvEntity();
        currentEntity.setFieldType(TYPES.get(currentTag));
        currentEntity.setFieldName(currentNode.getAttribute(XmlNode.Attribute.NAME));

        ParsingData childData = new ParsingData(currentData);

        switch (currentTag) {
            case XmlNode.Tag.ELEMENT:
                currentData.getXmlTypeReceiver().setFieldType(TYPES.get(XmlNode.Tag.SECTION));
                currentData.setXmlTypeReceiver(currentEntity);

                childData.setXmlTypeReceiver(currentEntity);
                childData.setCommentsReceiver(currentEntity);

                currentEntity.setConstraint(getConstraint(currentNode));
                break;

            case XmlNode.Tag.ATTRIBUTE:
                currentData.setXmlTypeReceiver(currentEntity);
                childData.setCommentsReceiver(currentEntity);
                currentEntity.setConstraint(ATTR_CONSTRAINT);
                break;

            case XmlNode.Tag.DOCUMENTATION:
                currentData.getCommentsReceiver().setComment(currentNode.getFormattedTextValue());
                break;

            case XmlNode.Tag.CHOICE:
                childData.setNumber(new Number(currentData.getNumber()));
                childData.getNumber().setChoiceTrue();
                break;
        }

        if (currentEntity.getFieldType() != null && !isRoot) {
            entities.add(currentEntity);

            currentData.appendToPath(currentEntity.getFieldName());
            currentEntity.setPath(currentData.getPathStr());

            childData.setNumber(new Number(currentData.getNumber()));
            childData.setPath(new StringBuilder(currentData.getPath()));

            currentEntity.setNumber(currentData.getNumber().toString());
            currentData.getNumber().inc();
        }

        XmlNode referencedTypeNode = getType(currentNode);
        if (referencedTypeNode != null) {
            parse(referencedTypeNode, new ParsingData(childData), false);
        }

        for (XmlNode childNode : currentNode.getChildren()) {
            parse(childNode, new ParsingData(childData), false);
        }

        if (referencedTypeNode != null) {
            currentData.getXmlTypeReceiver().setXmlType(currentNode.getXmlLocalType());
        }
    }

    private String getConstraint(XmlNode child) {
        String minOccurs = child.getAttribute(XmlNode.Attribute.MIN_OCCURS, DEFAULT_MIN_OCCURS);
        String maxOccurs = child.getAttribute(XmlNode.Attribute.MAX_OCCURS, DEFAULT_MAX_OCCURS);
        maxOccurs = maxOccurs.replace(UNBOUNDED_XML, UNBOUNDED_CSV);

        return String.format(CONSTRAINT_FMT, minOccurs, maxOccurs);
    }

    private XmlNode getType(XmlNode node) {
        List<String> xmlTypes = node.getCalculatedXmlTypes();
        if (xmlTypes.isEmpty()) {
            return null;
        }

        XmlNode typeDeclaration = node.getSchema().getXmlType(xmlTypes);

        if (typeDeclaration == null) {
           throw new RuntimeException(String.format(ERR_FMT_NO_DECLARATION, node.getXmlType(),
                   node.getSchema().getFile().getAbsolutePath(), node.getLine()));
        }

        return typeDeclaration;
    }

    private class ParsingData {
        private CsvEntity xmlTypeReceiver = CsvEntity.VOID;
        private CsvEntity commentsReceiver = CsvEntity.VOID;
        private Number number = new Number();
        private StringBuilder path = new StringBuilder();

        public ParsingData() {
        }

        public ParsingData(ParsingData data) {
            xmlTypeReceiver = data.xmlTypeReceiver;
            commentsReceiver = data.commentsReceiver;
            number = data.number;
            path = new StringBuilder(data.path);
        }

        public CsvEntity getXmlTypeReceiver() {
            return xmlTypeReceiver;
        }

        public void setXmlTypeReceiver(CsvEntity typeReceiver) {
            this.xmlTypeReceiver = typeReceiver;
        }

        public CsvEntity getCommentsReceiver() {
            return commentsReceiver;
        }

        public void setCommentsReceiver(CsvEntity commentsReceiver) {
            this.commentsReceiver = commentsReceiver;
        }

        public Number getNumber() {
            return number;
        }

        public void setNumber(Number number) {
            this.number = number;
        }

        public StringBuilder getPath() {
            return path;
        }

        public void setPath(StringBuilder path) {
            this.path = path;
        }

        public String getPathStr() {
            return path.substring(1);
        }

        public void appendToPath(String pathElement) {
            path.append(PATH_SEP).append(pathElement);
        }
    }

    public class Number {
        private String prefix = "";
        private int counter = 1;
        private boolean isChoice;

        public Number() {
        }

        public Number(Number number) {
            prefix = number.toString();
            counter = 1;
        }

        @Override
        public String toString() {
            String str = String.format(isChoice ? CHOICE_FORMAT : NUMBER_FORMAT, prefix, counter);

            return str.replaceAll("^\\.*", "");
        }

        public void inc() {
            counter++;
        }

        public void setChoiceTrue() {
            isChoice = true;
        }
    }

    public List<CsvEntity> getEntities() {
        return entities;
    }

    public String getType(String xmlTag) {
        return TYPES.get(xmlTag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CsvEntity entity : entities) {
            sb.append(entity).append(NEW_LINE);
        }

        return sb.toString();
    }
}
