package io.jooby.helper;

import io.jooby.Extension;
import io.jooby.Jooby;
import kong.unirest.Unirest;

import javax.annotation.Nonnull;

public class UniRestExtension implements Extension {
    @Override
    public void install(@Nonnull Jooby jooby) {
        jooby.onStop(Unirest::shutDown);
    }
}
