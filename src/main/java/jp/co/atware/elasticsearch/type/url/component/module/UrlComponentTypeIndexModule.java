package jp.co.atware.elasticsearch.type.url.component.module;

import org.elasticsearch.common.inject.Binder;
import org.elasticsearch.common.inject.Module;

public class UrlComponentTypeIndexModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(UrlComponentType.class).asEagerSingleton();
        binder.bind(UrlPathType.class).asEagerSingleton();
        binder.bind(UrlQueryType.class).asEagerSingleton();
    }
}
