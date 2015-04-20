package jp.co.atware.elasticsearch.type.url.component.mapper;

import org.apache.lucene.document.FieldType;
import org.elasticsearch.index.mapper.FieldMapper.Names;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapper.BuilderContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper.Defaults;

public class UrlPathTypeMapperBuilder extends AbstractFieldMapper.Builder<UrlPathTypeMapperBuilder, UrlPathTypeMapper> {

    private final Mapper.Builder<?, ?> selfPathBuilder;
    private final Mapper.Builder<?, ?> parentPathBuilder;

    public UrlPathTypeMapperBuilder(String name, Mapper.Builder<?, ?> selfPathBuilder, Mapper.Builder<?, ?> parentPathBuilder) {
        super(name, new FieldType(Defaults.FIELD_TYPE));
        this.selfPathBuilder = selfPathBuilder;
        this.parentPathBuilder = parentPathBuilder;
    }

    @Override
    public UrlPathTypeMapper build(BuilderContext context) {
        context.path().add(name);
        Mapper selfPathMapper = this.selfPathBuilder.build(context);
        Mapper parentPathMapper = this.parentPathBuilder.build(context);
        context.path().remove();
        return new UrlPathTypeMapper(new Names(name), selfPathMapper, parentPathMapper);
    }
}
