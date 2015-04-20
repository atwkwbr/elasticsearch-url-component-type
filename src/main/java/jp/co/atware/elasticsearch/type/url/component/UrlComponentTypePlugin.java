package jp.co.atware.elasticsearch.type.url.component;

import static org.elasticsearch.common.collect.Lists.*;

import java.util.Collection;

import jp.co.atware.elasticsearch.type.url.component.module.UrlComponentTypeIndexModule;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

public class UrlComponentTypePlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "url-component-type";
    }

    public String description() {
        return "Url component type for analyze access log";
    }

    @Override
    public Collection<Class<? extends Module>> indexModules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(UrlComponentTypeIndexModule.class);
        return modules;
    }
}
