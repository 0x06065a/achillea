package ru.stereohorse.cinimex.achillea.model;


import com.google.common.base.Strings;

public class CsvEntity {
    public static final CsvEntity VOID = new CsvEntity();

    private String number;
    private String fieldName;
    private String fieldType;
    private String xmlType;
    private String constraint;
    private String comment;
    private String path;

    public void setNumber(String number) {
        this.number = number;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public void setXmlType(String xmlType) {
        this.xmlType = xmlType;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNumber() {
        return number;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getXmlType() {
        return xmlType;
    }

    public String getConstraint() {
        return constraint;
    }

    public String getComment() {
        return comment;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        String[] values = new String[] {number, fieldName, path, fieldType, xmlType, constraint, comment};
        StringBuilder sb = new StringBuilder();

        for (String value : values) {
            sb.append(Csv.SEP).append(Strings.nullToEmpty(value));
        }

        return sb.toString().substring(1);
    }
}
