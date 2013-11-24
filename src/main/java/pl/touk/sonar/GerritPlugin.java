package pl.touk.sonar;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

@Properties({
        @Property(key = PropertyKey.GERRIT_HOST, name = PropertyKey.GERRIT_HOST),
        @Property(key = PropertyKey.GERRIT_PORT, name = PropertyKey.GERRIT_PORT),
        @Property(key = PropertyKey.GERRIT_USERNAME, name = PropertyKey.GERRIT_USERNAME)
})

public final class GerritPlugin extends SonarPlugin {
    @SuppressWarnings("unchecked")
    public List<?> getExtensions() {
        return Arrays.asList(GerritDecorator.class);
    }
}