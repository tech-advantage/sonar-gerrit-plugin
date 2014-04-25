package pl.touk.sonar;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

@Properties({
        @Property(key = PropertyKey.GERRIT_SCHEME, name = PropertyKey.GERRIT_SCHEME, defaultValue = "http", type = PropertyType.STRING),
        @Property(key = PropertyKey.GERRIT_HOST, name = PropertyKey.GERRIT_HOST),
        @Property(key = PropertyKey.GERRIT_HTTP_PORT, name = PropertyKey.GERRIT_HTTP_PORT, defaultValue = "80", type = PropertyType.INTEGER),
        @Property(key = PropertyKey.GERRIT_HTTP_USERNAME, name = PropertyKey.GERRIT_HTTP_USERNAME),
        @Property(key = PropertyKey.GERRIT_HTTP_PASSWORD , name = PropertyKey.GERRIT_HTTP_PASSWORD, type = PropertyType.PASSWORD),
        @Property(key = PropertyKey.GERRIT_BASE_URL, name = PropertyKey.GERRIT_BASE_URL, type = PropertyType.STRING),
        @Property(key = PropertyKey.GERRIT_LABEL, name = PropertyKey.GERRIT_LABEL, defaultValue = "Code-Review", type = PropertyType.STRING),
        @Property(key = PropertyKey.GERRIT_CHANGE_ID , name = PropertyKey.GERRIT_CHANGE_ID),
        @Property(key = PropertyKey.GERRIT_REVISION_ID , name = PropertyKey.GERRIT_REVISION_ID)
})

public final class GerritPlugin extends SonarPlugin {
    @SuppressWarnings("unchecked")
    public List<?> getExtensions() {
        return Arrays.asList(GerritDecorator.class);
    }
}
