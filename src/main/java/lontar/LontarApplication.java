package lontar;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication(scanBasePackages = {"lontar", "candi.runtime", "candi.auth.core", "candi.web", "candi.ui", "candi.data"})
@ImportRuntimeHints(LontarApplication.NativeHints.class)
public class LontarApplication {
    public static void main(String[] args) {
        SpringApplication.run(LontarApplication.class, args);
    }

    static class NativeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            try {
                hints.reflection().registerType(
                        Class.forName("org.hibernate.community.dialect.SQLiteDialect"),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("SQLiteDialect not found on classpath", e);
            }

            // RequestContext autowires HttpServletRequest (request-scoped proxy via AutowireUtils)
            hints.proxies().registerJdkProxy(
                    jakarta.servlet.http.HttpServletRequest.class);
        }
    }
}
