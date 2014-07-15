package ru.stereohorse.cinimex.achillea.model.formats;

import ru.stereohorse.cinimex.achillea.model.Csv;
import ru.stereohorse.cinimex.achillea.model.CsvEntity;

public class FullFormat extends CsvFormatter {
    private static final String HEADER = "Номер;Название поля;Тип поля;Тип;Обяз.;Комментарий";

    @Override
    public String format(Csv csv) {
        StringBuilder sb = new StringBuilder(HEADER).append(Csv.NEW_LINE);
        for (CsvEntity e : csv.getEntities()) {
            sb.append(concatFields(
                    e.getNumber(), e.getFieldName(), e.getFieldType(),
                    e.getXmlType(), e.getConstraint(), e.getComment()));
        }

        return sb.toString();
    }
}
