package pl.touk.sonar;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

@Properties({
        @Property(key = PropertyKey.GERRIT_HOST, name = PropertyKey.GERRIT_HOST),
        @Property(key = PropertyKey.GERRIT_PORT, name = PropertyKey.GERRIT_PORT, defaultValue = "29418", type = PropertyType.INTEGER),
        @Property(key = PropertyKey.GERRIT_USERNAME, name = PropertyKey.GERRIT_USERNAME),
        @Property(key = PropertyKey.GERRIT_PASSWORD , name = PropertyKey.GERRIT_PASSWORD),
        @Property(key = PropertyKey.GERRIT_CHANGE_ID , name = PropertyKey.GERRIT_CHANGE_ID),
        @Property(key = PropertyKey.GERRIT_REVISION_ID , name = PropertyKey.GERRIT_REVISION_ID)
})

public final class GerritPlugin extends SonarPlugin {
    @SuppressWarnings("unchecked")
    public List<?> getExtensions() {
        return Arrays.asList(GerritDecorator.class);
    }
}