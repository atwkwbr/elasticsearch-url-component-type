package jp.co.atware.elasticsearch.type.url.component.mapper;

import java.util.Map;

import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapper.Builder;
import org.elasticsearch.index.mapper.Mapper.TypeParser;
import org.elasticsearch.index.mapper.MapperParsingException;

public class UrlComponentTypeMapperParser implements Mapper.TypeParser {

    @Override
    public Builder<?, ?> parse(String name, Map<String, Object> node,
            ParserContext parserContext) throws MapperParsingException {
        UrlComponentTypeMapperBuilder builder = new UrlComponentTypeMapperBuilder(
                name);
        if (node.get("hasHostName") != null) {
            builder.setHasHostName((Boolean) node.get("hasHostName"));
        }
        Map<String, Object> fields = getValueAsMap(node, "fields");
        if (fields != null) {
            for (String key : fields.keySet()) {
                Map<String, Object> field = getValueAsMap(fields, key);
                String type = (String) field.get("type");
                TypeParser typeParser = parserContext.typeParser(type);
                builder.addBuilder(key,
                        typeParser.parse(key, field, parserContext));
            }
        }
        return builder;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getValueAsMap(Map<String, Object> map,
            String key) {
        return (Map<String, Object>) map.get(key);
    }
}
