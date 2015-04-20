package jp.co.atware.elasticsearch.type.url.component.mapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.mapper.FieldMapperListener;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MergeContext;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.ObjectMapperListener;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;

public class UrlQueryTypeMapper extends AbstractFieldMapper<Object> {

    private final StringFieldMapper queryNamesMapper;
    private final Map<String, Mapper> queryMappers;

    public UrlQueryTypeMapper(Names names, StringFieldMapper queryNamesMapper, Map<String, Mapper> queryMappers) {
        super(names, 1.0f, Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null, null);
        this.queryNamesMapper = queryNamesMapper;
        this.queryMappers = queryMappers;
    }

    private static final Pattern QUERIES_SPLITTER = Pattern.compile("&");
    private static final Pattern QUERY_SPLITTER = Pattern.compile("=");

    @Override
    protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {
        String queries = extractStringFieldValue(context);
        for (String query : QUERIES_SPLITTER.split(queries)) {
            String[] queryNameAndValue = QUERY_SPLITTER.split(query);
            parseQueryNameField(context, queryNameAndValue[0]);
            parseQueriesField(context, queryNameAndValue);
        }
    }

    private void parseQueryNameField(ParseContext context, String queryName) throws IOException {
        ParseContext extContext = context.createExternalValueContext(queryName);
        queryNamesMapper.parse(extContext);
    }

    private void parseQueriesField(ParseContext context, String[] queryNameAndValue) throws IOException {
        if (queryNameAndValue.length != 2) {
            return;
        }
        Mapper mapper = queryMappers.get(queryNameAndValue[0]);
        if (mapper != null) {
            String decodedQuery = URLDecoder.decode(queryNameAndValue[1], "UTF-8");
            ParseContext extContext = context.createExternalValueContext(decodedQuery);
            mapper.parse(extContext);
        }
    }

    private String extractStringFieldValue(ParseContext context) throws IOException {
        if (context.externalValueSet()) {
            return (String) context.externalValue();
        }
        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_STRING) {
            return parser.text();
        }
        return null;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(names.name());
        builder.field("type", contentType());
        builder.startObject("fields");
        Collection<Mapper> values = queryMappers.values();
        for (Mapper mapper : values) {
            mapper.toXContent(builder, params);
        }
        builder.endObject();
        return builder.endObject();
    }

    @Override
    protected String contentType() {
        return "url_query";
    }

    @Override
    public void close() {
        queryNamesMapper.close();
        for (Mapper mapper : queryMappers.values()) {
            mapper.close();
        }
    }

    @Override
    public Object value(Object value) {
        return null;
    }

    @Override
    public FieldType defaultFieldType() {
        return Defaults.FIELD_TYPE;
    }

    @Override
    public FieldDataType defaultFieldDataType() {
        return null;
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        queryNamesMapper.traverse(fieldMapperListener);
        for (Mapper mapper : queryMappers.values()) {
            mapper.traverse(fieldMapperListener);
        }
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
        queryNamesMapper.traverse(objectMapperListener);
        for (Mapper mapper : queryMappers.values()) {
            mapper.traverse(objectMapperListener);
        }
    }
}
