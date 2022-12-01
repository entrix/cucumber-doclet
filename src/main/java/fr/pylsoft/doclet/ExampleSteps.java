package fr.pylsoft.doclet;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Class comment (@see the class)
 */
public class ExampleSteps {

	@FindBy(how = How.ID, using = "example_button")
	private Objects exampleButton;

	private Scenario scenario;

	/**
	 * Field comment
	 */
	private static SomeData someData;

	private static Map<String, String> exampleStringMap = new HashMap<String, String>();

	@Before
	public void before(Scenario scenario) {
		this.scenario = scenario;
	}

	/**
	 *
	 * @param withoutErrors
	 */
	@Given("^<X>hanges are overdue(| without errors)$")
	public void changesAreOverdue(String withoutErrors) {
		throw new RuntimeException("Something was imported with error(s)");
	}

	@Given("^\"([^\"]*)\"(| matching) something is set for \"([^\"]*)\"$")
	public void setCodeForSomeone(String exampleCode, String isMatching) {
		if (isMatching.isEmpty()) {
			exampleStringMap.put(exampleCode, exampleCode + "-" + System.currentTimeMillis() % 1000);
		}
		exampleCode = exampleStringMap.containsKey(exampleCode) ? exampleStringMap.get(exampleCode) : exampleCode;
	}

	@Then("^[Pp]lan \"([^\"]+)\" and related data for \"([A-Za-z]+)\" is cached$")
	public void someDataForInterest(String planName) {
		someData = new SomeData(planName);
	}

	@Then("^[Pp]lan \"([^\"]+)\" and example for \"([A-Za-z]+)\" is mapped to that of \"([A-Za-z]+)\"'s$")
	public void tryMappings(String planName) {

		SomeData sourceData = someData;

		tryUseMappings(sourceData, planName);
	}

	@Then("^\"([^\"]*)\" warning message here$")
	public void checkWarningMessage(String message) {
	}

	private void tryUseMappings(SomeData sourceData, String string) {
	}

	/**
	 * Some comment here just for interest
	 */
	private class SomeData {
		String name;
		Set<Integer> planPeriodIds;
		Map<Integer, Set<Integer>> exampleSetMap;
		Map<Integer, Integer> exampleMap;

		SomeData(String name) {
			this.name = name;
		}
	}
}
