package jp.co.atware.elasticsearch.type.url.component.module;

import jp.co.atware.elasticsearch.type.url.component.mapper.UrlComponentTypeMapperParser;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;

public class UrlComponentType extends AbstractIndexComponent {

    @Inject
    public UrlComponentType(Index index, @IndexSettings Settings indexSettings,
            MapperService mapperService) {
        super(index, indexSettings);
        mapperService.documentMapperParser().putTypeParser("url_component",
                new UrlComponentTypeMapperParser());
    }

}
