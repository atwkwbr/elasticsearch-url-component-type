package jp.co.atware.elasticsearch.type.url.component.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.FieldType;
import org.elasticsearch.index.mapper.FieldMapper.Names;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperBuilders;
import org.elasticsearch.index.mapper.Mapper.Builder;
import org.elasticsearch.index.mapper.Mapper.BuilderContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper.Defaults;
import org.elasticsearch.index.mapper.core.StringFieldMapper;

public class UrlQueryTypeMapperBuilder extends AbstractFieldMapper.Builder<UrlQueryTypeMapperBuilder, UrlQueryTypeMapper> {

    private final String fieldName;
    private final StringFieldMapper.Builder queryNamesMapperBuilder;
    private final Map<String, Mapper.Builder<?, ?>> queryMapperBuilders = new HashMap<>();

    public UrlQueryTypeMapperBuilder(String name) {
        super(name, new FieldType(Defaults.FIELD_TYPE));
        this.queryNamesMapperBuilder = MapperBuilders.stringField("queryNames");
        this.fieldName = name;
    }

    public void putQueryMapperBuilder(String queryName, Mapper.Builder<?, ?> mapper) {
        this.queryMapperBuilders.put(queryName, mapper);
    }

    @Override
    public UrlQueryTypeMapper build(BuilderContext context) {
        Map<String, Mapper> queryMappers = new HashMap<>();
        context.path().add(fieldName);
        StringFieldMapper queryNamesMapper = queryNamesMapperBuilder.build(context);
        for (Entry<String, Builder<?, ?>> entry : queryMapperBuilders.entrySet()) {
            String queryName = entry.getKey();
            Builder<?, ?> mapperBuilder = entry.getValue();
            Mapper queryMapper = mapperBuilder.build(context);
            queryMappers.put(queryName, queryMapper);
        }
        context.path().remove();
        return new UrlQueryTypeMapper(new Names(fieldName), queryNamesMapper, queryMappers);
    }
}
