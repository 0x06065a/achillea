package ru.stereohorse.cinimex.achillea.model.formats;

import ru.stereohorse.cinimex.achillea.model.Csv;
import ru.stereohorse.cinimex.achillea.model.CsvEntity;
import ru.stereohorse.cinimex.achillea.model.XmlNode;

public class MappingStyleFormat extends CsvFormatter {
    private static final String HEADER = "Название поля;Тип поля;Тип;Обяз.;Комментарий";

    @Override
    public String format(Csv csv) {
        StringBuilder sb = new StringBuilder(HEADER).append(Csv.NEW_LINE);
        for (CsvEntity e : csv.getEntities()) {
            if (csv.getType(XmlNode.Tag.SECTION).equals(e.getFieldType())) {
                continue;
            }

            sb.append(concatFields(
                    e.getPath(), e.getFieldType(),
                    e.getXmlType(), e.getConstraint(), e.getComment()));
        }

        return sb.toString();
    }
}
