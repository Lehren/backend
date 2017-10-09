package org.fsg1.fmms.backend.app;

import org.fsg1.fmms.backend.services.CurriculumService;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;

/**
 * The AppBinder. This resolves dependencies using Jersey's HK2 DI.
 */
public final class AppBinder extends AbstractBinder {

    /**
     * Configures dependencies. Dependencies can be shared in a Singleton, recreated for every request
     * and more. Read HK2's DI documentation.
     */
    @Override
    protected void configure() {
        bind(CurriculumService.class).in(Singleton.class);
    }
}
