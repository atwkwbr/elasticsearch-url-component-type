package jp.co.atware.elasticsearch.type.url.component.mapper;

import java.io.IOException;
import java.util.List;

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

public class UrlPathTypeMapper extends AbstractFieldMapper<Object> {

    private final Mapper selfPathMapper;
    private final Mapper parentPathMapper;

    public UrlPathTypeMapper(Names names, Mapper pathMapper, Mapper parentPathMapper) {
        super(names, 1.0f, Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null, null);
        this.selfPathMapper = pathMapper;
        this.parentPathMapper = parentPathMapper;
    }

    @Override
    protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {
        String path = extractStringFieldValue(context);
        if (path == null) {
            return;
        }
        if (path.endsWith("/") && path.length() != 1) {
            path = path.substring(0, path.length() - 1);
        }
        context = context.createExternalValueContext(path);
        selfPathMapper.parse(context);
        String parentPath = path;
        while ((parentPath = parentPath(parentPath)) != null) {
            context = context.createExternalValueContext(parentPath);
            parentPathMapper.parse(context);
        }
    }

    private String parentPath(String path) {
        int idx = path.lastIndexOf("/");
        if (idx > 0) {
            // ignore root path
            return path.substring(0, idx);
        }
        return null;
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
        selfPathMapper.toXContent(builder, params);
        parentPathMapper.toXContent(builder, params);
        builder.endObject();
        builder.endObject();
        return builder;
    }

    @Override
    public void close() {
        selfPathMapper.close();
        parentPathMapper.close();
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
        return "url_path";
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        selfPathMapper.traverse(fieldMapperListener);
        parentPathMapper.traverse(fieldMapperListener);
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
        selfPathMapper.traverse(objectMapperListener);
        parentPathMapper.traverse(objectMapperListener);
    }
}
