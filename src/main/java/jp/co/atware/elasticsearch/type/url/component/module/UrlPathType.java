package jp.co.atware.elasticsearch.type.url.component.module;

import jp.co.atware.elasticsearch.type.url.component.mapper.UrlPathTypeMapperParser;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;

public class UrlPathType extends AbstractIndexComponent {

    @Inject
    public UrlPathType(Index index, @IndexSettings Settings indexSettings,
            MapperService mapperService) {
        super(index, indexSettings);
        mapperService.documentMapperParser().putTypeParser("url_path",
                new UrlPathTypeMapperParser());
    }

}
