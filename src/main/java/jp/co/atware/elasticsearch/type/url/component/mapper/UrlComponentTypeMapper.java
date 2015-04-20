package jp.co.atware.elasticsearch.type.url.component.mapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
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

public class UrlComponentTypeMapper extends AbstractFieldMapper<Object> {

    public static final String RAW_FIELD = "raw";
    public static final String PROTOCOL_FIELD = "protocol";
    public static final String HOSTNAME_FIELD = "hostname";
    public static final String PATH_FIELD = "path";
    public static final String QUERY_FIELD = "query";

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());
    private final boolean hasHostName;
    private final Map<String, Mapper> mappers;

    public UrlComponentTypeMapper(Names names, Map<String, Mapper> mappers, boolean hasHostName) {
        super(names, 1.0f, Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null, null);
        this.mappers = mappers;
        this.hasHostName = hasHostName;
    }

    @Override
    protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {
        String fieldValue = extractFieldValue(context);
        if (fieldValue == null) {
            return;
        }
        parseComponentField(context, fieldValue, RAW_FIELD);
        URL url = toURL(fieldValue);
        if (url == null) {
            return;
        }
        if (hasHostName) {
            parseComponentField(context, url.getProtocol(), PROTOCOL_FIELD);
            parseComponentField(context, url.getHost(), HOSTNAME_FIELD);
        }
        parseComponentField(context, url.getPath(), PATH_FIELD);
        parseComponentField(context, url.getQuery(), QUERY_FIELD);
    }

    private String extractFieldValue(ParseContext context) throws IOException {
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

    private URL toURL(String url) {
        try {
            if (hasHostName) {
                return new URL(url);
            }
            StringBuilder sb = new StringBuilder("http://dummy_hostname");
            if (!url.startsWith("/")) {
                sb.append('/');
            }
            return new URL(sb.append(url).toString());
        } catch (MalformedURLException e) {
            logger.warn("Failed parse url", e);
            return null;
        }
    }

    private void parseComponentField(ParseContext context, Object value, String fieldName) throws IOException {
        if (value == null) {
            return;
        }
        Mapper mapper = mappers.get(fieldName);
        if (mapper == null) {
            // TODO parse by dynamic field
            return;
        }
        ParseContext newContext = context.createExternalValueContext(value);
        mapper.parse(newContext);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name());
        builder.field("type", contentType());
        builder.field("hasHostName", hasHostName);
        builder.startObject("fields");
        for (Mapper mapper : mappers.values()) {
            mapper.toXContent(builder, params);
        }
        builder.endObject();
        builder.endObject();
        return builder;
    }

    @Override
    public void close() {
        for (Mapper mapper : mappers.values()) {
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
    protected String contentType() {
        return "url_component";
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        for (Mapper mapper : mappers.values()) {
            mapper.traverse(fieldMapperListener);
        }
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
        for (Mapper mapper : mappers.values()) {
            mapper.traverse(objectMapperListener);
        }
    }
}
