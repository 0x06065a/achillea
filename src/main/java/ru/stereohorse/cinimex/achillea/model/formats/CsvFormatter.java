package ru.stereohorse.cinimex.achillea.model.formats;

import com.google.common.base.Strings;
import ru.stereohorse.cinimex.achillea.model.Csv;

public abstract class CsvFormatter {
    public abstract String format(Csv csv);

    protected String concatFields(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(Csv.SEP).append(Strings.nullToEmpty(field));
        }
        return sb.append(Csv.NEW_LINE).substring(1);
    }
}
