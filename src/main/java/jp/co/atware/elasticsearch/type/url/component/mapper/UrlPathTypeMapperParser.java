package jp.co.atware.elasticsearch.type.url.component.mapper;

import java.util.Map;

import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperBuilders;
import org.elasticsearch.index.mapper.Mapper.Builder;
import org.elasticsearch.index.mapper.Mapper.TypeParser;
import org.elasticsearch.index.mapper.MapperParsingException;

public class UrlPathTypeMapperParser implements Mapper.TypeParser {

    @Override
    public Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
        Map<String, Object> builderSettings = getValueAsMap(node, "fields");
        Builder<?, ?> selfBuilder = createFieldMapperBuilder(parserContext, "self", builderSettings);
        Builder<?, ?> parentBuilder = createFieldMapperBuilder(parserContext, "parents", builderSettings);
        return new UrlPathTypeMapperBuilder(name, selfBuilder, parentBuilder);
    }

    private Builder<?, ?> createFieldMapperBuilder(ParserContext parserContext, String fieldName, Map<String, Object> builderSettings) {
        if (builderSettings == null || builderSettings.get(fieldName) == null) {
            return MapperBuilders.stringField(fieldName);
        }
        Map<String, Object> builderSetting = getValueAsMap(builderSettings, fieldName);
        TypeParser parser = parserContext.typeParser((String) builderSetting.get("type"));
        return parser.parse(fieldName, builderSetting, parserContext);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getValueAsMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }
}
