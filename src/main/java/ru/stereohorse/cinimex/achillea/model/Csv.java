package ru.stereohorse.cinimex.achillea.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Csv {
    private static final String HEADER = "Номер;Название поля;Тип поля;Тип;Обяз.;Комментарий";
    private static final String ERR_FMT_NO_DECLARATION = "No declaration for type [%s] referenced at [%s:%d]";

    private static final String DEFAULT_MIN_OCCURS = "1";
    private static final String DEFAULT_MAX_OCCURS = "1";
    private static final String UNBOUNDED_XML = "unbounded";
    private static final String UNBOUNDED_CSV = "n";

    private static final String NEW_LINE = String.format("%n");

    private static final Map<XmlTag, String> TYPES = new HashMap<>();

    private List<CsvEntity> entities = new ArrayList<>();

    static {
        TYPES.put(XmlTag.ATTRIBUTE, "атрибут");
        TYPES.put(XmlTag.ELEMENT, "элемент");
        TYPES.put(XmlTag.SECTION, "секция");
    }

    public Csv(XmlNode node) {
        parse(node, new ParsingData());
    }

    private void parse(XmlNode parent, ParsingData parsingData) {
        for (XmlNode node : parent.getChildren()) {
            XmlTag tag = node.getTag();

            if (tag.isComplexTypeDeclaration(parent)) {
                continue;
            }

            ParsingData childData = new ParsingData(parsingData);

            CsvEntity csvEntity = new CsvEntity();
            csvEntity.setFieldType(TYPES.get(tag));
            csvEntity.setFieldName(node.getAttribute(XmlAttribute.NAME));

            switch (tag) {
                case ELEMENT:
                    parsingData.getTypeReceiver().setFieldType(TYPES.get(XmlTag.SECTION));
                    childData.setTypeReceiver(csvEntity);
                    childData.setCommentsReceiver(csvEntity);
                    csvEntity.setConstraint(getConstraint(node));
                    break;

                case ATTRIBUTE:
                    childData.setCommentsReceiver(csvEntity);
                    break;

                case DOCUMENTATION:
                    parsingData.getCommentsReceiver().setComment(node.getFormattedTextValue());
                    break;

                case CHOICE:
                    // childData.setPrefix(childData.getPrefix() + "(choice)");
                    break;

                case EXTENSION:
                    // childData.setPrefix();
                    break;
            }

            if (csvEntity.getFieldType() != null) {
                entities.add(csvEntity);

                String number = String.format("%s%d.", parsingData.getPrefix(), parsingData.getCounter());
                csvEntity.setNumber(number.substring(0, number.length() - 1));

                parsingData.setCounter(parsingData.getCounter() + 1);

                childData.setPrefix(number);
                childData.setCounter(1);
            }

            XmlNode type = getType(node);
            if (type != null) {
                String typeName = node.getXmlLocalType();

                if (parsingData.getTypeReceiver().getXmlType() == null) {
                    parsingData.getTypeReceiver().setXmlType(typeName);
                } else {
                    csvEntity.setXmlType(typeName);
                }

                parse(type, childData);
            }

            parse(node, childData);
        }
    }

    private String getConstraint(XmlNode child) {
        String minOccurs = child.getAttribute(XmlAttribute.MIN_OCCURS, DEFAULT_MIN_OCCURS);
        String maxOccurs = child.getAttribute(XmlAttribute.MAX_OCCURS, DEFAULT_MAX_OCCURS);
        maxOccurs = maxOccurs.replace(UNBOUNDED_XML, UNBOUNDED_CSV);

        return String.format("%s..%s", minOccurs, maxOccurs);
    }

    private XmlNode getType(XmlNode node) {
        String xmlType = node.getCalculatedXmlType();
        if (xmlType == null) {
            return null;
        }

        XmlNode typeDeclaration = node.getSchema().getXmlType(xmlType);

        if (typeDeclaration == null) {
           throw new RuntimeException(String.format(ERR_FMT_NO_DECLARATION, node.getXmlType(),
                   node.getSchema().getFile().getAbsolutePath(), node.getLine()));
        }

        return typeDeclaration;
    }

    @Override
    public String toString() {
        StringBuilder sb =  new StringBuilder(HEADER).append(NEW_LINE);
        for (CsvEntity entity : entities) {
            sb.append(entity).append(NEW_LINE);
        }

        return sb.toString();
    }

    private class ParsingData {
        private CsvEntity typeReceiver = CsvEntity.DUMMY;
        private CsvEntity commentsReceiver = CsvEntity.DUMMY;
        private String prefix = "";
        private int counter = 1;

        public ParsingData() {}

        public ParsingData(ParsingData data) {
            typeReceiver = data.typeReceiver;
            commentsReceiver = data.commentsReceiver;
            prefix = data.prefix;
            counter = data.counter;
        }

        public CsvEntity getTypeReceiver() {
            return typeReceiver;
        }

        public void setTypeReceiver(CsvEntity typeReceiver) {
            this.typeReceiver = typeReceiver;
        }

        public CsvEntity getCommentsReceiver() {
            return commentsReceiver;
        }

        public void setCommentsReceiver(CsvEntity commentsReceiver) {
            this.commentsReceiver = commentsReceiver;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }
    }
}
