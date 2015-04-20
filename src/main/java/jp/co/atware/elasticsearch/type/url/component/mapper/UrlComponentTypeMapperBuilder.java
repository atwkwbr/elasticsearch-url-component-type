package jp.co.atware.elasticsearch.type.url.component.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.FieldType;
import org.elasticsearch.index.mapper.FieldMapper.Names;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapper.BuilderContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper.Defaults;

public class UrlComponentTypeMapperBuilder
        extends
        AbstractFieldMapper.Builder<UrlComponentTypeMapperBuilder, UrlComponentTypeMapper> {

    private final String fieldName;
    private final Map<String, Mapper.Builder<?, ?>> builders = new HashMap<>();
    private boolean hasHostName = true;
    
    public UrlComponentTypeMapperBuilder(String fieldName) {
        super(fieldName, new FieldType(Defaults.FIELD_TYPE));
        this.fieldName = fieldName;
    }
    
    public void addBuilder(String name, Mapper.Builder<?, ?> builder) {
        builders.put(name, builder);
    }

    public void setHasHostName(boolean hasHostName) {
        this.hasHostName = hasHostName;
    }
    
    @Override
    public UrlComponentTypeMapper build(BuilderContext context) {
        Map<String, Mapper> mappers = new HashMap<>();
        context.path().add(fieldName);
        for (Entry<String, Mapper.Builder<?, ?>> entry : builders.entrySet()) {
            String name = entry.getKey();
            Mapper.Builder<?, ?> builder = entry.getValue();
            mappers.put(name, builder.build(context));
        }
        context.path().remove();
        return new UrlComponentTypeMapper(new Names(fieldName), mappers, hasHostName);
    }

}
