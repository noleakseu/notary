package notary;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class SummaryBuilder extends Builder {
    public enum Language {
        en
    }

    private final Context ctx = new Context();
    private final Language language;
    private final List<Visit> visits = new LinkedList<>();

    public SummaryBuilder(Language language) {
        this.language = language;
    }

    @Override
    public SummaryBuilder append(Inspection... inspections) {
        Arrays.stream(inspections).forEach(inspection -> this.ctx.setVariable(inspection.getClass().getSimpleName(), inspection));
        return this;
    }

    @Override
    public SummaryBuilder append(Visit visit) {
        this.visits.add(visit);
        return this;
    }

    @Override
    public String build() {
        this.ctx.setVariable("title", Main.APP_TITLE);
        this.ctx.setVariable("version", Main.APP_VERSION);
        this.ctx.setVariable("vendor", Main.APP_VENDOR);
        this.ctx.setVariable("visits", this.visits);
        this.ctx.setVariable("deviceType", !this.visits.isEmpty() ? this.visits.get(0).getDeviceType() : null);
        this.ctx.setVariable("url", !this.visits.isEmpty() ? this.visits.get(0).getUrl() : null);
        this.ctx.setVariable("currentUrl", !this.visits.isEmpty() ? this.visits.get(0).getCurrentUrl() : null);
        this.ctx.setVariable("browserName", !this.visits.isEmpty() ? this.visits.get(0).getBrowserName() : null);
        this.ctx.setVariable("platformName", !this.visits.isEmpty() ? this.visits.get(0).getPlatformName() : null);
        this.ctx.setVariable("browserVersion", !this.visits.isEmpty() ? this.visits.get(0).getBrowserVersion() : null);

        final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        final TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        final Writer writer = new StringWriter();
        templateEngine.process("/templates/" + this.language.name() + ".html", this.ctx, writer);
        return writer.toString();
    }
}
