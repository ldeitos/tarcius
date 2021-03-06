package com.github.ldeitos.tarcius;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.ProducesAlternative;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.ldeitos.tarcius.audit.AuditContext;
import com.github.ldeitos.tarcius.audit.auditprocessor.AuditProcessorImpl;
import com.github.ldeitos.tarcius.audit.interceptor.AuditInterceptor;
import com.github.ldeitos.tarcius.audit.resolver.DefaultFormattedDateStringResolver;
import com.github.ldeitos.tarcius.audit.resolver.DefaultFormattedStringResolver;
import com.github.ldeitos.tarcius.audit.resolver.DefaultFormattedXMLResolver;
import com.github.ldeitos.tarcius.audit.resolver.DefaultJSONResolver;
import com.github.ldeitos.tarcius.audit.resolver.DefaultStringResolver;
import com.github.ldeitos.tarcius.audit.resolver.DefaultXMLResolver;
import com.github.ldeitos.tarcius.bootstrap.TarciusBootstrap;
import com.github.ldeitos.tarcius.configuration.ConfigInfoProvider;
import com.github.ldeitos.tarcius.configuration.Configuration;
import com.github.ldeitos.tarcius.producer.TarciusProducer;
import com.github.ldeitos.tarcius.support.CustomResolverImpl;
import com.github.ldeitos.tarcius.support.MessageDestination;
import com.github.ldeitos.tarcius.support.OutroTeste;
import com.github.ldeitos.tarcius.support.TestAuditDataDispatcher;
import com.github.ldeitos.tarcius.support.TestAuditDataFormatter;
import com.github.ldeitos.tarcius.support.Teste;
import com.github.ldeitos.tarcius.support.ToAudit;

@RunWith(CdiRunner.class)
@AdditionalClasses({ ToAudit.class, DefaultStringResolver.class, DefaultFormattedXMLResolver.class,
    DefaultJSONResolver.class, DefaultXMLResolver.class, DefaultFormattedXMLResolver.class,
    DefaultFormattedDateStringResolver.class, DefaultFormattedStringResolver.class, CustomResolverImpl.class,
    AuditContext.class, AuditInterceptor.class, TestAuditDataDispatcher.class, TestAuditDataFormatter.class,
    TarciusProducer.class, TarciusBootstrap.class, AuditProcessorImpl.class })
@InRequestScope
public class AuditTest {

	private static final String QUEBRA = System.getProperty("line.separator");

	@Inject
	private ToAudit test;

	@Produces
	@ProducesAlternative
	private ConfigInfoProvider configInfoProvier = new ConfigInfoProvider();

	@AfterClass
	public static void shutdown() {
		Configuration.reset();
	}

	@Test
	public void testAuditoriaSemParametroReferenciaNomeMetodo() {
		test.testRefMethodName();
		assertAudit("Método auditado: testRefMethodName");
	}

	@Test
	public void testAuditoriaSemParametroReferenciaEspecifica() {
		test.testDefinedRef();
		assertAudit("Método auditado: especificRef");
	}

	@Test
	public void testAuditoriaComParametroReferenciaEspecifica() {
		test.testParametrizedMethodDefinedRef("valorParametro");
		assertAudit("Método auditado: parametrized");
	}

	@Test
	public void testAuditoriaComParametroReferenciaNomeMetodo() {
		test.testParametrizedMethod("parametrized");
		assertAudit("Método auditado: testParametrizedMethod");
	}

	@Test
	public void testAuditoriaComParametroStringAuditado() {
		test.testStringParam("valorParametro");
		assertAudit("Método auditado: parameterTest" + QUEBRA + "Parâmetro auditado: parName" + QUEBRA
		    + "Valor: valorParametro");
	}

	@Test
	public void testAuditoriaComParametroStringNumericoAuditado() {
		test.testStringIntParam("valorParametro", 10);
		assertAudit("Método auditado: parameterTest2" + QUEBRA + "Parâmetro auditado: par1" + QUEBRA
		    + "Valor: valorParametro" + QUEBRA + "Parâmetro auditado: par2" + QUEBRA + "Valor: 10");
	}

	@Test
	public void testAuditoriaComParametroDataNumericoAuditadoEFormatados() {
		Date hoje = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dataFormatada = sdf.format(hoje);
		test.testFormattedDateIntParam(hoje, 100);
		assertAudit("Método auditado: parameterTest" + QUEBRA + "Parâmetro auditado: par1" + QUEBRA
		    + "Valor: " + dataFormatada + QUEBRA + "Parâmetro auditado: par2" + QUEBRA + "Valor: 00100");
	}

	@Test
	public void testAuditoriaComParametroXML() {
		test.testXML(new Teste("valPar"));
		assertAudit("Método auditado: parameterTest" + QUEBRA + "Parâmetro auditado: xmlPar" + QUEBRA
		    + "Valor: <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
		    + "<teste><field>valPar</field></teste>");
	}

	@Test
	public void testAuditoriaComParametroXMLFormatado() {
		test.testFormattedXML(new Teste("valPar"));
		assertAudit("Método auditado: parameterTest" + QUEBRA + "Parâmetro auditado: xmlPar" + QUEBRA
		    + "Valor: <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<teste>\n"
		    + "    <field>valPar</field>\n</teste>\n");
	}

	@Test
	public void testAuditoriaComParametroJSON() {
		test.testJSON(new Teste("valPar"));
		assertAudit("Método auditado: parameterTest" + QUEBRA + "Parâmetro auditado: jsonPar" + QUEBRA
		    + "Valor: {\"field\":\"valPar\"}");
	}

	@Test
	public void testAuditoriaCustomResolver() {
		test.testCustomResolver(new Teste("valPar"));
		assertAudit("Método auditado: parameterTest" + QUEBRA + "Parâmetro auditado: custom" + QUEBRA
		    + "Valor: CustomResolver: [valPar]");
	}

	@Test
	public void testAuditoriaParametroAnotadoEntidade() {
		test.testEntityAnnotation(new OutroTeste());
		assertAudit("Método auditado: parameterTest" + QUEBRA + "Parâmetro auditado: entity" + QUEBRA
		    + "Valor: teste anotação entidade.");
	}

	@Test
	public void testAuditoriaParametroAnotadoEntidadeIgnorado() {
		test.testIgnoredEntityAnnotation(new OutroTeste());
		assertAudit("Método auditado: parameterTest");
	}

	private void assertAudit(String string) {
		assertEquals(string, MessageDestination.getMessage());
	}
}
