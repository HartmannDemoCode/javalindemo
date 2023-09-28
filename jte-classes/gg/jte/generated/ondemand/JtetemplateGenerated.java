package gg.jte.generated.ondemand;
public final class JtetemplateGenerated {
	public static final String JTE_NAME = "template.jte";
	public static final int[] JTE_LINE_INFO = {1,1,1,1,1,1};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor) {
		jteOutput.writeContent("<h1>Hello From Template World</h1>\n<p>This is a template for some html</p>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		render(jteOutput, jteHtmlInterceptor);
	}
}
